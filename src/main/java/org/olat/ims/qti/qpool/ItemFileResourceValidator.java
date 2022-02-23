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
package org.olat.ims.qti.qpool;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.SAXParser;

import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentType;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXValidator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.io.ShieldInputStream;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.xml.XMLFactories;
import org.olat.ims.resources.IMSEntityResolver;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * Initial date: 27.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ItemFileResourceValidator {
	
	private static final Logger log = Tracing.createLoggerFor(ItemFileResourceValidator.class);

	public boolean validate(String filename, File file) {
		if(file == null || !file.exists()) return false;

		try(InputStream in = new FileInputStream(file)) {
			return validate(filename, in);
		} catch (Exception e) {
			return false;
		}
	}

	public boolean validate(String filename, VFSLeaf file) {
		if(file == null || !file.exists()) return false;
		
		try(InputStream in = file.getInputStream()) {
			return validate(filename, in);
		} catch (Exception e) {
			return false;
		}
	}

	private boolean validate(String filename, InputStream in) {
		boolean valid = false;

		if(filename.toLowerCase().endsWith(".xml")) {
			valid = validateXml(in);
		} else if(filename.toLowerCase().endsWith(".zip")) {
			try(ZipInputStream oZip = new ZipInputStream(in)) {
				ZipEntry oEntr = oZip.getNextEntry();
				while (oEntr != null) {
					if (!oEntr.isDirectory() && validateXml(new ShieldInputStream(oZip))) {
						valid = true;
					}
					oZip.closeEntry();
					oEntr = oZip.getNextEntry();
				}
			} catch(Exception e) {
				log.error("", e);
				valid = false;
			}
		}
		
		return valid;
	}
	
	private boolean validateXml(InputStream in) {
		boolean valid = false;
		Document doc = readDocument(in);
		if(doc != null) {
			DocumentType docType = doc.getDocType();
			if(docType == null) {
				doc.addDocType("questestinterop", null, "ims_qtiasiv1p2p1.dtd");
			}
			valid = validateDocument(doc);
		}
		return valid;
	}
	
	private Document readDocument(InputStream in) {
		try {
			SAXReader reader = SAXReader.createDefault();
			reader.setEntityResolver(new IMSEntityResolver());
			reader.setValidation(false);
			return reader.read(in, "");
		} catch (Exception e) {
			return null;
		}
	}
	
	private boolean validateDocument(Document in) {
		try {
			SimpleErrorHandler errorHandler = new SimpleErrorHandler();
			ItemContentHandler contentHandler = new ItemContentHandler();
			
			SAXParser parser = XMLFactories.newSAXParser(true, true);
			XMLReader reader = parser.getXMLReader();
			reader.setEntityResolver(new IMSEntityResolver());
			reader.setErrorHandler(errorHandler);
			reader.setContentHandler(contentHandler);

			SAXValidator validator = new SAXValidator(reader);
			validator.validate(in);
			
			return errorHandler.isValid() && contentHandler.isItem();
		} catch (Exception e) {
			return false;
		}
	}
	
	private static class SimpleErrorHandler implements ErrorHandler {
		private int error = 0;
		
		public boolean isValid() {
			return error == 0;
		}

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			//
		}

		@Override
		public void error(SAXParseException exception) throws SAXException {
			error++;
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			error++;
		}
	}
	
	private static class ItemContentHandler extends DefaultHandler {
		private boolean interop;
		private boolean item;
		
		public boolean isItem() {
			return item;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
		throws SAXException {	
			
			if("questestinterop".equals(qName)) {
				interop = true;
			} else if("item".equals(localName) || "item".equals(qName)) {
				if(interop) {
					item = true;
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
		throws SAXException {
			if("questestinterop".equals(qName)) {
				interop = false;
			}
		}
	}
}
