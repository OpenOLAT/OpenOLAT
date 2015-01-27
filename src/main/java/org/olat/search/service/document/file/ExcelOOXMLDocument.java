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
import java.util.HashMap;
import java.util.Map;
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
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
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
public class ExcelOOXMLDocument extends FileDocument {
	private static final long serialVersionUID = 2322994231200065526L;
	private static final OLog log = Tracing.createLoggerFor(ExcelOOXMLDocument.class);

	private static final String SHEET = "xl/worksheets/sheet";
	public final static String EXCEL_FILE_TYPE = "type.file.excel";

	public static Document createDocument(SearchResourceContext leafResourceContext, VFSLeaf leaf) throws IOException, DocumentException,
			DocumentAccessException {
		ExcelOOXMLDocument officeDocument = new ExcelOOXMLDocument();
		officeDocument.init(leafResourceContext, leaf);
		officeDocument.setFileType(EXCEL_FILE_TYPE);
		officeDocument.setCssIcon(CSSHelper.createFiletypeIconCssClassFor(leaf.getName()));
	
		if (log.isDebug()) {
			log.debug(officeDocument.toString());
		}
		return officeDocument.getLuceneDocument();
	}

	@Override
	public FileContent readContent(VFSLeaf leaf) throws IOException, DocumentException {
		//first step parse shared strings
		Map<String,String> sharedStrings = parseSharedStrings(leaf);
		//parse sheets
		String content = parseSheets(sharedStrings, leaf);
		
		return new FileContent(content);
	}
	
	
	private String parseSheets(Map<String,String> sharedStrings, VFSLeaf leaf)  throws IOException, DocumentException {
		InputStream stream = null;
		ZipInputStream zip = null;
		try {
			stream = leaf.getInputStream();
			zip = new ZipInputStream(stream);
			ZipEntry entry = zip.getNextEntry();
			
			SlicedDocument doc = new SlicedDocument();
			while (entry != null) {
				String name = entry.getName();
				if(name.startsWith(SHEET) && name.endsWith(".xml")) {
					String position = name.substring(SHEET.length(), name.indexOf(".xml"));
					
					OfficeDocumentHandler dh = new OfficeDocumentHandler(sharedStrings);
					parse(new ShieldInputStream(zip), dh);
					doc.setContent(Integer.parseInt(position), dh.getContent());
				}
				entry = zip.getNextEntry();
			}
			return doc.toStringAndClear();
		} catch (DocumentException e) {
			throw e;
		} catch (Exception e) {
			throw new DocumentException(e.getMessage());
		} finally {
			FileUtils.closeSafely(zip);
			FileUtils.closeSafely(stream);
		}
	}
	
	private Map<String,String> parseSharedStrings( VFSLeaf leaf) throws IOException, DocumentException {
		SharedStringsHandler dh = new SharedStringsHandler();
		
		InputStream stream = null;
		ZipInputStream zip = null;
		try {
			stream = leaf.getInputStream();
			zip = new ZipInputStream(stream);
			ZipEntry entry = zip.getNextEntry();
			while (entry != null) {
				String name = entry.getName();
				if(name.endsWith("xl/sharedStrings.xml")) {
					parse(new ShieldInputStream(zip), dh);
					break;
				}
				entry = zip.getNextEntry();
			}
			return dh.getMap();
		} catch (DocumentException e) {
			throw e;
		} catch (Exception e) {
			throw new DocumentException(e.getMessage());
		} finally {
			FileUtils.closeSafely(zip);
			FileUtils.closeSafely(stream);
		}
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
		
		private boolean row = false;
		private boolean sharedStrings = false;
		private Map<String,String> strings;
		
		public OfficeDocumentHandler(Map<String,String> strings) {
			this.strings = strings;
		}

		public StringBuilder getContent() {
			return sb;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if("row".equals(qName)) {
				row = true;
			} else if (row && "c".equals(qName)) {
				String t = attributes.getValue("t");
				if("s".equals(t)) {
					sharedStrings = true;
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if("row".equals(qName)) {
				row = false;
				if(sb .length() > 0 && sb.charAt(sb.length() - 1) != '\n'){
					sb.append('\n');
				}
			} else if (row && "c".equals(qName)) {
				sharedStrings = false;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) {
			if(sharedStrings) {
				String key = new String(ch, start, length);
				String value = strings.get(key);
				if(value != null) {
					if(sb .length() > 0 && sb.charAt(sb.length() - 1) != ' '){
						sb.append(' ');
					}
					sb.append(value);
				}
			} else {
				if(sb .length() > 0 && sb.charAt(sb.length() - 1) != ' '){
					sb.append(' ');
				}
				sb.append(ch, start, length);
			}
		}
	}
	
	private class SharedStringsHandler extends DefaultHandler {
		private int position = 0;
		private StringBuilder sb;
		private Map<String,String> strings = new HashMap<String,String>();

		public Map<String,String> getMap() {
			return strings;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if("t".equals(qName)) {
				sb = new StringBuilder();
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if("si".equals(qName)) {
				String string = sb.toString().trim();
				strings.put(Integer.toString(position), string);
				position++;
			}
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