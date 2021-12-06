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
package org.olat.commons.calendar.ui;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * 
 * Initial date: 26.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CalendarPersonalConfigurationDataModel extends DefaultFlexiTableDataModel<CalendarPersonalConfigurationRow>
	implements SortableFlexiTableDataModel<CalendarPersonalConfigurationRow> {
	
	public CalendarPersonalConfigurationDataModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CalendarPersonalConfigurationRow> configRows = new CalendarPersonalConfigurationTableSort(orderBy, this, null).sort();
			super.setObjects(configRows);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CalendarPersonalConfigurationRow configRow = getObject(row);
		return getValueAt(configRow, col);
	}

	@Override
	public Object getValueAt(CalendarPersonalConfigurationRow row, int col) {
		switch(ConfigCols.values()[col]) {
			case type: return row.getWrapper();
			case name: return row.getDisplayName();
			case identifier: return row.getIdentifier();
			case cssClass: return row.getColorLink();
			case visible: return row.getVisibleLink();
			case aggregated: return row.getAggregatedLink();
			case feed: return row.getFeedLink();
			case tools: return row.getToolsLink();
		}
		return null;
	}
	
	public enum ConfigCols implements FlexiSortableColumnDef {
		type("table.header.type"),
		name("table.header.name"),
		identifier("table.header.identifier"),
		cssClass("table.header.color"),
		visible("table.header.visible"),
		aggregated("table.header.aggregated.feed"),
		feed("table.header.url"),
		tools("table.header.tools");
		
		private final String i18nKey;
		
		private ConfigCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != tools && this != feed;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
