package org.activiti.spring.rest.web;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.spring.rest.model.Deployment;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@RooWebJson(jsonObject = Deployment.class)
@Controller
@RequestMapping("/deployments")
@RooWebScaffold(path = "deployments", formBackingObject = Deployment.class)
public class DeploymentController {

    protected static final Logger LOGGER = LoggerFactory
    		.getLogger(DeploymentController.class);

    @Autowired
    private ProcessEngine processEngine;

    @RequestMapping(method = RequestMethod.POST, consumes = "multipart/form-data")
    @ResponseBody
    public ResponseEntity uploadMultipleFiles(HttpServletRequest request) {
        org.activiti.engine.repository.Deployment deployment = null;
        final FileItemFactory factory = new DiskFileItemFactory();
        final ServletFileUpload fileUpload = new ServletFileUpload(factory);
        try {
            DeploymentBuilder builder = processEngine.getRepositoryService().createDeployment();
            final List<?> items = fileUpload.parseRequest(request);
            final Map<String, String> processes = new HashMap<String, String>();
            // final Fetcher fetcher = new Fetcher();
            for (Iterator<?> iter = items.iterator(); iter.hasNext(); ) {
                final FileItem item = (FileItem) iter.next();
                if (item.isFormField() && "deploymentName".equals(item.getFieldName())) {
                    LOGGER.debug(String.format("Field Name: %1$s, Field Value: %2$s", item.getFieldName(), item.getString()));
                    builder.name(item.getString());
                } else {
                    LOGGER.debug(String.format("Deploying file: %1$s", item.getName()));
                    if (item.getName().toLowerCase().endsWith(".bpmn") || item.getName().toLowerCase().endsWith(".bpmn20.xml")) {
                        String bpmn = item.getString("UTF-8");
                        System.out.println("BPMN: " + bpmn);
                        processes.put(item.getName(), bpmn);
                        builder.addString(item.getName(), bpmn.trim());
                    } else {
                        builder.addInputStream(item.getName(), item.getInputStream());
                    }
                }
            }
            if (isValid(processes)) {
                deployment = builder.deploy();
                // getLogger().fine(
                // "Deployed: " + deployment.getName() + "("
                // + deployment.getId() + ")");
                for (Entry<String, ResourceEntity> entry : ((DeploymentEntity) deployment).getResources().entrySet()) {
                    // getLogger().finer("  ...including: " + entry.getKey());
                }
                return new ResponseEntity(deployment, HttpStatus.CREATED);
            } else {
                // getLogger().severe(
                // "Rejected BPMN as unsupported, see log for details.");
                return new ResponseEntity("Process is invalid", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            // getLogger().severe(e.getClass() + ":" + e.getMessage());
            // if (getLogger().isLoggable(Level.SEVERE)) {
            e.printStackTrace(System.err);
            // }
            return new ResponseEntity(e.getClass().getName() + ": " + e.getMessage(), HttpStatus.BAD_REQUEST);
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
}
