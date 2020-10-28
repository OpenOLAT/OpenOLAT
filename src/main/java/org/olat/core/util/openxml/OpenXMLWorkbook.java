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
package org.olat.core.util.openxml;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.openxml.workbookstyle.Border;
import org.olat.core.util.openxml.workbookstyle.CellStyle;
import org.olat.core.util.openxml.workbookstyle.Fill;
import org.olat.core.util.openxml.workbookstyle.Font;

/**
 * 
 * Initial date: 21.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenXMLWorkbook implements Closeable {
	
	private static final Logger log = Tracing.createLoggerFor(OpenXMLWorkbook.class);
	

	public static final String SCHEMA_RELATIONSHIPS = "http://schemas.openxmlformats.org/package/2006/relationships";
	
	private static final String SCHEMA_CONTENT_TYPES = "http://schemas.openxmlformats.org/package/2006/content-types";
	private static final String SCHEMA_CORE_PROPERTIES = "http://schemas.openxmlformats.org/package/2006/metadata/core-properties";
	private static final String SCHEMA_EXT_PROPERTIES = "http://schemas.openxmlformats.org/officeDocument/2006/extended-properties";
	private static final String SCHEMA_DOC_PROPS_VT = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes";
	private static final String SCHEMA_DC_TERMS = "http://purl.org/dc/terms/";
	private static final String SCHEMA_DC = "http://purl.org/dc/elements/1.1/";
	
	private static final String CT_RELATIONSHIP = "application/vnd.openxmlformats-package.relationships+xml";
	private static final String CT_EXT_PROPERTIES = "application/vnd.openxmlformats-officedocument.extended-properties+xml";
	private static final String CT_CORE_PROPERTIES = "application/vnd.openxmlformats-package.core-properties+xml";

	private List<OpenXMLWorksheet> worksheets = new ArrayList<>(10);
	private final OpenXMLWorkbookStyles styles = new OpenXMLWorkbookStyles();
	private OpenXMLWorkbookSharedStrings sharedStrings = new OpenXMLWorkbookSharedStrings();
	private final Filter xmlCharactersFilter = FilterFactory.getXMLValidCharacterFilter();
	
	private int currentId = 4;
	private boolean opened;
	
	private final ZipOutputStream zout;
	private final int numberOfWorksheet;
	private final List<String> worksheetsNames;
	private OpenXMLWorksheet currentWorkSheet;
	
	public OpenXMLWorkbook(OutputStream outputStream, int numberOfWorksheet) {
		this(outputStream, numberOfWorksheet, Collections.emptyList());
	}
	
	public OpenXMLWorkbook(OutputStream outputStream, int numberOfWorksheet, List<String> worksheetsNames) {
		zout = new ZipOutputStream(outputStream);
		zout.setLevel(9);
		this.numberOfWorksheet = numberOfWorksheet;
		this.worksheetsNames = worksheetsNames;
		for(int i=0; i<numberOfWorksheet; i++) {
			worksheets.add(new OpenXMLWorksheet(generateId(), this, zout));
		}
	}
	
	public int getNumberOfWorksheets() {
		return numberOfWorksheet;
	} 
	
	public OpenXMLWorksheet nextWorksheet() {
		try {
			if(!opened) {
				appendPrologue();
				opened = true;
			}
			
			int index = 0;
			if(currentWorkSheet == null) {
				currentWorkSheet = worksheets.get(0);
			} else {
				currentWorkSheet.close();
				zout.closeEntry();
				index = worksheets.indexOf(currentWorkSheet) + 1;
				currentWorkSheet = worksheets.get(index);
			}
			
			ZipEntry worksheetEntry = new ZipEntry("xl/worksheets/sheet" + (index + 1) + ".xml");
			zout.putNextEntry(worksheetEntry);
			return currentWorkSheet;
		} catch (IOException e) {
			log.error("", e);
			return null;
		}
	}
	
	@Override
	public void close() throws IOException {
		if(opened) {
			currentWorkSheet.close();
			zout.closeEntry();
			appendEpilogue();
			zout.flush();
			zout.close();
		}
	}
	
	public OpenXMLWorkbookStyles getStyles() {
		return styles;
	}
	
	public OpenXMLWorkbookSharedStrings getSharedStrings() {
		return sharedStrings;
	}
	
	private String generateId() {
		return "rId" + (++currentId);
	}
	
	private final void appendPrologue() {
		try {
			//_rels
			zout.putNextEntry(new ZipEntry("_rels/.rels"));
			appendShadowDocumentRelationships();
			zout.closeEntry();
			
			//[Content_Types].xml
			zout.putNextEntry(new ZipEntry("[Content_Types].xml"));
			appendContentTypes();
			zout.closeEntry();
			
			//docProps/app.xml
			zout.putNextEntry(new ZipEntry("docProps/app.xml"));
			appendDocPropsApp(zout);
			zout.closeEntry();
			
			//docProps/core.xml
			zout.putNextEntry(new ZipEntry("docProps/core.xml"));
			appendDocPropsCore(zout);
			zout.closeEntry();
			
			//xl/_rels/workbook.xml.rels
			zout.putNextEntry(new ZipEntry("xl/_rels/workbook.xml.rels"));
			appendWorkbookRelationships();
			zout.closeEntry();
			
			//xl/theme/theme1.xml
			zout.putNextEntry(new ZipEntry("xl/theme/theme1.xml"));
			try(InputStream in = OpenXMLDocumentWriter.class.getResourceAsStream("_resources/theme1.xml")) {
				IOUtils.copy(in, zout);
			} catch (IOException e) {
				log.error("", e);
			}
			zout.closeEntry();

			//xl/workbook.xml
			zout.putNextEntry(new ZipEntry("xl/workbook.xml"));
			appendWorkbook();
			zout.closeEntry();
		} catch (IOException e) {
			log.error("", e);
		}	
	}
	
	private void appendEpilogue() {
		try {
			//xl/styles
			zout.putNextEntry(new ZipEntry("xl/styles.xml"));
			appendPredefinedStyles();
			zout.closeEntry();
			
			//xl/sharedStrings.xml
			if(getSharedStrings().size() > 0) {
				zout.putNextEntry(new ZipEntry("xl/sharedStrings.xml"));
				appendSharedString();
				zout.closeEntry();
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	/*
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
	<Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml" />
	<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml" />
	<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml" />
</Relationships>
	*/
	private final void appendShadowDocumentRelationships() {
		try {
			XMLStreamWriter writer = OpenXMLUtils.createStreamWriter(zout);
			writer.writeStartDocument("UTF-8", "1.0");
			writer.writeStartElement("Relationships");
			writer.writeNamespace("", SCHEMA_RELATIONSHIPS);

			addRelationship("rId1", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument",
					"xl/workbook.xml", writer);
			addRelationship("rId2", "http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties",
					"docProps/core.xml", writer);
			addRelationship("rId3", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties",
					"docProps/app.xml", writer);
			
			writer.writeEndElement();// end Relationships
			writer.writeEndDocument();
			writer.flush();
			writer.close();
		} catch (XMLStreamException e) {
			log.error("", e);
		}
	}
	
	private final void addRelationship(String id, String type, String target, XMLStreamWriter writer)
	throws XMLStreamException {
		writer.writeStartElement("Relationship");
		writer.writeAttribute("Id", id);
		writer.writeAttribute("Type", type);
		writer.writeAttribute("Target", target);
		writer.writeEndElement();
	}
	
