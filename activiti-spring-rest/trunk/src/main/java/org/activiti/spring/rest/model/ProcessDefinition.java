package org.activiti.spring.rest.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Id;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.form.StartFormData;
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
public class ProcessDefinition {

	private static final String[] JSON_FIELDS = { "name", "category",
			"description", "version", "resourceName", "deploymentId",
			"diagramResourceName", "key" };

	private static ProcessEngine processEngine;

	@Id
	private String id;

	/**
     */
	private String name;

	/**
     */
	private String category;

	/**
     */
	private String description;

	/**
     */
	private Integer version;

	/**
     */
	private String resourceName;

	/**
     */
	private Integer deploymentId;

	/**
     */
	private String diagramResourceName;

	/**
     */
	private String key;

	private String formKey;

	private List<FormProperty> formProperties;

	public ProcessDefinition() {
		super();
		formProperties = new ArrayList<FormProperty>();
	}

	public ProcessDefinition(org.activiti.engine.repository.ProcessDefinition pd) {
		this();
		setId(pd.getId());
		setKey(pd.getKey());
		setName(pd.getName());
		setCategory(pd.getCategory());
		setDeploymentId(Integer.valueOf(pd.getDeploymentId()));
		setDescription(pd.getDescription());
		setVersion(Integer.valueOf(pd.getVersion()));
		setResourceName(pd.getResourceName());
		setDiagramResourceName(pd.getDiagramResourceName());
	}

	// Autowiring static fields is obviously dangerous, but should be ok in this
	// case as PE is thread safe.
	@Autowired(required = true)
	public void setProcessEngine(ProcessEngine pe) {
		ProcessDefinition.processEngine = pe;
	}

	public static long countProcessDefinitions() {
		return processEngine.getRepositoryService()
				.createProcessDefinitionQuery().count();
	}

	public static List<ProcessDefinition> findAllProcessDefinitions() {
		return wrap(processEngine.getRepositoryService()
				.createProcessDefinitionQuery().list());
	}

	public static ProcessDefinition findProcessDefinition(String id) {
		ProcessDefinition pd = new ProcessDefinition(processEngine
				.getRepositoryService().createProcessDefinitionQuery()
				.processDefinitionId(id).singleResult());
		StartFormData formData = processEngine.getFormService()
				.getStartFormData(id);
		pd.setFormKey(formData.getFormKey());
		for (org.activiti.engine.form.FormProperty prop : formData
				.getFormProperties()) {
			pd.getFormProperties().add(new FormProperty(prop));
		}
		return pd;
	}

	public static List<ProcessDefinition> findProcessDefinitionEntries(
			int firstResult, int maxResults) {
		return wrap(processEngine.getRepositoryService()
				.createProcessDefinitionQuery()
				.listPage(firstResult, maxResults));
	}

	public static List<ProcessDefinition> findProcessDefinitionEntries(
			int firstResult, int maxResults, String sortFieldName,
			String sortOrder) {
		// TODO honour sort order
		return wrap(processEngine.getRepositoryService()
				.createProcessDefinitionQuery().orderByProcessDefinitionName()
				.asc().listPage(firstResult, maxResults));
	}

	public static List<ProcessDefinition> findAllProcessDefinitions(
			String sortFieldName, String sortOrder) {
		System.out.println("pe: " + processEngine);
		// TODO honour sort order
		return wrap(processEngine.getRepositoryService()
				.createProcessDefinitionQuery().orderByProcessDefinitionName()
				.asc().list());
	}

	private static List<ProcessDefinition> wrap(
			final List<org.activiti.engine.repository.ProcessDefinition> list) {
		ArrayList<ProcessDefinition> list2 = new ArrayList<ProcessDefinition>();
		for (org.activiti.engine.repository.ProcessDefinition processDefinition : list) {
			list2.add(new ProcessDefinition(processDefinition));
		}
		return list2;
	}

	public String toJson() {
		return toJson(JSON_FIELDS);
	}

	public String toJson(String[] fields) {
		return new JSONSerializer().include(fields).exclude("*.class")
				.exclude("*.processEngine").serialize(this);
	}

	public static String toJsonArray(Collection<ProcessDefinition> collection) {
		return toJsonArray(collection, JSON_FIELDS);
	}

	public static String toJsonArray(Collection<ProcessDefinition> collection,
			String[] fields) {
		System.out.println("toJsonArray....");
		return new JSONSerializer().exclude("*.class")
				.exclude("*.processEngine").include(fields)
				.serialize(collection);
	}
}
