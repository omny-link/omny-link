package link.omny.supportservices.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import link.omny.supportservices.Application;
import link.omny.supportservices.model.NumberSequence;
import link.omny.supportservices.repositories.NumberSequenceRepository;

@RunWith(SpringJUnit4ClassRunner.class)
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
