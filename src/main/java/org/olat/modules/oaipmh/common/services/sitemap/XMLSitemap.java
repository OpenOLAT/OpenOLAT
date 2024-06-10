/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.oaipmh.common.services.sitemap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import org.olat.core.util.Formatter;
import org.olat.modules.oaipmh.common.exceptions.XmlWriteException;
import org.olat.modules.oaipmh.common.model.Metadata;
import org.olat.modules.oaipmh.common.xml.XSLPipeline;
import org.olat.modules.oaipmh.common.xml.XmlWritable;
import org.olat.modules.oaipmh.common.xml.XmlWriter;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ResourceInfoDispatcher;

/**
 * Initial date: Feb 01, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class XMLSitemap implements XmlWritable {

	public static final String NAMESPACE_URI = "http://www.sitemaps.org/schemas/sitemap/0.9";
	public static final String XML_ENCODING = "UTF-8";
	public static final String XML_VERSION = "1.0";

	private final List<RepositoryEntry> repositoryEntries;

	public XMLSitemap(List<RepositoryEntry> repositoryEntries) {
		this.repositoryEntries = repositoryEntries;
	}


	@Override
	public void write(XmlWriter writer) throws XmlWriteException {
		String infoUrl = "";
		String lastModDate = "";

		try {
			writer.writeStartDocument(XML_ENCODING, XML_VERSION);
			writer.writeStartElement("urlset");
			writer.writeNamespace("", NAMESPACE_URI);

			for (RepositoryEntry repositoryEntry : repositoryEntries) {
				infoUrl = ResourceInfoDispatcher.getUrl(repositoryEntry.getKey().toString());
				lastModDate = Formatter.formatDatetime(repositoryEntry.getLastModified());

				writer.writeStartElement(NAMESPACE_URI, "url");

				writer.writeStartElement(NAMESPACE_URI, "loc");
				writer.writeCharacters(infoUrl);
				writer.writeEndElement();
				writer.writeStartElement(NAMESPACE_URI, "lastmod");
				writer.writeCharacters(lastModDate);
				writer.writeEndElement();
				writer.writeStartElement(NAMESPACE_URI, "changefreq");
				writer.writeCharacters("weekly");
				writer.writeEndElement();

				writer.writeEndElement();
			}
			writer.writeEndElement();
		} catch (XMLStreamException e) {
			throw new XmlWriteException(e);
		}
	}

	public String getSiteMapXML() {
		String xmlSitemap = "";

		try {
			xmlSitemap = new Metadata(toPipeline().process()).getString();
		} catch (XMLStreamException | TransformerException | IOException | XmlWriteException e) {
			//
		}
		return xmlSitemap;
	}

	private XSLPipeline toPipeline() throws XmlWriteException, XMLStreamException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		XmlWriter writer = new XmlWriter(output);
		Metadata metadata;

		metadata = new Metadata(new XMLSitemap(repositoryEntries));

		metadata.write(writer);
		writer.close();
		return new XSLPipeline(new ByteArrayInputStream(output.toByteArray()), true);
	}
}
