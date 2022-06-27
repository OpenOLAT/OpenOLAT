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
package org.olat.course.disclaimer.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.course.disclaimer.ui.CourseDisclaimerConsentTableModel.ConsentCols;

/**
 * 
 * Initial date: 20 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseDisclaimerConsentTableSortableDelegate extends SortableFlexiTableModelDelegate<CourseDisclaimerConsenstPropertiesRow> {

	private static final ConsentCols[] COLS = ConsentCols.values();
	
	public CourseDisclaimerConsentTableSortableDelegate(SortKey orderBy, CourseDisclaimerConsentTableModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<CourseDisclaimerConsenstPropertiesRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex < CourseDisclaimerConsentOverviewController.USER_PROPS_OFFSET) {
			switch(COLS[columnIndex]) {
				case consent: Collections.sort(rows, new ConsentComparator()); break;
				default: super.sort(rows); break;
			}
		} else {
			super.sort(rows);
		}
	}

	private class ConsentComparator implements Comparator<CourseDisclaimerConsenstPropertiesRow> {
		@Override
		public int compare(CourseDisclaimerConsenstPropertiesRow o1, CourseDisclaimerConsenstPropertiesRow o2) {
			int c = 0;
			
			if(o1 == null || o2 == null) {
				c = compareNullObjects(o1, o2);
			} else {
				Date c1 = o1.getConsentDate();
				Date c2 = o2.getConsentDate();
				if(c1 == null || c2 == null) {
					c = compareNullObjects(c1, c2);
				} else {
					c = compareDateAndTimestamps(c1, c2);
				}
			}
			return c;
		}
	}
}
