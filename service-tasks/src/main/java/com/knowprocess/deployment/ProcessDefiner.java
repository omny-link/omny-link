package com.knowprocess.deployment;

import java.util.UUID;

import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.Task;
import org.activiti.bpmn.model.UserTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knowprocess.bpmn.model.TaskType;


/**
 * Create BPMN models from simple textual descriptions. This is not a substitute
 * for a full modeling tool but provides a quick way to 'sketch' processes.
 *
 * @author Tim Stephenson
 */
public class ProcessDefiner {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(ProcessDefiner.class);

    private static final String EOL = System.getProperty("line.separator");

    protected BpmnModel parse(String markup) {
        BpmnModel bpmnModel = new BpmnModel();
        org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();
        process.setId(generateId());

        LOGGER.debug("Adding start event");
        FlowElement previousElement = new StartEvent();
        previousElement.setId(generateId());
        process.addFlowElement(previousElement);

        String[] lines = markup.split(EOL);
        for (String line : lines) {
//            line = line.trim();
            if (line.length() == 0) {
                continue;
            }

            if (Character.isDigit(line.charAt(0))) {
                LOGGER.debug("Adding serial task: " + line);
                // Pattern assignee = Pattern.compile("[+]\\w");
                // // Matcher matcher = Matcher;
                // Matcher matcher = assignee.matcher(line);
                // boolean found = matcher.find();
                // LOGGER.debug(matcher.group());

                Task currentElement = getTask(line);

                process.addFlowElement(currentElement);
                process.addFlowElement(new SequenceFlow(
                        previousElement.getId(), currentElement.getId()));
                previousElement = currentElement;
//            }else if (isUnordered) { 
//                

            }
        }

        LOGGER.debug("Adding end event");
        EndEvent currentElement = new EndEvent();
        currentElement.setId(generateId());
        process.addFlowElement(currentElement);
        process.addFlowElement(new SequenceFlow(previousElement.getId(),
                currentElement.getId()));

        bpmnModel.addProcess(process);
        return bpmnModel;
    }

    protected Task getTask(String markup) {
        Task currentElement = null;
        TaskType taskType = getTaskType(markup);
        switch (taskType) {
        case SERVICE_TASK:
            currentElement = new ServiceTask();
            break;
        case USER_TASK:
            currentElement = new UserTask();
            break;
        default:
            // TODO Activiti does not provide a concrete implementation on
            // unspecified task so assume UserTask
            currentElement = new UserTask();
            break;
        }

        currentElement.setId(generateId());
        if (markup.contains("+")) {
            currentElement.setName(markup.substring(markup.indexOf(' ',
 markup.indexOf('+'))).trim());
        } else {
            currentElement.setName(markup.trim());
        }

        return currentElement;
    }

    protected TaskType getTaskType(String markup) {
        int start = markup.indexOf(":") + 1;
        if (start > 0) {
            int end = markup.indexOf(' ', start);
            String sType = markup.substring(start,
                    end >= start ? end : markup.length());
            switch (sType.toLowerCase()) {
            case "businessrule":
            case "decision":
                return TaskType.BUSINESS_RULE_TASK;
            case "receive":
                return TaskType.RECEIVE_TASK;
            case "script":
            case "javascript":
                return TaskType.SCRIPT_TASK;
            case "send":
                return TaskType.SEND_TASK;
            case "get":
            case "mail":
            case "post":
            case "put":
            case "service":
                return TaskType.SERVICE_TASK;
            default:
                return TaskType.TASK;
            }
        } else {
            if (markup.contains("+")) {
                return TaskType.USER_TASK;
            } else {
                return TaskType.TASK;
            }
        }
    }

    private String generateId() {
        // use underscore since UUIDs can start with numbers which make invalid
        // IDs in XML
        return "_" + UUID.randomUUID().toString();
    }

    protected byte[] convertToBpmn(BpmnModel model, String encoding) {
        BpmnXMLConverter converter = new BpmnXMLConverter();

        BpmnAutoLayout layout = new BpmnAutoLayout(model);
        layout.execute();

        return converter.convertToXML(model, encoding);
    }

    public byte[] convertToBpmn(String markup, String encoding) {
        return convertToBpmn(parse(markup), encoding);
    }
}
