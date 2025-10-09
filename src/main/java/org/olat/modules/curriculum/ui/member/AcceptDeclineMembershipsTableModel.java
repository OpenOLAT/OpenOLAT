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
import org.olat.resource.accesscontrol.ConfirmationByEnum;
import org.olat.resource.accesscontrol.ResourceReservation;

/**
 * 
 * Initial date: 10 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AcceptDeclineMembershipsTableModel extends DefaultFlexiTableDataModel<AcceptDeclineMembershipRow>
implements SortableFlexiTableDataModel<AcceptDeclineMembershipRow> {
	
	private static final AcceptDeclineCols[] COLS = AcceptDeclineCols.values();
	
	private final Locale locale;
	
	public AcceptDeclineMembershipsTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			SortableFlexiTableModelDelegate<AcceptDeclineMembershipRow> sort = new SortableFlexiTableModelDelegate<>(orderBy, this, locale);
			super.setObjects(sort.sort());
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		AcceptDeclineMembershipRow acceptRow = getObject(row);
		return getValueAt(acceptRow, col);
	}

	@Override
	public Object getValueAt(AcceptDeclineMembershipRow row, int col) {
		if(col >= 0 && col < COLS.length) {
			return switch(COLS[col]) {
				case modifications -> row.getReservations().isEmpty()
					? ModificationStatus.NONE : ModificationStatus.MODIFICATION;
				case accepted -> getNumOfModificationsToAccept(row);
				case declined -> getNumOfModificationsToDecline(row);
				default -> "ERROR";
			};	
		}

		int propPos = col - AbstractMembersController.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}
	
	private DualNumber getNumOfModificationsToAccept(AcceptDeclineMembershipRow row) {
		int countConfirmationByAdmin = 0;
		for(ResourceReservation reservation:row.getReservations()) {
			if(reservation.getConfirmableBy() == ConfirmationByEnum.ADMINISTRATIVE_ROLE) {
				countConfirmationByAdmin++;
			}
		}
		int numOfReservations = row.getReservations().size();
		boolean warning = numOfReservations > 0 && countConfirmationByAdmin != numOfReservations;
		return new DualNumber(countConfirmationByAdmin, numOfReservations, warning);
	}
	
	private DualNumber getNumOfModificationsToDecline(AcceptDeclineMembershipRow row) {
		int numOfReservations = row.getReservations().size();
		return new DualNumber(numOfReservations, numOfReservations, false);
	}
	
	public enum AcceptDeclineCols implements FlexiSortableColumnDef {
		modifications("table.header.modification"),
		accepted("table.header.accepted"),
		declined("table.header.declined");
		
		private final String i18nKey;
		
		private AcceptDeclineCols(String i18nKey) {
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
