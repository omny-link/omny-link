package com.knowprocess.resource.spi;

import static org.junit.Assert.fail;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.el.FixedValue;
import org.activiti.engine.impl.el.JuelExpression;
import org.activiti.engine.impl.el.ParsingElContext;
import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ExpressionFactory;
import org.activiti.engine.impl.javax.el.ValueExpression;
import org.activiti.engine.impl.juel.ExpressionFactoryImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.knowprocess.test.activiti.ExtendedRule;

public class RestPostTest {

    @Rule
    public ExtendedRule activitiRule = new ExtendedRule("test-activiti.cfg.xml");

    // private Map<String, Object> vars;

    protected ExpressionFactory expressionFactory;
    // Default implementation (does nothing)
    protected ELContext parsingElContext = new ParsingElContext();

    @Before
    public void setUp() throws Exception {
        expressionFactory = new ExpressionFactoryImpl();
        // Context.setProcessEngineConfiguration((ProcessEngineConfigurationImpl)
        // activitiRule
        // .getProcessEngine().getProcessEngineConfiguration());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    @Ignore
    public void testPostForm() {
        try {
            RestService post = new RestPost();
            post.setGlobalResource(new FixedValue(
                    "http://dev.firmgains.com/wp-admin/admin-ajax.php"));
            post.setResponseVar(new FixedValue("response"));
            post.data = createExpression("action=p_register_async,log=tim@knowprocess.com,user_api_id=fake_id");
            DelegateExecution execution = new ExecutionEntity();
            // Can execute this up to the final executon.setVariable but then
            // fails as no db connection
            // post.execute(execution);
            // assertNotNull(execution.getVariable("response"));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private Expression createExpression(String expression) {
        ValueExpression valueExpression = expressionFactory
                .createValueExpression(parsingElContext, expression.trim(),
                        Object.class);
        return new JuelExpression(valueExpression, expression);
    }

}
