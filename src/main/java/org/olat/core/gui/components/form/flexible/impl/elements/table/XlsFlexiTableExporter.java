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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.render.EmptyURLBuilder;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.StringOutputPool;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;

/**
 * Export as excel file with POI
 * 
 * Initial date: 11.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class XlsFlexiTableExporter implements FlexiTableExporter {
	private static final Logger log = Tracing.createLoggerFor(XlsFlexiTableExporter.class);
	private static final URLBuilder ubu = new EmptyURLBuilder();
	
	public static final String LINE_BREAK_MARKER = "<LBM>";
	
	private final String filename;
	
	public XlsFlexiTableExporter() {
		this(null);
	}
	
	public XlsFlexiTableExporter(String filename) {
		this.filename = filename;
	}
	
	@Override
	public MediaResource export(FlexiTableComponent ftC, List<FlexiColumnModel> columns, Translator translator) {

		String label = StringHelper.containsNonWhitespace(filename) ? filename :
				"TableExport_"
				+ Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
				+ ".xlsx";
		
		return new OpenXMLWorkbookResource(label) {
			@Override
			protected void generate(OutputStream out) {
				try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
					OpenXMLWorksheet sheet = workbook.nextWorksheet();
					createHeader(columns, translator, sheet, workbook);
					createData(ftC, columns, translator, sheet, workbook);
					if(ftC.getFormItem().getTableDataModel() instanceof FlexiTableFooterModel) {
						createFooter(ftC, columns, translator, sheet, workbook);
					}
				} catch (IOException e) {
					log.error("", e);
				}
			}
		};
	}

	protected void createHeader(List<FlexiColumnModel> columns, Translator translator,
			OpenXMLWorksheet sheet, OpenXMLWorkbook workbook) {
		sheet.setHeaderRows(1);
		Row headerRow = sheet.newRow();
		for (int c=0; c<columns.size(); c++) {
			FlexiColumnModel cd = columns.get(c);
			
			String headerVal = cd.getHeaderLabel() == null ?
					translator.translate(cd.getHeaderKey()) : cd.getHeaderLabel();
			headerRow.addCell(c, headerVal, workbook.getStyles().getHeaderStyle());
		}
	}

	protected void createData(FlexiTableComponent ftC, List<FlexiColumnModel> columns, Translator translator,
			OpenXMLWorksheet sheet, OpenXMLWorkbook workbook) {
		FlexiTableDataModel<?> dataModel = ftC.getFormItem().getTableDataModel();
		
		int numOfRow = dataModel.getRowCount();
		int numOfColumns = columns.size();
		for (int r=0; r<numOfRow; r++) {
			Row dataRow = sheet.newRow();
			for (int c = 0; c<numOfColumns; c++) {
				FlexiColumnModel cd = columns.get(c);
				createCell(ftC, cd, dataRow, r, c, translator, workbook);
			}
		}
	}
	
	protected void createFooter(FlexiTableComponent ftC, List<FlexiColumnModel> columns, Translator translator,
			OpenXMLWorksheet sheet, OpenXMLWorkbook workbook) {
		
		boolean footerHeader = false;
		int numOfCols = columns.size();
		FlexiTableFooterModel footerDataModel = (FlexiTableFooterModel)ftC.getFormItem().getTableDataModel(); 

		Row dataRow = sheet.newRow();
		for (int j = 0; j<numOfCols; j++) {
			FlexiColumnModel cd = columns.get(j);
			int columnIndex = cd.getColumnIndex();
			Object cellValue = columnIndex >= 0 ? footerDataModel.getFooterValueAt(columnIndex) : null;
			if(cellValue == null && !footerHeader) {
				dataRow.addCell(j, footerDataModel.getFooterHeader(), workbook.getStyles().getHeaderStyle());
				footerHeader = true;
			} else {
				Object value = footerDataModel.getFooterValueAt(columnIndex);
				renderValue(ftC, cd.getFooterCellRenderer(), dataRow, value, -1, j, translator, workbook);
			}
		}
	}
	
	protected void createCell(FlexiTableComponent ftC, FlexiColumnModel cd, Row dataRow, int row, int col, Translator translator,
			OpenXMLWorkbook workbook) {
		FlexiTableDataModel<?> dataModel = ftC.getFormItem().getTableDataModel();
		
		try {
			int colIndex = cd.getColumnIndex();
			if(colIndex >= 0) {
				Object value = dataModel.getValueAt(row, colIndex);
				renderValue(ftC, cd.getCellRenderer(), dataRow, value, row, col, translator, workbook);
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	protected void renderValue(FlexiTableComponent ftC, FlexiCellRenderer cellRenderer, Row dataRow, Object value,
			int row, int col, Translator translator, OpenXMLWorkbook workbook) {
		if(value instanceof Date date) {
			dataRow.addCell(col, date, workbook.getStyles().getDateStyle());
		} else if(value instanceof Number number) {
			dataRow.addCell(col, number, null);
		} else if(value instanceof FormLink fLink) {
			String customDisplayText = fLink.getComponent().getCustomDisplayText();
			if(StringHelper.isLong(customDisplayText)) {
				dataRow.addCell(col, Long.valueOf(customDisplayText), null);
			} else if(StringHelper.containsNonWhitespace(customDisplayText)) {
				renderCell(ftC, cellRenderer, dataRow, customDisplayText, row, col, translator);
			}
		} else if(value instanceof FormItem) {
			// do nothing
		} else {
			renderCell(ftC, cellRenderer, dataRow, value, row, col, translator);
		}
	}
	
	protected void renderCell(FlexiTableComponent ftC, FlexiCellRenderer renderer, Row dataRow, Object value, int row, int col, Translator translator) {
		try(StringOutput so = StringOutputPool.allocStringBuilder(1000)) {
			renderer.render(null, so, value, row, ftC, ubu, translator);
			String cellValue = StringOutputPool.freePop(so);
			
			cellValue = StringHelper.stripLineBreaks(cellValue)
					.replace(LINE_BREAK_MARKER, "\r\n");
			cellValue = FilterFactory.getHtmlTagsFilter().filter(cellValue);
			if(StringHelper.containsNonWhitespace(cellValue)) {
				cellValue = StringHelper.unescapeHtml(cellValue);
			}
			if(StringHelper.isLong(cellValue)) {
				dataRow.addCell(col, Long.valueOf(cellValue), null);
			} else {
				dataRow.addCell(col, cellValue, null);
			}
		} catch(IOException e) {
			log.error("", e);
		}
	}
}
