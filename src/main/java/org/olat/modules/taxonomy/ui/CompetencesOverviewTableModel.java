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
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.util.StringHelper;

/**
 * Initial date: 19.03.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CompetencesOverviewTableModel extends DefaultFlexiTreeTableDataModel<CompetencesOverviewTableRow> implements SortableFlexiTableDataModel<CompetencesOverviewTableRow> {

	private static final CompetencesOverviewCols[] COLS = CompetencesOverviewCols.values();
	
	public CompetencesOverviewTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		CompetencesOverviewTableRow competenceRow = getObject(row);
		return getValueAt(competenceRow, col);
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(StringHelper.containsNonWhitespace(searchString)) {
			String lowerSearchString = searchString.toLowerCase();
			List<CompetencesOverviewTableRow> objects = getObjects();
			
			if (objects == null || objects.isEmpty()) {
				setFilteredObjects(new ArrayList<>());
			}
			
			setFilteredObjects(objects.stream().filter(row -> row.getDisplayName().toLowerCase().contains(lowerSearchString)).collect(Collectors.toList()));
		} else {
			setUnfilteredObjects();
		}
	}

	@Override
	public void sort(SortKey sortKey) {
		if (sortKey != null) {
			//
		}
		
	}
	
	@Override
	public boolean hasChildren(int row) {
		return getObject(row).hasChildren();
	}

	@Override
	public Object getValueAt(CompetencesOverviewTableRow row, int col) {
		switch (COLS[col]) {
			case key: return row.getKey();
			case competence: return row.getDisplayName();
			case resource: return row.getResource();
			case type: return row.getType();
			case taxonomyDisplayName: return row.getTaxonomyDisplayName();
			case taxonomyIdentifier: return row.getTaxonomyIdentifier();
			case taxonomyExternalId: return row.getTaxonomyExternalId();
			case taxonomyLevelIdentifier: return row.getTaxonomyLevelIdentifier();
			case taxonomyLevelDisplayName: return row.getTaxonomyLevelDisplayName();
			case taxonomyLevelExternalId: return row.getTaxonomyLevelExternalId();
			case taxonomyLevelType: return row.getTaxonomyLevelType();
			case expiration: return row.getExpiration();
			case creationDate: return row.getCreationDate();
			case remove: return Boolean.valueOf(!row.isManaged() && row.isCompetence());
			case info: return row.getDetailsLink();
			default: return null;
		}
	}
	
	public enum CompetencesOverviewCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		competence("table.header.competence"),
		resource("table.header.resource"),
		taxonomyIdentifier("table.header.taxonomy.identifier"),
		taxonomyDisplayName("table.header.taxonomy.displayName"),
		taxonomyExternalId("table.header.taxonomy.externalId"),
		taxonomyLevelIdentifier("table.header.taxonomy.level.identifier"),
		taxonomyLevelDisplayName("table.header.taxonomy.level.displayName"),
		taxonomyLevelType("table.header.taxonomy.level.type"),
		taxonomyLevelExternalId("table.header.taxonomy.level.externalId"),
		type("table.header.competence.type"),
		expiration("table.header.competence.expiration"),
		info("table.header.info", "o_icon o_icon_fw o_icon_description"),
		creationDate("table.hader.competence.creation.date"),
		
		remove("remove");

		private final String i18nHeaderKey;
		private final String iconHeader;

		private CompetencesOverviewCols(String i18nHeaderKey) {
			this(i18nHeaderKey, null);
		}
		
		private CompetencesOverviewCols(String i18nHeaderKey, String iconHeader) {
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
