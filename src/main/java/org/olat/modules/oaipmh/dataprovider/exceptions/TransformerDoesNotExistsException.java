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
public class TransformerDoesNotExistsException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -8417020535730995337L;

    /**
     * Creates a new instance of <code>TransformerDoesNotExistsException</code>
     * without detail message.
     */
    public TransformerDoesNotExistsException() {
    }

    /**
     * Constructs an instance of <code>TransformerDoesNotExistsException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public TransformerDoesNotExistsException(String msg) {
        super(msg);
    }
}
