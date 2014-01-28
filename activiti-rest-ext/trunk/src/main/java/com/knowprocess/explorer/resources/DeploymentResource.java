package com.knowprocess.explorer.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.TransformerConfigurationException;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.knowprocess.deployment.DeploymentService;
import com.knowprocess.resource.spi.Fetcher;
import com.knowprocess.xslt.TransformTask;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Path("/ext/repository")
public class DeploymentResource {

	private ProcessEngine processEngine;

	private DeploymentService deploymentService;

	@Context
	private UriInfo uriInfo;

	private TransformTask transformService;

	public DeploymentResource() throws TransformerConfigurationException {
		transformService = new TransformTask();
		transformService
				.setXsltResource("/xslt/ExecutableTweaker.xsl,/xslt/KpSupportRules.xsl");
	}

	@POST
	@Path("/deployments")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response deployBpmnResource(@Context SecurityContext sc,
			@FormParam("definitionUrl") String url,
			@FormParam("processParticipant") String processParticipant,
			@FormParam("startInstance") boolean startInstance) {
		getLogger().info(String.format("Deploying %1$s", url));
		url = URLDecoder.decode(url);
		getLogger().info(String.format(".. after decoding %1$s", url));
		Authentication.setAuthenticatedUserId(getUsername(sc));
		ProcessInstance processInstance = getDeploymentHelper()
				.submitDeploymentRequest(url,
						wrapAsVariables(processParticipant), startInstance);
		Authentication.setAuthenticatedUserId(null);

		return buildResponse(processInstance);
	}

	private Map<String, Object> wrapAsVariables(String processParticipant) {
		Map<String, Object> vars = new HashMap<String, Object>();
		if (processParticipant != null
				&& processParticipant.trim().length() > 0) {
			vars.put("processParticipantToExecute", processParticipant);
		}
		return vars;
	}

