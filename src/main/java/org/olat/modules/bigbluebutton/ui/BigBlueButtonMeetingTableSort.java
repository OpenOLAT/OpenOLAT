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
package org.olat.modules.bigbluebutton.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonMeetingTableModel.BMeetingsCols;

/**
 * 
 * Initial date: 9 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonMeetingTableSort extends SortableFlexiTableModelDelegate<BigBlueButtonMeetingRow> {
	
	public BigBlueButtonMeetingTableSort(SortKey orderBy, SortableFlexiTableDataModel<BigBlueButtonMeetingRow> tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<BigBlueButtonMeetingRow> rows) {
		int columnIndex = getColumnIndex();
		BMeetingsCols column = BMeetingsCols.values()[columnIndex];
		switch(column) {
			case start: Collections.sort(rows, new StartDateComparator(isAsc())); break;
			case end: Collections.sort(rows, new EndDateComparator(isAsc())); break;
			default: super.sort(rows); break;
		}
	}
	
	private class StartDateComparator implements Comparator<BigBlueButtonMeetingRow> {
		
		private final boolean ascending;
		
		public StartDateComparator(boolean ascending) {
			this.ascending = ascending;
		}
		
		@Override
		public int compare(BigBlueButtonMeetingRow m1, BigBlueButtonMeetingRow m2) {
			int c = 0;
			if(m1.getStartDate() == null && m2.getStartDate() == null) {
				c = 0;
			} else if(m1.getStartDate() == null) {
				c = ascending ? -1 : 1;
			} else if(m2.getStartDate() == null) {
				c = ascending ? 1 : -1;
			} else {
				c = m1.getStartDate().compareTo(m2.getStartDate());
			}
			
			if(c == 0) {
				c = compareString(m1.getName(), m2.getName());
			}
			return c;
		}
	}
	
	private class EndDateComparator implements Comparator<BigBlueButtonMeetingRow> {

		private final boolean ascending;
		
		public EndDateComparator(boolean ascending) {
			this.ascending = ascending;
		}
		
		@Override
		public int compare(BigBlueButtonMeetingRow m1, BigBlueButtonMeetingRow m2) {
			int c = 0;
			if(m1.getEndDate() == null && m2.getEndDate() == null) {
				c = 0;
			} else if(m1.getEndDate() == null) {
				c = ascending ? -1 : 1;
			} else if(m2.getEndDate() == null) {
				c = ascending ? 1 : -1;
			} else {
				c = m1.getEndDate().compareTo(m2.getEndDate());
			}
			
			if(c == 0) {
				c = compareString(m1.getName(), m2.getName());
			}
			return c;
		}
	}

}
