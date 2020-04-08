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

import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
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
implements SortableFlexiTableDataModel<BigBlueButtonServerRow> {
	
	private static final ServersCols[] COLS = ServersCols.values();

	private final Locale locale;
	
	public BigBlueButtonAdminServersTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey sortKey) {
		//
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
			case enabled: return row.isEnabled();
			case capacityFactor: return row.getCapacityFactor();
			case moderatorCount: return row.getModeratorCount();
			case participantCount: return row.getParticipantCount();
			case listenerCount: return row.getListenerCount();
			case voiceParticipantCount: return row.getVoiceParticipantCount();
			case videoCount: return row.getVideoCount();
			case maxUsers: return row.getMaxUsers();
			case recordingMeetings: return row.getRecordingMeetings();
			case breakoutRecordingMeetings: return row.getBreakoutRecordingMeetings();
			case load: return row.getLoad();
			default: return "ERROR";
		}
	}

	@Override
	public BigBlueButtonAdminServersTableModel createCopyWithEmptyList() {
		return new BigBlueButtonAdminServersTableModel(getTableColumnModel(), locale);
	}
	
	public enum ServersCols implements FlexiSortableColumnDef {
		
		url("table.header.server.url"),
		enabled("table.header.server.enabled"),
		capacityFactor("table.header.capacity.factor"),
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
