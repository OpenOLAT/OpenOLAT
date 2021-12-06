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
package org.olat.modules.adobeconnect.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.adobeconnect.AdobeConnectMeeting;

/**
 * 
 * Initial date: 4 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectMeetingTableModel extends DefaultFlexiTableDataModel<AdobeConnectMeeting>
implements SortableFlexiTableDataModel<AdobeConnectMeeting> {
	
	private final Locale locale;
	
	public AdobeConnectMeetingTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<AdobeConnectMeeting> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		AdobeConnectMeeting meeting = getObject(row);
		return getValueAt(meeting, col);
	}

	@Override
	public Object getValueAt(AdobeConnectMeeting row, int col) {
		switch(ACMeetingsCols.values()[col]) {
			case name: return row.getName();
			case permanent: return row.isPermanent();
			case start: return row.getStartDate();
			case end: return row.getEndDate();
			case resource: return getResourceName(row);
			default: return "ERROR";
		}
	}
	
	private String getResourceName(AdobeConnectMeeting row) {
		String displayName = null;
		if(row.getEntry() != null) {
			displayName = row.getEntry().getDisplayname();
		} else if(row.getBusinessGroup() != null) {
			displayName = row.getBusinessGroup().getName();
		}
		return displayName;
	}
	
	public enum ACMeetingsCols implements FlexiSortableColumnDef {
		
		name("meeting.name"),
		permanent("table.header.permanent"),
		start("meeting.start"),
		end("meeting.end"),
		resource("meeting.resource");
		
		private final String i18nHeaderKey;
		
		private ACMeetingsCols(String i18nHeaderKey) {
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
