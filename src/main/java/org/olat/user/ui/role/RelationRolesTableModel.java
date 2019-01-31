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
package org.olat.user.ui.role;

import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.RelationRoleManagedFlag;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 29 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RelationRolesTableModel extends DefaultFlexiTableDataModel<RelationRoleRow>
implements SortableFlexiTableDataModel<RelationRoleRow> {
	
	private final Locale locale;
	
	public RelationRolesTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<RelationRoleRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		RelationRoleRow relationRole = getObject(row);
		return getValueAt(relationRole, col);
	}

	@Override
	public Object getValueAt(RelationRoleRow row, int col) {
		switch(RelationRoleCols.values()[col]) {
			case key: return row.getKey();
			case managed: return row.getRelationRole().getManagedFlags().length > 0;
			case role: return row.getRole();
			case delete: return !RelationRoleManagedFlag.isManaged(row.getRelationRole(), RelationRoleManagedFlag.delete);
			default: return "ERROR";
		}
	}

	@Override
	public RelationRolesTableModel createCopyWithEmptyList() {
		return new RelationRolesTableModel(getTableColumnModel(), locale);
	}
	
	public enum RelationRoleCols implements FlexiSortableColumnDef {
		key("table.header.id"),
		managed("table.header.managed"),
		role("table.header.role"),
		delete("delete");
		
		private final String i18nKey;
		
		private RelationRoleCols(String i18nKey) {
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