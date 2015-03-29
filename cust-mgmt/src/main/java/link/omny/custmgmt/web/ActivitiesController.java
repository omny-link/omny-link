package link.omny.custmgmt.web;

import java.io.IOException;
import java.util.List;

import link.omny.custmgmt.model.Activity;
import link.omny.custmgmt.repositories.ActivityRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * REST web service for uploading and accessing a file of JSON Activities (over
 * and above the CRUD offered by spring data).
 * 
 * @author Tim Stephenson
 */
@Controller
@RequestMapping(value = "/{tenantId}/activities")
public class ActivitiesController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ActivitiesController.class);

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
}
