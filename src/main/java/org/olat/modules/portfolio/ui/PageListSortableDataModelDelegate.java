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
package org.olat.modules.portfolio.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.ui.PageListDataModel.PageCols;
import org.olat.modules.portfolio.ui.model.PageRow;

/**
 * 
 * Initial date: 23.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageListSortableDataModelDelegate extends SortableFlexiTableModelDelegate<PageRow> {
	
	public PageListSortableDataModelDelegate(SortKey orderBy, SortableFlexiTableDataModel<PageRow> tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}

	@Override
	protected void sort(List<PageRow> rows) {
		int columnIndex = getColumnIndex();
		PageCols column = PageCols.values()[columnIndex];
		switch(column) {
			case status: Collections.sort(rows, new StatusComparator()); break;
			default: Collections.sort(rows, new ClassicComparator());
		}
	}
	
	private class StatusComparator implements Comparator<PageRow> {
		@Override
		public int compare(PageRow t1, PageRow t2) {
			PageStatus s1 = t1.getPageStatus();
			PageStatus s2 = t2.getPageStatus();
			if(s1 == null && s2 != null) {
				return -1;
			}
			if(s1 != null && s2 == null) {
				return 1;
			}
			
			int compare = 0;
			if(s1 != null && s2 != null) {
				compare = Integer.compare(s1.ordinal(), s2.ordinal());
			}
			if(compare == 0) {
				compare = compareString(t1.getTitle(), t2.getTitle());
			}
			return compare;
		}
	}

	private class ClassicComparator implements Comparator<PageRow> {
		
		@Override
		public int compare(PageRow r1, PageRow r2) {
			Section s1 = r1.getSection();
			Section s2 = r2.getSection();
			if(s1 == null && s2 != null) {
				return -1;
			}
			if(s1 != null && s2 == null) {
				return 1;
			}

			int c = compare(s1, s2);
			if(c == 0) {
				Page p1 = r1.getPage();
				Page p2 = r2.getPage();
				if(p1 == null && p2 != null) {
					return -1;
				}
				if(p1 != null && p2 == null) {
					return 1;
				}
				c = compareDateAndTimestamps(p1.getCreationDate(), p2.getCreationDate());
			}
			return c;
		}
		
		private int compare(Section s1, Section s2) {
			Date b1 = s1.getBeginDate();
			if(b1 == null) {
				b1 = s1.getCreationDate();
			}
			Date b2 = s2.getBeginDate();
			if(b2 == null) {
				b2 = s2.getBeginDate();
			}
			
			return compareDateAndTimestamps(b1, b2);
		}
	}
	
}
