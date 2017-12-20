package com.knowprocess.deployment;

import com.knowprocess.bpmn.model.TaskSubType;

public class ActivityModel {

    public final TaskSubType subType;
    
    public final String name;
    
    public final String doc;
    
    public final String actor;
    
    public ActivityModel(TaskSubType subType, String name, String actor, String doc) {
        this.subType = subType;
        this.name = name;
        this.doc = doc;
        this.actor = actor;
    }

    public ActivityModel(TaskSubType subType, String name, String actor) {
        this.subType = subType;
        this.name = name;
        this.doc = null;
        this.actor = actor;
    }

    public ActivityModel(String name, String actor) {
        this.subType = TaskSubType.USER;
        this.name = name;
        this.doc = null;
        this.actor = actor;
    }

    public ActivityModel(String name, String actor, String doc) {
        this.subType = TaskSubType.USER;
        this.name = name;
        this.doc = doc;
        this.actor = actor;
    }
}
