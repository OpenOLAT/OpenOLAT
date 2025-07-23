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
import org.olat.modules.coach.model.ParticipantStatisticsEntry.Certificates;
import org.olat.modules.coach.model.ParticipantStatisticsEntry.SuccessStatus;
import org.olat.modules.coach.ui.CoursesTableDataModel.Columns;

/**
 * 
 * Initial date: 19 juin 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoursesTableSortDelegate extends SortableFlexiTableModelDelegate<CourseStatEntryRow> {
	
	public CoursesTableSortDelegate(SortKey orderBy, CoursesTableDataModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<CourseStatEntryRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex == Columns.certificates.ordinal()) {
			Collections.sort(rows, new CertificatesComparator());
		} else if(columnIndex == Columns.successStatus.ordinal()) {
			Collections.sort(rows, new SuccessStatusComparator());
		} else {
			super.sort(rows);
		}
	}
	

	private class SuccessStatusComparator implements Comparator<CourseStatEntryRow> {
		@Override
		public int compare(CourseStatEntryRow o1, CourseStatEntryRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			SuccessStatus s1 = o1.getSuccessStatus();
			SuccessStatus s2 = o2.getSuccessStatus();
			
			int c = 0;
			if(s1 == null || s2 == null) {
				c = compareNullObjects(s1, s1);
			} else {
				c = Long.compare(s1.numPassed(), s2.numPassed());
				
				if(c == 0) {
					c = Long.compare(s1.numFailed(), s2.numFailed());
				}
				if(c == 0) {
					c = Long.compare(s1.numUndefined(), s2.numUndefined());
				}
			}
			
			if(c == 0) {
				c = compareString(o1.getDisplayName(), o2.getDisplayName());
			}
			return c;
		}
	}

	private class CertificatesComparator implements Comparator<CourseStatEntryRow> {
		@Override
		public int compare(CourseStatEntryRow o1, CourseStatEntryRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			Certificates c1 = o1.getCertificates();
			Certificates c2 = o2.getCertificates();
			
			int c = 0;
			if(c1 == null || c2 == null) {
				c = compareNullObjects(c1, c1);
			} else {
				c = Long.compare(c1.numOfCertificates(), c2.numOfCertificates());
				
				if(c == 0) {
					c = Long.compare(c1.numOfCoursesWithCertificates(), c2.numOfCoursesWithCertificates());
				}
				if(c == 0) {
					c = Long.compare(c1.numOfCoursesWithInvalidCertificates(), c2.numOfCoursesWithInvalidCertificates());
				}
			}
			
			if(c == 0) {
				c = compareString(o1.getDisplayName(), o2.getDisplayName());
			}
			return c;
		}
	}
}
