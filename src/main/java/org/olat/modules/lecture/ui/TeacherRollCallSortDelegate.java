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
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.lecture.ui.TeacherRollCallDataModel.RollCols;

/**
 * 
 * Initial date: 13 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeacherRollCallSortDelegate extends SortableFlexiTableModelDelegate<TeacherRollCallRow> {

	public TeacherRollCallSortDelegate(SortKey orderBy, TeacherRollCallDataModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<TeacherRollCallRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex >= TeacherRollCallController.CHECKBOX_OFFSET) {
			int checkboxIndex = columnIndex - TeacherRollCallController.CHECKBOX_OFFSET;
			Collections.sort(rows, new CheckboxComparator(checkboxIndex));
		} else if(columnIndex < TeacherRollCallController.USER_PROPS_OFFSET) {
			switch(RollCols.values()[columnIndex]) {
				case authorizedAbsence: Collections.sort(rows, new AuthorizedAbsenceComparator()); break;
				case comment: Collections.sort(rows, new CommentComparator()); break;
				default: {
					super.sort(rows);
				}
			}
		} else {
			super.sort(rows);
		}
	}
	
	private class CommentComparator implements Comparator<TeacherRollCallRow> {

		@Override
		public int compare(TeacherRollCallRow s1, TeacherRollCallRow s2) {
			TextElement c1 = s1.getCommentEl();
			TextElement c2 = s2.getCommentEl();
			if(c1 == null || c2 == null) {
				return compareNullObjects(c1, c2);
			}
			String t1 = c1.getValue();
			String t2 = c2.getValue();
			return compareString(t1, t2);
		}
	}
	
	private class AuthorizedAbsenceComparator implements Comparator<TeacherRollCallRow> {

		@Override
		public int compare(TeacherRollCallRow s1, TeacherRollCallRow s2) {
			MultipleSelectionElement c1 = s1.getAuthorizedAbsence();
			MultipleSelectionElement c2 = s2.getAuthorizedAbsence();
			if(c1 == null || c2 == null) {
				return compareNullObjects(c1, c2);
			}
			boolean check1 = c1.isAtLeastSelected(1);
			boolean check2 = c2.isAtLeastSelected(1);
			return compareBooleans(check1, check2);
		}
	}
	
	private class CheckboxComparator implements Comparator<TeacherRollCallRow> {
		
		private final int checkboxIndex;
		
		public CheckboxComparator(int checkboxIndex) {
			this.checkboxIndex = checkboxIndex;
		}
		
		@Override
		public int compare(TeacherRollCallRow s1, TeacherRollCallRow s2) {
			MultipleSelectionElement c1 = s1.getCheck(checkboxIndex);
			MultipleSelectionElement c2 = s2.getCheck(checkboxIndex);
			if(c1 == null || c2 == null) {
				return compareNullObjects(c1, c2);
			}
			boolean check1 = c1.isAtLeastSelected(1);
			boolean check2 = c2.isAtLeastSelected(1);
			return compareBooleans(check1, check2);
		}
	}
}