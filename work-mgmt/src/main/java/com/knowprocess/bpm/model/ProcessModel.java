package com.knowprocess.bpm.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "OL_PROCESS_MODEL")
public class ProcessModel implements
        org.activiti.engine.repository.ProcessDefinition {

    @Id
    @Column(name = "id")
    // @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String category;

    @JsonProperty
    private String description;

    @JsonProperty
    private int version;

    @JsonProperty
    @Column(name = "resource_name")
    private String resourceName;

    @JsonProperty
    @Column(name = "deployment_id")
    private Integer deploymentId;

    @Lob
    @JsonProperty
    @Column(name = "diag_resource_name")
    private String diagramResourceName;

    @Column(name = "PROC_KEY")
    @JsonProperty
    private String key;

    @Lob
    @JsonProperty
    @Column(name = "bpmn_string")
    private String bpmnString;

    @Temporal(TemporalType.TIMESTAMP)
    // Since this is SQL 92 it should be portable
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    @JsonProperty
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty
    @Column(name = "last_updated")
    private Date lastUpdated;

    @JsonProperty
    @Column(name = "tenant_id")
    private String tenantId;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "model")
    private List<ModelIssue> issues;

    public void setBpmnString(String bpmn) {
        this.bpmnString = bpmn.replaceAll("\n", "").replaceAll("\r", "");
    }

    @Override
    public String getDeploymentId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasStartFormKey() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasGraphicalNotation() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isSuspended() {
        return true;
    }

    public List<ModelIssue> getIssues() {
        if (issues == null) {
            issues = new ArrayList<ModelIssue>();
        }
        return issues;
    }

    public void setIssuesAsString(String issues2) {
        String[] tmpIssues = issues2.split("\n");
        for (String issue : tmpIssues) {
            issue = issue.trim();
            if (issue.length() > 0
                    && !issue.toLowerCase().startsWith("ignored")
                    && !issue.toLowerCase().startsWith("pass")
                    && !issue.toLowerCase().startsWith("unsupported")) {
                ModelIssue mi = new ModelIssue(issue);
                mi.setModel(this);
                getIssues().add(mi);
            }
        }
    }

}
