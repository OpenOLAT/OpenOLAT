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
package org.olat.modules.curriculum.ui.wizard;

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableFooterModel;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.ui.member.EditMemberCurriculumElementRow;

/**
 * 
 * Initial date: 12 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditMembershipsTableModel extends DefaultFlexiTreeTableDataModel<EditMemberCurriculumElementRow>
implements FlexiTableFooterModel {

	private static final MembershipsCols[] COLS = MembershipsCols.values();
	private static final CurriculumRoles[] ROLES = CurriculumRoles.values();
	
	public EditMembershipsTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		//
	}
	
	@Override
	public boolean hasChildren(int row) {
		return false;
	}
	
	@Override
	public boolean hasOpenCloseAll() {
		return false;
	}
	
	public boolean isParentOf(EditMemberCurriculumElementRow parentRow, EditMemberCurriculumElementRow node) {
		for(EditMemberCurriculumElementRow parent=node.getParent(); parent != null; parent=parent.getParent()) {
			if(parent.getKey().equals(parentRow.getKey())) {
				return true;
			}
		}
		return false;
	}
	
	public int getIndexOf(EditMemberCurriculumElementRow row) {
		int numOfObjects = this.getRowCount();
		for(int i=0; i<numOfObjects; i++) {
			EditMemberCurriculumElementRow object = this.getObject(i);
			if(object.getKey().equals(row.getKey())) {
				return i;
			}
		}
		return -1;
	}
	
	public EditMemberCurriculumElementRow getObject(CurriculumElement curriculumElement) {
		List<EditMemberCurriculumElementRow> rows = getObjects();
		for(EditMemberCurriculumElementRow row:rows) {
			if(curriculumElement.getKey().equals(row.getKey())) {
				return row;
			}
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		EditMemberCurriculumElementRow detailsRow = getObject(row);
		if(col >= 0 && col < COLS.length) {
			CurriculumElement element = detailsRow.getCurriculumElement();
			return switch(COLS[col]) {
				case key -> element.getKey();
				case displayName -> element.getDisplayName();
				case externalRef -> element.getIdentifier();
				case externalId -> element.getExternalId();
				default -> "ERROR";
			};
		}
		
		int roleCol = col - EditMembershipsController.ROLES_OFFSET;
		if(roleCol >= 0 && roleCol < ROLES.length) {
			CurriculumRoles role = ROLES[roleCol];
			return detailsRow.getButton(role);
		}
		return "ERROR";
	}

	@Override
	public String getFooterHeader() {
		return "";
	}

	@Override
	public Object getFooterValueAt(int col) {
		int roleCol = col - EditMembershipsController.ROLES_OFFSET;
		if(roleCol >= 0 && roleCol < ROLES.length) {
			CurriculumRoles role = ROLES[roleCol];
			int count = countRoles(role);
			return count + "/" + getRowCount() ;	
		}
		return null;
	}
	
	private int countRoles(CurriculumRoles role) {
		int count = 0;
		for(EditMemberCurriculumElementRow detailsRow:getObjects()) {
			if(detailsRow.getModification(role) != null) {
				count++;
			}
		}
		return count;
	}

	public enum MembershipsCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		displayName("table.header.displayName"),
		externalRef("table.header.external.ref"),
		externalId("table.header.external.id");
		
		private final String i18nKey;
		
		private MembershipsCols(String i18nKey) {
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
