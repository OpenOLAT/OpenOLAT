/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.learningpath.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.XlsFlexiTableExporter;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.course.learningpath.ui.LearningPathIdentityDataModel.LearningPathIdentityCols;

/**
 * 
 * Initial date: 18 Sep 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathIdentityExport extends XlsFlexiTableExporter {
	
	@Override
	protected void createCell(FlexiTableComponent ftC, FlexiColumnModel cd, Row dataRow, int row, int col,
			Translator translator, OpenXMLWorkbook workbook) {
		int colIndex = cd.getColumnIndex();
		if (colIndex < LearningPathIdentityDataModel.USER_PROPS_OFFSET) {
			switch(LearningPathIdentityCols.values()[colIndex]) {
				case progress:
					FlexiTableDataModel<?> dataModel = ftC.getFormItem().getTableDataModel();
					if (dataModel.getValueAt(row, colIndex) instanceof LearningPathIdentityRow lpRow) {
						 if (lpRow.getCompletion() != null) {
							dataRow.addCell(col, lpRow.getCompletion(), workbook.getStyles().getPercentStyle());
						}
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
