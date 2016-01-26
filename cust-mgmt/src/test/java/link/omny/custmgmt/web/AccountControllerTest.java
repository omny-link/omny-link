package link.omny.custmgmt.web;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import link.omny.custmgmt.Application;
import link.omny.custmgmt.model.Account;
import link.omny.custmgmt.repositories.AccountRepository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Tim Stephenson
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class AccountControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private AccountController controller;

    @Test
    public void testLifecycle() throws IOException {
        String content = "{\"name\":\"trademark\","
                + "\"companyNumber\":null,\"aliases\":null,"
                + "\"businessWebsite\":\"\",\"shortDesc\":null,"
                + "\"description\":\"test\",\"incorporationYear\":null,"
                + "\"noOfEmployees\":null,\"tenantId\":\"firmgains\","
                + "\"firstContact\":null,\"lastUpdated\":null,"
                + "\"customFields\":{},\"id\":\"338\"}";

        Account acct = objectMapper.readValue(content,
                new TypeReference<Account>() {
                });
        assertNotNull(acct);
    }

}
