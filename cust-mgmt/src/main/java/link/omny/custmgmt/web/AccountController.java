package link.omny.custmgmt.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import link.omny.custmgmt.model.Account;
import link.omny.custmgmt.repositories.AccountRepository;
import lombok.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
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
     * @return Accounts for a specific tenant.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public @ResponseBody List<ShortAccount> listForTenant(
            @PathVariable("tenantId") String tenantId,
            @AuthenticationPrincipal UserDetails activeUser,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info(String.format("List accounts for tenant %1$s", tenantId));

        List<Account> list;
        if (limit == null) {
            list = accountRepo.findAllForTenant(tenantId);
        } else {
            Pageable pageable = new PageRequest(page == null ? 0 : page, limit);
            list = accountRepo.findPageForTenant(tenantId, pageable);
        }
        LOGGER.info(String.format("Found %1$s contacts", list.size()));

        return wrap(list);
    }

    /**
     * Create a new account.
     * 
     * @return if successful: status = 201 and Location header
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ResponseStatus(value = HttpStatus.CREATED)
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<?> create(
            @PathVariable("tenantId") String tenantId,
            @RequestBody Account account) {
        account.setTenantId(tenantId);
        accountRepo.save(account);

        UriComponentsBuilder builder2 = MvcUriComponentsBuilder
                .fromController(getClass());
        HashMap<String, String> vars = new HashMap<String, String>();
        vars.put("tenantId", tenantId);
        vars.put("id", account.getId().toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder2.path("/{id}")
                .buildAndExpand(vars).toUri());
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

    private List<ShortAccount> wrap(List<Account> list) {
        List<ShortAccount> resources = new ArrayList<ShortAccount>(list.size());
        for (Account account : list) {
            resources.add(wrap(account));
        }
        return resources;
    }

    private ShortAccount wrap(Account account) {
        ShortAccount resource = new ShortAccount();
        BeanUtils.copyProperties(account, resource);
        Link detail = linkTo(AccountRepository.class, account.getId())
                .withSelfRel();
        resource.add(detail);
        resource.setSelfRef(detail.getHref());
        return resource;
    }

    private Link linkTo(
            @SuppressWarnings("rawtypes") Class<? extends CrudRepository> clazz,
            Long id) {
        return new Link(clazz.getAnnotation(RepositoryRestResource.class)
                .path() + "/" + id);
    }

    @Data
    public static class ShortAccount extends ResourceSupport {
        private String selfRef;
        private String name;
        private Date firstContact;
        private Date lastUpdated;
    }
}

