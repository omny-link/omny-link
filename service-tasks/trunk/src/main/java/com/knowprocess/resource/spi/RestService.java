package com.knowprocess.resource.spi;

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;

public abstract class RestService implements JavaDelegate {

    protected Expression resourceUsername;
    protected Expression resourcePassword;
    protected Expression globalResource;
    protected Expression outputVar;

    public void setGlobalResource(Expression globalResource) {
    	this.globalResource = globalResource;
    }

    public void setResourceUsername(Expression resourceUsername) {
    	this.resourceUsername = resourceUsername;
    }

    public void setResourcePassword(Expression resourcePassword) {
    	this.resourcePassword = resourcePassword;
    }
    // public String getOutputVar() {
    // return outputVar;
    // }

    public void setOutputVar(Expression outputVar) {
    	this.outputVar = outputVar;
    }

}
