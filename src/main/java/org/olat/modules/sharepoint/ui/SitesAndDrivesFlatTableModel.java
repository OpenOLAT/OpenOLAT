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
package org.olat.modules.sharepoint.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.modules.sharepoint.model.SiteAndDriveConfiguration;

/**
 * 
 * Initial date: 8 juil. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SitesAndDrivesFlatTableModel extends DefaultFlexiTableDataModel<SiteAndDriveConfiguration> {
	
	private static final ConfigurationCols[] COLS = ConfigurationCols.values();
	
	public SitesAndDrivesFlatTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		SiteAndDriveConfiguration config = getObject(row);
		return switch(COLS[col]) {
			case siteId -> config.getSiteId();
			case siteDisplayName -> config.getSiteDisplayName();
			case siteName -> config.getSiteName();
			case driveId -> config.getDriveId();
			case driveName -> config.getDriveName();
			default -> "ERROR";
		};
	}
	
	public enum ConfigurationCols implements FlexiSortableColumnDef {
		
		siteId("table.header.site.id"),
		siteDisplayName("table.header.site.displayname"),
		siteName("table.header.site.name"),
		driveId("table.header.drive.id"),
		driveName("table.header.drive.name");
		
		private final String i18nKey;
		
		private ConfigurationCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}

}
