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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 


package org.olat.modules.selectus.ui;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.EnvironmentProfile;
import org.apache.fop.apps.EnvironmentalProfileFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.apache.logging.log4j.Logger;
import org.apache.xmlgraphics.io.ResourceResolver;
import org.apache.xmlgraphics.io.URIResolverAdapter;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.xml.XMLFactories;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.manager.TemplatesCache;
import org.olat.modules.selectus.model.Notes;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import org.olat.modules.selectus.ui.components.ExportTableDataModel;
import org.olat.modules.selectus.ui.document.PDFApplicationCombinedHelper;
import org.olat.modules.selectus.ui.resources.FOPMediaResource;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  18 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class FOPTableExport {
	
	private static final Logger log = Tracing.createLoggerFor(FOPTableExport.class);
	
	public static final float ARIAL_12_LETTER_WIDTH = 6.0f;
	
	public FOPMediaResource exportApplications(Identity downloader, Position position, ExportTableDataModel<?> dataModel, Locale locale)  {
		return export(downloader, position, dataModel, "applications.xslt", locale);
	}
	
	public FOPMediaResource exportApplicationsForStaff(Identity downloader, Position position, ExportTableDataModel<?> dataModel, Locale locale)  {
		return export(downloader, position, dataModel, "applications_staff.xslt", locale);
	}
	
	public FOPMediaResource exportRatings(Identity downloader, Position position, ExportTableDataModel<?> dataModel, Locale locale)  {
		return export(downloader, position, dataModel, "ratings.xslt", locale);
	}
	
	public FOPMediaResource exportNotes(Identity downloader, Position position, ExportTableDataModel<?> dataModel, Locale locale)  {
		return export(downloader, position, dataModel, "notes.xslt", locale);
	}
	
	public FOPMediaResource exportRejectionLog(Identity downloader, Position position, ExportTableDataModel<?> dataModel, Locale locale)  {
		return export(downloader, position, dataModel, "rejection_log.xslt", locale);
	}
	
	public byte[] exportAsByteArray(Identity downloader, Position position, ExportTableDataModel<?> dataModel, String template, Locale locale)  {
		try(	ByteArrayOutputStream out = new ByteArrayOutputStream();
				BufferedOutputStream bout = new BufferedOutputStream(out)) {
			// Setup input for XSLT transformation
			Document doc = createDocument(downloader, position, dataModel, locale);
			transform(doc, bout, template);
			bout.flush();
			out.flush();
			return out.toByteArray();
		} catch (Exception e) {
			log.error("cannot export the table model to PDF: ", e);
			return new byte[0];
		}
	}

	private FOPMediaResource export(Identity downloader, Position position, ExportTableDataModel<?> dataModel, String template, Locale locale)  {
		try(ByteArrayOutputStream out = new ByteArrayOutputStream();
				BufferedOutputStream bout = new BufferedOutputStream(out)) {
			// Setup input for XSLT transformation
			Document doc = createDocument(downloader, position, dataModel, locale);
			transform(doc, bout, template);
			bout.flush();
			out.flush();
			
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			BufferedInputStream bis = new BufferedInputStream(in);
			String charset = WebappHelper.getDefaultCharset();
			return new FOPMediaResource(bis, charset);
		} catch (Exception e) {
			log.error("cannot export the table model to PDF: ", e);
			return null;
		}
	}
	
	public void transform(Document doc, OutputStream out, String templateName) 
	throws TransformerException, FOPException {
        // Setup output
    	// configure fopFactory as desired
		
		URI defaultBaseUri = new File(WebappHelper.getContextRoot()).toURI();
		ResourceResolver resourceResolver = new URIResolverAdapter(new ClasspathURIResolver());
		EnvironmentProfile env = EnvironmentalProfileFactory.createDefault(defaultBaseUri, resourceResolver);

		FopFactoryBuilder config = new FopFactoryBuilder(env);
        FopFactory fopFactory = config.build();

        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        // configure foUserAgent as desired
    	
        // Construct fop with desired output format
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

        Transformer transformer = CoreSpringFactory
        		.getImpl(TemplatesCache.class).getTransformer(templateName);
        // Set the value of a <param> in the stylesheet
        transformer.setParameter("versionParam", "2.0");
        Source src = new DOMSource(doc);

        // Resulting SAX events (the generated FO) must be piped through to FOP
        Result res = new SAXResult(fop.getDefaultHandler());

        // Start XSLT transformation and FOP processing
        transformer.transform(src, res);
	}
	
	private Document createDocument(Identity downloader, Position position, ExportTableDataModel<?> dataModel, Locale locale)
	throws ParserConfigurationException {
		RecruitingModule recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		DocumentBuilderFactory dbf = XMLFactories.newDocumentBuilderFactory();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

		int numOfRows = dataModel.getRowCount();
		int[] columns = dataModel.getExportColumnIndex();
		int numOfColumns = columns.length;

		//set root element
		Element rootEl = doc.createElement("export");
		doc.appendChild(rootEl);
		
		Date date = new Date();
		String dateStr = DateCellRenderer.format(date);
		rootEl.setAttribute("output-date", dateStr);

		//add position informations
		Element positionEl = doc.createElement("position");
		rootEl.appendChild(positionEl);
		appendTextNodeTo(positionEl, "title", position.getMLTitle(locale));
		if(recruitingModule.isPositionPlannigIdEnabled()) {
			appendTextNodeTo(positionEl, "planumber", position.getPlaningsNumber());
		}
		if(recruitingModule.isPositionDepartmentEnabled()) {
			String department = position.getMLDepartement(locale);
			appendTextNodeTo(positionEl, "department", department);
		}
		if(recruitingModule.isPositionHomepageEnabled()) {
			appendTextNodeTo(positionEl, "homepage", position.getHomepage());
		}
		
		if(downloader != null) {
			Element downloaderEl = doc.createElement("downloader");
			rootEl.appendChild(downloaderEl);
			appendTextNodeTo(downloaderEl, "lastname", downloader.getUser().getProperty(UserConstants.LASTNAME, null));
			appendTextNodeTo(downloaderEl, "firstname", downloader.getUser().getProperty(UserConstants.FIRSTNAME, null));
			String downloadDateStr = DateCellRenderer.format(date);
			appendTextNodeTo(downloaderEl, "download-date", downloadDateStr);
		}

		//add header
		Element headers = doc.createElement("headers");
		rootEl.appendChild(headers);
		
		for (int c = 0; c < numOfColumns; c++) {
			String headerVal = dataModel.getHeader(columns[c]);
			Element headerEl = appendTextNode("header", headerVal, doc);
			headerEl.setAttribute("attribute-name", dataModel.getFieldNameAt(columns[c]));
			headers.appendChild(headerEl);
		}
		
		// data
		Element content = doc.createElement("content");
		rootEl.appendChild(content);
		for (int r = 0; r < numOfRows; r++) {
			Element row = doc.createElement("row");
			row.setAttribute("line-number", Integer.toString(r + 1));
			content.appendChild(row);
			for (int c = 0; c < numOfColumns; c++) {
				Class<?> type = dataModel.getTypeAt(r, columns[c]);
				Object obj = dataModel.getValueForExportAt(r, columns[c]);
				Element cellEl = appendCell(type, obj, doc);
				cellEl.setAttribute("attribute-name", dataModel.getFieldNameAt(columns[c]));
				row.appendChild(cellEl);
			}
		}
		
		//print(doc);
		return doc;
	}
	
	public Element appendCell(Class<?> type, Object obj, Document doc) {
		if(obj instanceof Collection) {
			Element cell = doc.createElement("cell");
			Collection<?> collection = (Collection<?>)obj;
			for(Object collObj:collection) {
				if(type.equals(UserRating.class)) {
					appendUserRating(cell, (UserRating)collObj, doc); 
				}
			}
			return cell;
		} else if (Notes.class.equals(type)) {
			return appendNotes((Notes)obj, doc);
		} else {
			String cellValue = obj == null ? "" : obj.toString();
			return appendTextNode("cell", cellValue, doc);
		}
	}
	
	public Element appendNotes(Notes notes, Document doc) {
		Element cell = doc.createElement("cell");
		String content = notes == null ? null : notes.getContent();
		if(StringHelper.containsNonWhitespace(content)) {
			StringBuilder sb = new StringBuilder(300);
			int len = content.length();
			char[] cs = content.toCharArray();
			for (int i = 0; i < len; i++) {
				char c = cs[i];
				switch (c) {
					case '\n':
						cell.appendChild(doc.createTextNode(sb.toString()));
						cell.appendChild(doc.createElement("br"));
						sb = new StringBuilder(300);
						break;
					default:
						sb.append(c);
				}
			}
			cell.appendChild(doc.createTextNode(sb.toString()));
		}
		return cell;
	}
	
	public void appendUserRating(Element cell, UserRating rating, Document doc) {
		Element ratingEl = doc.createElement("rating");
		cell.appendChild(ratingEl);
		if(rating != null && rating.getRating() != null) {
			ratingEl.setAttribute("value", rating.getRating().toString());
		}
	}
	
	public void print(Document document) {
	    try {
	    	TransformerFactory factory = XMLFactories.newTransformerFactory();
			Transformer transformer = factory.newTransformer();
			Source source = new DOMSource(document);
			Result output = new StreamResult(System.out);
			transformer.transform(source, output);
			System.out.println();
		} catch (TransformerFactoryConfigurationError | TransformerException e) {
			log.error("", e);
		}
	  }
	
	public Element appendTextNode(String nodeName, String text, Document doc) {
		Element header = doc.createElement(nodeName);
		header.appendChild(doc.createTextNode(text));
		return header;
	}
	
	public Element appendHtmlNode(String nodeName, String text, Document doc) {
		Element header = doc.createElement(nodeName);
		header.appendChild(doc.createTextNode(text));
		return header;
	}
	
	public void appendTextNodeTo(Element el, String nodeName, String text) {
		if(StringHelper.containsNonWhitespace(text)) {
			Element node = el.getOwnerDocument().createElement(nodeName);
			node.appendChild(el.getOwnerDocument().createTextNode(text));
			el.appendChild(node);
		}
	}
	
	private class ClasspathURIResolver implements URIResolver {
		@Override
		public Source resolve(String href, String base)
		throws TransformerException {
			if(href != null) {
				InputStream in = PDFApplicationCombinedHelper.class.getResourceAsStream(href);
				if(in != null) {
					return new StreamSource(in);
				}
			}
			return null;
		}
	}
}