
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
