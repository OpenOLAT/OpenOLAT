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
package org.olat.modules.curriculum.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.curriculum.ui.CurriculumComposerTableModel.ElementCols;

/**
 * 
 * Initial date: 31 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumComposerTableSortDelegate extends SortableFlexiTableModelDelegate<CurriculumElementRow> {

	private static final ElementCols[] COLS = ElementCols.values();
	
	public CurriculumComposerTableSortDelegate(SortKey orderBy, CurriculumComposerTableModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<CurriculumElementRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex >= 0 && columnIndex < COLS.length) {
			switch(COLS[columnIndex]) {
				case resources: Collections.sort(rows, new ResourcesComparator()); break;
				case beginDate: Collections.sort(rows, new BeginDateComparator()); break;
				case endDate: Collections.sort(rows, new EndDateComparator()); break;
				default: super.sort(rows); break;
			}
		} else {
			super.sort(rows);
		}
	}
	
	@Override
	protected void reverse(List<CurriculumElementRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex >= 0 && columnIndex < COLS.length) {
			switch(COLS[columnIndex]) {
				case endDate, beginDate: break;
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
	
	private class BeginDateComparator implements Comparator<CurriculumElementRow> {
		@Override
		public int compare(CurriculumElementRow o1, CurriculumElementRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			Date d1 = o1.getBeginDate();
			Date d2 = o2.getBeginDate();
			if (d1 == null || d2 == null) {
				return compareNullLast(d1, d2);
			}
			int c = compareDateAndTimestamps(d1, d2, true);
			return isAsc() ? c : -c;
		}
	}
	
	private class EndDateComparator implements Comparator<CurriculumElementRow> {
		@Override
		public int compare(CurriculumElementRow o1, CurriculumElementRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			Date d1 = o1.getBeginDate();
			Date d2 = o2.getBeginDate();
			if (d1 == null || d2 == null) {
				return compareNullLast(d1, d2);
			}
			int c = compareDateAndTimestamps(d1, d2, true);
			return isAsc() ? c : -c;
		}
	}
	
	private class ResourcesComparator implements Comparator<CurriculumElementRow> {
		@Override
		public int compare(CurriculumElementRow o1, CurriculumElementRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			long r1 = o1.getNumOfResources();
			long r2 = o2.getNumOfResources();
			
			int c = Long.compare(r1, r2);
			if(c == 0) {
				c = compareString(o1.getDisplayName(), o2.getDisplayName());
			}
			if(c == 0) {
				c = compareLongs(o1.getKey(), o2.getKey());
			}

			return c;
		}
	}
}
