/*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributors
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
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import link.omny.custmgmt.internal.DateUtils;
import link.omny.custmgmt.model.Account;
import link.omny.custmgmt.model.Contact;
import link.omny.custmgmt.model.CustomAccountField;
import link.omny.custmgmt.model.views.AccountViews;
import link.omny.custmgmt.repositories.AccountRepository;
import link.omny.custmgmt.repositories.ContactRepository;
import link.omny.custmgmt.repositories.CustomAccountRepository;
import link.omny.supportservices.exceptions.BusinessEntityNotFoundException;
import link.omny.supportservices.model.Activity;
import link.omny.supportservices.model.ActivityType;
import link.omny.supportservices.model.Document;
import link.omny.supportservices.model.Note;

/**
 * REST web service for uploading and accessing a file of JSON Accounts (over
 * and above the CRUD offered by spring data).
 *
 * @author Tim Stephenson
 */
@Controller
@RequestMapping(value = "/{tenantId}")
@Tag(name = "Account API")
public class AccountController {

    public static final Logger LOGGER = LoggerFactory
            .getLogger(AccountController.class);

    @Autowired
    public AccountRepository accountRepo;

    @Autowired
    private CustomAccountRepository accountSvc;

    @Autowired
    private ContactRepository contactRepo;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Add activity to the specified account.
     * @return the created activity.
     */
    @PostMapping(value = "/accounts/{accountId}/activities")
    @Operation(summary = "Add an activity to the specified account.")
    public @ResponseBody ResponseEntity<Activity> addActivity(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("accountId") Long accountId, @RequestBody Activity activity) {
        Account account = findById(tenantId, accountId);
        account.getActivities().add(activity);
        account.setLastUpdated(new Date());
        account = accountRepo.save(account);
        activity = account.getActivities().stream()
                .reduce((first, second) -> second).orElse(null);

        HttpHeaders headers = new HttpHeaders();
        URI uri = MvcUriComponentsBuilder.fromController(getClass())
                .path("/accounts/{id}/activities/{activityId}")
                .buildAndExpand(tenantId, account.getId(), activity.getId())
                .toUri();
        headers.setLocation(uri);

        return new ResponseEntity<Activity>(activity, headers, HttpStatus.CREATED);
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
    @PostMapping(value = "/accounts/upload")
    @Operation(hidden = true)
    public @ResponseBody Iterable<Account> handleFileUpload(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "file", required = true) MultipartFile file)
            throws IOException {
        LOGGER.info("Uploading accounts for: {}", tenantId);
        String content = new String(file.getBytes());

        List<Account> list = objectMapper.readValue(content,
                new TypeReference<List<Account>>() {
                });
        LOGGER.info("  found {} accounts", list.size());
        for (Account account : list) {
            account.setTenantId(tenantId);
        }

        Iterable<Account> result = accountRepo.saveAll(list);
        LOGGER.info("  saved.");

        return result;
    }

    @PostMapping(value = "/accounts/archive")
    @Transactional
    @Operation(hidden = true)
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
    @GetMapping(value = "/accounts/")
    @JsonView(value = AccountViews.Summary.class)
    @Operation(summary = "Retrieves the accounts for a specific tenant.")
    public @ResponseBody List<EntityModel<Account>> listForTenantAsJson(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return addLinks(tenantId, accountSvc.listForTenant(tenantId, page, limit));
    }

