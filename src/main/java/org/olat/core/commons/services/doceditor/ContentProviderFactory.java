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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
	private static final ContentProvider DRAWIO = new DrawioContentProvider();
	private static final ContentProvider DRAWIOWB = new DrawioContentProvider();
	private static final ContentProvider DRAWIOSVG = new DrawioSvgContentProvider();
	private static final ContentProvider PNG = new PngContentProvider();
	
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
	
	public static ContentProvider emptyDrawio() {
		return DRAWIO;
	}
	
	public static ContentProvider emptyDrawiowb() {
		return DRAWIOWB;
	}
	
	public static ContentProvider emptyDrawioSvg() {
		return DRAWIOSVG;
	}
	
	public static ContentProvider emptyPng() {
		return PNG;
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
			try {
				return PptxContentProvider.class.getResourceAsStream("empty.pptx");
			} catch (Exception e) {
				log.error("", e);
			}
			return new EmptyContentProvider().getContent(locale);
		}
	}
	
	private static final class DrawioContentProvider implements ContentProvider {
		
		// https://github.com/jgraph/drawio-nextcloud/blob/2a251b5d8e8418f2f38ffdff661eba871445281a/src/editor.js
		private static final String DRAWIO_CONTENT = "<mxGraphModel><root><mxCell id=\"0\"/><mxCell id=\"1\" parent=\"0\"/></root></mxGraphModel>";
		
		@Override
		public InputStream getContent(Locale locale) {
			return new ByteArrayInputStream(DRAWIO_CONTENT.getBytes(StandardCharsets.UTF_8));
		}
	}
	
	private static final class DrawioSvgContentProvider implements ContentProvider {
		
		private static final String DRAWIOSVG_CONTENT = "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\" width=\"1px\" height=\"1px\" viewBox=\"-0.5 -0.5 1 1\" content=\"&lt;mxfile &gt;&lt;diagram name=&quot;Page-1&quot; &gt;ddHNEoIgEADgp+GuUGZnM7t08tCZkU2YQddBGq2nTwfJGOvE8u3C8kNY1oyF4Z28ogBNaCRGwk6E0jhKdtMwy9PLPnVSGyUWW6FUL/CFiz6UgD4otIjaqi7ECtsWKhsYNwaHsOyOOuza8Ro2UFZcb/WmhJVOU3pY/QKqlr5znBxdpuG+eLlJL7nA4YtYTlhmEK2LmjEDPb+efxe37vwn+zmYgdb+WDAF697TJPgilr8B&lt;/diagram&gt;&lt;/mxfile&gt;\"><defs/><g/></svg>";
		
		@Override
		public InputStream getContent(Locale locale) {
			return new ByteArrayInputStream(DRAWIOSVG_CONTENT.getBytes(StandardCharsets.UTF_8));
		}
	}
	
	private static final class PngContentProvider implements ContentProvider {
		
		// 1px x 1px, transparent
		private static final byte[] PNG_CONTENT = Base64.getDecoder().decode(
				"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAC0lEQVQIW2NgAAIAAAUAAR4f7BQAAAAASUVORK5CYII=");
		
		@Override
		public InputStream getContent(Locale locale) {
			return new ByteArrayInputStream(PNG_CONTENT);
		}
	}

}
