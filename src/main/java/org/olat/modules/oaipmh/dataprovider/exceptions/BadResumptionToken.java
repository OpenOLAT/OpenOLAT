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
public class BadResumptionToken extends HandlerException {

    /**
     *
     */
    private static final long serialVersionUID = -3155813328644172294L;

    /**
     * Creates a new instance of <code>BadResumptionToken</code> without detail
     * message.
     */
    public BadResumptionToken() {
    }

    /**
     * Constructs an instance of <code>BadResumptionToken</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public BadResumptionToken(String msg) {
        super(msg);
    }
}
