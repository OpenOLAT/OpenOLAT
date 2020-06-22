/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.fo.manager;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.filter.Filter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;
import nu.validator.htmlparser.sax.HtmlSerializer;

/**
 * 
 * Initial date: 18 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuoterFilter implements Filter {
	
	private static final Logger log = Tracing.createLoggerFor(QuoterFilter.class);
	
	@Override
	public String filter(String original) {
		if(original == null) return null;
		if(original.isEmpty()) return "";
		
		try {
			HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
			Writer writer = new StringWriter();
			QuoteSerializer contentHandler = new QuoteSerializer(writer);
			parser.setContentHandler(contentHandler);
			parser.parse(new InputSource(new StringReader(original)));
			return writer.toString();
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	private class QuoteSerializer extends HtmlSerializer {
		
		public QuoteSerializer(Writer writer) {
			super(writer);
		}

		@Override
		public void startDocument() throws SAXException {
			// no doctype
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
			if("img".equals(localName)) {
				String img = "[" + atts.getValue("src") + "]";
				char[] imgChArr = img.toCharArray();
				characters(imgChArr, 0, imgChArr.length);
				return;
			}
			if(ignore(localName)) {
				return;
			}
			super.startElement(uri, localName, qName, atts);
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if(ignore(localName)) {
				return;
			}
			super.endElement(uri, localName, qName);
		}
		
		private boolean ignore(String localName) {
			return "html".equals(localName) || "head".equals(localName)
					|| "body".equals(localName) || "img".equals(localName);
		}
	}
}