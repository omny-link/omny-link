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
     * Expression to evaluate to get the name of the var holding the template.
     */
    protected Expression templateVar;
    
    /**
     * Var to put template result into.
     */
    protected Expression responseVar;
    
    public void setHtmlVar(Expression templateVar) {
        this.templateVar = templateVar;
    }

    public void setResponseVar(Expression responseVar) {
        this.responseVar = responseVar;
    }
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String template = (String) execution.getVariable((String) templateVar.getValue(execution));
        if (template == null) { 
            throw new IllegalStateException("Need to specify a process variable named templateVar containing the variable expression providing the name of the template to merge");
        }
        String outVar = (String) responseVar.getValue(execution);
        if (outVar == null) { 
            throw new IllegalStateException("Need to specify a process variable named responseVar to receive template output");
        }        
        try {
            String result = evaluateTemplate(template, execution.getVariables());
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
        return (String) expr.getValue(context);
    }

}
