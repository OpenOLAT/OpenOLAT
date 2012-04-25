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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.document.Document;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.xwpf.model.XWPFCommentsDecorator;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.model.XWPFHyperlinkDecorator;
import org.apache.poi.xwpf.model.XWPFParagraphDecorator;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.xmlbeans.XmlException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.search.service.SearchResourceContext;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;

/**
 * 
 * Description:<br>
 * Parse the Word XML document (.docx) with Apache POI
 * 
 * <P>
 * Initial Date:  14 dec. 2009 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class WordOOXMLDocument extends FileDocument {
	private static final long serialVersionUID = 3684533132759600322L;
	private static final OLog log = Tracing.createLoggerFor(WordOOXMLDocument.class);

	public final static String FILE_TYPE = "type.file.word";

	public WordOOXMLDocument() {
		//
	}
	
	public static Document createDocument(SearchResourceContext leafResourceContext, VFSLeaf leaf) throws IOException,DocumentException,DocumentAccessException {
		WordOOXMLDocument wordDocument = new WordOOXMLDocument();
    wordDocument.init(leafResourceContext,leaf);
    wordDocument.setFileType(FILE_TYPE);
		wordDocument.setCssIcon("b_filetype_doc");
		if (log.isDebug()) log.debug(wordDocument.toString());
		return wordDocument.getLuceneDocument();
	}

	protected String readContent(VFSLeaf leaf) throws IOException, DocumentException {
		BufferedInputStream bis = null;
		StringBuilder buffy = new StringBuilder();
		try {
			bis = new BufferedInputStream(leaf.getInputStream());
			POIXMLTextExtractor extractor = (POIXMLTextExtractor) ExtractorFactory.createExtractor(bis);
			POIXMLDocument document = extractor.getDocument();
			
			if (document instanceof XWPFDocument) {
				XWPFDocument xDocument = (XWPFDocument) document;
				XWPFHeaderFooterPolicy hfPolicy = xDocument.getHeaderFooterPolicy();
				extractHeaders(buffy, hfPolicy);
				extractContent(buffy, xDocument);
				extractFooters(buffy, hfPolicy);
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

	private void extractContent(StringBuilder buffy, XWPFDocument document)
	throws IOException, XmlException {
		// first all paragraphs
		Iterator<XWPFParagraph> i = document.getParagraphsIterator();
		while (i.hasNext()) {
			XWPFParagraph paragraph = i.next();
			CTSectPr ctSectPr = null;
			if (paragraph.getCTP().getPPr() != null) {
				ctSectPr = paragraph.getCTP().getPPr().getSectPr();
			}

			XWPFHeaderFooterPolicy headerFooterPolicy = null;
			if (ctSectPr != null) {
				headerFooterPolicy = new XWPFHeaderFooterPolicy(document, ctSectPr);
				extractHeaders(buffy, headerFooterPolicy);
			}

			XWPFParagraphDecorator decorator = new XWPFCommentsDecorator(new XWPFHyperlinkDecorator(paragraph, null, true));

			CTBookmark[] bookmarks = paragraph.getCTP().getBookmarkStartArray();
			for (CTBookmark bookmark : bookmarks) {
				buffy.append(bookmark.getName()).append(' ');
			}

			buffy.append(decorator.getText()).append(' ');

			if (ctSectPr != null) {
				extractFooters(buffy, headerFooterPolicy);
			}
		}
	}

	private void extractFooters(StringBuilder buffy, XWPFHeaderFooterPolicy hfPolicy) {
		if (hfPolicy.getFirstPageFooter() != null) {
			buffy.append(hfPolicy.getFirstPageFooter().getText()).append(' ');
		}
		if (hfPolicy.getEvenPageFooter() != null) {
			buffy.append(hfPolicy.getEvenPageFooter().getText()).append(' ');
		}
		if (hfPolicy.getDefaultFooter() != null) {
			buffy.append(hfPolicy.getDefaultFooter().getText()).append(' ');
		}
	}

	private void extractHeaders(StringBuilder buffy, XWPFHeaderFooterPolicy hfPolicy) {
		if (hfPolicy.getFirstPageHeader() != null) {
			buffy.append(hfPolicy.getFirstPageHeader().getText()).append(' ');
		}
		if (hfPolicy.getEvenPageHeader() != null) {
			buffy.append(hfPolicy.getEvenPageHeader().getText()).append(' ');
		}
		if (hfPolicy.getDefaultHeader() != null) {
			buffy.append(hfPolicy.getDefaultHeader().getText()).append(' ');
		}
	}
}