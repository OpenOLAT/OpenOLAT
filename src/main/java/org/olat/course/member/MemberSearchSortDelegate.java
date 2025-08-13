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
package org.olat.course.member;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.course.member.MemberSearchTableModel.MembersCols;
import org.olat.group.ui.main.CourseMembership;

/**
 * 
 * Initial date: 11 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class MemberSearchSortDelegate extends SortableFlexiTableModelDelegate<MemberRow> {

	private static final MembersCols[] COLS = MembersCols.values();

	public MemberSearchSortDelegate(SortKey orderBy, MemberSearchTableModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}

	@Override
	protected void sort(List<MemberRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex >= 0 && columnIndex < COLS.length) {
			switch(COLS[columnIndex]) {
				case role: Collections.sort(rows, new MembershipComparator()); break;
				default: super.sort(rows); break;
			}
		} else {
			super.sort(rows);
		}
	}
	
	private class MembershipComparator implements Comparator<MemberRow> {

		@Override
		public int compare(MemberRow o1, MemberRow o2) {
			CourseMembership m1 = o1.getMembership();
			CourseMembership m2 = o2.getMembership();
			
			if(m1 == null || m2 == null) {
				return compareNullObjects(m1, m2);
			}
			
			int c = 0;
			if(m1.isOwner()) {
				if(m1.isOwner()) {
					c = 0;
				} else {
					c = 1;
				}
			} else if(m2.isOwner()) {
				c = -1;
			}
			
			if(c == 0) {
				if(m1.isCoach()) {
					if(m1.isCoach()) {
						c = 0;
					} else {
						c = 1;
					}
				} else if(m2.isCoach()) {
					c = -1;
				}
			}
			
			if(c == 0) {
				if(m1.isParticipant()) {
					if(m1.isParticipant()) {
						c = 0;
					} else {
						c = 1;
					}
				} else if(m2.isParticipant()) {
					c = -1;
				}
			}

			return c;
		}
	}
}