/*
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
	<Default Extension="xml" ContentType="application/xml" />
	<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml" />
	<Default Extension="jpeg" ContentType="image/jpeg" />
	<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml" />
	<Override PartName="/xl/theme/theme1.xml" ContentType="application/vnd.openxmlformats-officedocument.theme+xml" />
	<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml" />
	<Override PartName="/xl/sharedStrings.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml" />
	<Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml" />
	<Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml" />
	<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml" />
	
</Types>
 */
	protected void appendContentTypes() {
		try {
			XMLStreamWriter writer = OpenXMLUtils.createStreamWriter(zout);
			writer.writeStartDocument("UTF-8", "1.0");
			writer.writeStartElement("Types");
			writer.writeNamespace("", SCHEMA_CONTENT_TYPES);

			//Default
			createContentTypesDefault("rels", CT_RELATIONSHIP, writer);
			createContentTypesDefault("xml", "application/xml", writer);
			createContentTypesDefault("jpeg", "image/jpeg", writer);
			createContentTypesDefault("jpg", "image/jpeg", writer);
			createContentTypesDefault("png", "image/png", writer);
			createContentTypesDefault("gif", "image/gif", writer);
			//Override
			createContentTypesOverride("/docProps/app.xml", CT_EXT_PROPERTIES, writer);
			createContentTypesOverride("/docProps/core.xml", CT_CORE_PROPERTIES, writer);
			//Override workbook
			createContentTypesOverride("/xl/workbook.xml",
					"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml", writer);
			createContentTypesOverride("/xl/theme/theme1.xml",
					"application/vnd.openxmlformats-officedocument.theme+xml", writer);
			createContentTypesOverride("/xl/styles.xml",
					"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml", writer);
			if(getSharedStrings().size() > 0) {
				createContentTypesOverride("/xl/sharedStrings.xml",
					"application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml", writer);
			}
			//Override worksheets
			for(int i=0; i<getNumberOfWorksheets(); i++) {
				createContentTypesOverride("/xl/worksheets/sheet" + (i+1) + ".xml",
						"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml", writer);
			}
			
			writer.writeEndElement();// end Types
			writer.flush();
			writer.close();
		} catch (XMLStreamException e) {
			log.error("", e);
		}
	}
	
	private final void createContentTypesDefault(String extension, String type, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("Default");
		writer.writeAttribute("Extension", extension);
		writer.writeAttribute("ContentType", type);
		writer.writeEndElement();
	}
	
	private final void createContentTypesOverride(String partName, String type, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("Override");
		writer.writeAttribute("PartName", partName);
		writer.writeAttribute("ContentType", type);
		writer.writeEndElement();
	}
	
