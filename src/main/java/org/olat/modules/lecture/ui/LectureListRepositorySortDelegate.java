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
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.lecture.model.LectureBlockRow;
import org.olat.modules.lecture.ui.LectureListRepositoryDataModel.BlockCols;

/**
 * 
 * Initial date: 29 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureListRepositorySortDelegate extends SortableFlexiTableModelDelegate<LectureBlockRow> {

	private static final BlockCols[] COLS = BlockCols.values();
	
	public LectureListRepositorySortDelegate(SortKey orderBy, LectureListRepositoryDataModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<LectureBlockRow> rows) {
		int columnIndex = getColumnIndex();
		switch(COLS[columnIndex]) {
			case location: Collections.sort(rows, new LocationComparator()); break;
			default: super.sort(rows); break;
		}
	}
	
	private class LocationComparator implements Comparator<LectureBlockRow> {
		
		@Override
		public int compare(LectureBlockRow o1, LectureBlockRow o2) {
			int c = 0;
			if(o1 == null || o2 == null) {
				c = compareNullObjects(o1, o2);
			} else {
				String l1 = o1.getLocation();
				String l2 = o2.getLocation();
				if(l1 == null || l2 == null) {
					c = compareNullObjects(l1, l2);
				} else {
					c = compareString(l1, l2);	
				}
				
				if(c == 0) {
					c = compareBooleans(o2.hasOnlineMeeting(), o1.hasOnlineMeeting());
				}
				
				if(c == 0) {
					String t1 = o1.getTitle();
					String t2 = o2.getTitle();
					if(t1 == null || t2 == null) {
						c = compareNullObjects(t1, t2);
					} else {
						c = compareString(t1, t2);	
					}
				}
			}
			return c;
		}
	}
}