package com.knowprocess.bpm.test.actions;

import org.activiti.bdd.ActivitiSpec;
import org.activiti.bdd.ExternalAction;
import org.activiti.engine.test.ActivitiRule;

import com.knowprocess.bpm.model.HistoricDetail;
import com.knowprocess.bpm.web.ProcessInstanceController;

public class DumpAuditTrail implements ExternalAction {

    private final ActivitiRule activitiRule;

    /**
     * @param activitiRule
     */
    public DumpAuditTrail(ActivitiRule activitiRule) {
        this.activitiRule = activitiRule;
    }

    public void execute(ActivitiSpec spec) throws Exception {
        new com.knowprocess.bpm.model.ProcessInstance()
                .setProcessEngine(activitiRule.getProcessEngine());
        ProcessInstanceController controller = new ProcessInstanceController();
        controller.processEngine = activitiRule.getProcessEngine();
        com.knowprocess.bpm.model.ProcessInstance pi = controller
                .getInstanceIncAuditTrail(spec.getProcessInstance().getId());
        System.out.println("Audit trail: ");
        for (HistoricDetail hist : pi.getAuditTrail()) {
            System.out.println(String.format("  : %1$s", hist));
        }
    }
}