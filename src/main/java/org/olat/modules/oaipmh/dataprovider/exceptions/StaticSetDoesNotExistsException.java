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
public class StaticSetDoesNotExistsException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 2596586736462175699L;

    /**
     * Creates a new instance of <code>StaticSetDoesNotExistsException</code>
     * without detail message.
     */
    public StaticSetDoesNotExistsException() {
    }

    /**
     * Constructs an instance of <code>StaticSetDoesNotExistsException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public StaticSetDoesNotExistsException(String msg) {
        super(msg);
    }
}