/*
	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
	<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
		<Application>
			Microsoft Macintosh Excel
		</Application>
		<DocSecurity>
			0
		</DocSecurity>
		<ScaleCrop>
			false
		</ScaleCrop>
		<HeadingPairs>
			<vt:vector size="2" baseType="variant">
				<vt:variant>
					<vt:lpstr>
						Arbeitsblätter
					</vt:lpstr>
				</vt:variant>
				<vt:variant>
					<vt:i4>
						1
					</vt:i4>
				</vt:variant>
			</vt:vector>
		</HeadingPairs>
		<TitlesOfParts>
			<vt:vector size="1" baseType="lpstr">
				<vt:lpstr>Blatt1
				</vt:lpstr>
			</vt:vector>
		</TitlesOfParts>
		<Company>
			frentix
		</Company>
		<LinksUpToDate>
			false
		</LinksUpToDate>
		<SharedDoc>
			false
		</SharedDoc>
		<HyperlinksChanged>
			false
		</HyperlinksChanged>
		<AppVersion>
			14.0300
		</AppVersion>
	</Properties>
*/
	private static final void appendDocPropsApp(ZipOutputStream out) {
		try {
			XMLStreamWriter writer = OpenXMLUtils.createStreamWriter(out); 
			writer.writeStartDocument("UTF-8", "1.0");
			writer.writeStartElement("Properties");
			writer.writeNamespace("", SCHEMA_EXT_PROPERTIES);
			writer.writeNamespace("vt", SCHEMA_DOC_PROPS_VT);

			appendTag("Application", "Microsoft Macintosh Excel", writer);
			appendTag("DocSecurity", "0", writer);
			appendTag("ScaleCrop", "false", writer);
			
			//HeadingPairs
			writer.writeStartElement("HeadingPairs");
			writer.writeStartElement("vt:vector");
			writer.writeAttribute("size", "2");
			writer.writeAttribute("baseType", "variant");
			
			//1 variant
			writer.writeStartElement("vt:variant");
			appendTag("vt:lpstr", "Arbeitsblätter", writer);
			writer.writeEndElement();
	
			//2 variant
			writer.writeStartElement("vt:variant");
			appendTag("vt:i4", "1", writer);
			writer.writeEndElement();
			
			writer.writeEndElement();
			writer.writeEndElement();// end HeadingPairs
			
			//TitlesOfParts
			writer.writeStartElement("TitlesOfParts");
			writer.writeStartElement("vt:vector");
			writer.writeAttribute("size", "1");
			writer.writeAttribute("baseType", "lpstr");
			writer.writeStartElement("vt:lpstr");
			writer.writeCharacters("Sheet 1");
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();// end TitlesOfParts

			appendTag("Company", "frentix", writer);
			appendTag("LinksUpToDate", "false", writer);
			appendTag("SharedDoc", "false", writer);
			appendTag("HyperlinksChanged", "false", writer);
			appendTag("AppVersion", "14.0300", writer);

			writer.writeEndElement();// end properties
			writer.flush();
			writer.close();
		} catch (XMLStreamException e) {
			log.error("", e);
		}
	}
	
