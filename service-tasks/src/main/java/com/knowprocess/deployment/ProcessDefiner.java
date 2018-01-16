/*******************************************************************************
 *Copyright 2011-2018 Tim Stephenson and contributors
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
package com.knowprocess.deployment;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerConfigurationException;

import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.BusinessRuleTask;
import org.activiti.bpmn.model.CallActivity;
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

    private static final int TASK_WIDTH = 110;

    private static final int TASK_HEIGHT = 70;

    private static final int MAX_TASK_NAME_LENGTH = 20;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(ProcessDefiner.class);

    private static final String EOL = System.getProperty("line.separator");

    private static final String POSTPROCESSOR_RESOURCES = "/xslt/ProcessDefinerPostProcessor.xsl";

    private TransformTask postProcessor;

    protected BpmnModel parse(String markdown, String rootProcessName) {
        BpmnModel bpmnModel = new BpmnModel();
        org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();
        process.setId(toIdentifier(rootProcessName));
        process.setName(rootProcessName);

        LOGGER.debug("Adding start event");
        FlowElement previousElement = new StartEvent();
        previousElement.setId("startEvent");
        // TODO need to fix label bounds
//        previousElement.setName("Start event");
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
                subProcess.setName(line.substring(line.indexOf(' ')).trim());

                process.addFlowElement(subProcess);
                process.addFlowElement(new SequenceFlow(
                        previousElement.getId(), subProcess.getId()));
                previousElement = subProcess;

                i++;
                FlowElement spPreviousElement = null;
                while (i < (lines.length - 1)
                        && Character.isWhitespace(lines[i + 1].charAt(0))) {
                    Activity spCurrentElement = getActivity(lines[i].trim());

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

                Activity currentElement = getActivity(line);

                process.addFlowElement(currentElement);
                SequenceFlow seqFlow = new SequenceFlow(
                        previousElement.getId(), currentElement.getId());
                seqFlow.setId(String.format("%1$s-%2$s",
                        previousElement.getId(), currentElement.getId()));
                process.addFlowElement(seqFlow);
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
        currentElement.setId("endEvent");
        // TODO need to fix label bounds
        // currentElement.setName("End event");
        process.addFlowElement(currentElement);
        process.addFlowElement(new SequenceFlow(previousElement.getId(),
                currentElement.getId()));

        bpmnModel.addProcess(process);
        return bpmnModel;
    }

    protected Activity getActivity(String markdown) {
        Activity currentElement = null;
        ActivityModel activityModel = parseLine(markdown);
        TaskSubType taskType = activityModel.subType;
        switch (taskType) {
        case BUSINESS_RULE:
            currentElement = new BusinessRuleTask();
            break;
        case CALL_ACTIVITY:
            currentElement = new CallActivity();
            ((CallActivity) currentElement).setCalledElement(activityModel.actor);
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
            ((ScriptTask) currentElement).setScript("// TODO");
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

        currentElement.setId(toIdentifier(activityModel.name));
        currentElement.setName(wrapLines(activityModel.name));
        currentElement.setDocumentation(activityModel.doc);
        return currentElement;
    }

    private String toIdentifier(String text) {
        if (text == null) return generateId();
        String leadingCaps = toLeadingCaps(text).replaceAll("[\\s-\\.]", "");
        return leadingCaps.substring(0,1).toLowerCase()+leadingCaps.substring(1);
    }

    private String toLeadingCaps(String text) {
        if (text == null) return generateId();
        String[] strings = text.split(" ");
        StringBuffer sb = new StringBuffer();
        for (String s : strings) {
            sb.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).append(" ");
        }
        return sb.toString();
    }

    protected String wrapLines(String name) {
        StringBuffer sb = new StringBuffer();
        while (name.length() > MAX_TASK_NAME_LENGTH) {
            int idx = name.indexOf(' ', MAX_TASK_NAME_LENGTH);
            if (idx == -1) {
                sb.append(name.substring(0).trim()).append('\n');
                name = "";
            } else {
                sb.append(name.substring(0, idx).trim()).append('\n');
                name = name.substring(idx).trim();
            }
        }
        sb.append(name);
        return sb.toString().trim();
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

    protected ActivityModel parseLine(String markdown) {
        LOGGER.info("parseLine: {}", markdown);
        Pattern p = Pattern.compile("^([0-9]). (:(?<subType>[a-zA-Z]*) )?(\\+(?<owner>[a-zA-Z@\\.]*) )?(?<name>[a-zA-Z0-9!\"£$%^&\\*\\(\\)_\\-\\+=\\. ]+)(// ?(?<comment>.*))?$");
        Matcher m = p.matcher(markdown);
        if (!m.matches()) {
            throw new IllegalArgumentException(String.format("%1$s is not a valid line of process markdown", markdown));
        }
        if (m.group("subType") == null && m.group("owner") != null) {
            if (m.group("comment") == null) {
                return new ActivityModel(m.group("name").trim(), m.group("owner"));
            } else {
                return new ActivityModel(m.group("name").trim(), m.group("owner"), m.group("comment").trim());
            }
        } else {
            String actor = m.group("owner");
            if (m.group("subType") != null && TaskSubType.parse(m.group("subType")) == TaskSubType.CALL_ACTIVITY) {
                actor = m.group("subType");
            }
            if (m.group("comment") != null && m.group("subType") != null) {
                return new ActivityModel(TaskSubType.parse(m.group("subType")), m.group("name").trim(), actor, m.group("comment").trim());
            } else if (m.group("comment") == null && m.group("subType") != null) {
                return new ActivityModel(TaskSubType.parse(m.group("subType")), m.group("name").trim(), actor);
            } else if (m.group("comment") == null && m.group("subType") == null) {
                return new ActivityModel(TaskSubType.LOG, m.group("name").trim(), actor);
            }
            throw new RuntimeException(String.format("%1$s is an unsupported combination of process markdown", markdown));
        }
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
        layout.setTaskHeight(TASK_HEIGHT);
        layout.setTaskWidth(TASK_WIDTH);
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
