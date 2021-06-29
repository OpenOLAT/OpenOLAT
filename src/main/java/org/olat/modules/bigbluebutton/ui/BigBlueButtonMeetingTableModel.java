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

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.BigBlueButtonServer;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonMeetingTableModel extends DefaultFlexiTableDataModel<BigBlueButtonMeetingRow>
implements SortableFlexiTableDataModel<BigBlueButtonMeetingRow> {
	
	private static final BMeetingsCols[] COLS = BMeetingsCols.values();
	
	private final Locale locale;
	
	public BigBlueButtonMeetingTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<BigBlueButtonMeetingRow> views = new BigBlueButtonMeetingTableSort(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	public BigBlueButtonMeeting getMeeting(int row) {
		BigBlueButtonMeetingRow r = getObject(row);
		return r == null ? null : r.getMeeting();
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		BigBlueButtonMeetingRow meeting = getObject(row);
		return getValueAt(meeting, col);
	}

	@Override
	public Object getValueAt(BigBlueButtonMeetingRow row, int col) {
		switch(COLS[col]) {
			case name: return row.getName();
			case permanent: return Boolean.valueOf(row.isPermanent());
			case start: return row.getStartDate();
			case end: return row.getEndDate();
			case template: return getTemplate(row);
			case server: return getServer(row);
			case resource: return getResourceName(row);
			case edit: return editable(row);
			case tools: return row.getToolsLink();
			default: return "ERROR";
		}
	}
	
	private Boolean editable(BigBlueButtonMeetingRow row) {
		return row.isPermanent()
				|| (row.getEndDate() != null && !row.getEndDate().before(new Date()));
	}
	
	private String getServer(BigBlueButtonMeetingRow meeting) {
		BigBlueButtonServer server = meeting.getServer();
		return server == null ? null : server.getUrl();
	}
	
	private String getTemplate(BigBlueButtonMeetingRow meeting) {
		BigBlueButtonMeetingTemplate template = meeting.getTemplate();
		return template == null ? null: template.getName();
	}
	
	private String getResourceName(BigBlueButtonMeetingRow row) {
		String displayName = null;
		if(row.getEntry() != null) {
			displayName = row.getEntry().getDisplayname();
		} else if(row.getBusinessGroup() != null) {
			displayName = row.getBusinessGroup().getName();
		}
		return displayName;
	}

	@Override
	public BigBlueButtonMeetingTableModel createCopyWithEmptyList() {
		return new BigBlueButtonMeetingTableModel(getTableColumnModel(), locale);
	}
	
	public enum BMeetingsCols implements FlexiSortableColumnDef {
		
		name("meeting.name"),
		permanent("table.header.permanent"),
		start("meeting.start"),
		end("meeting.end"),
		template("table.header.template"),
		server("table.header.server"),
		resource("meeting.resource"),
		edit("edit"),
		tools("tools");
		
		private final String i18nHeaderKey;
		
		private BMeetingsCols(String i18nHeaderKey) {
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
