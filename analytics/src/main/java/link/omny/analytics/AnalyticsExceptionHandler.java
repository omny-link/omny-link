/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
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
