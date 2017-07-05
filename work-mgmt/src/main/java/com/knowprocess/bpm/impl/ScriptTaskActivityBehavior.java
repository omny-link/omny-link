package com.knowprocess.bpm.impl;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension class is all about getting better debug information in the event
 * of failure, essential in non-trivial process hierarchies.
 * @author Tim Stephenson
 */
public class ScriptTaskActivityBehavior extends org.activiti.engine.impl.bpmn.behavior.ScriptTaskActivityBehavior {

    private static final long serialVersionUID = -9180026549695880616L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(ScriptTaskActivityBehavior.class);

    public ScriptTaskActivityBehavior(String script, String language,
            String resultVariable, boolean autoStoreVariables) {
       super(script, language, resultVariable, autoStoreVariables);
    }

    @Override
    public void execute(ActivityExecution execution) throws Exception {
        try {
            super.execute(execution);
        } catch (ActivitiException e) {
            String msg = String.format("Exception executing script task '%1$s' (%2$s) in process %3$s (%4$s). Script error is: %5$s",
                    execution.getCurrentActivityName(), execution.getCurrentActivityId(),
                    execution.getProcessInstanceId(), execution.getProcessDefinitionId(),
                    e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
            LOGGER.error(msg);
            throw new ActivitiException(msg);
        }
    }

}
