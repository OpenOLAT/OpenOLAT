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
import org.olat.modules.bigbluebutton.BigBlueButtonServer;

/**
 * 
 * Initial date: 7 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonConfigurationServersTableModel extends DefaultFlexiTableDataModel<BigBlueButtonServer>
implements SortableFlexiTableDataModel<BigBlueButtonServer> {
	
	private static final ConfigServerCols[] COLS = ConfigServerCols.values();

	private final Locale locale;
	
	public BigBlueButtonConfigurationServersTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey sortKey) {
		//
	}

	@Override
	public Object getValueAt(int row, int col) {
		BigBlueButtonServer server = getObject(row);
		return getValueAt(server, col);
	}

	@Override
	public Object getValueAt(BigBlueButtonServer row, int col) {
		switch(COLS[col]) {
			case url: return row.getUrl();
			case recordingUrl: return row.getRecordingUrl();
			case enabled: return Boolean.valueOf(row.isEnabled());
			default: return "ERROR";
		}
	}

	@Override
	public BigBlueButtonConfigurationServersTableModel createCopyWithEmptyList() {
		return new BigBlueButtonConfigurationServersTableModel(getTableColumnModel(), locale);
	}
	
	public enum ConfigServerCols implements FlexiSortableColumnDef {
		
		url("table.header.server.url"),
		recordingUrl("table.header.server.recording"),
		enabled("table.header.server.enabled");
		
		private final String i18nHeaderKey;
		
		private ConfigServerCols(String i18nHeaderKey) {
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
