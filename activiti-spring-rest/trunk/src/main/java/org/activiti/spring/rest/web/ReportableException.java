package org.activiti.spring.rest.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An exception suitable to be returned (as JSON or XML) across a REST API.
 * Useful for client developers debugging but must not reveal anything they do
 * not already know about the system (such as that a particular user, process
 * etc. exists).
 * 
 * @author tstephen
 * 
 */
public class ReportableException extends Exception {

	private static final long serialVersionUID = 786708592317L;

	protected static final Logger LOGGER = LoggerFactory
			.getLogger(ReportableException.class);

	private String details;

	public ReportableException(String msg) {
		super(msg);
		LOGGER.error(toLog());
	}

	public ReportableException(String msg, String details) {
		super(msg);
		LOGGER.error(toLog());
		this.details = details;
	}

	public ReportableException(String msg, Exception cause) {
		super(msg, cause);
		if (cause.getStackTrace().length> 0){ 
			this.details = cause.getStackTrace()[0].getClassName() + "."
					+ cause.getStackTrace()[0].getMethodName() + "("
					+ cause.getStackTrace()[0].getLineNumber() + ")";
		}
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
