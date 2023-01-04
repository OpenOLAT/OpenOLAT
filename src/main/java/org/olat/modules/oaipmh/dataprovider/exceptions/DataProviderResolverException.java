/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.olat.modules.oaipmh.dataprovider.exceptions;

public class DataProviderResolverException extends Exception {
    public DataProviderResolverException() {
    }

    public DataProviderResolverException(String message) {
        super(message);
    }

    public DataProviderResolverException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataProviderResolverException(Throwable cause) {
        super(cause);
    }
}
