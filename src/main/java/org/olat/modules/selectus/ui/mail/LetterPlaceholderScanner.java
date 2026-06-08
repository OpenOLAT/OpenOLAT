/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.mail;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;

/**
 * Detect the tag math and a CSS clas named math
 * 
 * 
 * Initial date: 23 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LetterPlaceholderScanner {
	
	private static final Logger log = Tracing.createLoggerFor(LetterPlaceholderScanner.class);
	

	public List<LetterPlaceholder> scan(String original) {
		if (original == null) return Collections.emptyList();
		
		try {
			HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
			HTMLHandler contentHandler = new HTMLHandler();
			parser.setContentHandler(contentHandler);
			parser.parse(new InputSource(new StringReader(original)));
			return contentHandler.getPlaceholders();
		} catch (Exception e) {
			log.error("", e);
			return Collections.emptyList();
		}
	}

	private static class HTMLHandler extends DefaultHandler {

		private final List<LetterPlaceholder> placeholders = new ArrayList<>();
		
		private CollectMode collect = CollectMode.none;
		private LetterPlaceholder currentPlaceholder;
		
		public List<LetterPlaceholder> getPlaceholders() {
			return placeholders;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if("DIV".equalsIgnoreCase(localName)) {
				String id = attributes.getValue("id");
				String type = attributes.getValue("data-sel-type");
				String label = attributes.getValue("data-sel-label");
				String variable = attributes.getValue("data-sel-variable");
				String mandatory = attributes.getValue("data-sel-mandatory");
				if(StringHelper.containsNonWhitespace(id) && StringHelper.containsNonWhitespace(variable)) {
					currentPlaceholder = new LetterPlaceholder(id, variable, type, label, !"false".equals(mandatory));
					placeholders.add(currentPlaceholder);
					if("formatted".equals(type)) {
						collect = CollectMode.formatted;
					} else {
						collect = CollectMode.simple;
					}
				}
			} else if("IMG".equalsIgnoreCase(localName) && collect != CollectMode.none && currentPlaceholder != null) {
				String src = attributes.getValue("src");
				src = src.replace("data:image/jpeg;base64,", "");
				currentPlaceholder.setDefaultValue(src);
			} else if(collect != CollectMode.none && currentPlaceholder != null) {
				if(collect == CollectMode.formatted) {
					currentPlaceholder.appendDefaultValue("<");
					currentPlaceholder.appendDefaultValue(localName);
					currentPlaceholder.appendDefaultValue(">");
				} else if("P".equalsIgnoreCase(localName) || "BR".equalsIgnoreCase(localName)) {
					currentPlaceholder.appendDefaultValue("\n");
				}
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if(currentPlaceholder != null) {
				if(collect == CollectMode.formatted) {
					currentPlaceholder.appendDefaultValue(new String(ch, start, length));
				} else if(collect == CollectMode.simple) {
					String text = new String(ch, start, length);
					currentPlaceholder.appendDefaultValue(text.trim());
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if("DIV".equalsIgnoreCase(localName)) {
				collect = CollectMode.none;
			} else if(collect == CollectMode.formatted && currentPlaceholder != null) {
				currentPlaceholder.appendDefaultValue("</");
				currentPlaceholder.appendDefaultValue(localName);
				currentPlaceholder.appendDefaultValue(">");
			}
		}
	}
	
	public enum CollectMode {
		none,
		simple,
		formatted
	}
}
