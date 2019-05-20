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
import org.olat.core.util.StringHelper;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;

/**
 * Detect tags.
 * 
 * Initial date: 10.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HtmlScanner {
	
	private static final Logger log = Tracing.createLoggerFor(HtmlScanner.class);

	public boolean scan(String original) {
		if (original == null) return false;
		
		try {
			HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALLOW);
			parser.setCheckingNormalization(false);
			HTMLHandler contentHandler = new HTMLHandler();
			parser.setContentHandler(contentHandler);
			parser.setErrorHandler(contentHandler);
			parser.parseFragment(new InputSource(new StringReader(original)), "");
			return contentHandler.tagFound();
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}

	private static class HTMLHandler extends DefaultHandler {
		private boolean tagFound = false;
		
		public boolean tagFound() {
			return tagFound;
		}

		@Override
		public void error(SAXParseException exception) throws SAXException {
			String msg = exception.getMessage();
			if(msg != null && (msg.equals("Stray start tag \u201Chtml\\u201D.") || msg.equals("Stray start tag \u201Cbody\u201D."))) {
				tagFound = true;
			}
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if(!tagFound && StringHelper.containsNonWhitespace(localName) && isTagAllowed(localName)) {
				tagFound = true;
			}
		}
		
		private boolean isTagAllowed(String localName) {
			switch(localName) {
				case "textEntryInteraction": return false;
				case "textentryinteraction": return false;
				case "TEXTENTRYINTERACTION": return false;
				default: return true;
			}
		}
	}
}
