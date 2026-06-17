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
package org.olat.modules.selectus.ui.feedback.appsfeedback;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

import org.olat.modules.selectus.ui.feedback.appsfeedback.MemberFeedbacksTableModel.MemberFeedCols;

/**
 * 
 * Initial date: 4 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberFeedbacksSortTableModelDelegate extends SortableFlexiTableModelDelegate<MemberFeedbackRow> {
	
	private final Locale locale;
	
	public MemberFeedbacksSortTableModelDelegate(SortKey orderBy, MemberFeedbacksTableModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
		this.locale = locale;
	}
	
	@Override
	protected void sort(List<MemberFeedbackRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex >= 0 && columnIndex < MemberFeedCols.values().length) {
			switch(MemberFeedCols.values()[columnIndex]) {
				case positionTitle: Collections.sort(rows, new PositionTitleComparator()); break;
				case myFeedback: Collections.sort(rows, new MyFeedbackComparator()); break;
				default: super.sort(rows);
			}
		}
	}
	
	private int compareDeadline(MemberFeedbackRow o1, MemberFeedbackRow o2) {
		int c = 0;
		Date d1 = o1.getDeadline();
		Date d2 = o2.getDeadline();
		if(d1 != null) {
			if(d2 == null) {
				c = 1;
			} else {
				c = d1.compareTo(d2);
			}
		} else if(d2 != null) {
			c = -1;
		}
		return c;
	}
	
	private int compareComment(MemberFeedbackRow o1, MemberFeedbackRow o2) {
		boolean c1 = o1.hasComment();
		boolean c2 = o2.hasComment();
		
		int c = 0;
		if(c1 && !c2) {
			c = 1;
		} else if(!c1 && c2) {
			c = -1;
		}
		return c;
	}

	private class MyFeedbackComparator implements Comparator<MemberFeedbackRow> {

		@Override
		public int compare(MemberFeedbackRow o1, MemberFeedbackRow o2) {
			int c = compareComment(o1, o2);
			if(c == 0) {
				c = compareDeadline(o1, o2);
			}
			if(c == 0) {
				String t1 = o1.getPosition().getMLTitle(locale);
				String t2 = o2.getPosition().getMLTitle(locale);
				c = compareString(t1, t2);
			}
			return c;
		}
	}
	
	private class PositionTitleComparator implements Comparator<MemberFeedbackRow> {

		@Override
		public int compare(MemberFeedbackRow o1, MemberFeedbackRow o2) {
			String t1 = o1.getPosition().getMLTitle(locale);
			String t2 = o2.getPosition().getMLTitle(locale);
			
			int c = compareString(t1, t2);
			if(c == 0) {
				c = compareComment(o1, o2);
			}
			if(c == 0) {
				c = compareDeadline(o1, o2);
			}
			return c;
		}
	}
}
