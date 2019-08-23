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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.openxml.OpenXMLDocument.HeaderReference;
import org.olat.core.util.openxml.OpenXMLDocument.ListParagraph;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * Initial date: 03.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenXMLDocumentWriter {
	
	private static final Logger log = Tracing.createLoggerFor(OpenXMLDocumentWriter.class);
	
	public static final String SCHEMA_CONTENT_TYPES = "http://schemas.openxmlformats.org/package/2006/content-types";
	public static final String SCHEMA_CORE_PROPERTIES = "http://schemas.openxmlformats.org/package/2006/metadata/core-properties";
	public static final String SCHEMA_EXT_PROPERTIES = "http://schemas.openxmlformats.org/officeDocument/2006/extended-properties";
	public static final String SCHEMA_DOC_PROPS_VT = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes";
	public static final String SCHEMA_DC_TERMS = "http://purl.org/dc/terms/";
	public static final String SCHEMA_DC = "http://purl.org/dc/elements/1.1/";
	public static final String SCHEMA_RELATIONSHIPS = "http://schemas.openxmlformats.org/package/2006/relationships";

	public static final String CT_RELATIONSHIP = "application/vnd.openxmlformats-package.relationships+xml";
	public static final String CT_EXT_PROPERTIES = "application/vnd.openxmlformats-officedocument.extended-properties+xml";
	public static final String CT_CORE_PROPERTIES = "application/vnd.openxmlformats-package.core-properties+xml";
	public static final String CT_WORD_DOCUMENT = "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml";
	public static final String CT_NUMBERING = "application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml";
	public static final String CT_STYLES = "application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml";
	public static final String CT_HEADER = "application/vnd.openxmlformats-officedocument.wordprocessingml.header+xml";
	public static final String CT_THEME = "application/vnd.openxmlformats-officedocument.theme+xml";


	public void createDocument(ZipOutputStream out, OpenXMLDocument document)
			throws IOException {
		//flush header...
		document.appendPageSettings();

		//_rels
		out.putNextEntry(new ZipEntry("_rels/.rels"));
		createShadowDocumentRelationships(out);
		out.closeEntry();

		//[Content_Types].xml
		out.putNextEntry(new ZipEntry("[Content_Types].xml"));
		createContentTypes(document, out);
		out.closeEntry();

		//docProps/app.xml
		out.putNextEntry(new ZipEntry("docProps/app.xml"));
		createDocPropsApp(out);
		out.closeEntry();

		//docProps/core.xml
		out.putNextEntry(new ZipEntry("docProps/core.xml"));
		createDocPropsCore(out);
		out.closeEntry();

		//word/_rels/document.xml.rels
		out.putNextEntry(new ZipEntry("word/_rels/document.xml.rels"));
		createDocumentRelationships(out, document);
		out.closeEntry();

		//word/media
		appendMedias(out, document);

		// word/theme/theme1.xml
		out.putNextEntry(new ZipEntry("word/theme/theme1.xml"));
		try(InputStream in = OpenXMLDocumentWriter.class.getResourceAsStream("_resources/theme1.xml")) {
			IOUtils.copy(in, out);
		} catch (IOException e) {
			log.error("", e);
		}
		out.closeEntry();
		
		//word/numbering
		ZipEntry numberingDocument = new ZipEntry("word/numbering.xml");
		out.putNextEntry(numberingDocument);
		appendNumbering(out, document);
		out.closeEntry();
		
		//word/document.xml
		ZipEntry wordDocument = new ZipEntry("word/document.xml");
		out.putNextEntry(wordDocument);
		OpenXMLUtils.writeTo(document.getDocument(), out, false);
		out.closeEntry();
		
		//word/headerxxx.xml
		for(HeaderReference headerRef:document.getHeaders()) {
			ZipEntry headerDocument = new ZipEntry("word/" + headerRef.getFilename());
			out.putNextEntry(headerDocument);
			IOUtils.write(headerRef.getHeader(), out, Charset.forName("UTF-8"));
			out.closeEntry();
		}

		//word/styles.xml
		ZipEntry styles = new ZipEntry("word/styles.xml");
		out.putNextEntry(styles);
		appendPredefinedStyles(out, document.getStyles());
		out.closeEntry();
	}

	protected void appendMedias(ZipOutputStream out, OpenXMLDocument document)
			throws IOException {
		for(DocReference img:document.getImages()) {
			try(InputStream in = img.getUrl().openStream()) {
				ZipEntry wordDocument = new ZipEntry("word/media/" + img.getFilename());
				out.putNextEntry(wordDocument);

				IOUtils.copy(in, out);
				OpenXMLUtils.writeTo(document.getDocument(), out, false);
				out.closeEntry();
			} catch(Exception e) {
				log.error("", e);
			}
		}
	}
	
	private void appendNumbering(ZipOutputStream out, OpenXMLDocument document) {
		try(InputStream in = OpenXMLDocumentWriter.class.getResourceAsStream("_resources/numbering.xml")) {
			Collection<ListParagraph> numberingList = document.getNumbering();
			if(numberingList != null && numberingList.size() > 0) {
				Document numberingDoc = OpenXMLUtils.createDocument(in);
				NodeList numberingElList = numberingDoc.getElementsByTagName("w:numbering");
				Node numberingEl = numberingElList.item(0);
				for(ListParagraph numberingItem : numberingList) {
					Element abstractEl = document.createAbstractNumbering(numberingItem, numberingDoc);
					numberingEl.appendChild(abstractEl);
					Element numEl = document.createNumbering(numberingItem, numberingDoc);
					numberingEl.appendChild(numEl);
				}
				OpenXMLUtils.writeTo(numberingDoc, out, false);
			} else {
				IOUtils.copy(in, out);
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	private void appendPredefinedStyles(ZipOutputStream out, OpenXMLStyles styles) {
		try(InputStream in = OpenXMLDocumentWriter.class.getResourceAsStream("_resources/styles.xml")) {
			if(styles != null) {
				Document stylesDoc = OpenXMLUtils.createDocument(in);
				NodeList stylesElList = stylesDoc.getElementsByTagName("w:styles");
				if(stylesElList.getLength() == 1) {
					//Node stylesEl = stylesElList.item(0);
					//System.out.println("Append:" + stylesEl);
				}
				OpenXMLUtils.writeTo(stylesDoc, out, false);
			} else {
				IOUtils.copy(in, out);
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	/*
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings" Target="settings.xml"/>
  <Relationship Id="rId5" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/webSettings" Target="webSettings.xml"/>
  <Relationship Id="rId6" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/fontTable" Target="fontTable.xml"/>
  <Relationship Id="rId7" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme" Target="theme/theme1.xml"/>
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
  <Relationship Id="rId2" Type="http://schemas.microsoft.com/office/2007/relationships/stylesWithEffects" Target="stylesWithEffects.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/numbering" Target="numbering.xml" />
</Relationships>
	 */
	protected void createDocumentRelationships(ZipOutputStream out, OpenXMLDocument document) {
		try {
			XMLStreamWriter writer = OpenXMLUtils.createStreamWriter(out);
			writer.writeStartDocument("UTF-8", "1.0");
			writer.writeStartElement("Relationships");
			writer.writeNamespace("", SCHEMA_RELATIONSHIPS);
			
			Document doc = OpenXMLUtils.createDocument();
			Element relationshipsEl = (Element)doc.appendChild(doc.createElement("Relationships"));
			relationshipsEl.setAttribute("xmlns", SCHEMA_RELATIONSHIPS);

			addRelationship("rId1", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles",
					"styles.xml", writer);
			addRelationship("rId2", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/numbering",
					"numbering.xml", writer);
			
			if(document != null) {
				for(DocReference docRef:document.getImages()) {
					addRelationship(docRef.getId(), "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
							"media/" + docRef.getFilename(), writer);
				}
				
				for(HeaderReference headerRef:document.getHeaders()) {
					addRelationship(headerRef.getId(), "http://schemas.openxmlformats.org/officeDocument/2006/relationships/header",
							headerRef.getFilename(), writer);
				}
			}
			
			addRelationship(document.generateId(), "http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme",
					"theme/theme1.xml", writer);


			writer.writeEndElement();// end Relationships
			writer.writeEndDocument();
			writer.flush();
			writer.close();
		} catch (XMLStreamException e) {
			log.error("", e);
		}
	}
	
	/*
<?xml version="1.0" encoding="UTF-8"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
<Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>
	*/
	protected void createShadowDocumentRelationships(ZipOutputStream zout) {
		try {
			XMLStreamWriter writer = OpenXMLUtils.createStreamWriter(zout);
			writer.writeStartDocument("UTF-8", "1.0");
			writer.writeStartElement("Relationships");
			writer.writeNamespace("", SCHEMA_RELATIONSHIPS);

			addRelationship("rId1", "http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties",
					"docProps/core.xml", writer);
			addRelationship("rId2", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties",
					"docProps/app.xml", writer);
			addRelationship("rId3", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument",
					"word/document.xml", writer);
			
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
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties"
  xmlns:dcterms="http://purl.org/dc/terms/"
  xmlns:dc="http://purl.org/dc/elements/1.1/">
	<dc:creator>docx4j</dc:creator>
	<cp:lastModifiedBy>docx4j</cp:lastModifiedBy>
</cp:coreProperties>
	 */
	protected void createDocPropsCore(OutputStream out) {
		try {
			Document doc = OpenXMLUtils.createDocument();
			Element propertiesEl = (Element)doc.appendChild(doc.createElement("cp:coreProperties"));
			propertiesEl.setAttribute("xmlns:cp", SCHEMA_CORE_PROPERTIES);
			propertiesEl.setAttribute("xmlns:dcterms", SCHEMA_DC_TERMS);
			propertiesEl.setAttribute("xmlns:dc", SCHEMA_DC);
			addDCProperty("creator", "OpenOLAT", propertiesEl, doc);
			addCPProperty("lastModifiedBy", "OpenOLAT", propertiesEl, doc);
			OpenXMLUtils.writeTo(doc, out, false);
		} catch (DOMException e) {
			log.error("", e);
		}
	}

	private final void addCPProperty(String name, String value, Element propertiesEl, Document doc) {
		Element defaultEl = (Element)propertiesEl.appendChild(doc.createElement("cp:" + name));
		defaultEl.appendChild(doc.createTextNode(value));
	}

	private final void addDCProperty(String name, String value, Element propertiesEl, Document doc) {
		Element defaultEl = (Element)propertiesEl.appendChild(doc.createElement("dc:" + name));
		defaultEl.appendChild(doc.createTextNode(value));
	}

	/*
    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <properties:Properties xmlns:properties="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties"
      xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
        <properties:Application>OpenOLAT</properties:Application>
        <properties:AppVersion>9.1.0.</properties:AppVersion>
    </properties:Properties>
     */
	protected void createDocPropsApp(OutputStream out) {
		try {
			Document doc = OpenXMLUtils.createDocument();
			Element propertiesEl = (Element)doc.appendChild(doc.createElement("properties:Properties"));
			propertiesEl.setAttribute("xmlns:properties", SCHEMA_EXT_PROPERTIES);
			addExtProperty("Application", "Microsoft Macintosh Word", propertiesEl, doc);
			addExtProperty("AppVersion", "14.0000", propertiesEl, doc);
			OpenXMLUtils.writeTo(doc, out, false);
		} catch (DOMException e) {
			log.error("", e);
		}
	}

	private final void addExtProperty(String name, String value, Element propertiesEl, Document doc) {
		Element defaultEl = (Element)propertiesEl.appendChild(doc.createElement("properties:" + name));
		defaultEl.appendChild(doc.createTextNode(value));
	}

	/*
    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
        <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml" />
        <Override ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml" PartName="/docProps/app.xml" />
        <Override ContentType="application/vnd.openxmlformats-package.core-properties+xml" PartName="/docProps/core.xml" />
        <Override ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml" PartName="/word/document.xml" />
        <Override ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml" PartName="/word/numbering.xml" />
        <Override ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml" PartName="/word/styles.xml" />
    </Types>
     */
	protected void createContentTypes(OpenXMLDocument document, ZipOutputStream out) {
		try {
			XMLStreamWriter writer = OpenXMLUtils.createStreamWriter(out);
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
			createContentTypesOverride("/word/document.xml", CT_WORD_DOCUMENT, writer);
			createContentTypesOverride("/word/styles.xml", CT_STYLES, writer);
			createContentTypesOverride("/word/numbering.xml", CT_NUMBERING, writer);
			createContentTypesOverride("/word/theme/theme1.xml", CT_THEME, writer);
			
			for(HeaderReference headerRef:document.getHeaders()) {
				createContentTypesOverride("/word/" + headerRef.getFilename(), CT_HEADER, writer);
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

}
