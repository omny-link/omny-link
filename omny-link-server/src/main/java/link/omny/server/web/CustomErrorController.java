package link.omny.server.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    private static final String PATH = "/error";

    @RequestMapping(value = PATH)
    public String error(HttpServletRequest request, Throwable t) {
        Integer statusCode = (Integer) request
                .getAttribute("javax.servlet.error.status_code");
        String message = (String) request
                .getAttribute("javax.servlet.error.message");
        // return "forward:/";
        return String.format("redirect:/index.html?statusCode=%1$d&msg=%2$s",
                statusCode, message);
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
}