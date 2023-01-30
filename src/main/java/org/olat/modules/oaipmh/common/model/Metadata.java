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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.olat.modules.oaipmh.common.exceptions.XmlWriteException;
import org.olat.modules.oaipmh.common.oaidc.OAIDCMetadata;
import org.olat.modules.oaipmh.common.oaioo.OAIOOMetadata;
import org.olat.modules.oaipmh.common.xml.EchoElement;
import org.olat.modules.oaipmh.common.xml.XmlWritable;
import org.olat.modules.oaipmh.common.xml.XmlWriter;

public class Metadata implements XmlWritable {
	protected OAIDCMetadata value;
	protected OAIOOMetadata ooValue;
	private String string;

	public Metadata(OAIDCMetadata value) {
		this.value = value;
	}

	public Metadata(OAIOOMetadata value) {
		this.ooValue = value;
	}

	public Metadata(String value) {
		this.string = value;
	}

	public Metadata(InputStream value) throws IOException {
		this.string = IOUtils.toString(value);
	}

	@Override
	public void write(XmlWriter writer) throws XmlWriteException {
		if (this.ooValue != null)
			this.ooValue.write(writer);
		else if (this.value != null) {
			this.value.write(writer);
		} else {
			EchoElement elem = new EchoElement(string);
			elem.write(writer);
		}
	}

	public OAIDCMetadata getValue() {
		return value;
	}

	public OAIOOMetadata getOoValue() {
		return ooValue;
	}

}
