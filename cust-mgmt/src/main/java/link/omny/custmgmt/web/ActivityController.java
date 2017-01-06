package link.omny.custmgmt.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import link.omny.custmgmt.model.Activity;
import link.omny.custmgmt.repositories.ActivityRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
import com.knowprocess.bpmn.BusinessEntityNotFoundException;

/**
 * REST web service for uploading and accessing a file of JSON Activities (over
 * and above the CRUD offered by spring data).
 * 
 * @author Tim Stephenson
 */
@Controller
@RequestMapping(value = "/{tenantId}/activities")
public class ActivityController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ActivityController.class);

    @Autowired
    private ActivityRepository repo;

    @Autowired
    private ObjectMapper objectMapper;

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
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public @ResponseBody Iterable<Activity> handleFileUpload(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "file", required = true) MultipartFile file)
            throws IOException {
        LOGGER.info(String.format("Uploading activities for: %1$s", tenantId));
        String content = new String(file.getBytes());

        List<Activity> list = objectMapper.readValue(content,
                new TypeReference<List<Activity>>() {
                });
        LOGGER.info(String.format("  found %1$d activities", list.size()));

        Iterable<Activity> result = repo.save(list);
        LOGGER.info("  saved.");

        return result;
    }

    /**
     * Return all activities associated with a contact.
     * 
     * @return contacts matching that contact.
     * @throws BusinessEntityNotFoundException
     */
    @RequestMapping(value = "/findByContactId/{contactId}", method = RequestMethod.GET)
    @Transactional
    public @ResponseBody List<ShortActivity> findByContactId(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") String contactId)
            throws BusinessEntityNotFoundException {
        LOGGER.debug(String.format("Find activities for contact %1$s",
                contactId));

        return wrapShort(repo.findByContactId(Long.parseLong(contactId)));
    }

    private List<ShortActivity> wrapShort(List<Activity> list) {
        List<ShortActivity> resources = new ArrayList<ShortActivity>(
                list.size());
        for (Activity activity : list) {
            resources.add(wrap(activity));
        }
        return resources;
    }

    private ShortActivity wrap(Activity activity) {
        ShortActivity resource = new ShortActivity();
        BeanUtils.copyProperties(activity, resource);
        Link detail = linkTo(ActivityRepository.class, activity.getId())
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
    public static class ShortActivity extends ResourceSupport {
        private String selfRef;
        private String type;
        private Date occurred;
        private String content;
    }
}

