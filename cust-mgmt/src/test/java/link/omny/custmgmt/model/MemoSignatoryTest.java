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
package link.omny.custmgmt.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MemoSignatoryTest {

    @Test
    public void testSingleSignatureFormattedForDocuSign() {
        MemoSignatory signatory = new MemoSignatory("${contact.getString('fullName','')}", "${contact.getString('email','')}", 250, 340, 1);
        assertEquals("{"
                + "\"name\": \"${contact.getString('fullName','')}\","
                + "\"email\": \"${contact.getString('email','')}\","
                + "\"recipientId\": \"\","
                + "\"tabs\": { \"signHereTabs\": [{ \"xPosition\": \"250\", \"yPosition\": \"340\", \"documentId\": \"1\", \"pageNumber\": \"1\" }]}"
                + "}", signatory.formatForDocuSign());
        assertEquals("250,340,1", signatory.getTabs());
    }

}
