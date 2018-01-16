/*******************************************************************************
 *Copyright 2011-2018 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
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
    private Expression trgtType;

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

    public void setTargetType(Expression trgtType) {
        this.trgtType = trgtType;
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
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("About to convert %1$s (%2$s) to %3$s",
                    srcVar.getExpressionText(), srcVar.getValue(exec),
                    trgtType.getExpressionText()));
        }
        Class<?> clazz = getClass().getClassLoader().loadClass(
                trgtType.getExpressionText());
        Object trgt = convert(srcVar.getValue(exec), clazz);
        exec.setVariable(trgtVar.getExpressionText(), trgt);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(String.format(
                    "Converted %1$s to %2$s and stored as %3$s",
                    srcVar.getValue(exec), trgt, trgtVar.getExpressionText()));
        }
    }

}
