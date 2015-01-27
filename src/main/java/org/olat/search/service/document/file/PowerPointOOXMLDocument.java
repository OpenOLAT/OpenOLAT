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
package org.olat.search.service.document.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.lucene.document.Document;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.io.ShieldInputStream;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.file.utils.SlicedDocument;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * 
 * Description:<br>
 * Parse the Microsoft Office XML document (.pptx, .docx...) with a SAX parser
 * 
 * <P>
 * Initial Date:  5 nov. 2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PowerPointOOXMLDocument extends FileDocument {
	private static final long serialVersionUID = 2322994231200065526L;
	private static final OLog log = Tracing.createLoggerFor(PowerPointOOXMLDocument.class);

	public final static String POWERPOINT_FILE_TYPE = "type.file.ppt";
	private static final String SLIDE = "ppt/slides/slide";

	public static Document createDocument(SearchResourceContext leafResourceContext, VFSLeaf leaf) throws IOException, DocumentException,
			DocumentAccessException {
		PowerPointOOXMLDocument officeDocument = new PowerPointOOXMLDocument();
		officeDocument.init(leafResourceContext, leaf);
		officeDocument.setFileType(POWERPOINT_FILE_TYPE);
		officeDocument.setCssIcon(CSSHelper.createFiletypeIconCssClassFor(leaf.getName()));
		if (log.isDebug()) {
			log.debug(officeDocument.toString());
		}
		return officeDocument.getLuceneDocument();
	}

	@Override
	public FileContent readContent(VFSLeaf leaf) throws IOException, DocumentException {
		SlicedDocument doc = new SlicedDocument();
		
		InputStream stream = null;
		ZipInputStream zip = null;
		try {
			stream = leaf.getInputStream();

			zip = new ZipInputStream(stream);
			ZipEntry entry = zip.getNextEntry();
			while (entry != null) {
				String name = entry.getName();
				if(name.startsWith(SLIDE) && name.endsWith(".xml")) {
					int lastIndex = name.indexOf(".xml");
					String position = name.substring(SLIDE.length(), lastIndex);
					
					OfficeDocumentHandler dh = new OfficeDocumentHandler();
					parse(new ShieldInputStream(zip), dh);
					doc.setContent(Integer.parseInt(position), dh.getContent());
				}
				entry = zip.getNextEntry();
			}
		} catch (DocumentException e) {
			throw e;
		} catch (Exception e) {
			throw new DocumentException(e.getMessage());
		} finally {
			FileUtils.closeSafely(zip);
			FileUtils.closeSafely(stream);
		}
		return new FileContent(doc.toStringAndClear());
	}
	
	private void parse(InputStream stream, DefaultHandler handler) throws DocumentException {
		try {
			XMLReader parser = XMLReaderFactory.createXMLReader();
			parser.setContentHandler(handler);
			parser.setEntityResolver(handler);
			try {
			parser.setFeature("http://xml.org/sax/features/validation", false);
			} catch(Exception e) {
				log.error("Cannot deactivate validation", e);
			}
			parser.parse(new InputSource(stream));
		} catch (Exception e) {
			throw new DocumentException("XML parser configuration error", e);
		}
	}
	
	private class OfficeDocumentHandler extends DefaultHandler {
		private final StringBuilder sb = new StringBuilder();

		public StringBuilder getContent() {
			return sb;
		}

		@Override
		public InputSource resolveEntity(String publicId, String systemId) {
			return null;
		}

		@Override
		public void characters(char[] ch, int start, int length) {
			if(sb .length() > 0 && sb.charAt(sb.length() - 1) != ' '){
				sb.append(' ');
			}
			sb.append(ch, start, length);
		}
	}
}