package com.knowprocess.resource.spi;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.context.Context;

import com.knowprocess.resource.internal.UrlResource;

// Initially supports Twilio URL Encoded POST but some prelim. support for Form 
// encoded that requires testing. 
public class RestPost extends RestService implements JavaDelegate {

    /**
     * Comma-separated set of fields to POST to the REST resource in the form
     * key=value. May contain expressions.
     */
    protected Expression formFields;

    protected Expression getExpression(DelegateExecution execution,
            String variable) {
        return Context.getProcessEngineConfiguration().getExpressionManager()
                .createExpression(variable);
    }

    protected String getStringFromField(Expression expression,
            DelegateExecution execution) {
        if (expression != null) {
            Object value = expression.getValue(execution);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String resource = (String) globalResource.getValue(execution);
        String usr = (String) (resourceUsername == null ? null
                : resourceUsername.getValue(execution));
        String pwd = (String) (resourcePassword == null ? null
                : resourcePassword.getValue(execution));
        System.out.println("POSTing to " + resource + " as " + usr);

        List<String> ff = Arrays.asList(((String) formFields
                .getExpressionText()).split(","));
        Map<String, String> data = new HashMap<String, String>();
        String response = null;
        for (String field : ff) {
            System.out.println("Field expression: " + field);
            String tmp = getStringFromField(getExpression(execution, field),
                    execution);
            System.out.println("Field: " + tmp);
            String name = tmp.substring(0, tmp.indexOf('='));
            String value = tmp.substring(tmp.indexOf('=') + 1);
            // TODO This is a bit of a hack
            if (value.contains("${")) {
                value = getStringFromField(getExpression(execution, value),
                        execution);
                System.out.println("  : " + value);
            }

            data.put(name, value);
        }

        UrlResource ur = null;
        if (usr == null || pwd == null) {
            ur = new UrlResource();
        } else {
            ur = new UrlResource(usr, pwd);
        }
        InputStream is = null;
        try {
            is = ur.getResource(resource, "POST",
                    "application/x-www-form-urlencoded",
                    "application/json", data);
            response = new Scanner(is).useDelimiter("\\A").next();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                ;
            }
        }

        execution.setVariable(outputVar.getExpressionText(), response);
    }
}
