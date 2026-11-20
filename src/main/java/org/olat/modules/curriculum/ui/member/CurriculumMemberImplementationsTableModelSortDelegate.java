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
package org.olat.modules.curriculum.ui.member;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.ui.member.CurriculumMemberImplementationsTableModel.ImplementationCols;

/**
 * 
 * Initial date: 20 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumMemberImplementationsTableModelSortDelegate extends SortableFlexiTableModelDelegate<CurriculumMemberImplementationRow> {

	private static final ImplementationCols[] COLS = ImplementationCols.values();
	
	public CurriculumMemberImplementationsTableModelSortDelegate(SortKey orderBy, CurriculumMemberImplementationsTableModel tableModel,
			Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<CurriculumMemberImplementationRow> rows) {
		int columnIndex = getColumnIndex();

		switch(COLS[columnIndex]) {
			case relevance: Collections.sort(rows, new RelevanceComparator()); break;
			case curriculum: Collections.sort(rows, new CurriculumComparator()); break;
			default: super.sort(rows); break;
		}
	}

	private class CurriculumComparator implements Comparator<CurriculumMemberImplementationRow> {
		@Override
		public int compare(CurriculumMemberImplementationRow o1, CurriculumMemberImplementationRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			int c = compareString(o1.getCurriculum().getDisplayName(), o2.getCurriculum().getDisplayName());
			if(c == 0) {
				c = compareString(o1.getCurriculumElement().getDisplayName(), o2.getCurriculumElement().getDisplayName());
			}
			return c;
		}
	}
	
	private class RelevanceComparator implements Comparator<CurriculumMemberImplementationRow> {
		@Override
		public int compare(CurriculumMemberImplementationRow o1, CurriculumMemberImplementationRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			int c = - compareDateAndTimestamps(o1.getBeginDate(), o2.getBeginDate());
			if(c == 0) {
				c = - compareDateAndTimestamps(o1.getEndDate(), o2.getEndDate());
			}
			if(c == 0) {
				int s1 = status(o1.getCurriculumElement().getElementStatus());
				int s2 = status(o2.getCurriculumElement().getElementStatus());
				c = Integer.compare(s1, s2);
			}
			return c;
		}
		
		private int status(CurriculumElementStatus status) {
			if(status != null) {
				return switch(status) {
					case preparation -> 3;
					case provisional -> 6;
					case confirmed, active -> 10;
					case finished -> 1;
					default -> -1;
				};
			}
			return -1;
		}
	}
}
