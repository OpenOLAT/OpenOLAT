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
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.CellType;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.io.LimitedContentWriter;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.search.service.SearchResourceContext;

/**
 * Lucene document mapper.
 * 
 * @author Christian Guretzki
 */
public class ExcelDocument extends FileDocument {
	private static final long serialVersionUID = 1592080527374169362L;
	private static final OLog log = Tracing.createLoggerFor(ExcelDocument.class);

	public final static String FILE_TYPE = "type.file.excel";

	public ExcelDocument() {
		//
	}

	public static Document createDocument(SearchResourceContext leafResourceContext, VFSLeaf leaf)
	throws IOException, DocumentException, DocumentAccessException {
		ExcelDocument excelDocument = new ExcelDocument();
		excelDocument.init(leafResourceContext, leaf);
		excelDocument.setFileType(FILE_TYPE);
		excelDocument.setCssIcon(CSSHelper.createFiletypeIconCssClassFor(leaf.getName()));
		if (log.isDebug()) log.debug(excelDocument.toString());
		return excelDocument.getLuceneDocument();
	}

	@Override
	protected FileContent readContent(VFSLeaf leaf) throws IOException, DocumentException {
		
		int cellNullCounter = 0;
		int rowNullCounter = 0;
		int sheetNullCounter = 0;

		try(BufferedInputStream bis = new BufferedInputStream(leaf.getInputStream());
				HSSFWorkbook workbook = new HSSFWorkbook(new POIFSFileSystem(bis));
				LimitedContentWriter content = new LimitedContentWriter((int)leaf.getSize(), FileDocumentFactory.getMaxFileSize())) {
			for (int sheetNumber = 0; sheetNumber < workbook.getNumberOfSheets(); sheetNumber++) {
				HSSFSheet sheet = workbook.getSheetAt(sheetNumber);
				if (sheet != null) {
					for (int rowNumber = sheet.getFirstRowNum(); rowNumber <= sheet.getLastRowNum(); rowNumber++) {
						HSSFRow row = sheet.getRow(rowNumber);
						if (row != null) {
							for (int cellNumber = row.getFirstCellNum(); cellNumber <= row.getLastCellNum(); cellNumber++) {
								HSSFCell cell = row.getCell(cellNumber);
								if (cell != null) {
									if (cell.getCellType() == CellType.STRING) {
										content.append(cell.getStringCellValue()).append(' ');
									}
								} else {
									cellNullCounter++;
								}
							}
						} else {
							rowNullCounter++;
						}
					}
				} else {
					sheetNullCounter++;
				}
			}
			if (log.isDebug()) {
				if ((cellNullCounter > 0) || (rowNullCounter > 0) || (sheetNullCounter > 0)) {
					log.debug("Read Excel content cell=null #:" + cellNullCounter + ", row=null #:" + rowNullCounter + ", sheet=null #:"
							+ sheetNullCounter);
				}
			}
			return new FileContent(content.toString());
		} catch (Exception ex) {
			throw new DocumentException("Can not read XLS Content. File=" + leaf.getName(), ex);
		}
	}
}
