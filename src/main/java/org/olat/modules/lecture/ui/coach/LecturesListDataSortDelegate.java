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
import org.olat.modules.lecture.ui.coach.LecturesListDataModel.StatsCols;

/**
 * 
 * Initial date: 10 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesListDataSortDelegate extends SortableFlexiTableModelDelegate<LectureBlockIdentityStatisticsRow> {
	
	public LecturesListDataSortDelegate(SortKey orderBy, LecturesListDataModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<LectureBlockIdentityStatisticsRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex == StatsCols.unauthorizedAbsenceLectures.ordinal() || columnIndex == StatsCols.absentLectures.ordinal()) {
			Collections.sort(rows, new AbsenceComparator());
		} else {
			super.sort(rows);
		}
	}
	
	private class AbsenceComparator implements Comparator<LectureBlockIdentityStatisticsRow> {
		@Override
		public int compare(LectureBlockIdentityStatisticsRow o1, LectureBlockIdentityStatisticsRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			long block1 = o1.getStatistics().getTotalAbsentLectures();
			long block2 = o2.getStatistics().getTotalAbsentLectures();
			return Long.compare(block1, block2);
		}
	}
}
