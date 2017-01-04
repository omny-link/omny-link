package com.knowprocess.bpm.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.TransformerConfigurationException;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.repository.DeploymentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.knowprocess.bpm.api.UnsupportedBpmnException;
import com.knowprocess.bpm.model.Deployment;
import com.knowprocess.bpm.model.ProcessModel;
import com.knowprocess.bpm.repositories.ProcessModelRepository;
import com.knowprocess.xslt.TransformTask;

@Controller
@RequestMapping("/{tenantId}/deployments")
public class DeploymentController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(DeploymentController.class);

    private static final String PREPROCESSOR_RESOURCES = "/xslt/ExecutableTweaker.xsl";

    private static final String VALIDATOR_RESOURCES = "/xslt/KpSupportRules.xsl";

    /**
     * Set true to make verbose debug level logging.
     */
    protected static boolean verbose;

    @Autowired(required = true)
    ProcessEngine processEngine;

    @Autowired
    private ProcessModelRepository processModelRepo;

    private TransformTask preProcessor;

    private TransformTask validator;

    @RequestMapping(value = "/", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody List<Deployment> showAllJson(
            @PathVariable("tenantId") String tenantId) {
        LOGGER.info(String.format("showAllJson(%1$s)", tenantId));

        List<Deployment> list = Deployment.findAllDeployments(tenantId);
        LOGGER.info("Deployments: " + list.size());
        return list;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody Deployment showJson(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id) {
        LOGGER.info(String.format("showJson %1$s for tenant %2$s", id, tenantId));

        Deployment deployment = Deployment.findDeployment(id);
        return deployment;
    }

    @RequestMapping(value = "/{resource}/", method = RequestMethod.POST, headers = "Accept=application/json")
    public final @ResponseBody org.activiti.engine.repository.Deployment deployFromClasspath(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("resource") String resource,
            @RequestParam(required = false) String deploymentName)
            throws UnsupportedEncodingException, IOException,
            UnsupportedBpmnException {

        org.activiti.engine.repository.Deployment deployment = null;

        LOGGER.debug(String.format("Deploy %1$s with name %2$s", resource,
                deploymentName));
        resource = resource.replace('.', '/') + ".bpmn";

        DeploymentBuilder builder = processEngine.getRepositoryService()
                .createDeployment();
        builder.tenantId(tenantId);
        if (deploymentName != null) {
            builder.name(deploymentName);
        }
        builder.addClasspathResource(resource);
        builder.deploy();

        return deployment;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/x-www-form-urlencoded", headers = "Accept=application/json")
    public final @ResponseBody org.activiti.engine.repository.Deployment deploy(
            @RequestParam String tenantId, @RequestParam String name,
            @RequestParam String bpmn) throws UnsupportedEncodingException,
            IOException, UnsupportedBpmnException {
        org.activiti.engine.repository.Deployment deployment = null;

        LOGGER.debug(String.format("deploymentName: %1$s", name));

        DeploymentBuilder builder = processEngine.getRepositoryService()
                .createDeployment().name(name).tenantId(tenantId);

        final Map<String, String> processes = new HashMap<String, String>();
        processes.put(name, bpmn);

        try {
            for (Entry<String, String> entry : runExecutableTweaker(processes)
                    .entrySet()) {
                builder.addString(entry.getKey() + ".bpmn", entry.getValue());
            }
            deployment = builder.deploy();

            LOGGER.info(String.format(
                    "Completed deployment: %1$s(%2$s) at %3$s", deployment
                            .getName(), deployment.getId(), deployment
                            .getDeploymentTime().toString()));
            return deployment;
        } catch (ActivitiException e) {
            LOGGER.warn(String
                    .format("Processes rejected for execution, continue as non-executable. Reason: %1$s",
                            e.getMessage()));
            handleIncompleteModel(tenantId, processes);
            return null;
        }

    }

    @RequestMapping(method = RequestMethod.POST, consumes = "multipart/form-data", headers = "Accept=application/json")
    public final @ResponseBody org.activiti.engine.repository.Deployment uploadMultipleFiles(
            UriComponentsBuilder uriBuilder, @RequestParam String tenantId,
            @RequestParam(required = false) String deploymentName,
            @RequestParam MultipartFile... resourceFile)
            throws UnsupportedEncodingException, IOException,
            UnsupportedBpmnException {
        org.activiti.engine.repository.Deployment deployment = null;

        LOGGER.debug(String.format("deploymentName: %1$s", deploymentName));
        LOGGER.debug(String.format("# of resources: %1$s", resourceFile.length));

        DeploymentBuilder builder = processEngine.getRepositoryService()
                .createDeployment();
        if (deploymentName != null) {
            builder.name(deploymentName);
        }
        if (tenantId != null) {
            builder.tenantId(tenantId);
        }
        final Map<String, String> processes = new HashMap<String, String>();
        for (MultipartFile resource : resourceFile) {
            LOGGER.debug(String.format("Deploying file: %1$s",
                    resource.getOriginalFilename()));
            if (resource.getOriginalFilename().toLowerCase().endsWith(".bpmn")
                    || resource.getOriginalFilename().toLowerCase()
                            .endsWith(".bpmn20.xml")) {
                LOGGER.info("... BPMN resource: "
                        + resource.getOriginalFilename());
                processes.put(resource.getOriginalFilename(), new String(
                        resource.getBytes()));
            } else {
                LOGGER.debug("... non-BPMN resource");
                builder.addInputStream(resource.getOriginalFilename(),
                        resource.getInputStream());
            }
        }

        if (isValid(processes)) {
            try {
                for (Entry<String, String> entry : runExecutableTweaker(
                        processes).entrySet()) {
                    builder.addString(entry.getKey(), entry.getValue());
                }
                deployment = builder.deploy();

                LOGGER.info(String.format(
                        "Completed deployment: %1$s(%2$s) at %3$s", deployment
                                .getName(), deployment.getId(), deployment
                                .getDeploymentTime().toString()));
                for (Entry<String, ResourceEntity> entry : ((DeploymentEntity) deployment)
                        .getResources().entrySet()) {
                    LOGGER.debug("  ...including: " + entry.getKey());
                }
                return deployment;
            } catch (ActivitiException e) {
                LOGGER.warn(String
                        .format("Processes rejected for execution, continue as non-executable. Reason: %1$s",
                                e.getMessage()));
                handleIncompleteModel(tenantId, processes);
                return null;
            } catch (Exception e) {
                LOGGER.error(String
                        .format("Unable to read file, attempt to continue as non-executable. Reason: %1$s",
                                e.getMessage()));
                handleIncompleteModel(tenantId, processes);
                return null;
            }
        } else {
            handleIncompleteModel(tenantId, processes);
            return null;
        }
    }

    private Map<String, String> runExecutableTweaker(
            Map<String, String> processes) throws UnsupportedEncodingException {
        HashMap<String, String> tweakedProcesses = new HashMap<String, String>();
        for (Entry<String, String> entry : processes.entrySet()) {
            String bpmn = getPreProcessor().transform(
                    new String(entry.getValue().getBytes(), "UTF-8"));
            if (LOGGER.isDebugEnabled() && verbose) {
                LOGGER.debug("BPMN: " + bpmn);
            }
            tweakedProcesses.put(entry.getKey(), bpmn);
        }
        return tweakedProcesses;
    }

    private void handleIncompleteModel(String tenantId,
            final Map<String, String> processes) {
        for (Entry<String, String> entry : processes.entrySet()) {
            ProcessModel model = new ProcessModel();
            model.setName(entry.getKey());
            model.setBpmnString(entry.getValue());
            // First id="xyz" is our id
            int start = entry.getValue().indexOf("id=") + 4;
            int end = entry.getValue().indexOf("\"", start);
            if (entry.getValue().indexOf("'", start) > -1
                    && entry.getValue().indexOf("'", start) < end) {
                end = entry.getValue().indexOf("'", start);
            }
            String id = entry.getValue().substring(start, end);
            model.setId(id);
            // model.setDeploymentId(deploymentId);
            model.setTenantId(tenantId);

            String issues = getValidator().transform(entry.getValue());
            if (LOGGER.isDebugEnabled() && verbose) {
                LOGGER.debug("ISSUES: " + issues);
            }
            model.setIssuesAsString(issues);

            createModel(model);
        }
    }

    private TransformTask getPreProcessor() {
        if (preProcessor == null) {
            preProcessor = new TransformTask();
            try {
                preProcessor.setXsltResources(PREPROCESSOR_RESOURCES);
            } catch (TransformerConfigurationException e) {
                LOGGER.error(String.format(
                        "Unable to location deployment pre-processors: %1$s",
                        PREPROCESSOR_RESOURCES), e);
            }
        }
        return preProcessor;
    }

    private TransformTask getValidator() {
        if (validator == null) {
            validator = new TransformTask();
            try {
                validator.setXsltResources(VALIDATOR_RESOURCES);
            } catch (TransformerConfigurationException e) {
                LOGGER.error(String.format(
                        "Unable to locate deployment validator: %1$s",
                        VALIDATOR_RESOURCES), e);
            }
        }
        return validator;
    }
    private boolean isValid(Map<String, String> processes) {
        // TODO rethink this in more Activiti way
        // for (Entry<String, String> entry : processes.entrySet()) {
        // if (transformService.transform(entry.getValue()).contains(
        // TransformTask.ERROR_KEY)) {
        // return false;
        // }
        // }
        return true;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public @ResponseBody void deleteFromJson(@PathVariable("id") String id) {
        LOGGER.info(String.format("deleting deployment: %1$s", id));
        try {
            Deployment deployment = Deployment.findDeployment(id);
            deployment.remove();
        } catch (Exception e) {
            // assume this is an incomplete model....
            processModelRepo.delete(id);
        }
    }

    protected ProcessModel createModel(ProcessModel model) {
        return processModelRepo.save(model);
    }

}
