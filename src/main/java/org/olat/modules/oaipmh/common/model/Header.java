/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.olat.modules.oaipmh.common.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.olat.modules.oaipmh.common.exceptions.XmlWriteException;
import org.olat.modules.oaipmh.common.xml.XmlWritable;
import org.olat.modules.oaipmh.common.xml.XmlWriter;

public class Header implements XmlWritable {
	protected String identifier;
	protected Date datestamp;
	protected List<String> setSpec = new ArrayList<>();
	protected Status status;

	public String getIdentifier() {
		return identifier;
	}

	public Header withIdentifier(String value) {
		this.identifier = value;
		return this;
	}

	public Date getDatestamp() {
		return datestamp;
	}

	public Header withDatestamp(Date value) {
		this.datestamp = value;
		return this;
	}

	public List<String> getSetSpecs() {
		return this.setSpec;
	}

	public Status getStatus() {
		return status;
	}

	public Header withStatus(Status value) {
		this.status = value;
		return this;
	}

	public Header withSetSpec(String setSpec) {
		this.setSpec.add(setSpec);
		return this;
	}

	public boolean isDeleted() {
		return this.status != null;
	}

	@Override
	public void write(XmlWriter writer) throws XmlWriteException {
		try {
			if (this.status != null)
				writer.writeAttribute("status", this.status.value());
			writer.writeElement("identifier", identifier);
			writer.writeElement("datestamp", datestamp);
			for (String setSpec : this.getSetSpecs())
				writer.writeElement("setSpec", setSpec);
		} catch (XMLStreamException e) {
			throw new XmlWriteException(e);
		}
	}


	public enum Status {
		DELETED("deleted");

		private final String representation;

		Status(String representation) {
			this.representation = representation;
		}

		public static Status fromRepresentation(String representation) {
			for (Status status : Status.values()) {
				if (status.representation.equals(representation)) {
					return status;
				}
			}
			throw new IllegalArgumentException(representation);
		}

		public String value() {
			return representation;
		}
	}
}
