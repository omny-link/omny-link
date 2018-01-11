/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
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
            String msg = String.format(
                    "ActivitiException executing script task '%1$s' (%2$s) in process %3$s (%4$s). Script error is: %5$s",
                    execution.getCurrentActivityName(),
                    execution.getCurrentActivityId(),
                    execution.getProcessInstanceId(),
                    execution.getProcessDefinitionId(),
                    e.getCause() == null ? e.getMessage()
                            : e.getCause().getMessage());
            LOGGER.error(msg);
            throw new ActivitiException(msg);
        } catch (Throwable e) {
            String msg = String.format("Exception executing script task '%1$s' (%2$s) in process %3$s (%4$s). Script error is: %5$s",
                    execution.getCurrentActivityName(),
                    execution.getCurrentActivityId(),
                    execution.getProcessInstanceId(),
                    execution.getProcessDefinitionId(),
                    e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
            LOGGER.error(msg);
            throw new ActivitiException(msg);
        }
    }

}
