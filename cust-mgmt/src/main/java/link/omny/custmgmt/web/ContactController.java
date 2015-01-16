package link.omny.custmgmt.web;

import java.util.List;

import link.omny.custmgmt.model.Contact;
import link.omny.custmgmt.repositories.ContactRepository;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST web service for uploading and accessing a file of JSON Contacts (over
 * and above the CRUD offererd by spring data).
 * 
 * /models/upload?file={file} Add a model POST file: A file posted in a
 * multi-part request
 * 
 * @author Tim Stephenson
 */
@Controller
@RequestMapping(value = "/models")
public class ContactController {

    private static final Logger LOGGER = Logger
            .getLogger(ContactController.class);

    @Autowired
    private ContactRepository repo;

    /**
     * Adds a model to the repository.
     * 
     * Url: /models/upload?file={file} [POST]
     * 
     * @param file
     *            A file posted in a multi-part request
     * @return The meta data of the added model
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public @ResponseBody List<Contact> handleFileUpload(
            @RequestParam(value = "file", required = true) MultipartFile file) {

        try {
            String content = new String(file.getBytes());

            // TODO deserialise JsoN resource
            // repo.save();
            return null;
        } catch (RuntimeException e) {
            LOGGER.error("Error while uploading.", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error while uploading.", e);
            throw new RuntimeException(e);
        }
    }
}
