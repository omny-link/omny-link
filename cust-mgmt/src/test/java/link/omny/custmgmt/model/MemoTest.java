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
package link.omny.custmgmt.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MemoTest {

    @Test
    public void testSetPlainContent() {
        Memo memo = new Memo();
        memo.setRichContent("<h1>The War of the Worlds</h1><br/><h2>By H.G. Wells</h2>");
        assertEquals("The War of the Worlds By H.G. Wells",
                memo.getPlainContent());
    }

    @Test
    public void testSetDefaultSignatories() {
        Memo memo = new Memo();
        memo.getSignatories()
                .add(new MemoSignatory("${owner.getJsonObject(0).getString('fullName','')}",
                        "${owner.getJsonObject(0).getString('email','')}", 250, 250, 1));
        memo.getSignatories()
                .add(new MemoSignatory("${contact.getString('fullName','')}",
                        "${contact.getString('email','')}", 250, 340, 1));
        assertEquals(2,  memo.getSignatories().size());

        assertEquals(250, memo.getSignatories().get(0).getSignHereTabs().get(0).getX());
        assertEquals(250, memo.getSignatories().get(0).getSignHereTabs().get(0).getY());
        assertEquals(1, memo.getSignatories().get(0).getSignHereTabs().get(0).getPage());

        assertEquals(250, memo.getSignatories().get(1).getSignHereTabs().get(0).getX());
        assertEquals(340, memo.getSignatories().get(1).getSignHereTabs().get(0).getY());
        assertEquals(1, memo.getSignatories().get(1).getSignHereTabs().get(0).getPage());
        assertEquals("{\"signers\":[{"
        + "\"name\": \"${owner.getJsonObject(0).getString('fullName','')}\","
        + "\"email\": \"${owner.getJsonObject(0).getString('email','')}\","
        + "\"recipientId\": \"1\","
        + "\"tabs\": { \"signHereTabs\": [{ \"xPosition\": \"250\", \"yPosition\": \"250\", \"documentId\": \"1\", \"pageNumber\": \"1\" }]}"
        + "},{"
        + "\"name\": \"${contact.getString('fullName','')}\","
        + "\"email\": \"${contact.getString('email','')}\","
        + "\"recipientId\": \"2\","
        + "\"tabs\": { \"signHereTabs\": [{ \"xPosition\": \"250\", \"yPosition\": \"340\", \"documentId\": \"1\", \"pageNumber\": \"1\" }]}"
        + "}]}", memo.formatSignatoriesForDocuSign());
    }

    @Test
    public void testToCsv() {
        Memo memo = new Memo();
        memo.setName("BookRecommendation");
        memo.setTitle("Your latest recommended reading");
        memo.setRichContent("<h1>The War of the Worlds</h1>\n<br/>\n<h2>By H.G. Wells</h2>");
        assertEquals(",BookRecommendation,Your latest recommended reading,Draft,,<h1>The War of the Worlds</h1>" +
                "<br/><h2>By H.G. Wells</h2>,The War of the Worlds By H.G. Wells",
                memo.toCsv());
    }
}

