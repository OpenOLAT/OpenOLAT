/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content got modified for OpenOlat Context
 */

package org.olat.modules.oaipmh.common.model;

import org.olat.modules.oaipmh.common.exceptions.XmlWriteException;
import org.olat.modules.oaipmh.common.xml.EchoElement;
import org.olat.modules.oaipmh.common.xml.XmlWritable;
import org.olat.modules.oaipmh.common.xml.XmlWriter;

public class About implements XmlWritable {
	private final String value;

	public About(String xmlValue) {
		this.value = xmlValue;
	}

	public String getValue() {
		return value;
	}

	@Override
	public void write(XmlWriter writer) throws XmlWriteException {
		if (this.value != null) {
			EchoElement elem = new EchoElement(value);
			elem.write(writer);
		}
	}
}
