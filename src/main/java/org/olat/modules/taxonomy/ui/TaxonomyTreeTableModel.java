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
package org.olat.modules.taxonomy.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 14 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyTreeTableModel extends DefaultFlexiTableDataModel<TaxonomyLevelRow>
implements FilterableFlexiTableModel  {
	
	private List<TaxonomyLevelRow> backup;
	
	public TaxonomyTreeTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}
	
	@Override
	public void filter(List<FlexiTableFilter> filters) {
		if(filters != null && filters.size() > 0 && filters.get(0) != null) {
			Set<Long> typeKeys = new HashSet<>();
			boolean noType = false;
			for(FlexiTableFilter filter:filters) {
				if("-".equals(filter.getFilter())) {
					noType = true;
				} else if(StringHelper.isLong(filter.getFilter())) {
					typeKeys.add(new Long(filter.getFilter()));
				}
			}
			
			List<TaxonomyLevelRow> filteredRows = new ArrayList<>(backup.size());
			for(TaxonomyLevelRow row:backup) {
				if(accept(row, typeKeys, noType)) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backup);
		}
	}
	
	private boolean accept(TaxonomyLevelRow row, Set<Long> typeKeys, boolean noType) {
		if(row.getTypeKey() == null) {
			return noType;
		}
		return typeKeys.contains(row.getTypeKey());
	}

	@Override
	public DefaultFlexiTableDataModel<TaxonomyLevelRow> createCopyWithEmptyList() {
		return new TaxonomyTreeTableModel(getTableColumnModel());
	}

	@Override
	public Object getValueAt(int row, int col) {
		TaxonomyLevelRow level = getObject(row);
		switch(TaxonomyLevelCols.values()[col]) {
			case key: return level.getKey();
			case displayName: return level.getDisplayName();
			case identifier: return level.getIdentifier();
			case typeIdentifier: return level.getTypeIdentifier();
			case numOfChildren: return level.getNumberOfChildren();
			case tools: return level.getToolsLink();
			default: return "ERROR";
		}
	}
	
	@Override
	public void setObjects(List<TaxonomyLevelRow> objects) {
		super.setObjects(objects);
		backup = objects;
	}

	public enum TaxonomyLevelCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		displayName("table.header.taxonomy.level.displayName"),
		identifier("table.header.taxonomy.level.identifier"),
		typeIdentifier("table.header.taxonomy.level.type.identifier"),
		numOfChildren("table.header.taxonomy.level.num.children"),
		tools("table.header.actions");
		
		private final String i18nKey;
		
		private TaxonomyLevelCols(String i18nKey) {
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
