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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.search.service.document.file;

import java.io.BufferedInputStream;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.xslf.XSLFSlideShow;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.xmlbeans.XmlException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.search.service.SearchResourceContext;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRegularTextRun;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;
import org.openxmlformats.schemas.presentationml.x2006.main.CTComment;
import org.openxmlformats.schemas.presentationml.x2006.main.CTCommentList;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTNotesSlide;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlide;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideIdListEntry;

/**
 * 
 * Description:<br>
 * Parse the PowerPoint XML document (.pptx) with Apache POI
 * 
 * <P>
 * Initial Date:  14 dec. 2009 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class PowerPointOOXMLDocument extends FileDocument {
	private static final long serialVersionUID = 2322994231200065526L;
	private static final OLog log = Tracing.createLoggerFor(PowerPointOOXMLDocument.class);

	public final static String FILE_TYPE = "type.file.ppt";

	public PowerPointOOXMLDocument() {
		//
	}

	public static Document createDocument(SearchResourceContext leafResourceContext, VFSLeaf leaf) throws IOException, DocumentException,
			DocumentAccessException {
		PowerPointOOXMLDocument powerPointDocument = new PowerPointOOXMLDocument();
		powerPointDocument.init(leafResourceContext, leaf);
		powerPointDocument.setFileType(FILE_TYPE);
		powerPointDocument.setCssIcon("b_filetype_ppt");
		if (log.isDebug()) log.debug(powerPointDocument.toString());
		return powerPointDocument.getLuceneDocument();
	}

	public String readContent(VFSLeaf leaf) throws IOException, DocumentException {
		BufferedInputStream bis = null;
		StringBuilder buffy = new StringBuilder();
		try {
			bis = new BufferedInputStream(leaf.getInputStream());
			POIXMLTextExtractor extractor = (POIXMLTextExtractor) ExtractorFactory.createExtractor(bis);
			POIXMLDocument document = extractor.getDocument();

			if (document instanceof XSLFSlideShow) {
				XSLFSlideShow slideShow = (XSLFSlideShow) document;
				XMLSlideShow xmlSlideShow = new XMLSlideShow(slideShow);
				extractContent(buffy, xmlSlideShow);
			}

			return buffy.toString();
		} catch (Exception e) {
			throw new DocumentException(e.getMessage());
		} finally {
			if (bis != null) {
				bis.close();
			}
		}
	}

	private void extractContent(StringBuilder buffy, XMLSlideShow xmlSlideShow) throws IOException, XmlException {
		XSLFSlide[] slides = xmlSlideShow.getSlides();
		for (XSLFSlide slide : slides) {
			CTSlide rawSlide = slide._getCTSlide();
			CTSlideIdListEntry slideId = slide._getCTSlideId();

			CTNotesSlide notes = xmlSlideShow._getXSLFSlideShow().getNotes(slideId);
			CTCommentList comments = xmlSlideShow._getXSLFSlideShow().getSlideComments(slideId);

			extractShapeContent(buffy, rawSlide.getCSld().getSpTree());

			if (comments != null) {
				for (CTComment comment : comments.getCmArray()) {
					buffy.append(comment.getText()).append(' ');
				}
			}

			if (notes != null) {
				extractShapeContent(buffy, notes.getCSld().getSpTree());
			}
		}
	}

	private void extractShapeContent(StringBuilder buffy, CTGroupShape gs) {
		CTShape[] shapes = gs.getSpArray();
		for (CTShape shape : shapes) {
			CTTextBody textBody = shape.getTxBody();
			if (textBody != null) {
				CTTextParagraph[] paras = textBody.getPArray();
				for (CTTextParagraph textParagraph : paras) {
					CTRegularTextRun[] textRuns = textParagraph.getRArray();
					for (CTRegularTextRun textRun : textRuns) {
						buffy.append(textRun.getT()).append(' ');
					}
				}
			}
		}
	}
}