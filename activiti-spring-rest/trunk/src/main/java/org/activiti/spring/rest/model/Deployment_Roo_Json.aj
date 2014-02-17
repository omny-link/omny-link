// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.activiti.spring.rest.model;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.activiti.spring.rest.model.Deployment;

privileged aspect Deployment_Roo_Json {
    
    public String Deployment.toJson() {
        return new JSONSerializer()
        .exclude("*.class").serialize(this);
    }
    
    public String Deployment.toJson(String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(this);
    }
    
    public static Deployment Deployment.fromJsonToDeployment(String json) {
        return new JSONDeserializer<Deployment>()
        .use(null, Deployment.class).deserialize(json);
    }
    
    public static Collection<Deployment> Deployment.fromJsonArrayToDeployments(String json) {
        return new JSONDeserializer<List<Deployment>>()
        .use("values", Deployment.class).deserialize(json);
    }
    
}
