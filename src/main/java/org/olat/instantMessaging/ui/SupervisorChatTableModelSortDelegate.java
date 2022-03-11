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
package org.olat.instantMessaging.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.instantMessaging.model.RosterChannelInfos.RosterStatus;
import org.olat.instantMessaging.ui.SupervisorChatDataModel.SupervisedChatCols;
import org.olat.instantMessaging.ui.component.RosterEntryWithUnreadCellRenderer;

/**
 * 
 * Initial date: 10 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SupervisorChatTableModelSortDelegate extends SortableFlexiTableModelDelegate<RosterRow> {

	private static final SupervisedChatCols[] COLS = SupervisedChatCols.values();
	
	public SupervisorChatTableModelSortDelegate(SortKey orderBy, SupervisorChatDataModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<RosterRow> rows) {
		int columnIndex = getColumnIndex();
		switch(COLS[columnIndex]) {
			case participant: Collections.sort(rows, new ParticipantComparator()); break;
			case status: Collections.sort(rows, new RosterStatusComparator()); break;
			default: super.sort(rows); break;
		}
	}
	
	private int compareParticipants(RosterRow o1, RosterRow o2) {
		String n1 = RosterEntryWithUnreadCellRenderer.getName(o1.getRoster(), false);
		String n2 = RosterEntryWithUnreadCellRenderer.getName(o2.getRoster(), false);
		if(n1 == null || n2 == null) {
			return compareNullObjects(n1, n2);
		}
		return compareString(n1, n2);
	}
	
	private class ParticipantComparator implements Comparator<RosterRow> {
		@Override
		public int compare(RosterRow o1, RosterRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			return compareParticipants(o1, o2);
		}
	}
	
	private class RosterStatusComparator implements Comparator<RosterRow> {
		@Override
		public int compare(RosterRow o1, RosterRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			RosterStatus r1 = o1.getRosterStatus();
			RosterStatus r2 = o2.getRosterStatus();
		
			if(r1 == null || r2 == null) {
				return compareNullObjects(r1, r2);
			}
			int c = r1.compareTo(r2);
			if(c == 0) {
				c = compareParticipants(o1, o2);
			}
			return c;
		}
	}
}
