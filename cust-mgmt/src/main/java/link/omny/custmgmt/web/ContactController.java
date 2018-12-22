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

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

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
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import link.omny.custmgmt.internal.CsvImporter;
import link.omny.custmgmt.internal.DateUtils;
import link.omny.custmgmt.internal.NullAwareBeanUtils;
import link.omny.custmgmt.json.JsonCustomContactFieldDeserializer;
import link.omny.custmgmt.json.JsonCustomFieldSerializer;
import link.omny.custmgmt.model.Account;
import link.omny.custmgmt.model.Activity;
import link.omny.custmgmt.model.ActivityType;
import link.omny.custmgmt.model.Contact;
import link.omny.custmgmt.model.CustomContactField;
import link.omny.custmgmt.model.CustomField;
import link.omny.custmgmt.model.Document;
import link.omny.custmgmt.model.Note;
import link.omny.custmgmt.model.views.ContactViews;
import link.omny.custmgmt.repositories.AccountRepository;
import link.omny.custmgmt.repositories.ContactRepository;
import link.omny.supportservices.exceptions.BusinessEntityNotFoundException;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * REST web service for uploading and accessing a file of JSON Contacts (over
 * and above the CRUD offered by spring data).
 *
 * @author Tim Stephenson
 */
@Controller
@RequestMapping(value = "/{tenantId}/contacts")
public class ContactController extends BaseTenantAwareController {

    static final Logger LOGGER = LoggerFactory
            .getLogger(ContactController.class);

    @Autowired
    private ContactRepository contactRepo;

    @Autowired
    private AccountRepository accountRepo;

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
    @RequestMapping(value = "/contacts.csv", method = RequestMethod.GET, produces = "text/csv")
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
        LOGGER.info(String.format(
                "Set contacts of %1$s older than %2$s to '%3$s'", tenantId,
                beforeDate.toString(), stage));

