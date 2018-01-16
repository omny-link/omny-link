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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.knowprocess.beans.model.AccountInfo;
import com.knowprocess.beans.model.ActionType;
import com.knowprocess.beans.model.Contact;
import com.knowprocess.beans.model.LeadActivity;
import com.knowprocess.sugarcrm.api.SugarAccount;
import com.knowprocess.sugarcrm.api.SugarContact;
import com.knowprocess.sugarcrm.api.SugarLead;

public class ModeledConversionTest {

    private static ModelBasedConversionService conversionService;

    @BeforeClass
    public static void setUpClass() throws Exception {
        conversionService = new ModelBasedConversionService();
        conversionService.init("/uml2/domain.xml", "Sugar",
                "com.knowprocess.beans.model", "com.knowprocess.sugarcrm.api");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testLeadActivityToSugarActivity() {
        assertTrue(conversionService.canConvert(LeadActivity.class,
                SugarLead.class));
        LeadActivity lead = new LeadActivity("User read article XYZ");
        lead.setActionType(ActionType.DOWNLOAD);
        assertNotNull(lead.getDateOfActivity());

        SugarLead sLead = conversionService.convert(lead, SugarLead.class);
        System.out.println("sugar activity created: " + sLead);
        assertEquals(lead.getDateOfActivity(), sLead.getDateEntered());
        assertEquals(lead.getDescription(), sLead.getDescription());
        // TODO model conversion currently not handling entity relationships
        // assertNotNull("Action type not found",
        // sLead.getCustom("action_website_c"));
        // assertEquals(lead.getActionType().getId(),
        // sLead.getCustom("action_website_c"));

    }

    @Test
    public void testContactToSugarContact() {
        assertTrue(conversionService.canConvert(Contact.class,
                SugarContact.class));
    }

    @Test
    public void testAccountInfoToSugarAccount() {
        assertTrue(conversionService.canConvert(AccountInfo.class,
                SugarAccount.class));
    }

    @Test
    public void testSugarActivityToLeadActivity() {
        assertTrue(
                "Cannot support expected conversion from SugarLead to LeadActivity",
                conversionService.canConvert(SugarLead.class,
                LeadActivity.class));
        SugarLead sLead = new SugarLead();
        sLead.setDescription("User read article XYZ");
        assertNotNull(sLead.getDescription());
        LeadActivity lead = conversionService
                .convert(sLead, LeadActivity.class);
        System.out.println("sugar activity converted to: " + lead);
        assertEquals(sLead.getDescription(), lead.getDescription());
    }

}
