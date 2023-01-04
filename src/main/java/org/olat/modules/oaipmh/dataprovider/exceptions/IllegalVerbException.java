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
public class IllegalVerbException extends HandlerException {

    /**
     *
     */
    private static final long serialVersionUID = 2748244610538429452L;

    /**
     * Creates a new instance of <code>IllegalVerbException</code> without
     * detail message.
     */
    public IllegalVerbException() {
    }

    /**
     * Constructs an instance of <code>IllegalVerbException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public IllegalVerbException(String msg) {
        super(msg);
    }
}
