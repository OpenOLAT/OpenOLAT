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
package org.olat.core.commons.services.robots;

import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.robots.model.SitemapItem;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.oaipmh.common.exceptions.XmlWriteException;
import org.olat.modules.oaipmh.common.xml.XmlWritable;
import org.olat.modules.oaipmh.common.xml.XmlWriter;

/**
 * 
 * Initial date: 17 Oct 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class SitemapWriter {
	
	private static final Logger log = Tracing.createLoggerFor(SitemapWriter.class);
	
	private final List<SitemapItem> items;

	public SitemapWriter(List<SitemapItem> items) {
		this.items = items;
	}
	
	public String getSitemap() {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			XmlWriter writer = new XmlWriter(output);
			
			XMLSitemap xmlSitemap = new XMLSitemap();
			xmlSitemap.write(writer);
			writer.close();
			
			return output.toString();
		} catch (Exception e) {
			log.error("", e);
		}
		return "";
	}
	
	private class XMLSitemap implements XmlWritable {
		
		private static final String NAMESPACE_URI = "http://www.sitemaps.org/schemas/sitemap/0.9";
		private static final String XML_ENCODING = "UTF-8";
		private static final String XML_VERSION = "1.0";
		
		@Override
		public void write(XmlWriter writer) throws XmlWriteException {
			try {
				writer.writeStartDocument(XML_ENCODING, XML_VERSION);
				writer.writeStartElement("urlset");
				writer.writeNamespace("", NAMESPACE_URI);
				
				for (SitemapItem item : items) {
					if (StringHelper.containsNonWhitespace(item.getLoc())) {
						writer.writeStartElement(NAMESPACE_URI, "url");
						
						writer.writeStartElement(NAMESPACE_URI, "loc");
						writer.writeCharacters(item.getLoc());
						writer.writeEndElement();
						
						if (item.getLastModDate() != null) {
							writer.writeStartElement(NAMESPACE_URI, "lastmod");
							writer.writeCharacters(Formatter.formatDatetime(item.getLastModDate()));
							writer.writeEndElement();
						}
						
						if (StringHelper.containsNonWhitespace(item.getChangeFreq())) {
							writer.writeStartElement(NAMESPACE_URI, "changefreq");
							writer.writeCharacters(item.getChangeFreq());
							writer.writeEndElement();
						}
						
						writer.writeEndElement();
					}
				}
				writer.writeEndElement();
			} catch (XMLStreamException e) {
				throw new XmlWriteException(e);
			}
		}
	}

}
