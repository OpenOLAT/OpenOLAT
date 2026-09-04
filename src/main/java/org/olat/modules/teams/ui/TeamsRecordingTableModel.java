/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.teams.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 25 août 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class TeamsRecordingTableModel extends DefaultFlexiTableDataModel<TeamsRecordingRow>
implements SortableFlexiTableDataModel<TeamsRecordingRow> {
	
	private static final TeamsRecordingsCols[] COLS = TeamsRecordingsCols.values();
	
	private final Locale locale;
	
	public TeamsRecordingTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<TeamsRecordingRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		TeamsRecordingRow recording = getObject(row);
		return getValueAt(recording, col) ;
	}

	@Override
	public Object getValueAt(TeamsRecordingRow row, int col) {
		switch(COLS[col]) {
			case name: return row.getName();
			case start: return row.getStart();
			case end: return row.getEnd();
			case permanent: return row.getPermanent();
			case open: return row.isPublished();
			case publish: return row.getPublishLink();
			case tools: return row.getToolsLink();
			default: return "ERROR";
		}
	}
	
	public enum TeamsRecordingsCols implements FlexiSortableColumnDef {
		
		name("table.header.recording.name"),
		start("table.header.recording.start"),
		end("table.header.recording.end"),
		open("table.header.recording.open"),
		publish("table.header.publish"),
		permanent("table.header.recording.permanent"),
		tools("action.more");
		
		private final String i18nHeaderKey;
		
		private TeamsRecordingsCols(String i18nHeaderKey) {
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

