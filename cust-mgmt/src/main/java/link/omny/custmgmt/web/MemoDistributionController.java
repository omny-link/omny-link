/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import link.omny.custmgmt.model.Contact;
import link.omny.custmgmt.model.MemoDistribution;
import link.omny.custmgmt.model.Note;
import link.omny.custmgmt.repositories.ContactRepository;
import link.omny.custmgmt.repositories.MemoDistributionRepository;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * REST web service for uploading and accessing a file of JSON Mailshots (over
 * and above the CRUD offered by spring data).
 * 
 * @author Tim Stephenson
 */
@Controller
@RequestMapping(value = "/{tenantId}/memo-distributions")
public class MemoDistributionController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(MemoDistributionController.class);

    @Autowired
    private MemoDistributionRepository mailshotRepo;

    @Autowired
    private ContactRepository contactRepo;

    @Autowired
    private NoteRepository noteRepo;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Imports JSON representation of mailshots.
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
    public @ResponseBody Iterable<MemoDistribution> handleFileUpload(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "file", required = true) MultipartFile file)
            throws IOException {
        LOGGER.info(String.format("Uploading mailshots for: %1$s", tenantId));
        String content = new String(file.getBytes());

        List<MemoDistribution> list = objectMapper.readValue(content,
                new TypeReference<List<MemoDistribution>>() {
                });
        LOGGER.info(String.format("  found %1$d mailshots", list.size()));
        for (MemoDistribution mailshot : list) {
            mailshot.setTenantId(tenantId);
        }

        Iterable<MemoDistribution> result = mailshotRepo.save(list);
        LOGGER.info("  saved.");
        return result;
    }

    /**
     * Return just the mailshots for a specific tenant.
     * 
     * @return mailshots for that tenant.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public @ResponseBody List<ShortMemoDistribution> listForTenant(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info(String.format("List mailshots for tenant %1$s", tenantId));

        List<MemoDistribution> list;
        if (limit == null) {
            list = mailshotRepo.findAllForTenant(tenantId);
        } else {
            Pageable pageable = new PageRequest(page == null ? 0 : page, limit);
            list = mailshotRepo.findPageForTenant(tenantId, pageable);
        }
        LOGGER.info(String.format("Found %1$s mailshots", list.size()));

        return wrap(list);
    }

    /**
     * @return the distribution with any tags recipients expanded.
     */
    @RequestMapping(value = "/{distributionId}/expandedTags", method = RequestMethod.GET)
    public @ResponseBody MemoDistribution getExpandedDistribution(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("distributionId") Long distributionId) {
        LOGGER.info(String.format("getExpandedDistribution %1$s for tenant %2$s", distributionId, tenantId));
        
        MemoDistribution dist = mailshotRepo.findOne(distributionId);
        for (String tag : dist.getRecipientTagList()) {
            List<Contact> list = contactRepo.findByTag("%" + tag + "%",
                    tenantId);
            LOGGER.info(String.format("Found %1$s contact(s) for tag %2$s",
                    list.size(), tag));
            dist.addRecipients(list);
            dist.removeRecipient(tag);
        }
        
        return dist;
    }

    /**
     * Export all mailshots for the tenant.
     * 
     * @return mailshots for that tenant.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "text/csv")
    public @ResponseBody List<MemoDistribution> exportAsCsv(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info(String.format("Export mailshots for tenant %1$s", tenantId));

        List<MemoDistribution> list;
        if (limit == null) {
            list = mailshotRepo.findAllForTenant(tenantId);
        } else {
            Pageable pageable = new PageRequest(page == null ? 0 : page, limit);
            list = mailshotRepo.findPageForTenant(tenantId, pageable);
        }
        LOGGER.info(String.format("Found %1$s mailshots", list.size()));

        return list;
    }

    /**
     * Add a note to the specified mailshot.
     */
    @RequestMapping(value = "/{mailshotId}/notes", method = RequestMethod.POST)
    public @ResponseBody void addNote(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("mailshotId") Long mailshotId,
            @RequestParam("author") String author,
            @RequestParam("content") String content) {
        addNote(tenantId, mailshotId, new Note(author, content));
    }

    /**
     * Add a note to the specified mailshot.
     */
    // TODO Jackson cannot deserialise document because of mailshot reference
    // @RequestMapping(value = "/{mailshotId}/notes", method =
    // RequestMethod.PUT)
    public @ResponseBody void addNote(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("mailshotId") Long mailshotId, @RequestBody Note note) {
        MemoDistribution mailshot = mailshotRepo.findOne(mailshotId);
        // note.setMailshot(mailshot);
        noteRepo.save(note);
        // necessary to force a save
        mailshot.setLastUpdated(new Date());
        mailshotRepo.save(mailshot);
        // Similarly cannot return object until solve Jackson object cycle
        // return note;
    }

    private List<ShortMemoDistribution> wrap(List<MemoDistribution> list) {
        List<ShortMemoDistribution> resources = new ArrayList<ShortMemoDistribution>(
                list.size());
        for (MemoDistribution mailshot : list) {
            resources.add(wrap(mailshot));
        }
        return resources;
    }

    private ShortMemoDistribution wrap(MemoDistribution mailshot) {
        ShortMemoDistribution resource = new ShortMemoDistribution();
        BeanUtils.copyProperties(mailshot, resource);
        Link detail = linkTo(MemoDistributionRepository.class, mailshot.getId())
                .withSelfRel();
        resource.add(detail);
        resource.setSelfRef(detail.getHref());
        return resource;
    }
    
    
    private Link linkTo(Class<? extends CrudRepository> clazz, Long id) {
        return new Link(clazz.getAnnotation(RepositoryRestResource.class)
                .path() + "/" + id);
    }

    @Data
    public static class ShortMemoDistribution extends ResourceSupport {
        private String selfRef;
        private String name;
        private String status;
        private String owner;
        private Date created;
        private Date lastUpdated;
      }
}
