package com.knowprocess.bpm.impl;

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

    /**
     * Equivalent to XML configuration:
     * 
     * <pre>
     *   <bean id="messageAliases" class="org.springframework.beans.factory.config.PropertiesFactoryBean">
     *     <property name="location" value="classpath:messageAliases.properties"/>
     *   </bean>
     * </pre>
     */
    
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
        Object msgBean = null;
        try {
            Class<?> msgClass = getClass(msgType);
            Method method = getDeserializeMethod(msgType, msgClass, jsonBody);
            msgBean = method.invoke(msgBean, jsonBody);
        } catch (Exception e) {
            handleException(msgType, e);
            msgBean = jsonBody;
        }

        return msgBean;
    }

    private Method getDeserializeMethod(String msgType, Class<?> msgClass,
            String jsonBody) throws NoSuchMethodException {
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
        return method;
    }

    private Class<?> getClass(String msgType) throws ClassNotFoundException {
        msgType = getTypeForMessage(msgType.trim());
        return getClass().getClassLoader().loadClass(msgType);
    }

    public boolean canDeserialise(String msgType, String jsonBody) {
        // TODO cache reflection results
        try {
            Class<?> clazz = getClass(msgType);
            return getDeserializeMethod(msgType, clazz, jsonBody) != null;
        } catch (ClassNotFoundException e) {
            handleException(msgType, e);
            return false;
        } catch (NoSuchMethodException e) {
            handleException(msgType, e);
            return false;
        }
    }

    private void handleException(String msgType, Exception e) {
        String msg = String
                .format("Unable to deserialise message %1$s, will pass raw message to process instead.",
                        msgType);
        LOGGER.warn(msg);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(msg, e);
        }
    }
}