    /**
     * Return just the accounts for a specific tenant.
     *
     * @return accounts for that tenant.
     */
    @GetMapping(value = "/accounts/", produces = "text/csv")
    @Operation(summary = "Retrieves the accounts for a specific tenant.")
    public @ResponseBody ResponseEntity<String> listForTenantAsCsv(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        StringBuilder sb = new StringBuilder()
                .append("id,name,companyNumber,aliases,businessWebsite,"
                        + "address1,address2,town,countyOrCity,country,"
                        + "postCode,email,phone1,phone2,owner,stage,"
                        + "stageReason,stageDate,enquiryType,accountType,"
                        + "isExistingCustomer,tags,twitter,linkedIn,facebook,"
                        + "shortDesc,description,incorporationYear,noOfEmployees,"
                        + "tenantId,firstContact,lastUpdated,notes,documents,");
        List<String> customFieldNames = accountRepo.findCustomFieldNames(tenantId);
        LOGGER.info("Found {} custom field names while exporting accounts for {}: {}",
                customFieldNames.size(), tenantId, customFieldNames);
        for (String fieldName : customFieldNames) {
            sb.append(fieldName).append(",");
        }
        sb.append("\r\n");

        for (Account account : accountSvc.listForTenant(tenantId, page, limit)) {
            account.setCustomHeadings(customFieldNames);
            sb.append(account.toCsv()).append("\r\n");
        }
        LOGGER.info("Exporting CSV accounts for {} generated {} bytes",
                tenantId, sb.length());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentLength(sb.length());
        return new ResponseEntity<String>(
                sb.toString(), httpHeaders, HttpStatus.OK);
    }

    /**
     * @return Account id-name pairs for a specific tenant.
     */
    @GetMapping(value = "/account-pairs/")
    @JsonView(value = AccountViews.Pair.class)
    @Operation(summary = "Retrieve account id-name pairs for a specific tenant.")
    public @ResponseBody List<Account> listPairForTenant(
            @PathVariable("tenantId") String tenantId) {
        LOGGER.info("List account id-name pairs for tenant {}", tenantId);
        return accountSvc.listForTenant(tenantId);
    }

    /**
     * Create a new account.
     *
     * @return if successful: status = 201 and Location header
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(value = "/accounts/")
    @Operation(summary = "Create a new account.")
    public @ResponseBody ResponseEntity<?> create(
            @PathVariable("tenantId") String tenantId,
            @RequestBody Account account) {
        account = account.setTenantId(tenantId);
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
        headers.setLocation(builder2.path("/accounts/{id}")
                .buildAndExpand(vars).toUri());
        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    protected Account findById(final String tenantId, @NonNull final Long id) {
        return accountRepo.findById(id)
                .orElseThrow(() -> new BusinessEntityNotFoundException(
                        Account.class, id));
    }

    /**
     * Return just the matching account.
     *
     * @return the account with this id.
     * @throws BusinessEntityNotFoundException
     */
    @GetMapping(value = "/accounts/{id}")
    @JsonView(AccountViews.Detailed.class)
    @Transactional
    @Operation(summary = "Return the specified account.")
    public @ResponseBody HttpEntity<String> findEntityById(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String idOrCode)
            throws BusinessEntityNotFoundException {
        LOGGER.info("Find account for id {}", idOrCode);

        Account account;
        try {
            account = findById(tenantId, Long.parseLong(idOrCode));
        } catch (NumberFormatException e) {
            account = accountRepo.findByCodeForTenant(idOrCode, tenantId);
        }

        EntityModel<Account> entity = addLinks(tenantId, account);
        // Work around issue with Jackson serialisation:
        // If return EntityModel<Account> result is:
        // Resolved [org.springframework.http.converter.HttpMessageNotWritableException: Could not write JS
        // ON: Cannot override _serializer: had a `link.omny.supportservices.json.JsonCustomFieldSerializer`
        // , trying to set to `org.springframework.data.rest.webmvc.json.PersistentEntityJackson2Module$Nest
        // edEntitySerializer`]
        try {
            String json = objectMapper.writeValueAsString(entity);
            LOGGER.info("... found: {}", json);
            return new HttpEntity<String>(json);
        } catch (JsonProcessingException e) {
            LOGGER.error("Unable to serialise account with id {}, cause: {}", idOrCode, e);
            throw new BusinessEntityNotFoundException(Account.class, idOrCode);
        }
    }

    /**
     * Return just the matching account.
     *
     * @return the account with this name.
     * @throws BusinessEntityNotFoundException
     */
    @GetMapping(value = "/accounts/findByName/{name}")
    @JsonView(AccountViews.Detailed.class)
    @Operation(summary = "Return the specified account.")
    public @ResponseBody EntityModel<Account> findByName(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("name") String name)
            throws BusinessEntityNotFoundException {
        LOGGER.debug("Find account with name {}", name);

        Account account = accountRepo.findByNameForTenant(name, tenantId);
        if (account == null) {
            throw new BusinessEntityNotFoundException(Account.class, name);
        }
        account.getActivities(); // force load

        return addLinks(tenantId, account);
    }

