package link.omny.custmgmt.web;

import java.io.IOException;
import java.util.List;

import link.omny.custmgmt.model.Account;
import link.omny.custmgmt.repositories.AccountRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * REST web service for uploading and accessing a file of JSON Accounts (over
 * and above the CRUD offered by spring data).
 * 
 * @author Tim Stephenson
 */
@Controller
@RequestMapping(value = "/{tenantId}/accounts")
public class AccountController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AccountController.class);

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Imports JSON representation of accounts.
     * 
     * <p>
     * This is a handy link: http://shancarter.github.io/mr-data-converter/
     * 
     * @param file
     *            A file posted in a multi-part request
     * @return The meta data of the added model
     * @throws IOException
     *             If cannot parse the JSON.
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public @ResponseBody Iterable<Account> handleFileUpload(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "file", required = true) MultipartFile file)
            throws IOException {
        LOGGER.info(String.format("Uploading accounts for: %1$s", tenantId));
        String content = new String(file.getBytes());

        List<Account> list = objectMapper.readValue(content,
                new TypeReference<List<Account>>() {
                });
        LOGGER.info(String.format("  found %1$d accounts", list.size()));
        for (Account account : list) {
            account.setTenantId(tenantId);
        }

        Iterable<Account> result = accountRepo.save(list);
        LOGGER.info("  saved.");

        return result;
    }

    /**
     * Create a new contact.
     * 
     * @return
     */
    @ResponseStatus(value = HttpStatus.CREATED)
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<?> create(
            @PathVariable("tenantId") String tenantId,
            @RequestBody Account account, UriComponentsBuilder builder) {
        account.setTenantId(tenantId);
        accountRepo.save(account);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/contacts/{id}")
                .buildAndExpand(account.getId()).toUri());
        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    /**
     * Update an existing account.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = "application/json")
    public @ResponseBody void update(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long accountId,
            @RequestBody Account updatedAccount) {
        Account account = accountRepo.findOne(accountId);

        BeanUtils.copyProperties(updatedAccount, account, "id");
        account.setTenantId(tenantId);
        accountRepo.save(account);
    }
}

