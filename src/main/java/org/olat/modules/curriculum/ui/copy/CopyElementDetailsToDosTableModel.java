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
package org.olat.modules.curriculum.ui.copy;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 *
 * Initial date: 12 May 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CopyElementDetailsToDosTableModel extends DefaultFlexiTableDataModel<CopyElementDetailsToDosRow> {

	private static final CopyToDoCols[] COLS = CopyToDoCols.values();

	public CopyElementDetailsToDosTableModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		CopyElementDetailsToDosRow toDoRow = getObject(row);
		return switch (COLS[col]) {
			case key -> toDoRow.getKey();
			case title -> toDoRow.getTitle();
			case activity -> toDoRow.getCopySetting();
			case priority, dueDate, due, status -> toDoRow;
			case dateKind -> toDoRow.getDateKind();
			case assigned -> toDoRow.getAssigneesPortraits();
			case delegated -> toDoRow.getDelegateesPortraits();
			case tags -> toDoRow.getFormattedTags();
			default -> "ERROR";
		};
	}

	public enum CopyToDoCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		title("table.header.title"),
		activity("table.header.activity"),
		priority("task.priority"),
		dateKind("copy.todos.date.kind"),
		dueDate("task.due.date"),
		due("task.due"),
		status("task.status"),
		assigned("task.assigned"),
		delegated("task.delegated"),
		tags("tags");

		private final String i18nKey;

		private CopyToDoCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