    /**
     * Return accounts with the matching custom field.
     *
     * @return the account with this name.
     * @throws BusinessEntityNotFoundException
     */
    @GetMapping(value = "/accounts/findByCustomField/{name}/{value}")
    @JsonView(AccountViews.Summary.class)
    @Operation(hidden = true)
    public @ResponseBody List<EntityModel<Account>> findByCustomField(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("name") String name,
            @PathVariable("value") String value)
            throws BusinessEntityNotFoundException {
        LOGGER.debug("Find account with custom field {}={}", name, value);

        List<Account> accounts = accountRepo
                .findByCustomFieldForTenant(name, value, tenantId);

        return addLinks(tenantId, accounts);
    }

    /**
     * Update an existing account.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PutMapping(value = "/accounts/{id}", consumes = "application/json")
    @Operation(summary = "Update an existing account.")
    public @ResponseBody void update(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long accountId,
            @RequestBody Account updatedAccount) {
        Account account = findById(tenantId, accountId);
        BeanUtils.copyProperties(updatedAccount, account, "id", "activities", "documents", "notes");
        account.setTenantId(tenantId);
        account = accountRepo.save(account);
        updateCustomFields(tenantId, accountId, updatedAccount.getCustomFields());
    }

    /**
     * Update an existing account's custom fields.
     * @throws IOException
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PostMapping(value = "/accounts/{id}/customFields/", consumes = "application/json")
    @Operation(hidden = true)
    public @ResponseBody void updateCustomFields(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long accountId,
            @RequestBody Object customFields) {
        Account account = findById(tenantId, accountId);

        if (customFields instanceof String) {
            JsonNode jsonNode;
            try {
                jsonNode = objectMapper.readTree((String) customFields);
            } catch (JsonProcessingException e) {
                LOGGER.error("updateCustomFields({}, {}, {}), root cause: {}",
                        tenantId, accountId, customFields, e);
                throw new IllegalArgumentException("Unable to read account", e);
            }
            for (Iterator<String> it = jsonNode.fieldNames() ; it.hasNext() ;) {
                String key = it.next();
                account.addCustomField(
                        new CustomAccountField(key, jsonNode.get(key).asText()));
            }
        } else if (customFields instanceof Set<?>) {
            @SuppressWarnings("unchecked")
            Set<CustomAccountField> set = (Set<CustomAccountField>) customFields;
            set.forEach((field) -> {
                LOGGER.warn("About to try to store {}={}",
                        field.getName(), field.getValue());
                account.addCustomField(field);
            });
        } else if (customFields instanceof HashMap) {
            for (Map.Entry<?,?> entry : ((HashMap<?,?>) customFields).entrySet()) {
                LOGGER.warn("About to try to store {}={}", entry.getKey(), entry.getValue());
                account.addCustomField(
                        new CustomAccountField((String)entry.getKey(), entry.getValue().toString()));
            }
        } else {
            String msg = String.format(
                    "Attempt to set custom fields from %1$s is not supported",
                    customFields.getClass().getName());
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }

        accountRepo.save(account);
    }

    /**
     * Update the alerts of the specified account.
     *
     * @param tenantId
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PostMapping(value = "/accounts/{accountId}/alerts/")
    @Operation(hidden = true)
    public @ResponseBody void setAlerts(@PathVariable("tenantId") String tenantId,
            @PathVariable("accountId") Long accountId,
            @RequestBody String alerts) {
        Account account = findById(tenantId, accountId);
        account.setAlerts(alerts);
        account.setTenantId(tenantId);
        accountRepo.save(account);
    }

    /**
     * Change the sale stage the account is at.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PostMapping(value = "/accounts/{accountId}/stage/{stage}")
    @Operation(summary = "Sets the stage for the specified account.")
    public @ResponseBody void setStage(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("accountId") Long accountId,
            @PathVariable("stage") String stage) {
        LOGGER.info("Setting contact {} to stage {}", accountId, stage);

        Account account = findById(tenantId, accountId);
        String oldStage = account.getStage();
        if (oldStage == null || !oldStage.equals(stage)) {
            account.setStage(stage);
            accountRepo.save(account);

            Activity activity = new Activity(ActivityType.TRANSITION_TO_STAGE,
                    new Date(), String.format("From %1$s to %2$s", oldStage, stage));
            addActivity(tenantId, accountId, activity);
        }
    }

    /**
     * Update the tags of the specified account.
     *
     * @param tenantId
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PostMapping(value = "/accounts/{accountId}/tags/")
    @Operation(hidden = true)
    public @ResponseBody void setTags(@PathVariable("tenantId") String tenantId,
            @PathVariable("accountId") Long accountId,
            @RequestBody String tags) {
        Account account = findById(tenantId, accountId);
        account.setTags(tags);
        account.setTenantId(tenantId);
        accountRepo.save(account);
    }

    /**
     * Add a document to the specified account.
     */
    @PostMapping(value = "/accounts/{accountId}/documents", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add a document to the specified account.")
    public @ResponseBody ResponseEntity<Document> addDocument(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("accountId") Long accountId, @RequestBody Document doc) {
         Account account = findById(tenantId, accountId);
         account.getDocuments().add(doc);
         account.setLastUpdated(new Date());
         account = accountRepo.save(account);
         doc = account.getDocuments().stream()
                 .reduce((first, second) -> second).orElse(null);

         HttpHeaders headers = new HttpHeaders();
         URI uri = MvcUriComponentsBuilder.fromController(getClass())
                 .path("/accounts/{id}/documents/{docId}")
                 .buildAndExpand(tenantId, account.getId(), doc.getId())
                 .toUri();
         headers.setLocation(uri);

         return new ResponseEntity<Document>(doc, headers, HttpStatus.CREATED);
    }

    /**
     * Add a document to the specified account.
     *
     * <p>This is just a convenience method, see {@link #addDocument(String, Long, Document)}
     * @return
     *
     * @return The document created.
     */
    @PostMapping(value = "/accounts/{accountId}/documents",
            consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                    "application/x-www-form-urlencoded; charset=UTF-8" })
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
    @PostMapping(value = "/accounts/{accountId}/notes", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add a note to the specified account.")
    public @ResponseBody ResponseEntity<Note> addNote(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("accountId") Long accountId, @RequestBody Note note) {
        Account account = findById(tenantId, accountId);
        account.getNotes().add(note);
        account.setLastUpdated(new Date());
        account = accountRepo.save(account);
        note = account.getNotes().stream()
                .sorted(Comparator.comparing(Note::getCreated).reversed())
                .findFirst().orElse(null);

        HttpHeaders headers = new HttpHeaders();
        URI uri = MvcUriComponentsBuilder.fromController(getClass())
                .path("/accounts/{id}/notes/{noteId}")
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
    @PostMapping(value = "/accounts/{accountId}/notes",
            consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                         "application/x-www-form-urlencoded; charset=UTF-8" })
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
    @RequestMapping(value = "/accounts/{id}", method = RequestMethod.DELETE)
    @Operation(summary = "Delete the specified account.")
    public @ResponseBody void delete(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long accountId) {

        List<Contact> contacts = contactRepo.findByAccountId(accountId, tenantId);
        for (Contact contact : contacts) {
            contactRepo.deleteById(contact.getId());
        }

        accountRepo.deleteById(accountId);
    }

    protected List<EntityModel<Account>> addLinks(final String tenantId, final List<Account> list) {
        ArrayList<EntityModel<Account>> entities = new ArrayList<EntityModel<Account>>();
        for (Account account : list) {
            entities.add(addLinks(tenantId, account));
        }
        return entities;
    }

    protected EntityModel<Account> addLinks(final String tenantId, final Account account) {
        return EntityModel.of(account,
                linkTo(methodOn(AccountController.class)
                        .findEntityById(tenantId, account.getId().toString()))
                                .withSelfRel());
    }
}

