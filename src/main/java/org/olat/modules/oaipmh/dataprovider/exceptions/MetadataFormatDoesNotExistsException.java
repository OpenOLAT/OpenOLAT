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
public class MetadataFormatDoesNotExistsException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -7169710535202336338L;

    /**
     * Creates a new instance of
     * <code>MetadataFormatDoesNotExistsException</code> without detail message.
     */
    public MetadataFormatDoesNotExistsException() {
    }

    /**
     * Constructs an instance of
     * <code>MetadataFormatDoesNotExistsException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public MetadataFormatDoesNotExistsException(String msg) {
        super(msg);
    }
}
