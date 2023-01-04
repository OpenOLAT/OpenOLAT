/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.olat.modules.oaipmh.dataprovider.exceptions;

/**
 * @author "João Melo "
 */
public class UnknownParameterException extends HandlerException {
    private static final long serialVersionUID = -813886035789840394L;

    public UnknownParameterException() {
        super();
    }

    public UnknownParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownParameterException(String message) {
        super(message);
    }

    public UnknownParameterException(Throwable cause) {
        super(cause);
    }

}
