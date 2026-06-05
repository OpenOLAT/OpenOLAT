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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.olat.modules.oaipmh.common.exceptions.XmlWriteException;
import org.olat.modules.oaipmh.common.services.api.MetadataSearch;
import org.olat.modules.oaipmh.common.services.impl.MetadataSearchImpl;
import org.olat.modules.oaipmh.common.xml.XSISchema;
import org.olat.modules.oaipmh.common.xml.XmlWritable;
import org.olat.modules.oaipmh.common.xml.XmlWriter;

public class OAIDCMetadata implements XmlWritable {

	public static final String NAMESPACE_URI = "http://www.openarchives.org/OAI/2.0/oai_dc/";
	public static final String SCHEMA_LOCATION = "http://www.openarchives.org/OAI/2.0/oai_dc.xsd";
	public static final String NAMESPACE_DC = "http://purl.org/dc/elements/1.1/";

	protected List<Element> elements = new ArrayList<>();

	public List<Element> getElements() {
		return this.elements;
	}

	public OAIDCMetadata withElement(Element element) {
		this.elements.add(element);
		return this;
	}

	public String toString() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			this.write(new XmlWriter(out));
		} catch (XmlWriteException | XMLStreamException e) {
			// don't do anything
		}
		return out.toString();
	}

	@Override
	public void write(XmlWriter writer) throws XmlWriteException {
		try {
			writer.writeStartElement("oai_dc" + ":dc");
			writer.writeNamespace("oai_dc", NAMESPACE_URI);
			writer.writeNamespace("dc", NAMESPACE_DC);
			writer.writeNamespace(XSISchema.PREFIX, XSISchema.NAMESPACE_URI);
			writer.writeAttribute(XSISchema.PREFIX, XSISchema.NAMESPACE_URI, "schemaLocation",
					NAMESPACE_URI + " " + SCHEMA_LOCATION);

			for (Element element : getElements()) {
				writer.writeStartElement("dc", element.getName(), NAMESPACE_URI);
				element.write(writer);
				writer.writeEndElement();
			}
			writer.writeEndElement();
		} catch (XMLStreamException e) {
			throw new XmlWriteException(e);
		}
	}

	/**
	 * @return a simple searcher that returns search results as String elements.
	 */
	public MetadataSearch<String> searcher() {
		return new MetadataSearchImpl(this);
	}

}