        return contactRepo.updateStage(stage, beforeDate, tenantId);
    }

    /**
     * Return just the contacts for a specific tenant.
     *
     * @return contacts for that tenant.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "text/csv")
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
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<? extends ResourceSupport> listForTenantAsJson(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "returnFull", required = false) boolean returnFull) {
        if (returnFull) {
            return wrap(listForTenant(tenantId, page, limit));
        } else {
            return wrapShort(listForTenant(tenantId, page, limit));
        }
    }

    protected List<Contact> listForTenant(String tenantId,
            Integer page, Integer limit) {
        LOGGER.info(String.format("List contacts for tenant %1$s", tenantId));

        List<Contact> list;
        if (limit == null) {
            list = contactRepo.findAllForTenant(tenantId);
        } else {
            Pageable pageable = new PageRequest(page == null ? 0 : page, limit);
            list = contactRepo.findPageForTenant(tenantId, pageable);
        }
        LOGGER.info(String.format("Found %1$s contacts", list.size()));

        return list;
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

        return wrapShort(list);
    }

    /**
     * Return just the matching contacts (probably will be one in almost every
     * case).
     *
     * @return contacts for that tenant.
     */
    @RequestMapping(value = "/searchByAccountNameLastNameFirstName", method = RequestMethod.GET, params = {
            "accountName", "lastName", "firstName" })
    public @ResponseBody List<ContactResource> listForAccountNameLastNameFirstName(
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
    public @ResponseBody List<ContactResource> getForAccountNameLastNameFirstName(
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
    @RequestMapping(value = "/searchByEmail", method = RequestMethod.GET, params = { "email" })
    public @ResponseBody List<ContactResource> searchByEmail(
            @PathVariable("tenantId") String tenantId,
            @RequestParam("email") String email) {
        LOGGER.debug(String.format("List contacts for email %1$s", email));

        // The unwrap stems from a restriction in Activiti JSON handling on
        // multi-instance loops
        List<Contact> list = contactRepo.findByEmail(unwrap(email), tenantId);
        LOGGER.info(String.format("Found %1$s contacts", list.size()));

        if (list.size() > 10) {
            LOGGER.warn(String
                    .format("Loading activities for %1$d contacts, this may be a bottleneck",
                            list.size()));
        }
        // force load of custom fields
        for (Contact contact : list) {
            LOGGER.debug(String.format(
                    "  loaded %1$d custom fields for contact %2$d",
                    contact.getCustomFields().size(), contact.getId()));
        }

        return wrap(list);
    }

    /**
     * Return just the matching contacts.
     *
     * @return contacts for that tenant with the matching tag.
     */
    @RequestMapping(value = "/findByTag", method = RequestMethod.GET, params = { "tag" })
    public @ResponseBody List<ContactResource> findByTag(
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
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @JsonView(ContactViews.Detailed.class)
    public @ResponseBody Contact findById(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id) {
        LOGGER.debug(String.format("Find contact for id %1$s", id));
        return addLinks(tenantId, contactRepo.findOne(Long.parseLong(id)));
    }

    /**
     * Return all contacts associated with an account.
     *
     * @return contacts matching that account.
     */
    @RequestMapping(value = "/findByAccountId", method = RequestMethod.GET)
    @Transactional
    public @ResponseBody List<ContactResource> findByAccountId(
            @PathVariable("tenantId") String tenantId,
            @RequestParam("accountId") String accountId) {
        LOGGER.debug(String.format("Find contact for account %1$s", accountId));

        return wrap(contactRepo.findByAccountId(Long.parseLong(accountId),
                tenantId));
    }

    /**
     * @return contacts matching the specified account type.
     */
    @RequestMapping(value = "/findByAccountType", method = RequestMethod.GET)
    public @ResponseBody List<ContactResource> findByAccountType(
            @PathVariable("tenantId") String tenantId,
            @RequestParam("accountType") String accountType) {
        LOGGER.debug(String.format("Find contact for account %1$s", accountType));
        return wrap(contactRepo.findByAccountType(accountType, tenantId));
    }

    /**
     * Return just the matching contact.
     *
     * @return contacts for that tenant with the matching tag.
     */
    @RequestMapping(value = "/findByUuid", method = RequestMethod.GET, params = { "uuid" })
    @Transactional
    public @ResponseBody ContactResource findByUuid(
            @PathVariable("tenantId") String tenantId,
            @RequestParam("uuid") String uuid) {
        LOGGER.debug(String.format("Find contact for uuid %1$s", uuid));

        return (ContactResource) wrap(
                consolidateContactsWithUuid(uuid, tenantId),
                new ContactResource());
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
    @RequestMapping(value = "/findActive", method = RequestMethod.GET)
    @Transactional
    public @ResponseBody List<ShortContact> findActive(
            @PathVariable("tenantId") String tenantId) {
        LOGGER.debug(String.format("Find active contacts for tenant %1$s",
                tenantId));

        GregorianCalendar cal = new GregorianCalendar();
        cal.add(Calendar.MINUTE, minsConsideredActive);
        Date sinceDate = cal.getTime();
        return wrapShort(contactRepo.findActiveForTenant(sinceDate, tenantId));
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
    public @ResponseBody ResponseEntity<? extends Contact> create(
            @PathVariable("tenantId") String tenantId,
            @RequestBody Contact contact) {
        contact.setTenantId(tenantId);

        if (contact.getAccountId() != null) {
            contact.setAccount(accountRepo.findOne(contact.getAccountId()));
        }
        for (CustomContactField field : contact.getCustomFields()) {
            field.setContact(contact);
        }
        contact = contactRepo.save(contact);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(getGlobalUri(contact.getId()));

        // TODO migrate to http://host/tenant/contacts/id
        // headers.setLocation(getTenantBasedUri(tenantId, contact));

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
        Contact contact = contactRepo.findOne(contactId);

        if (updatedContact.isFirstNameDefault()) {
            updatedContact.setFirstName(contact.getFirstName());
        }
        if (updatedContact.isLastNameDefault()) {
            updatedContact.setLastName(contact.getLastName());
        }
        NullAwareBeanUtils.copyNonNullProperties(updatedContact, contact, "id",
                "account", "activities", "notes", "documents");
        contact.setTenantId(tenantId);
        contact.setLastUpdated(new Date());
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
    @RequestMapping(value = "/{contactId}/documents", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody ResponseEntity<Document> addDocument(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId, @RequestBody Document doc) {
         Contact contact = contactRepo.findOne(contactId);
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
        Contact contact = contactRepo.findOne(contactId);
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
        LOGGER.warn(String.format("PUT stage is deprecated, please switch to POST"));
        setStage(tenantId, contactId, stage);
    }

    /**
     * Change the sale stage the contact is at.
     */
    @RequestMapping(value = "/{contactId}/stage/{stage}", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<Contact> setStage(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId,
            @PathVariable("stage") String stage) {
        LOGGER.info(String.format("Setting contact %1$s to stage %2$s",
                contactId, stage));

        Contact contact = contactRepo.findOne(contactId);
        String oldStage = contact.getStage();
        if (oldStage == null || !oldStage.equals(stage)) {
            contact.setStage(stage);
            contactRepo.save(contact);

            Activity activity = new Activity(ActivityType.TRANSITION_TO_STAGE,
                    new Date(), String.format("From %1$s to %2$s", oldStage, stage));
            addActivity(tenantId, contactId, activity);
        }

        return new ResponseEntity<Contact>(contact,
                new HttpHeaders(), HttpStatus.NO_CONTENT);
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

        addActivity(tenantId, contactId,
                new Activity(ActivityType.LINK_ACCOUNT_TO_CONTACT,
                new Date(), String.format("Linked for %1$s", accountUri)));
    }

    /**
     * Add activity to the specified contact.
     * @return the created activity.
     */
    @RequestMapping(value = "/{contactId}/activities", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody ResponseEntity<Activity> addActivity(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId, @RequestBody Activity activity) {
        Contact contact = contactRepo.findOne(contactId);
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

        return new ResponseEntity<Activity>(activity, headers, HttpStatus.CREATED);
    }

    /**
     * Add an activity to the specified contact.
     * @return the created activity.
     */
    @RequestMapping(value = "/{contactId}/activities", method = RequestMethod.POST, consumes = "application/x-www-form-urlencoded")
    public @ResponseBody ResponseEntity<Activity> addActivity(
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

    private List<ShortContact> wrapShort(List<Contact> list) {
        List<ShortContact> resources = new ArrayList<ShortContact>(list.size());
        for (Contact contact : list) {
            resources.add((ShortContact) wrap(contact, new ShortContact()));
        }
        return resources;
    }

    private List<ContactResource> wrap(List<Contact> list) {
        List<ContactResource> resources = new ArrayList<ContactResource>(
                list.size());
        for (Contact contact : list) {
            resources
                    .add((ContactResource) wrap(contact, new ContactResource()));
        }
        return resources;
    }

    private ResourceSupport wrap(Contact contact, ResourceSupport resource) {
        BeanUtils.copyProperties(contact, resource);

        // 03 Jun 17 inexplicably Jackson started getting infinite recursion issue
        // maybe due to Spring introspection changes implied by #511?
        if (resource instanceof ContactResource) {
            ContactResource c = ((ContactResource) resource);
            c.setCustomFields(contact.getCustomFields());
            if (c.getAccount()!=null) {
                c.getAccount().setContact(null);
            }
        }

        Link detail = new Link(getGlobalUri(contact.getId()).toString());
        resource.add(detail);
        if (contact.getAccount() != null) {
            try {
                BeanUtils
                        .getPropertyDescriptor(resource.getClass(),
                                "accountName").getWriteMethod()
                        .invoke(resource, contact.getAccount().getName());
            } catch (Exception e) {
                LOGGER.warn(String
                        .format("Unable to set account name for contact %1$d"),
                        contact.getId());
            }
            resource.add(linkTo(AccountRepository.class,
                    contact.getAccount().getId()).withRel("account"));
        }
        try {
            BeanUtils.getPropertyDescriptor(resource.getClass(), "selfRef")
                    .getWriteMethod().invoke(resource, detail.getHref());
        } catch (Exception e) {
            LOGGER.warn(String.format("Unable to set selfRef for contact %1$d"),
                    contact.getId());
        }
        try {
            BeanUtils.getPropertyDescriptor(resource.getClass(), "alerts")
                    .getWriteMethod()
                    .invoke(resource, contact.getAlertsAsList());
        } catch (Exception e) {
            LOGGER.warn(String.format("Unable to set alerts for contact %1$d"),
                    contact.getId());
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
    @JsonInclude(value = Include.NON_EMPTY)
    public static class ContactResource extends ResourceSupport {
        private String selfRef;
        private String firstName;
        private String lastName;
        private String fullName;
        private String title;
        private boolean mainContact;
        private String address1;
        private String address2;
        private String town;
        private String countyOrCity;
        private String country;
        private String postCode;
        private String address;
        private String email;
        private boolean emailConfirmed;
        private Boolean emailOptIn;
        private String jobTitle;
        private String phone1;
        private String phone2;
        private String accountName;
        private String owner;
        private String stage;
        private String stageReason;
        private Date stageDate;
        private String enquiryType;
        private String accountType;
        private List<String> alerts;
        private boolean existingCustomer;
        private String source;
        private String source2;
        private String medium;
        private String campaign;
        private String keyword;
        private boolean doNotCall;
        private boolean doNotEmail;
        private String tags;
        private String uuid;
        private String twitter;
        private String linkedIn;
        private String facebook;
        private String skype;
        private String tenantId;
        private Date firstContact;
        private Date lastUpdated;
        private long timeSinceLogin;
        private long timeSinceFirstLogin;
        private long timeSinceRegistered;
        private long timeSinceEmail;
        private int emailsSent;
        private long timeSinceValuation;
        @JsonDeserialize(using = JsonCustomContactFieldDeserializer.class)
        @JsonSerialize(using = JsonCustomFieldSerializer.class)
        private List<CustomContactField> customFields;
        private Account account;
        private List<Activity> activities;
        private List<Note> notes;
        private List<Document> documents;

        public Object getCustomFieldValue(@NotNull String fieldName) {
            for (CustomField field : getCustomFields()) {
                if (fieldName.equals(field.getName())) {
                    return field.getValue();
                }
            }
            return null;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ShortContact extends ResourceSupport {
        private String selfRef;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String phone1;
        private String jobTitle;
        private boolean mainContact;
        // Note when migrating: this is not included in ContactViews.Summary
        private String accountName;
        private String owner;
        private String stage;
        private String enquiryType;
        private String accountType;
        private String tags;
        private List<String> alerts;
        private String tenantId;
        private Date firstContact;
        private Date lastUpdated;
    }

    private Contact addLinks(String tenantId, Contact contact) {
        List<Link> links = new ArrayList<Link>();
        links.add(new Link(String.format("/%1$s/contacts/%2$s",
                tenantId, contact.getId())));
        contact.setLinks(links);
        return contact;
    }
}
