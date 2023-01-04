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
public class BadArgumentException extends HandlerException {

    /**
     *
     */
    private static final long serialVersionUID = 6436751364163509217L;

    /**
     * Creates a new instance of <code>BadArgumentException</code> without
     * detail message.
     */
    public BadArgumentException() {
    }

    /**
     * Constructs an instance of <code>BadArgumentException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public BadArgumentException(String msg) {
        super(msg);
    }
}
