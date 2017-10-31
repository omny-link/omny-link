package com.knowprocess.bpm;

import java.io.IOException;
import java.io.StringWriter;
import java.net.UnknownHostException;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowprocess.bpm.api.BadJsonMessageException;
import com.knowprocess.bpm.api.ProcessDefinitionSuspendedException;
import com.knowprocess.bpm.api.ReportableException;

@ControllerAdvice
public class BpmControllerExceptionHandler {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(BpmControllerExceptionHandler.class);
    @Autowired
    @Qualifier("objectMapper")
    private ObjectMapper mapper;

    protected String toJson(Exception e) {
        StringWriter sw = new StringWriter();
        try {
            mapper.writeValue(sw, e);
            return sw.toString();
        } catch (IOException e2) {
            LOGGER.error(e2.getMessage(), e2);
            return String.format("{\"message\":\"%1$s\"}", e.getMessage());
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ActivitiObjectNotFoundException.class)
    public @ResponseBody String handleNotFound(Exception e) {
        LOGGER.error(e.getMessage());
        return toJson(e);
    }

    @ResponseStatus(HttpStatus.FOUND)
    @ExceptionHandler(ProcessDefinitionSuspendedException.class)
    public @ResponseBody String handleSuspended(ProcessDefinitionSuspendedException e) {
        LOGGER.warn(e.getMessage());
        return toJson(e);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ActivitiException.class)
    public void handleInternalServerError(ActivitiException e) {
        LOGGER.error(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ReportableException.class)
    public @ResponseBody String handleReportableException(ReportableException e) {
        LOGGER.error(e.getMessage());
        return toJson(e);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadJsonMessageException.class)
    public @ResponseBody String handleBadJsonMessage(BadJsonMessageException e) {
        LOGGER.error(e.getMessage(), e);
        return toJson(e);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public @ResponseBody String handleIllegalArgument(IllegalArgumentException e) {
        LOGGER.error(e.getMessage(), e);
        return toJson(e);
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(UnknownHostException.class)
    public @ResponseBody String handleUnknownHost(UnknownHostException e) {
        LOGGER.error(e.getMessage(), e);
        return toJson(e);
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
