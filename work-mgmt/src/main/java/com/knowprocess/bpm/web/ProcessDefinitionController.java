/*******************************************************************************
 *Copyright 2011-2018 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.knowprocess.bpm.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerConfigurationException;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.knowprocess.bpm.api.ReportableException;
import com.knowprocess.bpm.model.Deployment;
import com.knowprocess.bpm.model.ModelIssue;
import com.knowprocess.bpm.model.ProcessDefinition;
import com.knowprocess.bpm.model.ProcessInstance;
import com.knowprocess.bpm.model.ProcessModel;
import com.knowprocess.bpm.repositories.ProcessModelRepository;
import com.knowprocess.deployment.ProcessDefiner;
import com.knowprocess.xslt.TransformTask;

@RequestMapping("/{tenantId}/process-definitions")
@Controller
public class ProcessDefinitionController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(ProcessDefinitionController.class);

    private static final String MSG_INTROSPECTOR_RESOURCES = "/static/xslt/bpmn2msgs.xslt";

    private static final String DIAG_INTROSPECTOR_RESOURCES = "/static/xslt/bpmn2diags.xslt";

    private static final String RENDERER_RESOURCES = "/static/xslt/bpmn2svg.xslt";

    @Autowired
    protected ProcessEngine processEngine;

    @Autowired
    private ProcessModelRepository processModelRepo;

    private TransformTask messageIntrospector;

    private TransformTask diagramIntrospector;

    private static TransformTask renderer;

    @RequestMapping(value = "/", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody ProcessDefinition define(
            @PathVariable String tenantId,
            @RequestBody ProcessDefinition defn) {

        try {
            ProcessDefiner definer = new ProcessDefiner();
            String bpmn = new String(definer.convertToBpmn(
                    defn.getProcessText(), defn.getName(), "UTF-8"));
            defn.setBpmn(bpmn);
            defn.setSvgImage(getProcessRenderer().transform(bpmn));
            return defn;
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Failed to create process definition", e);
            throw new RuntimeException();
        }
    }

    @RequestMapping(value = "", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody List<ProcessDefinition> showAllJson(
            @PathVariable("tenantId") String tenantId) {
        LOGGER.info(String.format("showAllJson for %1$s", tenantId));

        List<ProcessDefinition> list = ProcessDefinition
                .findAllProcessDefinitions(tenantId);
        for (ProcessDefinition defn : list) {
            defn.setInstanceCount(getInstanceCount(defn, tenantId));
        }
        LOGGER.info("Deployed definitions: " + list.size());

        List<ProcessModel> incompleteModels = processModelRepo
                .findAllForTenant(tenantId);
        LOGGER.info("Incomplete definitions: " + incompleteModels.size());
        for (ProcessModel processModel : incompleteModels) {
            list.add(new ProcessDefinition(processModel));
        }

        LOGGER.info("Total definitions: " + list.size());
        return list;
    }

    protected Long getInstanceCount(ProcessDefinition defn, String tenantId) {
        // Historic count _includes_ active
        long count = processEngine.getHistoryService()
                .createHistoricProcessInstanceQuery()
                .processInstanceTenantId(tenantId)
                .processDefinitionId(defn.getId())
                .count();
        if (count == 0) {
            // double check, could be history has been trimmed
            count = processEngine.getRuntimeService()
                .createProcessInstanceQuery()
                .processInstanceTenantId(tenantId)
                .processDefinitionId(defn.getId())
                .count();
        }
        return count;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody ProcessDefinition showJson(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id) {
        LOGGER.info(String.format("%1$s definition with %2$s",
                RequestMethod.GET, id));

        ProcessDefinition pd = null;
        try {
            pd = ProcessDefinition.findProcessDefinition(id);

            // TODO why no API to get event subscriptions of proc def
            pd.setBpmn(getBpmn(id));
            String[] msgNames = getMessageIntrospector().transform(
                    pd.getBpmn()).split(",");
            for (String name : msgNames) {
                if (name != null && name.length() > 0) {
                    pd.addMessageName(name);
                }
            }

            pd.setDeployment(new Deployment(processEngine
                    .getRepositoryService().createDeploymentQuery()
                    .deploymentId(pd.getDeploymentId().toString())
                    .singleResult()));
            pd.setInstanceCount(getInstanceCount(pd, tenantId));
        } catch (NullPointerException e) {
            // assume this is an incomplete model....
            pd = new ProcessDefinition(processModelRepo.findOne(id));
        }
        pd.setMd5Hash(Md5HashUtils.getHash(pd.getBpmn()));
        pd.setDiagramIds(Arrays.asList(getDiagramIntrospector().transform(pd.getBpmn()).split(",")));
        return pd;
    }

    @RequestMapping(value = "/{id}/activate", method = RequestMethod.POST, headers = "Accept=application/json")
    public @ResponseBody void activate(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id) {
        LOGGER.info(String.format("activate %1$s definition", id));

        org.activiti.engine.repository.ProcessDefinition pd = processEngine
                .getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionId(id).singleResult();
        if (pd == null) {
            throw new IllegalArgumentException(String.format(
                    "Process definition %1$s not found", id));
        } else if (!pd.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException(
                    String.format(
                            "Process definition %1$s does not belong to %1$s",
                            tenantId));
        } else {
            processEngine.getRepositoryService().activateProcessDefinitionById(
                    id);
        }
    }

    @RequestMapping(value = "/{id}/suspend", method = RequestMethod.POST, headers = "Accept=application/json")
    public @ResponseBody void suspend(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id) {
        LOGGER.info(String.format("suspend %1$s definition", id));

        org.activiti.engine.repository.ProcessDefinition pd = processEngine
                .getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionId(id).singleResult();
        if (pd == null) {
            throw new IllegalArgumentException(String.format(
                    "Process definition %1$s not found", id));
        } else if (!pd.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException(
                    String.format(
                            "Process definition %1$s does not belong to %1$s",
                            tenantId));
        } else {
            processEngine.getRepositoryService().suspendProcessDefinitionById(
                    id);
        }
    }

    @RequestMapping(value = "/{id}/instances", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody List<ProcessInstance> showInstancesJson(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info(String.format("%1$s instances for definition %2$s",
                RequestMethod.GET, id));

        List<ProcessInstance> instances = new ArrayList<ProcessInstance>();

        HistoricProcessInstanceQuery query = processEngine
                .getHistoryService().createHistoricProcessInstanceQuery()
                .processDefinitionId(id).orderByProcessInstanceId().desc();
        if (limit == null) {
            instances.addAll(ProcessInstance.wrap(query.list()));
            mergeActiveInstDetails(id, instances);
        } else {
            if (page == null) {
                page = 0;
            }
            instances.addAll(ProcessInstance.wrap(query.listPage(page*limit, limit)));
            mergeActiveInstDetails(id, instances);
        }
        // double check in case simply missing historic data
        if (instances.size() == 0 && limit == null) {
            instances.addAll(ProcessInstance.wrap(
                    processEngine.getRuntimeService().createProcessInstanceQuery()
                    .processDefinitionId(id).orderByProcessInstanceId().desc().list()));
        } else if (instances.size() == 0) {
            instances.addAll(ProcessInstance.wrap(
                    processEngine.getRuntimeService().createProcessInstanceQuery()
                    .processDefinitionId(id).orderByProcessInstanceId().desc().listPage(page*limit, limit)));
        }
        return instances;
    }

    private void mergeActiveInstDetails(String procDefId,
            List<ProcessInstance> instances) {
        if (instances == null || instances.size() == 0) {
            LOGGER.debug("no instances to enhance");
            return;
        }
        ProcessInstanceQuery activeQuery = processEngine.getRuntimeService()
                .createProcessInstanceQuery().processDefinitionId(procDefId)
                .orderByProcessInstanceId().desc();

        Set<String> instanceIds = new HashSet<String>();
        for (ProcessInstance auditInst : instances) {
            if (!auditInst.getEnded()) {
                instanceIds.add(auditInst.getId());
            }
        }
        LOGGER.debug("Audit instance ids for merging: {}", instanceIds);
        if (instanceIds.size() > 0) {
            List<org.activiti.engine.runtime.ProcessInstance> activeList = activeQuery
                    .processDefinitionIds(instanceIds).list();
            LOGGER.debug("Active instances found: {}", activeList.size());
            for (ProcessInstance auditInst : instances) {
                if (!auditInst.getEnded()) {
                    LOGGER.debug("Unfinished instance {}, seeking merge...",
                            auditInst.getId());
                    for (org.activiti.engine.runtime.ProcessInstance activeInst : activeList) {
                        LOGGER.debug("Active instance {}, needed?",
                                activeInst.getId());
                        if (auditInst.getId().equals(activeInst.getId())) {
                            LOGGER.debug("FOUND: {} {}", activeInst.getId(),
                                    activeInst.getActivityId());
                            auditInst.setActivityId(activeInst.getActivityId());
                        }
                    }
                }
            }
        }
    }

    @RequestMapping(value = "/{id}/issues", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody List<ModelIssue> showIssuesJson(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id) {
        LOGGER.info(String.format("%1$s definition with %2$s",
                RequestMethod.GET, id));

        return processModelRepo.findOne(id).getIssues();
    }

    @RequestMapping(value = "/{id}.bpmn", method = RequestMethod.GET, produces = "application/xml")
    public @ResponseBody String showBpmn(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id) {
        LOGGER.info(String.format("%1$s BPMN with id %2$s", RequestMethod.GET,
                id));

        return getBpmn(id);
    }

    @RequestMapping(value = "/{id}/{diagramId}.png", method = RequestMethod.GET, produces = "image/png")
    public @ResponseBody byte[] showBpmnDiagram(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id,
            @PathVariable("diagramId") String diagramId) throws IOException {
        LOGGER.info(
                String.format("%1$s image of BPMN with id %2$s, diagram: %3$s",
                RequestMethod.GET, id, diagramId));

        return svgToPng(getBpmnDiagramAsSvg(tenantId, id, diagramId));
    }

    protected byte[] svgToPng(String bpmnDiagramAsSvg) {
        InputStream svgFileStream = null;
        try {
            svgFileStream = new ByteArrayInputStream(bpmnDiagramAsSvg.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            svgFileStream = new ByteArrayInputStream(bpmnDiagramAsSvg.getBytes());
            LOGGER.warn("Could not get StringBytes in UTF-8");
        }
        TranscoderInput inputSvgImage = new TranscoderInput(svgFileStream);
        PNGTranscoder converter = new PNGTranscoder();
        // TODO This appears to have no effect
//        converter.addTranscodingHint(PNGTranscoder.KEY_FORCE_TRANSPARENT_WHITE, true);
        ByteArrayOutputStream pngStream = new ByteArrayOutputStream();
        TranscoderOutput outputPngImage = new TranscoderOutput(pngStream);

        try {
            converter.transcode(inputSvgImage, outputPngImage);
        } catch (TranscoderException e) {
            String msg = "Error while converting SVG to PNG";
            LOGGER.error(msg, e);
            throw new RuntimeException(msg, e);
        }
        return pngStream.toByteArray();
    }

    @RequestMapping(value = "/{id}/{diagramId}.svg", method = RequestMethod.GET, produces = "image/svg+xml")
    public @ResponseBody byte[] showBpmnDiagramAsSvg(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id,
            @PathVariable("diagramId") String diagramId) throws IOException {
        return getBpmnDiagramAsSvg(tenantId, id, diagramId).getBytes();
    }

    @RequestMapping(value = "/", method = RequestMethod.DELETE)
    public @ResponseBody void deleteObsolete(
            @PathVariable("tenantId") String tenantId) {
        LOGGER.info(String.format("deleting obsolete definitions for %1$s", tenantId));
        List<org.activiti.engine.repository.ProcessDefinition> defs = processEngine
                .getRepositoryService()
                .createProcessDefinitionQuery()
                .processDefinitionTenantId(tenantId)
                .list();
        for (org.activiti.engine.repository.ProcessDefinition def : defs) {
            deleteObsoleteByKey(tenantId, def.getKey());
        }
    }

    @RequestMapping(value = "/{key}", method = RequestMethod.DELETE)
    public @ResponseBody void deleteObsoleteByKey(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("key") String key) {
        LOGGER.info(String.format("Deleting obsolete versions of: %1$s for %2$s", key, tenantId));
        try {
            org.activiti.engine.repository.ProcessDefinition latest = processEngine
                    .getRepositoryService().createProcessDefinitionQuery()
                    .processDefinitionKey(key)
                    .processDefinitionTenantId(tenantId)
                    .latestVersion().singleResult();
            List<org.activiti.engine.repository.ProcessDefinition> defs = processEngine
                    .getRepositoryService()
                    .createProcessDefinitionQuery()
                    .processDefinitionTenantId(tenantId)
                    .processDefinitionKey(key).list();
            for (org.activiti.engine.repository.ProcessDefinition def : defs) {
                if (def.getVersion() == latest.getVersion()) {
                    continue;
                }
                long count = processEngine.getRuntimeService()
                        .createProcessInstanceQuery()
                        .processDefinitionId(def.getId()).count();
                if (count == 0) {
                    LOGGER.info("Deleting obsolete process {} for {}",
                            def.getId(), def.getTenantId());
                    processEngine.getRepositoryService()
                            .deleteDeployment(def.getDeploymentId());
                }
            }
        } catch (Exception e) {
            String msg = String.format("Unable to delete definition with key %1$s, does it still have instances?", key);
            LOGGER.error(msg, e);
            throw new ReportableException(msg, e);
        }
    }

    protected String getBpmnDiagramAsSvg(String tenantId, String id,
            String diagramId) throws IOException {
        LOGGER.info(String.format("Get BPMN with id %1$s, diagram: %2$s", id,
                diagramId));

        Map<String, String> params = new HashMap<String, String>();
        params.put("diagramId", diagramId);
        return getProcessRenderer().transform(getBpmn(id), params);
    }

    protected String getBpmn(String id) {
        String bpmn = null;
        try {
            bpmn = ProcessDefinition.findProcessDefinitionAsBpmn(id);
        } catch (ActivitiException e) {
            // Activiti recognises the deployment but has not got the BPMN
            // Someone messing with the database?
            throw e;
        } catch (Exception e) {
            bpmn = processModelRepo.findOne(id).getBpmnString();
        }
        return bpmn;
    }

    protected TransformTask getMessageIntrospector() {
        if (messageIntrospector == null) {
            messageIntrospector = new TransformTask();
            try {
                messageIntrospector
                        .setXsltResources(MSG_INTROSPECTOR_RESOURCES);
            } catch (TransformerConfigurationException e) {
                LOGGER.error(
                        String.format(
                                "Unable to locate messageIntrospector pre-processors: %1$s",
                                RENDERER_RESOURCES), e);
            }
        }
        return messageIntrospector;
    }

    protected TransformTask getDiagramIntrospector() {
        if (diagramIntrospector == null) {
            diagramIntrospector = new TransformTask();
            try {
                diagramIntrospector
                        .setXsltResources(DIAG_INTROSPECTOR_RESOURCES);
            } catch (TransformerConfigurationException e) {
                LOGGER.error(
                        String.format(
                                "Unable to locate diagramIntrospector pre-processors: %1$s",
                                RENDERER_RESOURCES), e);
            }
        }
        return diagramIntrospector;
    }

    protected TransformTask getProcessRenderer() {
        if (renderer == null) {
            renderer = new TransformTask();
            try {
                renderer.setXsltResources(RENDERER_RESOURCES);
            } catch (TransformerConfigurationException e) {
                LOGGER.error(String.format(
                        "Unable to locate renderer pre-processors: %1$s",
                        RENDERER_RESOURCES), e);
            }
        }
        return renderer;
    }

}
