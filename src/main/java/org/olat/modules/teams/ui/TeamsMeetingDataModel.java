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
package org.olat.modules.teams.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataSourceModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.group.BusinessGroup;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 2 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsMeetingDataModel extends DefaultFlexiTableDataSourceModel<TeamsMeeting> {

	private static final SoMeetingsCols[] COLS = SoMeetingsCols.values();
	
	public TeamsMeetingDataModel(TeamsMeetingDataSource source, FlexiTableColumnModel columnModel) {
		super(source, columnModel);
	}
	
	@Override
	public void clear() {
		super.clear();
		getSourceDelegate().resetCount();
	}

	@Override
	public Object getValueAt(int row, int col) {
		TeamsMeeting meeting = getObject(row);
		switch(COLS[col]) {
			case subject: return meeting.getSubject();
			case startDate: return meeting.getStartDate();
			case endDate: return meeting.getEndDate();
			case context: return getContext(meeting);
			default: return "ERROR";
		}
	}
	
	private String getContext(TeamsMeeting meeting) {
		RepositoryEntry entry = meeting.getEntry();
		if(entry != null) {
			return entry.getDisplayname();
		}
		BusinessGroup group = meeting.getBusinessGroup();
		return group == null ? null : group.getName();
	}

	@Override
	public TeamsMeetingDataSource getSourceDelegate() {
		return (TeamsMeetingDataSource)super.getSourceDelegate();
	}

	@Override
	public TeamsMeetingDataModel createCopyWithEmptyList() {
		return new TeamsMeetingDataModel(getSourceDelegate(), getTableColumnModel());
	}
	
	public enum SoMeetingsCols implements FlexiSortableColumnDef {
		
		subject("meeting.subject"),
		startDate("meeting.start"),
		endDate("meeting.end"),
		context("meeting.context");
		
		private final String i18nHeaderKey;
		
		private SoMeetingsCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return this != context;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}
}
