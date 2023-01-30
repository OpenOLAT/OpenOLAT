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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.olat.modules.oaipmh.common.exceptions.XmlWriteException;
import org.olat.modules.oaipmh.common.xml.XSISchema;
import org.olat.modules.oaipmh.common.xml.XmlWritable;
import org.olat.modules.oaipmh.common.xml.XmlWriter;


public class OAIPMH implements XmlWritable {
	public static final String NAMESPACE_URI = "http://www.openarchives.org/OAI/2.0/";
	public static final String SCHEMA_LOCATION = "http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd";
	private final List<Error> errors = new ArrayList<Error>();
	private Date responseDate = new Date();
	private Request request;
	private Verb verb;

	public Date getResponseDate() {
		return responseDate;
	}

	public Request getRequest() {
		return request;
	}

	public OAIPMH withResponseDate(Date responseDate) {
		this.responseDate = responseDate;
		return this;
	}

	public OAIPMH withRequest(Request request) {
		this.request = request;
		return this;
	}

	public List<Error> getErrors() {
		return errors;
	}

	public OAIPMH withError(Error error) {
		this.errors.add(error);
		return this;
	}

	public boolean hasErrors() {
		return !this.errors.isEmpty();
	}

	public Verb getVerb() {
		return verb;
	}

	public OAIPMH withVerb(Verb verb) {
		this.verb = verb;
		return this;
	}

	@Override
	public void write(XmlWriter writer) throws XmlWriteException {
		try {
			writer.writeStartElement("OAI-PMH");
			writer.writeDefaultNamespace(NAMESPACE_URI);
			writer.writeNamespace(XSISchema.PREFIX, XSISchema.NAMESPACE_URI);
			writer.writeAttribute(XSISchema.PREFIX, XSISchema.NAMESPACE_URI, "schemaLocation",
					NAMESPACE_URI + " " + SCHEMA_LOCATION);

			writer.writeElement("responseDate", this.responseDate, Granularity.Second);
			writer.writeElement("request", request);

			if (!errors.isEmpty()) {
				for (Error error : errors)
					writer.writeElement("error", error);
			} else {
				if (verb == null) throw new XmlWriteException("An error or a valid response must be set");
				writer.writeElement(verb.getType().displayName(), verb);
			}

			writer.writeEndElement();
		} catch (XMLStreamException e) {
			throw new XmlWriteException(e);
		}
	}
}
