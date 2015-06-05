package com.knowprocess.beans.model;

public class ActionType {

    public static ActionType DOWNLOAD = new ActionType("DOWNLOAD", "Download");
    private String id;
    private String name;

    public ActionType() {
    }

    public ActionType(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
