package com.knowprocess.bpm;

/**
 * An exception to return minimal information to help with debugging API calls
 * but not reveal any internal information.
 *
 * @author Tim Stephenson
 */
public class BadJsonMessageException extends RuntimeException {

    private static final long serialVersionUID = 7459492641770180894L;

    public BadJsonMessageException(String msg) {
        super(msg);
    }

}
