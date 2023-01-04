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
public class OAIException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -3229816947775660398L;

    /**
     * Creates a new instance of <code>OAIException</code> without detail
     * message.
     */
    public OAIException() {
    }

    /**
     * Constructs an instance of <code>OAIException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public OAIException(String msg) {
        super(msg);
    }

    public OAIException(Exception ex) {
        super(ex.getMessage(), ex);
    }
}
