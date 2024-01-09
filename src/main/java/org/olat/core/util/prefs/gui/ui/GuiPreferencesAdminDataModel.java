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
package org.olat.core.util.prefs.gui.ui;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * Initial date: Dez 15, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class GuiPreferencesAdminDataModel extends DefaultFlexiTableDataModel<GuiPreferencesAdminRow>
		implements SortableFlexiTableDataModel<GuiPreferencesAdminRow> {

	public GuiPreferencesAdminDataModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		GuiPreferencesAdminRow guiPrefRow = getObject(row);
		return getValueAt(guiPrefRow, col);
	}

	@Override
	public void sort(SortKey sortKey) {
		List<GuiPreferencesAdminRow> rows = new SortableFlexiTableModelDelegate<>(sortKey, this, null).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(GuiPreferencesAdminRow row, int col) {
		return switch (GuiPrefAdminCols.values()[col]) {
			case attributedClass -> row.attributedClass();
			case count -> row.numOfEntries();
			case reset -> row.resetActionEl();
		};
	}

	public enum GuiPrefAdminCols implements FlexiSortableColumnDef {
		attributedClass("table.header.g.attr"),
		count("table.header.g.count"),
		reset("table.header.g.reset");

		private final String i18nKey;

		GuiPrefAdminCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
