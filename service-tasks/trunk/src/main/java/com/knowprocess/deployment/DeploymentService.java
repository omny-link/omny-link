package com.knowprocess.deployment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.engine.EngineServices;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;

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

    private static final String DEPLOYMENT_PROCESS_RESOURCE = "process/com/knowprocess/deployment/DeploymentProcess.bpmn";
    private static final String URL_DEPLOYMENT_PROCESS_RESOURCE = "process/com/knowprocess/deployment/UrlDeploymentProcess.bpmn";
    // public static final String EXECUTABLE_PROCESS_PARTICIPANTS =
    // "processParticipantToExecute";
    private EngineServices processEngine;
    private boolean failedBefore;

    /**
     * Default constructor. Used when executed as service task.
     */
    public DeploymentService() {
        // processEngine = ProcessEngines.getDefaultProcessEngine();
    }

    public DeploymentService(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    public ProcessInstance submitDeploymentRequest(String resourceUrl) {
        Map<String, Object> vars = new HashMap<String, Object>();
        return submitDeploymentRequest(resourceUrl, vars);
    }

    public ProcessInstance submitDeploymentRequest(String resourceName,
            String resourceBody, Map<String, Object> vars) {
        vars.put("resourceName", resourceName);
        vars.put("resource", resourceBody.getBytes());
        ProcessInstance processInstance = null;
        try {
            RuntimeService runtimeService = getProcessEngine()
                    .getRuntimeService();
            processInstance = runtimeService.startProcessInstanceByKey(
                    "deploymentProcess", vars);

            System.out.println("id " + processInstance.getId() + " "
                    + processInstance.getProcessDefinitionId());
        } catch (org.activiti.engine.ActivitiObjectNotFoundException e) {
            commission(e);
            processInstance = submitDeploymentRequest(resourceName,
                    resourceBody, vars);
        }
        return processInstance;
    }

    public ProcessInstance submitDeploymentRequest(String resourceUrl,
            Map<String, Object> deploymentInstructions) {
        deploymentInstructions.put("resource", resourceUrl);

        ProcessInstance processInstance = null;
        try {
            RuntimeService runtimeService = getProcessEngine()
                    .getRuntimeService();
            processInstance = runtimeService.startProcessInstanceByKey(
                    "urlDeploymentProcess", deploymentInstructions);

            System.out.println("id " + processInstance.getId() + " "
                    + processInstance.getProcessDefinitionId());
        } catch (org.activiti.engine.ActivitiObjectNotFoundException e) {
            commission(e);
            processInstance = submitDeploymentRequest(resourceUrl,
                    deploymentInstructions);
        }
        return processInstance;
    }

    private void commission(
            org.activiti.engine.ActivitiObjectNotFoundException e) {
        if (failedBefore) {
            throw e;
        } else {
            failedBefore = true;
            System.out
                    .println("Exception starting deploymentProcess, assume new system so try commissioning");
            deployDeploymentProcess(DEPLOYMENT_PROCESS_RESOURCE);
            deployDeploymentProcess(URL_DEPLOYMENT_PROCESS_RESOURCE);
        }
    }

    protected Deployment deployBpmnResource(String name, String bpmnDefinition)
            throws Exception {
        getLogger().info(
                String.format("Deploying as %1$s: %2$s", name, bpmnDefinition));
        RepositoryService repoSvc = getProcessEngine().getRepositoryService();
        // String resource = url.toExternalForm().substring(
        // url.toExternalForm().indexOf(".jar!") + ".jar!".length()
        // + 1);
        try {
            Deployment deployment = repoSvc.createDeployment()
                    .addString(name, bpmnDefinition).deploy();
            if (deployment instanceof DeploymentEntity) {
                System.out.println("deployment entity returned");
                DeploymentEntity de = (DeploymentEntity) deployment;
                System.out.println("deployment entity returned");
                // List<ProcessDefinition> list = de
                // .getDeployedArtifacts(ProcessDefinition.class);
                //
                // ProcessDefinition processDefinition = list.get(0);
                // System.out.println(processDefinition);
            }
            getLogger()
                    .info(String.format("... deployment ok: %1$s at %2$s",
                            deployment.getId(), deployment.getDeploymentTime()));
            //
            // Deployment result = repoSvc.createDeploymentQuery()
            // .deploymentId(deployment.getId()).singleResult();
            // assert result != null;
            //
            // ProcessDefinition template =
            // repoSvc.createProcessDefinitionQuery()
            // .deploymentId(deployment.getId()).singleResult();
            // System.out.println("template: " + template);
            // return template;
            return deployment;
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

    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    private EngineServices getProcessEngine() {
        // if (processEngine == null) {
        // System.out
        // .println("You should ensure a process engine is injected in prod environments this is a testing convenience only!");
        // processEngine = ProcessEngines.getDefaultProcessEngine();
        // }
        return processEngine;
    }

    private Logger getLogger() {
        return Logger.getLogger(getClass().getName());
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        processEngine = execution.getEngineServices();

        String resource = null;
        Object tmp = execution.getVariable("resource");
        if (tmp instanceof String) {
            resource = (String) tmp;
        } else {
            resource = new String((byte[]) tmp);
        }

        // TODO for some reason the execution cannot query proc def before the
        // JavaDelegate has completed
        // ProcessDefinition definition = deployBpmnResource(
        // (String) execution.getVariable("resourceName"), resource);
        // System.out.println("Template: " + definition);
        // execution.setVariable("templateId", definition.getId());
        // execution.setVariable("deploymentId", value);
        Deployment deployment = deployBpmnResource(
                (String) execution.getVariable("resourceName"), resource);
        execution.setVariable("deploymentId", deployment.getId());
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

    private void deployDeploymentProcess(String resource) {
        RepositoryService repositoryService = getProcessEngine()
                .getRepositoryService();
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource(resource).deploy();
        System.out.println("deployment returned: " + deployment);

        List<Deployment> deployments = getProcessEngine()
                .getRepositoryService().createDeploymentQuery().list();
        for (Deployment d : deployments) {
            System.out.println("deployment from search: " + d.getName() + "("
                    + d.getId() + ")");
        }
    }
}
