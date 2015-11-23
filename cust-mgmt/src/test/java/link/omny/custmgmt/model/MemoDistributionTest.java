package link.omny.custmgmt.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

public class MemoDistributionTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Test
    public void testSetRecipients() {
        MemoDistribution dist = new MemoDistribution();
        dist.setRecipients("john,paul,george,ringo");
        assertEquals(4, dist.getRecipientList().size());
    }

    @Test
    public void testSetRecipientList() {
        MemoDistribution dist = new MemoDistribution();
        dist.setRecipientList(Arrays.asList(new String[] { "john", "paul",
                "george", "ringo" }));
        assertEquals(4, dist.getRecipientList().size());
        assertEquals("john,paul,george,ringo", dist.getRecipients());
    }
}
