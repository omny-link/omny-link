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
package link.omny.custmgmt.json;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import link.omny.custmgmt.model.Contact;
import link.omny.custmgmt.model.CustomContactField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class JsonContactDeserializer extends JsonDeserializer<Contact> {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(JsonContactDeserializer.class);

    private static final List<String> FIELDS = Arrays.asList(new String[] {
            "firstName", "lastName", "title", "email", "landLine", "mobile",
            "address1", "address2", "town", "countyOrCity", "postCode",
            "country", "enquiryType", "stage", "enquiryType", "owner",
            "source", "medium", "campaign", "keyword",
            "doNotCall", "doNotEmail", "firstContact",
            "lastUpdated", "tenantId" });

    @Override
    public Contact deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        LOGGER.debug("Deserializing: " + jp.toString());

        ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);

        // This is standard but must be done explicitly so we can intercept
        // custom field serialisation
        Contact contact = new Contact();
        contact.setFirstName(node.get("firstName").asText());
        contact.setLastName(node.get("lastName").asText());
        contact.setTitle(node.get("title") == null ? null : node.get("title")
                .asText());
        contact.setEmail(node.get("email") == null ? null : node.get("email")
                .asText());
        contact.setPhone1(node.get("phone1") == null ? null : node
                .get("phone1").asText());
        contact.setPhone2(node.get("phone2") == null ? null : node
                .get("phone2").asText());
        contact.setAddress1(node.get("address1") == null ? null : node.get(
                "address1").asText());
        contact.setAddress2(node.get("address2") == null ? null : node.get(
                "address2").asText());
        contact.setTown(node.get("town") == null ? null : node.get("town")
                .asText());
        contact.setCountyOrCity(node.get("countyOrCity") == null ? null : node
                .get("countyOrCity").asText());
        contact.setPostCode(node.get("postCode") == null ? null : node.get(
                "postCode").asText());
        contact.setCountry(node.get("country") == null ? null : node.get(
                "country").asText());
        contact.setEnquiryType(node.get("enquiryType") == null ? null : node
                .get("enquiryType").asText());
        contact.setAccountType(node.get("accountType") == null ? null : node
                .get("accountType").asText());
        contact.setStage(node.get("stage") == null ? null : node.get("stage")
                .asText());
        contact.setOwner(node.get("owner") == null ? null : node.get("owner")
                .asText());
        contact.setSource(node.get("source") == null ? null : node
                .get("source").asText());
        contact.setMedium(node.get("medium") == null ? null : node
                .get("medium").asText());
        // TODO
        // contact.setFirstContact(node.get("firstContact").asText());
        // contact.setLastUpdated((Date) node.get("lastUpdated").asObject());
        contact.setTenantId(node.get("tenantId") == null ? null : node.get(
                "tenantId").asText());

        for (Iterator<Entry<String, JsonNode>> it = node.fields(); it.hasNext();) {
            Entry<String, JsonNode> entry = it.next();
            if (!FIELDS.contains(entry.getKey())) {
                contact.addCustomField(new CustomContactField(entry.getKey(),
                        entry.getValue().asText()));
            }
        }
        LOGGER.debug("Found: " + contact);
        return contact;
    }

}
