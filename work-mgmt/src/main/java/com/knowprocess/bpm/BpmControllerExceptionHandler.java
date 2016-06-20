package com.knowprocess.bpm;

import java.net.UnknownHostException;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.knowprocess.bpm.api.BadJsonMessageException;
import com.knowprocess.bpm.api.ReportableException;
import com.knowprocess.bpmn.BusinessEntityNotFoundException;

@ControllerAdvice
public class BpmControllerExceptionHandler {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(BpmControllerExceptionHandler.class);

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ActivitiObjectNotFoundException.class)
    public void handleNotFound(Exception e) {
        LOGGER.error(e.getMessage(), e);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(BusinessEntityNotFoundException.class)
    public @ResponseBody String handleNotFoundException(
            BusinessEntityNotFoundException e) {
        LOGGER.error(String.format("%1$s with id %2$s not found",
                e.getEntity(), e.getId()));
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ActivitiException.class)
    public void handleInternalServerError(Exception e) {
        LOGGER.error(e.getMessage(), e);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ReportableException.class)
    public void handleReportableException(ReportableException e) {
        LOGGER.error(e.getMessage(), e);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadJsonMessageException.class)
    public void handleBadJsonMessage(BadJsonMessageException e) {
        LOGGER.error(e.getMessage(), e);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public void handleIllegalArgument(IllegalArgumentException e) {
        LOGGER.error(e.getMessage(), e);
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(UnknownHostException.class)
    public void handleUnknownHost(UnknownHostException e) {
        LOGGER.error(e.getMessage(), e);
    }

//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ExceptionHandler(UnsupportedBpmnException.class)
//    public ModelAndView handleUnsupportedBpmnError(
//            HttpServletRequest req, UnsupportedBpmnException e)
//            throws Exception {
//        // If the exception is annotated with @ResponseStatus rethrow it and let
//        // the framework handle it - like the OrderNotFoundException example
//        // at the start of this post.
//        // AnnotationUtils is a Spring Framework utility class.
//        if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null)
//            throw e;
//
//        // Otherwise setup and send the user to a default error-view.
//        ModelAndView mav = new ModelAndView();
//        mav.addObject("exception", e);
//        mav.addObject("url", req.getRequestURL());
//        mav.setViewName("error");
//        return mav;
//    }
}
