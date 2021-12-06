/** * <a href="http://www.openolat.org">
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
package org.olat.group.ui.main;

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * Initial date: Dec 4, 2020<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class EditMemberShipReviewTableModel extends DefaultFlexiTreeTableDataModel<EditMembershipReviewTableRow>{

	public EditMemberShipReviewTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		EditMembershipReviewTableRow reviewTableRow = getObject(row);
		int rowMode = reviewTableRow.getRowMode();
		
		switch(EditMemberShipReviewCols.values()[col]) {
			case name:
				return reviewTableRow.getNameOrIdentifier();
			case participant:
				if (rowMode == 0) {
					return reviewTableRow.getParticipantPermissionState();
				} else if (rowMode == 1) {
					return String.valueOf(reviewTableRow.getTotalAddedParticipant()) + "," + String.valueOf(reviewTableRow.getTotalRemovedParticipant()); 
				}
			case coach:
				if (rowMode == 0) {
					return reviewTableRow.getTutorPermissionState();
				} else if (rowMode == 1) {
					return String.valueOf(reviewTableRow.getTotalAddedTutor()) + "," + String.valueOf(reviewTableRow.getTotalRemovedTutor()); 
				}
			case owner: 
				if (rowMode == 0) {
					return reviewTableRow.getOwnerPermissionState();
				} else if (rowMode == 1) {
					return String.valueOf(reviewTableRow.getTotalAddedOwner()) + "," + String.valueOf(reviewTableRow.getTotalRemovedOwner()); 
				}
			case waitingList:
				if (reviewTableRow.isWaitingListEnabled()) {
					if (rowMode == 0) {
						return reviewTableRow.getWaitingListPermissionState();
					} else if (rowMode == 1) {
						return String.valueOf(reviewTableRow.getTotalAddedWaitingList()) + "," + String.valueOf(reviewTableRow.getTotalRemovedWaitingList()); 
					}
				} else {
					return null;
				}
			default: return "ERROR";
		}
	}
	
	@Override
	public boolean hasChildren(int row) {
		return getObject(row).hasChildren();
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		
	}
	
	public enum EditMemberShipReviewCols implements FlexiSortableColumnDef {
		name("table.header.empty"),
		participant("role.repo.participant"),
		coach("role.repo.tutor"),
		owner("role.repo.owner"),
		waitingList("table.header.waitingList");
		

		private final String i18nHeaderKey;
		private final String iconHeader;
		
		private EditMemberShipReviewCols(String i18nHeaderKey, String iconHeader) {
			this.i18nHeaderKey = i18nHeaderKey;
			this.iconHeader = iconHeader;
		}
		private EditMemberShipReviewCols(String i18nHeaderKey) {
			this(i18nHeaderKey, null);
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
		
		@Override
		public String iconHeader() {
			return iconHeader;
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
