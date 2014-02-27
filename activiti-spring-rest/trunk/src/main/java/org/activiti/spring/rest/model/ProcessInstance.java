package org.activiti.spring.rest.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.equals.RooEquals;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.serializable.RooSerializable;
import org.springframework.roo.addon.tostring.RooToString;

import flexjson.JSONSerializer;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
@RooEquals
@RooSerializable
@RooJson
public class ProcessInstance extends Execution {

	private static ProcessEngine processEngine;

	/**
     */
    private String businessKey;

    /**
     */
    private String processDefinitionId;

    /**
     */
    private Boolean suspended;

	public ProcessInstance() {
		super();
	}

	public ProcessInstance(org.activiti.engine.runtime.ProcessInstance pi) {
		super();
		setBusinessKey(pi.getBusinessKey());
		setProcessDefinitionId(pi.getProcessDefinitionId());
		setSuspended(pi.isSuspended());
	}

	// Autowiring static fields is obviously dangerous, but should be ok in this
	// case as PE is thread safe.
	@Autowired(required = true)
	public void setProcessEngine(ProcessEngine pe) {
		ProcessInstance.processEngine = pe;
	}

	public static long countProcessInstances() {
		return processEngine.getRuntimeService()
				.createProcessInstanceQuery().count();
	}

	public static List<ProcessInstance> findAllProcessInstances() {
		return wrap(processEngine.getRuntimeService()
				.createProcessInstanceQuery().list());
	}

	public static ProcessInstance findProcessInstance(String id) {
		return wrap(
				processEngine.getRuntimeService()
						.createProcessInstanceQuery()
						.processDefinitionId(id).list()).get(0);
	}

	public static List<ProcessInstance> findProcessInstanceEntries(
			int firstResult, int maxResults) {
		return wrap(processEngine.getRuntimeService()
				.createProcessInstanceQuery()
				.listPage(firstResult, maxResults));
	}

	public static List<ProcessInstance> findProcessInstanceEntries(
			int firstResult, int maxResults, String sortFieldName,
			String sortOrder) {
		// TODO honour sort order
		return wrap(processEngine.getRuntimeService()
				.createProcessInstanceQuery().listPage(firstResult, maxResults));
	}

	public static List<ProcessInstance> findAllProcessInstances(
			String sortFieldName, String sortOrder) {
		System.out.println("pe: " + processEngine);
		// TODO honour sort order
		return wrap(processEngine.getRuntimeService()
				.createProcessInstanceQuery().list());
	}

	private static List<ProcessInstance> wrap(
			final List<org.activiti.engine.runtime.ProcessInstance> list) {
		ArrayList<ProcessInstance> list2 = new ArrayList<ProcessInstance>();
		for (org.activiti.engine.runtime.ProcessInstance instance : list) {
			list2.add(new ProcessInstance(instance));
		}
		return list2;
	}

	public static String toJsonArray(Collection<ProcessInstance> collection) {
		String[] fields = { "businessKey", "processDefinitionId", "suspended" };
		return toJsonArray(collection, fields);
	}

	public static String toJsonArray(Collection<ProcessInstance> collection,
			String[] fields) {
		System.out.println("toJsonArray....");
		return new JSONSerializer().exclude("*.class")
				.exclude("*.processEngine").include(fields)
				.serialize(collection);
	}
}
