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
