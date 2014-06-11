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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.WorkbookMediaResource;
import org.olat.core.gui.render.EmptyURLBuilder;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.StringOutputPool;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;

/**
 * Export as excel file with POI
 * 
 * Initial date: 11.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class XlsFlexiTableExporter implements FlexiTableExporter {
	private static final URLBuilder ubu = new EmptyURLBuilder();
	
	private CellStyle headerCellStyle;

	public MediaResource export(FlexiTableComponent ftC, List<FlexiColumnModel> columns, Translator translator) {
		Workbook wb = new HSSFWorkbook();
		headerCellStyle = getHeaderCellStyle(wb);
		
		Sheet exportSheet = wb.createSheet("Sheet 1");
		createHeader(columns, translator, exportSheet);
		createData(ftC, columns, translator, exportSheet);
		
		return new WorkbookMediaResource(wb);
	}

	private void createHeader(List<FlexiColumnModel> columns, Translator translator, Sheet sheet) {
		Row headerRow = sheet.createRow(0);
		for (int c=0; c<columns.size(); c++) {
			FlexiColumnModel cd = columns.get(c);
			String headerVal = cd.getHeaderLabel() == null ?
					translator.translate(cd.getHeaderKey()) : cd.getHeaderLabel();
			Cell cell = headerRow.createCell(c);
			cell.setCellValue(headerVal);
			cell.setCellStyle(headerCellStyle);
		}
	}

	private void createData(FlexiTableComponent ftC, List<FlexiColumnModel> columns, Translator translator, Sheet sheet) {
		FlexiTableDataModel<?> dataModel = ftC.getFlexiTableElement().getTableDataModel();
		
		int numOfRow = dataModel.getRowCount();
		int numOfColumns = columns.size();
		for (int r=0; r<numOfRow; r++) {
			Row dataRow = sheet.createRow(r+1);
			for (int c = 0; c<numOfColumns; c++) {
				FlexiColumnModel cd = columns.get(c);
				Cell cell = dataRow.createCell(c);
				int colIndex = cd.getColumnIndex();
				if(colIndex >= 0) {
					Object value = dataModel.getValueAt(r, colIndex);
					renderCell(cell, value, r, ftC, cd, translator);
				}
			}
		}
	}
	
	protected void renderCell(Cell cell,Object value, int row, FlexiTableComponent ftC, FlexiColumnModel cd, Translator translator) {
		StringOutput so = StringOutputPool.allocStringBuilder(1000);
		cd.getCellRenderer().render(null, so, value, row, ftC, ubu, translator);

		String cellValue = StringOutputPool.freePop(so);
		cellValue = StringHelper.stripLineBreaks(cellValue);
		cellValue = FilterFactory.getHtmlTagsFilter().filter(cellValue);
		if(StringHelper.containsNonWhitespace(cellValue)) {
			cellValue = StringEscapeUtils.unescapeHtml(cellValue);
		}
		cell.setCellValue(cellValue);
	}
	
	public static CellStyle getHeaderCellStyle(final Workbook wb) {
		CellStyle cellStyle = wb.createCellStyle();
		Font boldFont = wb.createFont();
		boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		cellStyle.setFont(boldFont);
		return cellStyle;
	}
}
