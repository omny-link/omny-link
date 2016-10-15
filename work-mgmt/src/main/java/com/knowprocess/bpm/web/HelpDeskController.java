package com.knowprocess.bpm.web;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import com.knowprocess.bpm.api.UnsupportedBpmnException;
import com.knowprocess.resource.internal.gdrive.GDriveRepository;

@Controller
@RequestMapping("/{tenantId}/helpdesk")
public class HelpDeskController {

    private static final String MSG_NAME = "omny.ticket";

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(HelpDeskController.class);

    @Autowired
    protected ProcessEngine processEngine;
    
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
        String page = request.getHeader("Referer");

        // This is a quick JSON hack producing a list of strings
        Pattern pattern = Pattern.compile("\"(.+?)\"");
        Matcher matcher = pattern.matcher(context);

        List<String> keyValuePairs = new ArrayList<String>();
        while (matcher.find()) {
            // System.out.println(matcher.group(1));.
            keyValuePairs.add(matcher.group(1));
        }
        
        // +1 because keys and values are all in one list
        byte[] bytes = extractImage(filename,
                keyValuePairs.get(keyValuePairs.indexOf("image") + 1));
        GDriveRepository gdriveRepo = new GDriveRepository();
        gdriveRepo.write(filename, MediaType.IMAGE_PNG_VALUE,
                whenOccurred, new ByteArrayInputStream(bytes));
        String url = gdriveRepo.getDriveUrl();

        String userMsg = keyValuePairs
                .get(keyValuePairs.indexOf("message") + 1);
        String msg = String.format("%1$s. Raised on page: %2$s, screenshot: %3$s",
                userMsg, page, url);

        String json = String.format(getMessageTemplate(), user.getFirstName(),
                user.getLastName(), user.getEmail(), "tim@omny.link",
                "Support Ticket", msg, page, filename, url, tenantId);

        return msgController.handleMessageStart(tenantId, MSG_NAME, bizDesc,
                json);
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

    private byte[] extractImage(String filename, String image)
            throws FileNotFoundException, IOException {
        byte[] bytes = Base64.getDecoder().decode(
                image.substring(image.indexOf(',') + 1).getBytes());

        if (LOGGER.isDebugEnabled()) {
            File file = new File(System.getProperty("java.io.tmpdir"), filename);
            FileOutputStream fileOuputStream = new FileOutputStream(file);
            fileOuputStream.write(bytes);
            fileOuputStream.close();
        }
        return bytes;
    }

}
