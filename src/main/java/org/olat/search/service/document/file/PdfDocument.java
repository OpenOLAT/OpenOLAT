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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.SearchServiceFactory;


/**
 * Lucene document mapper.
 * @author Christian Guretzki
 */
public class PdfDocument extends FileDocument {
	private static final long serialVersionUID = 6432923202585881794L;
	private static OLog log = Tracing.createLoggerFor(PdfDocument.class);

	public final static String FILE_TYPE = "type.file.pdf";
	
	private boolean pdfTextBuffering;

	private String pdfTextBufferPath;

	private String filePath;

	public PdfDocument() {
		pdfTextBuffering = SearchServiceFactory.getService().getSearchModuleConfig().getPdfTextBuffering();
		pdfTextBufferPath = SearchServiceFactory.getService().getSearchModuleConfig().getPdfTextBufferPath(); 
	}
	
	public static Document createDocument(SearchResourceContext leafResourceContext, VFSLeaf leaf) throws IOException,DocumentException,DocumentAccessException {
    PdfDocument textDocument = new PdfDocument();
    textDocument.setFilePath(getPdfTextTmpFilePath(leafResourceContext));
    textDocument.init(leafResourceContext,leaf);
    textDocument.setFileType(FILE_TYPE);
		textDocument.setCssIcon("b_filetype_pdf");
		if (log.isDebug() ) log.debug(textDocument.toString());
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
		if (log.isDebug()) log.debug("PdfTextTmpFilePath=" + pdfTextTmpFilePath);
		return pdfTextTmpFilePath;
	}

	protected String readContent(VFSLeaf leaf) throws DocumentException, DocumentAccessException {
		try {
			long startTime = 0;
			if (log.isDebug()) startTime = System.currentTimeMillis();
			String pdfText = null;
			String fullPdfTextTmpFilePath = pdfTextBufferPath + File.separator + getFilePath() + ".tmp";
			File pdfTextFile = new File(fullPdfTextTmpFilePath);
			if (pdfTextBuffering && !isNewPdfFile(leaf,pdfTextFile) ) {
				// text file with extracted text exist => read pdf text from there
				pdfText = getPdfTextFromBuffer(pdfTextFile);
			} else {
				// no text file with extracted text exist => extract text from pdf
				pdfText = extractTextFromPdf(leaf);
				if (pdfTextBuffering) {
					// store extracted pdf-text in 
					storePdfTextInBuffer(pdfText,fullPdfTextTmpFilePath,pdfTextFile);
				}
				if (log.isDebug()) log.debug("readContent from pdf done.");
			}
			if (log.isDebug()) {
  			long time = System.currentTimeMillis() - startTime;
	  		log.debug("readContent time=" + time);
			}
		  return pdfText;
		} catch (DocumentAccessException ex) {
			// pass exception
			throw ex;
		} catch (Exception ex) {
			throw new DocumentException("Can not read PDF content. File=" + leaf.getName() + ";" + ex.getMessage() );
		} 
	}

	private void storePdfTextInBuffer(String pdfText, String fullPdfTextTmpFilePath, File pdfTextFile) throws IOException {
		int lastSlash = fullPdfTextTmpFilePath.lastIndexOf('/');
		String dirPath = fullPdfTextTmpFilePath.substring(0,lastSlash);
		File dirFile = new File(dirPath);
		dirFile.mkdirs();
		FileUtils.save(new FileOutputStream(pdfTextFile), pdfText, "utf-8");
	}

	private String extractTextFromPdf(VFSLeaf leaf) throws IOException, DocumentAccessException {
		if (log.isDebug()) log.debug("readContent from pdf starts...");
		PDDocument document = null;
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(leaf.getInputStream());			
			document = PDDocument.load(bis);
			if (document.isEncrypted()) {
				try {
					document.decrypt("");
				} catch (Exception e) {
					throw new DocumentAccessException("PDF is encrypted. Can not read content file=" + leaf.getName());
				}
			}			
			if (log.isDebug()) log.debug("readContent PDDocument loaded");
			PDFTextStripper stripper = new PDFTextStripper();
			return stripper.getText(document);
		} finally {
			if (document != null) {
			  document.close();
			}
			if (bis != null) {
				bis.close();
			}
		}

	}

	private String getPdfTextFromBuffer(File pdfTextFile) throws IOException {
		if (log.isDebug()) log.debug("readContent from text file start...");
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(pdfTextFile));
			String pdfText = FileUtils.load(bis, "utf-8");
			if (log.isDebug()) log.debug("readContent from text file done.");
			return pdfText;
		} finally {
			if (bis != null) {
				bis.close();
			}
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
			pdfTextFile.delete();
			return true;
		}
		return false;
	}

}
