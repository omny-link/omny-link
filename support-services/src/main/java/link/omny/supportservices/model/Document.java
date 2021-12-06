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
package link.omny.supportservices.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "OL_DOCUMENT")
@AllArgsConstructor
@NoArgsConstructor
public class Document extends Auditable<String> implements Serializable {

    private static final long serialVersionUID = 157180600778360331L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Document.class);

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "documentIdSeq", sequenceName = "ol_document_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "documentIdSeq")
    @JsonProperty
    private Long id;

    @JsonProperty
    @Size(max = 50)
    @Column(name = "author")
    private String author;

    @JsonProperty
    @Size(max = 60)
    @Column(name = "name")
    private String name;

    @JsonProperty
    @Column(name = "url")
    private String url;

    @JsonProperty
    @Column(name = "favorite")
    private boolean favorite = true;

    @JsonProperty
    @Column(name = "confidential")
    private boolean confidential = false;

    public Document(String author, String url) {
        setAuthor(author);
        setUrl(url);
    }

    public Document(String author, String name, String url) {
        setAuthor(author);
        setName(name);
        setUrl(url);
    }

    public String toCsv() {
        return String.format(
                "%s,%s,%s,%s,%s]", id,
                author, created, name, url);
    }
}
