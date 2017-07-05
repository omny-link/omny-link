package com.knowprocess.bpmn;

public class BpmnRestHelper {

    public static final String ID = "bpmnRest";

    public String tenantUri(String tenant, String uri) {
        int startPath = uri.indexOf("/", uri.indexOf("//")+2);
        return String.format("%1$s/%2$s%3$s", uri.substring(0, startPath), tenant, uri.substring(startPath));
    }

    public Long uriToDbId(String uri) {
        if (uri == null) {
            throw new IllegalArgumentException(String.format("URI %1$s is null", uri));
        }

        return Long.parseLong(uri.substring(uri.lastIndexOf('/')+1));
    }

}
