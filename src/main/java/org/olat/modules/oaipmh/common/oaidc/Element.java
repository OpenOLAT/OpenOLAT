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

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.olat.core.CoreSpringFactory;
import org.olat.modules.oaipmh.common.exceptions.XmlWriteException;
import org.olat.modules.oaipmh.common.xml.XmlWritable;
import org.olat.modules.oaipmh.common.xml.XmlWriter;
import org.olat.repository.RepositoryService;

public class Element implements XmlWritable {
	protected List<Field> fields = new ArrayList<>();
	protected String name;
	protected String value;
	protected List<Element> elements = new ArrayList<>();

	RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);

	public Element(String name) {
		this.name = name;
	}

	public List<Field> getFields() {
		return fields;
	}

	public String getName() {
		return name;
	}

	public Element withName(String value) {
		this.name = value;
		return this;
	}

	public Element withField(Field field) {
		this.fields.add(field);
		return this;
	}

	public Element withValue(String value) {
		this.value = value;
		return this;
	}

	public Element withField(String name, String value) {
		this.value = value;
		//this.fields.add(new Field(value, name));
		return this;
	}

	public List<Element> getElements() {
		return this.elements;
	}

	public Element withElement(Element element) {
		this.elements.add(element);
		return this;
	}

	@Override
	public void write(XmlWriter writer) throws XmlWriteException {
		try {
			// if (this.name != null)
			//writer.writeAttribute("name", this.getName());

            /*for (RepositoryEntry repositoryEntry : repositoryService.loadRepositoryForMetadata("published")) {

            }*/

            /*for (Element element : this.getElements()) {
                writer.writeStartElement(getName());
                element.write(writer);
                writer.writeCharacters(value);
                writer.writeEndElement();
            }*/

			writer.writeCharacters(value);

            /*for (Field field : this.getFields()) {
                writer.writeStartElement("field");
                field.withValue(repositoryService.loadRepositoryForMetadata("published").get(0).getDisplayname());
                field.write(writer);
                writer.writeEndElement();
            }*/

		} catch (XMLStreamException e) {
			throw new XmlWriteException(e);
		}
	}
}
