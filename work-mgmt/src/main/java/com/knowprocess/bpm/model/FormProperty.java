/*******************************************************************************
 *Copyright 2011-2018 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.knowprocess.bpm.model;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class FormProperty implements Serializable {

    private static final long serialVersionUID = -458420306767602763L;

    /**
     */
    private String id;

    /**
     */
    private String name;

    private String value;

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
        setValue(p.getValue());
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
