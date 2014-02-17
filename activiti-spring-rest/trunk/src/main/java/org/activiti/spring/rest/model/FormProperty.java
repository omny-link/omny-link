package org.activiti.spring.rest.model;

import java.util.Collection;

import javax.validation.constraints.NotNull;

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

	public static String toJsonArray(Collection<FormProperty> collection) {
		String[] fields = { "id", "name", "type", "readable", "writeable",
				"datePattern", "enumValues" };
		return toJsonArray(collection, fields);
	}

	public static String toJsonArray(Collection<FormProperty> collection,
			String[] fields) {
		System.out.println("toJsonArray....");
		return new JSONSerializer().exclude("*.class")
				.exclude("*.processEngine").include(fields)
				.serialize(collection);
	}
}
