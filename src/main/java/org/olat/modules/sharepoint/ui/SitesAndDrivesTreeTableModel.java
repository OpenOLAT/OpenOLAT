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

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 8 juil. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SitesAndDrivesTreeTableModel extends DefaultFlexiTreeTableDataModel<SiteAndDriveRow> {
	
	private static final SitesAndDrivesCols[] COLS = SitesAndDrivesCols.values();
	
	public SitesAndDrivesTreeTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		//
	}

	@Override
	public Object getValueAt(int row, int col) {
		SiteAndDriveRow peerReviewRow = getObject(row);
		return getValueAt(peerReviewRow, col);
	}

	public Object getValueAt(SiteAndDriveRow row, int col) {
		return switch(COLS[col]) {
			case id -> row.getId();
			case name -> row.getName();
			default -> "ERROR";
		};
	}
	
	
	@Override
	public boolean hasChildren(int row) {
		SiteAndDriveRow peerReviewRow = getObject(row);
		return peerReviewRow.getParent() == null;
	}

	public enum SitesAndDrivesCols implements FlexiSortableColumnDef {
		
		id("table.header.id"),
		name("table.header.name");
		
		private final String i18nKey;
		
		private SitesAndDrivesCols(String i18nKey) {
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
