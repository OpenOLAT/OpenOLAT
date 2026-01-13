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
package org.olat.modules.catalog.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.util.DateUtils;


/**
 * 
 * Initial date: 20 dez 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CatalogEntryRowSortDelegate extends SortableFlexiTableModelDelegate<CatalogEntryRow> {
	
	public CatalogEntryRowSortDelegate(SortKey orderBy, CatalogEntryDataModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<CatalogEntryRow> rows) {
		if (getOrderBy() != null && CatalogEntryDataModel.SORT_BY_PRIORITY.equals(getOrderBy().getKey())) {
			Collections.sort(rows, new PriorityComparator());
			return;
		}
		
		int columnIndex = getColumnIndex();
		switch(CatalogEntryDataModel.COLS[columnIndex]) {
			case title: Collections.sort(rows, (r1, r2) -> compareString(r1.getTitle(), r2.getTitle())); break;
			case lifecycleStart: Collections.sort(rows, new BeginDateComparator()); break;
			case lifecycleEnd: Collections.sort(rows, new EndDateComparator()); break;
			default: super.sort(rows);
		}
	}
	
	@Override
	protected void reverse(List<CatalogEntryRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex >= 0 && columnIndex < CatalogEntryDataModel.COLS.length) {
			switch(CatalogEntryDataModel.COLS[columnIndex]) {
				case lifecycleStart, lifecycleEnd: break;
				default: super.reverse(rows); break;
			}
		} else {
			super.reverse(rows);
		}
	}
	
	private int compareNullLast(Object o1, Object o2) {
		if(o1 == null && o2 != null) {
			return 1;
		}
		if(o1 != null && o2 == null) {
			return -1;
		}
		return 0;
	}
	
	private class BeginDateComparator implements Comparator<CatalogEntryRow> {
		@Override
		public int compare(CatalogEntryRow o1, CatalogEntryRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			Date d1 = DateUtils.getStartOfDay(o1.getLifecycleStart());
			Date d2 = DateUtils.getStartOfDay(o2.getLifecycleStart());
			if (d1 == null || d2 == null) {
				return compareNullLast(d1, d2);
			}
			int c = compareDateAndTimestamps(d1, d2, true);
			return isAsc() ? c : -c;
		}
	}
	
	private class EndDateComparator implements Comparator<CatalogEntryRow> {
		@Override
		public int compare(CatalogEntryRow o1, CatalogEntryRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			Date d1 = DateUtils.getStartOfDay(o1.getLifecycleEnd());
			Date d2 = DateUtils.getStartOfDay(o2.getLifecycleEnd());
			if (d1 == null || d2 == null) {
				return compareNullLast(d1, d2);
			}
			int c = compareDateAndTimestamps(d1, d2, true);
			return isAsc() ? c : -c;
		}
	}
	
	private class PriorityComparator implements Comparator<CatalogEntryRow> {

		@Override
		public int compare(CatalogEntryRow o1, CatalogEntryRow o2) {
			// Higher priority is before lower priority
			int c = -o1.getSortPriority().compareTo(o2.getSortPriority());
			
			if (c == 0) {
				c = compareDateAndTimestamps(DateUtils.getStartOfDay(o1.getLifecycleStart()), DateUtils.getStartOfDay(o2.getLifecycleStart()), false);
			}
			
			if (c == 0) {
				c = compareDateAndTimestamps(DateUtils.getStartOfDay(o1.getLifecycleEnd()), DateUtils.getStartOfDay(o2.getLifecycleEnd()), false);
			}
			
			if (c == 0) {
				c = compareString(o1.getTitle(), o2.getTitle());
			}
			
			return c;
		}
		
	}

}
