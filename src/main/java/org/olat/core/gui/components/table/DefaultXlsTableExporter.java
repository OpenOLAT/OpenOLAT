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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;

/**
 * Description:<br>
 * Export the table as real Excel file with the apache poi library.
 * 
 * <P>
 * Initial Date:  Nov 18, 2010 <br>
 * @author patrick
 */
public class DefaultXlsTableExporter implements TableExporter {
	private static final OLog log = Tracing.createLoggerFor(DefaultXlsTableExporter.class);

	/**
	 * @see org.olat.core.gui.components.table.TableExporter#export(org.olat.core.gui.components.table.Table)
	 */
	@Override
	public MediaResource export(final Table table) {
		Translator translator = table.getTranslator();
		int cdcnt = table.getColumnCount();
		int rcnt = table.getRowCount();
		
		String label = "TableExport_"
				+ Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
				+ ".xlsx";

		return new OpenXMLWorkbookResource(label) {
			@Override
			protected void generate(OutputStream out) {
				try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
					OpenXMLWorksheet sheet = workbook.nextWorksheet();
					createHeader(table, translator, cdcnt, sheet, workbook);
					createData(table, cdcnt, rcnt, sheet);
				} catch (IOException e) {
					log.error("", e);
				}
			}
		};
	}

	private void createHeader(final Table table, final Translator translator, final int cdcnt,
			final OpenXMLWorksheet exportSheet, final OpenXMLWorkbook workbook) {
		Row headerRow = exportSheet.newRow();
		exportSheet.setHeaderRows(1);
		for (int c = 0; c < cdcnt; c++) {
			ColumnDescriptor cd = table.getColumnDescriptor(c);
			if (cd instanceof StaticColumnDescriptor) {
				// ignore static column descriptors - of no value in excel download!
				continue;
			}
			String headerKey = cd.getHeaderKey();
			String headerVal = cd.translateHeaderKey() ? translator.translate(headerKey) : headerKey;
			headerRow.addCell(c, headerVal, workbook.getStyles().getHeaderStyle());
		}
	}

	private void createData(final Table table, final int cdcnt, final int rcnt, final OpenXMLWorksheet exportSheet) {
		for (int r = 0; r < rcnt; r++) {
			Row dataRow = exportSheet.newRow();
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
					cellValue = StringHelper.unescapeHtml(cellValue);
					if(cellValue.length() >= 32767) {
						cellValue = Formatter.truncate(cellValue, 32760);
					}
				}
				dataRow.addCell(c, cellValue, null);
			}
		}
	}
}
