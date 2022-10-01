/*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package link.omny.server;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.databind.ObjectMapper;

import link.omny.supportservices.exceptions.BusinessEntityNotFoundException;

@ControllerAdvice
public class AppExceptionHandler {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(AppExceptionHandler.class);
    @Autowired
    @Qualifier("objectMapper")
    private ObjectMapper mapper;

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public @ResponseBody String handleConstraintViolationException(
            ConstraintViolationException e) throws IOException {
        LOGGER.error("Constraint violation: " + e.getMessage());
        StringWriter sw = new StringWriter();
        for (Iterator<ConstraintViolation<?>> it = e
                .getConstraintViolations().iterator(); it.hasNext();) {
            ConstraintViolation<?> cv = (ConstraintViolation<?>) it
                    .next();
            mapper.writeValue(sw, cv.getMessage());
            if (it.hasNext()) {
                sw.append(',');
            }
        }
        return sw.toString();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException.class)
    public @ResponseBody String handleEntityNotFoundException(
            EntityNotFoundException e) {
        LOGGER.error(e.getMessage());
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(BusinessEntityNotFoundException.class)
    public @ResponseBody String handleNotFoundException(
            BusinessEntityNotFoundException e) {
        LOGGER.error("{} with id {} not found", e.getEntity(), e.getId());
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    @ExceptionHandler(HttpClientErrorException.class)
    public @ResponseBody String handleProcessGatewayException(
            HttpClientErrorException e) {
        LOGGER.error("Upstream responded {}: {}", e.getStatusCode(), e.getStatusText());
        LOGGER.error("Response body: {}", e.getResponseBodyAsString());
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    @ExceptionHandler(ProcessStartException.class)
    public @ResponseBody String handleProcessStartException(
            ProcessStartException e) {
        LOGGER.error("Upstream responded {}: {}", e.getMessage(), e);
        return e.getMessage();
    }
}