/*
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties"
  xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/"
  xmlns:dcmitype="http://purl.org/dc/dcmitype/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<dc:creator>
		Stéphane Rossé
	</dc:creator>
	<cp:lastModifiedBy>
		Stéphane Rossé
	</cp:lastModifiedBy>
	<dcterms:created xsi:type="dcterms:W3CDTF">
		2016-04-21T14:56:49Z
	</dcterms:created>
	<dcterms:modified xsi:type="dcterms:W3CDTF">
		2016-04-21T15:03:56Z
	</dcterms:modified>
</cp:coreProperties>
*/
	private static final void appendDocPropsCore(ZipOutputStream out) {
		try {
			XMLStreamWriter writer = OpenXMLUtils.createStreamWriter(out);
			writer.writeStartDocument("UTF-8", "1.0");
			writer.writeStartElement("cp:coreProperties");
			writer.writeNamespace("cp", SCHEMA_CORE_PROPERTIES);
			writer.writeNamespace("dc", SCHEMA_DC);
			writer.writeNamespace("dcterms", SCHEMA_DC_TERMS);
			writer.writeNamespace("dcmitype", "http://purl.org/dc/dcmitype/");
			writer.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

			writer.writeStartElement("dc:creator");
			writer.writeCharacters("OpenOLAT");
			writer.writeEndElement();
			
			writer.writeStartElement("cp:lastModifiedBy");
			writer.writeCharacters("OpenOLAT");
			writer.writeEndElement();

			writer.writeEndElement();// end sst
			writer.flush();
			writer.close();
		} catch (XMLStreamException e) {
			log.error("", e);
		}
	}
		
