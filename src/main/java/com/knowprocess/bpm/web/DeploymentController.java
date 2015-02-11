package com.knowprocess.bpm.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

import com.knowprocess.bpm.api.ReportableException;
import com.knowprocess.bpm.model.Deployment;

@Controller
@RequestMapping("/deployments")
public class DeploymentController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(DeploymentController.class);

    /**
     * Set true to make verbose debug level logging.
     */
    protected static boolean verbose;

    @Autowired(required = true)
    ProcessEngine processEngine;

    @RequestMapping(value = "/", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody List<Deployment> showAllJson() {
        LOGGER.info("showAllJson");

        try {
            List<Deployment> list = Deployment.findAllDeployments();
            LOGGER.info("Deployments: " + list.size());
            return list;
        } catch (Exception e) {
            LOGGER.error(e.getClass().getName() + ":" + e.getMessage());
            e.printStackTrace(System.err);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "multipart/form-data", headers = "Accept=application/json")
    public final @ResponseBody org.activiti.engine.repository.Deployment uploadMultipleFiles(
            UriComponentsBuilder uriBuilder,
            @RequestParam String deploymentName,
            @RequestParam MultipartFile... resourceFile) {
        org.activiti.engine.repository.Deployment deployment = null;

        try {
            LOGGER.debug(String.format("deploymentName: %1$s", deploymentName));
            LOGGER.debug(String.format("# of resources: %1$s",
                    resourceFile.length));

            DeploymentBuilder builder = processEngine.getRepositoryService()
                    .createDeployment();
            if (deploymentName != null) {
                builder.name(deploymentName);
            }
            final Map<String, String> processes = new HashMap<String, String>();
            for (MultipartFile resource : resourceFile) {

                LOGGER.debug(String.format("Deploying file: %1$s",
                        resource.getOriginalFilename()));
                if (resource.getOriginalFilename().toLowerCase()
                        .endsWith(".bpmn")
                        || resource.getOriginalFilename().toLowerCase()
                                .endsWith(".bpmn20.xml")) {
                    LOGGER.debug("... BPMN resource");
                    String bpmn = new String(resource.getBytes(), "UTF-8");
                    if (LOGGER.isDebugEnabled() && verbose) {
                        LOGGER.debug("BPMN: " + bpmn);
                    }
                    processes.put(resource.getOriginalFilename(), bpmn);
                    builder.addString(resource.getOriginalFilename(),
                            bpmn.trim());
                } else {
                    LOGGER.debug("... non-BPMN resource");
                    builder.addInputStream(resource.getOriginalFilename(),
                            resource.getInputStream());
                }
            }

            if (isValid(processes)) {
                deployment = builder.deploy();
                LOGGER.info("Completed deployment: " + deployment.getName()
                        + "(" + deployment.getId() + ")");
                for (Entry<String, ResourceEntity> entry : ((DeploymentEntity) deployment)
                        .getResources().entrySet()) {
                    LOGGER.debug("  ...including: " + entry.getKey());
                }
                return deployment;
                // HttpHeaders headers = new HttpHeaders();
                // headers.add("Content-Type", "application/json");
                // RequestMapping a = getClass().getAnnotation(
                // RequestMapping.class);
                // headers.add(
                // "Location",
                // uriBuilder
                // .path(a.value()[0] + "/"
                // + deployment.getId().toString())
                // .build().toUriString());
                // return new ResponseEntity(deployment, headers,
                // HttpStatus.CREATED);
            } else {
                ReportableException e2 = new ReportableException(
                        "Rejected BPMN as unsupported, see log for details.");
                throw new RuntimeException(e2.toJson(), e2);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
            // ReportableException e2 = new ReportableException(e.getClass()
            // .getName() + ":" + e.getMessage(), e);
            // return new ResponseEntity(e2.toJson(), HttpStatus.BAD_REQUEST);
        }
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
        // HttpHeaders headers = new HttpHeaders();
        // headers.add("Content-Type", "application/json");
        try {
            Deployment deployment = Deployment.findDeployment(id);
            if (deployment == null) {
                // return new ResponseEntity<String>(headers,
                // HttpStatus.NOT_FOUND);
            }
            deployment.remove();
            // return deployment;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
