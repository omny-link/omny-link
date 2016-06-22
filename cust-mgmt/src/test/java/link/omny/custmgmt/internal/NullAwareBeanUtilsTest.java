package link.omny.custmgmt.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import link.omny.custmgmt.model.Contact;

import org.junit.BeforeClass;
import org.junit.Test;

public class NullAwareBeanUtilsTest {

    private static final String FIRST_NAME = "Bart";
    private static final String LAST_NAME = "Simpson";

    private static final String FIRST_NAME_2 = "Ralph";
    private static final String LAST_NAME_2 = "Wiggum";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Test
    public void testCopyNonNullProperties() {
        Contact srcBean = new Contact();
        srcBean.setId(1l);
        srcBean.setFirstName(FIRST_NAME);
        srcBean.setLastName(LAST_NAME);

        Contact trgtBean = new Contact();

        NullAwareBeanUtils.copyNonNullProperties(srcBean, trgtBean, "id");

        assertNull(trgtBean.getId());
        assertEquals(FIRST_NAME, trgtBean.getFirstName());
        assertEquals(LAST_NAME, trgtBean.getLastName());
    }

    @Test
    public void testCopyNonNullPropertiesAvoidingDefault() {
        Contact srcBean = new Contact();
        srcBean.setId(1l);
        srcBean.setFirstName(FIRST_NAME);
        srcBean.setLastName(LAST_NAME);

        Contact trgtBean = new Contact();
        srcBean.setFirstName(FIRST_NAME_2);
        srcBean.setLastName(LAST_NAME_2);

        NullAwareBeanUtils.copyNonNullProperties(srcBean, trgtBean, "id",
                "firstName", "lastName");

        assertNull(trgtBean.getId());
        assertEquals(FIRST_NAME_2, trgtBean.getFirstName());
        assertEquals(LAST_NAME_2, trgtBean.getLastName());
    }
}
