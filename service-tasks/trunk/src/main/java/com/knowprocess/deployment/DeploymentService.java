package com.knowprocess.deployment;

import java.util.logging.Logger;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * Handles deployment of BPMN 2 processes.
 * 
 * <p>
 * Process may be specified as:
 * <ul>
 * <li>HTTP(S) URL (no authentication support at this stage).
 * <li>Stream containing UTF-8 encoded BPMN XML serialisation, for example
 * delivered by file upload.
 * <li>Jar URL (something deployed as part of the application ahead of time).
 * 
 * <p>
 * The following steps are performed in each case:
 * <ol>
 * <li>If necessary, make some basic assumptions in order to make abstract
 * processes executable.
 * <li>Validate the constructs used in the process for support within the
 * engine.
 * <li>Deploy the process to the engine.
 * <li>Start an instance of the process.
 * 
 * @author tstephen
 */
public class DeploymentService implements JavaDelegate {

    private ProcessEngine processEngine;

    public String deployBpmnResource(String resource) {
        String piid = "TODO";
        // fetch
        // tweak
        // validate
        // deploy
        // start
        return piid;
    }

    protected ProcessDefinition deployBpmnResource(String name, String bpmnDefinition)
            throws Exception {
        getLogger().info(String.format("Deploying %1$s", bpmnDefinition));
        RepositoryService repoSvc = getProcessEngine().getRepositoryService();
        // String resource = url.toExternalForm().substring(
        // url.toExternalForm().indexOf(".jar!") + ".jar!".length()
        // + 1);
        try {
            Deployment deployment = repoSvc.createDeployment()
                    .addString(name, bpmnDefinition).deploy();
            getLogger()
                    .info(String.format("... deployment ok: %1$s at %2$s",
                            deployment.getId(), deployment.getDeploymentTime()));

            ProcessDefinition template = getProcessEngine()
                    .getRepositoryService().createProcessDefinitionQuery()
                    .deploymentId(deployment.getId()).singleResult();
            return template;
        } catch (Exception e) {
            getLogger().severe(
                    String.format("Exception during deployment: %1$s: %2$s", e
                            .getClass().getName(), e.getMessage()));
            throw e;
        }

    }

    // private InputStream getInputStream(String resource) {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // private String getResourceName(String resource) {
    // // TODO Auto-generated method stub
    // return null;
    // }

    private ProcessEngine getProcessEngine() {
        if (processEngine == null) {
            processEngine = ProcessEngines.getDefaultProcessEngine();
        }
        return processEngine;
    }

    private Logger getLogger() {
        return Logger.getLogger(getClass().getName());
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        ProcessDefinition definition = deployBpmnResource(
                (String) execution.getVariable("resourceName"),
                (String) execution.getVariable("resource"));
        // Almost certain that we will not be able to persist the resource due
        // to length and anyway no need now so set to null
        execution.setVariable("resource", null);
        execution.setVariable("template", definition);
    }

    /*
     * if (resource.contains(".jar!")) { URL url = new URL(resource); String r =
     * url.toExternalForm().substring( url.toExternalForm().indexOf(".jar!") +
     * ".jar!".length() + 1); deploymentBuilder.addClasspathResource(r); } else
     * if (resource.toLowerCase().startsWith("http")) {
     * deploymentBuilder.addInputStream(getResourceName(resource),
     * getInputStream(resource)); } else {
     * deploymentBuilder.addString(getResourceName(resource),
     * getText(resource)); }
     */
}
