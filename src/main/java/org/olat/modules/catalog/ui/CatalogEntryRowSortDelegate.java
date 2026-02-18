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
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;


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
			case lifecycleSoftkey: Collections.sort(rows, new LifecycleComparator()); break;
			case lifecycleLabel: Collections.sort(rows, (r1, r2) -> compareString(r1.getLifecycleLabel(), r2.getLifecycleLabel(), false)); break;
			case lifecycleStart: Collections.sort(rows, new DateNullAlwaysLastComparator(CatalogEntryRow::getLifecycleStart)); break;
			case lifecycleEnd: Collections.sort(rows, new DateNullAlwaysLastComparator(CatalogEntryRow::getLifecycleEnd)); break;
			default: super.sort(rows);
		}
	}
	
	private class LifecycleComparator implements Comparator<CatalogEntryRow> {
		
		@Override
		public int compare(CatalogEntryRow o1, CatalogEntryRow o2) {
			// Nulls after string values
			int c = -compareNullObjects(o1.getLifecycleSoftKey(), o2.getLifecycleSoftKey());
			
			// Rows with life cycle by date
			if (o1.getLifecycleSoftKey() != null && o2.getLifecycleSoftKey() != null) {
				if (c == 0) {
					c = compareDateAndTimestamps(o1.getLifecycleStart(), o2.getLifecycleStart(), false);
				}
				
				if (c == 0) {
					c = compareDateAndTimestamps(o1.getLifecycleEnd(), o2.getLifecycleEnd(), false);
				}
			}
			
			if (c == 0) {
				c = compareString(o1.getTitle(), o2.getTitle());
			}
			
			return c;
		}
	}
	
	private class PriorityComparator implements Comparator<CatalogEntryRow> {
		
		@Override
		public int compare(CatalogEntryRow o1, CatalogEntryRow o2) {
			// Higher priority is before lower priority
			int c = -o1.getSortPriority().compareTo(o2.getSortPriority());
			
			if (c == 0) {
				c = compareDateAndTimestamps(o1.getLifecycleStart(), o2.getLifecycleStart(), false);
			}
			
			if (c == 0) {
				c = compareDateAndTimestamps(o1.getLifecycleEnd(), o2.getLifecycleEnd(), false);
			}
			
			if (c == 0) {
				c = compareString(o1.getTitle(), o2.getTitle());
			}
			
			return c;
		}
		
	}

}
