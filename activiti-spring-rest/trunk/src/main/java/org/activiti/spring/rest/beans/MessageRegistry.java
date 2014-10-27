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

    protected String getTypeForMessage(final String messageType) {
        String type = properties.getProperty(messageType, messageType).trim();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("Found type %1$s for message type %2$s",
                    type, messageType));
        }
        return type;
    }

    public Object deserialiseMessage(String msgType, String jsonBody) {
        msgType = getTypeForMessage(msgType.trim());
        Object msgBean = null;
        try {
            Class<?> msgClass = getClass().getClassLoader().loadClass(msgType);
            msgBean = msgClass.newInstance();
            Method method;
            if (msgType.equals("com.knowprocess.mail.MailData")) {
                // TODO abusing the MailData class should be replaced with
                // native javax.json support
                method = msgClass.getMethod("fromJson", String.class);
            } else if (jsonBody.trim().startsWith("[")) {
                method = msgClass.getMethod(
                        "fromJsonArrayTo" + msgClass.getSimpleName() + "s",
                        String.class);
            } else {
                method = msgClass.getMethod(
                        "fromJsonTo" + msgClass.getSimpleName(), String.class);
            }
            msgBean = method.invoke(msgBean, jsonBody);
        } catch (Exception e) {
            String msg = "Unable to deserialise message, will pass raw message to process instead.";
            LOGGER.warn(msg, e);
            msgBean = jsonBody;
        }

        return msgBean;
    }
}
