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
package org.olat.commons.calendar.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.commons.calendar.ui.CalendarPersonalConfigurationDataModel.ConfigCols;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 01.09.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CalendarPersonalConfigurationTableSort extends SortableFlexiTableModelDelegate<CalendarPersonalConfigurationRow> {
	
	public CalendarPersonalConfigurationTableSort(SortKey orderBy, SortableFlexiTableDataModel<CalendarPersonalConfigurationRow> tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<CalendarPersonalConfigurationRow> rows) {
		int columnIndex = getColumnIndex();
		ConfigCols column = ConfigCols.values()[columnIndex];
		switch(column) {
			case type: Collections.sort(rows, new TypeComparator()); break;
			case cssClass: Collections.sort(rows, new CssClassComparator()); break;
			case visible: Collections.sort(rows, new VisibleComparator()); break;
			case aggregated: Collections.sort(rows, new AggregatedComparator()); break;
			default: super.sort(rows);
		}
	}
	
	private class TypeComparator implements Comparator<CalendarPersonalConfigurationRow> {
		
		@Override
		public int compare(CalendarPersonalConfigurationRow r1, CalendarPersonalConfigurationRow r2) {
			String t1 = r1.getCalendarType();
			String t2 = r2.getCalendarType();
			int c = compareString(t1, t2);
			if(c == 0) {
				boolean i1 = r1.isImported();
				boolean i2 = r2.isImported();
				c = compareBooleans(i1, i2);
			}
			if(c == 0) {
				String d1 = r1.getDisplayName();
				String d2 = r2.getDisplayName();
				c = compareString(d1, d2);
			}
			return c;
		}
	}
	
	private class CssClassComparator implements Comparator<CalendarPersonalConfigurationRow> {
		
		@Override
		public int compare(CalendarPersonalConfigurationRow r1, CalendarPersonalConfigurationRow r2) {
			String c1 = r1.getCssClass();
			String c2 = r2.getCssClass();
			int c = compareString(c1, c2);
			if(c == 0) {
				String d1 = r1.getDisplayName();
				String d2 = r2.getDisplayName();
				c = compareString(d1, d2);
			}
			return c;
		}
	}
	
	private class VisibleComparator implements Comparator<CalendarPersonalConfigurationRow> {
		
		@Override
		public int compare(CalendarPersonalConfigurationRow r1, CalendarPersonalConfigurationRow r2) {
			boolean v1 = r1.isVisible();
			boolean v2 = r2.isVisible();
			int c = compareBooleans(v1, v2);
			if(c == 0) {
				String d1 = r1.getDisplayName();
				String d2 = r2.getDisplayName();
				c = compareString(d1, d2);
			}
			return c;
		}
	}
	
	private class AggregatedComparator implements Comparator<CalendarPersonalConfigurationRow> {
		
		@Override
		public int compare(CalendarPersonalConfigurationRow r1, CalendarPersonalConfigurationRow r2) {
			boolean a1 = r1.isAggregated();
			boolean a2 = r2.isAggregated();
			int c = compareBooleans(a1, a2);
			if(c == 0) {
				String d1 = r1.getDisplayName();
				String d2 = r2.getDisplayName();
				c = compareString(d1, d2);
			}
			return c;
		}
	}
}