	@POST
	@Path("/deployments0")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(
			@Context SecurityContext sc,
			@FormDataParam("definitionFile") InputStream uploadedInputStream,
			@FormDataParam("definitionFile") FormDataContentDisposition fileDetail) {
		System.out.println("uploadFile");
		System.out.println("..:" + fileDetail.getFileName() + ","
				+ fileDetail.getName());

		if (fileDetail.getFileName().toLowerCase().endsWith(".bar")
				|| fileDetail.getFileName().toLowerCase().endsWith(".jar")
				|| fileDetail.getFileName().toLowerCase().endsWith(".zip")) {
			return uploadBar(sc, uploadedInputStream, fileDetail);
		} else if (fileDetail.getFileName().toLowerCase().endsWith(".bpmn")
				|| fileDetail.getFileName().toLowerCase()
						.endsWith(".bpmn20.xml")) {
			return uploadBpmn(sc, uploadedInputStream, fileDetail);
		} else {
			return Response.status(Status.BAD_REQUEST).build();// entity(createdContent).build();
		}
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("text/plain")
	@Path("/deployments")
	public Response uploadMultipleFiles(@Context HttpServletRequest request) {
		Deployment deployment = null;
		final FileItemFactory factory = new DiskFileItemFactory();
		final ServletFileUpload fileUpload = new ServletFileUpload(factory);
		try {
			DeploymentBuilder builder = getProcessEngine()
					.getRepositoryService().createDeployment();
			/*
			 * parseRequest returns a list of FileItem but in old (pre-java5)
			 * style
			 */
			final List<?> items = fileUpload.parseRequest(request);
			final Map<String, String> processes = new HashMap<String, String>();
			final Fetcher fetcher = new Fetcher();
			for (Iterator<?> iter = items.iterator(); iter.hasNext();) {
				final FileItem item = (FileItem) iter.next();
				if (item.isFormField()
						&& "deploymentName".equals(item.getFieldName())) {
					getLogger().fine(
							"Field Name: " + item.getFieldName()
									+ ", Field Value: " + item.getString());
					builder.name(item.getString());
				} else {
					getLogger().fine("Deploying file: " + item.getName());
					if (item.getName().toLowerCase().endsWith(".bpmn")
							|| item.getName().toLowerCase()
									.endsWith(".bpmn20.xml")) {
						String bpmn = fetcher.fetchToString(
												item.getInputStream(), item.getName(),
												Fetcher.MIME_XML);
						System.out.println("BPMN: " + bpmn);
						processes.put(item.getName(), bpmn);
						builder.addString(item.getName(), bpmn.trim());
					} else {
						builder.addInputStream(item.getName(),
								item.getInputStream());
					}
				}
			}

			if (isValid(processes)) {
				deployment = builder.deploy();
				getLogger().fine(
						"Deployed: " + deployment.getName() + "("
								+ deployment.getId() + ")");
				for (Entry<String, ResourceEntity> entry : ((DeploymentEntity) deployment)
						.getResources().entrySet()) {
					getLogger().finer("  ...including: " + entry.getKey());
				}
				return buildResponse(deployment);
			} else {
				getLogger().severe(
						"Rejected BPMN as unsupported, see log for details.");
				return Response.status(Status.BAD_REQUEST).build();
			}
		} catch (Exception e) {
			getLogger().severe(e.getClass() + ":" + e.getMessage());
			if (getLogger().isLoggable(Level.SEVERE)) {
				e.printStackTrace(System.err);
			}
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	private boolean isValid(Map<String, String> processes) {
		for (Entry<String, String> entry : processes.entrySet()) {
			if (transformService.transform(entry.getValue()).contains(
					TransformTask.ERROR_KEY)) {
				return false;
			}
		}
		return true;
	}

	private Response uploadBar(SecurityContext sc, InputStream is,
			FormDataContentDisposition fileDetail) {
		Deployment deployment = getProcessEngine().getRepositoryService()
				.createDeployment().name(fileDetail.getName())
				.addZipInputStream(new ZipInputStream(is)).deploy();
		System.out.println("deployment id: " + deployment.getId());
		return buildResponse(deployment);
	}

	private Response buildResponse(Deployment deployment) {
		URI createdUri = uriInfo.getBaseUriBuilder()
				.path("/service/repository/deployments/{id}")
				.build(deployment.getId());
		return Response.created(createdUri).build();
	}

	private Response uploadBpmn(SecurityContext sc,
			InputStream uploadedInputStream,
			FormDataContentDisposition fileDetail) {
		StringBuffer sb = new StringBuffer();
		try {
			char[] buf = new char[1024];
			Reader reader = new InputStreamReader(uploadedInputStream, "UTF-8");
			while (reader.read(buf) != -1) {
				sb.append(buf);
				// need to reset to avoid carried over chars last time thru
				buf = new char[1024];
			}
			System.out.println("Process uploaded: " + sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.status(Status.BAD_REQUEST).build();// entity(createdContent).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();// entity(createdContent).build();
		}

		Authentication.setAuthenticatedUserId(getUsername(sc));

		ProcessInstance processInstance = getDeploymentHelper()
				.submitDeploymentRequest(fileDetail.getFileName(),
						sb.toString(), wrapAsVariables("tim@trakeo.com"), true);
		Authentication.setAuthenticatedUserId(null);

		return buildResponse(processInstance);
	}

	private String getUsername(SecurityContext sc) {
		try {
			return sc.getUserPrincipal().getName();
		} catch (Exception e) {
			System.out.println("TODO Remove the Auth hack!");
			return "tim@trakeo.com";
		}
	}

	private Response buildResponse(ProcessInstance processInstance) {
		System.out.println("id " + processInstance.getId() + " "
				+ processInstance.getProcessDefinitionId());
		URI createdUri = uriInfo.getBaseUriBuilder()
				.path("/service/runtime/process-instances")
				.build(processInstance.getId());
		return Response.created(createdUri).build();// entity(createdContent).build();
													// }
	}

	@POST
	@Path("/simple")
	@Consumes("application/x-www-form-urlencoded")
	public Response deployBpmnResourceSimple(
			@FormParam("definitionUrl") String url) {
		getLogger().info(String.format("Deploying %1$s", url));
		url = URLDecoder.decode(url);
		getLogger().info(String.format(".. after decoding %1$s", url));

		RepositoryService repoSvc = getProcessEngine().getRepositoryService();
		// String resource = url.toExternalForm().substring(
		// url.toExternalForm().indexOf(".jar!") + ".jar!".length()
		// + 1);
		String resource = url;
		try {
			Deployment deployment = null;
			if (resource.startsWith("http")) {
				System.out.println("Loading definition using http");
				String resourceName = resource.substring(resource
						.lastIndexOf('/') + 1);
				// TODO Activiti only parses stuff ending w bpmn
				if (!resourceName.endsWith(".bpmn")) {
					resourceName += ".bpmn";
				}
				InputStream is = null;
				try {
					URL bpmnUrl = new URL(resource);
					is = bpmnUrl.openStream();
					System.out.println("Got the stream? " + is);
					deployment = repoSvc.createDeployment()
							.addInputStream(resourceName, is).deploy();
				} finally {
					is.close();
				}
			} else {
				deployment = repoSvc.createDeployment()
						.addClasspathResource(resource).deploy();
			}
			getLogger()
					.info(String.format("... deployment ok: %1$s at %2$s",
							deployment.getId(), deployment.getDeploymentTime()));

			ProcessDefinition template = getProcessEngine()
					.getRepositoryService().createProcessDefinitionQuery()
					.deploymentId(deployment.getId()).singleResult();
			URI createdUri = uriInfo.getBaseUriBuilder().path(getClass())
					.path(getClass(), "getProcessTemplate")
					.build(template.getId());
			// String createdContent = create(processInstance);
			return Response.created(createdUri).build();// entity(createdContent).build();
		} catch (Exception e) {
			getLogger().severe(
					String.format("Exception during deployment: %1$s: %2$s", e
							.getClass().getName(), e.getMessage()));
			return Response.serverError().build();
		}

	}

	// @GET
	// @Produces(MediaType.TEXT_HTML)
	// public Viewable getTemplateForm() {
	// return new Viewable("/template/show.jsp");
	// }

	@GET
	@Path("/image/{id}")
	// @Consumes("application/x-www-form-urlencoded")
	@Produces({ MediaType.APPLICATION_OCTET_STREAM })
	public InputStream getProcessTemplateImage(@PathParam("id") String id) {
		System.out.println("getProcessTemplateImage: " + id);
		InputStream is = getProcessEngine().getRepositoryService()
				.getProcessDiagram(id);
		return is;
	}

	@GET
	@Path("/{taskId}/definition.bpmn")
	@Produces({ MediaType.TEXT_XML })
	public String getDefinitionBpmn(@PathParam("taskId") String taskId) {
		System.out.println("getDefinitionBpmn: " + taskId);
		String resource = new String((byte[]) getProcessEngine()
				.getTaskService().getVariable(taskId, "resource"));
		return resource;
	}

	private ProcessEngine getProcessEngine() {
		if (processEngine == null) {
			processEngine = ProcessEngines.getDefaultProcessEngine();
			deploymentService = new DeploymentService(processEngine);
		}
		return processEngine;
	}

	private DeploymentService getDeploymentHelper() {
		if (deploymentService == null) {
			deploymentService = new DeploymentService(getProcessEngine());
		}
		return deploymentService;
	}

	private Logger getLogger() {
		return Logger.getLogger(getClass().getName());
	}
}
