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
package org.olat.repository.ui.list;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 13 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InPreparationSortDelegate extends SortableFlexiTableModelDelegate<InPreparationRow> {
	
	public InPreparationSortDelegate(SortKey orderBy, InPreparationDataModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<InPreparationRow> rows) {
		int columnIndex = getColumnIndex();
		switch(InPreparationDataModel.COLS[columnIndex]) {
			case lifecycleSoftkey: Collections.sort(rows, new LifecycleComparator()); break;
			case lifecycleLabel: Collections.sort(rows, (r1, r2) -> compareString(r1.getLifecycleLabel(), r2.getLifecycleLabel(), false)); break;
			case lifecycleStart: Collections.sort(rows, new DateNullAlwaysLastComparator(InPreparationRow::getLifecycleStart)); break;
			case lifecycleEnd: Collections.sort(rows, new DateNullAlwaysLastComparator(InPreparationRow::getLifecycleEnd)); break;
			default: super.sort(rows);
		}
	}
	
	private class LifecycleComparator implements Comparator<InPreparationRow> {
		
		@Override
		public int compare(InPreparationRow o1, InPreparationRow o2) {
			// Nulls after string values
			int c = -compareNullObjects(o1.getLifecycleSoftKey(), o2.getLifecycleLabel());
			
			// Rows with life cycle by date
			if (o1.getLifecycleSoftKey() != null && o2.getLifecycleLabel() != null) {
				if (c == 0) {
					c = compareDateAndTimestamps(o1.getLifecycleStart(), o2.getLifecycleStart(), false);
				}
				
				if (c == 0) {
					c = compareDateAndTimestamps(o1.getLifecycleEnd(), o2.getLifecycleEnd(), false);
				}
			}
			
			if (c == 0) {
				c = compareString(o1.getDisplayName(), o2.getDisplayName());
			}
			
			return c;
		}
	}

}
