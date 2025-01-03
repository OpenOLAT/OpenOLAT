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
package org.olat.modules.curriculum.ui.member;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.ui.component.CurriculumRolesComparator;
import org.olat.modules.curriculum.ui.member.MemberManagementTableModel.MemberCols;
import org.olat.modules.lecture.ui.coach.AbsenceNoticesListController;

/**
 * 
 * Initial date: 17 juil. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberManagementTableSortDelegate extends SortableFlexiTableModelDelegate<MemberRow> {

	private static final MemberCols[] COLS = MemberCols.values();
	private static final CurriculumRolesComparator roleComparator = new CurriculumRolesComparator();
	
	public MemberManagementTableSortDelegate(SortKey orderBy, MemberManagementTableModel model, Locale locale) {
		super(orderBy, model, locale);
	}
	
	@Override
	protected void sort(List<MemberRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex < AbsenceNoticesListController.USER_PROPS_OFFSET) {
			switch(COLS[columnIndex]) {
				case role: Collections.sort(rows, new RoleComparator()); break;
				default: super.sort(rows); break;
			}
		} else {
			super.sort(rows);
		}
	}
	
	private class RoleComparator implements Comparator<MemberRow> {
		@Override
		public int compare(MemberRow o1, MemberRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			List<CurriculumRoles> a1 = o1.getRoles();
			List<CurriculumRoles> a2 = o2.getRoles();
			if(a1 == null || a2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			int c = 0;
			for(int i=0; i<a1.size() && i<a2.size(); i++) {
				CurriculumRoles r1 = a1.get(i);
				CurriculumRoles r2 = a2.get(i);
				
				if(!r1.equals(r2)) {
					c = roleComparator.compare(r1, r2);
					break;
				}
			}
			
			if(c == 0) {
				c = Integer.compare(a1.size(), a2.size());
			}
			return c;
		}
	}
}
