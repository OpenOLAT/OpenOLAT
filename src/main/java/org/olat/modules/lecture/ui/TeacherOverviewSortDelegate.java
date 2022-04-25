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
package org.olat.modules.lecture.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.model.LectureBlockRow;
import org.olat.modules.lecture.ui.TeacherOverviewDataModel.TeachCols;

/**
 * 
 * Initial date: 13 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeacherOverviewSortDelegate extends SortableFlexiTableModelDelegate<LectureBlockRow> {

	public TeacherOverviewSortDelegate(SortKey orderBy, TeacherOverviewDataModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<LectureBlockRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex == TeachCols.status.ordinal()) {
			Collections.sort(rows, new StatusComparator());
		} else {
			super.sort(rows);
		}
	}
	
	private int compareLectureBlock(LectureBlock l1, LectureBlock l2) {
		int c = compareString(l1.getTitle(), l2.getTitle());
		if(c == 0) {
			c = compareLongs(l1.getKey(), l2.getKey());
		}
		return c;
	}

	private class StatusComparator implements Comparator<LectureBlockRow> {

		@Override
		public int compare(LectureBlockRow o1, LectureBlockRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			LectureBlock l1 = o1.getLectureBlock();
			LectureBlock l2 = o2.getLectureBlock();
			if(l1 == null || l2 == null) {
				return compareNullObjects(l1, l2);
			}

			int c = 0;
			LectureRollCallStatus s1 = l1.getRollCallStatus();
			LectureRollCallStatus s2 = l2.getRollCallStatus();
			if(s1 == null || s2 == null) {
				c = compareNullObjects(s1, s2);
			} else {
				c = s1.compareTo(s2);
			}
			
			if(c == 0) {
				c = compareLectureBlock(l1, l2);
			}
			return c;
		}
	}
}