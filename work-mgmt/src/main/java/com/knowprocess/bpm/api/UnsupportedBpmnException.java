/*******************************************************************************
 *Copyright 2011-2018 Tim Stephenson and contributors
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
package com.knowprocess.bpm.api;

import java.util.Iterator;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * An exception suitable to be returned (as JSON or XML) across a REST API.
 * Useful for client developers debugging but must not reveal anything they do
 * not already know about the system (such as that a particular user, process
 * etc. exists).
 * 
 * @author tstephen
 * 
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Unsupported or invalid BPMN")
public class UnsupportedBpmnException extends Exception {

    private static final long serialVersionUID = 786708592317L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(UnsupportedBpmnException.class);

    private String details;

    public UnsupportedBpmnException(String msg) {
        super(msg);
        LOGGER.error(toLog());
    }

    public UnsupportedBpmnException(String msg, String details) {
        super(msg);
        LOGGER.error(toLog());
        this.details = details;
    }

    public UnsupportedBpmnException(String msg, Exception cause) {
        super(msg, cause);
        LOGGER.error(toLog(), cause);
        if (cause.getStackTrace().length > 0) {
            this.details = cause.getStackTrace()[0].getClassName() + "."
                    + cause.getStackTrace()[0].getMethodName() + "("
                    + cause.getStackTrace()[0].getLineNumber() + ")";
        }
    }

	public UnsupportedBpmnException(ConstraintViolationException e) {
		this(e.getMessage());

		StringBuffer sb = new StringBuffer();
		for (Iterator<ConstraintViolation<?>> it = e.getConstraintViolations()
				.iterator(); it.hasNext();) {
			ConstraintViolation<?> violation = it.next();
			sb.append(violation.getRootBeanClass() + "."
					+ violation.getPropertyPath() + " "
					+ violation.getMessage() + ". Value found was "
					+ violation.getInvalidValue());
			if (it.hasNext()) {
				sb.append(";");
			}
		}
		this.details = sb.toString();
	}

	public String toJson() {
        return String.format("{\"error\":\"%1$s\",\"details\":\"%2$s\"}",
                getMessage(), details);
    }

    public String toLog() {
        return String.format("Reporting error: %1$s, details: %2$s",
                getMessage(), details);
    }
}
