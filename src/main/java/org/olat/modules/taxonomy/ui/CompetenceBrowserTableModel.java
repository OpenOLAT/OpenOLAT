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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSelectionDelegate;
import org.olat.core.util.StringHelper;
import org.olat.modules.taxonomy.ui.CompetenceBrowserController.TaxonomyTreeNodeComparator;

/**
 * Initial date: 29.03.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CompetenceBrowserTableModel extends DefaultFlexiTreeTableDataModel<CompetenceBrowserTableRow>
		implements FlexiTableSelectionDelegate<CompetenceBrowserTableRow> {

	private static final CompetenceBrowserCols[] COLS = CompetenceBrowserCols.values();

	private final boolean multiSelection;
	private final Collator collator;
	private String taxonomyFilterKey;

	public CompetenceBrowserTableModel(FlexiTableColumnModel columnModel, boolean multiSelection, Collator collator) {
		super(columnModel);
		this.multiSelection = multiSelection;
		this.collator = collator;
	}

	public void setTaxonomyFilter(String taxonomyFilterKey) {
		this.taxonomyFilterKey = taxonomyFilterKey;
	}

	@Override
	public boolean isSelectable(int row) {
		CompetenceBrowserTableRow tableRow = getObject(row);
		if (tableRow == null) {
			return true;
		}
		if (!multiSelection) {
			return true;
		}
		return !tableRow.isPreselected();
	}

	@Override
	public List<CompetenceBrowserTableRow> getSelectedTreeNodes() {
		return backupRows.stream()
				.filter(CompetenceBrowserTableRow::isSelected)
				.collect(Collectors.toList());
	}

	public void clearAllSelections() {
		for (CompetenceBrowserTableRow row : backupRows) {
			row.setSelected(false);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CompetenceBrowserTableRow tableRow = getObject(row);
		switch(COLS[col]) {
			case key: return tableRow.getKey();
			case competences: return tableRow.getTaxonomyOrLevel();
			case identifier: return tableRow.getIdentifier();
			case externalId: return tableRow.getExternalId();
			case taxonomy: return tableRow.getTaxonomy();
			case details: return tableRow.getDetailsLink();
			default: return "ERROR";
		}
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		boolean hasSearch = StringHelper.containsNonWhitespace(searchString);
		boolean hasTaxonomyFilter = taxonomyFilterKey != null;

		if (!hasSearch && !hasTaxonomyFilter) {
			setUnfilteredObjects();
			return;
		}

		String search = hasSearch ? searchString.toLowerCase() : null;
		List<CompetenceBrowserTableRow> filteredRows = backupRows.stream()
				.filter(row -> search == null || row.containsSearch(search))
				.filter(this::matchesTaxonomy)
				.collect(Collectors.toList());
		List<CompetenceBrowserTableRow> filteredRowsWithParents = new ArrayList<>(filteredRows);

		for (CompetenceBrowserTableRow row : filteredRows) {
			addToFilter(row, filteredRowsWithParents);
		}

		Collections.sort(filteredRowsWithParents, new TaxonomyTreeNodeComparator(collator));
		setFilteredObjects(filteredRowsWithParents);
	}

	private boolean matchesTaxonomy(CompetenceBrowserTableRow row) {
		if (taxonomyFilterKey == null) {
			return true;
		}
		return row.getTaxonomy() != null
				&& taxonomyFilterKey.equals(row.getTaxonomy().getKey().toString());
	}
	
	private void addToFilter(CompetenceBrowserTableRow row, List<CompetenceBrowserTableRow> filteredList) {
		if (!filteredList.contains(row)) {
			filteredList.add(row);
		}
		
		if (row.getParent()!= null) {
			CompetenceBrowserTableRow parent = (CompetenceBrowserTableRow) row.getParent();
			addToFilter(parent, filteredList);
		}
	}
	
	@Override
	public boolean hasChildren(int row) {
		return getObject(row).hasChildren();
	}
	
	public enum CompetenceBrowserCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		competences("table.header.competence"),
		identifier("table.header.taxonomy.level.external.ref"),
		externalId("table.header.taxonomy.level.external.id"),
		taxonomy("table.header.taxonomy.displayName"),
		details("table.header.info", "o_icon o_icon_fw o_icon_description"),
		type("table.header.taxonomy.level.type");
		
		private String iconHeader;
		private String i18nHeaderKey;
		
		private CompetenceBrowserCols(String i18nHeaderKey) {
			this(i18nHeaderKey, null);
		}
		
		private CompetenceBrowserCols(String i18nHeaderKey, String iconHeader) {
			this.i18nHeaderKey = i18nHeaderKey;
			this.iconHeader = iconHeader;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
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
