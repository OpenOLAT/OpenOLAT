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
package org.olat.user.ui.identity;

import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.IdentityToIdentityRelationManagedFlag;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 30 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserRelationsTableModel extends DefaultFlexiTableDataModel<IdentityRelationRow>
implements SortableFlexiTableDataModel<IdentityRelationRow> {
	
	private final Locale locale;
	
	public UserRelationsTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<IdentityRelationRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		IdentityRelationRow relation = getObject(row);
		return getValueAt(relation, col);
	}

	@Override
	public Object getValueAt(IdentityRelationRow row, int col) {
		if(col >= 0 && col < RelationCols.values().length) {
			switch(RelationCols.values()[col]) {
				case key: return row.getRelationKey();
				case managed: return row.getManagedFlags().length > 0;
				case role: return row.getRelationLabel();
				case remove: return !IdentityToIdentityRelationManagedFlag
						.isManaged(row.getManagedFlags(), IdentityToIdentityRelationManagedFlag.delete);
				default: return "ERROR";
			}
		}
		
		if(col >= UserRelationsController.USER_SOURCE_PROPS_OFFSET && col < UserRelationsController.USER_TARGET_PROPS_OFFSET) {
			int pos = col - UserRelationsController.USER_SOURCE_PROPS_OFFSET;
			return row.getSourceIdentity().getIdentityProp(pos);
		}
		if(col >= UserRelationsController.USER_TARGET_PROPS_OFFSET) {
			int pos = col - UserRelationsController.USER_TARGET_PROPS_OFFSET;
			return row.getTargetIdentity().getIdentityProp(pos);
		}
		return "ERROR";
	}
	
	public enum RelationCols implements FlexiSortableColumnDef {
		
		key("table.header.key"),
		managed("table.header.managed"),
		role("table.header.role"),
		remove("remove");
		
		private final String i18nHeaderKey;
		
		private RelationCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
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
