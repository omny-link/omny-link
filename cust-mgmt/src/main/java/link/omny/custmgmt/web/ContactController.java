package link.omny.custmgmt.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import link.omny.custmgmt.model.Contact;
import link.omny.custmgmt.model.Note;
import link.omny.custmgmt.repositories.AccountRepository;
import link.omny.custmgmt.repositories.ContactRepository;
import link.omny.custmgmt.repositories.NoteRepository;
import lombok.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * REST web service for uploading and accessing a file of JSON Contacts (over
 * and above the CRUD offered by spring data).
 * 
 * /models/upload?file={file} Add a model POST file: A file posted in a
 * multi-part request
 * 
 * @author Tim Stephenson
 */
@Controller
@RequestMapping(value = "/{tenantId}/contacts")
public class ContactController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ContactController.class);

    @Autowired
    private ContactRepository repo;

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private NoteRepository noteRepo;

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

        Iterable<Contact> result = repo.save(list);
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
            @PathVariable("tenantId") String tenantId) {

        LOGGER.info(String.format("List contacts for tenant %1$s", tenantId));

        List<Contact> list = repo.findAllForTenant(tenantId);
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

        List<Contact> list = repo.findByFirstNameLastNameAndAccountName(
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
     * Add a note to the specified contact.
     * 
     * @return contacts for that tenant.
     */
    @RequestMapping(value = "/{contactId}/notes", method = RequestMethod.POST)
    public @ResponseBody Note addNote(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId,
            @RequestParam("author") String author,
            @RequestParam("content") String content) {

        Note note = new Note(author, content);
        noteRepo.save(note);
        return note;
    }

    /**
     * Change the sale stage the contact is at.
     * 
     * @return contacts for that tenant.
     */
    @RequestMapping(value = "/{contactId}", method = RequestMethod.PUT)
    public @ResponseBody Contact setStage(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId,
            @RequestParam("stage") String stage) {
        LOGGER.info(String.format("Setting contact %1$s to stage %2$s",
                contactId, stage));

        Contact contact = repo.findOne(contactId);
        contact.setStage(stage);
        repo.save(contact);

        return contact;
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
        resource.setFirstName(contact.getFirstName());
        resource.setLastName(contact.getLastName());
        resource.setEmail(contact.getEmail());
        resource.setOwner(contact.getOwner());
        resource.setStage(contact.getStage());
        resource.setEnquiryType(contact.getEnquiryType());
        resource.setAccountType(contact.getAccountType());
        resource.setFirstContact(contact.getFirstContact());
        resource.setLastUpdated(contact.getLastUpdated());
        Link detail = linkTo(ContactRepository.class, contact.getId())
                .withSelfRel();
        resource.add(detail);
        if (contact.getAccount() != null) {
            resource.setAccountName(contact.getAccount().getName());
            resource.add(linkTo(AccountRepository.class,
                    contact.getAccount().getId()).withRel("account"));
        }
        return resource;
    }
    
    
    private Link linkTo(Class<? extends CrudRepository> clazz, Long id) {
        return new Link(clazz.getAnnotation(RepositoryRestResource.class)
                .path() + "/" + id);
    }

    @Data
    public static class ShortContact extends ResourceSupport {
        private String firstName;
        private String lastName;
        private String email;
        private String accountName;
        private String owner;
        private String stage;
        private String enquiryType;
        private String accountType;
        private Date firstContact;
        private Date lastUpdated;
      }
}
