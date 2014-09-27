package org.activiti.spring.rest.web;

import org.activiti.spring.rest.model.Deployment;
import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RooWebJson(jsonObject = Deployment.class)
@Controller
@RequestMapping("/deployments")
@RooWebScaffold(path = "deployments", formBackingObject = Deployment.class)
public class DeploymentController {

	// @Autowired
	// private ProcessEngine processEngine;
	//
	// @RequestMapping(method = RequestMapping.POST, consumes =
	// "multipart/form-data")
	// public ResponseEntity uploadMultipleFiles(
	// HttpServletRequest request) {
	// org.activiti.engine.repository.Deployment deployment = null;
	// final FileItemFactory factory = new DiskFileItemFactory();
	// final ServletFileUpload fileUpload = new ServletFileUpload(factory);
	// try {
	// DeploymentBuilder builder = processEngine
	// .getRepositoryService().createDeployment();
	// /*
	// * parseRequest returns a list of FileItem but in old (pre-java5)
	// * style
	// */
	// final List<?> items = fileUpload.parseRequest(request);
	// final Map<String, String> processes = new HashMap<String, String>();
	// // final Fetcher fetcher = new Fetcher();
	// for (Iterator<?> iter = items.iterator(); iter.hasNext();) {
	// final FileItem item = (FileItem) iter.next();
	// if (item.isFormField()
	// && "deploymentName".equals(item.getFieldName())) {
	// // getLogger().fine(
	// // "Field Name: " + item.getFieldName()
	// // + ", Field Value: " + item.getString());
	// builder.name(item.getString());
	// } else {
	// // getLogger().fine("Deploying file: " + item.getName());
	// if (item.getName().toLowerCase().endsWith(".bpmn")
	// || item.getName().toLowerCase()
	// .endsWith(".bpmn20.xml")) {
	// String bpmn = item.getString("UTF-8");
	// System.out.println("BPMN: " + bpmn);
	// processes.put(item.getName(), bpmn);
	// builder.addString(item.getName(), bpmn.trim());
	// } else {
	// builder.addInputStream(item.getName(),
	// item.getInputStream());
	// }
	// }
	// }
	//
	// if (isValid(processes)) {
	// deployment = builder.deploy();
	// // getLogger().fine(
	// // "Deployed: " + deployment.getName() + "("
	// // + deployment.getId() + ")");
	// for (Entry<String, ResourceEntity> entry : ((DeploymentEntity)
	// deployment)
	// .getResources().entrySet()) {
	// // getLogger().finer("  ...including: " + entry.getKey());
	// }
	// return new ResponseEntity(deployment, HttpStatus.CREATED);
	// } else {
	// // getLogger().severe(
	// // "Rejected BPMN as unsupported, see log for details.");
	// return new ResponseEntity("Process is invalid",
	// HttpStatus.BAD_REQUEST);
	// }
	// } catch (Exception e) {
	// // getLogger().severe(e.getClass() + ":" + e.getMessage());
	// // if (getLogger().isLoggable(Level.SEVERE)) {
	// e.printStackTrace(System.err);
	// // }
	// return new ResponseEntity(e.getClass().getName() + ": "
	// + e.getMessage(), HttpStatus.BAD_REQUEST);
	// }
	// }
	//
	// private boolean isValid(Map<String, String> processes) {
	// for (Entry<String, String> entry : processes.entrySet()) {
	// if (transformService.transform(entry.getValue()).contains(
	// TransformTask.ERROR_KEY)) {
	// return false;
	// }
	// }
	// return true;
	// }
}
