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

package org.olat.core.configuration.model;

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;


/**
 * Initial Date:  27.08.2020 <br>
 * @author aboeckle, mjenny, alexander.boeckle@frentix.com, http://www.frentix.com
 */


public class ConfigurationPropertiesTableModel extends DefaultFlexiTreeTableDataModel<ConfigurationPropertiesContentRow> {

	public ConfigurationPropertiesTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}
	
	@Override
	public boolean hasChildren(int row) {
		ConfigurationPropertiesContentRow element = getObject(row);
		return element.hasChidlren();
	}

	@Override
	public Object getValueAt(int row, int col) {
		ConfigurationPropertiesContentRow contentRow = getObject(row);
		
		switch (ConfigurationPropertiesCols.values()[col]) {
		case key:
			return contentRow.getKey();
		case value:
			return contentRow.getValue();
		default:
			return "ERROR";
		}
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		// Nothing to filter right now		
	}
	
	public enum ConfigurationPropertiesCols implements FlexiColumnDef {
		key("configuration.property.key"),
		value("configuration.property.value");

		private String i18nHeaderKey;
		
		private ConfigurationPropertiesCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return this.i18nHeaderKey;
		}
		
	}	
}
