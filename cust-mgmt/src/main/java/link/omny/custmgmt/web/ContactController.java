/*******************************************************************************
 *Copyright 2015-2018 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package link.omny.custmgmt.web;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import link.omny.custmgmt.internal.CsvImporter;
import link.omny.custmgmt.internal.DateUtils;
import link.omny.custmgmt.model.Account;
import link.omny.custmgmt.model.Contact;
import link.omny.custmgmt.model.CustomContactField;
import link.omny.custmgmt.model.views.ContactViews;
import link.omny.custmgmt.repositories.AccountRepository;
import link.omny.custmgmt.repositories.ContactRepository;
import link.omny.supportservices.exceptions.BusinessEntityNotFoundException;
import link.omny.supportservices.internal.NullAwareBeanUtils;
import link.omny.supportservices.model.Activity;
import link.omny.supportservices.model.ActivityType;
import link.omny.supportservices.model.Document;
import link.omny.supportservices.model.Note;

/**
 * REST web service for uploading and accessing a file of JSON Contacts (over
 * and above the CRUD offered by spring data).
 *
 * @author Tim Stephenson
 */
@Controller
@RequestMapping(value = "/{tenantId}/contacts")
public class ContactController {

    static final Logger LOGGER = LoggerFactory
            .getLogger(ContactController.class);

    @Autowired
    private ContactRepository contactRepo;

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private AccountController accountSvc;

    @Autowired
    private ObjectMapper objectMapper;

