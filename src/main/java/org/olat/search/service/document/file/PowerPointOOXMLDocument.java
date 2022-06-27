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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.logging.Tracing;
import org.olat.core.util.io.LimitedContentWriter;
import org.olat.core.util.io.ShieldInputStream;
import org.olat.core.util.vfs.JavaIOItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.xml.XMLFactories;
import org.olat.search.service.SearchResourceContext;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

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
	private static final Logger log = Tracing.createLoggerFor(PowerPointOOXMLDocument.class);

	private static final int MAX_ENTRIES = 64;
	
	public static final String POWERPOINT_FILE_TYPE = "type.file.ppt";
	private static final String SLIDE = "ppt/slides/slide";

	public static Document createDocument(SearchResourceContext leafResourceContext, VFSLeaf leaf)
	throws IOException, DocumentException, DocumentAccessException {
		PowerPointOOXMLDocument officeDocument = new PowerPointOOXMLDocument();
		officeDocument.init(leafResourceContext, leaf);
		officeDocument.setFileType(POWERPOINT_FILE_TYPE);
		officeDocument.setCssIcon(CSSHelper.createFiletypeIconCssClassFor(leaf.getName()));
		if (log.isDebugEnabled()) {
			log.debug(officeDocument.toString());
		}
		return officeDocument.getLuceneDocument();
	}

	@Override
	public FileContent readContent(VFSLeaf leaf) throws IOException, DocumentException {
		File file = ((JavaIOItem)leaf).getBasefile();
		try(LimitedContentWriter writer = new LimitedContentWriter(100000, FileDocumentFactory.getMaxFileSize());
				ZipFile wordFile = new ZipFile(file)) {
			int count = 0;
			List<String> contents = new ArrayList<>();
			for(Enumeration<? extends ZipEntry> entriesEnumeration=wordFile.entries(); entriesEnumeration.hasMoreElements() && count<MAX_ENTRIES; count++) {
				ZipEntry entry = entriesEnumeration.nextElement();
				String name = entry.getName();
				if(name.startsWith(SLIDE) && name.endsWith(".xml")) {
					contents.add(name);
				}
			}

			if(contents.size() > 1) {
				Collections.sort(contents, new PowerPointDocumentComparator());
			}
			
			for(String content:contents) {
				if(writer.accept()) {
					ZipEntry entry = wordFile.getEntry(content);
					InputStream zip = wordFile.getInputStream(entry);
					OfficeDocumentHandler dh = new OfficeDocumentHandler(writer);
					parse(new ShieldInputStream(zip), dh);
					zip.close();
				}
			}

			return new FileContent(writer.toString());
		} catch (DocumentException e) {
			throw e;
		} catch (Exception e) {
			throw new DocumentException(e.getMessage());
		}
	}
	
	private void parse(InputStream stream, DefaultHandler handler) throws DocumentException {
		try {
			XMLReader parser = XMLFactories.newSAXParser().getXMLReader();
			parser.setContentHandler(handler);
			parser.setEntityResolver(handler);
			parser.parse(new InputSource(stream));
		} catch (Exception e) {
			throw new DocumentException("XML parser configuration error", e);
		}
	}
	
	private static class OfficeDocumentHandler extends DefaultHandler {
		private final LimitedContentWriter sb;

		public OfficeDocumentHandler(LimitedContentWriter sb) {
			this.sb = sb;
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
			sb.write(ch, start, length);
		}
	}
	
	public static class PowerPointDocumentComparator extends AbstractOfficeDocumentComparator  {

		@Override
		public int compare(String f1, String f2) {
			int c = 0;
			if(f1.startsWith(SLIDE) && f2.startsWith(SLIDE)) {
				c = comparePosition(f1, f2, SLIDE);
			} else if(f1.startsWith(SLIDE)) {
				c = 1;
			} else if(f2.startsWith(SLIDE)) {
				c = -1;
			}
			
			if(c == 0) {
				c = f1.compareTo(f2);
			}
			return -c;
		}
		
	}
}