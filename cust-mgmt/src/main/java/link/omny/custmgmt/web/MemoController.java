package link.omny.custmgmt.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import link.omny.custmgmt.model.Memo;
import link.omny.custmgmt.model.Note;
import link.omny.custmgmt.repositories.MemoRepository;
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
 * REST web service for uploading and accessing a file of JSON Messages (over
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
    private MemoRepository messageRepo;

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

        Iterable<Memo> result = messageRepo.save(list);
        LOGGER.info("  saved.");
        return result;
    }

    /**
     * Return just the messages for a specific tenant.
     * 
     * @return messages for that tenant.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public @ResponseBody List<ShortMemo> listForTenant(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info(String.format("List messages for tenant %1$s", tenantId));

        List<Memo> list;
        if (limit == null) {
            list = messageRepo.findAllForTenant(tenantId);
        } else {
            Pageable pageable = new PageRequest(page == null ? 0 : page, limit);
            list = messageRepo.findPageForTenant(tenantId, pageable);
        }
        LOGGER.info(String.format("Found %1$s messages", list.size()));

        return wrap(list);
    }

    /**
     * Export all contacts for the tenant.
     * 
     * @return contacts for that tenant.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "text/csv")
    public @ResponseBody List<Memo> exportAsCsv(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info(String.format("Export messages for tenant %1$s", tenantId));

        List<Memo> list;
        if (limit == null) {
            list = messageRepo.findAllForTenant(tenantId);
        } else {
            Pageable pageable = new PageRequest(page == null ? 0 : page, limit);
            list = messageRepo.findPageForTenant(tenantId, pageable);
        }
        LOGGER.info(String.format("Found %1$s messages", list.size()));

        return list;
    }

    /**
     * Add a note to the specified message.
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
        Memo message = messageRepo.findOne(messageId);
        // note.setMessage(message);
        noteRepo.save(note);
        // necessary to force a save
        message.setLastUpdated(new Date());
        messageRepo.save(message);
        // Similarly cannot return object until solve Jackson object cycle
        // return note;
    }

    private List<ShortMemo> wrap(List<Memo> list) {
        List<ShortMemo> resources = new ArrayList<ShortMemo>(list.size());
        for (Memo message : list) {
            resources.add(wrap(message));
        }
        return resources;
    }

    private ShortMemo wrap(Memo message) {
        ShortMemo resource = new ShortMemo();
        BeanUtils.copyProperties(message, resource);
        Link detail = linkTo(MemoRepository.class, message.getId())
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
    public static class ShortMemo extends ResourceSupport {
        private String selfRef;
        private String name;
        private String title;
        private String status;
        private String owner;
        private Date created;
        private Date lastUpdated;
      }
}
