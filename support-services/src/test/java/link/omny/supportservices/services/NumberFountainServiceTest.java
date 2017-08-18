package link.omny.supportservices.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import link.omny.supportservices.Application;
import link.omny.supportservices.model.NumberFountain;
import link.omny.supportservices.repositories.NumberFountainRepository;
import link.omny.supportservices.repositories.NumberFountainService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { Application.class })
public class NumberFountainServiceTest {

    private static final String ENTITY_NAME = "Contact";

    @Autowired
    private NumberFountainRepository nfRepo;

    @Autowired
    private NumberFountainService svc;

    @Test
    public void testNumberFountain() {
        assertNotNull(svc);
        NumberFountain next = svc.getNext(ENTITY_NAME);
        assertNotNull(next);
        assertEquals(ENTITY_NAME, next.getEntityName());
        Long last = next.getLastUsed();
        assertNotNull(last);
        next = svc.getNext(ENTITY_NAME);
        assertNotNull(next);
        assertEquals(ENTITY_NAME, next.getEntityName());
        assertEquals(++last, next.getLastUsed());
    }

    @Test
    public void testNumberFountainFirstUse() {
        nfRepo.deleteAll();
        NumberFountain next = svc.getNext(ENTITY_NAME);
        assertNotNull(next);
        assertEquals(ENTITY_NAME, next.getEntityName());
        assertEquals(new Long(1l), next.getNext());
    }

    @Test
    public void testNumberFountainAfterDatabaseMeddling() {
        nfRepo.save(new NumberFountain(ENTITY_NAME));
        NumberFountain next = svc.getNext(ENTITY_NAME);
        assertNotNull(next);
        assertEquals(ENTITY_NAME, next.getEntityName());
        assertNotNull(next.getNext());
    }
}
