package com.knowprocess.collector.api;

public interface Poster {

    Status post(Status status) throws Exception;

    Status createStatus(String statusText, String markup);

}
