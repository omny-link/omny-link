package link.omny.catalog;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class CatalogExceptionHandler {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(CatalogExceptionHandler.class);

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public @ResponseBody String handleConstraintViolationException(
            ConstraintViolationException e) {
        LOGGER.error("Constraint violation: " + e.getMessage());
        StringBuilder sb = new StringBuilder();
        for (ConstraintViolation<?> cv : e.getConstraintViolations()) {
            sb.append(String.format(
                    "  %1$s %2$s but was %3$s, bean affected is %4$s",
                    cv.getPropertyPath(), cv.getMessage(),
                    cv.getInvalidValue(), cv.getLeafBean()));
        }
        LOGGER.error(sb.toString());
        return sb.toString();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(CatalogObjectNotFoundException.class)
    public @ResponseBody String handleEntityNotFoundException(
            CatalogObjectNotFoundException e) {
        LOGGER.warn(e.getMessage(), e);
        return String.format("{\"message\":\"%1$s\"}", e.getMessage());
    }
}
