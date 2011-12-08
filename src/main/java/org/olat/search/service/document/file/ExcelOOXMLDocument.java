/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.search.service.document.file;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Iterator;

import org.apache.lucene.document.Document;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.HeaderFooter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.search.service.SearchResourceContext;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTHeaderFooter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Description:<br>
 * Parse the Excel XML document (.xslx) with Apache POI
 * <P>
 * Initial Date: 14 dec. 2009 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class ExcelOOXMLDocument extends FileDocument {
	private static final OLog log = Tracing.createLoggerFor(ExcelOOXMLDocument.class);

	public final static String FILE_TYPE = "type.file.excel";

	public ExcelOOXMLDocument() {
		super();
	}

	public static Document createDocument(SearchResourceContext leafResourceContext, VFSLeaf leaf) throws IOException, DocumentException,
			DocumentAccessException {
		ExcelOOXMLDocument excelDocument = new ExcelOOXMLDocument();
		excelDocument.init(leafResourceContext, leaf);
		excelDocument.setFileType(FILE_TYPE);
		excelDocument.setCssIcon("b_filetype_xls");
		if (log.isDebug()) log.debug(excelDocument.toString());
		return excelDocument.getLuceneDocument();
	}

	protected String readContent(VFSLeaf leaf) throws DocumentException {
		BufferedInputStream bis = null;
		StringBuilder buffy = new StringBuilder();
		try {
			bis = new BufferedInputStream(leaf.getInputStream());

			OPCPackage pkg = OPCPackage.open(bis);
			XSSFReader r = new XSSFReader(pkg);
			SharedStringsTable sst = r.getSharedStringsTable();
			XMLReader parser = XMLReaderFactory.createXMLReader();
			StylesTable styles = r.getStylesTable();
			MySheetHandler handler = new MySheetHandler(buffy, styles, sst);
			parser.setContentHandler(handler);

			for (XSSFReader.SheetIterator it = (XSSFReader.SheetIterator) r.getSheetsData(); it.hasNext();) {
				InputStream sheet = it.next();
				InputSource sheetSource = new InputSource(sheet);
				parser.parse(sheetSource);
				sheet.close();
			}

			return buffy.toString();
		} catch (Exception e) {
			throw new DocumentException(e.getMessage(), e);
		} finally {
			FileUtils.closeSafely(bis);
		}
	}

	enum xssfDataType {
		BOOL, ERROR, FORMULA, INLINESTR, SSTINDEX, NUMBER,
	}

	public class MySheetHandler extends DefaultHandler {
		private final StringBuilder content;

		private StringBuilder buffer;
		private xssfDataType nextDataType;
		private int formatIndex;
		private String formatString;
		private CTHeaderFooter headerFooter = CTHeaderFooter.Factory.newInstance();

		private final SharedStringsTable sst;
		private final StylesTable stylesTable;
		private final DataFormatter formatter = new DataFormatter();

		public MySheetHandler(StringBuilder content, StylesTable styles, SharedStringsTable sst) {
			this.sst = sst;
			this.content = content;
			this.stylesTable = styles;
		}

		@Override
		public void startDocument() {
			headerFooter = CTHeaderFooter.Factory.newInstance();
		}

		@Override
		public void endDocument() {
			append(headerFooter.getFirstHeader());
			append(headerFooter.getOddHeader());
			append(headerFooter.getEvenHeader());

			append(headerFooter.getFirstFooter());
			append(headerFooter.getOddFooter());
			append(headerFooter.getEvenFooter());
		}

		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes) {
			if (name.equals("c")) {
				// c -> cell
				// Set up defaults.
				nextDataType = xssfDataType.NUMBER;
				formatIndex = -1;
				formatString = null;

				String cellType = attributes.getValue("t");
				String cellStyleStr = attributes.getValue("s");
				if ("b".equals(cellType)) {
					nextDataType = xssfDataType.BOOL;
				} else if ("e".equals(cellType)) {
					nextDataType = xssfDataType.ERROR;
				} else if ("inlineStr".equals(cellType)) {
					nextDataType = xssfDataType.INLINESTR;
				} else if ("s".equals(cellType)) {
					nextDataType = xssfDataType.SSTINDEX;
				} else if ("str".equals(cellType)) {
					nextDataType = xssfDataType.FORMULA;
				} else if (cellStyleStr != null) {
					// It's a number, but almost certainly one with a special style or
					// format
					int styleIndex = Integer.parseInt(cellStyleStr);
					XSSFCellStyle style = stylesTable.getStyleAt(styleIndex);
					formatIndex = style.getDataFormat();
					formatString = style.getDataFormatString();
					if (formatString == null) formatString = BuiltinFormats.getBuiltinFormat(formatIndex);
				}
			}
			// Clear contents cache
			buffer = new StringBuilder();
		}

		@Override
		public void endElement(String uri, String localName, String name) {
			if (name.equals("c")) {
				//
			} else if ("v".equals(name)) {
				String thisStr = null;
				switch (nextDataType) {
					case BOOL:
						char first = buffer.charAt(0);
						thisStr = first == '0' ? "FALSE" : "TRUE";
						break;
					case ERROR:
						thisStr = buffer.toString();
						break;
					case FORMULA:
						// A formula could result in a string value,
						// so always add double-quote characters.
						thisStr = buffer.toString();
						break;
					case INLINESTR:
						// TODO: have seen an example of this, so it's untested.
						XSSFRichTextString rtsi = new XSSFRichTextString(buffer.toString());
						thisStr = rtsi.toString();
						break;
					case SSTINDEX:
						String sstIndex = buffer.toString();
						try {
							int idx = Integer.parseInt(sstIndex);
							XSSFRichTextString rtss = new XSSFRichTextString(sst.getEntryAt(idx));
							thisStr = rtss.toString();
						} catch (NumberFormatException ex) {
							// sorry but it's not a disaster...
						}
						break;

					case NUMBER:
						String n = buffer.toString();
						if (this.formatString != null) thisStr = formatter.formatRawCellContents(Double.parseDouble(n), this.formatIndex,
								this.formatString);
						else thisStr = n;
						break;
				}

				append(thisStr);
			} else if ("firstHeader".equals(name)) {
				headerFooter.setFirstHeader(buffer.toString());
			} else if ("firstFooter".equals(name)) {
				headerFooter.setFirstFooter(buffer.toString());
			} else if ("evenFooter".equals(name)) {
				headerFooter.setEvenFooter(buffer.toString());
			} else if ("evenHeaderer".equals(name)) {
				headerFooter.setEvenHeader(buffer.toString());
			} else if ("oddHeader".equals(name)) {
				headerFooter.setOddHeader(buffer.toString());
			} else if ("oddFooter".equals(name)) {
				headerFooter.setOddFooter(buffer.toString());
			} else if ("row".equals(name)) {
				if (content.length() > 0 && content.charAt(content.length() - 1) != '\n') {
					content.append('\n');
				}
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) {
			buffer.append(ch, start, length);
		}

		private final void append(String str) {
			if (StringHelper.containsNonWhitespace(str)) {
				if (content.length() > 0) content.append(' ');
				content.append(str);
			}
		}
	}
}
