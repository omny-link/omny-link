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

import link.omny.custmgmt.internal.DateUtils;
import link.omny.custmgmt.model.Account;
import link.omny.custmgmt.model.Activity;
import link.omny.custmgmt.model.Contact;
import link.omny.custmgmt.model.CustomAccountField;
import link.omny.custmgmt.model.Document;
import link.omny.custmgmt.model.Note;
import link.omny.custmgmt.model.views.AccountViews;
import link.omny.custmgmt.repositories.AccountRepository;
import link.omny.custmgmt.repositories.ContactRepository;
import link.omny.supportservices.exceptions.BusinessEntityNotFoundException;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * REST web service for uploading and accessing a file of JSON Accounts (over
 * and above the CRUD offered by spring data).
 *
 * @author Tim Stephenson
 */
@Controller
@RequestMapping(value = "/{tenantId}")
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
    @RequestMapping(value = "/accounts/{accountId}/activities", method = RequestMethod.POST)
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
    @RequestMapping(value = "/accounts/{accountId}/activities", method = RequestMethod.POST, consumes = "application/x-www-form-urlencoded")
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
    @RequestMapping(value = "/accounts/upload", method = RequestMethod.POST)
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

    @RequestMapping(value = "/accounts/archive", method = RequestMethod.POST, headers = "Accept=application/json")
    @Transactional
    public @ResponseBody Integer archiveAccounts(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "before", required = false) String before) {
        Date beforeDate = before == null 
                ? DateUtils.oneMonthAgo() : DateUtils.parseDate(before);
        LOGGER.info("Place on hold accounts of {} older than {}",
                        tenantId, beforeDate.toString());

        return accountRepo.updateStage("On hold", beforeDate, tenantId);
    }

    /**
     * @return Accounts for a specific tenant.
     */
    @RequestMapping(value = "/accounts/", method = RequestMethod.GET)
//  @JsonView(AccountViews.Summary.class)
//  public @ResponseBody List<Account> listForTenantAsJson(
    public @ResponseBody List<ShortAccount> listForTenantAsJson(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return wrap(listForTenant(tenantId, page, limit));
    }

    protected List<Account> listForTenant(
            String tenantId, Integer page, Integer limit) {
        LOGGER.info(String.format("List accounts for tenant %1$s", tenantId));

        List<Account> list;
        if (limit == null) {
            list = accountRepo.findAllForTenant(tenantId);
        } else {
            Pageable pageable = new PageRequest(page == null ? 0 : page, limit);
            list = accountRepo.findPageForTenant(tenantId, pageable);
        }
        LOGGER.info(String.format("Found %1$s accounts", list.size()));

        return list;
    }

    /**
     * Return just the accounts for a specific tenant.
     *
     * @return accounts for that tenant.
     */
    @RequestMapping(value = "/accounts/", method = RequestMethod.GET, produces = "text/csv")
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

        for (Account account : listForTenant(tenantId, page, limit)) {
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
    @RequestMapping(value = "/account-pairs/", method = RequestMethod.GET)
    @JsonView(AccountViews.Pair.class)
    public @ResponseBody List<Account> listPairForTenant(
            @PathVariable("tenantId") String tenantId) {
        LOGGER.info(String.format("List account id-name pairs for tenant %1$s", tenantId));

        List<Account> list = accountRepo.findAllForTenant(tenantId);
        LOGGER.info(String.format("Found %1$s accounts", list.size()));

        return list;
    }

    /**
     * Create a new account.
     *
     * @return if successful: status = 201 and Location header
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ResponseStatus(value = HttpStatus.CREATED)
    @RequestMapping(value = "/accounts/", method = RequestMethod.POST)
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
        headers.setLocation(builder2.path("/accounts/{id}")
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
    @RequestMapping(value = "/accounts/{id}", method = RequestMethod.GET)
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
     * Return just the matching account.
     *
     * @return the account with this name.
     * @throws BusinessEntityNotFoundException
     */
    @JsonView(AccountViews.Detailed.class)
    @RequestMapping(value = "/accounts/findByName/{name}", method = RequestMethod.GET)
    public @ResponseBody Account findByName(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("name") String name)
            throws BusinessEntityNotFoundException {
        LOGGER.debug(String.format("Find account with name %1$s", name));

        Account account = accountRepo.findByNameForTenant(name, tenantId);
        if (account == null) {
            throw new BusinessEntityNotFoundException("account", name);
        }
        account.getActivities(); // force load

        return addLinks(tenantId, account);
    }

    /**
     * Update an existing account.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/accounts/{id}", method = RequestMethod.PUT, consumes = "application/json")
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
    @RequestMapping(value = "/accounts/{id}/customFields/", method = RequestMethod.POST, consumes = "application/json")
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
    @RequestMapping(value = "/accounts/{accountId}/documents", method = RequestMethod.PUT)
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
                 .path("/{id}/documents/{docId}")
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
    @RequestMapping(value = "/accounts/{accountId}/documents", method = RequestMethod.POST)
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
    @RequestMapping(value = "/accounts/{accountId}/notes", method = RequestMethod.PUT)
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
    @RequestMapping(value = "/accounts/{accountId}/notes", method = RequestMethod.POST)
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

