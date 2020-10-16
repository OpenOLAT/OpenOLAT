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
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.AbsenceNoticeTarget;
import org.olat.modules.lecture.AbsenceNoticeType;
import org.olat.modules.lecture.ui.coach.AbsenceNoticesListTableModel.NoticeCols;

/**
 * 
 * Initial date: 16 oct. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbsenceNoticesListTableModelSortDelegate extends SortableFlexiTableModelDelegate<AbsenceNoticeRow> {

	private static final NoticeCols[] COLS = NoticeCols.values();
	
	public AbsenceNoticesListTableModelSortDelegate(SortKey orderBy, AbsenceNoticesListTableModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<AbsenceNoticeRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex < AbsenceNoticesListController.USER_PROPS_OFFSET) {
			switch(COLS[columnIndex]) {
				case date: Collections.sort(rows, new DateComparator()); break;
				case entry: Collections.sort(rows, new EntriesLinkComparator()); break;
				case type: Collections.sort(rows, new TypeComparator()); break;
				default: super.sort(rows); break;
			}
		} else {
			super.sort(rows);
		}
	}
	
	private int compareNotices(AbsenceNoticeRow o1, AbsenceNoticeRow o2) {
		if(o1 == null || o2 == null) {
			return compareNullObjects(o1, o2);
		}
		if(o1.getAbsenceNotice() == null || o2.getAbsenceNotice() == null) {
			return compareNullObjects(o1.getAbsenceNotice(), o2.getAbsenceNotice());
		}
		
		Identity id1 = o1.getAbsenceNotice().getIdentity();
		Identity id2 = o2.getAbsenceNotice().getIdentity();
		if(id1 == null || id2 == null) {
			return compareNullObjects(id1, id2);
		}
		
		String l1 = id1.getUser().getLastName();
		String l2 = id2.getUser().getLastName();
		
		int c = compareString(l1, l2);
		if(c == 0) {
			String f1 = id1.getUser().getLastName();
			String f2 = id2.getUser().getLastName();
			c = compareString(f1, f2);
		}
		if(c == 0) {
			c = compareLongs(id1.getKey(), id2.getKey());
		}
		if(c == 0) {
			c = compareLongs(o1.getAbsenceNotice().getKey(), o2.getAbsenceNotice().getKey());
		}
		return c;
	}
	

	private class DateComparator implements Comparator<AbsenceNoticeRow> {
		@Override
		public int compare(AbsenceNoticeRow o1, AbsenceNoticeRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			boolean block1 = isRenderLectureBlock(o1);
			boolean block2 = isRenderLectureBlock(o2);
			
			int c = 0;
			if(block1 && block2) {
				c = compareInts(o1.getLectureBlocks().size(), o2.getLectureBlocks().size());
			} else if(!block1 && block2) {
				c = -1;
			} else if(block1) {
				c = 1;
			}

			if(c == 0) {
				c = compareDateAndTimestamps(o1.getStartDate(), o2.getStartDate());
			}
			if(c == 0) {
				c = compareNotices(o1, o2);
			}
			return c;
		}
		
		private boolean isRenderLectureBlock(AbsenceNoticeRow row) {
			return row.getAbsenceNotice().getNoticeTarget() == AbsenceNoticeTarget.lectureblocks
					&& (row.getLectureBlocks() != null && !row.getLectureBlocks().isEmpty());
		}
	}
	
	private class TypeComparator implements Comparator<AbsenceNoticeRow> {
		@Override
		public int compare(AbsenceNoticeRow o1, AbsenceNoticeRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			AbsenceNotice a1 = o1.getAbsenceNotice();
			AbsenceNotice a2 = o2.getAbsenceNotice();
			
			int c = compareBooleans(a1.getAbsenceAuthorized(), a2.getAbsenceAuthorized());
			if(c == 0) {
				AbsenceNoticeType t1 = a1.getNoticeType();
				AbsenceNoticeType t2 = a2.getNoticeType();
				if(t1 == null || t2 == null) {
					c = compareNullObjects(o1, o2);
				} else {
					c = t1.compareTo(t2);
				}	
			}

			if(c == 0) {
				c = compareNotices(o1, o2);
			}
			return c;
		}
	}
	
	private class EntriesLinkComparator implements Comparator<AbsenceNoticeRow> {
		@Override
		public int compare(AbsenceNoticeRow o1, AbsenceNoticeRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			FormLink e1 = o1.getEntriesLink();
			FormLink e2 = o2.getEntriesLink();
			if(e1 == null || e2 == null) {
				return compareNullObjects(e1, e2);
			}
			
			int c = compareString(e1.getI18nKey(), e2.getI18nKey());
			if(c == 0) {
				c = compareNotices(o1, o2);
			}
			return c;
		}
	}
}
