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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
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
public class JExcelTableExport {
	
	public static final float ARIAL_12_LETTER_WIDTH_FACTOR = 1.2f;
	
	private static final Logger log = Tracing.createLoggerFor(JExcelTableExport.class);

	public MediaResource export(String filename, String sheetName, final ExportTableDataModel<?> dataModel, final Translator translator) throws Exception {
		if(!StringHelper.containsNonWhitespace(sheetName)) {
			sheetName = "Sheet";
		}
		
		return new OpenXMLWorkbookResource(filename) {
			@Override
			protected void generate(OutputStream out) {
				try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
					OpenXMLWorksheet sheet = workbook.nextWorksheet();
					sheet.setHeaderRows(1);
					createHeader(dataModel, sheet, workbook);
					createData(dataModel, sheet, workbook, translator);
				} catch (IOException e) {
					log.error("", e);
				}
			}
		};
	}
	
	public byte[] exportAsByteArray(String sheetName, ExportTableDataModel<?> dataModel, Translator translator) throws Exception {
		try(ByteArrayOutputStream bous = new ByteArrayOutputStream();
				BufferedOutputStream ous = new BufferedOutputStream(bous)) {
			if(!StringHelper.containsNonWhitespace(sheetName)) {
				sheetName = "Sheet";
			}

			try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(ous, 1)) {
				OpenXMLWorksheet sheet = workbook.nextWorksheet();
				sheet.setHeaderRows(1);
				createHeader(dataModel, sheet, workbook);
				createData(dataModel, sheet, workbook, translator);
			} catch (Exception e) {
				log.error("", e);
			}
		    
		    ous.flush();
		    bous.flush();

		    return bous.toByteArray();
		} catch (Exception e) {
			log.error("Error while exporting " + sheetName + " to excel.", e);
			return null;
		}
	}
	
	private void createHeader(ExportTableDataModel<?> dataModel, OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		int[] columns = dataModel.getExportColumnIndex();
		int cdcnt = columns.length;

		Row headerRow = exportSheet.newRow();
		exportSheet.setHeaderRows(1);
		for (int c = 0; c < cdcnt; c++) {
			String headerVal = dataModel.getHeader(columns[c]);
			headerRow.addCell(c, headerVal, workbook.getStyles().getHeaderStyle());
		}
	}
	
	private void createData(ExportTableDataModel<?> dataModel, OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook, Translator translator) {
		int[] columns = dataModel.getExportColumnIndex();
		int rcnt = dataModel.getRowCount();
		int cdcnt = columns.length;
		for (int r = 0; r < rcnt; r++) {
			try {
				Row dataRow = exportSheet.newRow();
				for (int c = 0; c < cdcnt; c++) {
					Object obj = dataModel.getValueForExportAt(r, columns[c]);
					Class<?> type = dataModel.getTypeAt(r, columns[c]);
					fillCell(c, obj, type, dataRow, workbook, translator);
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
	
	private int fillCell(int c, Object obj, Class<?> type, Row dataRow, OpenXMLWorkbook workbook, Translator translator) {
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
