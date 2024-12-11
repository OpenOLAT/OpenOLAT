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

import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 11 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class RemoveMembershipsTableModel extends DefaultFlexiTableDataModel<RemoveMembershipRow>
implements SortableFlexiTableDataModel<RemoveMembershipRow> {
	
	private static final RemoveMembershipCols[] COLS = RemoveMembershipCols.values();
	
	private final Locale locale;
	
	public RemoveMembershipsTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			SortableFlexiTableModelDelegate<RemoveMembershipRow> sort = new SortableFlexiTableModelDelegate<>(orderBy, this, locale);
			super.setObjects(sort.sort());
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		RemoveMembershipRow removeRow = getObject(row);
		return getValueAt(removeRow, col);
	}

	@Override
	public Object getValueAt(RemoveMembershipRow row, int col) {
		if(col >= 0 && col < COLS.length) {
			return switch(COLS[col]) {
				case modifications -> getModificationStatus(row);
				case removed -> getNumOfRemoval(row);
				default -> "ERROR";
			};	
		}

		int propPos = col - AbstractMembersController.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}
	
	private ModificationStatus getModificationStatus(RemoveMembershipRow row) {
		if(!row.getMemberships().isEmpty()) {
			return ModificationStatus.REMOVE;
		}
		if(!row.getReservations().isEmpty()) {
			return ModificationStatus.MODIFICATION;
		}
		return ModificationStatus.NONE;
	}
	
	private String getNumOfRemoval(RemoveMembershipRow row) {
		int totalRemoved = row.getReservations().size() + row.getMemberships().size();
		return totalRemoved + "/" + totalRemoved;
	}
	
	public enum RemoveMembershipCols implements FlexiSortableColumnDef {
		modifications("table.header.modification"),
		removed("table.header.removed");
		
		private final String i18nKey;
		
		private RemoveMembershipCols(String i18nKey) {
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
