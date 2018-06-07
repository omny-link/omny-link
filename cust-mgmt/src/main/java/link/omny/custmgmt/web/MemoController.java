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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import link.omny.custmgmt.internal.NullAwareBeanUtils;
import link.omny.custmgmt.model.Memo;
import link.omny.custmgmt.model.Note;
import link.omny.custmgmt.model.views.MemoViews;
import link.omny.custmgmt.repositories.MemoRepository;
import link.omny.custmgmt.repositories.MemoSignatoryRepository;
import link.omny.custmgmt.repositories.NoteRepository;
import link.omny.supportservices.exceptions.BusinessEntityNotFoundException;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * REST web service for uploading and accessing a file of JSON memos (over
 * and above the CRUD offered by spring data).
 * 
 * @author Tim Stephenson
 */
@Controller
@RequestMapping(value = "/{tenantId}/memos")
public class MemoController extends BaseTenantAwareController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(MemoController.class);

    @Autowired
    private MemoRepository memoRepo;

    @Autowired
    private MemoSignatoryRepository memoSignatoryRepo;

    @Autowired
    private NoteRepository noteRepo;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Imports JSON representation of memos.
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
    public @ResponseBody Iterable<Memo> handleFileUpload(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "file", required = true) MultipartFile file)
            throws IOException {
        LOGGER.info(String.format("Uploading memos for: %1$s", tenantId));
        String content = new String(file.getBytes());

        List<Memo> list = objectMapper.readValue(content,
                new TypeReference<List<Memo>>() {
                });
        LOGGER.info(String.format("  found %1$d memos", list.size()));
        for (Memo message : list) {
            message.setTenantId(tenantId);
        }

        Iterable<Memo> result = memoRepo.save(list);
        LOGGER.info("  saved.");
        return result;
    }
    
    /**
     * Create a new memo.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ResponseStatus(value = HttpStatus.CREATED)
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<?> create(
            @PathVariable("tenantId") String tenantId,
            @RequestBody Memo memo) {
        memo.setTenantId(tenantId);

        memo = memoRepo.save(memo);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(getGlobalUri(memo.getId()));

        // TODO migrate to http://host/tenant/contacts/id
        // headers.setLocation(getTenantBasedUri(tenantId, contact));

        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    /**
     * Return just the memos for a specific tenant.
     * 
     * @return memos for that tenant.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public @ResponseBody List<ResourceSupport> listForTenant(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info(String.format("List memos for tenant %1$s", tenantId));
        return wrap(listAsCsv(tenantId, page, limit));
    }

    /**
     * @return memos for the specified tenant and status.
     */
    @RequestMapping(value = "/findByStatus/{status}", method = RequestMethod.GET)
    public @ResponseBody List<ResourceSupport> findByStatusForTenant(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("status") String status,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info("List memos with status {} for tenant {}", status, tenantId);

        List<Memo> list;
        if (limit == null) {
            list = memoRepo.findByStatusForTenant(status.toLowerCase(),
                    tenantId);
        } else {
            Pageable pageable = new PageRequest(page == null ? 0 : page, limit);
            list = memoRepo.findPageByStatusForTenant(
                    status.toLowerCase(), tenantId, pageable);
        }
        LOGGER.info("Found {} memos", list.size());

        return wrap(list);
    }

    /**
     * Return just the matching memo.
     * 
     * @param idOrName
     *            If a number will be assumed to be the id, otherwise the name.
     * @return memo for that tenant with the matching name or id.
     * @throws BusinessEntityNotFoundException
     */
    @RequestMapping(value = "/{idOrName}", method = RequestMethod.GET)
