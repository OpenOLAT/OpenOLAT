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
public class FilterDoesNotExistsException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 795857368865831163L;

    /**
     * Creates a new instance of <code>FilterDoesNotExistsException</code>
     * without detail message.
     */
    public FilterDoesNotExistsException() {
    }

    /**
     * Constructs an instance of <code>FilterDoesNotExistsException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public FilterDoesNotExistsException(String msg) {
        super(msg);
    }
}
