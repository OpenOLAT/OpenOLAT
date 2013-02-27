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
package org.olat.ims.qti.fileresource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.ims.resources.IMSEntityResolver;
import org.olat.search.service.document.file.utils.ShieldInputStream;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
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
	
	private static final OLog log = Tracing.createLoggerFor(ItemFileResourceValidator.class);

	public boolean validate(String filename, File file) {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			return validate(filename, in);
		} catch (FileNotFoundException e) {
			return false;
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	public boolean validate(String filename, VFSLeaf file) {
		InputStream in = null;
		try {
			in = file.getInputStream();
			return validate(filename, in);
		} catch (Exception e) {
			return false;
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	public boolean validate(String filename, InputStream in) {
		boolean valid = false;

		if(filename.toLowerCase().endsWith(".xml")) {
			valid = validateXml(in);
			IOUtils.closeQuietly(in);
		} else if(filename.toLowerCase().endsWith(".zip")) {
			ZipInputStream oZip = new ZipInputStream(in);
			try {
				ZipEntry oEntr = oZip.getNextEntry();
				while (oEntr != null) {
					if (!oEntr.isDirectory()) {
						if(validateXml(new ShieldInputStream(oZip))) {
							valid = true;
						}
					}
					oZip.closeEntry();
					oEntr = oZip.getNextEntry();
				}
			} catch(Exception e) {
				log.error("", e);
				valid = false;
			} finally {
				IOUtils.closeQuietly(oZip);
				IOUtils.closeQuietly(in);
			}
		}
		
		return valid;
	}
	
	private boolean validateXml(InputStream in) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(true);
			factory.setNamespaceAware(true);
			
			SimpleErrorHandler errorHandler = new SimpleErrorHandler();
			ItemContentHandler contentHandler = new ItemContentHandler();
			
			SAXParser parser = factory.newSAXParser();
			XMLReader reader = parser.getXMLReader();
			reader.setEntityResolver(new IMSEntityResolver());
			reader.setErrorHandler(errorHandler);
			reader.setContentHandler(contentHandler);
			reader.parse(new InputSource(in));
			return errorHandler.isValid() && contentHandler.isItem();
		} catch (ParserConfigurationException e) {
			return false;
		} catch (SAXException e) {
			return false;
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
			} else if("item".equals(localName)) {
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
