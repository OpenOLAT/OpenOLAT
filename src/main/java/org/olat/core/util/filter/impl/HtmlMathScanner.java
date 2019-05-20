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
package org.olat.core.util.filter.impl;

import java.io.StringReader;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
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
public class HtmlMathScanner {
	
	private static final Logger log = Tracing.createLoggerFor(HtmlMathScanner.class);

	public boolean scan(String original) {
		if (original == null) return false;
		
		try {
			HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
			HTMLHandler contentHandler = new HTMLHandler();
			parser.setContentHandler(contentHandler);
			parser.parse(new InputSource(new StringReader(original)));
			return contentHandler.mathFound();
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}

	private static class HTMLHandler extends DefaultHandler {
		private boolean mathFound = false;
		
		public boolean mathFound() {
			return mathFound;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if("MATH".equalsIgnoreCase(localName)) {
				mathFound = true;
			} else if(attributes != null) {
				String css = attributes.getValue("class");
				if(css != null) {
					String[] splited = css.split("\\s+");
					for(String split:splited) {
						if(split.equals("math")) {
							mathFound = true;
						}
					}
				}
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if(!mathFound && WebappHelper.isMathJaxMarkers()) {
				String content = new String(ch, start, length);
				if(content.contains("\\(") || content.contains("\\[") || content.contains("$$")) {
					mathFound = true;
				}
			}
		}
	}
}
