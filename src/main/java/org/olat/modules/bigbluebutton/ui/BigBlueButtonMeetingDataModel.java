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
package org.olat.modules.bigbluebutton.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataSourceModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.group.BusinessGroup;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.BigBlueButtonServer;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingAdminInfos;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 3 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonMeetingDataModel extends DefaultFlexiTableDataSourceModel<BigBlueButtonMeetingAdminInfos> {

	private static final SoMeetingsCols[] COLS = SoMeetingsCols.values();
	
	public BigBlueButtonMeetingDataModel(BigBlueButtonMeetingDataSource source, FlexiTableColumnModel columnModel) {
		super(source, columnModel);
	}
	
	@Override
	public void clear() {
		super.clear();
		getSourceDelegate().resetCount();
	}

	@Override
	public Object getValueAt(int row, int col) {
		BigBlueButtonMeetingAdminInfos meetingInfos = getObject(row);
		BigBlueButtonMeeting meeting = meetingInfos.getMeeting();
		switch(COLS[col]) {
			case name: return meeting.getName();
			case permanent: return Boolean.valueOf(meeting.isPermanent());
			case startDate: return meeting.getStartDate();
			case endDate: return meeting.getEndDate();
			case autoDelete: return meetingInfos.getAutoDeleteDate();
			case server: return getServer(meeting);
			case template: return getTemplate(meeting);
			case resource: return getContext(meeting);
			case recordings: return meetingInfos.getNumOfRecordings();
			default: return "ERROR";
		}
	}
	
	private String getServer(BigBlueButtonMeeting meeting) {
		BigBlueButtonServer server = meeting.getServer();
		return server == null ? null : server.getUrl();
	}
	
	private String getTemplate(BigBlueButtonMeeting meeting) {
		BigBlueButtonMeetingTemplate template = meeting.getTemplate();
		return template == null ? null: template.getName();
	}
	
	private String getContext(BigBlueButtonMeeting meeting) {
		RepositoryEntry entry = meeting.getEntry();
		if(entry != null) {
			return entry.getDisplayname();
		}
		BusinessGroup group = meeting.getBusinessGroup();
		return group == null ? null : group.getName();
	}

	@Override
	public BigBlueButtonMeetingDataSource getSourceDelegate() {
		return (BigBlueButtonMeetingDataSource)super.getSourceDelegate();
	}

	@Override
	public BigBlueButtonMeetingDataModel createCopyWithEmptyList() {
		return new BigBlueButtonMeetingDataModel(getSourceDelegate(), getTableColumnModel());
	}
	
	public enum SoMeetingsCols implements FlexiSortableColumnDef {
		
		name("meeting.name"),
		permanent("table.header.permanent"),
		startDate("meeting.start"),
		endDate("meeting.end"),
		autoDelete("table.header.auto.delete"),
		template("table.header.template"),
		server("table.header.server"),
		resource("meeting.resource"),
		recordings("table.header.recording.meetings");
		
		private final String i18nHeaderKey;
		
		private SoMeetingsCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return true;
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
