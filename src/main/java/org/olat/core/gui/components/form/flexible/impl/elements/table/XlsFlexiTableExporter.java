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

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.render.EmptyURLBuilder;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.StringOutputPool;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.apache.logging.log4j.Logger;
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
	
	@Override
	public MediaResource export(FlexiTableComponent ftC, List<FlexiColumnModel> columns, Translator translator) {

		String label = "TableExport_"
				+ Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
				+ ".xlsx";
		
		return new OpenXMLWorkbookResource(label){
			@Override
			protected void generate(OutputStream out) {
				try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
					OpenXMLWorksheet sheet = workbook.nextWorksheet();
					createHeader(columns, translator, sheet, workbook);
					createData(ftC, columns, translator, sheet, workbook);
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
		FlexiTableDataModel<?> dataModel = ftC.getFlexiTableElement().getTableDataModel();
		
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
	
	protected void createCell(FlexiTableComponent ftC, FlexiColumnModel cd, Row dataRow, int row, int col, Translator translator,
			OpenXMLWorkbook workbook) {
		FlexiTableDataModel<?> dataModel = ftC.getFlexiTableElement().getTableDataModel();
		
		try {
			int colIndex = cd.getColumnIndex();
			if(colIndex >= 0) {
				Object value = dataModel.getValueAt(row, colIndex);
				if(value instanceof Date) {
					dataRow.addCell(col, (Date)value, workbook.getStyles().getDateStyle());
				} else if(value instanceof Number) {
					dataRow.addCell(col, (Number)value, null);
				} else if(value instanceof FormItem) {
					// do nothing
				} else {
					StringOutput so = StringOutputPool.allocStringBuilder(1000);
					cd.getCellRenderer().render(null, so, value, row, ftC, ubu, translator);
					String cellValue = StringOutputPool.freePop(so);
					
					cellValue = StringHelper.stripLineBreaks(cellValue);
					cellValue = FilterFactory.getHtmlTagsFilter().filter(cellValue);
					if(StringHelper.containsNonWhitespace(cellValue)) {
						cellValue = StringEscapeUtils.unescapeHtml(cellValue);
					}
					dataRow.addCell(col, cellValue, null);
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
}
