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
package org.olat.core.commons.services.notifications.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;

/**
 * Initial date: Aug 27, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ContextualSubscriptionListDataModel extends DefaultFlexiTableDataModel<ContextualSubscriptionListRow>
		implements FlexiTableCssDelegate {

	private static final ContextualSubscriptionListCols[] COLS = ContextualSubscriptionListCols.values();

	public ContextualSubscriptionListDataModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		ContextualSubscriptionListRow subRow = getObject(row);
		return switch(COLS[col]) {
			case description -> subRow.getDescription();
			case iconCssClass -> subRow.getIconCssClass();
			case date -> subRow.getFormattedDate();
		};
	}

	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		return "";
	}

	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		return "";
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		return "";
	}

	public enum ContextualSubscriptionListCols implements FlexiSortableColumnDef {
		description("table.header.con.subs.title"),
		iconCssClass("table.header.con.subs.author"),
		date("table.header.con.subs.date");

		private final String i18nKey;

		ContextualSubscriptionListCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return "";
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
