package link.omny.custmgmt.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;

import org.junit.Test;

public class AccountTest {

    @Test
    public void testMergeCustomFields() {
        Account Account = new Account();
        CustomAccountField field1 = new CustomAccountField("field1", "foo");
        field1.setId(1l);
        Account.addCustomField(field1);

        CustomAccountField field2 = new CustomAccountField("field1", "foo");
        assertNull(field2.getId());
        
        Account.setCustomFields(Collections.singletonList(field2));
        
        assertEquals(1, Account.getCustomFields().size());
        assertEquals(field1.getId(), Account.getCustomFields().get(0).getId());
    }

}
