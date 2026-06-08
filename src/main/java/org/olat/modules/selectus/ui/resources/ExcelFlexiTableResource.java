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
package org.olat.modules.selectus.ui.resources;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.selectus.ui.components.ExportTableDataModel;


/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  10 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ExcelFlexiTableResource extends OpenXMLWorkbookResource {
	
	private static final Logger log = Tracing.createLoggerFor(ExcelFlexiTableResource.class);
	
	protected final ExportTableDataModel<?> dataModel;
	protected final Translator translator;
	
	public ExcelFlexiTableResource(String filename, final ExportTableDataModel<?> dataModel, final Translator translator) {
		super(filename);
		this.dataModel = dataModel;
		this.translator = translator;
	}
	
	@Override
	protected void generate(OutputStream out) {
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			sheet.setHeaderRows(1);
			createHeader(sheet, workbook);
			createData(sheet, workbook);
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	protected void createHeader(OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		int[] columns = dataModel.getExportColumnIndex();
		int cdcnt = columns.length;

		Row headerRow = exportSheet.newRow();
		exportSheet.setHeaderRows(1);
		int c = 0;
		for ( ; c < cdcnt; c++) {
			String headerVal = dataModel.getHeader(columns[c]);
			headerRow.addCell(c, headerVal, workbook.getStyles().getHeaderStyle());
		}
		createHeader(headerRow, c, workbook);
	}
	
	/**
	 * This method is there to override.
	 * 
	 * @param headerRow The header row
	 * @param currentPos The current position / column
	 * @param workbook The workbook
	 */
	protected void createHeader(Row headerRow, int currentPos, OpenXMLWorkbook workbook) {
		//do something
	}
	
	protected void createData(OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		int[] columns = dataModel.getExportColumnIndex();
		int rcnt = dataModel.getRowCount();
		for (int r = 0; r < rcnt; r++) {
			try {
				createData(exportSheet, workbook, r, columns);
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
	
	protected void createData(OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook, int row, int[] columns) {
		Row dataRow = exportSheet.newRow();
		
		int c = 0;
		for ( ; c < columns.length; c++) {
			Object obj = dataModel.getValueForExportAt(row, columns[c]);
			Class<?> type = dataModel.getTypeAt(row, columns[c]);
			fillCell(c, obj, type, dataRow, workbook);
		}
		
		createData(exportSheet, dataRow, row, c, workbook);
	}
	
	/**
	 * This method is there to override.
	 * 
	 * @param exportSheet The sheet
	 * @param dataRow The current row of the sheet
	 * @param row The row number in the data model
	 * @param pos The position or column
	 * @param workbook The workbook
	 */
	protected void createData(OpenXMLWorksheet exportSheet, Row dataRow, int row, int pos, OpenXMLWorkbook workbook) {
		//dom something
	}
	
	private int fillCell(int c, Object obj, Class<?> type, Row dataRow, OpenXMLWorkbook workbook) {
		int length = -1;
		if(obj == null) {
			//
		} else if(type == null || Object.class.equals(type)) {
			String value = obj.toString();
			dataRow.addCell(c, value, null);
			length = value.length();
		} else if (String.class.equals(type) && obj instanceof String) {
			String value = (String)obj;
			dataRow.addCell(c, value, null);
			length = value.length();
		} else if (Integer.class.equals(type) && obj instanceof Integer) {
			Integer value = (Integer)obj;
			dataRow.addCell(c, value, workbook.getStyles().getIntegerStyle());
		} else if (obj instanceof Number) {
			Number value = (Number)obj;
			dataRow.addCell(c, value, null);
		} else if (Date.class.equals(type) && obj instanceof Date) {
			Date value = (Date)obj;
			dataRow.addCell(c, value, workbook.getStyles().getDateStyle());
		} else if (Boolean.class.equals(type) && obj instanceof Boolean) {
			Boolean value = (Boolean)obj;
			dataRow.addCell(c, value.booleanValue() ? translator.translate("yes") : translator.translate("no"));
		} else {
			String value = obj.toString();
			dataRow.addCell(c, value, null);
			length = value.length();
		}
		return length;
	}
}
