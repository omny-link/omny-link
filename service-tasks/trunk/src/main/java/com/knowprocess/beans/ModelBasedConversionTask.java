package com.knowprocess.beans;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;

public class ModelBasedConversionTask extends ModelBasedConversionService
        implements JavaDelegate {

    private Expression modelResourceExpr;

    private Expression domainExpr;
    private Expression srcPkg;
    private Expression trgtPkgExpr;

    private Expression srcVar;

    private Expression trgtVar;

    public void setModelResource(Expression modelResource) {
        this.modelResourceExpr = modelResource;
    }

    public void setDomain(Expression domain) {
        this.domainExpr = domain;
    }

    public void setSourcePackage(Expression srcPkg) {
        this.srcPkg = srcPkg;
    }

    public void setTargetPackage(Expression trgtPkg) {
        this.trgtPkgExpr = trgtPkg;
    }

    public void setSourceVar(Expression srcVar) {
        this.srcVar = srcVar;
    }

    public void setTargetVar(Expression trgtVar) {
        this.trgtVar = trgtVar;
    }

    @Override
    public void execute(DelegateExecution exec) throws Exception {
        String modelResource = (String) modelResourceExpr.getValue(exec);
        String domain = (String) domainExpr.getValue(exec);
        String trgtPkg = (String) trgtPkgExpr.getValue(exec);
        init(modelResource, domain, (String) srcPkg.getValue(exec), trgtPkg);
        Object trgt = convert(srcVar.getValue(exec));
        exec.setVariable(trgtVar.getExpressionText(), trgt);
    }

}
