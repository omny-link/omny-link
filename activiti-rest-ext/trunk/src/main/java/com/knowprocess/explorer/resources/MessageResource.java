package com.knowprocess.explorer.resources;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.runtime.ProcessInstance;

/**
 * Handle REST requests sending BPMN message events to start or modify process
 * instances.
 * 
 * @author timstephenson
 * 
 */
@Path("/msg")
public class MessageResource {

	@Context
	private UriInfo uriInfo;

	private ProcessEngine processEngine;

	private Logger logger = Logger.getLogger(getClass().getName());

	public MessageResource() {
		processEngine = ProcessEngines.getDefaultProcessEngine();
		assert processEngine != null;
	}

	protected void setUriInfo(UriInfo uriInfo) {
		this.uriInfo = uriInfo;
	}

	@GET
	@Path("/{msgId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doInOutMep(@Context SecurityContext sc,
			@PathParam("msgId") String msgId,
			@QueryParam("query") String jsonBody) {
		logger.info("getMessage: " + msgId + ", json:" + jsonBody);
		String msg = "";
		try {
			Map<String, Object> vars = new HashMap<String, Object>();
			vars.put("query", msgId);
			vars.put(msgId, jsonBody);
			Response response = handleMep(sc, msgId, jsonBody, vars);
			URI locationHeader = (URI) response.getMetadata().get("Location")
					.get(0);
			System.out.println("location: " + locationHeader);
			String header = locationHeader.toURL().toExternalForm();
			String id = header.substring(header.lastIndexOf('/') + 1);
			System.out.println("id: " + id);
			try {
				msg = (String) processEngine.getRuntimeService().getVariable(
						id, msgId);
			} catch (ActivitiObjectNotFoundException e) {
				// assume process is closed
				HistoricVariableInstance instance = processEngine
						.getHistoryService()
						.createHistoricVariableInstanceQuery()
						.processInstanceId(id).variableName(msgId)
						.singleResult();
				msg = (String) instance.getValue();
			}
			System.out.println("msg: " + msg);
			return Response.ok(msg).contentLocation(locationHeader).build();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.ok(msg).build();
		}
	}

	@POST
	@Path("/{msgId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doInOnlyMep(@Context SecurityContext sc,
			@PathParam("msgId") String msgId, String jsonBody) {
		long start = System.currentTimeMillis();
		logger.info("doInOnlyMep: " + msgId + ", json:" + jsonBody);

		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("message", msgId);
		vars.put(msgId, jsonBody);

		Response response = handleMep(sc, msgId, jsonBody, vars);

		logger.info(String.format("doInOnlyMep took: %1$s ms",
				(System.currentTimeMillis() - start)));
		return response;
	}

	public Response handleMep(SecurityContext sc, String msgId,
			String jsonBody, Map<String, Object> vars) {
		if (sc.getUserPrincipal() == null) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		String username = sc.getUserPrincipal().getName();
		logger.info(" ... from " + username);
		try {

			String bizKey = msgId + " - " + new Date().getTime();
			Authentication.setAuthenticatedUserId(username);
			vars.put("initiator", sc.getUserPrincipal().getName());

			ProcessInstance instance = processEngine.getRuntimeService()
					.startProcessInstanceByMessage(msgId, bizKey, vars);

			List<ProcessInstance> list = processEngine.getRuntimeService()
					.createProcessInstanceQuery()
					.processInstanceId(instance.getId()).list();
			String path = "/service/runtime/process-instances/{id}";
			if (list.size() == 0) {
				// already ended
				path = "/service/history/historic-process-instances/{id}";
			}
			URI uri = uriInfo.getBaseUriBuilder().path(path)
					.build(instance.getId());
			return Response.created(uri).build();
		} catch (ActivitiException e) {
			logger.severe(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace(System.err);
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
		} catch (Exception e) {
			logger.severe(e.getClass().getName() + ": " + e.getMessage());
			return Response.status(Response.Status.BAD_REQUEST).build();
		} finally {
			Authentication.setAuthenticatedUserId(null);
		}
	}

}