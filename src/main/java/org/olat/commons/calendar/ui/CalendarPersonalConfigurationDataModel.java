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

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 26.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CalendarPersonalConfigurationDataModel extends DefaultFlexiTableDataModel<CalendarPersonalConfigurationRow> {
	
	public CalendarPersonalConfigurationDataModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public DefaultFlexiTableDataModel<CalendarPersonalConfigurationRow> createCopyWithEmptyList() {
		return new CalendarPersonalConfigurationDataModel(getTableColumnModel());
	}

	@Override
	public Object getValueAt(int row, int col) {
		CalendarPersonalConfigurationRow configRow = getObject(row);
		switch(ConfigCols.values()[col]) {
			case type: return configRow.getWrapper();
			case name: return configRow.getDisplayName();
			case cssClass: return configRow.getColorLink();//.getCssClass();
			case visible: return configRow.getVisibleLink();
			case aggregated: return configRow.getAggregatedLink();
			case feed: return configRow.getFeedLink();
			case tools: return configRow.getToolsLink();
		}
		return null;
	}
	
	public enum ConfigCols {
		type("table.header.type"),
		name("table.header.name"),
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
	}
}
