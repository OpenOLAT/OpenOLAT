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
package org.olat.modules.portfolio.ui;

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.modules.portfolio.ui.model.BinderRow;

/**
 * 
 * Initial date: 07.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BindersDataModel extends DefaultFlexiTableDataModel<BinderRow> {

	public BindersDataModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		BinderRow portfolio = getObject(row);
		switch(PortfolioCols.values()[col]) {
			case key: return portfolio.getKey();
			case title: return portfolio.getTitle();
			case open: return portfolio.getOpenLink();
			case tools: return portfolio.getToolsLink();
		}
		return null;
	}
	
	public BinderRow getObjectByKey(Long key) {
		if(key == null) return null;
		
		List<BinderRow> rows = getObjects();
		for(BinderRow row:rows) {
			if(key.equals(row.getKey())) {
				return row;
			}
		}
		return null;
	}
	
	public enum PortfolioCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		title("table.header.title"),
		open("table.header.open"),
		tools("table.header.tools");
		
		private final String i18nKey;
		
		private PortfolioCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
