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
package org.olat.fileresource.types;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.SAXParser;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.xml.XMLFactories;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A very simple handler for SAX event to detect some markup specific
 * to some key resources as glossary or course.
 * 
 * 
 * Initial date: 12.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class XMLScanner extends DefaultHandler {
	
	private static final Logger log = Tracing.createLoggerFor(XMLScanner.class);
	
	private static SAXParser saxParser;
	static {
		try {
			saxParser = XMLFactories.newSAXParser();
		} catch(Exception ex) {
	  		log.error("", ex);
		}
	}
	
	private boolean glossaryListMarkup;
	private boolean editorTreeModelMarkup;

	public void scan(Path file) {
		if(file == null) return;
		
		try(InputStream in = Files.newInputStream(file)) {
		  	synchronized(saxParser) {
		  		saxParser.parse(in, this);
		  	}
		} catch (SAXParseException ex) {
		  	log.warn("SAX Parser error while parsing " + file, ex);
		} catch(Exception ex) {
		  	log.error("Error while parsing " + file, ex);
		}
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if("list".equals(qName)) {
			glossaryListMarkup = true;
		} else if("org.olat.course.tree.CourseEditorTreeModel".equals(qName)) {
			editorTreeModelMarkup = true;
		}
		super.startElement(uri, localName, qName, attributes);
	}

	public boolean hasGlossaryListMarkup() {
		return glossaryListMarkup;
	}
	
	public boolean hasEditorTreeModelMarkup() {
		return editorTreeModelMarkup;
	}
}
