/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.olat.modules.oaipmh.dataprovider.exceptions;

/**
 * @author Development @ Lyncode
 * @version 3.1.0
 */
public class InvalidContextException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -2541049223278976241L;

    /**
     * Creates a new instance of <code>InvalidContextException</code> without
     * detail message.
     */
    public InvalidContextException() {
    }

    /**
     * Constructs an instance of <code>InvalidContextException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public InvalidContextException(String msg) {
        super(msg);
    }
}