/*
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
	<Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml" />
	<Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings" Target="sharedStrings.xml" />
	<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml" />
	<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme" Target="theme/theme1.xml" />
</Relationships>
*/
	private final void appendWorkbookRelationships() {
		try {
			XMLStreamWriter writer = OpenXMLUtils.createStreamWriter(zout);
			writer.writeStartDocument("UTF-8", "1.0");
			writer.writeStartElement("Relationships");
			writer.writeNamespace("", SCHEMA_RELATIONSHIPS);

			addRelationship("rId1", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles",
					"styles.xml", writer);
			
			if(getSharedStrings().size() > 0) {
				addRelationship("rId2", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings",
					"sharedStrings.xml", writer);
			}
			addRelationship("rId3", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme",
					"theme/theme1.xml", writer);
			
			for(int i=0; i<getNumberOfWorksheets(); i++) {
				OpenXMLWorksheet worksheet = worksheets.get(i);
				addRelationship(worksheet.getId(), "http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet",
						"worksheets/sheet" + (i+1) + ".xml", writer);
			}

			writer.writeEndElement();// end Relationships
			writer.writeEndDocument();
			writer.flush();
			writer.close();
		} catch (XMLStreamException e) {
			log.error("", e);
		}
	}
	
	/*
	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
	<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
		<fileVersion appName="xl" lastEdited="5" lowestEdited="5" rupBuild="25823" /><workbookPr showInkAnnotation="0" autoCompressPictures="0" />
		<bookViews>
			<workbookView xWindow="5060" yWindow="2060" windowWidth="25600" windowHeight="19020" tabRatio="500" />
		</bookViews>
		<sheets>
			<sheet name="Blatt1" sheetId="1" r:id="rId1" />
		</sheets>
		<calcPr calcId="140000" concurrentCalc="0" />
		<extLst>
			<ext uri="{7523E5D3-25F3-A5E0-1632-64F254C22452}" xmlns:mx="http://schemas.microsoft.com/office/mac/excel/2008/main">
				<mx:ArchID Flags="2" />
			</ext>
		</extLst>
	</workbook>
*/
	private void appendWorkbook() {
		try {
			XMLStreamWriter writer = OpenXMLUtils.createStreamWriter(zout); 
			writer.writeStartDocument("UTF-8", "1.0");
			writer.writeStartElement("workbook");
			writer.writeNamespace("", "http://schemas.openxmlformats.org/spreadsheetml/2006/main");
			writer.writeNamespace("r", "http://schemas.openxmlformats.org/officeDocument/2006/relationships");
			
			//fileVersion
			writer.writeStartElement("fileVersion");
			writer.writeAttribute("appName", "xl");
			writer.writeAttribute("lastEdited", "1");
			writer.writeAttribute("lowestEdited", "1");
			writer.writeAttribute("rupBuild", "25823");
			writer.writeEndElement();
			
			//workbookPr
			writer.writeStartElement("workbookPr");
			writer.writeAttribute("showInkAnnotation", "0");
			writer.writeAttribute("autoCompressPictures", "0");
			writer.writeEndElement();
			
			//bookViews / workbookView
			writer.writeStartElement("bookViews");
			writer.writeStartElement("workbookView");
			writer.writeAttribute("xWindow", "5060");
			writer.writeAttribute("yWindow", "2060");
			writer.writeAttribute("windowWidth", "25600");
			writer.writeAttribute("windowHeight", "19020");
			writer.writeAttribute("tabRatio", "500");
			writer.writeEndElement();
			writer.writeEndElement();
			
			//sheets
			writer.writeStartElement("sheets");
			int count = 1;
			for(OpenXMLWorksheet sheet:worksheets) {
				writer.writeStartElement("sheet");
				
				String sheetName = null;
				if(worksheetsNames != null && count <= worksheetsNames.size()) {
					sheetName = worksheetsNames.get(count - 1);
				}
				if(!StringHelper.containsNonWhitespace(sheetName)) {
					sheetName = "Sheet " + count;
				}
				writer.writeAttribute("name", normalizeWorksheetName(sheetName));
				writer.writeAttribute("sheetId", Integer.toString(count++));
				writer.writeAttribute("r:id", sheet.getId());
				writer.writeEndElement();
			}
			writer.writeEndElement();
			
			//workbookPr
			writer.writeStartElement("calcPr");
			writer.writeAttribute("calcId", "140000");
			writer.writeAttribute("concurrentCalc", "0");
			writer.writeEndElement();
			
			//extLst
			writer.writeStartElement("extLst");
			writer.writeStartElement("ext");
			writer.writeAttribute("uri", "{7523E5D3-25F3-A5E0-1632-64F254C22452}");
			writer.writeNamespace("mx", "http://schemas.microsoft.com/office/mac/excel/2008/main");
			writer.writeStartElement("ext");
			writer.writeStartElement("mx:ArchID");
			writer.writeAttribute("Flags", "2");
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndElement();

			writer.writeEndElement();// end workbook
			writer.flush();
			writer.close();
		} catch (XMLStreamException e) {
			log.error("", e);
		}
	}
	
	private String normalizeWorksheetName(String text) {
		text = text.replace('?', ' ')
				.replace('\\', ' ')
				.replace('/', ' ')
				.replace('$', ' ')
				.replace('{', '(')
				.replace('}', ')')
				.replace('[', '(')
				.replace(']', ')');
		return Normalizer.normalize(text, Normalizer.Form.NFKD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+","");
	}
	
	/*
	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
	<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" mc:Ignorable="x14ac" xmlns:x14ac="http://schemas.microsoft.com/office/spreadsheetml/2009/9/ac">
		<fonts count="1" x14ac:knownFonts="1"/>
		<fills count="4"/>
		<borders count="2"/>
		<cellStyleXfs count="1"/>
		<cellXfs count="5"/>
		<cellStyles count="1">
			<cellStyle name="Standard" xfId="0" builtinId="0" />
		</cellStyles>
		<dxfs count="0" /><tableStyles count="0" defaultTableStyle="TableStyleMedium9" defaultPivotStyle="PivotStyleMedium4" />
	</styleSheet>
	*/
	private void appendPredefinedStyles() {
		try {
			XMLStreamWriter writer = OpenXMLUtils.createStreamWriter(zout);
			writer.writeStartDocument("UTF-8", "1.0");
			writer.writeStartElement("styleSheet");
			writer.writeNamespace("", "http://schemas.openxmlformats.org/spreadsheetml/2006/main");
			writer.writeNamespace("mc", "http://schemas.openxmlformats.org/markup-compatibility/2006");
			writer.writeAttribute("mc:Ignorable", "x14ac");
			writer.writeNamespace("x14ac", "http://schemas.microsoft.com/office/spreadsheetml/2009/9/ac");
			
			appendFonts(styles.getFonts(), writer);
			appendFills(styles.getFills(), writer);
			appendBorders(styles.getBorders(), writer);
			
			//cellStyleXfs
			writer.writeStartElement("cellStyleXfs");
			writer.writeAttribute("count", "1");
			writer.writeStartElement("xf");
			writer.writeAttribute("numFmtId", "0");
			writer.writeAttribute("fontId", "0");
			writer.writeAttribute("fillId", "0");
			writer.writeAttribute("borderId", "0");
			writer.writeEndElement();
			writer.writeEndElement();
			
			//cellXfs
			appendCellXfs(styles.getCellXfs(), writer);

			//cellStyles
			writer.writeStartElement("cellStyles");
			writer.writeAttribute("count", "1");
			writer.writeStartElement("cellStyle");
			writer.writeAttribute("name", "Standard");
			writer.writeAttribute("xfId", "0");
			writer.writeAttribute("builtinId", "0");
			writer.writeEndElement();
			writer.writeEndElement();
			
			//dxfs
			writer.writeStartElement("dxfs");
			writer.writeAttribute("count", "0");
			writer.writeEndElement();

			//tableStyles
			writer.writeStartElement("tableStyles");
			writer.writeAttribute("count", "0");
			writer.writeAttribute("defaultTableStyle", "TableStyleMedium9");
			writer.writeAttribute("defaultPivotStyle", "PivotStyleMedium4");
			writer.writeEndElement();

			writer.writeEndElement();// end sst
			writer.flush();
			writer.close();
		} catch (XMLStreamException e) {
			log.error("", e);
		}
	}

	
/*
<cellXfs count="5">
	<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0" />
	<xf numFmtId="0" fontId="0" fillId="2" borderId="0" xfId="0" applyFill="1" />
	<xf numFmtId="0" fontId="0" fillId="3" borderId="0" xfId="0" applyFill="1" />
	<xf numFmtId="14" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1" />
	<xf numFmtId="0" fontId="0" fillId="0" borderId="1" xfId="0" applyBorder="1" />
</cellXfs>
*/
	private void appendCellXfs(List<CellStyle> cellXfs, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("cellXfs");
		writer.writeAttribute("count", Integer.toString(cellXfs.size()));
		for (CellStyle style: cellXfs) {
			appendCellXfs(style, writer);
		}
		writer.writeEndElement();
	}
	
	private void appendCellXfs(CellStyle style, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("xf");
		writer.writeAttribute("numFmtId", style.getNumFmtId());
		writer.writeAttribute("fontId", Integer.toString(style.getFont().getIndex()));
		writer.writeAttribute("fillId", Integer.toString(style.getFill().getIndex()));
		writer.writeAttribute("borderId", Integer.toString(style.getBorder().getIndex()));
		writer.writeAttribute("xfId", style.getId());
		if(style.getBorder().getIndex() > 0) {
			writer.writeAttribute("applyBorder", "1");
		}
		if(style.getFill().getIndex() > 0) {
			writer.writeAttribute("applyFill", "1");
		}
		if(StringHelper.containsNonWhitespace(style.getApplyNumberFormat())) {
			writer.writeAttribute("applyNumberFormat", style.getApplyNumberFormat());
		}
		if(StringHelper.containsNonWhitespace(style.getApplyAlignment())) {
			writer.writeAttribute("applyAlignment", style.getApplyAlignment());
		}
		
		if(style.getAlignment() != null) {
			writer.writeStartElement("alignment");
			if(StringHelper.containsNonWhitespace(style.getAlignment().getVertical())) {
				writer.writeAttribute("vertical", style.getAlignment().getVertical());
			}
			if(StringHelper.containsNonWhitespace(style.getAlignment().getWrapText())) {
				writer.writeAttribute("wrapText", style.getAlignment().getWrapText());
			}
			writer.writeEndElement();
		}
		
		writer.writeEndElement();
	}
		
	/*
	<font>
		<sz val="12" />
		<color theme="1" />
		<name val="Calibri" />
		<family val="2" />
		<scheme val="minor" />
	</font>
	*/
	private void appendFonts(List<Font> fonts, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("fonts");
		writer.writeAttribute("count", Integer.toString(fonts.size()));
		writer.writeAttribute("x14ac:knownFonts", Integer.toString(fonts.size()));
		for (Font font: fonts) {
			writer.writeStartElement("font");
			appendTag("sz", "val", font.getSzVal(), writer);
			appendTag("color", "theme", font.getColorTheme(), writer);
			appendTag("name", "val", font.getNameVal(), writer);
			appendTag("family", "val", font.getFamilyVal(), writer);
			appendTag("scheme", "val", font.getSchemeVal(), writer);
			writer.writeEndElement();
		}
		writer.writeEndElement();
	}
	
	/*
	<fill>
		<patternFill patternType="none" />
	</fill>
	*/
	private void appendFills(List<Fill> fills, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("fills");
		writer.writeAttribute("count", Integer.toString(fills.size()));
		for (Fill fill: fills) {
			writer.writeStartElement("fill");
			if(StringHelper.containsNonWhitespace(fill.getPatternType())) {
				writer.writeStartElement("patternFill");
				writer.writeAttribute("patternType", fill.getPatternType());
				if(StringHelper.containsNonWhitespace(fill.getFgColorRgb())) {
					//<fgColor rgb="FFC3FFC0" />
					appendTag("fgColor", "rgb", fill.getFgColorRgb(), writer);
				}
				if(StringHelper.containsNonWhitespace(fill.getBgColorIndexed())) {
					//<bgColor indexed="64" />
					appendTag("bgColor", "indexed", fill.getBgColorIndexed(), writer);
				}
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}
		writer.writeEndElement();
	}

	/*
	<border>
		<left /><right /><top /><bottom /><diagonal />
	</border>
	*/
	private void appendBorders(List<Border> borders, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("borders");
		writer.writeAttribute("count", Integer.toString(borders.size()));
		for (Border border: borders) {
			writer.writeStartElement("border");
			appendBorderLine("left",  border.getLeft(), writer);
			appendBorderLine("right",  border.getRight(), writer);
			appendBorderLine("top",  border.getTop(), writer);
			appendBorderLine("bottom",  border.getBottom(), writer);
			writer.writeEmptyElement("diagonal");
			writer.writeEndElement();
		}
		writer.writeEndElement();
	}
	
	private void appendBorderLine(String position, String value, XMLStreamWriter writer) throws XMLStreamException {
		if(StringHelper.containsNonWhitespace(value)) {
			writer.writeStartElement(position);
			writer.writeAttribute("style", value);
			appendTag("color", "auto", "1", writer);
			writer.writeEndElement();
		} else {
			writer.writeEmptyElement(position);
		}
	}
	
	/*
	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
	<sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" count="4" uniqueCount="4">
		<si>
			<t>
				Test
			</t>
		</si>
	</sst>
	*/
	private void appendSharedString() {
		try {
			XMLStreamWriter writer = OpenXMLUtils.createStreamWriter(zout);
			writer.writeStartDocument("UTF-8", "1.0");
			writer.writeStartElement("sst");
			writer.writeNamespace("", "http://schemas.openxmlformats.org/spreadsheetml/2006/main");
			writer.writeAttribute("count", Integer.toString(sharedStrings.size()));
			writer.writeAttribute("uniqueCount", Integer.toString(sharedStrings.size()));
			
			for (String sharedString: sharedStrings) {
				writer.writeStartElement("si");
				writer.writeStartElement("t");
				String cleanedSharedString = xmlCharactersFilter.filter(sharedString);
				if(cleanedSharedString.contains("<") || cleanedSharedString.contains(">")) {
					writer.writeCData(cleanedSharedString);
				} else {
					writer.writeCharacters(cleanedSharedString);
				}
				writer.writeEndElement();
				writer.writeEndElement();
			}
			
			writer.writeEndElement();// end sst
			writer.flush();
			writer.close();
		} catch (XMLStreamException e) {
			log.error("", e);
		}
	}
	
	private static final void appendTag(String tag, String characters, XMLStreamWriter writer)
	throws XMLStreamException {
		writer.writeStartElement(tag);
		writer.writeCharacters(characters);
		writer.writeEndElement();
	}
	
	private static final void appendTag(String tag, String attribute, String attributeValue, XMLStreamWriter writer)
	throws XMLStreamException {
		writer.writeStartElement(tag);
		writer.writeAttribute(attribute, attributeValue);
		writer.writeEndElement();
	}
}
