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
package org.olat.modules.lecture.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.XlsFlexiTableExporter;
import org.olat.core.gui.translator.Translator;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.lecture.ui.ParticipantListDataModel.ParticipantsCols;

/**
 * 
 * 
 * Initial date: 10 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ParticipantListExport extends XlsFlexiTableExporter {
	
	private static final Logger log = Tracing.createLoggerFor(ParticipantListExport.class);

	@Override
	protected void createCell(FlexiTableComponent ftC, FlexiColumnModel cd, Row dataRow, int row, int col,
			Translator translator, OpenXMLWorkbook workbook) {
		try {
			int colIndex = cd.getColumnIndex();
			if(colIndex < ParticipantListRepositoryController.USER_PROPS_OFFSET) {
				switch(ParticipantsCols.values()[colIndex]) {
					case rate:
						FlexiTableDataModel<?> dataModel = ftC.getFormItem().getTableDataModel();
						Object rate = dataModel.getValueAt(row, colIndex);
						if(rate instanceof Number) {
							dataRow.addCell(col, (Number)rate, workbook.getStyles().getPercentStyle());
						}
						break;
					default:
						super.createCell(ftC, cd, dataRow, row, col, translator, workbook);
						break;
				}
			} else {
				super.createCell(ftC, cd, dataRow, row, col, translator, workbook);
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
}
