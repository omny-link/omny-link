package com.knowprocess.explorer.resources;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
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

	@PUT
	@Path("/{msgId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendMessage(@Context SecurityContext sc,
			@PathParam("msgId") String msgId, String jsonBody) {
		logger.info("sendMessage: " + msgId + ", json:" + jsonBody);

		try {
			Map<String, Object> vars = new HashMap<String, Object>();
			vars.put(msgId, jsonBody);
			String bizKey = msgId + " - " + new Date().getTime();
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
		}
	}
}