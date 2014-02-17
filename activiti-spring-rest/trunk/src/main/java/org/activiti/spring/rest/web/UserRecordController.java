package org.activiti.spring.rest.web;
import org.activiti.spring.rest.model.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@RooWebJson(jsonObject = UserRecord.class)
@Controller
@RequestMapping("/users")
public class UserRecordController {
	
	protected static final String PATH = "/users";

	protected static final Logger LOGGER = LoggerFactory
			.getLogger(UserRecordController.class);

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
	public ResponseEntity<String> showJson(@PathVariable("id") String id, HttpServletRequest request) {
		LOGGER.info("%1$s /%2$s", RequestMethod.GET, id);
		
		System.out.println("id from req: "+ request.getServletPath());
		id = request.getServletPath().substring(request.getServletPath().lastIndexOf('/')+1);
		System.out.println("Find user with id: " + id);
        UserRecord userRecord = UserRecord.findUserRecord(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        if (userRecord == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(userRecord.toJson(), headers, HttpStatus.OK);
    }
}
