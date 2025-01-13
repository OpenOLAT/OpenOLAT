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
import java.util.Locale;

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.ui.member.MembershipModification;
import org.olat.modules.curriculum.ui.wizard.UsersOverviewTableModel.UserOverviewCols;

/**
 * 
 * Initial date: 12 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReviewEditedMembershipsTableModel extends DefaultFlexiTableDataModel<ReviewEditedMembershipsRow>
implements SortableFlexiTableDataModel<ReviewEditedMembershipsRow> {
	
	private static final UserOverviewCols[] COLS = UserOverviewCols.values();
	private static final CurriculumRoles[] ROLES = CurriculumRoles.values();
	
	private final Locale locale;
	
	public ReviewEditedMembershipsTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			SortableFlexiTableModelDelegate<ReviewEditedMembershipsRow> sort = new SortableFlexiTableModelDelegate<>(orderBy, this, locale);
			super.setObjects(sort.sort());
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		ReviewEditedMembershipsRow memberRow = getObject(row);
		return getValueAt(memberRow, col);
	}

	@Override
	public Object getValueAt(ReviewEditedMembershipsRow row, int col) {
		if(col >= 0 && col < COLS.length) {
			return switch(COLS[col]) {
				case modifications -> row.getModificationSummary();
				default -> "ERROR";
			};
		}
		
		if(col >= ReviewEditedMembershipsController.USER_PROPS_OFFSET && col < ReviewEditedMembershipsController.ROLES_OFFSET) {
			int propPos = col - ReviewEditedMembershipsController.USER_PROPS_OFFSET;
			return row.getIdentityProp(propPos);
		}
		
		if(col >= ReviewEditedMembershipsController.ROLES_OFFSET) {
			int roleCol = col - ReviewEditedMembershipsController.ROLES_OFFSET;
			if(roleCol >= 0 && roleCol < ROLES.length) {
				CurriculumRoles role = ROLES[roleCol];
				return getNumOfModifications(row, role);
			}
		}
		return "ERROR";
	}
	
	private SingleNumber getNumOfModifications(ReviewEditedMembershipsRow row, CurriculumRoles role) {
		int found = row.getNumOfModifications(role);
		int applicableModification = 0; 
		List<MembershipModification> modifications = row.getModifications();
		for(MembershipModification modification:modifications) {
			if(modification.role() == role) {
				GroupMembershipStatus currentStatus = row.getStatusBy(modification.curriculumElement().getKey(), modification.role());
				GroupMembershipStatus nextStatus = modification.nextStatus();
				if(GroupMembershipStatus.allowedAsNextStep(currentStatus, nextStatus)) {
					applicableModification++;
				}
			}
		}
		boolean warning = applicableModification != found;
		return new SingleNumber(applicableModification, true, warning);
	}

	public enum ReviewEditedMembershipsCols implements FlexiSortableColumnDef {
		modifications("table.header.activity");
		
		private final String i18nKey;
		
		private ReviewEditedMembershipsCols(String i18nKey) {
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
