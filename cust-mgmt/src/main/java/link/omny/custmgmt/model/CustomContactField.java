/*******************************************************************************
 *Copyright 2015-2018 Tim Stephenson and contributors
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
package link.omny.custmgmt.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import link.omny.supportservices.model.CustomField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "OL_CONTACT_CUSTOM")
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "contact" })
@ToString(callSuper = true, exclude = "contact")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NoArgsConstructor
public class CustomContactField extends CustomField {

    private static final long serialVersionUID = -7683896817261973079L;

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "contactCustomIdSeq", sequenceName = "ol_contact_custom_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contactCustomIdSeq")
    @JsonProperty
    private Long id;

    @ManyToOne(optional = false, targetEntity = Contact.class)
    @JsonBackReference
    private Contact contact;

    public CustomContactField(String key, Object value) {
        super(key, value);
    }

}