    // @Value("omny.minsConsideredActive:-60")
    private int minsConsideredActive = -60;

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
    public @ResponseBody List<EntityModel<Contact>> handleFileUpload(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "file", required = true) MultipartFile file)
            throws IOException {
        LOGGER.info("Uploading contacts for: {}", tenantId);
        String content = new String(file.getBytes());

        List<Contact> list = objectMapper.readValue(content,
                new TypeReference<List<Contact>>() { });
        LOGGER.info("  found {} contacts", list.size());
        for (Contact contact : list) {
            contact.setTenantId(tenantId);
        }

        Iterable<Contact> result = contactRepo.saveAll(list);
        LOGGER.info("  saved.");
        return addLinks(tenantId, result);
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
        LOGGER.info("Uploading CSV contacts for: {}", tenantId);
        String content = new String(file.getBytes());
        List<Contact> list = new CsvImporter().readContacts(new StringReader(
                content), content.substring(0, content.indexOf('\n'))
                .split(","));
        LOGGER.info("  found {} contacts", list.size());
        for (Contact contact : list) {
            contact.setTenantId(tenantId);
            contact.getAccount().setTenantId(tenantId);
        }

        Iterable<Contact> result = contactRepo.saveAll(list);
        LOGGER.info("  saved.");
        return result;
    }

    /**
     * Return just the contacts for a specific tenant.
     *
     * @return contacts for that tenant.
     */
    @GetMapping(value = "/contacts.csv", produces = "text/csv")
    public @ResponseBody ResponseEntity<String> listForTenantAsCsvAlt(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return listForTenantAsCsv(tenantId, page, limit);
    }

    @RequestMapping(value = "/archive", method = RequestMethod.POST, headers = "Accept=application/json")
    @Transactional
    public @ResponseBody Integer archiveContacts(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "before", required = false) String before,
            @RequestParam(value = "stage", required = false) String stage) {
        Date beforeDate = before == null
                ? DateUtils.oneMonthAgo() : DateUtils.parseDate(before);
        if (stage == null || stage.length() == 0) {
            stage = "On hold";
        }
        LOGGER.info("Set contacts of {} older than {} to '{}'",
                tenantId, beforeDate.toString(), stage);

        return contactRepo.updateStage(stage, beforeDate, tenantId);
    }

    /**
     * Return just the contacts for a specific tenant.
     *
     * @return contacts for that tenant.
     */
    @GetMapping(value = "/", produces = "text/csv")
    public @ResponseBody ResponseEntity<String> listForTenantAsCsv(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        StringBuilder sb = new StringBuilder().append("id,accountId,"
                + "firstName,lastName,title,"
                + "isMainContact,address1,address2,town,countyOrCity,country,"
                + "postCode,email,jobTitle,phone1,phone2,owner,stage,"
                + "stageReason,stageDate,enquiryType,accountType,"
                + "isExistingCustomer,source,medium,campaign,keyword,"
                + "isDoNotCall,isDoNotEmail,tags,uuid,twitter,linkedIn,"
                + "facebook,tenantId,firstContact,lastUpdated,timeSinceLogin,"
                + "timeSinceFirstLogin,timeSinceRegistered,timeSinceEmail,notes,documents,");
        List<String> customFieldNames = contactRepo.findCustomFieldNames(tenantId);
        LOGGER.info("Found {} custom field names while exporting contacts for {}: {}",
                customFieldNames.size(), tenantId, customFieldNames);
        for (String fieldName : customFieldNames) {
            sb.append(fieldName).append(",");
        }
        sb.append("\r\n");

        for (Contact contact : listForTenant(tenantId, page, limit)) {
            contact.setCustomHeadings(customFieldNames);
            sb.append(contact.toCsv()).append("\r\n");
        }
        LOGGER.info("Exporting CSV contacts for {} generated {} bytes",
                tenantId, sb.length());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentLength(sb.length());
        return new ResponseEntity<String>(
                sb.toString(), httpHeaders, HttpStatus.OK);
    }

    /**
     * Return just the contacts for a specific tenant.
     *
     * @return contacts for that tenant.
     */
    @GetMapping(value = "/", produces = "application/json")
    @JsonView(ContactViews.Summary.class)
    public @ResponseBody List<EntityModel<Contact>> listForTenantAsJson(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "returnFull", required = false) boolean returnFull) {
        // TODO optimise with light value object?
        return addLinks(tenantId, listForTenant(tenantId, page, limit));
    }

    protected List<Contact> listForTenant(String tenantId,
            Integer page, Integer limit) {
        LOGGER.info("List contacts for tenant {}", tenantId);

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
            Pageable pageable = PageRequest.of(page == null ? 0 : page, limit);
            list = contactRepo.findPageForTenant(tenantId, pageable);
        }
        LOGGER.info("Found {} contacts", list.size());
        return list;
    }

    /**
     * Return just the 'mailable' contacts for a specific tenant.
     *
     * @return contacts for that tenant.
     */
    @GetMapping(value = "/emailable")
    @JsonView(ContactViews.Summary.class)
    public @ResponseBody List<EntityModel<Contact>> listMailableForTenant(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info("List emailable contacts for tenant {}", tenantId);

        List<Contact> list;
        if (limit == null) {
            list = contactRepo.findAllEmailableForTenant(tenantId);
        } else {
            Pageable pageable = PageRequest.of(page == null ? 0 : page, limit);
            list = contactRepo.findEmailablePageForTenant(tenantId, pageable);
        }
        LOGGER.info("Found {} contacts", list.size());
        return addLinks(tenantId, list);
    }

    /**
     * Return just the matching contacts (probably will be one in almost every
     * case).
     *
     * @return contacts for that tenant.
     */
    @GetMapping(value = "/searchByAccountNameLastNameFirstName", params = {
            "accountName", "lastName", "firstName" })
    @JsonView(ContactViews.Summary.class)
    public @ResponseBody List<EntityModel<Contact>> listForAccountNameLastNameFirstName(
            @PathVariable("tenantId") String tenantId,
            @RequestParam("lastName") String lastName,
            @RequestParam("firstName") String firstName,
            @RequestParam("accountName") String accountName) {
        LOGGER.debug("List contacts for account and name {}, {} {}",
                accountName, lastName, firstName);

        List<Contact> list = contactRepo.findByFirstNameLastNameAndAccountName(
                firstName, lastName, accountName);
        LOGGER.info("Found {} contacts", list.size());

        return addLinks(tenantId, list);
    }

    /**
     * Return just the matching contacts (probably will be one in almost every
     * case).
     *
     * @return contacts for that tenant.
     */
    @GetMapping(value = "/{lastName}/{firstName}/{accountName}")
    public @ResponseBody List<EntityModel<Contact>> getForAccountNameLastNameFirstName(
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
    @Transactional
    @GetMapping(value = "/searchByEmail", params = { "email" })
    @JsonView(ContactViews.Summary.class)
    public @ResponseBody List<EntityModel<Contact>> searchByEmail(
            @PathVariable("tenantId") String tenantId,
            @RequestParam("email") String email) {
        LOGGER.debug("List contacts for email {}", email);

        // The unwrap stems from a restriction in Activiti JSON handling on
        // multi-instance loops
        List<Contact> list = contactRepo.findByEmail(unwrap(email), tenantId);
        LOGGER.info("Found {} contacts", list.size());

        if (list.size() > 10) {
            LOGGER.warn("Loading activities for {} contacts, this may be a bottleneck",
                            list.size());
        }
        // force load of custom fields
        for (Contact contact : list) {
            LOGGER.debug("  loaded {} custom fields for contact {}",
                    contact.getCustomFields().size(), contact.getId());
        }

        return addLinks(tenantId, list);
    }

    /**
     * Return just the matching contacts.
     *
     * @return contacts for that tenant with the matching tag.
     */
    @GetMapping(value = "/findByTag", params = { "tag" })
    public @ResponseBody List<EntityModel<Contact>> findByTag(
            @PathVariable("tenantId") String tenantId,
            @RequestParam("tag") String tag) {
        LOGGER.debug("List contacts for tag {}", tag);

        // The unwrap stems from a restriction in Activiti JSON handling on
        // multi-instance loops
        List<Contact> list = contactRepo.findByTag("%" + unwrap(tag) + "%",
                tenantId);
        LOGGER.info("Found {} contacts", list.size());

        return addLinks(tenantId, list);
    }

    protected Contact findById(final String tenantId, final Long id) {
        return contactRepo.findById(id)
                .orElseThrow(() -> new BusinessEntityNotFoundException(
                        Contact.class, id));
    }

    /**
     * Return just the matching contact.
     *
     * @return the contact with this id.
     */
    @GetMapping(value = "/{id}")
    @JsonView(ContactViews.Detailed.class)
    public @ResponseBody EntityModel<Contact> findEntityById(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long id) {
        LOGGER.debug("Find contact for id {}", id);
        return addLinks(tenantId, findById(tenantId, id));
    }

    /**
     * Return all contacts associated with an account.
     *
     * @return contacts matching that account.
     */
    @GetMapping(value = "/findByAccountId")
    @Transactional
    public @ResponseBody List<EntityModel<Contact>> findByAccountId(
            @PathVariable("tenantId") String tenantId,
            @RequestParam("accountId") String accountId) {
        LOGGER.debug("Find contact for account {}", accountId);

        return addLinks(tenantId,
                contactRepo.findByAccountId(Long.parseLong(accountId), tenantId));
    }

    /**
     * @return contacts matching the specified account type.
     */
    @GetMapping(value = "/findByAccountType")
    public @ResponseBody List<EntityModel<Contact>> findByAccountType(
            @PathVariable("tenantId") String tenantId,
            @RequestParam("accountType") String accountType) {
        LOGGER.debug("Find contact for account {}", accountType);
        return addLinks(tenantId, contactRepo.findByAccountType(accountType, tenantId));
    }

    /**
     * Return just the contacts matching the custom field.
     *
     * @return contacts matching the custom field for that tenant.
     */
    @GetMapping(value = "/findByCustomField/{key}/{value}")
    public @ResponseBody List<EntityModel<Contact>> findByCustomField(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("key") String key,
            @PathVariable("value") String value) {
        LOGGER.debug("List contacts for custom field {}={}", key, value);

        List<Contact> list = contactRepo.findByCustomField(key, value, tenantId);
        LOGGER.info("Found {} contacts", list.size());

        return addLinks(tenantId, list);
    }

    /**
     * Return just the matching contact.
     *
     * @return contacts for that tenant with the matching tag.
     */
    @GetMapping(value = "/findByUuid", params = { "uuid" })
    @Transactional
    public @ResponseBody EntityModel<Contact> findByUuid(
            @PathVariable("tenantId") String tenantId,
            @RequestParam("uuid") String uuid) {
        LOGGER.debug("Find contact for uuid {}", uuid);

        return addLinks(tenantId, consolidateContactsWithUuid(uuid, tenantId));
    }

    /**
     * Return contacts currently active on the site.
     *
     * <p>
     * TODO 'Currently' can be defined per tenant.
     * </p>
     *
     * @return contacts for that tenant with the matching tag.
     */
    @GetMapping(value = "/findActive")
    @Transactional
    public @ResponseBody List<EntityModel<Contact>> findActive(
            @PathVariable("tenantId") String tenantId) {
        LOGGER.debug("Find active contacts for tenant {}", tenantId);

        GregorianCalendar cal = new GregorianCalendar();
        cal.add(Calendar.MINUTE, minsConsideredActive);
        Date sinceDate = cal.getTime();
        return addLinks(tenantId, contactRepo.findActiveForTenant(sinceDate, tenantId));
    }

    protected synchronized Contact consolidateContactsWithUuid(String uuid,
            String tenantId) {
        // Because of ASYNC calls from JavaScript uuid may not be unique...
        List<Contact> contacts = contactRepo.findByUuid(uuid, tenantId);
        switch (contacts.size()) {
        case 0:
            LOGGER.warn("No contact with uuid: {}", uuid);
            throw new BusinessEntityNotFoundException(Contact.class, uuid);
        case 1:
            Contact contact = contacts.get(0);
            LOGGER.info("Found contact: {}", contact.getId());
            return contact;
        default:
            // TODO should we attempt a cleanup?
            LOGGER.warn("Found {} contacts with uuid: {}...", 
                    contacts.size(), uuid);
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
    public @ResponseBody ResponseEntity<EntityModel<Contact>> create(
            @PathVariable("tenantId") String tenantId,
            @RequestBody Contact contact) {
        contact.setTenantId(tenantId);

        if (contact.getAccountId() != null) {
            contact.setAccount(accountSvc.findById(tenantId, contact.getAccountId()));
        }
        for (CustomContactField field : contact.getCustomFields()) {
            field.setContact(contact);
        }
        EntityModel<Contact> entity = addLinks(tenantId, contactRepo.save(contact));
        LOGGER.debug("Created contact {}", entity.getLink("self"));

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(entity.getLink("self").get().toUri());

        return new ResponseEntity(contact, headers, HttpStatus.CREATED);
    }

    /**
     * Update an existing contact.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { "application/json" })
    public @ResponseBody void update(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long contactId,
            @RequestBody Contact updatedContact) {
        Contact contact = findById(tenantId, contactId);

        if (updatedContact.isFirstNameDefault()) {
            updatedContact.setFirstName(contact.getFirstName());
        }
        if (updatedContact.isLastNameDefault()) {
            updatedContact.setLastName(contact.getLastName());
        }
        NullAwareBeanUtils.copyNonNullProperties(updatedContact, contact, "id",
                "account", "activities", "notes", "documents");
        for (CustomContactField field : updatedContact.getCustomFields()) {
            Optional<CustomContactField> trgtField = contact.getCustomFields().stream().filter(f -> f.getName().equals(field.getName())).findAny();
            if (trgtField.isPresent()) {
                trgtField.get().setValue(field.getValue());
            } else {
                contact.getCustomFields().add(field);
            }
        }
        contact.setTenantId(tenantId);
        contact.setLastUpdated(new Date());
        contactRepo.save(contact);

        // For some reason contact never has account deserialised even though it
        // is sent by browser
        if (updatedContact.getAccount() != null) {
            Account account = accountSvc.findById(tenantId,
                    updatedContact.getAccount().getId());
            NullAwareBeanUtils.copyNonNullProperties(
                    updatedContact.getAccount(), account, "id");
            account.setTenantId(tenantId);
            accountRepo.save(account);
        }
    }
    /**
     * Delete an existing contact.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, consumes = { "application/json" })
    public @ResponseBody void delete(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long contactId) {
        Contact contact = findById(tenantId, contactId);

        contactRepo.delete(contact);
    }

    /**
     * Add a document to the specified contact.
     */
    @RequestMapping(value = "/{contactId}/documents", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody ResponseEntity<Document> addDocument(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId, @RequestBody Document doc) {
         Contact contact = findById(tenantId, contactId);
         contact.getDocuments().add(doc);
         contact.setLastUpdated(new Date());
         contactRepo.save(contact);
         doc = contact.getDocuments().get(contact.getDocuments().size()-1);

         HttpHeaders headers = new HttpHeaders();
         URI uri = MvcUriComponentsBuilder.fromController(getClass())
                 .path("/{id}/documents/{docId}")
                 .buildAndExpand(tenantId, contact.getId(), doc.getId())
                 .toUri();
         headers.setLocation(uri);

         return new ResponseEntity<Document>(doc, headers, HttpStatus.CREATED);
    }

    /**
     * Add a document to the specified contact.
     *      *
     * <p>This is just a convenience method, see {@link #addDocument(String, Long, Document)}
     * @return
     *
     * @return The document created.
     */
    @RequestMapping(value = "/{contactId}/documents", method = RequestMethod.POST, consumes = "application/x-www-form-urlencoded")
    public @ResponseBody Document addDocument(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId,
            @RequestParam("author") String author,
            @RequestParam("name") String name,
            @RequestParam("url") String url) {

        return addDocument(tenantId, contactId, new Document(author, name, url)).getBody();
    }

    /**
     * Add a note to the specified contact.
     * @return the created note.
     */
    @RequestMapping(value = "/{contactId}/notes", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody ResponseEntity<Note> addNote(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId, @RequestBody Note note) {
        Contact contact = findById(tenantId, contactId);
        contact.getNotes().add(note);
        contact.setLastUpdated(new Date());
        contactRepo.save(contact);
        note = contact.getNotes().get(contact.getNotes().size()-1);

        HttpHeaders headers = new HttpHeaders();
        URI uri = MvcUriComponentsBuilder.fromController(getClass())
                .path("/{id}/notes/{noteId}")
                .buildAndExpand(tenantId, contact.getId(), note.getId())
                .toUri();
        headers.setLocation(uri);

        return new ResponseEntity<Note>(note, headers, HttpStatus.CREATED);
    }

    /**
     * Add a note to the specified contact from its parts.
     *
     * <p>This is just a convenience method, see {@link #addNote(String, Long, Note)}
     *
     * @return The note created.
     */
    @RequestMapping(value = "/{contactId}/notes", method = RequestMethod.POST, consumes = "application/x-www-form-urlencoded")
    public @ResponseBody Note addNote(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId,
            @RequestParam("author") String author,
            @RequestParam("favorite") boolean favorite,
            @RequestParam("content") String content) {
        return addNote(tenantId, contactId, new Note(author, content, favorite)).getBody();
    }

    /**
     * Change the sale stage the contact is at.
     */
    @RequestMapping(value = "/{contactId}/stage", method = RequestMethod.PUT, consumes = "application/x-www-form-urlencoded")
    public @ResponseBody void setStageDeprecated2(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId,
            @RequestParam("stage") String stage) {
        LOGGER.warn("PUT stage is deprecated, please switch to POST");
        setStage(tenantId, contactId, stage);
    }

    /**
     * Change the sale stage the contact is at.
     */
    @RequestMapping(value = "/{contactId}/stage/{stage}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public @ResponseBody void setStage(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId,
            @PathVariable("stage") String stage) {
        LOGGER.info("Setting contact {} to stage {}", contactId, stage);

        Contact contact = findById(tenantId, contactId);
        String oldStage = contact.getStage();
        if (oldStage == null || !stage.equals(oldStage)) {
            contact.setStage(stage);
            contactRepo.save(contact);

            addActivity(tenantId, contactId, ActivityType.TRANSITION_TO_STAGE.name(),
                    String.format("From %1$s to %2$s", oldStage, stage));
        }
    }

    /**
     * Set the account this contact belongs to.
     */
    @RequestMapping(value = "/{contactId}/account", method = RequestMethod.PUT, consumes = "text/uri-list")
    @Transactional
    public @ResponseBody void setAccount(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId,
            @RequestBody String accountUri) {
        LOGGER.info("Linking account {} to contact {}", contactId, accountUri);

        Long acctId = Long.parseLong(accountUri.substring(accountUri
                .lastIndexOf('/') + 1));

        contactRepo.setAccount(contactId, acctId);

        addActivity(tenantId, contactId,
                new Activity(ActivityType.LINK_ACCOUNT_TO_CONTACT,
                new Date(), String.format("Linked account %1$d to contact %2$d", 
                        acctId, contactId)));
    }

    /**
     * Add an activity to the specified contact.
     */
    @RequestMapping(value = "/{contactId}/activities", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody ResponseEntity<Void> addActivity(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId,
            @RequestBody Activity activity) {
        Contact contact = findById(tenantId, contactId);
        contact.getActivities().add(activity);
        contact.setLastUpdated(new Date());
        contactRepo.save(contact);
        activity = contact.getActivities().get(contact.getActivities().size()-1);

        HttpHeaders headers = new HttpHeaders();
        URI uri = MvcUriComponentsBuilder.fromController(getClass())
                .path("/{id}/activities/{activityId}")
                .buildAndExpand(tenantId, contact.getId(), activity.getId())
                .toUri();
        headers.setLocation(uri);

        return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
    }

    /**
     * Add an activity to the specified contact.
     * @return the created activity.
     */
    @RequestMapping(value = "/{contactId}/activities", method = RequestMethod.POST, consumes = "application/x-www-form-urlencoded")
    @Transactional
    public @ResponseBody ResponseEntity<Void> addActivity(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId,
            @RequestParam("type") String type,
            @RequestParam("content") String content) {
        return addActivity(tenantId, contactId, new Activity(ActivityType.valueOf(type), new Date(), content));
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
        Contact contact = findById(tenantId, contactId);
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

    protected List<EntityModel<Contact>> addLinks(final String tenantId, final Iterable<Contact> result) {
        ArrayList<EntityModel<Contact>> entities = new ArrayList<EntityModel<Contact>>();
        for (Contact contact : result) {
            entities.add(addLinks(tenantId, contact));
        }
        return entities;
    }

    protected EntityModel<Contact> addLinks(final String tenantId, final Contact contact) {
        return EntityModel.of(contact,
                linkTo(methodOn(ContactController.class).findEntityById(tenantId, contact.getId()))
                        .withSelfRel());
    }
}
