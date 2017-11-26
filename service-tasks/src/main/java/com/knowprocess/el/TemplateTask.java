package com.knowprocess.el;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import java.util.Map.Entry;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.StandardELContext;
import javax.el.ValueExpression;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateTask implements JavaDelegate {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(TemplateTask.class);
    
    /**
     * Expression to evaluate to get the template itself.
     */
    protected Expression template;
    
    /**
     * Expression to evaluate to get the name of the var holding the template.
     */
    protected Expression templateVar;
    
    /**
     * Var to put template result into.
     */
    protected Expression responseVar;
    
    /**
     * @deprecated Use setTemplateVar
     */
    public void setHtmlVar(Expression templateVar) {
        this.templateVar = templateVar;
    }

    public void setTemplateVar(Expression templateVar) {
        this.templateVar = templateVar;
    }

    public void setResponseVar(Expression responseVar) {
        this.responseVar = responseVar;
    }
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String templateVal;
        try {
            templateVal = (String) execution.getVariable((String) templateVar.getValue(execution));
            if (templateVal == null) {
                LOGGER.info("templateVar not specified, try template");
                throw new NullPointerException();
            }
        } catch (NullPointerException e) {
            try {
                templateVal = (String) template.getValue(execution);
                if (templateVal == null) {
                    throw new NullPointerException();
                }
            } catch (NullPointerException e2) {
                throw new IllegalStateException("Need to specify either 'template' that holds the template directly or 'templateVar' containing a process variable expression that resolves to the template");
            }
        }
        LOGGER.debug("TemplateTask.execute inputs {}", templateVal);
        String outVar = (String) responseVar.getValue(execution);
        LOGGER.debug("TemplateTask.execute output {}", outVar);
        if (outVar == null) { 
            throw new IllegalStateException("Need to specify responseVar that names the process variable to receive template output");
        }        
        try {
            String result = evaluateTemplate(templateVal, execution.getVariables());
            execution.setVariable(outVar, result);
        } catch (Throwable e) {
            LOGGER.error(String.format("Unable to transform template in activity '%1$s' within execution '%2$s'", 
                    execution.getCurrentActivityId(), execution.getId()));
            throw e;
        }
    }

    protected String evaluateTemplate(String template, Map<String,Object> params) throws NoSuchMethodException {
        ExpressionFactory factory = ExpressionFactory.newInstance();
        ELContext context = new StandardELContext(factory);

        for (Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue()==null) {
                context.getVariableMapper().setVariable(entry.getKey(), factory.createValueExpression("", String.class));
            } else {
                context.getVariableMapper().setVariable(entry.getKey(), factory.createValueExpression(entry.getValue(), entry.getValue().getClass()));
            }
        }
        
        // 'Built-in' expressions
        context.getVariableMapper().setVariable("dateFormatter", factory.createValueExpression(new DateFormatter(), DateFormatter.class));
        context.getVariableMapper().setVariable("gbpFormatter", factory.createValueExpression(DecimalFormat.getCurrencyInstance(), NumberFormat.class));
        
        ValueExpression expr = factory.createValueExpression(context, template, String.class);
        String html = escape((String) expr.getValue(context));
        return html;
    }

    private String escape(String value) {
        return value.replaceAll("£", "&pound;");
    }

}
