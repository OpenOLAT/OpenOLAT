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
package org.olat.course.assessment.ui.inspection;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.course.assessment.AssessmentInspectionStatusEnum;
import org.olat.course.assessment.ui.inspection.AssessmentInspectionOverviewListModel.OverviewCols;

/**
 * 
 * Initial date: 17 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionOverviewListModelSortDelegate extends SortableFlexiTableModelDelegate<AssessmentInspectionRow> {
	
	private static final OverviewCols[] COLS = OverviewCols.values();
	
	public AssessmentInspectionOverviewListModelSortDelegate(SortKey orderBy, AssessmentInspectionOverviewListModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<AssessmentInspectionRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex >= 0 && columnIndex < COLS.length) {
			switch(COLS[columnIndex]) {
				case courseNode: Collections.sort(rows, new CourseNodeComparator()); break;
				case inspectionPeriod: Collections.sort(rows, new PeriodComparator()); break;
				case inspectionStatus: Collections.sort(rows, new InspectionStatusComparator()); break;
				default: super.sort(rows); break;
			}
		} else {
			super.sort(rows);
		}
	}
	
	private class InspectionStatusComparator implements Comparator<AssessmentInspectionRow> {
		@Override
		public int compare(AssessmentInspectionRow o1, AssessmentInspectionRow o2) {
			AssessmentInspectionStatusEnum t1 = o1.getInspectionStatus();
			AssessmentInspectionStatusEnum t2 = o2.getInspectionStatus();
			
			int c = 0;
			if(t1 == null || t2 == null) {
				c = compareNullObjects(t1, t2);
			} else {
				c = t1.compareTo(t2);
			}
			return c;
		}
	}
	
	private class PeriodComparator implements Comparator<AssessmentInspectionRow> {
		@Override
		public int compare(AssessmentInspectionRow o1, AssessmentInspectionRow o2) {
			Date f1 = o1.getFromDate();
			Date f2 = o2.getFromDate();
			int c = compareDateAndTimestamps(f1, f2);
			if(c == 0) {
				Date t1 = o1.getToDate();
				Date t2 = o2.getToDate();
				c = compareDateAndTimestamps(t1, t2);
			}
			return c;
		}
	}

	private class CourseNodeComparator implements Comparator<AssessmentInspectionRow> {
		@Override
		public int compare(AssessmentInspectionRow o1, AssessmentInspectionRow o2) {
			String t1 = o1.getCourseNodeTitle();
			String t2 = o2.getCourseNodeTitle();
			return compareString(t1, t2);
		}
	}

}
