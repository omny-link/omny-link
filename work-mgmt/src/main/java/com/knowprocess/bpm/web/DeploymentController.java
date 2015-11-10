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
import com.knowprocess.xslt.TransformTask;

@Controller
@RequestMapping("/{tenantId}/deployments")
public class DeploymentController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(DeploymentController.class);

    private static final String PREPROCESSOR_RESOURCES = "/xslt/ExecutableTweaker.xsl";

    /**
     * Set true to make verbose debug level logging.
     */
    protected static boolean verbose;

    @Autowired(required = true)
    ProcessEngine processEngine;

    private TransformTask preProcessor;

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

    @RequestMapping(method = RequestMethod.POST, consumes = "multipart/form-data", headers = "Accept=application/json")
    public final @ResponseBody org.activiti.engine.repository.Deployment uploadMultipleFiles(
            UriComponentsBuilder uriBuilder,
            @RequestParam String tenantId,
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
                LOGGER.debug("... BPMN resource");
                String bpmn = getPreProcessor().transform(
                        new String(resource.getBytes(), "UTF-8"));
                if (LOGGER.isDebugEnabled() && verbose) {
                    LOGGER.debug("BPMN: " + bpmn);
                }
                processes.put(resource.getOriginalFilename(), bpmn);
                builder.addString(resource.getOriginalFilename(), bpmn.trim());
            } else {
                LOGGER.debug("... non-BPMN resource");
                builder.addInputStream(resource.getOriginalFilename(),
                        resource.getInputStream());
            }
        }

        if (isValid(processes)) {
            try {
                deployment = builder.deploy();
            } catch (ActivitiException e) {
                UnsupportedBpmnException e2 = new UnsupportedBpmnException(
                        String.format("Unsupported BPMN, cause: %1$s",
                                e.getMessage()));
                throw e2;
            }
            LOGGER.info("Completed deployment: " + deployment.getName() + "("
                    + deployment.getId() + ")");
            for (Entry<String, ResourceEntity> entry : ((DeploymentEntity) deployment)
                    .getResources().entrySet()) {
                LOGGER.debug("  ...including: " + entry.getKey());
            }
            return deployment;
        } else {
            UnsupportedBpmnException e2 = new UnsupportedBpmnException(
                    "Rejected BPMN as unsupported, see log for details.");
            throw new RuntimeException(e2.toJson(), e2);
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
        Deployment deployment = Deployment.findDeployment(id);
        // if (deployment == null) {
            // return new ResponseEntity<String>(headers,
            // HttpStatus.NOT_FOUND);
        // }
        deployment.remove();
        // return deployment;
    }

}
