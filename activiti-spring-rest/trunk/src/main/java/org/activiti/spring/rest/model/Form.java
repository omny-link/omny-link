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
import org.springframework.stereotype.Component;

import flexjson.JSONSerializer;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
@RooEquals
@RooSerializable
@RooJson
@Component
public class Form {

	private static ProcessEngine processEngine;

    /**
     */
    private String formKey;

    /**
     */
    private String deploymentId;

    /**
     */
    private String processDefinitionId;

    /**
     */
    private String processDefinitionUrl;

    /**
     */
	private String formId;

    /**
     */
	private String formUrl;

	public Form() {
		super();
	}

	public Form(org.activiti.engine.form.FormData f) {
		this();
		setFormKey(f.getFormKey());
		setDeploymentId(f.getDeploymentId());
	}

	// Autowiring static fields is obviously dangerous, but should be ok in this
	// case as PE is thread safe.
	@Autowired(required = true)
	public void setProcessEngine(ProcessEngine pe) {
		Form.processEngine = pe;
	}

	// public static long countForms() {
	// // return processEngine.getFormService()..count();
	// // TODO
	// return 0;
	// }
	//
	// public static List<Form> findAllForms() {
	// return wrap(processEngine.getFormService().createFormQuery().list());
	// }
	//
	// public static Form findForm(Long id) {
	// return wrap(
	// processEngine.getFormService().createFormQuery()
	// .processDefinitionId(String.valueOf(id)).list()).get(0);
	// }
	//
	// public static List<Form> findFormEntries(int firstResult, int maxResults)
	// {
	// return wrap(processEngine.getFormService().createFormQuery()
	// .listPage(firstResult, maxResults));
	// }
	//
	// public static List<Form> findFormEntries(int firstResult, int maxResults,
	// String sortFieldName, String sortOrder) {
	// // TODO honour sort order
	// return wrap(processEngine.getFormService().createFormQuery()
	// .listPage(firstResult, maxResults));
	// }
	//
	// public static List<Form> findAllForms(String sortFieldName, String
	// sortOrder) {
	// System.out.println("pe: " + processEngine);
	// // TODO honour sort order
	// return wrap(processEngine.getFormService().createFormQuery().list());
	// }

	private static List<Form> wrap(
			final List<org.activiti.engine.form.FormData> list) {
		ArrayList<Form> list2 = new ArrayList<Form>();
		for (org.activiti.engine.form.FormData instance : list) {
			list2.add(new Form(instance));
		}
		return list2;
	}

	public static String toJsonArray(Collection<Form> collection) {
		String[] fields = { "assignee", "createTime", "id", "name", "owner",
				"parentFormId", "priority", "processDefinitionId", "suspended",
				"formDefinitionKey" };
		return toJsonArray(collection, fields);
	}

	public static String toJsonArray(Collection<Form> collection,
			String[] fields) {
		System.out.println("toJsonArray....");
		return new JSONSerializer().exclude("*.class")
				.exclude("*.processEngine").include(fields)
				.serialize(collection);
	}
}
