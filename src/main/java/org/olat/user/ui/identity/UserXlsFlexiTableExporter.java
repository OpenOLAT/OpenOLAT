/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.user.ui.identity;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.XlsFlexiTableExporter;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;

/**
 * 
 * Initial date: 29 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserXlsFlexiTableExporter extends XlsFlexiTableExporter {
	private static final Logger log = Tracing.createLoggerFor(XlsFlexiTableExporter.class);
	private static final String IDENTIFIER = UserConstants.INSTITUTIONALUSERIDENTIFIER.toLowerCase();
	
	@Override
	protected void createCell(FlexiTableComponent ftC, FlexiColumnModel cd, Row dataRow, int row, int col, Translator translator,
			OpenXMLWorkbook workbook) {
		FlexiTableDataModel<?> dataModel = ftC.getFormItem().getTableDataModel();

		try {
			int colIndex = cd.getColumnIndex();
			if(colIndex >= 0) {
				Object value = dataModel.getValueAt(row, colIndex);
				if(cd.getColumnKey().endsWith(IDENTIFIER) && value instanceof String string) {
					dataRow.addCell(col, string, null);
				} else {
					renderValue(ftC, cd.getCellRenderer(), dataRow, value, row, col, translator, workbook);
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

}
