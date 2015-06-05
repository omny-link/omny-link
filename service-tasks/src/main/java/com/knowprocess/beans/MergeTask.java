package com.knowprocess.beans;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MergeTask {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(MergeTask.class);
    /**
     * Merge source into target
     * 
     * @param source
     * @param target
     * @throws Exception
     */
    public <T> void merge(T source, T target) throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(source.getClass());
        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {

            // Only attempt to copy writable fields
            if (descriptor.getWriteMethod() == null) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("No write method for: " + descriptor.getName());
                }
            } else {
                Object val = descriptor.getReadMethod().invoke(target);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(String.format("Setting %1$s to %2$s",
                            descriptor.getName(), val));
                }
                descriptor.getWriteMethod().invoke(source, val);
            }

        }
    }

}
