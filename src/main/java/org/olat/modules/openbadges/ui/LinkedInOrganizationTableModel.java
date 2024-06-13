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
package org.olat.modules.openbadges.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * Initial date: 2024-06-13<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class LinkedInOrganizationTableModel extends DefaultFlexiTableDataModel<LinkedInOrganizationRow>
		implements SortableFlexiTableDataModel<LinkedInOrganizationRow> {

	private static final Columns[] COLS = Columns.values();
	private final Locale locale;

	public LinkedInOrganizationTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public Object getValueAt(int row, int col) {
		LinkedInOrganizationRow rowObject = getObject(row);
		return getValueAt(rowObject, col);
	}

	@Override
	public Object getValueAt(LinkedInOrganizationRow row, int col) {
		switch(COLS[col]) {
			case organizationId -> { return row.getOrganizationId(); }
			case organizationName -> { return row.getOrganizationName(); }
			case tools -> { return row.getToolLink(); }
			default -> { return "ERROR"; }
		}
	}

	@Override
	public void sort(SortKey sortKey) {
		if (sortKey != null) {
			List<LinkedInOrganizationRow> rows = new SortableFlexiTableModelDelegate<>(sortKey, this, locale).sort();
			super.setObjects(rows);
		}
	}

	public enum Columns implements FlexiSortableColumnDef {
		organizationId("organization.id"),
		organizationName("organization.name"),
		tools("table.header.actions");

		private final String i18nKey;

		Columns(String i18nKey) {
			this.i18nKey = i18nKey;
		}


		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
