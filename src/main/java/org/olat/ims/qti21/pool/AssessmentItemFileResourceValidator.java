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
package org.olat.ims.qti21.pool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.io.ShieldInputStream;
import org.olat.core.util.vfs.VFSLeaf;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import uk.ac.ed.ph.jqtiplus.xmlutils.XmlFactories;

/**
 * 
 * Validate an file
 * 
 * Initial date: 05.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemFileResourceValidator {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentItemFileResourceValidator.class);

	public boolean validate(String filename, File file) {
		if(file == null || !file.exists()) return false;

		try(InputStream in = new FileInputStream(file)) {
			return validate(filename, in);
		} catch (IOException e) {
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
				valid = walkZip(oZip);
			} catch(Exception e) {
				log.error("", e);
				valid = false;
			}
		}
		
		return valid;
	}
	
	private boolean walkZip(ZipInputStream oZip) {
		boolean valid = false;
		try {
			ZipEntry oEntr = oZip.getNextEntry();
			while (oEntr != null) {
				if (!oEntr.isDirectory()) {
					String fname = oEntr.getName().toLowerCase();
					if(!"imsmanifest.xml".equals(fname) && fname.endsWith(".xml")) {
						if(validateXml(new ShieldInputStream(oZip))) {
							valid = true;
						}
					} else if(fname.endsWith(".zip")) {
						ZipInputStream subZip = new ZipInputStream(new ShieldInputStream(oZip));
						valid |= walkZip(subZip);
					}
				}
				oZip.closeEntry();
				oEntr = oZip.getNextEntry();
			}
		} catch(Exception e) {
			log.error("", e);
			valid = false;
		}
		return valid;
	}

	private boolean validateXml(InputStream in) {
		try {
			XMLReader reader = XmlFactories.newSAXParser().getXMLReader();

			SimpleErrorHandler errorHandler = new SimpleErrorHandler();
			reader.setErrorHandler(errorHandler);
			AssessmentItemContentHandler contentHandler = new AssessmentItemContentHandler();
			reader.setContentHandler(contentHandler);
			reader.parse(new InputSource(in));

			return errorHandler.isValid() && contentHandler.isAssessmentItem();
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
	
	private static class AssessmentItemContentHandler extends DefaultHandler {
		private boolean assessmentItem;
		
		public boolean isAssessmentItem() {
			return assessmentItem;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
		throws SAXException {	
			if("assessmentItem".equals(qName)) {
				assessmentItem = true;
			}
		}
	}
}
