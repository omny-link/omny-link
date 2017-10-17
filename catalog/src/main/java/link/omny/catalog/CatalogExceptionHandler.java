package link.omny.catalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class CatalogExceptionHandler {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(CatalogExceptionHandler.class);

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(CatalogObjectNotFoundException.class)
    public @ResponseBody String handleEntityNotFoundException(
            CatalogObjectNotFoundException e) {
        LOGGER.warn(e.getMessage(), e);
        return String.format("{\"message\":\"%1$s\"}", e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public @ResponseBody String handleDataIntegrityViolationException(
            DataIntegrityViolationException e) {
         return unwrap(e);
    }

    private String unwrap(Throwable e) {
        if (e.getCause() != null) {
            return unwrap(e.getCause());
        } else {
            LOGGER.warn(e.getMessage());
            return String.format("{\"message\":\"%1$s\"}", e.getMessage());
        }
    }

}
