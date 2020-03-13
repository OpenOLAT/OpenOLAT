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
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.lecture.model.LectureRepositoryEntryInfos;
import org.olat.modules.lecture.ui.coach.RepositoryEntriesListTableModel.LectureRepoCols;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;

/**
 * 
 * Initial date: 13 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntriesListTableModelSortDelegate extends SortableFlexiTableModelDelegate<LectureRepositoryEntryInfos> {
	
	public RepositoryEntriesListTableModelSortDelegate(SortKey orderBy, RepositoryEntriesListTableModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<LectureRepositoryEntryInfos> rows) {
		int columnIndex = getColumnIndex();
		if(LectureRepoCols.access.ordinal() == columnIndex) {
			Collections.sort(rows, new AccessComparator());
		} else {
			super.sort(rows);
		}
	}
	
	private class AccessComparator implements Comparator<LectureRepositoryEntryInfos> {
		@Override
		public int compare(LectureRepositoryEntryInfos o1, LectureRepositoryEntryInfos o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			RepositoryEntry r1 = o1.getEntry();
			RepositoryEntry r2 = o2.getEntry();
			if(r1 == null || r2 == null) {
				return compareNullObjects(r1, r2);
			}
			
			RepositoryEntryStatusEnum s1 = r1.getEntryStatus();
			RepositoryEntryStatusEnum s2 = r2.getEntryStatus();
			if(s1 == null || s2 == null) {
				return compareNullObjects(s1, s2);
			}
			
			int c = s1.compareTo(s2);
			if(c == 0) {
				c = compareString(r1.getDisplayname(), r2.getDisplayname());
			}
			if(c == 0) {
				c = compareLongs(r1.getKey(), r2.getKey());
			}
			return c;
		}
	}

}
