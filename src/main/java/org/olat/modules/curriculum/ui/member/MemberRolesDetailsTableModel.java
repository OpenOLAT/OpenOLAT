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
package org.olat.modules.curriculum.ui.member;

import java.util.List;

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableFooterModel;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;

/**
 * 
 * Initial date: 11 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberRolesDetailsTableModel extends DefaultFlexiTreeTableDataModel<MemberRolesDetailsRow>
implements FlexiTableFooterModel {
	
	private static final MemberDetailsCols[] COLS = MemberDetailsCols.values();
	private static final CurriculumRoles[] ROLES = CurriculumRoles.values();
	
	private final String footerHeader;
	
	public MemberRolesDetailsTableModel(FlexiTableColumnModel columnModel, String footerHeader) {
		super(columnModel);
		this.footerHeader = footerHeader;
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

	@Override
	public Object getValueAt(int row, int col) {
		MemberRolesDetailsRow detailsRow = getObject(row);
		if(col >= 0 && col < COLS.length) {
			CurriculumElement element = detailsRow.getCurriculumElement();
			return switch(COLS[col]) {
				case key -> element.getKey();
				case modifications -> detailsRow.hasModifications();
				case displayName -> element.getDisplayName();
				case externalRef -> element.getIdentifier();
				case externalId -> element.getExternalId();
				default -> "ERROR";
			};
		}
		
		int roleCol = col - MemberRolesDetailsController.ROLES_OFFSET;
		if(roleCol >= 0 && roleCol < ROLES.length) {
			CurriculumRoles role = ROLES[roleCol];
			return getStatus(role, detailsRow);	
		}
		
		int byCol = col - MemberRolesDetailsController.CONFIRMATION_BY_OFFSET;
		if(byCol >= 0 && byCol < ROLES.length) {
			CurriculumRoles role = ROLES[byCol];
			return detailsRow.getConfirmationBy(role);	
		}
		
		int untilCol = col - MemberRolesDetailsController.CONFIRMATION_UNTIL_OFFSET;
		if(untilCol >= 0 && untilCol < ROLES.length) {
			CurriculumRoles role = ROLES[untilCol];
			return detailsRow.getConfirmationUntil(role);	
		}
		
		return "ERROR";
	}
	
	private GroupMembershipStatus getStatus(CurriculumRoles role, MemberRolesDetailsRow detailsRow) {
		GroupMembershipStatus status = detailsRow.getModificationStatus(role);
		if(status == null) {
			status = detailsRow.getStatus(role);
		}
		return status;
	}

	@Override
	public String getFooterHeader() {
		return footerHeader;
	}

	@Override
	public Object getFooterValueAt(int col) {
		int roleCol = col - MemberRolesDetailsController.ROLES_OFFSET;
		if(roleCol >= 0 && roleCol < ROLES.length) {
			CurriculumRoles role = ROLES[roleCol];
			int count = countRoles(role);
			return count + "/" + getRowCount() ;	
		}
		return null;
	}
	
	private int countRoles(CurriculumRoles role) {
		int count = 0;
		for(MemberRolesDetailsRow detailsRow:getObjects()) {
			GroupMembershipStatus status = detailsRow.getStatus(role);
			if(status != null
					&& status != GroupMembershipStatus.declined
					&& status != GroupMembershipStatus.removed) {
				count++;
			}
		}
		return count;
	}

	public enum MemberDetailsCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		modifications("table.header.modification"),
		displayName("table.header.displayName"),
		externalRef("table.header.external.ref"),
		externalId("table.header.external.id");
		
		private final String i18nKey;
		
		private MemberDetailsCols(String i18nKey) {
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