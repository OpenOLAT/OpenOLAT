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
package org.olat.modules.coach.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.coach.ui.CoursesIdentityTableDataModel.Columns;
import org.olat.modules.lecture.model.LectureBlockStatistics;

/**
 * 
 * Initial date: 15 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CoursesIdentityTableSortDelegate extends SortableFlexiTableModelDelegate<CourseIdentityRow> {

	private static final Columns[] COLS = Columns.values();
	
	public CoursesIdentityTableSortDelegate(SortKey orderBy, CoursesIdentityTableDataModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
		
	@Override
	protected void sort(List<CourseIdentityRow> rows) {
		int columnIndex = getColumnIndex();
		switch(COLS[columnIndex]) {
			case lecturesProgress: Collections.sort(rows, new LecturesProgressComparator()); break;
			case completion: Collections.sort(rows, new CompletionComparator()); break;
			case numberAssessments: Collections.sort(rows, new NumberAssessmentComparator()); break;
			default: super.sort(rows); break;
		}
	}
	
	private final int defaultComparator(CourseIdentityRow o1, CourseIdentityRow o2) {
		String d1 = o1.getRepositoryEntryDisplayname();
		String d2 = o2.getRepositoryEntryDisplayname();
		
		int c = compareString(d1, d2);
		if(c == 0) {
			c = compareLongs(o1.getRepositoryEntryKey(), o2.getRepositoryEntryKey());
		}
		return c;
	}
	
	private class NumberAssessmentComparator implements Comparator<CourseIdentityRow> {
		@Override
		public int compare(CourseIdentityRow o1, CourseIdentityRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjectsLast(o1, o2);
			}
			if(o1.getNumberAssessment() == null || o2.getNumberAssessment() == null) {
				return compareNullObjectsLast(o1.getNumberAssessment(), o2.getNumberAssessment());
			}
			
			int c = Integer.compare(o1.getNumberAssessment().getGreen(), o2.getNumberAssessment().getGreen());
			if(c == 0) {
				c = defaultComparator(o1, o2);
			}
			
			return c;
		}
	}

	private class CompletionComparator implements Comparator<CourseIdentityRow> {
		@Override
		public int compare(CourseIdentityRow o1, CourseIdentityRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjectsLast(o1, o2);
			}
			
			if(o1.getAssessmentEntryCompletion() == null || o2.getAssessmentEntryCompletion() == null) {
				return compareNullObjectsLast(o1.getAssessmentEntryCompletion(), o2.getAssessmentEntryCompletion());
			}
			
			int c = compareDoubles(o1.getAssessmentEntryCompletion().doubleValue(), o2.getAssessmentEntryCompletion().doubleValue());
			if(c == 0) {
				c = defaultComparator(o1, o2);
			}
			return c;
		}
	}

	private class LecturesProgressComparator implements Comparator<CourseIdentityRow> {
		@Override
		public int compare(CourseIdentityRow o1, CourseIdentityRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjectsLast(o1, o2);
			}
			if(o1.getLectureBlockStatistics() == null || o2.getLectureBlockStatistics() == null) {
				return compareNullObjectsLast(o1.getLectureBlockStatistics(), o2.getLectureBlockStatistics());
			}
			
			LectureBlockStatistics s1 = o1.getLectureBlockStatistics();
			LectureBlockStatistics s2 = o2.getLectureBlockStatistics();
			
			int c = Long.compare(s1.getTotalAttendedLectures(), s2.getTotalAttendedLectures());
			if(c == 0) {
				c = Long.compare(s1.getTotalAuthorizedAbsentLectures(), s2.getTotalAuthorizedAbsentLectures());
			}
			
			if(c == 0) {
				c = defaultComparator(o1, o2);
			}
			return c;
		}
	}
}
