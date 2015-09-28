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
package org.olat.core.gui.components.table;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.olat.core.gui.media.CleanupAfterDeliveryFileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.filter.FilterFactory;

/**
 * Description:<br>
 * Export the table as real Excel file with the apache poi library.
 * 
 * <P>
 * Initial Date:  Nov 18, 2010 <br>
 * @author patrick
 */
public class DefaultXlsTableExporter implements TableExporter {
	private CellStyle headerCellStyle;


	/**
	 * @see org.olat.core.gui.components.table.TableExporter#export(org.olat.core.gui.components.table.Table)
	 */
	@Override
	public MediaResource export(final Table table) {
		Translator translator = table.getTranslator();
		int cdcnt = table.getColumnCount();
		int rcnt = table.getRowCount();
		
		Workbook wb = new HSSFWorkbook();
		headerCellStyle = getHeaderCellStyle(wb);
		
		String tableExportTitle = translator.translate("table.export.title");
		String saveTitle = WorkbookUtil.createSafeSheetName(tableExportTitle);
		Sheet exportSheet = wb.createSheet(saveTitle);
		createHeader(table, translator, cdcnt, exportSheet);
		createData(table, cdcnt, rcnt, exportSheet);
		
		return createMediaResourceFromDocument(wb);
	}

	private void createHeader(final Table table, final Translator translator, final int cdcnt, final Sheet exportSheet) {
		
		Row headerRow = exportSheet.createRow(0);
		for (int c = 0; c < cdcnt; c++) {
			ColumnDescriptor cd = table.getColumnDescriptor(c);
			if (cd instanceof StaticColumnDescriptor) {
				// ignore static column descriptors - of no value in excel download!
				continue;
			}
			String headerKey = cd.getHeaderKey();
			String headerVal = cd.translateHeaderKey() ? translator.translate(headerKey) : headerKey;
			Cell cell = headerRow.createCell(c);
			cell.setCellValue(headerVal);
			cell.setCellStyle(headerCellStyle);
		}
	}

	private void createData(final Table table, final int cdcnt, final int rcnt, final Sheet exportSheet) {
		
		for (int r = 0; r < rcnt; r++) {
			Row dataRow = exportSheet.createRow(r+1);
			for (int c = 0; c < cdcnt; c++) {
				ColumnDescriptor cd = table.getColumnDescriptor(c);
				if (cd instanceof StaticColumnDescriptor) {
					// ignore static column descriptors - of no value in excel download!
					continue;
				}
				StringOutput so = new StringOutput();
				cd.renderValue(so, r, null);
				String cellValue = so.toString();
				cellValue = StringHelper.stripLineBreaks(cellValue);
				cellValue = FilterFactory.getHtmlTagsFilter().filter(cellValue);
				if(StringHelper.containsNonWhitespace(cellValue)) {
					cellValue = StringEscapeUtils.unescapeHtml(cellValue);
					if(cellValue.length() >= 32767) {
						cellValue = Formatter.truncate(cellValue, 32760);
					}
				}
				Cell cell = dataRow.createCell(c);
				cell.setCellValue(cellValue);
			}
		}
	}


	private MediaResource createMediaResourceFromDocument(final Workbook wb) {
		FileOutputStream fos = null;
		try {
			File f = new File(WebappHelper.getTmpDir(), "TableExport" + CodeHelper.getUniqueID() + ".xls");
			fos = new FileOutputStream(f);
			wb.write(fos);
			fos.close();
			return new CleanupAfterDeliveryFileMediaResource(f);
		} catch (IOException e) {
			throw new AssertException("error preparing media resource for XLS Table Export", e);
		} finally {
			if(fos != null){
				try {
					fos.close();
				} catch (IOException e1) {
					throw new AssertException("error preparing media resource for XLS Table Export and closing stream", e1);
				}
			}
		}
	}
	
	private CellStyle getHeaderCellStyle(final Workbook wb) {
		CellStyle cellStyle = wb.createCellStyle();
		Font boldFont = wb.createFont();
		boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		cellStyle.setFont(boldFont);
		return cellStyle;
	}
}
