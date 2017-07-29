package link.omny.custmgmt.web;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

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
import org.springframework.security.access.annotation.Secured;
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

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowprocess.bpm.impl.DateUtils;
import com.knowprocess.bpmn.BusinessEntityNotFoundException;

import link.omny.custmgmt.model.Account;
import link.omny.custmgmt.model.Activity;
import link.omny.custmgmt.model.Contact;
import link.omny.custmgmt.model.CustomAccountField;
import link.omny.custmgmt.model.Document;
import link.omny.custmgmt.model.Note;
import link.omny.custmgmt.model.views.AccountViews;
import link.omny.custmgmt.repositories.AccountRepository;
import link.omny.custmgmt.repositories.ContactRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
    private ContactRepository contactRepo;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Add activity to the specified account.
     * @return the created activity.
     */
    @RequestMapping(value = "/{accountId}/activities", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody ResponseEntity<Activity> addActivity(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("accountId") Long accountId, @RequestBody Activity activity) {
        Account account = accountRepo.findOne(accountId);
        account.getActivities().add(activity);
        account.setLastUpdated(new Date());
        accountRepo.save(account);
        activity = account.getActivities().get(account.getActivities().size()-1);

        HttpHeaders headers = new HttpHeaders();
        URI uri = MvcUriComponentsBuilder.fromController(getClass())
                .path("/{id}/activities/{activityId}")
                .buildAndExpand(tenantId, account.getId(), activity.getId())
                .toUri();
        headers.setLocation(uri);

        return new ResponseEntity<Activity>(activity, headers, HttpStatus.CREATED);
    }

    /**
     * Add an activity to the specified account.
     * @return the created activity.
     */
    @RequestMapping(value = "/{accountId}/activities", method = RequestMethod.POST, consumes = "application/x-www-form-urlencoded")
    public @ResponseBody ResponseEntity<Activity> addActivity(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("accountId") Long accountId,
            @RequestParam("type") String type,
            @RequestParam("content") String content) {
        return addActivity(tenantId, accountId, new Activity(type, new Date(), content));
    }

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

    @RequestMapping(value = "/archive", method = RequestMethod.POST, headers = "Accept=application/json")
    @Secured("ROLE_ADMIN")
    @Transactional
    public @ResponseBody Integer archiveAccounts(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "before", required = false) String before) {
        Date beforeDate = before == null
                ? DateUtils.oneMonthAgo() : DateUtils.parseDate(before);
        LOGGER.info(String.format(
                "Place on hold accounts of %1$s older than %2$s", tenantId,
                beforeDate.toString()));

        return accountRepo.updateStage("On hold", beforeDate, tenantId);
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
        LOGGER.info(String.format("Found %1$s accounts", list.size()));

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
        for (CustomAccountField field : account.getCustomFields()) {
            field.setAccount(account);
        }
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
     * Return just the matching account.
     *
     * @return the account with this id.
     * @throws BusinessEntityNotFoundException
     */
    @JsonView(AccountViews.Detailed.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public @ResponseBody Account findById(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id)
            throws BusinessEntityNotFoundException {
        LOGGER.debug(String.format("Find account for id %1$s", id));

        Account account;
        try {
            account = accountRepo.findOne(Long.parseLong(id));
        } catch (NumberFormatException e) {
            account = accountRepo.findByCodeForTenant(id, tenantId);
        }
        if (account == null) {
            throw new BusinessEntityNotFoundException("account", id);
        }
        account.getActivities(); // force load

        return addLinks(tenantId, account);
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

        BeanUtils.copyProperties(updatedAccount, account, "id", "notes", "documents");
        account.setTenantId(tenantId);
        accountRepo.save(account);
    }

    /**
     * Update an existing account's custom fields.
     * @throws IOException
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}/customFields/", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody void updateCustomFields(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long accountId,
            @RequestBody Object customFields) throws IOException {
        Account account = accountRepo.findOne(accountId);

        if (customFields instanceof String) {
            JsonNode jsonNode = objectMapper.readTree((String) customFields);
            for (Iterator<String> it = jsonNode.fieldNames() ; it.hasNext() ;) {
                String key = it.next();
                account.addCustomField(
                        new CustomAccountField(key, jsonNode.get(key).asText()));
            }
        } else if (customFields instanceof HashMap) {
            for (Map.Entry<?,?> entry : ((HashMap<?,?>) customFields).entrySet()) {
                LOGGER.warn("About to try to store {}={}", entry.getKey(), entry.getValue());
                account.addCustomField(
                        new CustomAccountField((String)entry.getKey(), entry.getValue().toString()));
            }
        } else {
            String msg = String.format("Attempt to set custom fields from %1$s is not supported", customFields.getClass().getName());
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }

        accountRepo.save(account);
    }

    /**
     * Add a document to the specified account.
     */
    @RequestMapping(value = "/{accountId}/documents", method = RequestMethod.PUT)
    public @ResponseBody ResponseEntity<Document> addDocument(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("accountId") Long accountId, @RequestBody Document doc) {
         Account account = accountRepo.findOne(accountId);
         account.getDocuments().add(doc);
         account.setLastUpdated(new Date());
         accountRepo.save(account);
         doc = account.getDocuments().get(account.getDocuments().size()-1);

         HttpHeaders headers = new HttpHeaders();
         URI uri = MvcUriComponentsBuilder.fromController(getClass())
                 .path("/{id}/notes/{noteId}")
                 .buildAndExpand(tenantId, account.getId(), doc.getId())
                 .toUri();
         headers.setLocation(uri);

         return new ResponseEntity<Document>(doc, headers, HttpStatus.CREATED);
    }

    /**
     * Add a document to the specified account.
     *      *
     * <p>This is just a convenience method, see {@link #addDocument(String, Long, Document)}
     * @return
     *
     * @return The document created.
     */
    @RequestMapping(value = "/{accountId}/documents", method = RequestMethod.POST)
    public @ResponseBody Document addDocument(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("accountId") Long accountId,
            @RequestParam("author") String author,
            @RequestParam("name") String name,
            @RequestParam("url") String url) {

        return addDocument(tenantId, accountId, new Document(author, name, url)).getBody();
    }

    /**
     * Add a note to the specified account.
     * @return the created note.
     */
    @RequestMapping(value = "/{accountId}/notes", method = RequestMethod.PUT)
    public @ResponseBody ResponseEntity<Note> addNote(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("accountId") Long accountId, @RequestBody Note note) {
        Account account = accountRepo.findOne(accountId);
        account.getNotes().add(note);
        account.setLastUpdated(new Date());
        accountRepo.save(account);
        note = account.getNotes().get(account.getNotes().size()-1);

        HttpHeaders headers = new HttpHeaders();
        URI uri = MvcUriComponentsBuilder.fromController(getClass())
                .path("/{id}/notes/{noteId}")
                .buildAndExpand(tenantId, account.getId(), note.getId())
                .toUri();
        headers.setLocation(uri);

        return new ResponseEntity<Note>(note, headers, HttpStatus.CREATED);
    }

    /**
     * Add a note to the specified account from its parts.
     *
     * <p>This is just a convenience method, see {@link #addNote(String, Long, Note)}
     *
     * @return The note created.
     */
    @RequestMapping(value = "/{accountId}/notes", method = RequestMethod.POST)
    public @ResponseBody Note addNote(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("accountId") Long accountId,
            @RequestParam("author") String author,
            @RequestParam("favorite") boolean favorite,
            @RequestParam("content") String content) {
        return addNote(tenantId, accountId, new Note(author, content, favorite)).getBody();
    }

    /**
     * Delete an existing account.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public @ResponseBody void delete(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long accountId) {

        List<Contact> contacts = contactRepo.findByAccountId(accountId, tenantId);
        for (Contact contact : contacts) {
            contactRepo.delete(contact.getId());
        }

        accountRepo.delete(accountId);
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

        String orgCode = (String) account.getCustomFieldValue("orgCode");
        if (orgCode != null) {
            resource.setOrgCode(orgCode);
        }

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
    @EqualsAndHashCode(callSuper = true)
    public static class ShortAccount extends ResourceSupport {
        private String selfRef;
        private String name;
        private String orgCode;
        private String companyNumber;
        private String businessWebsite;
        private String email;
        private String owner;
        private String phone1;
        private String parentOrg;
        private String stage;
        private String enquiryType;
        private String accountType;
        private String alerts;
        private String tags;
        private Date firstContact;
        private Date lastUpdated;
    }

    private Account addLinks(String tenantId, Account account) {
        List<Link> links = new ArrayList<Link>();
        links.add(new Link(String.format("/%1$s/accounts/%2$s",
                tenantId, account.getId())));
        account.setLinks(links);
        return account;
    }
}

