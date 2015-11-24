package link.omny.custmgmt.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
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
     * Return just the contacts for a specific tenant.
     * 
     * @return contacts for that tenant.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public @ResponseBody List<ShortContact> listForTenant(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info(String.format("List contacts for tenant %1$s", tenantId));

        List<Contact> list;
        if (limit == null) {
            list = contactRepo.findAllForTenant(tenantId);
        } else {
            Pageable pageable = new PageRequest(page == null ? 0 : page, limit);
            list = contactRepo.findPageForTenant(tenantId, pageable);
        }
        LOGGER.info(String.format("Found %1$s contacts", list.size()));

        return wrap(list);
    }

    /**
     * Return just the contacts for a specific tenant.
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
    @RequestMapping(value = "/searchByAccountNameLastNameFirstName",
            method = RequestMethod.GET, 
            params = { "accountName", "lastName", "firstName" })
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
     * @return contacts for that tenant.
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
     * Create a new contact.
     * 
     * @return
     */
    @ResponseStatus(value = HttpStatus.CREATED)
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<?> create(
            @PathVariable("tenantId") String tenantId,
            @RequestBody Contact contact, UriComponentsBuilder builder) {
        contact.setTenantId(tenantId);
        contactRepo.save(contact);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/contacts/{id}")
                .buildAndExpand(contact.getId()).toUri());
        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    /**
     * Update an existing contact.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = "application/json")
    public @ResponseBody void update(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long contactId,
            @RequestBody Contact updatedContact) {
        Contact contact = contactRepo.findOne(contactId);

        BeanUtils.copyProperties(updatedContact, contact, "id");
        contact.setTenantId(tenantId);
        contactRepo.save(contact);
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
            @RequestParam("url") String url) {

        addDocument(tenantId, contactId, new Document(author, url));
    }

    /**
     * Add a note to the specified contact.
     */
    // TODO Jackson cannot deserialise document because of contact reference
//    @RequestMapping(value = "/{contactId}/notes", method = RequestMethod.PUT)
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
            @RequestParam("content") String content) {
        addNote(tenantId, contactId, new Note(author, content));
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
        Link detail = linkTo(ContactRepository.class, contact.getId())
                .withSelfRel();
        resource.add(detail);
        resource.setSelfRef(detail.getHref());
        if (contact.getAccount() != null) {
            resource.setAccountName(contact.getAccount().getName());
            resource.add(linkTo(AccountRepository.class,
                    contact.getAccount().getId()).withRel("account"));
        }
        return resource;
    }
    
    
    private Link linkTo(
            @SuppressWarnings("rawtypes") Class<? extends CrudRepository> clazz,
            Long id) {
        return new Link(clazz.getAnnotation(RepositoryRestResource.class)
                .path() + "/" + id);
    }

    @Data
    public static class ShortContact extends ResourceSupport {
        private String selfRef;
        private String firstName;
        private String lastName;
        private String town;
        private String countyOrCity;
        private String country;
        private String email;
        private String accountName;
        private String owner;
        private String stage;
        private String enquiryType;
        private String accountType;
        private String tags;
        private Date firstContact;
        private Date lastUpdated;
      }
}
