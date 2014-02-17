// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.activiti.spring.rest.model;

import flexjson.JSONDeserializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.activiti.spring.rest.model.Task;

privileged aspect Task_Roo_Json {
    
    public static Task Task.fromJsonToTask(String json) {
        return new JSONDeserializer<Task>()
        .use(null, Task.class).deserialize(json);
    }
    
    public static Collection<Task> Task.fromJsonArrayToTasks(String json) {
        return new JSONDeserializer<List<Task>>()
        .use("values", Task.class).deserialize(json);
    }
    
}
