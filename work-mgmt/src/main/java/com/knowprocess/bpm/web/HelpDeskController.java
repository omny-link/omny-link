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
import java.util.Enumeration;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowprocess.auth.JwtSettings;
import com.knowprocess.auth.WebSecurityConfig;
import com.knowprocess.auth.jwt.extractor.TokenExtractor;
import com.knowprocess.auth.model.token.RawAccessJwtToken;
import com.knowprocess.bpm.api.UnsupportedBpmnException;
import com.knowprocess.resource.internal.gdrive.GDriveRepository;

@Controller
@RequestMapping("/{tenantId}/helpdesk")
public class HelpDeskController {

    private static final String UNKNOWN = "unknown";

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(HelpDeskController.class);

    @Value("${kp.application.root-user:tim@knowprocess.com}")
    protected String rootUser;

    @Value("${kp.ticket.msg-name:omny.ticket}")
    protected String ticketMsgName;

    @Value("${kp.ticket.subject:Support Ticket}")
    protected String ticketSubject;

    @Autowired
    private TokenExtractor tokenExtractor;

    @Autowired
    private JwtSettings jwtSettings;

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
        Optional<User> user = findUser(request);
        String bizDesc = String.format("ticket-%1$s-%2$s",
                (user.isPresent() ? user.get().getEmail() : UNKNOWN),
                isoFormatter.format(whenOccurred));
        String filename = bizDesc + ".png";

        JsonNode json = objectMapper.readTree(context);

        // image will be URL encoded, strip "data:image/png;base64,"
        String s = json.get("image").asText();
        String url = uploadImage(whenOccurred, filename,
                Base64.getDecoder().decode(s.substring(s.indexOf(",") + 1).getBytes()));

        String ticketMsg = String.format("%1$s. Raised on page: %2$s, screenshot: %3$s",
                json.get("message").asText(), request.getHeader("Referer"), url);

        String ticketMsgPayload;
        if (user.isPresent()) {
            ticketMsgPayload = String.format(getMessageTemplate(),
                    user.get().getFirstName(), user.get().getLastName(),
                    user.get().getEmail(), rootUser, ticketSubject, ticketMsg,
                    request.getHeader("Referer"), filename, url, tenantId);
        } else {
            ticketMsgPayload = String.format(getMessageTemplate(),
                    UNKNOWN, UNKNOWN, UNKNOWN,
                    rootUser, ticketSubject, ticketMsg,
                    request.getHeader("Referer"), filename, url, tenantId);
        }
        LOGGER.debug("Sending support ticket: {}", ticketMsgPayload);
        return msgController.handleMessageStart(
                tenantId, ticketMsgName, bizDesc, ticketMsgPayload);
    }

    private Optional<User> findUser(HttpServletRequest request) {
        try {
            for (Enumeration<String> en = request.getHeaderNames() ; en.hasMoreElements() ; ) {
                String header = en.nextElement();
                LOGGER.info("header: {} = {}", header, request.getHeader(header));
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        try {
            Object name = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            LOGGER.info("username {}", name);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        try {
            String username = request.getUserPrincipal().getName();
            LOGGER.debug("Found username {} in request principal", username);
            return Optional.ofNullable(processEngine.getIdentityService().createUserQuery()
                    .userId(username).singleResult());
        } catch (Exception e) {
            String tokenPayload = request.getHeader(WebSecurityConfig.JWT_TOKEN_HEADER_PARAM);
            LOGGER.debug("Considering JWT header: {}", tokenPayload);
            RawAccessJwtToken token = new RawAccessJwtToken(tokenExtractor.extract(tokenPayload));
            String username = token.parseClaims(jwtSettings.getTokenSigningKey()).getBody().getSubject();
            LOGGER.debug("Found username {} in JWT header", username);
            return Optional.ofNullable(processEngine.getIdentityService().createUserQuery()
                    .userId(username).singleResult());
        }
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
