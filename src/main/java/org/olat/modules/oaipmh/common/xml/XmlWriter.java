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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.olat.core.util.xml.XMLFactories;
import org.olat.modules.oaipmh.common.exceptions.XmlWriteException;
import org.olat.modules.oaipmh.common.model.Granularity;
import org.olat.modules.oaipmh.common.model.ResumptionToken;
import org.olat.modules.oaipmh.common.services.api.DateProvider;
import org.olat.modules.oaipmh.common.services.api.ResumptionTokenFormat;
import org.olat.modules.oaipmh.common.services.impl.SimpleResumptionTokenFormat;
import org.olat.modules.oaipmh.common.services.impl.UTCDateProvider;

public class XmlWriter implements XMLStreamWriter {
	private final DateProvider dateProvider;
	private final WriterContext writerContext;
	
    private final XMLStreamWriter writer;
    private final OutputStream outputStream;

	public XmlWriter(OutputStream output) throws XMLStreamException {
		this.outputStream = output;
		this.dateProvider = new UTCDateProvider();
		this.writerContext = defaultContext();
		writer = XMLFactories.newXMLOutputFactory().createXMLStreamWriter(output, "UTF-8");
	}

	public XmlWriter(OutputStream output, WriterContext writerContext) throws XMLStreamException {
		this.outputStream = output;
		this.dateProvider = new UTCDateProvider();
		this.writerContext = writerContext;
		writer = XMLFactories.newXMLOutputFactory().createXMLStreamWriter(output, "UTF-8");
	}

	public static String toString(XmlWritable writable) throws XMLStreamException, XmlWriteException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		XmlWriter writer = new XmlWriter(outputStream, defaultContext());
		writable.write(writer);
		writer.close();
		return outputStream.toString();
	}

	public static WriterContext defaultContext() {
		return new WriterContext(Granularity.Second, new SimpleResumptionTokenFormat());
	}
	
    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        writer.writeStartElement(localName);
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        writer.writeStartElement(namespaceURI, localName);
    }

    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        writer.writeStartElement(prefix, localName, namespaceURI);
    }

    @Override
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        writer.writeEmptyElement(namespaceURI, localName);
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        writer.writeEmptyElement(prefix, localName, namespaceURI);
    }

    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException {
        writer.writeEmptyElement(localName);
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        writer.writeEndElement();
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        writer.writeEndDocument();
    }

    @Override
    public void close() throws XMLStreamException {
        writer.close();
    }

    @Override
    public void flush() throws XMLStreamException {
        writer.flush();
    }
    
	@Override
	public void writeAttribute(String localName, String value) throws XMLStreamException {
		if (value != null)
			writer.writeAttribute(localName, value);
	}

    @Override
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
        writer.writeAttribute(prefix, namespaceURI, localName, value);
    }

    @Override
    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
        writer.writeAttribute(namespaceURI, localName, value);
    }

    @Override
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        writer.writeNamespace(prefix, namespaceURI);
    }

    @Override
    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
        writer.writeDefaultNamespace(namespaceURI);
    }

    @Override
    public void writeComment(String data) throws XMLStreamException {
        writer.writeComment(data);
    }

    @Override
    public void writeProcessingInstruction(String target) throws XMLStreamException {
        writer.writeProcessingInstruction(target);
    }

    @Override
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        writer.writeProcessingInstruction(target, data);
    }

    @Override
    public void writeCData(String data) throws XMLStreamException {
        writer.writeCData(data);
    }

    @Override
    public void writeDTD(String dtd) throws XMLStreamException {
        writer.writeDTD(dtd);
    }

    @Override
    public void writeEntityRef(String name) throws XMLStreamException {
        writer.writeEntityRef(name);
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        writer.writeStartDocument();
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        writer.writeStartDocument(version);
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        writer.writeStartDocument(encoding, version);
    }

	@Override
	public void writeCharacters(String text) throws XMLStreamException {
		if (text != null) {
			writer.writeCharacters(text);
		}
	}

    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        writer.writeCharacters(text, start, len);
    }

    @Override
    public String getPrefix(String uri) throws XMLStreamException {
        return writer.getPrefix(uri);
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        writer.setPrefix(prefix, uri);
    }

    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
        writer.setDefaultNamespace(uri);
    }

    @Override
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        writer.setNamespaceContext(context);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return writer.getNamespaceContext();
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return writer.getProperty(name);
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

	public void writeDate(Date date) throws XmlWriteException {
		try {
			this.writeCharacters(dateProvider.format(date, writerContext.granularity));
		} catch (XMLStreamException e) {
			throw new XmlWriteException(e);
		}
	}

	public void writeDate(Date date, Granularity granularity) throws XmlWriteException {
		try {
			this.writeCharacters(dateProvider.format(date, granularity));
		} catch (XMLStreamException e) {
			throw new XmlWriteException(e);
		}
	}

	public void writeElement(String elementName, String elementValue) throws XmlWriteException {
		try {
			this.writeStartElement(elementName);
			this.writeCharacters(elementValue);
			this.writeEndElement();
		} catch (XMLStreamException e) {
			throw new XmlWriteException(e);
		}
	}

	public void writeElement(String elementName, XmlWritable writable) throws XmlWriteException {
		try {
			if (writable != null) {
				this.writeStartElement(elementName);
				writable.write(this);
				this.writeEndElement();
			}
		} catch (XMLStreamException e) {
			throw new XmlWriteException(e);
		}
	}

	public void writeElement(String elementName, Date date, Granularity granularity) throws XmlWriteException {
		this.writeElement(elementName, dateProvider.format(date, granularity));
	}

	public void writeElement(String elementName, Date date) throws XmlWriteException {
		this.writeElement(elementName, dateProvider.format(date, writerContext.granularity));
	}

	public void writeAttribute(String name, Date date) throws XmlWriteException {
		try {
			this.writeAttribute(name, dateProvider.format(date, writerContext.granularity));
		} catch (XMLStreamException e) {
			throw new XmlWriteException(e);
		}
	}

	public void writeAttribute(String name, Date value, Granularity granularity) throws XmlWriteException {
		try {
			this.writeAttribute(name, dateProvider.format(value, granularity));
		} catch (XMLStreamException e) {
			throw new XmlWriteException(e);
		}
	}

	public void write(XmlWritable writable) throws XmlWriteException {
		if (writable != null)
			writable.write(this);
	}

	public void write(ResumptionToken.Value value) throws XmlWriteException {
		try {
			if (!value.isEmpty())
				writeCharacters(writerContext.formatter.format(value));
		} catch (XMLStreamException e) {
			throw new XmlWriteException(e);
		}
	}

	public static class WriterContext {
		private final Granularity granularity;
		private final ResumptionTokenFormat formatter;

		public WriterContext(Granularity granularity, ResumptionTokenFormat formatter) {
			this.granularity = granularity;
			this.formatter = formatter;
		}
	}
}
