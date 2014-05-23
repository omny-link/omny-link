package com.knowprocess.beans;


public class MessageRegistry extends
        org.activiti.spring.rest.beans.MessageRegistry {

    @Override
    public Object deserialiseMessage(String msgType, String jsonBody) {
        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX Override!!!!!!!");
        return super.deserialiseMessage(msgType, jsonBody);
    }

}
