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
package org.olat.modules.selectus.ui.position;

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.position.model.EditStepRow;

/**
 * 
 * Initial date: 12 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditApplicationEditStepsTableModel extends DefaultFlexiTableDataModel<EditStepRow> {
	
	private static final EditStepCols[] COLS = EditStepCols.values();
	
	public PositionEditApplicationEditStepsTableModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		EditStepRow stepRow = getObject(row);
		switch(COLS[col]) {
			case name: return getName(stepRow);
			case edit: return stepRow.getEditLabelButton();
			case status: return stepRow;
			case infos: return stepRow.getExplain();
			case enable: return stepRow.isConfigurable() ? Boolean.valueOf(stepRow.isEnabled()) : null;
			case delete: return stepRow.step().customStep() ? Boolean.TRUE : null;
			case up: return isUpEnabled(stepRow, row);
			case down: return isDownEnabled(stepRow, row);
			default: return "ERROR";
		}
	}
	
	private Boolean isUpEnabled(EditStepRow stepRow, int row) {
		int previousIndex = row - 1;
		if(stepRow.step().customStep() && previousIndex >= 0
				&& getObject(previousIndex) != null && getObject(previousIndex).step().customStep()) {
			return Boolean.TRUE;
		}
		return null;
	}
	
	private Boolean isDownEnabled(EditStepRow stepRow, int row) {
		int nextIndex = row + 1;
		if(stepRow.step().customStep() && nextIndex >= 0 && nextIndex < getRowCount()
				&& getObject(nextIndex) != null && getObject(nextIndex).step().customStep()) {
			return Boolean.TRUE;
		}
		return null;
	}
	
	private Object getName(EditStepRow stepRow) {
		if(stepRow.getTitleEl() != null) {
			return stepRow.getTitleEl();
		}
		if(StringHelper.containsNonWhitespace(stepRow.getCustomName())) {
			return stepRow.getCustomName();
		}
		if(StringHelper.containsNonWhitespace(stepRow.getCustomNameDe())) {
			return stepRow.getCustomNameDe();
		}
		return stepRow.getName();
	}
	
	public int indexOf(Tab tab) {
		List<EditStepRow> rows = getObjects();
		int numOfRows = rows.size();
		for(int i=0; i<numOfRows; i++) {
			EditStepRow row = rows.get(i);
			if(row.step() == tab) {
				return i;
			}
		}
		return -1;
	}
	
	public enum EditStepCols implements FlexiColumnDef {

		name("table.header.step.name"),
		edit("table.header.step.edit.name"),
		status("table.header.step.status"),
		infos("table.header.step.infos"),
		enable("table.header.action"),
		delete("table.header.delete"),
		up("table.header.up"),
		down("table.header.down"),
		;
		
		private String i18nKey;
		
		private EditStepCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
