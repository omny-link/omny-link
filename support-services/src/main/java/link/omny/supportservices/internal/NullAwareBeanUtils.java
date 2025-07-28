/*******************************************************************************
 * Copyright 2015-2025 Tim Stephenson and contributors
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
package link.omny.supportservices.internal;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.InvalidPropertyException;

public class NullAwareBeanUtils {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(NullAwareBeanUtils.class);

	public static Set<String> getNullPropertyNames(Object source) {
		final BeanWrapper src = new BeanWrapperImpl(source);
		PropertyDescriptor[] pds = src.getPropertyDescriptors();
	
		Set<String> emptyNames = new HashSet<String>();
		for (PropertyDescriptor pd : pds) {
            try {
                if (pd.getReadMethod() == null
                        || src.getPropertyValue(pd.getName()) == null) {
                    emptyNames.add(pd.getName());
                }
            } catch (InvalidPropertyException e) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(String
                            .format("Unable to read property %1$s, assume null. Cause: %2$s: %3$s",
                                    pd.getName(), e.getClass().getName(),
                                    e.getMessage()));
                }
                emptyNames.add(pd.getName());
            }
		}
		return emptyNames;
	}

	public static void copyNonNullProperties(Object srcBean,
			Object trgtBean) {
		Set<String> ignoreNames = getNullPropertyNames(srcBean);
		String[] ignoreProperties = new String[ignoreNames.size()];
		BeanUtils.copyProperties(srcBean, trgtBean,
				ignoreNames.toArray(ignoreProperties));
	}

	public static void copyNonNullProperties(Object srcBean,
			Object trgtBean,
			String... additionalIgnores) {
		Set<String> ignoreNames = getNullPropertyNames(srcBean);
		for (String prop : additionalIgnores) {
			ignoreNames.add(prop);
		}
		String[] ignoreProperties = new String[ignoreNames.size()];
		BeanUtils.copyProperties(srcBean, trgtBean,
				ignoreNames.toArray(ignoreProperties));
	}

    public static void trimStringProperties(Object srcBean) {
        final BeanWrapper src = new BeanWrapperImpl(srcBean);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        for (PropertyDescriptor pd : pds) {
            if (pd.getPropertyType().equals(String.class)
                    && pd.getWriteMethod() != null
                    && pd.getReadMethod() != null
                    && src.getPropertyValue(pd.getName()) != null) {
                Optional<String> val = Optional.of((String) src.getPropertyValue(pd.getName()));
                if (val.isPresent()) {
                    src.setPropertyValue(pd.getName(), val.get().trim());
                }
            }
        }
    }
}
