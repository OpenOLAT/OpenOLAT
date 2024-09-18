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

import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.SearchServiceFactory;
import org.olat.search.service.document.file.pdf.PdfExtractor;


/**
 * Lucene document mapper.
 * @author Christian Guretzki
 */
public class PdfDocument extends FileDocument {
	private static final long serialVersionUID = 6432923202585881794L;
	private static final Logger log = Tracing.createLoggerFor(PdfDocument.class);

	public static final String FILE_TYPE = "type.file.pdf";
	
	private boolean externalIndexer;

	public PdfDocument() {
		this(SearchServiceFactory.getService().getSearchModuleConfig().isPdfExternalIndexer());
	}
	
	public PdfDocument(boolean externalIndexer) {
		this.externalIndexer = externalIndexer;
	}
	
	public static Document createDocument(SearchResourceContext leafResourceContext, VFSLeaf leaf) throws IOException,DocumentException,DocumentAccessException {
		PdfDocument textDocument = new PdfDocument();
		textDocument.init(leafResourceContext,leaf);
		textDocument.setFileType(FILE_TYPE);
		textDocument.setCssIcon(CSSHelper.createFiletypeIconCssClassFor(leaf.getName()));
		if (log.isDebugEnabled() ) log.debug(textDocument.toString());
		return textDocument.getLuceneDocument();
	}

	@Override
	protected FileContent readContent(VFSLeaf leaf) throws DocumentException, DocumentAccessException {
		try {
			String bean = externalIndexer ? "pdfExternalIndexer" : "pdfInternalIndexer";
			PdfExtractor extractor = CoreSpringFactory.getBean(bean, PdfExtractor.class);
			return extractor.extract(leaf);
		} catch (DocumentAccessException ex) {
			// pass exception
			throw ex;
		} catch (Exception ex) {
			throw new DocumentException("Can not read PDF content. File=" + leaf.getName(), ex);
		} 
	}
}
