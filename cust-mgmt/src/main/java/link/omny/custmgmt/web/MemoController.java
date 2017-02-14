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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowprocess.bpmn.BusinessEntityNotFoundException;

import link.omny.custmgmt.internal.NullAwareBeanUtils;
import link.omny.custmgmt.model.Memo;
import link.omny.custmgmt.model.Note;
import link.omny.custmgmt.repositories.MemoRepository;
import link.omny.custmgmt.repositories.NoteRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * REST web service for uploading and accessing a file of JSON Messages (over
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
    private NoteRepository noteRepo;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Imports JSON representation of messages.
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
        LOGGER.info(String.format("Uploading messages for: %1$s", tenantId));
        String content = new String(file.getBytes());

        List<Memo> list = objectMapper.readValue(content,
                new TypeReference<List<Memo>>() {
                });
        LOGGER.info(String.format("  found %1$d messages", list.size()));
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
     * Return just the messages for a specific tenant.
     * 
     * @return messages for that tenant.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public @ResponseBody List<ResourceSupport> listForTenant(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info(String.format("List messages for tenant %1$s", tenantId));

        List<Memo> list;
        if (limit == null) {
            list = memoRepo.findAllForTenant(tenantId);
        } else {
            Pageable pageable = new PageRequest(page == null ? 0 : page, limit);
            list = memoRepo.findPageForTenant(tenantId, pageable);
        }
        LOGGER.info(String.format("Found %1$s messages", list.size()));

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
    @Transactional
    public @ResponseBody ResourceSupport findById(
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
        return wrap(memo, new MemoResource());
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
    public @ResponseBody ResourceSupport clone(
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
        return wrap(resource, new MemoResource());
    }

    /**
     * @return Export all memos for the specified tenant as CSV.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "text/csv")
    public @ResponseBody List<Memo> exportAsCsv(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info(String.format("Export messages for tenant %1$s", tenantId));

        List<Memo> list;
        if (limit == null) {
            list = memoRepo.findAllForTenant(tenantId);
        } else {
            Pageable pageable = new PageRequest(page == null ? 0 : page, limit);
            list = memoRepo.findPageForTenant(tenantId, pageable);
        }
        LOGGER.info(String.format("Found %1$s messages", list.size()));

        return list;
    }

    /**
     * Update an existing memo.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { "application/json" })
    public @ResponseBody void update(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long memoId,
            @RequestBody Memo updatedMemo) {
        Memo memo = memoRepo.findOne(memoId);

        NullAwareBeanUtils.copyNonNullProperties(updatedMemo, memo, "id");
        memo.setTenantId(tenantId);
        memoRepo.save(memo);
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
        Link detail = linkTo(MemoRepository.class, message.getId())
                .withSelfRel();
        resource.add(detail);
        try {
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

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class MemoResource extends ResourceSupport {
        private String selfRef;
        private String name;
        private String title;
        private String plainContent;
        private String richContent;
        private String shortContent;
        private String status;
        private String owner;
        private String requiredVars;
        private Date created;
        private Date lastUpdated;
    }
}
