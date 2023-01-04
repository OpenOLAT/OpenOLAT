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
 */
public class CannotDisseminateFormatException extends HandlerException {

    private static final long serialVersionUID = -6654346398053844736L;

    /**
     * Creates a new instance of <code>CannotDisseminateFormatException</code>
     * without detail message.
     */
    public CannotDisseminateFormatException() {
    }

    /**
     * Constructs an instance of <code>CannotDisseminateFormatException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public CannotDisseminateFormatException(String msg) {
        super(msg);
    }
}
