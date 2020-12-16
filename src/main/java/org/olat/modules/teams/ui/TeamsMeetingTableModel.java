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

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.modules.teams.TeamsMeeting;

/**
 * 
 * Initial date: 24 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsMeetingTableModel extends DefaultFlexiTableDataModel<TeamsMeeting>
implements SortableFlexiTableDataModel<TeamsMeeting> {
	
	private static final MeetingsCols[] COLS = MeetingsCols.values();
	
	private final Locale locale;
	
	public TeamsMeetingTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<TeamsMeeting> views = new TeamsMeetingTableSort(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		TeamsMeeting meeting = getObject(row);
		return getValueAt(meeting, col);
	}

	@Override
	public Object getValueAt(TeamsMeeting row, int col) {
		switch(COLS[col]) {
			case subject: return row.getSubject();
			case permanent: return Boolean.valueOf(row.isPermanent());
			case start: return row.getStartDate();
			case end: return row.getEndDate();
			case edit: return canEdit(row);
			default: return "ERROR";
		}
	}
	
	private boolean canEdit(TeamsMeeting row) {
		return row.isPermanent()
				|| (row.getEndWithFollowupTime() != null && row.getEndWithFollowupTime().after(new Date()));
	}

	@Override
	public DefaultFlexiTableDataModel<TeamsMeeting> createCopyWithEmptyList() {
		return new TeamsMeetingTableModel(getTableColumnModel(), locale);
	}
	
	public enum MeetingsCols implements FlexiSortableColumnDef {
		
		subject("meeting.subject"),
		permanent("table.header.permanent"),
		start("meeting.start"),
		end("meeting.end"),
		edit("edit");
		
		private final String i18nHeaderKey;
		
		private MeetingsCols(String i18nHeaderKey) {
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
