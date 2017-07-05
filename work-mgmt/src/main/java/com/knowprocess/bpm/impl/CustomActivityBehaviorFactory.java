package com.knowprocess.bpm.impl;

import org.activiti.bpmn.model.ScriptTask;
import org.activiti.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;
import org.activiti.engine.impl.scripting.ScriptingEngines;

public class CustomActivityBehaviorFactory
        extends DefaultActivityBehaviorFactory {

    @Override
    public org.activiti.engine.impl.bpmn.behavior.ScriptTaskActivityBehavior createScriptTaskActivityBehavior(ScriptTask scriptTask) {
        String language = scriptTask.getScriptFormat();
        if (language == null) {
            // TODO should respect expressionLanguage attribute of definitions
          language = ScriptingEngines.DEFAULT_SCRIPTING_LANGUAGE;
        }
        return new ScriptTaskActivityBehavior(scriptTask.getScript(), language, scriptTask.getResultVariable(), scriptTask.isAutoStoreVariables());
      }


}
