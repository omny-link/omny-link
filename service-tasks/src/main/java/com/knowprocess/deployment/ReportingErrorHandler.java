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
package com.knowprocess.deployment;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import com.knowprocess.deployment.ValidationError.ValidationLevel;

/**
 * 
 * @author tim.stephenson@knowprocess.com
 * 
 */
public class ReportingErrorHandler implements ErrorHandler {

	private List<ValidationError> errors = new ArrayList<ValidationError>();

    public void warning(SAXParseException e) {
        System.out.println(e.getMessage());
		errors.add(new ValidationError(ValidationLevel.WARN, e.getMessage()));
    }

    public void error(SAXParseException e) {
        System.out.println(e.getMessage());
		errors.add(new ValidationError(ValidationLevel.ERROR, e.getMessage()));
    }

    public void fatalError(SAXParseException e) {
        System.out.println(e.getMessage());
		errors.add(new ValidationError(ValidationLevel.FATAL, e.getMessage()));
    }

	public List<ValidationError> getErrors() {
		return errors;
	}
}
