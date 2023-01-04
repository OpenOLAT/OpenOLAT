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
public class NoMatchesException extends HandlerException {

    /**
     *
     */
    private static final long serialVersionUID = 7051492953854730413L;

    /**
     * Creates a new instance of <code>NoMatchesException</code> without detail
     * message.
     */
    public NoMatchesException() {
    }

    /**
     * Constructs an instance of <code>NoMatchesException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public NoMatchesException(String msg) {
        super(msg);
    }
}
