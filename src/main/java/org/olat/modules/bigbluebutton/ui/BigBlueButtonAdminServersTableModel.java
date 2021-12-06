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

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * 
 * Initial date: 7 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonAdminServersTableModel extends DefaultFlexiTableDataModel<BigBlueButtonServerRow>
implements SortableFlexiTableDataModel<BigBlueButtonServerRow>, FilterableFlexiTableModel {
	
	private static final ServersCols[] COLS = ServersCols.values();

	private boolean allInstances = true;
	
	public BigBlueButtonAdminServersTableModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public void sort(SortKey sortKey) {
		//
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(filters != null && !filters.isEmpty() && "this".equals(filters.get(0).getFilter())) {
			allInstances = false;
		} else {
			allInstances = true;
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		BigBlueButtonServerRow server = getObject(row);
		return getValueAt(server, col);
	}

	@Override
	public Object getValueAt(BigBlueButtonServerRow row, int col) {
		switch(COLS[col]) {
			case url: return row.getUrl();
			case status: return row.isEnabled();
			case capacityFactor: return row.getCapacityFactor();
			case numberMeetings:return allInstances
					? row.getAllInstancesServerInfos().getNumberOfMeetings() : row.getServerInfos().getNumberOfMeetings();
			case moderatorCount: return allInstances
					? row.getAllInstancesServerInfos().getModeratorCount() : row.getServerInfos().getModeratorCount();
			case participantCount: return allInstances
					?  row.getAllInstancesServerInfos().getParticipantCount() : row.getServerInfos().getParticipantCount();
			case listenerCount: return allInstances
					?  row.getAllInstancesServerInfos().getListenerCount() : row.getServerInfos().getListenerCount();
			case voiceParticipantCount: return allInstances
					?  row.getAllInstancesServerInfos().getVoiceParticipantCount() : row.getServerInfos().getVoiceParticipantCount();
			case videoCount: return allInstances
					?  row.getAllInstancesServerInfos().getVideoCount() : row.getServerInfos().getVideoCount();
			case maxUsers: return allInstances
					?  row.getAllInstancesServerInfos().getMaxUsers() : row.getServerInfos().getMaxUsers();
			case recordingMeetings: return allInstances
					?  row.getAllInstancesServerInfos().getRecordingMeetings() : row.getServerInfos().getRecordingMeetings();
			case breakoutRecordingMeetings: return allInstances
					?  row.getAllInstancesServerInfos().getBreakoutRecordingMeetings() : row.getServerInfos().getBreakoutRecordingMeetings();
			case load: return allInstances
					?   row.getAllInstancesServerInfos().getLoad() : row.getServerInfos().getLoad();
			default: return "ERROR";
		}
	}
	
	public enum ServersCols implements FlexiSortableColumnDef {
		
		url("table.header.server.url"),
		status("table.header.server.status"),
		capacityFactor("table.header.capacity.factor"),
		numberMeetings("table.header.number.meetings"),
		moderatorCount("table.header.moderator.count"),
		participantCount("table.header.participant.count"),
		listenerCount("table.header.listener.count"),
		voiceParticipantCount("table.header.voice.participant.count"),
		videoCount("table.header.video.count"),
		maxUsers("table.header.max.users"),
		recordingMeetings("table.header.recording.meetings"),
		breakoutRecordingMeetings("table.header.breakout.recording.meetings"),
		load("table.header.load");
		
		private final String i18nHeaderKey;
		
		private ServersCols(String i18nHeaderKey) {
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
