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
package org.olat.course.reminder.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.course.reminder.model.SentReminderRow;
import org.olat.course.reminder.ui.CourseSendReminderTableModel.SendCols;

/**
 * 
 * Initial date: 10.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseSendReminderTableSort extends SortableFlexiTableModelDelegate<SentReminderRow> {
	
	public CourseSendReminderTableSort(SortKey orderBy, SortableFlexiTableDataModel<SentReminderRow> tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}

	@Override
	protected void sort(List<SentReminderRow> rows) {
		int col = getColumnIndex();
		if(col == SendCols.status.ordinal()) {
			Collections.sort(rows, new StatusComparator());
		} else {
			super.sort(rows);
		}
	}
	
	private static class StatusComparator implements Comparator<SentReminderRow> {
		@Override
		public int compare(SentReminderRow o1, SentReminderRow o2) {
			String s1 = o1.getStatus();
			String s2 = o2.getStatus();
			
			if(s1 == null) {
				if(s2 == null) return 0;
				return -1;
			}
			if(s2 == null) return 1;
			
			return s1.compareTo(s2);
		}
	}
}