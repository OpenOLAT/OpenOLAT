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
package org.olat.modules.lecture.ui.coach;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.model.LectureReportRow;
import org.olat.modules.lecture.ui.coach.LecturesReportTableModel.ReportCols;

/**
 * 
 * Initial date: 14 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesReportTableModelSortDelegate extends SortableFlexiTableModelDelegate<LectureReportRow> {
	
	public LecturesReportTableModelSortDelegate(SortKey orderBy, LecturesReportTableModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<LectureReportRow> rows) {
		int columnIndex = getColumnIndex();
		ReportCols column = ReportCols.values()[columnIndex];
		switch(column) {
			case teachers: Collections.sort(rows, new TeachersComparator()); break;
			case owners: Collections.sort(rows, new OwnersComparator()); break;
			case status: Collections.sort(rows, new StatusComparator()); break;
			default: super.sort(rows); break;
		}
	}
	
	private class StatusComparator implements Comparator<LectureReportRow> {
		@Override
		public int compare(LectureReportRow o1, LectureReportRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			LectureRollCallStatus r1 = o1.getRollCallStatus();
			LectureRollCallStatus r2 = o2.getRollCallStatus();
			if(r1 == null || r2 == null) {
				return compareNullObjects(r1, r2);
			}
			
			int ri1 = r1.ordinal();
			int ri2 = r2.ordinal();
			return Integer.compare(ri1, ri2);
		}
	}
	
	private int compareIdentityList(List<Identity> i1, List<Identity> i2) {
		if(i1 == null || i2 == null) {
			return compareNullObjects(i1, i2);
		}
		
		int c = 0;
		if(i1.isEmpty() && i2.isEmpty()) {
			c = 0;
		} else if(!i1.isEmpty() && i2.isEmpty()) {
			c = 1;
		} else if(i1.isEmpty() && !i2.isEmpty()) {
			c = -1;
		} else {
			int max = Math.min(i1.size(), i2.size());
			for(int i=0; i<max && c == 0; i++) {
				c = compareIdentity(i1.get(i), i2.get(i));
			}
		}
		return c;
	}

	private int compareIdentity(Identity i1, Identity i2) {
		if(i1 == null || i2 == null) {
			return compareNullObjects(i1, i2);
		}

		int c = compareString(i1.getUser().getLastName(), i2.getUser().getLastName());
		if(c == 0) {
			c = compareString(i1.getUser().getFirstName(), i2.getUser().getFirstName());
		}
		if(c == 0) {
			c = this.compareLongs(i1.getKey(), i2.getKey());
		}
		return c;
	}
	
	private class TeachersComparator implements Comparator<LectureReportRow> {
		@Override
		public int compare(LectureReportRow o1, LectureReportRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			return compareIdentityList(o1.getTeachers(), o2.getTeachers());
		}
	}

	private class OwnersComparator implements Comparator<LectureReportRow> {
		@Override
		public int compare(LectureReportRow o1, LectureReportRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			return compareIdentityList(o1.getOwners(), o2.getOwners());
		}
	}
}
