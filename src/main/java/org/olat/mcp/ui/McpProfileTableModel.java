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
package org.olat.mcp.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * The table model of the MCP profiles, sorted in memory. The number of
 * profiles of an instance is small.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpProfileTableModel extends DefaultFlexiTableDataModel<McpProfileRow>
		implements SortableFlexiTableDataModel<McpProfileRow> {

	private static final ProfileCols[] COLS = ProfileCols.values();

	private final Locale locale;

	public McpProfileTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<McpProfileRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		McpProfileRow profileRow = getObject(row);
		return getValueAt(profileRow, col);
	}

	@Override
	public Object getValueAt(McpProfileRow row, int col) {
		return switch(COLS[col]) {
			case identifier -> row.getIdentifier();
			case name -> row.getName();
			case type -> row.getTranslatedType();
			case organisations -> row.getOrganisations();
			case enabled -> Boolean.valueOf(row.isEnabled());
			case tools -> Integer.valueOf(row.getNumOfTools());
			case members -> row.getNumOfMembers();
			case rateLimit -> row.getRateLimitRequestsPerMinute();
			case manageMembers -> Boolean.valueOf(row.isUserType());
		};
	}

	public enum ProfileCols implements FlexiSortableColumnDef {
		identifier("profile.identifier", true),
		name("profile.name", true),
		type("profile.type", true),
		organisations("profile.organisations", true),
		enabled("profile.enabled", true),
		tools("profile.col.tools", true),
		members("profile.col.members", true),
		rateLimit("profile.col.ratelimit", true),
		manageMembers("profile.col.manage.members", false);

		private final String i18nKey;
		private final boolean sortable;

		private ProfileCols(String i18nKey, boolean sortable) {
			this.i18nKey = i18nKey;
			this.sortable = sortable;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return sortable;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
