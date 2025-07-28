/*******************************************************************************
 * Copyright 2015-2025 Tim Stephenson and contributors
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
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.CreatedDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import link.omny.supportservices.internal.CsvUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "OL_ACTIVITY")
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class Activity extends Auditable<String> implements Serializable {

    private static final long serialVersionUID = -3132677793751164824L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Activity.class);

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "activityIdSeq", sequenceName = "ol_activity_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "activityIdSeq")
    @JsonProperty
    private Long id;

    @NotNull
    @JsonProperty
    @Column(name = "type")
    private String type;

    @JsonProperty
    @Column(name = "content")
    private String content;
    
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    // Since this is SQL 92 it should be portable
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", name="occurred", updatable = false)
    @JsonProperty
    private Date occurred;

    public Activity() {
        super();
        setOccurred(new Date());
    }

    /**
     * @deprecated {@link #Activity(ActivityType, Date)}
     */
    @Deprecated
    public Activity(String type, Date occurred) {
        super();
        setType(type);
        setOccurred(occurred);
    }

    public Activity(ActivityType type, Date occurred) {
        super();
        setType(type.toString());
        setOccurred(occurred);
    }

    /**
     * @deprecated {@link #Activity(ActivityType, Date)}
     */
    @Deprecated
    public Activity(String type, Date occurred, String content) {
        this(type, occurred);
        setContent(content);
    }

    public Activity(ActivityType type, Date occurred, String content) {
        this(type, occurred);
        setContent(content);
    }

    public String toCsv() {
        StringBuilder sb = new StringBuilder() ;
        sb.append(String.format(
                "%1$d,%2$s,%3$s",
                getId(),
                getType(),
                content == null ? "" : CsvUtils.quoteIfNeeded(content)));
        return sb.toString();
    }

}
