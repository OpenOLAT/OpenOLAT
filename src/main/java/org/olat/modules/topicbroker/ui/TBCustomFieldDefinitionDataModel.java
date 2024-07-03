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
package org.olat.modules.topicbroker.ui;

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 26 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBCustomFieldDefinitionDataModel extends DefaultFlexiTableDataModel<TBCustomFieldDefinitionRow> {
	
	private static final CustomFieldDefinitionCols[] COLS = CustomFieldDefinitionCols.values();

	public TBCustomFieldDefinitionDataModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}
	
	public TBCustomFieldDefinitionRow getObjectByKey(Long key) {
		List<TBCustomFieldDefinitionRow> rows = getObjects();
		for (TBCustomFieldDefinitionRow row: rows) {
			if (row != null && row.getKey().equals(key)) {
				return row;
			}
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		TBCustomFieldDefinitionRow project = getObject(row);
		return getValueAt(project, col);
	}

	public Object getValueAt(TBCustomFieldDefinitionRow row, int col) {
		switch(COLS[col]) {
		case identifier: return row.getIdentifier();
		case name: return row.getName();
		case type: return row.getTypeName();
		case displayInTable: return row.isDisplayInTable();
		case upDown: return row.getUpDown();
		case tools: return row.getToolsLink();
		default: return null;
		}
	}
	
	public enum CustomFieldDefinitionCols implements FlexiColumnDef {
		identifier("custom.field.def.identifier"),
		name("custom.field.def.name"),
		type("custom.field.def.type"),
		displayInTable("custom.field.def.in.table"),
		upDown("updown"),
		tools("tools");
		
		private final String i18nKey;
		
		private CustomFieldDefinitionCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
