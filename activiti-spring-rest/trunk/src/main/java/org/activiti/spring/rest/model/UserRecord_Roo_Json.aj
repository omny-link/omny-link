// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.activiti.spring.rest.model;

import flexjson.JSONDeserializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.activiti.spring.rest.model.UserRecord;

privileged aspect UserRecord_Roo_Json {
    
    public static UserRecord UserRecord.fromJsonToUserRecord(String json) {
        return new JSONDeserializer<UserRecord>()
        .use(null, UserRecord.class).deserialize(json);
    }
    
    public static Collection<UserRecord> UserRecord.fromJsonArrayToUserRecords(String json) {
        return new JSONDeserializer<List<UserRecord>>()
        .use("values", UserRecord.class).deserialize(json);
    }
    
}
