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
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.resource.accesscontrol.ResourceReservation;

/**
 * 
 * Initial date: 20 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CancelMembershipsTableModel extends DefaultFlexiTableDataModel<CancelMembershipRow>
implements SortableFlexiTableDataModel<CancelMembershipRow> {
	
	private static final CancelCols[] COLS = CancelCols.values();
	
	private final Locale locale;
	private List<CurriculumElement> elements;
	
	public CancelMembershipsTableModel(FlexiTableColumnModel columnModel, List<CurriculumElement> elements, Locale locale) {
		super(columnModel);
		this.locale = locale;
		this.elements = elements;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			SortableFlexiTableModelDelegate<CancelMembershipRow> sort = new SortableFlexiTableModelDelegate<>(orderBy, this, locale);
			super.setObjects(sort.sort());
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CancelMembershipRow cancelRow = getObject(row);
		return getValueAt(cancelRow, col);
	}

	@Override
	public Object getValueAt(CancelMembershipRow row, int col) {
		if(col >= 0 && col < COLS.length) {
			return switch(COLS[col]) {
				case modifications -> getModificationStatus(row);
				case cancelled -> getNumOfModificationsToDecline(row);
				case cancellationFee -> row.getCancellationFee();
				default -> "ERROR";
			};	
		}

		int propPos = col - AbstractMembersController.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}
	
	private ModificationStatus getModificationStatus(CancelMembershipRow row) {
		if(!row.getMembershipsToElements().isEmpty()) {
			return ModificationStatus.REMOVE;
		}
		if(!row.getReservations().isEmpty()) {
			return ModificationStatus.MODIFICATION;
		}
		return null;
	}
	
	private DualNumber getNumOfModificationsToDecline(CancelMembershipRow row) {
		int numOfCancellation = 0;
		final int numOfElements = elements.size();
		final List<CurriculumElement> memberOfElements = row.getMembershipsToElements();
		
		for(CurriculumElement element:elements) {
			boolean found = memberOfElements.contains(element);
			
			if(!found) {
				List<ResourceReservation> reservations = row.getReservations();
				if(reservations != null) {
					for(ResourceReservation reservation:reservations) {
						if(reservation.getResource().equals(element.getResource())) {
							found = true;
						}
					}
				}
			}
			
			if(found) {
				numOfCancellation++;
			}
		}
		return new DualNumber(numOfCancellation, numOfElements, false);
	}
	
	public enum CancelCols implements FlexiSortableColumnDef {
		modifications("table.header.modification"),
		cancelled("table.header.cancelled"),
		cancellationFee("table.header.cancellation.fee");
		
		private final String i18nKey;
		
		private CancelCols(String i18nKey) {
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
