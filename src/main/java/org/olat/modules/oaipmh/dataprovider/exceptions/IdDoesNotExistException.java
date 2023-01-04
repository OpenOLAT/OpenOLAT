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
public class IdDoesNotExistException extends HandlerException {

    /**
     *
     */
    private static final long serialVersionUID = -657866486396669641L;

    /**
     * Creates a new instance of <code>IdDoesNotExistException</code> without
     * detail message.
     */
    public IdDoesNotExistException() {
    }

    /**
     * Constructs an instance of <code>IdDoesNotExistException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public IdDoesNotExistException(String msg) {
        super(msg);
    }

    public IdDoesNotExistException(Exception e) {
        super(e);
    }
}
