/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.olat.modules.oaipmh.dataprovider.exceptions;

@SuppressWarnings("serial")
public class MarshallingException extends Exception {

	public MarshallingException() {
	}

	public MarshallingException(String arg0) {
		super(arg0);
	}

	public MarshallingException(Throwable arg0) {
		super(arg0);
	}

	public MarshallingException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
