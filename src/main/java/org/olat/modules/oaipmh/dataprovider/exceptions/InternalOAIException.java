/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.olat.modules.oaipmh.dataprovider.exceptions;

public class InternalOAIException extends RuntimeException {
    public InternalOAIException() {
    }

    public InternalOAIException(String message) {
        super(message);
    }

    public InternalOAIException(String message, Throwable cause) {
        super(message, cause);
    }

    public InternalOAIException(Throwable cause) {
        super(cause);
    }
}
