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

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import link.omny.custmgmt.model.Account;
import link.omny.custmgmt.model.Contact;
import link.omny.custmgmt.model.CustomAccountField;
import link.omny.custmgmt.model.CustomContactField;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

public class CsvImporter {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CsvImporter.class);

    public List<Contact> readContacts(Reader in, String[] headers)
            throws IOException {
        List<Contact> contacts = new ArrayList<Contact>();
        PropertyDescriptor[] propertyDescriptors = BeanUtils
                .getPropertyDescriptors(Contact.class);
        PropertyDescriptor[] acctPropertyDescriptors = BeanUtils
                .getPropertyDescriptors(Account.class);
        // This JavaDoc is not (currently) true:
        // If your source contains a header record, you can simplify your
        // code and safely reference columns, by using withHeader(String...)
        // with no arguments:
        // CSVFormat.EXCEL.withHeader();
        
        final CSVParser parser = new CSVParser(in,
                CSVFormat.EXCEL.withHeader(headers));
        Iterable<CSVRecord> records = parser.getRecords();
//        Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader(headers)
//                .parse(in);

        for (CSVRecord record : records) {
            // skip header
            if (record.getRecordNumber() > 1) {
                Contact contact = new Contact();
                contact.setAccount(new Account());
                for (PropertyDescriptor pd : propertyDescriptors) {
                    if (record.isMapped(pd.getName())) {
                        setField(contact, pd, record.get(pd.getName()).trim());
                    }
                }

                for (PropertyDescriptor pd : acctPropertyDescriptors) {
                    String name = "account." + pd.getName();
                    if (record.isMapped(name)) {
                        setField(contact.getAccount(), pd, record.get(name)
                                .trim());
                    }
                }
                for (String hdr : headers) {
                    if (hdr.startsWith("account.")) {
                        String atomicHdr = hdr.substring("account.".length());
                        if (BeanUtils.getPropertyDescriptor(Account.class,
                                atomicHdr) == null) {
                            contact.getAccount()
                                    .getCustomFields()
                                    .add(new CustomAccountField(atomicHdr,
                                            record.get(hdr)));
                        }
                    } else {
                        if (BeanUtils.getPropertyDescriptor(Contact.class, hdr) == null) {
                            contact.getCustomFields()
                                    .add(new CustomContactField(hdr, record
                                            .get(hdr)));
                        }
                    }
                }
                contacts.add(contact);
            }
        }
        parser.close();
        return contacts;
    }

    private void setField(Object bean, PropertyDescriptor propertyDescriptor,
            Object value) {
        try {
            Method method = propertyDescriptor.getWriteMethod();
            switch (method.getParameterTypes()[0].getName()) {
            case "boolean":
                method.invoke(bean, Boolean.parseBoolean(value.toString()));
                break;
            default:
                method.invoke(bean, value.toString());
            }

        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            LOGGER.error(String.format("Error parsing CSV into %1$s", bean
                    .getClass().getName()));
        }
    }

}
