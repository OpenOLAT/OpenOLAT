/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content got modified for OpenOlat Context
 */

package org.olat.modules.oaipmh.common.oaidc;

import javax.xml.stream.XMLStreamException;

import org.olat.modules.oaipmh.common.exceptions.XmlWriteException;
import org.olat.modules.oaipmh.common.xml.XmlWritable;
import org.olat.modules.oaipmh.common.xml.XmlWriter;

public class Field implements XmlWritable {
	protected String value;
	protected String name;

	public Field() {
	}

	public Field(String value, String name) {
		this.value = value;
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public Field withValue(String value) {
		this.value = value;
		return this;
	}

	public String getName() {
		return name;
	}

	public Field withName(String value) {
		this.name = value;
		return this;
	}

	@Override
	public void write(XmlWriter writer) throws XmlWriteException {
		try {
			//if (this.name != null)
			//    writer.writeAttribute("name", this.getName());

			if (this.value != null)
				writer.writeCharacters(value);

		} catch (XMLStreamException e) {
			throw new XmlWriteException(e);
		}
	}
}
