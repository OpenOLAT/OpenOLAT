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
package org.olat.modules.grading.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.id.Identity;
import org.olat.modules.grading.GraderStatus;
import org.olat.modules.grading.ui.GradersListTableModel.GradersCol;
import org.olat.modules.grading.ui.component.GraderStatusCellRenderer;
import org.olat.user.AbsenceLeave;

/**
 * 
 * Initial date: 16 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradersListTableSortDelegate extends SortableFlexiTableModelDelegate<GraderRow> {

	private static final GradersCol[] COLS = GradersCol.values();
	
	public GradersListTableSortDelegate(SortKey orderBy, GradersListTableModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<GraderRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex >= 0 && columnIndex < COLS.length) {
			switch(COLS[columnIndex]) {
				case status: Collections.sort(rows, new StatusComparator()); break;
				case absence: Collections.sort(rows, new AbsenceComparator()); break;
				default: super.sort(rows); break;
			}
		} else {
			super.sort(rows);
		}
	}
	
	private int compareGraderRow(GraderRow r1, GraderRow r2) {
		Identity i1 = r1.getGrader();
		Identity i2 = r2.getGrader();
		if(i1 == null || i2 == null) {
			return compareNullObjects(i1, i2);
		}
		
		String l1 = i1.getUser().getLastName();
		String l2 = i2.getUser().getLastName();
		int c = compareString(l1, l2);
		if(c == 0) {
			c = compareLongs(i1.getKey(), i2.getKey());
		}
		
		return c;
	}
	
	private class StatusComparator implements Comparator<GraderRow> {
		
		@Override
		public int compare(GraderRow o1, GraderRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}

			GraderStatus s1 = GraderStatusCellRenderer.getFinalStatus(o1.getGraderStatus());
			GraderStatus s2 = GraderStatusCellRenderer.getFinalStatus(o2.getGraderStatus());
			if(s1 == null || s2 == null) {
				return compareNullObjects(o1, o2);
			}
			int c = Integer.compare(s1.ordinal(), s2.ordinal());
			if(c == 0) {
				c = compareGraderRow(o1, o2);
			}
			return c;
		}
	}
	
	private class AbsenceComparator implements Comparator<GraderRow> {
		
		@Override
		public int compare(GraderRow o1, GraderRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}

			List<AbsenceLeave> s1 = o1.getAbsenceLeaves();
			List<AbsenceLeave> s2 = o2.getAbsenceLeaves();
			
			int c = 0;
			if(s1 == null || s2 == null) {
				c = compareNullObjects(o1, o2);
			} else if(s1.isEmpty() || s2.isEmpty()) {
				c =  Boolean.compare(s1.isEmpty(), s2.isEmpty());
			} else {
				Date a1 = s1.get(0).getAbsentFrom();
				Date a2 = s2.get(0).getAbsentFrom();
				c = compareDateAndTimestamps(a1, a2);
			}
			
			if(c == 0) {
				c = compareGraderRow(o1, o2);
			}
			return c;
		}
	}
}
