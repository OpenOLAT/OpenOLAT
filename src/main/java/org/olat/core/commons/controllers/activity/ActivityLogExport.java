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
package org.olat.core.commons.controllers.activity;

import java.util.Date;

import org.olat.core.commons.controllers.activity.ActivityLogTableModel.ActivityLogCols;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.XlsFlexiTableExporter;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.lecture.ui.ParticipantListRepositoryController;

/**
 * 
 * Initial date: 12 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ActivityLogExport extends XlsFlexiTableExporter {
	
	@Override
	protected void createCell(FlexiTableComponent ftC, FlexiColumnModel cd, Row dataRow, int row, int col,
			Translator translator, OpenXMLWorkbook workbook) {
		int colIndex = cd.getColumnIndex();
		if (colIndex < ParticipantListRepositoryController.USER_PROPS_OFFSET) {
			switch(ActivityLogCols.values()[colIndex]) {
				case date:
					FlexiTableDataModel<?> dataModel = ftC.getFormItem().getTableDataModel();
					if (dataModel.getValueAt(row, colIndex) instanceof Date date) {
						dataRow.addCell(col, date, workbook.getStyles().getDateTimeStyle());
					}
					break;
				default:
					super.createCell(ftC, cd, dataRow, row, col, translator, workbook);
					break;
			}
		} else {
			super.createCell(ftC, cd, dataRow, row, col, translator, workbook);
		}
	}

}
