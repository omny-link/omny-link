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
import org.springframework.hateoas.EntityModel;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import link.omny.custmgmt.model.Memo;
import link.omny.custmgmt.repositories.MemoRepository;
import link.omny.custmgmt.repositories.MemoSignatoryRepository;
import link.omny.supportservices.exceptions.BusinessEntityNotFoundException;
import link.omny.supportservices.internal.NullAwareBeanUtils;
import link.omny.supportservices.model.Note;
import link.omny.supportservices.repositories.NoteRepository;
import lombok.Data;

/**
 * REST web service for uploading and accessing a file of JSON memos (over
 * and above the CRUD offered by spring data).
 * 
 * @author Tim Stephenson
 */
@Controller
@RequestMapping(value = "/{tenantId}/memos")
public class MemoController {

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
        LOGGER.info("Uploading memos for: {}", tenantId);
        String content = new String(file.getBytes());

        List<Memo> list = objectMapper.readValue(content,
                new TypeReference<List<Memo>>() {
                });
        LOGGER.info("  found {} memos", list.size());
        for (Memo message : list) {
            message.setTenantId(tenantId);
        }

        Iterable<Memo> result = memoRepo.saveAll(list);
        LOGGER.info("  saved.");
        return result;
    }
    
    /**
     * Create a new memo.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ResponseStatus(value = HttpStatus.CREATED)
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<EntityModel<Memo>> create(
            @PathVariable("tenantId") String tenantId,
            @RequestBody Memo memo) {
        memo.setTenantId(tenantId);

        EntityModel<Memo> entity = addLinks(tenantId, memoRepo.save(memo));
        LOGGER.debug("Created memo {}", entity.getLink("self"));

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(entity.getLink("self").get().toUri());

        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    /**
     * Return just the memos for a specific tenant.
     * 
     * @return memos for that tenant.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public @ResponseBody List<EntityModel<Memo>> listForTenant(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info("List memos for tenant {}", tenantId);
        return addLinks(tenantId, listAsCsv(tenantId, page, limit));
    }

    /**
     * @return memos for the specified tenant and status.
     */
    @RequestMapping(value = "/findByStatus/{status}", method = RequestMethod.GET)
    public @ResponseBody List<EntityModel<Memo>> findByStatusForTenant(
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
            Pageable pageable = PageRequest.of(page == null ? 0 : page, limit);
            list = memoRepo.findPageByStatusForTenant(
                    status.toLowerCase(), tenantId, pageable);
        }
        LOGGER.info("Found {} memos", list.size());
        return addLinks(tenantId, list);
    }

    protected Memo findById(final String tenantId, final Long id) {
        return memoRepo.findById(id)
                .orElseThrow(() -> new BusinessEntityNotFoundException(
                        Memo.class, id));
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
    public @ResponseBody Memo findById(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("idOrName") String idOrName) {
        LOGGER.debug("Find memo {}", idOrName);

        Memo memo;
        try {
            memo = findById(tenantId, Long.parseLong(idOrName));
        } catch (NumberFormatException e) {
            memo = memoRepo.findByName(idOrName, tenantId);
        }
        if (memo == null) {
            LOGGER.error("Unable to find memo from {}", idOrName);
            throw new BusinessEntityNotFoundException(Memo.class, idOrName);
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
    public @ResponseBody EntityModel<Memo> clone(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("idOrName") String idOrName) {
        LOGGER.debug("Clone memo {}", idOrName);

        Memo memo;
        try {
            memo = findById(tenantId, Long.parseLong(idOrName));
        } catch (NumberFormatException e) {
            memo = memoRepo.findByName(idOrName, tenantId);
        }
        if (memo == null) {
            LOGGER.error("Unable to find memo from {}", idOrName);
            throw new BusinessEntityNotFoundException(Memo.class, idOrName);
        }
        Memo resource = new Memo();
        BeanUtils.copyProperties(memo, resource, "id");
        resource.setName(memo.getName() + "Copy");
        resource.setStatus("Draft");
        memoRepo.save(resource);
        return addLinks(tenantId, resource);
    }

    /**
     * @return Export all memos for the specified tenant as CSV.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = { "text/csv" })
    public @ResponseBody List<Memo> listAsCsv(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info("Export memos for tenant {}", tenantId);

        List<Memo> list;
        if (limit == null) {
            list = memoRepo.findAllForTenant(tenantId);
        } else {
            Pageable pageable = PageRequest.of(page == null ? 0 : page, limit);
            list = memoRepo.findPageForTenant(tenantId, pageable);
        }
        LOGGER.info("Found {} memos", list.size());
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
        Memo memo = findById(tenantId, memoId);
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
        memoRepo.deleteById(memoId);
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
        Memo message = findById(tenantId, messageId);
        // note.setMessage(message);
        noteRepo.save(note);
        // necessary to force a save
        message.setLastUpdated(new Date());
        memoRepo.save(message);
        // Similarly cannot return object until solve Jackson object cycle
        // return note;
    }

    @Data
    public static class ShortMemo {
        private String selfRef;
        private String name;
        private String title;
        private String status;
        private String owner;
        private String requiredVars;
        private Date created;
        private Date lastUpdated;
      }
    
    protected List<EntityModel<Memo>> addLinks(final String tenantId, final List<Memo> list) {
        ArrayList<EntityModel<Memo>> entities = new ArrayList<EntityModel<Memo>>();
        for (Memo memo : list) {
            entities.add(addLinks(tenantId, memo));
        }
        return entities;
    }

    protected EntityModel<Memo> addLinks(final String tenantId, final Memo memo) {
        return EntityModel.of(memo,
                linkTo(methodOn(MemoController.class).findById(tenantId, memo.getId()))
                        .withSelfRel());
    }
}
