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
package org.olat.modules.portfolio.ui.shared;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.PageUserStatus;
import org.olat.modules.portfolio.ui.shared.SharedPagesDataModel.SharePageCols;

/**
 * 
 * Initial date: 12 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharedPagesDataModelSortDelegate extends SortableFlexiTableModelDelegate<SharedPageRow> {
	
	public SharedPagesDataModelSortDelegate(SortKey orderBy, SharedPagesDataModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<SharedPageRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex >= 0 && columnIndex < SharePageCols.values().length) {
			SharePageCols column = SharePageCols.values()[columnIndex];
			switch(column) {
				case bookmark: Collections.sort(rows, new BookmarkComparator()); break;
				case pageName: Collections.sort(rows, new PageTitleComparator()); break;
				case userInfosStatus: Collections.sort(rows, new StatusComparator()); break;
				default: {
					super.sort(rows);
				}
			}
		} else {
			super.sort(rows);
		}
	}
	
	private class BookmarkComparator implements Comparator<SharedPageRow> {
		@Override
		public int compare(SharedPageRow o1, SharedPageRow o2) {
			boolean m1 = o1.isMark();
			boolean m2 = o2.isMark();
			int c = Boolean.compare(m1, m2);
			if(c == 0) {
				String s1 = o1.getPageTitle();
				String s2 = o2.getPageTitle();
				c = compareString(s1, s2);
			}
			return c;
		}
	}
	
	private class PageTitleComparator implements Comparator<SharedPageRow> {
		@Override
		public int compare(SharedPageRow o1, SharedPageRow o2) {
			String s1 = o1.getPageTitle();
			String s2 = o2.getPageTitle();
			return compareString(s1, s2);
		}
	}
	
	private class StatusComparator implements Comparator<SharedPageRow> {
		@Override
		public int compare(SharedPageRow o1, SharedPageRow o2) {
			int score1 = getScore(o1);
			int score2 = getScore(o2);
			int c = Integer.compare(score1, score2);
			if(c == 0) {
				c = compareString(o1.getPageTitle(), o2.getPageTitle());
			}
			return c;
		}
		
		private int getScore(SharedPageRow o) {
			PageStatus pageStatus = o.getStatus();
			PageUserStatus userStatus = o.getUserStatus();
			
			int score;
			if(pageStatus == null || pageStatus == PageStatus.draft) {
				score = 1;
			} else if(pageStatus == PageStatus.inRevision) {
				score = 2;
			} else if(userStatus == PageUserStatus.incoming) {
				score = 3;
			} else if(userStatus == PageUserStatus.inProcess) {
				score = 4;
			} else if(userStatus == PageUserStatus.done) {
				score = 5;
			} else if(pageStatus == PageStatus.closed) {
				score = 6;	
			} else if(pageStatus == PageStatus.deleted) {
				score = 7;
			} else {
				score = 8;
			}
			return score;
		}
	}
}