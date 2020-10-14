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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.io.LimitedContentWriter;
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
	private String pdfTextBufferPath;
	private String filePath;

	public PdfDocument() {
		this(SearchServiceFactory.getService().getSearchModuleConfig().getPdfTextBufferPath(),
				SearchServiceFactory.getService().getSearchModuleConfig().isPdfExternalIndexer());
	}
	
	public PdfDocument(String pdfTextBufferPath, boolean externalIndexer) {
		this.pdfTextBufferPath = pdfTextBufferPath;
		this.externalIndexer = externalIndexer;
	}
	
	public static Document createDocument(SearchResourceContext leafResourceContext, VFSLeaf leaf) throws IOException,DocumentException,DocumentAccessException {
		PdfDocument textDocument = new PdfDocument();
		textDocument.setFilePath(getPdfTextTmpFilePath(leafResourceContext));
		textDocument.init(leafResourceContext,leaf);
		textDocument.setFileType(FILE_TYPE);
		textDocument.setCssIcon(CSSHelper.createFiletypeIconCssClassFor(leaf.getName()));
		if (log.isDebugEnabled() ) log.debug(textDocument.toString());
		return textDocument.getLuceneDocument();
	}
	
	private void setFilePath(String filePath2) {
		this.filePath = filePath2;
	}
	
	/**
	 * Create a file-path for certain SearchResourceContext.
	 * E.g. '04\1601914104anuale_print.pdf'
	 */
	private static String getPdfTextTmpFilePath(SearchResourceContext leafResourceContext) {
		int hashCode = Math.abs(leafResourceContext.getResourceUrl().hashCode());
		String hashCodeAsString = Integer.toString(hashCode);
		String splitDirName = hashCodeAsString.substring(hashCodeAsString.length()-2);
		String pdfTextTmpFilePath = splitDirName + File.separator + hashCodeAsString + leafResourceContext.getFilePath();
		if (log.isDebugEnabled()) log.debug("PdfTextTmpFilePath={}", pdfTextTmpFilePath);
		return pdfTextTmpFilePath;
	}

	@Override
	protected FileContent readContent(VFSLeaf leaf) throws DocumentException, DocumentAccessException {
		try {
			String bean = externalIndexer ? "pdfExternalIndexer" : "pdfInternalIndexer";
			PdfExtractor extractor = (PdfExtractor)CoreSpringFactory.getBean(bean);
			File pdfTextFile = new File(pdfTextBufferPath, getFilePath() + ".tmp");
			if (isNewPdfFile(leaf, pdfTextFile)) {
				//prepare dirs
				if(!pdfTextFile.getParentFile().exists()) {
					pdfTextFile.getParentFile().mkdirs();
				}
				extractor.extract(leaf, pdfTextFile);
			}

			// text file with extracted text exist => read pdf text from there
			return getPdfTextFromBuffer(pdfTextFile);
		} catch (DocumentAccessException ex) {
			// pass exception
			throw ex;
		} catch (Exception ex) {
			throw new DocumentException("Can not read PDF content. File=" + leaf.getName(), ex);
		} 
	}

	private FileContent getPdfTextFromBuffer(File pdfTextFile) throws IOException {
		if (log.isDebugEnabled()) log.debug("readContent from text file start...");

		try(BufferedReader br = new BufferedReader(new FileReader(pdfTextFile));
				LimitedContentWriter sb = new LimitedContentWriter(5000, FileDocumentFactory.getMaxFileSize())) {
			//search the title
			char[] cbuf = new char[4096];
			int length = br.read(cbuf);
			int indexSep = 0;
			String title = "";
			
			if(length > 0) {
				String firstChunk = new String(cbuf, 0, length);
				indexSep = firstChunk.indexOf("\u00A0|\u00A0");
				if(indexSep > 0) {
					title = firstChunk.substring(0, indexSep);
					sb.append(firstChunk.substring(indexSep + 3));
				} else {
					sb.append(firstChunk);
				}
				while((length = br.read(cbuf)) > 0) {
					sb.write(cbuf, 0, length);
				}
			}
	
			return new FileContent(title, sb.toString());
		} catch(IOException e) {
			log.error("", e);
			throw e;
		}
	}

	private String getFilePath() {
		return filePath;
	}

	private boolean isNewPdfFile(VFSLeaf leaf, File pdfTextFile) {
		if (pdfTextFile == null) {
			return true;
		}
		if (!pdfTextFile.exists()) {
			return true;
		}	
		if (leaf.getLastModified() > pdfTextFile.lastModified() ) {
			// pdf file is newer => delete it
			FileUtils.deleteFile(pdfTextFile);
			return true;
		}
		return false;
	}
}
