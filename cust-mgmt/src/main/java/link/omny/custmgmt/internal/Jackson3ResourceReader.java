/*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributors
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
package link.omny.custmgmt.internal;

import java.io.InputStream;

import org.springframework.core.io.Resource;
import org.springframework.data.repository.init.ResourceReader;
import org.springframework.util.Assert;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.type.CollectionType;

/**
 * Jackson 3 based {@link ResourceReader} implementation.
 * Replaces Spring Data's Jackson2ResourceReader with Jackson 3 support.
 */
public class Jackson3ResourceReader implements ResourceReader {

    private final ObjectMapper mapper;

    /**
     * Creates a new Jackson3ResourceReader with the given ObjectMapper.
     * 
     * @param mapper must not be {@literal null}.
     */
    public Jackson3ResourceReader(ObjectMapper mapper) {
        Assert.notNull(mapper, "ObjectMapper must not be null!");
        this.mapper = mapper;
    }

    /**
     * Reads the given resource into an {@link Object}.
     * 
     * @param resource must not be {@literal null}.
     * @param classLoader can be {@literal null}.
     * @return the deserialized object
     */
    @Override
    public Object readFrom(Resource resource, ClassLoader classLoader) throws Exception {
        Assert.notNull(resource, "Resource must not be null!");

        try (InputStream stream = resource.getInputStream()) {
            // Try to read as array first
            Class<?> type = classLoader != null ? 
                classLoader.loadClass("java.util.Collection") : 
                java.util.Collection.class;
            
            CollectionType collectionType = mapper.getTypeFactory()
                .constructCollectionType(java.util.ArrayList.class, Object.class);
            
            return mapper.readValue(stream, collectionType);
        } catch (Exception e) {
            // If array reading fails, try as single object
            try (InputStream stream = resource.getInputStream()) {
                return mapper.readValue(stream, Object.class);
            }
        }
    }
}
