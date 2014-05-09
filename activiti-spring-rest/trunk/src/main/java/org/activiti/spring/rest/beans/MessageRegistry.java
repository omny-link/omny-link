package org.activiti.spring.rest.beans;

import java.lang.reflect.Method;
import java.util.Properties;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class MessageRegistry {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(MessageRegistry.class);

    // initialise empty in case no resource injected
    @Resource(name = "messageAliases")
    protected Properties properties = new Properties();

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    protected String getTypeForMessage(String messageType) {
        return properties.getProperty(messageType, messageType);
    }

    public Object deserialiseMessage(String msgType, String jsonBody) {
        msgType = getTypeForMessage(msgType);
        Object msgBean = null;
        try {
            Class<?> msgClass = getClass().getClassLoader().loadClass(msgType);
            msgBean = msgClass.newInstance();
            Method method = msgClass.getMethod(
                    "fromJsonTo" + msgClass.getSimpleName(), String.class);
            msgBean = method.invoke(msgBean, jsonBody);
        } catch (Exception e) {
            String msg = "Unable to deserialise message, will pass raw message to process instead.";
            LOGGER.warn(msg, e);
            msgBean = jsonBody;
        }

        return msgBean;
    }
}
