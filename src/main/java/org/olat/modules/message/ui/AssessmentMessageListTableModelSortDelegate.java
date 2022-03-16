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
package org.olat.modules.message.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.message.ui.AssessmentMessageListDataModel.MessagesCols;

/**
 * 
 * Initial date: 16 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentMessageListTableModelSortDelegate extends SortableFlexiTableModelDelegate<AssessmentMessageRow> {

	private static final MessagesCols[] COLS = MessagesCols.values();
	
	public AssessmentMessageListTableModelSortDelegate(SortKey orderBy, AssessmentMessageListDataModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<AssessmentMessageRow> rows) {
		int columnIndex = getColumnIndex();
		switch(COLS[columnIndex]) {
			case read: Collections.sort(rows, new NumReadComparator()); break;
			default: super.sort(rows); break;
		}
	}
	
	private int compareMessages(AssessmentMessageRow o1, AssessmentMessageRow o2) {
		String c1 = o1.getContent();
		String c2 = o2.getContent();
		
		int c = 0;
		if(c1 == null || c2 == null) {
			c = compareNullObjects(o1, o2);
		} else {
			c = getCollator().compare(c1, c2);
		}
		
		if(c == 0) {
			c = o1.getKey().compareTo(o2.getKey());
		}
		return c;
	}
	
	private class NumReadComparator implements Comparator<AssessmentMessageRow> {
		@Override
		public int compare(AssessmentMessageRow o1, AssessmentMessageRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			int c = Long.compare(o1.getNumOfRead(), o2.getNumOfRead());
			if(c == 0) {
				c = compareMessages(o1, o2);
			}
			return c;
		}
	}
}
