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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.course.reminder.model.ReminderRow;

/**
 * 
 * Initial date: 02.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseReminderTableModel extends DefaultFlexiTableDataModel<ReminderRow> implements SortableFlexiTableDataModel<ReminderRow> {
	
	public CourseReminderTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public CourseReminderTableModel createCopyWithEmptyList() {
		return new CourseReminderTableModel(getTableColumnModel());
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<ReminderRow> views = new CourseReminderTableSort(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		ReminderRow reminder = getObject(row);
		return getValueAt(reminder, col);
	}
		
	@Override
	public Object getValueAt(ReminderRow reminder, int col) {	
		switch(ReminderCols.values()[col]) {
			case id: return reminder.getKey();
			case description: return reminder.getDescription();
			case creator: return reminder.getCreator();
			case creationDate: return reminder.getCreationDate();
			case lastModified: return reminder.getLastModified();
			case sendTime: return reminder.getSendTime();
			case send: return reminder.getSend();
			case tools: return reminder.getToolsLink();
			case untilDate: return reminder.getBeforeDateChooser();
			case afterDate: return reminder.getAfterDateChooser();
			case descriptionElement: return reminder.getDescriptionElement();
			default: return "ERROR";
		}
	}
	
	public enum ReminderCols implements FlexiColumnDef {
		id("table.header.id"),
		description("table.header.description"),
		descriptionElement("table.header.description"),
		afterDate("table.header.date.after"),
		untilDate("table.header.date.until"),
		creator("table.header.creator"),
		creationDate("table.header.creationDate"),
		lastModified("table.header.lastModified"),
		sendTime("table.header.sendTime"),
		send("table.header.send"),
		tools("table.header.actions");
		
		private final String i18nKey;
		private final String icon;
		
		private ReminderCols(String i18nKey, String icon) {
			this.i18nKey = i18nKey;
			this.icon = icon;
		}

		private ReminderCols(String i18nKey) {
			this(i18nKey, null);
		}
		

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public String iconHeader() {
			return icon;
		}
		
	}
}