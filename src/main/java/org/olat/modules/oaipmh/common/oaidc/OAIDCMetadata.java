/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content got modified for OpenOlat Context
 */

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="client://java.sun.com/xml/jaxb">client://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.06.27 at 01:16:00 PM WEST 
//

package org.olat.modules.oaipmh.common.oaidc;


import static com.lyncode.xml.matchers.QNameMatchers.localPart;
import static com.lyncode.xml.matchers.XmlEventMatchers.aStartElement;
import static com.lyncode.xml.matchers.XmlEventMatchers.anElement;
import static com.lyncode.xml.matchers.XmlEventMatchers.anEndElement;
import static com.lyncode.xml.matchers.XmlEventMatchers.elementName;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import com.lyncode.xml.XmlReader;
import com.lyncode.xml.exceptions.XmlReaderException;
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

	public static OAIDCMetadata parse(InputStream inputStream) throws XmlReaderException {
		XmlReader reader = new XmlReader(inputStream);
		OAIDCMetadata OAIDCMetadata = new OAIDCMetadata();
		if (!reader.next(aStartElement()).current(allOf(aStartElement(), elementName(localPart(equalTo("metadata"))))))
			throw new XmlReaderException("Invalid XML. Expecting entity 'metadata'");

		while (reader.next(anElement()).current(aStartElement())) {
			if (reader.current(elementName(localPart(equalTo("element"))))) // Nested element
				OAIDCMetadata.withElement(Element.parse(reader));
			else throw new XmlReaderException("Unexpected element");
		}

		if (!reader.current(allOf(anEndElement(), elementName(localPart(equalTo("metadata"))))))
			throw new XmlReaderException("Invalid XML. Expecting end of entity 'metadata'");

		reader.close();
		return OAIDCMetadata;
	}

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
