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

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.olat.core.commons.services.search.SearchModule;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.search.service.SearchResourceContext;

/**
 * Lucene document mapper.
 * <p>Supported file-types : 
 * <lu>
 * <li>pdf => PDF document</li>
 * <li>xls => Excel document</li>
 * <li>doc => Word document</li>
 * <li>ppt => Power-point document</li>
 * <li>odt, ods, odp, odf, odg => OpenDocument document</li>
 * <li>htm, html, xhtml, xml => HTML document</li>
 * <li>txt, tex, README, csv => Text document</li>
 * @author Christian Guretzki
 */
public class FileDocumentFactory {
	
	private static OLog log = Tracing.createLoggerFor(FileDocumentFactory.class);

	static FileDocumentFactory instance;

	private static boolean pptFileEnabled;
	private static boolean excelFileEnabled;

	private final static String PDF_SUFFIX = "pdf";
	private final static String EXCEL_SUFFIX = "xls";
	private final static String WORD_SUFFIX = "doc";
	private final static String POWERPOINT_SUFFIX = "ppt";
	private final static String EXCEL_X_SUFFIX = "xlsx";
	private final static String WORD_X_SUFFIX = "docx";
	private final static String POWERPOINT_X_SUFFIX = "pptx";
	private final static String OD_TEXT_SUFFIX = "odt";
	private final static String OD_SPREADSHEET_SUFFIX = "ods";
	private final static String OD_PRESENTATION_SUFFIX = "odp";
	private final static String OD_FORMULA_SUFFIX = "odf";
	private final static String OD_GRAPHIC_SUFFIX = "odg";

	private final static String HTML_SUFFIX = "htm html xhtml";
	private final static String XML_SUFFIX = "xml";
	private final static String TEXT_SUFFIX = "txt tex readme csv";
  
  //as a special parser;
  private static final String IMS_MANIFEST_FILE = "imsmanifest.xml";
  
  private static List<String> checkFileSizeSuffixes;
  private static long maxFileSize;
  private int  excludedFileSizeCount = 0;

	private List<String> fileBlackList; 
  
	/**
	 * [used by spring]
	 * @param searchModule
	 */
	public FileDocumentFactory(SearchModule searchModule) {
		instance = this;
		fileBlackList = searchModule.getFileBlackList();
		pptFileEnabled = searchModule.getPptFileEnabled();
		if (!pptFileEnabled) log.info("PPT files are disabled in indexer.");
		excelFileEnabled = searchModule.getExcelFileEnabled();
		if (!excelFileEnabled) log.info("Excel files are disabled in indexer.");
		checkFileSizeSuffixes = searchModule.getFileSizeSuffixes();
		maxFileSize = searchModule.getMaxFileSize();
	}
	
	public static Document createDocument(SearchResourceContext leafResourceContext, VFSLeaf leaf)
	throws DocumentNotImplementedException, IOException, DocumentException, DocumentAccessException {
		
		String fileName = leaf.getName();
		String suffix = getSuffix(fileName);
		if (log.isDebug()) log.debug("suffix=" + suffix);

		if (PDF_SUFFIX.indexOf(suffix) >= 0) {
			return PdfDocument.createDocument(leafResourceContext, leaf);
		}
		if (HTML_SUFFIX.indexOf(suffix) >= 0) {
			return HtmlDocument.createDocument(leafResourceContext, leaf);
		}
		if (XML_SUFFIX.indexOf(suffix) >= 0) {
			if(IMS_MANIFEST_FILE.equals(fileName)) {
				return IMSMetadataDocument.createDocument(leafResourceContext, leaf);
			}
			return XmlDocument.createDocument(leafResourceContext, leaf);
		}
		if (TEXT_SUFFIX.indexOf(suffix) >= 0) {
			return TextDocument.createDocument(leafResourceContext, leaf);
		}

		//microsoft openxml
		if (suffix.indexOf(WORD_X_SUFFIX) >= 0) {
			return WordOOXMLDocument.createDocument(leafResourceContext, leaf);
		}
		if (suffix.indexOf(EXCEL_X_SUFFIX) >= 0) {
			if (excelFileEnabled)
				return ExcelOOXMLDocument.createDocument(leafResourceContext, leaf);
			return null;
		}
		if (suffix.indexOf(POWERPOINT_X_SUFFIX) >= 0) {
			if(pptFileEnabled)
				return PowerPointOOXMLDocument.createDocument(leafResourceContext, leaf);
			return null;
		}

		//microsoft
		if (WORD_SUFFIX.indexOf(suffix) >= 0) {
			return WordDocument.createDocument(leafResourceContext, leaf);
		}
		if (POWERPOINT_SUFFIX.indexOf(suffix) >= 0) {
			if(pptFileEnabled)
				return PowerPointDocument.createDocument(leafResourceContext, leaf);
			return null;
		}
		if (EXCEL_SUFFIX.indexOf(suffix) >= 0) {
			if (excelFileEnabled)
				return ExcelDocument.createDocument(leafResourceContext, leaf);
			return null;
		}
		
		//open document
		if (OD_TEXT_SUFFIX.indexOf(suffix) >= 0 || OD_SPREADSHEET_SUFFIX.indexOf(suffix) >= 0
				|| OD_PRESENTATION_SUFFIX.indexOf(suffix) >= 0 || OD_FORMULA_SUFFIX.indexOf(suffix) >= 0
				|| OD_GRAPHIC_SUFFIX.indexOf(suffix) >= 0) {
			return OpenDocument.createDocument(leafResourceContext, leaf);
		}
		
		return UnkownDocument.createDocument(leafResourceContext, leaf);
	}

	private static String getSuffix(String fileName) throws DocumentNotImplementedException {
		int dotpos = fileName.lastIndexOf('.');
		if (dotpos < 0 || dotpos == fileName.length() - 1) {
			if (log.isDebug()) log.debug("I cannot detect the document suffix (marked with '.').");
			throw new DocumentNotImplementedException("I cannot detect the document suffix (marked with '.') for " + fileName);
		}
		String suffix = fileName.substring(dotpos+1).toLowerCase();
		return suffix;
	}
	
	/**
	 * Check if certain file is supported.
	 * @param fileName
	 * @return
	 */
	public boolean isFileSupported(VFSLeaf leaf) {
		String fileName = leaf.getName();
		if (fileName != null && fileName.startsWith(".")) {
			//don't index all mac os x hidden files
			return false;
		}
		
		String suffix;
		try {
			suffix = getSuffix(fileName);
		} catch (DocumentNotImplementedException e) {
			return false;
		}
		
		// 1. Check if file is not on fileBlackList
		if (fileBlackList.contains(fileName)) {
			// File name is on blacklist 
			return false;
		}
		// 2. Check for certain file-type the file size
		if (checkFileSizeSuffixes.contains(suffix)) {
			if ( (maxFileSize != 0) && (leaf.getSize() > maxFileSize) ) {
				log.info("File too big, exlude from search index. filename=" + fileName);
				excludedFileSizeCount++;
				return false;
			}
		}
		/* 3. Check if suffix is supported
		if (supportedSuffixes.indexOf(suffix) >= 0) {
			return true;
		}*/
		//index all files (index metadatas)
		return true;
	}
	
	public int getExcludedFileSizeCount( ) {
	  return excludedFileSizeCount;
	}

	public void resetExcludedFileSizeCount( ) {
	  excludedFileSizeCount = 0;
	}

}
