package link.omny.analytics;

import java.net.UnknownHostException;

import link.omny.analytics.api.AnalyticsException;
import link.omny.analytics.api.AnalyticsNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class AnalyticsExceptionHandler {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(AnalyticsExceptionHandler.class);

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(AnalyticsNotFoundException.class)
    public void handleNotFound(Exception e) {
        LOGGER.error(e.getMessage(), e);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(AnalyticsException.class)
    public void handleInternalServerError(Exception e) {
        LOGGER.error(e.getMessage(), e);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public void handleIllegalArgument(IllegalArgumentException e) {
        LOGGER.error(e.getMessage(), e);
    }

    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    @ExceptionHandler(UnknownHostException.class)
    public void handleUnknownHost(UnknownHostException e) {
        LOGGER.error(e.getMessage(), e);
    }

}
