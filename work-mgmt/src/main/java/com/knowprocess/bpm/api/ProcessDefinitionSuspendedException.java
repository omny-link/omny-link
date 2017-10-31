package com.knowprocess.bpm.api;

public class ProcessDefinitionSuspendedException extends RuntimeException {

    private static final long serialVersionUID = 5326388242292011989L;

    public ProcessDefinitionSuspendedException(String msg) {
        super(msg);
    }

    public String toJson() {
        return String.format("{\"error\":\"%1$s\"}", getMessage());
    }
}
