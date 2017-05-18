package link.omny.custmgmt.web;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import link.omny.custmgmt.internal.ContactAvatarService;
import link.omny.custmgmt.model.Contact;
import link.omny.custmgmt.repositories.ContactRepository;

@Controller
@RequestMapping(value = "/{tenantId}/gravatars")
public class GravatarController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GravatarController.class);

    @Autowired
    private ContactRepository contactRepo;

    @Autowired
    private ContactAvatarService avatarSvc;

    /**
     * Return a gravatar based on contact's initials if known email hash or
     * nothing.
     */
    @RequestMapping(value = "/{emailHash}.png", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody void getGravatar(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("emailHash") String emailHash,
            HttpServletResponse response) {

        List<Contact> contacts = contactRepo.findByEmailHash(emailHash,
                tenantId);

        try {
            response.setContentType(MediaType.IMAGE_PNG_VALUE);
            if (contacts == null || contacts.size() < 1) {
                LOGGER.warn(String.format(
                        "Unable to find contact for email hash %1$s, displaying N/A",
                        emailHash));
                avatarSvc.create("N/A",
                        response.getOutputStream());
            } else {
                avatarSvc.create(contacts.get(0).initials(),
                        response.getOutputStream());
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Unable to create avatar for %1$s",
                    emailHash), e);
        }
    }
}
