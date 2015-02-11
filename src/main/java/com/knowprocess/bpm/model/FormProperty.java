package com.knowprocess.bpm.model;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class FormProperty {

    /**
     */
    private String id;

    /**
     */
    private String name;

    /**
     */
    private String type;

    /**
     */
    private Boolean readable;

    /**
     */
    private Boolean writeable;

    /**
     */
    @NotNull
    private Boolean required;

    /**
     */
    private String datePattern;

    /**
     */
    private String enumValues;

	public FormProperty() {
		super();
	}

	public FormProperty(org.activiti.engine.form.FormProperty p) {
		this();
		setId(p.getId());
		setName(p.getName());
		setType(p.getType() == null ? "String" : p.getType().getName());
		setReadable(p.isReadable());
		setWriteable(p.isWritable());
		setRequired(p.isRequired());
	}

	// public static String toJsonArray(Collection<FormProperty> collection) {
	// String[] fields = { "id", "name", "type", "readable", "writeable",
	// "datePattern", "enumValues" };
	// return toJsonArray(collection, fields);
	// }
	//
	// public static String toJsonArray(Collection<FormProperty> collection,
	// String[] fields) {
	// System.out.println("toJsonArray....");
	// return new JSONSerializer().exclude("*.class")
	// .exclude("*.processEngine").include(fields)
	// .serialize(collection);
	// }
}
