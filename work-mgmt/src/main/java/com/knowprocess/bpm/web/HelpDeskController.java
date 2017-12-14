package com.knowprocess.bpm.web;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowprocess.bpm.api.UnsupportedBpmnException;
import com.knowprocess.resource.internal.gdrive.GDriveRepository;

@Controller
@RequestMapping("/{tenantId}/helpdesk")
public class HelpDeskController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(HelpDeskController.class);

    @Value("${kp.application.root-user:tim@knowprocess.com}")
    protected String rootUser;

    @Value("${kp.ticket.msg-name:omny.ticket}")
    protected String ticketMsgName;

    @Value("${kp.ticket.subject:Support Ticket}")
    protected String ticketSubject;

    @Autowired
    protected ProcessEngine processEngine;

    @Autowired
    protected ObjectMapper objectMapper;

    protected DateFormat isoFormatter = new SimpleDateFormat(
            "yyyy-MM-dd'T'hh:mm");

    @Autowired
    private Message2Controller msgController;

    @RequestMapping(method = RequestMethod.POST)
    public final @ResponseBody ResponseEntity<String> uploadMultipleFiles(
            UriComponentsBuilder uriBuilder,
            HttpServletRequest request,
            @PathVariable("tenantId") String tenantId,
            @RequestBody String context)
            throws UnsupportedEncodingException, IOException,
            UnsupportedBpmnException {

        Date whenOccurred = new Date();
        String username = request.getUserPrincipal().getName();
        String bizDesc = String.format("ticket-%1$s-%2$s", username,
                isoFormatter.format(whenOccurred));
        String filename = bizDesc + ".png";
        User user = processEngine.getIdentityService().createUserQuery()
                .userId(username).singleResult();
        JsonNode json = objectMapper.readTree(context);

        // image will be URL encoded, strip "data:image/png;base64,"
        String s = json.get("image").asText();
        String url = uploadImage(whenOccurred, filename,
                Base64.getDecoder().decode(s.substring(s.indexOf(",") + 1).getBytes()));

        String ticketMsg = String.format("%1$s. Raised on page: %2$s, screenshot: %3$s",
                json.get("message").asText(), request.getHeader("Referer"), url);

        String ticketMsgPayload = String.format(getMessageTemplate(),
                user.getFirstName(), user.getLastName(), user.getEmail(),
                rootUser, ticketSubject, ticketMsg,
                request.getHeader("Referer"), filename, url, tenantId);

        return msgController.handleMessageStart(
                tenantId, ticketMsgName, bizDesc, ticketMsgPayload);
    }

    private void writeTmpImage(String filename, byte[] bytes) {
        File file = new File(System.getProperty("java.io.tmpdir"), filename);
        FileOutputStream fileOuputStream;
        try {
            fileOuputStream = new FileOutputStream(file);
            fileOuputStream.write(bytes);
            fileOuputStream.close();
            LOGGER.debug("Tmp image written to: {}", file.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    protected String uploadImage(Date whenOccurred, String filename, byte[] image)
            throws IOException {
        if (LOGGER.isDebugEnabled()) {
            writeTmpImage(filename, image);
        }
        GDriveRepository gdriveRepo = new GDriveRepository();
        gdriveRepo.write(filename, MediaType.IMAGE_PNG_VALUE, whenOccurred,
                new ByteArrayInputStream(image));
        String url = gdriveRepo.getDriveUrl();
        LOGGER.info("Uploaded image to {}", url);
        return url;
    }

    protected String getMessageTemplate() {
        return "{"
              + "\"firstName\":\"%1$s\","
              + "\"lastName\":\"%2$s\","
              + "\"email\":\"%3$s\","
              + "\"owner\":\"%4$s\","
              + "\"type\":\"%5$s\","
              + "\"message\":\"%6$s\","
              + "\"page\":\"%7$s\","
              + "\"image\":\"%8$s\","
              + "\"imageUrl\":\"%9$s\","
              + "\"tenantId\":\"%10$s\""
              + "}";
    }

}
