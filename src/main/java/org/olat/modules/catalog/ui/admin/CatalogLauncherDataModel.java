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
package org.olat.modules.catalog.ui.admin;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 2 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.comm
 *
 */
public class CatalogLauncherDataModel extends DefaultFlexiTableDataModel<CatalogLauncherRow> {
	
	private static final CatalogLauncherCols[] COLS = CatalogLauncherCols.values();
	
	
	public CatalogLauncherDataModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		CatalogLauncherRow performanceClassRow = getObject(row);
		return getValueAt(performanceClassRow, col);
	}

	private Object getValueAt(CatalogLauncherRow row, int col) {
		switch(COLS[col]) {
			case upDown: return row.getUpDown();
			case type: return row.getTranslatedType();
			case name: return row.getTranslatedName();
			case details: return row.getDetails();
			case enabled: return Boolean.valueOf(row.getCatalogLauncher().isEnabled());
			case tools: return row.getToolsLink();
			default: return null;
		}
	}
	
	public enum CatalogLauncherCols implements FlexiColumnDef {
		upDown("table.header.updown"),
		type("admin.launcher.type"),
		name("admin.launcher.name"),
		details("admin.launcher.details"),
		enabled("admin.launcher.enabled"),
		tools("table.header.tools");
		
		private final String i18nKey;
		
		private CatalogLauncherCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
