/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content got modified for OpenOlat Context
 */

package org.olat.modules.oaipmh.common.xml;


import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.XMLEvent;

import org.olat.modules.oaipmh.common.exceptions.XmlWriteException;

public class EchoElement implements XmlWritable {
	private static final XMLInputFactory factory = XMLInputFactory.newFactory();
	private final Stack<Set<String>> declaredPrefixes = new Stack<>();
	private String xmlString = null;

	public EchoElement(String xmlString) {
		this.xmlString = xmlString;
	}

	@Override
	public void write(XmlWriter writer) throws XmlWriteException {
		try {
			XMLEventReader reader = factory.createXMLEventReader(new ByteArrayInputStream(xmlString.getBytes()));
			while (reader.hasNext()) {
				XMLEvent event = reader.nextEvent();

				if (event.isStartElement()) {
					declaredPrefixes.push(new HashSet<>());

					QName name = event.asStartElement().getName();
					writer.writeStartElement(name.getPrefix(), name.getLocalPart(), name.getNamespaceURI());
					addNamespaceIfRequired(writer, name);

					// Copy any other namespace declarations
					Iterator<Namespace> itNamespaces = event.asStartElement().getNamespaces();
					while (itNamespaces.hasNext()) {
						Namespace namespace = itNamespaces.next();
						addNamespaceIfRequired(writer, new QName(namespace.getNamespaceURI(), "", namespace.getPrefix()));
					}

					// Copy attributes
					@SuppressWarnings("unchecked")
					Iterator<Attribute> it = event.asStartElement().getAttributes();

					while (it.hasNext()) {
						Attribute attr = it.next();
						QName attrName = attr.getName();
						addNamespaceIfRequired(writer, attrName);
						writer.writeAttribute(attrName.getPrefix(), attrName.getNamespaceURI(), attrName.getLocalPart(), attr.getValue());
					}
				} else if (event.isEndElement()) {
					declaredPrefixes.pop();
					writer.writeEndElement();
				} else if (event.isCharacters()) {
					writer.writeCharacters(event.asCharacters().getData());
				}
			}
		} catch (XMLStreamException e) {
			throw new XmlWriteException("Error trying to output '" + this.xmlString + "'", e);
		}
	}

	private void addNamespaceIfRequired(XmlWriter writer, QName name) throws XMLStreamException {
		// Search for namespace in scope, starting from the root.
		for (Set<String> ancestorNamespaces : declaredPrefixes) {
			if (ancestorNamespaces.contains(name.getPrefix() + name.getNamespaceURI())) { // Prefixes might be reused.
				return;
			}
		}

		writer.writeNamespace(name.getPrefix(), name.getNamespaceURI());
		declaredPrefixes.peek().add(name.getPrefix() + name.getNamespaceURI());
	}
}
