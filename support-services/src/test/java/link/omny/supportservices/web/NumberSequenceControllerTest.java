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
package link.omny.supportservices.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import link.omny.supportservices.Application;
import link.omny.supportservices.model.NumberSequence;
import link.omny.supportservices.repositories.NumberSequenceRepository;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { Application.class })
public class NumberSequenceControllerTest {

    private static final String SEQ_NAME = "Contact";

    private static final String TENANT_ID = "acme";

    @Autowired
    private NumberSequenceRepository nfRepo;

    @Autowired
    private NumberSequenceController svc;

    @Test
    public void testNumberFountain() {
        assertNotNull(svc);
        NumberSequence next = svc.getNext(SEQ_NAME, TENANT_ID);
        assertNotNull(next);
        assertEquals(SEQ_NAME, next.getName());
        Long last = next.getLastUsed();
        assertNotNull(last);
        next = svc.getNext(SEQ_NAME, TENANT_ID);
        assertNotNull(next);
        assertEquals(SEQ_NAME, next.getName());
        assertEquals(++last, next.getLastUsed());
    }

    @Test
    public void testNumberFountainFirstUse() {
        nfRepo.deleteAll();
        NumberSequence next = svc.getNext(SEQ_NAME, TENANT_ID);
        assertNotNull(next);
        assertEquals(SEQ_NAME, next.getName());
        assertEquals(new Long(1l), next.getNext());
    }

    @Test
    public void testNumberFountainAfterDatabaseMeddling() {
        nfRepo.save(new NumberSequence(SEQ_NAME, TENANT_ID));
        NumberSequence next = svc.getNext(SEQ_NAME, TENANT_ID);
        assertNotNull(next);
        assertEquals(SEQ_NAME, next.getName());
        assertNotNull(next.getNext());
    }
}
