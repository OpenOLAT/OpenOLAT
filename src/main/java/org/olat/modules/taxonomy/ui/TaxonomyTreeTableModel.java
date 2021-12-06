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
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.util.StringHelper;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 14 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyTreeTableModel extends DefaultFlexiTreeTableDataModel<TaxonomyLevelRow>
implements FilterableFlexiTableModel  {
	
	public TaxonomyTreeTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}
	
	public int indexOf(TaxonomyLevel level) {
		List<TaxonomyLevelRow> objects = getObjects();
		for(int i=0; i<objects.size(); i++) {
			if(level.getKey().equals(objects.get(i).getKey())) {
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(filters != null && !filters.isEmpty() && filters.get(0) != null) {
			Set<Long> typeKeys = new HashSet<>();
			boolean noType = false;
			boolean showAll = false;
			for(FlexiTableFilter filter:filters) {
				if("-".equals(filter.getFilter())) {
					noType = true;
				} else if(StringHelper.isLong(filter.getFilter())) {
					typeKeys.add(Long.valueOf(filter.getFilter()));
				} else if(filter.isShowAll()) {
					showAll = true;
				}
			}
			
			if(showAll) {
				setUnfilteredObjects();
			} else {
				List<TaxonomyLevelRow> filteredRows = new ArrayList<>(backupRows.size());
				for(TaxonomyLevelRow row:backupRows) {
					if(accept(row, typeKeys, noType)) {
						filteredRows.add(row);
					}
				}
				setFilteredObjects(filteredRows);
			}
		} else {
			setUnfilteredObjects();
		}
	}
	
	private boolean accept(TaxonomyLevelRow row, Set<Long> typeKeys, boolean noType) {
		if(row.getTypeKey() == null) {
			return noType;
		}
		return typeKeys.contains(row.getTypeKey());
	}

	@Override
	public boolean hasChildren(int row) {
		TaxonomyLevelRow level = getObject(row);
		if(level.getNumberOfChildren() == 0) {
			return false;
		}
		return true;
	}

	@Override
	public Object getValueAt(int row, int col) {
		TaxonomyLevelRow level = getObject(row);
		switch(TaxonomyLevelCols.values()[col]) {
			case key: return level.getKey();
			case displayName: return level.getDisplayName();
			case identifier: return level.getIdentifier();
			case externalId: return level.getExternalId();
			case typeIdentifier: return level.getTypeIdentifier();
			case numOfChildren: return level.getNumberOfChildren();
			case tools: return level.getToolsLink();
			case updateWarning: return level.isUpdated();
			case description: return level.getDescription();
			case order: return level.getOrder();
			case path: return level.getTaxonomyLevel().getMaterializedPathIdentifiersWithoutSlash();
			default: return "ERROR";
		}
	}

	public enum TaxonomyLevelCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		displayName("table.header.taxonomy.level.displayName"),
		identifier("table.header.taxonomy.level.identifier"),
		externalId("table.header.taxonomy.level.externalId"),
		typeIdentifier("table.header.taxonomy.level.type.identifier"),
		numOfChildren("table.header.taxonomy.level.num.children"),
		tools("table.header.actions"),
		order("table.header.taxonomy.level.order"),
		description("table.header.taxonomy.level.description"),
		updateWarning("table.header.taxonomy.update.warning", "o_icon o_icon_fw o_icon_warn"),
		path("table.header.taxonomy.level.path");
		
		private final String i18nKey;
		private final String iconHeader;
		
		
		private TaxonomyLevelCols(String i18nKey) {
			this(i18nKey, null);
		}
		
		private TaxonomyLevelCols(String i18nKey, String iconHeader) {
			this.i18nKey = i18nKey;
			this.iconHeader = iconHeader;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
		
		@Override
		public String iconHeader() {
			return iconHeader;
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