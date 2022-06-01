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
package org.olat.course.nodes.practice.ui;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * 
 * Initial date: 6 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharedResourceTableModel extends DefaultFlexiTableDataModel<SharedResourceRow>
implements SortableFlexiTableDataModel<SharedResourceRow> {
	
	private static final SharedResourceCols[] COLS = SharedResourceCols.values();
	
	public SharedResourceTableModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}
	
	@Override
	public void sort(SortKey sortKey) {
		//
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		SharedResourceRow resourceRow = getObject(row);
		return getValueAt(resourceRow, col);
	}

	@Override
	public Object getValueAt(SharedResourceRow row, int col) {
		switch(COLS[col]) {
			case id: return row.getKey();
			case icon: return getIcon(row);
			case name: return row.getName();
			default: return "ERROR";
		}
	}
	
	public String getIcon(SharedResourceRow resource) {
		String iconCssClass = null;
		if(resource.getPool() != null) {
			iconCssClass = "o_icon-lg o_icon_pool_pool";
		} else if(resource.getCollection() != null) {
			iconCssClass = "o_icon-lg o_icon_pool_collection";
		} else if(resource.getBusinessGroup() != null) {
			iconCssClass = "o_icon-lg o_icon_pool_share";
		}
		return iconCssClass;
	}

	public enum SharedResourceCols implements FlexiSortableColumnDef {
		id("table.header.id"),
		icon("table.header.icon"),
		name("table.header.questions");
		
		private final String i18nKey;
		
		private SharedResourceCols(String i18nKey) {
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
