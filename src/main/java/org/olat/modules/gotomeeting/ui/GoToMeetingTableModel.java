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
package org.olat.modules.gotomeeting.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.gotomeeting.GoToMeeting;
import org.olat.modules.gotomeeting.GoToOrganizer;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 22.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GoToMeetingTableModel extends DefaultFlexiTableDataModel<GoToMeeting> implements SortableFlexiTableDataModel<GoToMeeting> {
	
	public GoToMeetingTableModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<GoToMeeting> views = new GoToMeetingTableModelSort(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		GoToMeeting meeting = getObject(row);
		return getValueAt(meeting, col);
	}

	@Override
	public Object getValueAt(GoToMeeting meeting, int col) {
		switch(MeetingsCols.values()[col]) {
			case name: return meeting.getName();
			case start: return meeting.getStartDate();
			case end: return meeting.getEndDate();
			case organizer: {
				GoToOrganizer organizer = meeting.getOrganizer();
				String name = "";
				if(organizer != null) {
					if(StringHelper.containsNonWhitespace(organizer.getName())) {
						name = organizer.getName();
					} else if(StringHelper.containsNonWhitespace(organizer.getLastName())) {
						if(StringHelper.containsNonWhitespace(organizer.getFirstName())) {
							name += organizer.getFirstName() + " ";
						}
						name += organizer.getLastName();
					} else if(StringHelper.containsNonWhitespace(organizer.getUsername())) {
						name = organizer.getUsername();
					}
				}
				return name;
			}
			case resource: {
				RepositoryEntry entry = meeting.getEntry();
				if(entry != null) {
					return entry.getDisplayname();
				}
				BusinessGroup bGroup = meeting.getBusinessGroup();
				if(bGroup != null) {
					return bGroup.getName();
				}
				return null;
			}
			
		}
		return null;
	}
	
	public enum MeetingsCols {
		
		name("meeting.name"),
		start("meeting.start"),
		end("meeting.end"),
		organizer("meeting.organizer"),
		resource("meeting.resource");
		
		private final String i18nHeaderKey;
		
		private MeetingsCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}
	
	private static class GoToMeetingTableModelSort extends SortableFlexiTableModelDelegate<GoToMeeting> {
		
		public GoToMeetingTableModelSort(SortKey orderBy, SortableFlexiTableDataModel<GoToMeeting> tableModel, Locale locale) {
			super(orderBy, tableModel, locale);
		}

		@Override
		protected void sort(List<GoToMeeting> rows) {
			int columnIndex = getColumnIndex();
			MeetingsCols column = MeetingsCols.values()[columnIndex];
			switch(column) {
				default: {
					super.sort(rows);
				}
			}
		}
	}
}
