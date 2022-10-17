/*******************************************************************************
 * Copyright 2011-2022 Tim Stephenson and contributors
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
package link.omny.custmgmt.services;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.StandardELContext;
import jakarta.el.ValueExpression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import link.omny.custmgmt.internal.DateFormatter;

@Service
public class ElTemplateFiller {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ElTemplateFiller.class);

    public String evaluateTemplate(String template, Map<String,Object> params) throws NoSuchMethodException {
        long start = System.currentTimeMillis();
        LOGGER.info("evaluateTemplate {}", template);
        ExpressionFactory factory = ExpressionFactory.newInstance();
        ELContext context = new StandardELContext(factory);

        for (Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue()==null) {
                context.getVariableMapper().setVariable(entry.getKey(),
                        factory.createValueExpression("", String.class));
            } else {
                context.getVariableMapper().setVariable(entry.getKey(),
                        factory.createValueExpression(entry.getValue(),
                                entry.getValue().getClass()));
            }
        }

        // 'Built-in' expressions
        context.getVariableMapper().setVariable("now",
                factory.createValueExpression(new Date(), Date.class));
        context.getVariableMapper().setVariable("dateFormatter",
                factory.createValueExpression(new DateFormatter(), DateFormatter.class));
        context.getVariableMapper().setVariable("gbpFormatter",
                factory.createValueExpression(DecimalFormat.getCurrencyInstance(), NumberFormat.class));

        ValueExpression expr = factory.createValueExpression(context, template, String.class);
        String html = escape((String) expr.getValue(context));
        LOGGER.info("evaluateTemplate took {}ms", (System.currentTimeMillis()-start));
        return html;
    }

    private String escape(String value) {
        return value.replaceAll("£", "&pound;");
    }

}
