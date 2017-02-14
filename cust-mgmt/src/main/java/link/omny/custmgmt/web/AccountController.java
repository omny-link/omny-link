package link.omny.custmgmt.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.knowprocess.bpmn.BusinessEntityNotFoundException;

import link.omny.custmgmt.json.JsonCustomAccountFieldDeserializer;
import link.omny.custmgmt.json.JsonCustomFieldSerializer;
import link.omny.custmgmt.model.Account;
import link.omny.custmgmt.model.Contact;
import link.omny.custmgmt.model.CustomAccountField;
import link.omny.custmgmt.model.Document;
import link.omny.custmgmt.model.Note;
import link.omny.custmgmt.repositories.AccountRepository;
import link.omny.custmgmt.repositories.ContactRepository;
import link.omny.custmgmt.repositories.DocumentRepository;
import link.omny.custmgmt.repositories.NoteRepository;
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
    private DocumentRepository docRepo;

    @Autowired
    private NoteRepository noteRepo;

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
     * Return just the matching contact.
     * 
     * @return the contact with this id.
     * @throws BusinessEntityNotFoundException
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @Transactional
    public @ResponseBody ShortAccount findById(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id)
            throws BusinessEntityNotFoundException {
        LOGGER.debug(String.format("Find account for id %1$s", id));

        return wrap(accountRepo.findOne(Long.parseLong(id)));
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

    /**
     * Add a document to the specified contact.
     */
    // Jackson cannot deserialise document because of contact reference
    // @RequestMapping(value = "/{contactId}/documents", method =
    // RequestMethod.PUT)
    public @ResponseBody void addDocument(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("accountId") Long accountId, @RequestBody Document doc) {
        Account account = accountRepo.findOne(accountId);
        doc.setAccount(account);
        docRepo.save(doc);
        // necessary to force a save
        account.setLastUpdated(new Date());
        accountRepo.save(account);
        // Similarly cannot return object until solve Jackson object cycle
        // return doc;
    }

    /**
     * Add a document to the specified contact.
     */
    @RequestMapping(value = "/{accountId}/documents", method = RequestMethod.POST)
    public @ResponseBody void addDocument(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("accountId") Long accountId,
            @RequestParam("author") String author,
            @RequestParam("name") String name, @RequestParam("url") String url) {

        addDocument(tenantId, accountId, new Document(author, name, url));
    }

    /**
     * Add a note to the specified contact.
     */
    // TODO Jackson cannot deserialise document because of contact reference
    // @RequestMapping(value = "/{contactId}/notes", method = RequestMethod.PUT)
    public @ResponseBody void addNote(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("accountId") Long accountId, @RequestBody Note note) {
        Account account = accountRepo.findOne(accountId);
        note.setAccount(account);
        noteRepo.save(note);
        // necessary to force a save
        account.setLastUpdated(new Date());
        accountRepo.save(account);
        // Similarly cannot return object until solve Jackson object cycle
        // return note;
    }

    /**
     * Add a note to the specified contact.
     */
    @RequestMapping(value = "/{accountId}/notes", method = RequestMethod.POST)
    public @ResponseBody void addNote(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("accountId") Long accountId,
            @RequestParam("author") String author,
            @RequestParam("favorite") boolean favorite,
            @RequestParam("content") String content) {
        addNote(tenantId, accountId, new Note(author, content, favorite));
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
        private String companyNumber;
        private String sic;
        private String aliases;
        private String businessWebsite;
        private String email;
        private boolean emailConfirmed;
        private String emailHash;
        private String phone1;
        private String phone2;
        private String address1;
        private String address2;
        private String town;
        private String countyOrCity;
        private String postCode;
        private String country;
        private String address;
        private String twitter;
        private String facebook;
        private String linkedIn;
        private String shortDesc;
        private String description;
        private Integer incorporationYear;
        private String noOfEmployees;
        private String stage;
        private String stageReason;
        private Date stageDate;
        private String enquiryType;
        private String accountType;
        private String owner;
        private String alerts;
        private String tags;
        @JsonDeserialize(using = JsonCustomAccountFieldDeserializer.class)
        @JsonSerialize(using = JsonCustomFieldSerializer.class)
        private List<CustomAccountField> customFields;
        private Date firstContact;
        private Date lastUpdated;
    }
}

