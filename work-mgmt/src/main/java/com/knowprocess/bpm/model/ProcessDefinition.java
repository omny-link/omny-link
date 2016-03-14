package com.knowprocess.bpm.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.persistence.Id;

import lombok.Data;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.form.StartFormData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@Component
public class ProcessDefinition implements Serializable {
    private static final long serialVersionUID = -2657367116355427744L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(ProcessDefinition.class);
	// private static final String[] JSON_FIELDS = { "name", "category",
	// "description", "version", "resourceName", "deploymentId",
	// "diagramResourceName", "key" };

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

    private String tenantId;

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
        if (pd.getDeploymentId() != null) {
            setDeploymentId(Integer.valueOf(pd.getDeploymentId()));
        }
		setDescription(pd.getDescription());
		setVersion(Integer.valueOf(pd.getVersion()));
		setResourceName(pd.getResourceName());
		setDiagramResourceName(pd.getDiagramResourceName());
        setTenantId(pd.getTenantId());
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

    public static List<ProcessDefinition> findAllProcessDefinitions(
            String tenantId) {
        return wrap(processEngine.getRepositoryService()
                .createProcessDefinitionQuery()
                .processDefinitionTenantId(tenantId).list());
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

    public static String findProcessDefinitionAsBpmn(String id) {
        InputStream is = null;
        try {
            is = processEngine.getRepositoryService().getProcessModel(id);
            return new Scanner(is).useDelimiter("\\A").next();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                ;
            }
        }
    }

    public static byte[] findProcessDefinitionDiagram(String id)
            throws IOException {
        InputStream is = null;
        try {
            is = processEngine.getRepositoryService().getProcessDiagram(id);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();

            return buffer.toByteArray();
        } catch (IOException e) {
            String msg = String.format("Unable to read diagram for %1$s", id);
            LOGGER.error(msg, e);
            throw new ActivitiObjectNotFoundException(msg);
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                ;
            }
        }
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

	// public String toJson() {
	// return toJson(JSON_FIELDS);
	// }
	//
	// public String toJson(String[] fields) {
	// return new JSONSerializer().include(fields).exclude("*.class")
	// .exclude("*.processEngine").serialize(this);
	// }
	//
	// public static String toJsonArray(Collection<ProcessDefinition>
	// collection) {
	// return toJsonArray(collection, JSON_FIELDS);
	// }
	//
	// public static String toJsonArray(Collection<ProcessDefinition>
	// collection,
	// String[] fields) {
	// System.out.println("toJsonArray....");
	// return new JSONSerializer().exclude("*.class")
	// .exclude("*.processEngine").include(fields)
	// .serialize(collection);
	// }
}
