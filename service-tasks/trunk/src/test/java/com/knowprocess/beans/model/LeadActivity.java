package com.knowprocess.beans.model;

import java.io.Serializable;
import java.util.Date;

public class LeadActivity implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4058597813090948518L;

    private String description;
    private Date dateOfActivity;

    public LeadActivity() {
        super();
        this.dateOfActivity = new Date();
    }

    public LeadActivity(String description) {
        this();
        this.description = description;
    }

    public Date getDateOfActivity() {
        return dateOfActivity;
    }

    public void setDateOfActivity(Date dateOfActivity) {
        this.dateOfActivity = dateOfActivity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

}
