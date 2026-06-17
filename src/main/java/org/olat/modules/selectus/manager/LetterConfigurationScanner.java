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
package org.olat.modules.selectus.manager;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.olat.modules.selectus.model.letter.LetterLanguageConfiguration;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;

/**
 * 
 * Initial date: 14 avr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LetterConfigurationScanner {
	
	private static final Logger log = Tracing.createLoggerFor(LetterConfigurationScanner.class);
	
	public String render(String original, LetterLanguageConfiguration configuration) {
		if (original == null) return null;
		
		try {
			HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
			HTMLHandler contentHandler = new HTMLHandler(configuration);
			parser.setContentHandler(contentHandler);
			parser.parse(new InputSource(new StringReader(original)));
			return contentHandler.toString();
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

	private static class HTMLHandler extends DefaultHandler {
		
		private boolean blockCharacters = false;
		private boolean blockImages = false;
		private final Writer writer = new StringWriter();
		
		private final LetterLanguageConfiguration configuration;
		
		public HTMLHandler(LetterLanguageConfiguration configuration) {
			this.configuration = configuration;
		}

		@Override
		public String toString() {
			return writer.toString();
		}

		@Override
	    public void startDocument() throws SAXException {
	        try {
	            writer.write("<!DOCTYPE html>\n");
	        } catch (IOException e) {
	            throw new SAXException(e);
	        }
	    }

		@Override
		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
	        try {
	        	if("IMG".equalsIgnoreCase(localName) && blockImages) {
	 				return;
	 			}
	        	
	            writer.write('<');
	            writer.write(localName);
	            for (int i = 0; i < atts.getLength(); i++) {
	                String attUri = atts.getURI(i);
	                if (attUri.length() == 0) {
	                    writer.write(' ');
	                } else {
	                    continue;
	                }
	                writer.write(atts.getLocalName(i));
	                writer.write('=');
	                writer.write('"');
	                String val = atts.getValue(i);
	                for (int j = 0; j < val.length(); j++) {
	                    char c = val.charAt(j);
	                    switch (c) {
	                        case '"':
	                            writer.write("&quot;");
	                            break;
	                        case '&':
	                            writer.write("&amp;");
	                            break;
	                        case '\u00A0':
	                            writer.write("&nbsp;");
	                            break;
	                        default:
	                            writer.write(c);
	                            break;
	                    }
	                }
	                writer.write('"');
	            }
	            writer.write('>');
	            
	            if("DIV".equalsIgnoreCase(localName)) {
					String variable = atts.getValue("data-sel-variable");
					String type = atts.getValue("data-sel-type");
					if(StringHelper.containsNonWhitespace(variable)) {
						String val = configuration.getValue(variable);
						if(StringHelper.containsNonWhitespace(val)) {
							if("image".equals(type)) {
								writer.append("<img src=\"");
								if(!val.startsWith("data:image")) {
									writer.append("data:image/jpeg;base64,");
								}
								writer.append(val);
								writer.append("\" />");
								blockImages = true;
							} else {
								writer.write(val);
							}
							blockCharacters = true;
						}
					}
				}
	        } catch (IOException e) {
	            throw new SAXException(e);
	        }
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			try {
				if(!blockCharacters) {
					String text = new String(ch, start, length);
					StringHelper.escapeHtml(writer, text);
				}
			} catch (Exception e) {
	            throw new SAXException(e);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if("DIV".equalsIgnoreCase(localName) && blockCharacters) {
				blockCharacters = false;
				blockImages = false;
			} else if("BR".equalsIgnoreCase(localName)
					|| "IMG".equalsIgnoreCase(localName)
					|| "META".equalsIgnoreCase(localName)) {
				return;
			}
			
			try {
				writer.write('<');
				writer.write('/');
				writer.write(localName);
				writer.write('>');
			} catch (IOException e) {
				throw new SAXException(e);
			}
		}

		@Override
		public void endDocument() throws SAXException {
			try {
				writer.flush();
				writer.close();
			} catch (IOException e) {
				throw new SAXException(e);
			}
		}
	}
}
