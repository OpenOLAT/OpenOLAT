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

import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.io.LimitedContentWriter;
import org.olat.search.service.document.file.FileDocumentFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;

/**
 * Description:<br>
 * Filter the HTML code using HtmlParser SAX parser and extract the content.
 * It parses the HTML entities too and deliver cleaned text.
 * 
 * <P>
 * Initial Date:  2 dec. 2009 <br>
 * @author srosse
 */
public class HtmlFilter implements Filter {
	private static final Logger log = Tracing.createLoggerFor(HtmlFilter.class);
	
	public static final Set<String> blockTags = new HashSet<>();
	public static final Set<String> toBeSkippedTags = new HashSet<>();
	static {
		blockTags.addAll(Arrays.asList("address","blockquote","br","dir","div","dl","fieldset","form","h1","h2","h3","h4","h5","h6","hr","noframes","noscript","ol","p","pre","table","ul","li"));
		toBeSkippedTags.addAll(Arrays.asList("script","style"));
	}

	@Override
	public String filter(String original) {
		return filter(original, false);
	}
	
	public String filter(String original, boolean pretty) {
		if(original == null) return null;
		if(original.isEmpty()) return "";
		
		try {
			HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
			HtmlHandler contentHandler = new HtmlHandler((int)(original.length() * 0.66f), pretty);
			parser.setContentHandler(contentHandler);
			parser.parse(new InputSource(new StringReader(original)));
			return contentHandler.toString();
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

	public HtmlContent filter(InputStream in) {
		if (in == null) return null;
		try {
			HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
			HtmlHandler contentHandler = new HtmlHandler((int)(1000 * 0.66f), false);
			parser.setContentHandler(contentHandler);
			parser.parse(new InputSource(in));
			return contentHandler.getContent();
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	public static class HtmlContent {
		private final String title;
		private final LimitedContentWriter content;
		
		public HtmlContent(String title, LimitedContentWriter content) {
			this.title = title;
			this.content = content;
		}
		
		public String getTitle() {
			return title;
		}
		
		public String getContent() {
			return content.toString();
		}
	}
	
	private static class HtmlHandler extends DefaultHandler {
		private boolean collect = true;
		private boolean consumeBlanck = false;
		private boolean consumeTitle = true;
		private final boolean pretty;
		private final LimitedContentWriter content;
		private final StringBuilder title;
		
		public HtmlHandler(int size, boolean pretty) {
			this.pretty = pretty;
			content = new LimitedContentWriter(size, FileDocumentFactory.getMaxFileSize());
			title = new StringBuilder(32);
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			String elem = localName.toLowerCase();
			if(toBeSkippedTags.contains(elem)) {
				collect = false;
			} else {
				if(pretty) {
					// format text line breaks for plain text as rendered in HTML
					if("li".equals(elem)) {
						content.append("\u00B7 ");
					} else if("ul".equals(elem)) {
						// add break before the list start (for correct rendering of first list element)
						content.append('\n');
					} else if("br".equals(elem)) {
						content.append('\n');
					} else if("p".equals(elem)) {
						// for p tags: line break before and after
						content.append('\n');
					} else if("h2".equals(elem) || "h3".equals(elem) || "h4".equals(elem) || "h5".equals(elem) || "h6".equals(elem)) {
						// for h tags: line break before and after. For H1 which is usually the start of the page omit the trailing return.
						content.append("\n\n");
					}
					// preserve links
					if ("a".equals(elem)) {
						String href = attributes.getValue("href");
						// write absolute url's only
						if (href != null && href.startsWith("http")) {							
							content.append(href);
							content.append(" ");
						}
					}
				}
				if("title".equals(elem)) {
					consumeTitle = true;
				}
				// add a single whitespace before each block element but only if there is not already a whitespace 
				if(blockTags.contains(elem) && content.length() > 0 && content.charAt(content.length() -1) != ' ' ) {
					consumeBlanck = true;
				}
			}
		}
		
		@Override
		public void characters(char[] chars, int offset, int length) {
			if(collect) {
				if(consumeBlanck) {
					if(content.length() > 0 && content.charAt(content.length() -1) != ' ' && length > 0 && chars[offset] != ' ' && content.charAt(content.length() -1) != '\n') { 
						// Add space only if there is not already space and we are not right after a line break
						content.append(' ');
					}
					consumeBlanck = false;
				}
				content.write(chars, offset, length);
				if(consumeTitle) {
					title.append(chars, offset, length);
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) {
			String elem = localName.toLowerCase();
			if(toBeSkippedTags.contains(elem)) {
				collect = true;
			} else {
				if(pretty && ("li".equals(elem) || "p".equals(elem) || "h1".equals(elem) || "h2".equals(elem) || "h3".equals(elem) || "h4".equals(elem) || "h5".equals(elem) || "h6".equals(elem) )) {
					// start with new line after paragraph, list item and header elements
					content.append('\n');
				}
				if("title".equals(elem)) {
					consumeTitle = false;
				}
				if(blockTags.contains(elem) && content.length() > 0 && content.charAt(content.length() -1) != ' ' ) {
					consumeBlanck = true;
				}
			}
		}

		public HtmlContent getContent() {
			return new HtmlContent(title.toString(), content);
		}
		
		@Override
		public String toString() {
			return content.toString();
		}
	}
}
