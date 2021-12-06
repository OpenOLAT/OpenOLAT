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
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;

/**
 * 
 * Initial date: 19 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonTemplateTableModel extends DefaultFlexiTableDataModel<BigBlueButtonMeetingTemplate>
implements SortableFlexiTableDataModel<BigBlueButtonMeetingTemplate> {
	
	private static final BTemplatesCols[] COLS = BTemplatesCols.values();
	
	private final Locale locale;
	
	public BigBlueButtonTemplateTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<BigBlueButtonMeetingTemplate> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		BigBlueButtonMeetingTemplate meeting = getObject(row);
		return getValueAt(meeting, col);
	}

	@Override
	public Object getValueAt(BigBlueButtonMeetingTemplate row, int col) {
		switch(COLS[col]) {
			case name: return row.getName();
			case system: return Boolean.valueOf(row.isSystem());
			case enabled: return Boolean.valueOf(row.isEnabled());
			case maxConcurrentMeetings: return row.getMaxConcurrentMeetings();
			case maxParticipants: return row.getMaxParticipants();
			case maxDuration: return row.getMaxDuration();
			case webcamsOnlyForModerator: return row.getWebcamsOnlyForModerator();
			case externalUsers: return row.isExternalUsersAllowed();
			default: return "ERROR";
		}
	}
	
	public enum BTemplatesCols implements FlexiSortableColumnDef {
		
		name("meeting.name"),
		system("table.header.system"),
		enabled("table.header.enabled"),
		maxConcurrentMeetings("table.header.max.concurrent.meetings"),
		maxParticipants("table.header.max.participants"),
		maxDuration("table.header.max.duration"),
		webcamsOnlyForModerator("table.header.webcams.only.moderator"),
		externalUsers("table.header.external.users");
		
		private final String i18nHeaderKey;
		
		private BTemplatesCols(String i18nHeaderKey) {
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
