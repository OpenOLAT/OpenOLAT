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
public class NoMetadataFormatsException extends HandlerException {

    /**
     *
     */
    private static final long serialVersionUID = 7091872607176190034L;

    /**
     * Creates a new instance of <code>NoMetadataFormatsException</code> without
     * detail message.
     */
    public NoMetadataFormatsException() {
    }

    /**
     * Constructs an instance of <code>NoMetadataFormatsException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public NoMetadataFormatsException(String msg) {
        super(msg);
    }
}
