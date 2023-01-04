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
public class ConfigurationException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -8207824896000568979L;

    /**
     * Creates a new instance of <code>ConfigurationException</code> without
     * detail message.
     */
    public ConfigurationException() {
    }

    /**
     * Constructs an instance of <code>ConfigurationException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ConfigurationException(String msg) {
        super(msg);
    }

    public ConfigurationException(String message, Exception ex) {
        super(message, ex);
    }
}
