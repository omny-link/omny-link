package com.knowprocess.deployment;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.transform.TransformerConfigurationException;

import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.BusinessRuleTask;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ReceiveTask;
import org.activiti.bpmn.model.ScriptTask;
import org.activiti.bpmn.model.SendTask;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.StringDataObject;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.Task;
import org.activiti.bpmn.model.UserTask;
import org.activiti.bpmn.model.ValuedDataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knowprocess.bpmn.model.TaskSubType;
import com.knowprocess.xslt.TransformTask;

/**
 * Create BPMN models from simple textual descriptions. This is not a substitute
 * for a full modeling tool but provides a quick way to 'sketch' processes.
 *
 * @author Tim Stephenson
 */
public class ProcessDefiner {

    private static final int MAX_TASK_NAME_LENGTH = 30;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(ProcessDefiner.class);

    private static final String EOL = System.getProperty("line.separator");

    private static final String POSTPROCESSOR_RESOURCES = "/xslt/ProcessDefinerPostProcessor.xsl";

    private TransformTask postProcessor;

    protected BpmnModel parse(String markdown, String rootProcessName) {
        BpmnModel bpmnModel = new BpmnModel();
        org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();
        process.setId(generateId());
        process.setName(rootProcessName);

        LOGGER.debug("Adding start event");
        FlowElement previousElement = new StartEvent();
        previousElement.setId(generateId());
        process.addFlowElement(previousElement);

        String[] lines = markdown.split(EOL);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            // line = line.trim();
            if (line.length() == 0) {
                continue;
            }

            if (i < (lines.length - 1)
                    && Character.isWhitespace(lines[i + 1].charAt(0))) {
                // TODO no support for nested sub-proc as yet
                SubProcess subProcess = new SubProcess();
                subProcess.setId(generateId());
                subProcess
                        .setName(line.substring(markdown.indexOf(' ')).trim());

                process.addFlowElement(subProcess);
                process.addFlowElement(new SequenceFlow(
                        previousElement.getId(), subProcess.getId()));
                previousElement = subProcess;

                i++;
                FlowElement spPreviousElement = null;
                while (i < (lines.length - 1)
                        && Character.isWhitespace(lines[i + 1].charAt(0))) {
                    Task spCurrentElement = getTask(lines[i]);

                    subProcess.addFlowElement(spCurrentElement);
                    if (spPreviousElement != null) {
                        subProcess.addFlowElement(new SequenceFlow(
                                spPreviousElement.getId(), spCurrentElement
                                        .getId()));
                    }
                    spPreviousElement = spCurrentElement;
                    i++;
                }

            } else if (Character.isDigit(line.charAt(0))) {
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
                // }else if (isUnordered) {
                //

            }

            // Now data ...
            // TODO does not appear in output
            List<ValuedDataObject> dataObjects = new ArrayList<ValuedDataObject>();
            ValuedDataObject datum = new StringDataObject();
            datum.setName("test");
            dataObjects.add(datum);
            process.setDataObjects(dataObjects);
//            System.out.println("XXX"+process.getDataObjects());
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

    protected Task getTask(String markdown) {
        Task currentElement = null;
        TaskSubType taskType = getTaskType(markdown);
        switch (taskType) {
        case BUSINESS_RULE:
            currentElement = new BusinessRuleTask();
            break;
        case GET:
        case LINK:
        case LOG:
        case MAILING_LIST:
        case MAIL_TEMPLATE:
        case POST:
        case PUT:
        case SERVICE:
        case USER_INFO:
            currentElement = new ServiceTask();
            // TODO does not appear in output
            currentElement.getAttributes().put("class", taskType.getClassName());
            break;
        case RECEIVE:
            currentElement = new ReceiveTask();
            break;
        case SCRIPT:
        case JAVASCRIPT:
            currentElement = new ScriptTask();
            break;
        case SEND:
            currentElement = new SendTask();
            break;
        case USER:
            currentElement = new UserTask();
            setResources((UserTask) currentElement, markdown);
            break;
        default:
            // TODO Activiti does not provide a concrete implementation on
            // unspecified task so assume UserTask
            currentElement = new UserTask();
            break;
        }

        currentElement.setId(generateId());
        String text;
        if (markdown.contains("+")) {
            text = markdown.substring(
                    markdown.indexOf(' ', markdown.indexOf('+'))).trim();
        } else {
            text = markdown.trim();
        }
        if (text.indexOf('.') != -1) {
            currentElement.setName(text.substring(0, text.indexOf('.')));
            currentElement
                    .setDocumentation(text.substring(text.indexOf('.') + 1));
        } else if (text.length() > MAX_TASK_NAME_LENGTH) {
            currentElement.setName(text.substring(0, MAX_TASK_NAME_LENGTH - 1)
                    + "…");
            currentElement.setDocumentation(text);
        } else {
            currentElement.setName(text);
        }
        return currentElement;
    }

    protected void setResources(UserTask task, String markdown) {
        int start = markdown.indexOf('+') + 1;
        String resource = markdown.substring(start,
                markdown.indexOf(' ', start));
        if (resource.indexOf('@') == -1) {
            task.getCandidateGroups().add(resource);
        } else {
            task.getCandidateUsers().add(resource);
        }
    }

    protected TaskSubType getTaskType(String markdown) {
        int start = markdown.indexOf(":") + 1;
        if (start > 0) {
            int end = markdown.indexOf(' ', start);
            String sType = markdown.substring(start, end >= start ? end
                    : markdown.length());
            return TaskSubType.parse(sType);
        } else if (markdown.contains("+")) {
            return TaskSubType.USER;
        }
        return TaskSubType.LOG;
    }

    private String generateId() {
        // use underscore since UUIDs can start with numbers which make invalid
        // IDs in XML
        return "_" + UUID.randomUUID().toString();
    }

    protected byte[] convertToBpmn(BpmnModel model, String encoding)
            throws UnsupportedEncodingException {
        BpmnXMLConverter converter = new BpmnXMLConverter();

        BpmnAutoLayout layout = new BpmnAutoLayout(model);
        layout.execute();

        return getPreProcessor().transform(
                new String(converter.convertToXML(model, encoding), encoding))
                .getBytes();
    }

    private TransformTask getPreProcessor() {
        // if (preProcessor == null) {
        postProcessor = new TransformTask();
        try {
            postProcessor.setXsltResources(POSTPROCESSOR_RESOURCES);
        } catch (TransformerConfigurationException e) {
            LOGGER.error(String.format(
                    "Unable to load process definer post-processors: %1$s",
                    POSTPROCESSOR_RESOURCES), e);
        }
        // }
        return postProcessor;
    }

    public byte[] convertToBpmn(String markdown, String rootProcessName,
            String encoding)
            throws UnsupportedEncodingException {
        return convertToBpmn(parse(markdown, rootProcessName), encoding);
    }
}
