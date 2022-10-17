/*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributors
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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

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
@Table(name = "OL_NOTE")
@AllArgsConstructor
@NoArgsConstructor
public class Note extends Auditable<String> implements Serializable {

    private static final long serialVersionUID = 6032851169275605576L;

    protected static final Logger LOGGER = LoggerFactory.getLogger(Note.class);

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "noteIdSeq", sequenceName = "ol_note_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "noteIdSeq")
    @JsonProperty
    private Long id;

    @JsonProperty
    @Size(max = 50)
    @Column(name = "author")
    private String author;

    @JsonProperty
    @Column(name = "content")
    private String content;

    @JsonProperty
    @Column(name = "favorite")
    private boolean favorite = true;

    @JsonProperty
    @Column(name = "confidential")
    private boolean confidential = false;

    public Note(String author, String content) {
        super();
        setAuthor(author);
        setContent(content);
    }

    public Note(String author, String content, boolean favorite) {
        this(author, content);
        setFavorite(favorite);
    }

    public String toCsv() {
        return String.format(
                "%s,%s,%s,%b,%b,%s",
                id, author, created, favorite, confidential, content);
    }

}
