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
package org.olat.modules.coach.ui.curriculum.course;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.coach.ui.curriculum.course.CurriculumElementWithViewsDataModel.ElementViewCols;

/**
 * 
 * Initial date: 11 juin 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementWithViewsSortDelegate extends SortableFlexiTableModelDelegate<CourseCurriculumTreeWithViewsRow> {
	
	private static final ElementViewCols[] COLS = ElementViewCols.values();
	
	public CurriculumElementWithViewsSortDelegate(SortKey orderBy, CurriculumElementWithViewsDataModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<CourseCurriculumTreeWithViewsRow> rows) {
		int columnIndex = getColumnIndex();
		switch(COLS[columnIndex]) {
			case completion: Collections.sort(rows, new CompletionComparator()); break;
			default: super.sort(rows); break;
		}
	}
	
	private class CompletionComparator implements Comparator<CourseCurriculumTreeWithViewsRow> {

		@Override
		public int compare(CourseCurriculumTreeWithViewsRow o1, CourseCurriculumTreeWithViewsRow o2) {
			Double c1 = o1.getCompletion();
			Double c2 = o2.getCompletion();
			
			int c = 0;
			if(c1 == null || c2 == null) {
				c = compareNullObjects(c1, c2);
			} else {
				c = c1.compareTo(c2);
			}
			
			if(c == 0) {
				String d1 = o1.getDisplayName();
				String d2 = o2.getDisplayName();
				if(d1 == null || d2 == null) {
					c = compareNullObjects(d1, d2);
				} else {
					c = compareString(d1, d2);
				}
			}
			return c;
		}
	}
}
