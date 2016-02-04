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
package org.olat.group.ui.main;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.ui.main.BusinessGroupListFlexiTableModel.Cols;
import org.olat.repository.RepositoryEntryShort;

/**
 * 
 * Delegate which implements the sort
 * 
 * Initial date: 10.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupFlexiTableModelSort extends SortableFlexiTableModelDelegate<BGTableItem> {
	
	public BusinessGroupFlexiTableModelSort(SortKey orderBy, SortableFlexiTableDataModel<BGTableItem> tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}

	@Override
	protected void sort(List<BGTableItem> rows) {
		int columnIndex = getColumnIndex();
		Cols column = Cols.values()[columnIndex];
		switch(column) {
			case name: Collections.sort(rows, new BusinessGroupNameComparator()); break;
			case mark: Collections.sort(rows, new MarkComparator()); break;
			case resources: Collections.sort(rows, new ResourcesComparator()); break;
			case role: Collections.sort(rows, new RoleComparator()); break;
			default: {
				super.sort(rows);
			}
		}
	}
	
	private class BusinessGroupNameComparator implements Comparator<BGTableItem> {

		@Override
		public int compare(BGTableItem t1, BGTableItem t2) {
			return compareString(t1.getBusinessGroupName(), t2.getBusinessGroupName());
		}
	}
	
	private class MarkComparator implements Comparator<BGTableItem> {
		
		@Override
		public int compare(BGTableItem t1, BGTableItem t2) {
			if(t1.isMarked()) {
				if(t2.isMarked()) {
					return compareString(t1.getBusinessGroupName(), t2.getBusinessGroupName());
				}
				return -1;
			} else if(t2.isMarked()) {
				return 1;
			}
			return compareString(t1.getBusinessGroupName(), t2.getBusinessGroupName());
		}
	}
	
	private class ResourcesComparator implements Comparator<BGTableItem> {
	
		@Override
		public int compare(BGTableItem t1, BGTableItem t2) {
			List<RepositoryEntryShort> r1 = t1.getRelations();
			List<RepositoryEntryShort> r2 = t2.getRelations();
				
			if(r1 != null && r1.size() > 0) {
				if(r2 != null && r2.size() > 0) {
					return compareTo(r1, r2);
				}
				return 1;
			} else if(r2 != null && r2.size() > 0) {
				return -1;
			}
			return compareString(t1.getBusinessGroupName(), t2.getBusinessGroupName());
		}
		
		private int compareTo(List<RepositoryEntryShort> r1, List<RepositoryEntryShort> r2) {
			int size = Math.min(r1.size(), r2.size());
			
			for(int i=0; i<size; i++) {
				String n1 = r1.get(i).getDisplayname();
				String n2 = r2.get(i).getDisplayname();
				int compare = compareString(n1, n2);
				if(compare != 0) {
					return compare;
				}
			}
			
			return (r1.size() < r2.size() ? -1 : (r1.size()==r2.size() ? 0 : 1));
		}
	}
	
	private class RoleComparator implements Comparator<BGTableItem> {
		
		private final BusinessGroupMembershipComparator MEMBERSHIP_COMPARATOR = new BusinessGroupMembershipComparator();

		@Override
		public int compare(BGTableItem t1, BGTableItem t2) {
			BusinessGroupMembership m1 = t1.getMembership();
			BusinessGroupMembership m2 = t2.getMembership();

			int compare = MEMBERSHIP_COMPARATOR.compare(m1, m2);
			if(compare == 0) {
				compare = compareString(t1.getBusinessGroupName(), t2.getBusinessGroupName());
			}
			return compare;
		}
	}
}
