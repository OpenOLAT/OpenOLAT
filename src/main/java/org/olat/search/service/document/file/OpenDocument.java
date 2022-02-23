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

import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.logging.Tracing;
import org.olat.core.util.io.LimitedContentWriter;
import org.olat.core.util.io.ShieldInputStream;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.xml.XMLFactories;
import org.olat.search.service.SearchResourceContext;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * Description:<br>
 * A simple parser for Open Document files. It take the content and use
 * a SAXParser to collect the text within.
 * 
 * <P>
 * Initial Date:  14 dec. 2009 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class OpenDocument extends FileDocument {
	private static final long serialVersionUID = 7285894180135411850L;
	private static final Logger log = Tracing.createLoggerFor(OpenDocument.class);
	
	private static final int MAX_ENTRIES = 64;
	
	public static final String TEXT_FILE_TYPE = "type.file.odt";
	public static final String SPEADSHEET_FILE_TYPE = "type.file.ods";
	public static final String PRESENTATION_FILE_TYPE = "type.file.odp";
	public static final String FORMULA_FILE_TYPE = "type.file.odf";
	public static final String GRAPHIC_FILE_TYPE = "type.file.odg";

	public static Document createDocument(SearchResourceContext leafResourceContext, VFSLeaf leaf)
	throws IOException, DocumentException, DocumentAccessException {
		OpenDocument openDocument = new OpenDocument();
		openDocument.init(leafResourceContext, leaf);
		if(leaf.getName().toLowerCase().endsWith(".odt")) {
			openDocument.setFileType(TEXT_FILE_TYPE);
		} else if(leaf.getName().toLowerCase().endsWith(".ods")) {
			openDocument.setFileType(SPEADSHEET_FILE_TYPE);
		} else if(leaf.getName().toLowerCase().endsWith(".odp")) {
			openDocument.setFileType(PRESENTATION_FILE_TYPE);
		} else if(leaf.getName().toLowerCase().endsWith(".odg")) {
			openDocument.setFileType(GRAPHIC_FILE_TYPE);
		} else if(leaf.getName().toLowerCase().endsWith(".odf")) {
			openDocument.setFileType(FORMULA_FILE_TYPE);
		}
		openDocument.setCssIcon(CSSHelper.createFiletypeIconCssClassFor(leaf.getName()));

		if (log.isDebugEnabled()) log.debug(openDocument.toString());
		return openDocument.getLuceneDocument();
	}

	public FileContent readContent(VFSLeaf leaf) throws DocumentException {
		final OpenDocumentHandler dh = new OpenDocumentHandler();

		int count = 0;
		try(InputStream stream = leaf.getInputStream();
				ZipInputStream zip = new ZipInputStream(stream)) {
			ZipEntry entry = zip.getNextEntry();
			while (entry != null && count < MAX_ENTRIES) {
				if (entry.getName().endsWith("content.xml")) {
					parse(new ShieldInputStream(zip), dh);
					break;//we parsed only content
				}
				entry = zip.getNextEntry();
				count++;
			}
		} catch (DocumentException e) {
			throw e;
		} catch (Exception e) {
			throw new DocumentException(e.getMessage());
		}
		return new FileContent(dh.getContent());
	}
	
	private void parse(InputStream stream, DefaultHandler handler) throws DocumentException {
		try {
			XMLReader parser = XMLFactories.newSAXParser().getXMLReader();
			parser.setContentHandler(handler);
			parser.setEntityResolver(handler);
			parser.parse(new InputSource(stream));
		} catch(StopSAXException e) {
			//
		} catch (Exception e) {
			throw new DocumentException("XML parser configuration error", e);
		}
	}
	
	private class OpenDocumentHandler extends DefaultHandler {

		private final LimitedContentWriter sb = new LimitedContentWriter(5000, FileDocumentFactory.getMaxFileSize());

		@Override
		public InputSource resolveEntity(String publicId, String systemId) {
			if("//OpenOffice.org//DTD Modified W3C MathML 1.01//EN".equals(publicId) ||
					systemId.endsWith("math.dtd")) {
				InputStream dtdStream = OpenDocument.this.getClass().getResourceAsStream("_resources/math.dtd");
				return new InputSource(dtdStream);
			}
			return null;
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if(sb .length() > 0 && sb.charAt(sb.length() - 1) != ' '){
				sb.append(' ');
			}
			sb.write(ch, start, length);
			if(!sb.accept()) {
				throw new StopSAXException();
			}
		}
		
		public String getContent() {
			return sb.toString();
		}
	}
}
