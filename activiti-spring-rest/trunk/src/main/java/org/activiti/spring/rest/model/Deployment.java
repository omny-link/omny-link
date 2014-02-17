package org.activiti.spring.rest.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.activiti.engine.ProcessEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
public class Deployment {

	private static ProcessEngine processEngine;

    /**
     */
    @Column(unique = true)
    private String id;

    /**
     */
	private String name;

	/**
     */
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date deploymentTime;

	/**
     */
	private String category;

	/**
     */
	private String url;

	public Deployment() {
		super();
	}

	public Deployment(org.activiti.engine.repository.Deployment d) {
		this();
		setId(d.getId());
		setName(d.getName());
		setDeploymentTime(d.getDeploymentTime());
		setCategory(d.getCategory());
		// setUrl(d.getUrl());
	}

	// Autowiring static fields is obviously dangerous, but should be ok in this
	// case as PE is thread safe.
	@Autowired(required = true)
	public void setProcessEngine(ProcessEngine pe) {
		Deployment.processEngine = pe;
	}

	public static long countDeployments() {
		return processEngine.getRepositoryService().createDeploymentQuery()
				.count();
	}

	public static List<Deployment> findAllDeployments() {
		return wrap(processEngine.getRepositoryService()
				.createDeploymentQuery().list());
	}

	public static Deployment findDeployment(Long id) {
		return wrap(
				processEngine.getRepositoryService().createDeploymentQuery()
						.deploymentId(String.valueOf(id)).list()).get(0);
	}

	public static List<Deployment> findDeploymentEntries(int firstResult,
			int maxResults) {
		return wrap(processEngine.getRepositoryService()
				.createDeploymentQuery()
				.listPage(firstResult, maxResults));
	}

	public static List<Deployment> findDeploymentEntries(int firstResult,
			int maxResults,
			String sortFieldName, String sortOrder) {
		// TODO honour sort order
		return wrap(processEngine.getRepositoryService()
				.createDeploymentQuery()
				.listPage(firstResult, maxResults));
	}

	public static List<Deployment> findAllDeployments(String sortFieldName,
			String sortOrder) {
		System.out.println("pe: " + processEngine);
		// TODO honour sort order
		return wrap(processEngine.getRepositoryService()
				.createDeploymentQuery().list());
	}

	private static List<Deployment> wrap(
			final List<org.activiti.engine.repository.Deployment> list) {
		ArrayList<Deployment> list2 = new ArrayList<Deployment>();
		for (org.activiti.engine.repository.Deployment instance : list) {
			list2.add(new Deployment(instance));
		}
		return list2;
	}

	public static String toJsonArray(Collection<Deployment> collection) {
		String[] fields = { "assignee", "createTime", "id", "name", "owner",
				"parentDeploymentId", "priority", "processDefinitionId",
				"suspended", "repositoryDefinitionKey" };
		return toJsonArray(collection, fields);
	}

	public static String toJsonArray(Collection<Deployment> collection,
			String[] fields) {
		System.out.println("toJsonArray....");
		return new JSONSerializer().exclude("*.class")
				.exclude("*.processEngine").include(fields)
				.serialize(collection);
	}
}
