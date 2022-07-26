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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

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
			case start: Collections.sort(rows, new RowDateComparator(isAsc(), BigBlueButtonMeetingRow::getStartDate)); break;
			case end: Collections.sort(rows, new RowDateComparator(isAsc(), BigBlueButtonMeetingRow::getEndDate)); break;
			case autoDelete: Collections.sort(rows, new RowDateComparator(isAsc(), BigBlueButtonMeetingRow::getAutoDeleteDate)); break;
			default: super.sort(rows); break;
		}
	}
	
	private class RowDateComparator implements Comparator<BigBlueButtonMeetingRow> {
		
		private final boolean ascending;
		private final Function<BigBlueButtonMeetingRow, Date> dateFunction;
		
		public RowDateComparator(boolean ascending, Function<BigBlueButtonMeetingRow, Date> dateFunction) {
			this.ascending = ascending;
			this.dateFunction = dateFunction;
		}
		
		@Override
		public int compare(BigBlueButtonMeetingRow m1, BigBlueButtonMeetingRow m2) {
			int c = 0;
			Date date1 = dateFunction.apply(m1);
			Date date2 = dateFunction.apply(m2);
			if(date1 == null && date2 == null) {
				c = 0;
			} else if(date1 == null) {
				c = ascending ? -1 : 1;
			} else if(date2 == null) {
				c = ascending ? 1 : -1;
			} else {
				c = date1.compareTo(date2);
			}
			
			if(c == 0) {
				c = compareString(m1.getName(), m2.getName());
			}
			return c;
		}
	}

}
