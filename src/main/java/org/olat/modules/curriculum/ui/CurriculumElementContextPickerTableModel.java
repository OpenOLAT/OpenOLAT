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
package org.olat.modules.curriculum.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * Initial date: 4 May 2026<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class CurriculumElementContextPickerTableModel extends DefaultFlexiTableDataModel<CurriculumElementContextPickerRow> {

	private static final PickerCols[] COLS = PickerCols.values();

	public CurriculumElementContextPickerTableModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		CurriculumElementContextPickerRow pickerRow = getObject(row);
		return switch(COLS[col]) {
			case displayName -> pickerRow;
			case externalRef -> pickerRow.getIdentifier();
			case beginDate -> pickerRow.getBeginDate();
			case endDate -> pickerRow.getEndDate();
			case numOfCourses -> pickerRow.getNumOfResources();
			case numOfEvents -> pickerRow.getNumOfLectureBlocks();
		};
	}

	public enum PickerCols implements FlexiColumnDef {
		displayName("table.header.title"),
		externalRef("table.header.external.ref"),
		beginDate("table.header.begin.date"),
		endDate("table.header.end.date"),
		numOfCourses("table.header.num.of.courses"),
		numOfEvents("table.header.num.of.lecture.blocks");

		private final String i18nKey;

		private PickerCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
