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

import java.io.Serializable;

/**
 * 
 * @author tim.stephenson@knowprocess.com
 * 
 */
public class ValidationError implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -459460675024974897L;

    public enum ValidationLevel {
        DEBUG, INFO, WARN, ERROR, FATAL;
	}

	private ValidationLevel level;

	private String message;

	public ValidationError() {

	}

	public ValidationError(ValidationLevel level, String message) {
		super();
		this.level = level;
		this.message = message;
	}

	public ValidationLevel getLevel() {
		return level;
	}

	public void setLevel(ValidationLevel level) {
		this.level = level;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
