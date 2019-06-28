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
package org.olat.core.gui.components.form.flexible.impl.elements.richText;

import java.io.StringReader;
import java.util.ArrayList;
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
 * 
 * Initial date: 19 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum TextMode {
	//order is important, from the simplest to the sophisticated
	oneLine,
	multiLine,
	formatted;

	private static final Logger log = Tracing.createLoggerFor(TextMode.class);
	
	public static TextMode guess(String text) {
		if(StringHelper.containsNonWhitespace(text)) {
			try {
				TextAnalyser analyser = new TextAnalyser();
				parse(text, analyser);
				if(analyser.isOtherTags()) {
					return formatted;
				} else if(analyser.isBr()) {
					return multiLine;
				} else if(analyser.getP() > 1) {
					return multiLine;
				}
				return oneLine;
			} catch (Exception e) {
				log.error("", e);
				return formatted;
			}
		}
		return oneLine;
	}
	
	public static String toMultiLine(String text) {
		if(StringHelper.containsNonWhitespace(text)) {
			try {
				LineExtractor handler = new LineExtractor("\n");
				parse(text, handler);
				return handler.getText();
			} catch (Exception e) {
				log.error("", e);
				return text;
			}
		}
		return text;
	}
	
	public static String fromMultiLine(String text) {
		String formattedTex;
		if(StringHelper.containsNonWhitespace(text)) {
			StringBuilder sb = new StringBuilder(text.length() * 2);
			String[] lines = text.split("\r?\n");
			for(String line:lines) {
				sb.append("<p>").append(line).append("</p>");
			}
			formattedTex = sb.toString();
		} else {
			formattedTex = "";
		}
		return formattedTex;
	}
	
	public static String toOneLine(String text) {
		if(StringHelper.containsNonWhitespace(text)) {
			try {
				LineExtractor handler = new LineExtractor(" ");
				parse(text, handler);
				return handler.getText();
			} catch (Exception e) {
				log.error("", e);
				return text;
			}
		}
		return text;
	}
	
	public static String fromOneLine(String text) {
		if(StringHelper.containsNonWhitespace(text)) {
			return "<p>" + text.trim() + "</p>";
		}
		return "";
	}
	
	private static void parse(String text, DefaultHandler handler) throws Exception {
		HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
		parser.setContentHandler(handler);
		parser.parseFragment(new InputSource(new StringReader(text)), "");
	}
	
	private static final class TextAnalyser extends DefaultHandler {

		private int p = 0;
		private boolean br = false;
		private boolean otherTags = false;

		public int getP() {
			return p;
		}
		
		public boolean isBr() {
			return br;
		}

		public boolean isOtherTags() {
			return otherTags;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
		throws SAXException {
			if("p".equalsIgnoreCase(qName)) {
				p++;
			} else if("br".equalsIgnoreCase(qName)) {
				br = true;
			} else  {
				otherTags = true;
			}
		}
	}
	
	
	private static final class LineExtractor extends DefaultHandler {
		
		private final String separator;
		private StringBuilder sb = new StringBuilder();
		private List<String> lines = new ArrayList<>();
		
		public LineExtractor(String separator) {
			this.separator = separator;
		}

		public String getText() {
			StringBuilder content = new StringBuilder(1024);
			if(sb.length() > 0) {
				content.append(sb);
			}
			for(String line:lines) {
				line = line.trim();
				if(StringHelper.containsNonWhitespace(line)) {
					if(content.length() > 0) content.append(separator);
					content.append(line);
				}	
			}
			return content.toString();
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
		throws SAXException {
			if("p".equalsIgnoreCase(qName) || "br".equalsIgnoreCase(qName)) {
				lines.add(sb.toString());
				sb = new StringBuilder(128);
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if(start >= 0 && length > 0) {
				sb.append(ch, start, length);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if("p".equalsIgnoreCase(qName)) {
				lines.add(sb.toString());
				sb = new StringBuilder(128);
			}
		}
	}
}
