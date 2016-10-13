package link.omny.custmgmt.web;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import link.omny.custmgmt.internal.CsvImporter;
import link.omny.custmgmt.internal.NullAwareBeanUtils;
import link.omny.custmgmt.model.Account;
import link.omny.custmgmt.model.Activity;
import link.omny.custmgmt.model.Contact;
import link.omny.custmgmt.model.Document;
import link.omny.custmgmt.model.Note;
import link.omny.custmgmt.repositories.AccountRepository;
import link.omny.custmgmt.repositories.ActivityRepository;
import link.omny.custmgmt.repositories.ContactRepository;
import link.omny.custmgmt.repositories.DocumentRepository;
import link.omny.custmgmt.repositories.NoteRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.User;
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
import org.springframework.ui.Model;
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
import com.knowprocess.bpmn.BusinessEntityNotFoundException;

/**
 * REST web service for uploading and accessing a file of JSON Contacts (over
 * and above the CRUD offered by spring data).
 * 
 * @author Tim Stephenson
 */
@Controller
@RequestMapping(value = "/{tenantId}/contacts")
public class ContactController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ContactController.class);

    @Autowired
    private ContactRepository contactRepo;

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private DocumentRepository docRepo;

    @Autowired
    private NoteRepository noteRepo;

    @Autowired
    private ActivityRepository activityRepo;

    @Autowired
    private ObjectMapper objectMapper;

    // @Value("omny.minsConsideredActive:-60")
    private int minsConsideredActive = -60;

    @Autowired
    private ProcessEngine processEngine;

    /**
     * Imports JSON representation of contacts.
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
    @RequestMapping(value = "/uploadjson", method = RequestMethod.POST)
    public @ResponseBody Iterable<Contact> handleFileUpload(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "file", required = true) MultipartFile file)
            throws IOException {
        LOGGER.info(String.format("Uploading contacts for: %1$s", tenantId));
        String content = new String(file.getBytes());

        List<Contact> list = objectMapper.readValue(content,
                new TypeReference<List<Contact>>() {
                });
        LOGGER.info(String.format("  found %1$d contacts", list.size()));
        for (Contact contact : list) {
            contact.setTenantId(tenantId);
        }

        Iterable<Contact> result = contactRepo.save(list);
        LOGGER.info("  saved.");
        return result;
    }

    /**
     * Imports JSON representation of contacts.
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
    @RequestMapping(value = "/uploadcsv", method = RequestMethod.POST)
    public @ResponseBody Iterable<Contact> handleCsvFileUpload(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "file", required = true) MultipartFile file)
            throws IOException {
        LOGGER.info(String.format("Uploading CSV contacts for: %1$s", tenantId));
        String content = new String(file.getBytes());
        List<Contact> list = new CsvImporter().readContacts(new StringReader(
                content), content.substring(0, content.indexOf('\n'))
                .split(","));
        LOGGER.info(String.format("  found %1$d contacts", list.size()));
        for (Contact contact : list) {
            contact.setTenantId(tenantId);
            contact.getAccount().setTenantId(tenantId);
        }

        Iterable<Contact> result = contactRepo.save(list);
        LOGGER.info("  saved.");
        return result;
    }

    /**
     * Return just the contacts for a specific tenant.
     * 
     * @return contacts for that tenant.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public @ResponseBody List<ShortContact> listForTenant(
            @PathVariable("tenantId") String tenantId,
            @AuthenticationPrincipal UserDetails activeUser,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info(String.format("List contacts for tenant %1$s", tenantId));

        List<Contact> list;
        if (limit == null) {
            // TODO unfortunately activeUser is null, prob some kind of class
            // cast error it seems
            // Use SecurityContextHolder as temporary fallback
            // Authentication authentication =
            // SecurityContextHolder.getContext()
            // .getAuthentication();

            // if (LOGGER.isDebugEnabled()) {
            // for (GrantedAuthority a : authentication.getAuthorities()) {
            // System.out.println("  " + a.getAuthority());
            // System.out.println("  "
            // + a.getAuthority().equals("ROLE_editor"));
            // System.out.println("  " + a.getAuthority().equals("editor"));
            // }
            // }

            // if (authentication.getAuthorities().contains("ROLE_editor")) {
            list = contactRepo.findAllForTenant(tenantId);
            // } else {
            // list = contactRepo.findAllForTenantOwnedByUser(tenantId,
            // authentication.getName());
            // }
        } else {
            Pageable pageable = new PageRequest(page == null ? 0 : page, limit);
            list = contactRepo.findPageForTenant(tenantId, pageable);
        }
        LOGGER.info(String.format("Found %1$s contacts", list.size()));

        return wrap(list);
    }

    /**
     * Return just the 'mailable' contacts for a specific tenant.
     * 
     * @return contacts for that tenant.
     */
    @RequestMapping(value = "/emailable", method = RequestMethod.GET)
    public @ResponseBody List<ShortContact> listMailableForTenant(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info(String.format("List emailable contacts for tenant %1$s",
                tenantId));

        List<Contact> list;
        if (limit == null) {
            list = contactRepo.findAllEmailableForTenant(tenantId);
        } else {
            Pageable pageable = new PageRequest(page == null ? 0 : page, limit);
            list = contactRepo.findEmailablePageForTenant(tenantId, pageable);
        }
        LOGGER.info(String.format("Found %1$s contacts", list.size()));

        return wrap(list);
    }

    /**
     * Return just the matching contacts (probably will be one in almost every
     * case).
     * 
     * @return contacts for that tenant.
     */
    @RequestMapping(value = "/searchByAccountNameLastNameFirstName", method = RequestMethod.GET, params = {
            "accountName", "lastName", "firstName" })
    public @ResponseBody List<ShortContact> listForAccountNameLastNameFirstName(
            @PathVariable("tenantId") String tenantId,
            @RequestParam("lastName") String lastName,
            @RequestParam("firstName") String firstName,
            @RequestParam("accountName") String accountName) {

        LOGGER.debug(String.format(
                "List contacts for account and name %1$s, %2$s %3$s",
                accountName, lastName, firstName));

        List<Contact> list = contactRepo.findByFirstNameLastNameAndAccountName(
                firstName, lastName, accountName);
        LOGGER.info(String.format("Found %1$s contacts", list.size()));

        return wrap(list);
    }

    /**
     * Return just the matching contacts (probably will be one in almost every
     * case).
     * 
     * @return contacts for that tenant.
     */
    @RequestMapping(value = "/{lastName}/{firstName}/{accountName}", method = RequestMethod.GET)
    public @ResponseBody List<ShortContact> getForAccountNameLastNameFirstName(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("accountName") String accountName,
            @PathVariable("lastName") String lastName,
            @PathVariable("firstName") String firstName) {

        return listForAccountNameLastNameFirstName(tenantId, lastName,
                firstName, accountName);
    }

    /**
     * Return just the matching contacts (probably will be one in almost every
     * case).
     * 
     * @return contacts for that tenant with the specified email address.
     */
    @RequestMapping(value = "/searchByEmail", method = RequestMethod.GET, params = { "email" })
    public @ResponseBody List<ShortContact> searchByEmail(
            @PathVariable("tenantId") String tenantId,
            @RequestParam("email") String email) {
        LOGGER.debug(String.format("List contacts for email %1$s", email));

        // The unwrap stems from a restriction in Activiti JSON handling on
        // multi-instance loops
        List<Contact> list = contactRepo.findByEmail(unwrap(email), tenantId);
        LOGGER.info(String.format("Found %1$s contacts", list.size()));

        return wrap(list);
    }

    /**
     * Return just the matching contacts.
     * 
     * @return contacts for that tenant with the matching tag.
     */
    @RequestMapping(value = "/findByTag", method = RequestMethod.GET, params = { "tag" })
    public @ResponseBody List<ShortContact> findByTag(
            @PathVariable("tenantId") String tenantId,
            @RequestParam("tag") String tag) {
        LOGGER.debug(String.format("List contacts for tag %1$s", tag));

        // The unwrap stems from a restriction in Activiti JSON handling on
        // multi-instance loops
        List<Contact> list = contactRepo.findByTag("%" + unwrap(tag) + "%",
                tenantId);
        LOGGER.info(String.format("Found %1$s contacts", list.size()));

        return wrap(list);
    }

    /**
     * Return just the matching contact.
     * 
     * @return the contact with this id.
     * @throws BusinessEntityNotFoundException
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @Transactional
    public @ResponseBody ShortContact findById(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id)
            throws BusinessEntityNotFoundException {
        LOGGER.debug(String.format("Find contact for id %1$s", id));

        return wrap(contactRepo.findOne(Long.parseLong(id)));
    }

    /**
     * Return all contacts associated with an account.
     * 
     * @return contacts matching that account.
     * @throws BusinessEntityNotFoundException
     */
    @RequestMapping(value = "/findByAccountId", method = RequestMethod.GET)
    @Transactional
    public @ResponseBody List<ShortContact> findByAccountId(
            @PathVariable("tenantId") String tenantId,
            @RequestParam("accountId") String accountId)
            throws BusinessEntityNotFoundException {
        LOGGER.debug(String.format("Find contact for account %1$s", accountId));

        return wrap(contactRepo.findByAccountId(Long.parseLong(accountId),
                tenantId));
    }

    /**
     * Return just the matching contact.
     * 
     * @return contacts for that tenant with the matching tag.
     * @throws BusinessEntityNotFoundException
     */
    @RequestMapping(value = "/findByUuid", method = RequestMethod.GET, params = { "uuid" })
    @Transactional
    public @ResponseBody ShortContact findByUuid(
            @PathVariable("tenantId") String tenantId,
            @RequestParam("uuid") String uuid)
            throws BusinessEntityNotFoundException {
        LOGGER.debug(String.format("Find contact for uuid %1$s", uuid));

        return wrap(consolidateContactsWithUuid(uuid, tenantId));
    }

    /**
     * Return contacts currently active on the site.
     * 
     * <p>
     * TODO 'Currently' can be defined per tenant.
     * </p>
     * 
     * @return contacts for that tenant with the matching tag.
     * @throws BusinessEntityNotFoundException
     */
    @RequestMapping(value = "/findActive", method = RequestMethod.GET)
    @Transactional
    public @ResponseBody List<ShortContact> findActive(
            @PathVariable("tenantId") String tenantId)
            throws BusinessEntityNotFoundException {
        LOGGER.debug(String.format("Find active contacts for tenant %1$s",
                tenantId));

        GregorianCalendar cal = new GregorianCalendar();
        cal.add(Calendar.MINUTE, minsConsideredActive);
        Date sinceDate = cal.getTime();
        return wrap(contactRepo.findActiveForTenant(sinceDate, tenantId));
    }

    protected synchronized Contact consolidateContactsWithUuid(String uuid,
            String tenantId) {
        // Because of ASYNC calls from JavaScript uuid may not be unique...
        List<Contact> contacts = contactRepo.findByUuid(uuid, tenantId);
        switch (contacts.size()) {
        case 0:
            String msg = String.format("No contact with uuid: %1$s", uuid);
            LOGGER.warn(msg);
            throw new BusinessEntityNotFoundException("Contact", uuid);
        case 1:
            Contact contact = contacts.get(0);
            LOGGER.info(String.format("Found contact: ", contact.getId()));
            return contact;
        default:
            // TODO should we attempt a cleanup?
            LOGGER.warn(String.format("Found %1$d contacts with uuid: %2$s...",
                    contacts.size(), uuid));
            contact = contacts.get(0);
            return contact;
        }
    }

    /**
     * Create a new contact.
     * 
     * @return
     * @throws URISyntaxException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ResponseStatus(value = HttpStatus.CREATED)
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<?> create(
            @PathVariable("tenantId") String tenantId,
            @RequestBody Contact contact) {
        contact.setTenantId(tenantId);
        contact = contactRepo.save(contact);


        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(getGlobalUri(contact));

        // TODO migrate to http://host/tenant/contacts/id
        // headers.setLocation(getTenantBasedUri(tenantId, contact));

        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    protected URI getGlobalUri(Contact contact) {
        try {
            UriComponentsBuilder builder = MvcUriComponentsBuilder
                    .fromController(getClass());
            String uri = builder.build().toUriString()
                    .replace("{tenantId}/", "");
            return new URI(uri
                    + "/" + contact.getId());
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected URI getTenantBasedUri(String tenantId, Contact contact) {
        UriComponentsBuilder builder = MvcUriComponentsBuilder
                .fromController(getClass());
        HashMap<String, String> vars = new HashMap<String, String>();
        vars.put("tenantId", tenantId);
        vars.put("id", contact.getId().toString());
        return builder.path("/{id}").buildAndExpand(vars).toUri();
    }

    /**
     * Update an existing contact.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { "application/json" })
    public @ResponseBody void update(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long contactId,
            @RequestBody Contact updatedContact) {
        Contact contact = contactRepo.findOne(contactId);

        LOGGER.debug(String.format("  contact: %1$s", contact));
        LOGGER.debug(String.format("  updated contact: %1$s", updatedContact));

        if (updatedContact.isFirstNameDefault()) {
            updatedContact.setFirstName(contact.getFirstName());
        }
        if (updatedContact.isLastNameDefault()) {
            updatedContact.setLastName(contact.getLastName());
        }
        NullAwareBeanUtils.copyNonNullProperties(updatedContact, contact, "id",
                "account");
        contact.setTenantId(tenantId);
        contactRepo.save(contact);

        // For some reason contact never has account deserialised even though it
        // is sent by browser
        if (updatedContact.getAccount() != null) {
            Account account = accountRepo.findOne(updatedContact.getAccount()
                    .getId());
            NullAwareBeanUtils.copyNonNullProperties(
                    updatedContact.getAccount(), account, "id");
            account.setTenantId(tenantId);
            accountRepo.save(account);
        }
    }


    /**
     * Link anonymous contact to a known one.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{email}/", method = RequestMethod.POST, produces = { "application/json" })
    @Transactional(value = TxType.REQUIRED)
    public @ResponseBody void linkToKnownContact(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("email") String email,
            @RequestParam("uuid") String uuid) {
        // Almost certainly only one contact but apparently we're not 100%
        List<Contact> contacts = contactRepo.findByEmail(email, tenantId);

        Contact anonContact = consolidateContactsWithUuid(uuid, tenantId);
        if (anonContact.getEmail() == null) {
            updateActivities(anonContact, contacts);

            // move uuid and activities to existing un-anonymous contact
            updateKnownContact(tenantId, uuid, contacts, anonContact);

        } else {
            LOGGER.info(String
                    .format("UUID %1$s already linked to known contact %2$s, record login.",
                            uuid, anonContact.getFullName()));
            Activity entity = new Activity("login", new Date(), String.format(
                    "User %1$s (%2$d) logged in", anonContact.getFullName(),
                    anonContact.getId()));
            entity.setContact(anonContact);
            activityRepo.save(entity);

        }
    }

    // @Transactional(value = TxType.REQUIRES_NEW)
    public void updateActivities(Contact anonContact, List<Contact> contacts) {
        for (Contact contact : contacts) {
            activityRepo.updateContact(anonContact, contact);
        }
    }

    // @Transactional(value = TxType.REQUIRES_NEW)
    public void updateKnownContact(String tenantId, String uuid,
            List<Contact> contacts, Contact anonContact) {
        for (Contact contact : contacts) {
            contact.setUuid(uuid);
            contact.setTenantId(tenantId);
            contactRepo.save(contact);

            Activity entity = new Activity("linkToKnownContact", new Date(),
                    String.format("Linked %1$s (%2$d) to user %3$s (%4$d)",
                            anonContact.getUuid(), anonContact.getId(),
                            contact.getFullName(), contact.getId()));
            entity.setContact(contact);
            activityRepo.save(entity);
        }
        if (contacts.size() > 0) {
            contactRepo.delete(anonContact.getId());
        }
    }

    /**
     * Link anonymous contact's activities to the specified known contact.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{email}/activities", method = RequestMethod.POST, produces = { "application/json" })
    @Transactional(value = TxType.REQUIRES_NEW)
    public @ResponseBody void linkActivitiesToKnownContact(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("email") String email,
            @RequestParam("uuid") String uuid) {
        LOGGER.info(String.format(
                "linkActivitiesToKnownContact %1$s %2$s %3$s", tenantId, email,
                uuid));

        // Almost certainly only one contact but apparently we're not 100%
        List<Contact> contacts = contactRepo.findByEmail(email, tenantId);

        List<Contact> anonContacts = contactRepo.findAnonByUuid(uuid, tenantId);
        // Contact anonContact = consolidateContactsWithUuid(uuid, tenantId);

        for (Contact contact : contacts) {
            for (Contact anonContact : anonContacts) {
                activityRepo.updateContact(anonContact, contact);
            }
        }
    }

    /**
     * Delete an existing contact.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, consumes = { "application/json" })
    public @ResponseBody void delete(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long contactId) {
        Contact contact = contactRepo.findOne(contactId);

        contactRepo.delete(contact);
    }

    /**
     * Add a document to the specified contact.
     */
    // Jackson cannot deserialise document because of contact reference
    // @RequestMapping(value = "/{contactId}/documents", method =
    // RequestMethod.PUT)
    public @ResponseBody void addDocument(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId, @RequestBody Document doc) {
        Contact contact = contactRepo.findOne(contactId);
        doc.setContact(contact);
        docRepo.save(doc);
        // necessary to force a save
        contact.setLastUpdated(new Date());
        contactRepo.save(contact);
        // Similarly cannot return object until solve Jackson object cycle
        // return doc;
    }

    /**
     * Add a document to the specified contact.
     */
    @RequestMapping(value = "/{contactId}/documents", method = RequestMethod.POST)
    public @ResponseBody void addDocument(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId,
            @RequestParam("author") String author,
            @RequestParam("name") String name,
            @RequestParam("url") String url) {

        addDocument(tenantId, contactId, new Document(author, name, url));
    }

    /**
     * Add a note to the specified contact.
     */
    // TODO Jackson cannot deserialise document because of contact reference
    // @RequestMapping(value = "/{contactId}/notes", method = RequestMethod.PUT)
    public @ResponseBody void addNote(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId, @RequestBody Note note) {
        Contact contact = contactRepo.findOne(contactId);
        note.setContact(contact);
        noteRepo.save(note);
        // necessary to force a save
        contact.setLastUpdated(new Date());
        contactRepo.save(contact);
        // Similarly cannot return object until solve Jackson object cycle
        // return note;
    }

    /**
     * Add a note to the specified contact.
     */
    @RequestMapping(value = "/{contactId}/notes", method = RequestMethod.POST)
    public @ResponseBody void addNote(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId,
            @RequestParam("author") String author,
            @RequestParam("favorite") boolean favorite,
            @RequestParam("content") String content) {
        addNote(tenantId, contactId, new Note(author, content, favorite));
    }

    /**
     * @deprecated This method does not follow conventions, leading to a clash
     *             with {@link #update}.
     * @see {@link #setStage}
     */
    @RequestMapping(value = "/{contactId}", method = RequestMethod.PUT, consumes = "application/x-www-form-urlencoded")
    public @ResponseBody void setStageDeprecated(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId,
            @RequestParam("stage") String stage) {
        LOGGER.info(String.format("Setting contact %1$s to stage %2$s",
                contactId, stage));

        Contact contact = contactRepo.findOne(contactId);
        contact.setStage(stage);
        contactRepo.save(contact);

        addActivity(tenantId, contactId, "transition-to-stage",
                String.format("Waiting for %1$s", stage));

        // return contact;
    }

    /**
     * Change the sale stage the contact is at.
     */
    @RequestMapping(value = "/{contactId}/stage", method = RequestMethod.PUT, consumes = "application/x-www-form-urlencoded")
    public @ResponseBody void setStage(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId,
            @RequestParam("stage") String stage) {
        LOGGER.info(String.format("Setting contact %1$s to stage %2$s",
                contactId, stage));

        Contact contact = contactRepo.findOne(contactId);
        contact.setStage(stage);
        contactRepo.save(contact);

        addActivity(tenantId, contactId, "transition-to-stage",
                String.format("Waiting for %1$s", stage));

        // return contact;
    }

    /**
     * Change the sale stage the contact is at.
     */
    @RequestMapping(value = "/{contactId}/account", method = RequestMethod.PUT, consumes = "text/uri-list")
    @Transactional
    public @ResponseBody void setAccount(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId,
            @RequestBody String accountUri) {
        LOGGER.info(String.format("Linking account %2$s to contact %1$s",
                contactId, accountUri));

        Long acctId = Long.parseLong(accountUri.substring(accountUri
                .lastIndexOf('/') + 1));

        contactRepo.setAccount(contactId, acctId);

        addActivity(tenantId, contactId, "link-account",
                String.format("Linked for %1$s", accountUri));
    }

    /**
     * Add an activity to the specified contact.
     */
    @RequestMapping(value = "/{contactId}/activities", method = RequestMethod.POST)
    public @ResponseBody void addActivity(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId,
            @RequestParam("type") String type,
            @RequestParam("content") String content) {

        Activity activity = new Activity(type, new Date(), content);
        activity.setContact(contactRepo.findOne(contactId));
        activityRepo.save(activity);
    }

    /**
     * Confirm a user's email by returning a code sent to that address.
     * 
     * <p>
     * Note that this may be used interactively as well as API so return status
     * code 200 / 403 together with HTML for human consumption.
     * 
     * @param tenantId
     *            The tenant this contact is associated with.
     * @param contactId
     *            Id of contact whose address is being confirmed.
     * @param email
     *            Address being confirmed
     * @param code
     *            Code to compare to the one previously issued.
     */
    @RequestMapping(value = "/{contactId}/{email}", method = RequestMethod.POST)
    public String confirmEmail(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId,
            @PathVariable("email") String email,
            @RequestParam("emailConfirmationCode") String emailConfirmationCode,
            Model model) {
        Contact contact = contactRepo.findOne(contactId);
        contact.confirmEmail(emailConfirmationCode);
        contactRepo.save(contact);
        return "emailConfirmation";
    }

    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{uuid}/reset-password", method = RequestMethod.POST, headers = "Accept=application/json")
    public @ResponseBody void resetPassword(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("uuid") String uuid,
            @RequestParam(name = "password") String pwd,
            @RequestParam(name = "password2") String pwd2) {
        LOGGER.info(String.format("Updating password of %1$s", uuid));

        if (!pwd.equals(pwd2)) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        IdentityService idSvc = processEngine.getIdentityService();
        List<Contact> contacts = contactRepo.findByUuid(uuid, tenantId);
        if (contacts.size() > 0) {
            try {
                User user = idSvc.createUserQuery()
                        .userEmail(contacts.get(0).getEmail())
                        .memberOfGroup("user").singleResult();
                user.setPassword(pwd);
                idSvc.saveUser(user);
            } catch (NullPointerException e) {
                LOGGER.error(String.format("No user found with email %1$s",
                        contacts.get(0).getEmail()), e);
                throw new BusinessEntityNotFoundException("user", contacts.get(
                        0).getEmail());
            } catch (Exception e) {
                LOGGER.error(String.format(
                        "Email %1$s does not resolve to a unique user",
                        contacts.get(0).getEmail()), e);
                throw new BusinessEntityNotFoundException("user", contacts.get(
                        0).getEmail());
            }
        } else {
            throw new BusinessEntityNotFoundException("contact", uuid);
        }
    }

    /**
     * If value is in fact "value", remove the quotes.
     *
     * @param value
     *            Any string.
     * @return The value param without the leading and trailing quotes.
     */
    private String unwrap(String value) {
        if (value == null)
            return null;
        else if (value.toString().startsWith("\""))
            return value.toString().substring(1, value.toString().length() - 1);
        return value.toString();
    }

    private List<ShortContact> wrap(List<Contact> list) {
        List<ShortContact> resources = new ArrayList<ShortContact>(list.size());
        for (Contact contact : list) {
            resources.add(wrap(contact));
        }
        return resources;
    }

    private ShortContact wrap(Contact contact) {
        ShortContact resource = new ShortContact();
        BeanUtils.copyProperties(contact, resource);
        resource.setAlerts(contact.getAlertsAsList());
        Link detail = new Link(getGlobalUri(contact).toString());
        resource.add(detail);
        resource.setSelfRef(detail.getHref());
        if (contact.getAccount() != null) {
            resource.setAccountName(contact.getAccount().getName());
            resource.add(linkTo(AccountRepository.class,
                    contact.getAccount().getId()).withRel("account"));
        }
        return resource;
    }

    // TODO this produces relative URIs (no host) i.e. a chocolate teapot
    private Link linkTo(
            @SuppressWarnings("rawtypes") Class<? extends CrudRepository> clazz,
            Long id) {
        return new Link(clazz.getAnnotation(RepositoryRestResource.class)
                .path() + "/" + id);
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ShortContact extends ResourceSupport {
        private String selfRef;
        private String firstName;
        private String lastName;
        private String fullName;
        private String town;
        private String countyOrCity;
        private String country;
        private String email;
        private String jobTitle;
        private String phone1;
        private String phone2;
        private String accountName;
        private String owner;
        private String stage;
        private String enquiryType;
        private String accountType;
        private List<String> alerts;
        private String tags;
        private String uuid;
        private String tenantId;
        private Date firstContact;
        private Date lastUpdated;
    }
}
