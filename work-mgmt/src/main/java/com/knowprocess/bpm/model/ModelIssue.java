package com.knowprocess.bpm.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Entity
@NoArgsConstructor
@Table(name = "OL_MODEL_ISSUE")
public class ModelIssue implements Serializable {

    private static final long serialVersionUID = -6918729558177212977L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    private Long id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String description;

    @JsonProperty
    private String modelRef;

    @JsonProperty
    private String level;

    @ManyToOne(targetEntity = ProcessModel.class)
    @JoinColumn(name = "model_id")
    private ProcessModel model;

    public ModelIssue(String issue) {
        level = issue.substring(0, issue.indexOf(':')).trim();
        description = issue.substring(issue.indexOf(':') + 1).trim();
        modelRef = issue.substring(issue.lastIndexOf(':') + 1).trim();
    }

}
