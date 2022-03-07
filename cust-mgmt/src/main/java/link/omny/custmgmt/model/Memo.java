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
package link.omny.custmgmt.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jsoup.Jsoup;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import link.omny.supportservices.model.Auditable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Entity
@NamedEntityGraph(name = "memoWithAll", attributeNodes = {
        @NamedAttributeNode("signatories")
})
@Table(name = "OL_MEMO")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
public class Memo extends Auditable<String> implements Serializable {

    private static final long serialVersionUID = 5885971394766699832L;

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "memoIdSeq", sequenceName = "ol_memo_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "memoIdSeq")
    @JsonProperty
    private Long id;

    @JsonProperty
    @Size(max = 50)
    @Column(name = "owner")
    private String owner;

    @JsonProperty
    @NotNull
    @Size(max = 100)
    @Column(name = "name")
    private String name;

    @JsonProperty
    @Size(max = 100)
    @Column(name = "title")
    private String title;

    @JsonProperty
    @Column(name = "required_vars")
    private String requiredVars;

    @JsonProperty
    @Column(name = "rich_content")
    private String richContent;

    @JsonProperty
    @Column(name = "plain_content")
    private String plainContent;

    @JsonProperty
    @Size(max = 140)
    @Column(name = "short_content")
    private String shortContent;

    @NotNull
    @JsonProperty
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @JsonProperty
    @Size(max = 30)
    @Column(name = "status")
    private String status;

    @JsonProperty
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "memo")
    private List<MemoSignatory> signatories;

    public Memo() {
        created = new Date();
    }

    @JsonIgnore
    public void setRequiredVarList(List<String> vars) {
        if (vars == null || vars.size()==0) {
            requiredVars = null;
        } else {
            requiredVars = vars.toString();
        }
    }

    @JsonIgnore
    public List<String> getRequiredVarList() {
        if (requiredVars == null || requiredVars.length()==0) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(requiredVars.split(","));
        }
    }

    public String getPlainContent() {
        if ((plainContent == null || plainContent.trim().length() == 0)
                && richContent != null) {
            plainContent = Jsoup.parse(richContent).text();
        }
        return plainContent;
    }

    public List<MemoSignatory> getSignatories() {
        if (signatories == null) {
            signatories = new ArrayList<MemoSignatory>();
        }
        return signatories;
    }

    public void setSignatories(List<MemoSignatory> signatories) {
        getSignatories().clear();
        getSignatories().addAll(signatories);
    }

    public void addAllSignatories(List<MemoSignatory> signatories2) {
        for (MemoSignatory signatory : signatories2) {
            signatory.setMemo(this);
            getSignatories().add(signatory);
        }
    }

    @PrePersist
    public void preInsert() {
        if (created == null) {
            created = new Date();
        }
    }

    @PreUpdate
    public void preUpdate() {
        cleanHtml();
        lastUpdated = new Date();
    }

    private void cleanHtml() {
        if (richContent != null) {
          setRichContent(richContent.replace("&#39;", "\'"));
        }
    }

    public String toCsv() {
        return String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s",
                id == null ? "" : id.toString(),
                name,
                title,
                status == null ? "Draft" : status,
                owner == null ? "" : owner,
                richContent == null ? "" : richContent.replaceAll("\\n", ""),
                getPlainContent());
    }

    public String formatSignatoriesForDocuSign() {
        StringBuilder sb = new StringBuilder("{\"signers\":[");
        for (int i = 0; i < getSignatories().size(); i++) {
            MemoSignatory sig = getSignatories().get(i);
            sb.append(sig.formatForDocuSign()
                    .replaceAll("\"recipientId\": \"\"", "\"recipientId\": \""+(i+1)+"\""));
            if ((i+1) < getSignatories().size()) {
                sb.append(",");
            }
        }
        sb.append("]}");
        return sb.toString();
    }

}
