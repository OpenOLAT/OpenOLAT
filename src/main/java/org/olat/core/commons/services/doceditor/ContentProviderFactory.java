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
package org.olat.core.commons.services.doceditor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.openxml.OpenXMLDocument;
import org.olat.core.util.openxml.OpenXMLDocumentWriter;
import org.olat.core.util.openxml.OpenXMLWorkbook;

/**
 * 
 * Initial date: 20 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ContentProviderFactory {
	
	private static final Logger log = Tracing.createLoggerFor(ContentProviderFactory.class);
	
	private static final ContentProvider EMPTY = new EmptyContentProvider();
	private static final ContentProvider XML = new XmlContentProvider();
	private static final ContentProvider DOCX = new DocxContentProvider();
	private static final ContentProvider XLSX = new XlsxContentProvider();
	private static final ContentProvider PPTX = new PptxContentProvider();
	
	public static ContentProvider empty() {
		return EMPTY;
	}
	
	public static ContentProvider emptyXml() {
		return XML;
	}
	
	
	public static ContentProvider emptyDocx() {
		return DOCX;
	}
	
	public static ContentProvider emptyXlsx() {
		return XLSX;
	}
	
	public static ContentProvider emptyPptx() {
		return PPTX;
	}
	
	private static final class EmptyContentProvider implements ContentProvider {

		private static final byte[] EMPTY_CONTENT = new byte[0];
		
		@Override
		public InputStream getContent(Locale locale) {
			return new ByteArrayInputStream(EMPTY_CONTENT);
		}
	}
	
	private static final class XmlContentProvider implements ContentProvider {

		private static final String XML_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
		
		@Override
		public InputStream getContent(Locale locale) {
			return new ByteArrayInputStream(XML_CONTENT.getBytes(StandardCharsets.UTF_8));
		}
	}
	
	private static final class DocxContentProvider implements ContentProvider {

		@Override
		public InputStream getContent(Locale locale) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ZipOutputStream zout = null;
			try {
				OpenXMLDocument document = new OpenXMLDocument();
				document.setDocumentHeader("");

				zout = new ZipOutputStream(out);
				zout.setLevel(9);
				OpenXMLDocumentWriter writer = new OpenXMLDocumentWriter(locale);
				writer.createDocument(zout, document);
			} catch (Exception e) {
				log.error("", e);
			} finally {
				if(zout != null) {
					try {
						zout.finish();
					} catch (IOException e) {
						log.error("", e);
					}
				}
			}
			return new ByteArrayInputStream(out.toByteArray());
		}
	}
	
	private static final class XlsxContentProvider implements ContentProvider {

		@Override
		public InputStream getContent(Locale locale) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
				workbook.nextWorksheet();
			} catch (Exception e) {
				log.error("", e);
			}
			return new ByteArrayInputStream(out.toByteArray());
		}
	}
	
	private static final class PptxContentProvider implements ContentProvider {

		@Override
		public InputStream getContent(Locale locale) {
			URL url = PptxContentProvider.class.getResource("empty.pptx");
			try {
				return new FileInputStream(url.getFile());
			} catch (FileNotFoundException e) {
				log.error("", e);
			}
			return new EmptyContentProvider().getContent(locale);
		}
	}

}
