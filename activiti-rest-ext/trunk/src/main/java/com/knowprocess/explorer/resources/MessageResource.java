package com.knowprocess.explorer.resources;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
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
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
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

	@GET
	@Path("/{msgId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doInOutMep(@Context SecurityContext sc,
			@PathParam("msgId") String msgId,
			@QueryParam("query") String jsonBody) {
		logger.info("getMessage: " + msgId + ", json:" + jsonBody);

		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("query", msgId);
		vars.put(msgId, jsonBody);
		Response response = handleMep(sc, msgId, jsonBody, vars);
		String locationHeader = (String) response.getMetadata().get("Location")
				.get(0);
		System.out.println("location: " + locationHeader);
		String id = locationHeader.substring(locationHeader.lastIndexOf('/'));
		System.out.println("id: " + id);
		String msg = (String) processEngine.getRuntimeService().getVariable(id,
				msgId);
		System.out.println("msg: " + msg);

		return Response.ok(msg).build();
	}

	@POST
	@Path("/{msgId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doInOnlyMep(@Context SecurityContext sc,
			@PathParam("msgId") String msgId, String jsonBody) {
		logger.info("sendMessage: " + msgId + ", json:" + jsonBody);

		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("message", msgId);
		vars.put(msgId, jsonBody);

		return handleMep(sc, msgId, jsonBody, vars);
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

			URI uri = uriInfo.getBaseUriBuilder()
					.path("/service/runtime/process-instances")
					.build(instance.getId());
			return Response.created(uri).build();
		} catch (ActivitiException e) {
			logger.severe(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace(System.err);
			return Response.status(Response.Status.SERVICE_UNAVAILABLE)
					.build();
		} catch (Exception e) {
			logger.severe(e.getClass().getName() + ": " + e.getMessage());
			return Response.status(Response.Status.BAD_REQUEST)
					.build();
		} finally {
			Authentication.setAuthenticatedUserId(null);
		}
	}
}