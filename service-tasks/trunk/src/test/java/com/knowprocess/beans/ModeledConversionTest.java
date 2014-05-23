package com.knowprocess.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.knowprocess.beans.model.AccountInfo;
import com.knowprocess.beans.model.Contact;
import com.knowprocess.beans.model.LeadActivity;
import com.knowprocess.sugarcrm.api.SugarAccount;
import com.knowprocess.sugarcrm.api.SugarContact;
import com.knowprocess.sugarcrm.api.SugarLead;

public class ModeledConversionTest {

    private ModelBasedConversionService conversionService;

    @Before
    public void setUp() throws Exception {
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
        assertNotNull(lead.getDateOfActivity());
        SugarLead sLead = conversionService.convert(lead, SugarLead.class);
        System.out.println("sugar activity created: " + sLead);
        assertEquals(lead.getDateOfActivity(), sLead.getDateEntered());
        assertEquals(lead.getDescription(), sLead.getDescription());
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
        SugarLead sLead = new SugarLead("User read article XYZ");
        assertNotNull(sLead.getDescription());
        LeadActivity lead = conversionService
                .convert(sLead, LeadActivity.class);
        System.out.println("sugar activity converted to: " + lead);
        assertEquals(sLead.getDescription(), lead.getDescription());
    }

}
