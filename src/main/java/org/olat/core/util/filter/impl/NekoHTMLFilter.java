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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.cyberneko.html.parsers.SAXParser;
import org.olat.core.logging.LogDelegator;
import org.olat.core.util.filter.Filter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Description:<br>
 * Filter the HTML code using Neko SAX parser and extract the content.
 * Neko parse the HTML entities too and deliver cleaned text.
 * 
 * <P>
 * Initial Date:  2 dec. 2009 <br>
 * @author srosse
 */
public class NekoHTMLFilter extends LogDelegator implements Filter {
	
	public static final Set<String> blockTags = new HashSet<String>();
	static {
		blockTags.addAll(Arrays.asList("address","blockquote","br","dir","div","dl","fieldset","form","h1","h2","h3","h4","h5","h6","hr","noframes","noscript","ol","p","pre","table","ul","li"));
	}

	@Override
	public String filter(String original) {
		if (original == null) return null;
		try {
			SAXParser parser = new SAXParser();
			HTMLHandler contentHandler = new HTMLHandler((int)((float)original.length() * 0.66f));
			parser.setContentHandler(contentHandler);
			parser.parse(new InputSource(new StringReader(original)));
			return contentHandler.toString();
		} catch (SAXException e) {
			logError("", e);
			return null;
		} catch (IOException e) {
			logError("", e);
			return null;
		} catch (Exception e) {
			logError("", e);
			return null;
		}
	}

	public NekoContent filter(InputStream in) {
		if (in == null) return null;
		try {
			SAXParser parser = new SAXParser();
			HTMLHandler contentHandler = new HTMLHandler((int)(1000 * 0.66f));
			parser.setContentHandler(contentHandler);
			parser.parse(new InputSource(in));
			return contentHandler.getContent();
		} catch (SAXException e) {
			logError("", e);
			return null;
		} catch (IOException e) {
			logError("", e);
			return null;
		} catch (Exception e) {
			logError("", e);
			return null;
		}
	}
	
	public static class NekoContent {
		private final String title;
		private final String content;
		
		public NekoContent(String title, String content) {
			this.title = title;
			this.content = content;
		}
		
		public String getTitle() {
			return title;
		}
		
		public String getContent() {
			return content;
		}
	}
	
	private static class HTMLHandler extends DefaultHandler {
		private boolean collect = true;
		private boolean consumeBlanck = false;
		private boolean consumeTitle = true;
		private final StringBuilder sb;
		private final StringBuilder title;
		
		public HTMLHandler(int size) {
			sb = new StringBuilder(size);
			title = new StringBuilder(32);
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			String elem = localName.toLowerCase();
			if("script".equals(elem)) {
				collect = false;
			// add a single whitespace before each block element but only if not there is not already a whitespace there
			} else {
				if("title".equals(elem)) {
					consumeTitle = true;
				}
				if(blockTags.contains(elem) && sb.length() > 0 && sb.charAt(sb.length() -1) != ' ' ) {
					consumeBlanck = true;
				}
			}
		}
		
		@Override
		public void characters(char[] chars, int offset, int length) {
			if(collect) {
				if(consumeBlanck) {
					if(sb.length() > 0 && sb.charAt(sb.length() -1) != ' ' && length > 0 && chars[offset] != ' ') { 
						sb.append(' ');
					}
					consumeBlanck = false;
				}
				sb.append(chars, offset, length);
				if(consumeTitle) {
					title.append(chars, offset, length);
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) {
			String elem = localName.toLowerCase();
			if("script".equals(elem)) {
				collect = true;
			} else {
				if("title".equals(elem)) {
					consumeTitle = false;
				}
				if(blockTags.contains(elem) && sb.length() > 0 && sb.charAt(sb.length() -1) != ' ' ) {
					consumeBlanck = true;
				}
			}
		}

		public NekoContent getContent() {
			return new NekoContent(title.toString(), sb.toString());
		}
		
		@Override
		public String toString() {
			return sb.toString();
		}
	}
}
