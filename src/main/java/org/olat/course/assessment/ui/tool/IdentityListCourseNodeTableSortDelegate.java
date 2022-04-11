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
package org.olat.course.assessment.ui.tool;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.date.TimeElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeTableModel.IdentityCourseElementCols;
import org.olat.modules.assessment.ui.AssessedIdentityElementRow;
import org.olat.modules.assessment.ui.component.CompletionItem;

/**
 * 
 * Initial date: 7 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityListCourseNodeTableSortDelegate extends SortableFlexiTableModelDelegate<AssessedIdentityElementRow> {
	
	public IdentityListCourseNodeTableSortDelegate(SortKey orderBy, IdentityListCourseNodeTableModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<AssessedIdentityElementRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex == IdentityCourseElementCols.score.ordinal()) {
			Collections.sort(rows, new ScoreComparator());
		} else if(columnIndex == IdentityCourseElementCols.currentCompletion.ordinal()) {
			Collections.sort(rows, new CurrentCompletionComparator());
		}  else if(columnIndex == IdentityCourseElementCols.currentRunStart.ordinal()) {
			Collections.sort(rows, new CurrentRunStartComparator());
		} else {
			super.sort(rows);
		}
	}
	
	private class ScoreComparator implements Comparator<AssessedIdentityElementRow> {
		
		@Override
		public int compare(AssessedIdentityElementRow r1, AssessedIdentityElementRow r2) {
			if(r1 == null || r2 == null) {
				return compareNullObjects(r1, r2);
			}
			
			BigDecimal s1 = r1.getScore();
			BigDecimal s2 = r2.getScore();
			if(s1 == null || s2 == null) {
				return compareNullObjects(s1, s2);
			}
			return s1.compareTo(s2);
		}
		
	}
	
	private class CurrentRunStartComparator implements Comparator<AssessedIdentityElementRow> {
		
		private final Calendar cal = Calendar.getInstance();

		@Override
		public int compare(AssessedIdentityElementRow r1, AssessedIdentityElementRow r2) {
			if(r1 == null || r2 == null) {
				return compareNullObjects(r1, r2);
			}
			
			TimeElement i1 = r1.getCurrentRunStart();
			TimeElement i2 = r2.getCurrentRunStart();
			if(i1 == null || i2 == null) {
				return compareNullObjects(i1, i2);
			}
			
			Date d1= i1.getDate();
			Date d2 = i2.getDate();
			if(d1 == null || d2 == null) {
				return compareNullObjects(i1, i2);
			}
			
			int c = compareTime(d1, d2);
			if(c == 0) {
				Long k1 = r1.getIdentityKey();
				Long k2 = r2.getIdentityKey();
				c = compareLongs(k1, k2);
			}
			return c;
		}
		
		private int compareTime(Date i1, Date i2) {
			Date d1 = normalize(i1);
			Date d2 = normalize(i2);
			return compareDateAndTimestamps(d1, d2);
		}
		
		private Date normalize(Date d) {
			cal.setTime(d);
			cal.set(Calendar.YEAR, 2022);
			cal.set(Calendar.MONTH, 4);
			cal.set(Calendar.DATE, 5);
			return cal.getTime();
		}
	}

	private class CurrentCompletionComparator implements Comparator<AssessedIdentityElementRow> {

		@Override
		public int compare(AssessedIdentityElementRow r1, AssessedIdentityElementRow r2) {
			if(r1 == null || r2 == null) {
				return compareNullObjects(r1, r2);
			}
			
			CompletionItem i1 = r1.getCurrentCompletion();
			CompletionItem i2 = r2.getCurrentCompletion();
			if(i1 == null || i2 == null) {
				return compareNullObjects(i1, i2);
			}
			
			int c = compareCompletion(i1, i2);
			if(c == 0) {
				Long k1 = r1.getIdentityKey();
				Long k2 = r2.getIdentityKey();
				c = compareLongs(k1, k2);
			}
			return c;
		}
		
		private int compareCompletion(CompletionItem i1, CompletionItem i2) {
			double d1 = completion(i1);
			double d2 = completion(i2);
			return Double.compare(d1, d2);
		}
		
		private double completion(CompletionItem item) {
			double completion;
			if(item.isEnded()) {
				completion = 1.0d;
			} else if(item.getCompletion() != null) {
				completion = Math.min(item.getCompletion().doubleValue(), 1.0d);
			} else {
				completion = 0.0d;
			}
			return completion;
		}
	}
}
