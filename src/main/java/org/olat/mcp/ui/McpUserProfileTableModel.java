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
package org.olat.mcp.ui;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.mcp.McpProfile;
import org.olat.mcp.McpProfileRef;

/**
 * The profiles of one user in the user administration in one table: the
 * matching system profiles (role profiles, read-only) and the user profiles
 * the manager may assign (with a switch for the membership). The column
 * type sorts the role profiles before the user profiles, each by name.
 *
 * Initial date: 12 Oct 2026<br>
 * @author AI, ai@frentix.com, https://www.frentix.com
 */
public class McpUserProfileTableModel extends DefaultFlexiTableDataModel<McpUserProfileTableModel.UserProfileRow>
		implements SortableFlexiTableDataModel<McpUserProfileTableModel.UserProfileRow> {

	private static final UserProfileCols[] COLS = UserProfileCols.values();

	private final Locale locale;

	public McpUserProfileTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<UserProfileRow> rows = new UserProfileSortDelegate(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		return getValueAt(getObject(row), col);
	}

	@Override
	public Object getValueAt(UserProfileRow row, int col) {
		return switch(COLS[col]) {
			case type -> row.getTypeLabel();
			case identifier -> row.getProfile().getIdentifier();
			case name -> row.getProfile().getName();
			case organisations -> row.getOrganisations();
			case enabled -> Boolean.valueOf(row.getProfile().isEnabled());
			case tools -> Integer.valueOf(row.getProfile().getTools().size());
			case member -> row.getMemberToggle();
		};
	}

	/**
	 * Sorts by the type in the order of {@link org.olat.mcp.McpProfileType}
	 * (role profiles first) and then by name.
	 */
	private static class UserProfileSortDelegate extends SortableFlexiTableModelDelegate<UserProfileRow> {

		public UserProfileSortDelegate(SortKey orderBy, McpUserProfileTableModel tableModel, Locale locale) {
			super(orderBy, tableModel, locale);
		}

		@Override
		protected void sort(List<UserProfileRow> rows) {
			if(COLS[getColumnIndex()] == UserProfileCols.type) {
				Comparator<UserProfileRow> byType = Comparator.comparing(row -> row.getProfile().getType());
				rows.sort(byType.thenComparing(row -> row.getProfile().getName(), getCollator()));
			} else {
				super.sort(rows);
			}
		}
	}

	public enum UserProfileCols implements FlexiSortableColumnDef {
		type("profile.type", true),
		name("profile.name", true),
		identifier("profile.identifier", true),
		organisations("profile.organisations", true),
		enabled("profile.enabled", true),
		tools("profile.col.tools", true),
		member("user.admin.member", false);

		private final String i18nKey;
		private final boolean sortable;

		private UserProfileCols(String i18nKey, boolean sortable) {
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

	public static class UserProfileRow implements McpProfileRef {

		private final McpProfile profile;
		private final String typeLabel;
		private final String organisations;
		private FormToggle memberToggle;

		public UserProfileRow(McpProfile profile, String typeLabel, String organisations) {
			this.profile = profile;
			this.typeLabel = typeLabel;
			this.organisations = organisations;
		}

		@Override
		public Long getKey() {
			return profile.getKey();
		}

		public McpProfile getProfile() {
			return profile;
		}

		public String getTypeLabel() {
			return typeLabel;
		}

		public String getOrganisations() {
			return organisations;
		}

		/**
		 * @return The switch of the membership, null for a role profile
		 */
		public FormToggle getMemberToggle() {
			return memberToggle;
		}

		public void setMemberToggle(FormToggle memberToggle) {
			this.memberToggle = memberToggle;
		}
	}
}
