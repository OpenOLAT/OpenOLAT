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
package org.olat.admin.user.course;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.admin.user.course.CourseOverviewMembershipDataModel.MSCols;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.group.ui.main.CourseMembership;
import org.olat.group.ui.main.CourseMembershipComparator;
import org.olat.repository.RepositoryEntryStatusEnum;

/**
 * 
 * Initial date: 16 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseOverviewMembershipSortDelegate extends SortableFlexiTableModelDelegate<CourseMemberView> {

	private final CourseMembershipComparator membershipComparator = new CourseMembershipComparator();
	
	public CourseOverviewMembershipSortDelegate(SortKey orderBy, CourseOverviewMembershipDataModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<CourseMemberView> rows) {
		int columnIndex = getColumnIndex();
		MSCols column = MSCols.values()[columnIndex];
		switch(column) {
			case role: Collections.sort(rows, new MembershipComparator()); break;
			case entry: Collections.sort(rows, new TypeComparator()); break;
			default: super.sort(rows); break;
		}
	}
	
	private class TypeComparator implements Comparator<CourseMemberView> {

		@Override
		public int compare(CourseMemberView o1, CourseMemberView o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			RepositoryEntryStatusEnum s1 = o1.getEntry().getEntryStatus();
			RepositoryEntryStatusEnum s2 = o2.getEntry().getEntryStatus();
			
			int st1 = getStatusScore(s1);
			int st2 = getStatusScore(s2);
			int c = Integer.compare(st1, st2);
			if(c == 0) {
				String r1 = o1.getEntry().getOlatResource().getResourceableTypeName();
				String r2 = o2.getEntry().getOlatResource().getResourceableTypeName();
				c = compareString(r1, r2);
			}
			if(c == 0) {
				c = compareString(o1.getDisplayName(), o2.getDisplayName());
			}
			if(c == 0) {
				c = compareLongs(o1.getRepoKey(), o2.getRepoKey());
			}
			return c;
		}
		
		private int getStatusScore(RepositoryEntryStatusEnum status) {
			switch(status) {
				case closed: return 1;
				case trash:
				case deleted: return 2;
				default: return 0;
			}
		}
	}
	
	private class MembershipComparator implements Comparator<CourseMemberView> {
		
		@Override
		public int compare(CourseMemberView o1, CourseMemberView o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			CourseMembership m1 = o1.getMembership();
			CourseMembership m2 = o2.getMembership();
			int c = membershipComparator.compare(m1, m2);
			if(c == 0) {
				c = compareString(o1.getDisplayName(), o2.getDisplayName());
			}
			if(c == 0) {
				c = compareLongs(o1.getRepoKey(), o2.getRepoKey());
			}
			return c;
		}
	}
}