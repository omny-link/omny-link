/*******************************************************************************
 *Copyright 2015-2018 Tim Stephenson and contributors
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
package link.omny.server.web.converters;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import link.omny.custmgmt.model.Contact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

public class CsvBeanConverter extends
        AbstractHttpMessageConverter<List<Serializable>> {

    public static final MediaType MEDIA_TYPE = new MediaType("text", "csv");

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CsvBeanConverter.class);

    public CsvBeanConverter() {
        super(MEDIA_TYPE);
    }

    protected boolean supports(Class<?> clazz) {
        LOGGER.debug("supports {}?", clazz.getName());
        ArrayList<Contact> arrayList = new ArrayList<Contact>();
        // for clazz.getGenericInterfaces())
        // getInterfaces
        return clazz.isInstance(arrayList);
    }

    protected void writeInternal(List<Serializable> list,
            HttpOutputMessage output)
            throws IOException, HttpMessageNotWritableException {
        output.getHeaders().setContentType(MEDIA_TYPE);
        output.getHeaders().set("Content-Disposition",
                "attachment; filename=\"export.csv\"");
        OutputStream out = output.getBody();
        Writer writer = new OutputStreamWriter(out);

        for (Object bean : list) {
            Method method;
            try {
                method = bean.getClass().getMethod("toCsv", (Class[]) null);
                writer.write((String) method.invoke(bean, (Object[]) null));
                writer.write("\n");
            } catch (NoSuchMethodException | SecurityException
                    | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                String msg = "Unable to serialize to CSV";
                LOGGER.error(msg, e);
                throw new IllegalArgumentException(msg, e);
            }
        }
        writer.close();
    }

    @Override
    protected List<Serializable> readInternal(
            Class<? extends List<Serializable>> clazz,
            HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        // TODO Auto-generated method stub
        return null;
    }

}