//    @Transactional
    @JsonView(MemoViews.Detailed.class)
    public @ResponseBody Memo findById(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("idOrName") String idOrName)
            throws BusinessEntityNotFoundException {
        LOGGER.debug(String.format("Find memo %1$s", idOrName));

        Memo memo;
        try {
            memo = memoRepo.findOne(Long.parseLong(idOrName));
        } catch (NumberFormatException e) {
            memo = memoRepo.findByName(idOrName, tenantId);
        }
        if (memo == null) {
            LOGGER.error(String.format("Unable to find memo from %1$s",
                    idOrName));
            throw new BusinessEntityNotFoundException("Memo", idOrName);
        }
        return memo;
//        return wrap(memo, new MemoResource());
    }

    /**
     * Clone the specified memo.
     * 
     * @param idOrName
     *            If a number will be assumed to be the id, otherwise the name.
     * @return memo for that tenant with the matching name or id.
     * @throws BusinessEntityNotFoundException
     */
    @RequestMapping(value = "/{idOrName}/clone", method = RequestMethod.POST)
    @Transactional
    @JsonView(MemoViews.Detailed.class)
    public @ResponseBody Memo clone(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("idOrName") String idOrName)
            throws BusinessEntityNotFoundException {
        LOGGER.debug(String.format("Clone memo %1$s", idOrName));

        Memo memo;
        try {
            memo = memoRepo.findOne(Long.parseLong(idOrName));
        } catch (NumberFormatException e) {
            memo = memoRepo.findByName(idOrName, tenantId);
        }
        if (memo == null) {
            LOGGER.error(String.format("Unable to find memo from %1$s",
                    idOrName));
            throw new BusinessEntityNotFoundException("Memo", idOrName);
        }
        Memo resource = new Memo();
        BeanUtils.copyProperties(memo, resource, "id");
        resource.setName(memo.getName() + "Copy");
        memoRepo.save(resource);
        addLinks(tenantId, resource);
        return resource;
//        return wrap(resource, new MemoResource());
    }

    /**
     * @return Export all memos for the specified tenant as CSV.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = { "text/csv" })
    public @ResponseBody List<Memo> listAsCsv(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info(String.format("Export memos for tenant %1$s", tenantId));

        List<Memo> list;
        if (limit == null) {
            list = memoRepo.findAllForTenant(tenantId);
        } else {
            Pageable pageable = new PageRequest(page == null ? 0 : page, limit);
            list = memoRepo.findPageForTenant(tenantId, pageable);
        }
        LOGGER.info(String.format("Found %1$s memos", list.size()));

        return list;
    }

    /**
     * Update an existing memo.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { "application/json" })
    @Transactional
    public @ResponseBody void update(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long memoId,
            @RequestBody Memo updatedMemo) {
        memoSignatoryRepo.deleteAllForMemo(updatedMemo.getId());
        Memo memo = memoRepo.findOne(memoId);
        NullAwareBeanUtils.copyNonNullProperties(updatedMemo, memo, "id", "signatories");
        memo.addAllSignatories(updatedMemo.getSignatories());
        memo.setTenantId(tenantId);
        memo = memoRepo.save(memo);
    }

    /**
     * Delete an existing memo.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public @ResponseBody void delete(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long memoId) {
        memoRepo.delete(memoId);
    }

    /**
     * Add a note to the specified memo.
     */
    @RequestMapping(value = "/{messageId}/notes", method = RequestMethod.POST)
    public @ResponseBody void addNote(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("messageId") Long messageId,
            @RequestParam("author") String author,
            @RequestParam("content") String content) {
        addNote(tenantId, messageId, new Note(author, content));
    }

    /**
     * Add a note to the specified message.
     */
    // TODO Jackson cannot deserialise document because of message reference
    // @RequestMapping(value = "/{messageId}/notes", method = RequestMethod.PUT)
    public @ResponseBody void addNote(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("messageId") Long messageId, @RequestBody Note note) {
        Memo message = memoRepo.findOne(messageId);
        // note.setMessage(message);
        noteRepo.save(note);
        // necessary to force a save
        message.setLastUpdated(new Date());
        memoRepo.save(message);
        // Similarly cannot return object until solve Jackson object cycle
        // return note;
    }

    private List<ResourceSupport> wrap(List<Memo> list) {
        List<ResourceSupport> resources = new ArrayList<ResourceSupport>(
                list.size());
        for (Memo message : list) {
            resources.add(wrap(message, new ShortMemo()));
        }
        return resources;
    }

    private ResourceSupport wrap(Memo message, ResourceSupport resource) {
        BeanUtils.copyProperties(message, resource);
        try {
            Link detail = linkTo(MemoRepository.class, message.getId())
                    .withSelfRel();
            resource.add(detail);
            Method method = resource.getClass().getMethod("setSelfRef", String.class);
            method.invoke(resource, detail.getHref());
        } catch (NoSuchMethodException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            LOGGER.error("Unable to set self reference.", e);
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
    @EqualsAndHashCode(callSuper = true)
    public static class ShortMemo extends ResourceSupport {
        private String selfRef;
        private String name;
        private String title;
        private String status;
        private String owner;
        private String requiredVars;
        private Date created;
        private Date lastUpdated;
      }

    private void addLinks(String tenantId, Memo memo) {
        List<Link> links = new ArrayList<Link>();
        links.add(new Link(String.format("/%1$s/memos/%2$s",
                tenantId, memo.getId())));
        memo.setLinks(links);
    }
}
