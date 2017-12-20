package com.knowprocess.bpmn.model;

import java.util.Collections;
import java.util.List;

import org.activiti.bpmn.model.ExtensionAttribute;

public enum TaskSubType {
    BUSINESS_RULE, CALL_ACTIVITY, DELETE, GET, JAVASCRIPT, LINK, LOG, MAILING_LIST, MAIL_TEMPLATE, POST, PUT, RECEIVE, SCRIPT, SEND, SERVICE, USER, USER_INFO;

    public static TaskSubType parse(String name) {
        switch (name.toLowerCase()) {
        case "businessrule":
        case "decision":
        case "rule":
            return BUSINESS_RULE;
        case "callactivity":
        case "callprocess":
            return CALL_ACTIVITY;
        case "delete":
            return DELETE;
        case "get":
            return GET;
        case "link":
            return LINK;
        case "mail":
            return MAIL_TEMPLATE;
        case "post":
            return POST;
        case "put":
            return PUT;
        case "script":
        case "javascript":
            return SCRIPT;
        case "receive":
            return RECEIVE;
        case "send":
            return SEND;
        case "service":
            return SERVICE;
        case "user-info":
            return USER_INFO;
        default:
            // Making an assumption that the unrecognised type is an existing process
            return CALL_ACTIVITY;
        }
    }

    public List<ExtensionAttribute> getClassName() {
        ExtensionAttribute attr = new ExtensionAttribute("http://activiti.org/bpmn",
                "class");
        switch (this) {
        case BUSINESS_RULE:
            attr.setValue("com.knowprocess.decisions.DecisionTask");
            return Collections.singletonList(attr);
        case DELETE:
            attr.setValue("com.knowprocess.resource.spi.RestDelete");
            return Collections.singletonList(attr);
        case GET:
            attr.setValue("com.knowprocess.resource.spi.RestGet");
            return Collections.singletonList(attr);
        case JAVASCRIPT:
        case SCRIPT:
            return Collections.emptyList();
        case LINK:
            attr.setValue("com.knowprocess.logging.LoggingService");
            return Collections.singletonList(attr);
        case LOG:
        case SERVICE:
            attr.setValue("com.knowprocess.logging.LoggingService");
            return Collections.singletonList(attr);
        case MAIL_TEMPLATE:
            attr.setValue("com.knowprocess.logging.LoggingService");
            return Collections.singletonList(attr);
        case MAILING_LIST:
            attr.setValue("com.knowprocess.logging.LoggingService");
            return Collections.singletonList(attr);
        case POST:
            attr.setValue("com.knowprocess.resource.spi.RestPost");
            return Collections.singletonList(attr);
        case PUT:
            attr.setValue("com.knowprocess.resource.spi.RestPut");
            return Collections.singletonList(attr);
        case RECEIVE:
            return Collections.emptyList();
        case SEND:
            return Collections.emptyList();
        case USER:
            return Collections.emptyList();
        case USER_INFO:
            attr.setValue("com.knowprocess.logging.LoggingService");
            return Collections.singletonList(attr);
        default:
            attr.setValue("com.knowprocess.logging.LoggingService");
            return Collections.singletonList(attr);
        }
    }
}
