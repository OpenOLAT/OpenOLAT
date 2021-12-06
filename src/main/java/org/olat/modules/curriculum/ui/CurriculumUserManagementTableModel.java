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
package org.olat.modules.curriculum.ui;

import java.util.Locale;

import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * 
 * Initial date: 15 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumUserManagementTableModel extends DefaultFlexiTableDataModel<CurriculumMemberRow>
implements SortableFlexiTableDataModel<CurriculumMemberRow> {
	
	private final Locale locale;
	
	public CurriculumUserManagementTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			CurriculumUserManagementTableSortDelegate sort = new CurriculumUserManagementTableSortDelegate(orderBy, this, locale);
			super.setObjects(sort.sort());
		}
	}
	
	@Override
	public boolean isSelectable(int row) {
		CurriculumMemberRow member = getObject(row);
		return member.getInheritanceMode() == GroupMembershipInheritance.root
				|| member.getInheritanceMode() == GroupMembershipInheritance.none;
	}

	@Override
	public Object getValueAt(int row, int col) {
		CurriculumMemberRow member = getObject(row);
		return getValueAt(member, col);
	}

	@Override
	public Object getValueAt(CurriculumMemberRow row, int col) {
		if(col >= 0 && col < CurriculumMemberCols.values().length) {
			switch(CurriculumMemberCols.values()[col]) {
				case role: return row.getRole();
				default : return "ERROR";
			}
		}
		
		int propPos = col - CurriculumElementUserManagementController.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}
	
	public enum CurriculumMemberCols implements FlexiSortableColumnDef {
		role("table.header.role");
		
		private final String i18nKey;
		
		private CurriculumMemberCols(String i18nKey) {
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
