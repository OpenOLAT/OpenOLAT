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

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.course.reminder.model.SentReminderRow;

/**
 * 
 * Initial date: 07.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseSendReminderTableModel extends DefaultFlexiTableDataModel<SentReminderRow> implements SortableFlexiTableDataModel<SentReminderRow> {
	
	private static final SendCols[] COLS = SendCols.values();
	
	public CourseSendReminderTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<SentReminderRow> views = new CourseSendReminderTableSort(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		SentReminderRow reminder = getObject(row);
		return getValueAt(reminder, col);
	}

	@Override
	public Object getValueAt(SentReminderRow reminder, int col) {
		if(col >= 0 && col < COLS.length) {
			switch(COLS[col]) {
				case reminder: return reminder.getReminderDescription();
				case status: return reminder.getStatus();
				case sendTime: return reminder.getSendDate();
				case courseRun: return reminder.getCourseRun();
				default: return "ERROR";
			}
		}
		if(col >= CourseSendReminderListController.USER_PROPS_OFFSET) {
			int propIndex = col - CourseSendReminderListController.USER_PROPS_OFFSET;
			return reminder.getIdentityProp(propIndex);
		} 
		return null;
	}
	
	public enum SendCols implements FlexiSortableColumnDef {
		reminder("table.header.reminder"),
		status("table.header.status"),
		sendTime("table.header.sendTime"),
		courseRun("table.header.course.run");
		
		private final String i18nKey;
		
		private SendCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}