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
package org.olat.modules.catalog.ui;

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataSourceModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 24 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogRepositoryEntryDataModel extends DefaultFlexiTableDataSourceModel<CatalogRepositoryEntryRow> {
	
	private static final CatalogRepositoryEntryCols[] COLS = CatalogRepositoryEntryCols.values();
	
	public CatalogRepositoryEntryDataModel(CatalogRepositoryEntryDataSource source, FlexiTableColumnModel columnModel) {
		super(source, columnModel);
	}
	
	public CatalogRepositoryEntryRow getObjectByKey(Long key) {
		List<CatalogRepositoryEntryRow> rows = getObjects();
		for (CatalogRepositoryEntryRow row: rows) {
			if (row != null && row.getKey().equals(key)) {
				return row;
			}
		}
		return null;
	}

	@Override
	public void clear() {
		super.clear();
		getSourceDelegate().resetCount();
	}

	@Override
	public DefaultFlexiTableDataSourceModel<CatalogRepositoryEntryRow> createCopyWithEmptyList() {
		return new CatalogRepositoryEntryDataModel(getSourceDelegate(), getTableColumnModel());
	}

	@Override
	public CatalogRepositoryEntryDataSource getSourceDelegate() {
		return (CatalogRepositoryEntryDataSource)super.getSourceDelegate();
	}

	@Override
	public Object getValueAt(int row, int col) {
		CatalogRepositoryEntryRow catalogRepositoryEntryRow = getObject(row);
		if(catalogRepositoryEntryRow == null) {
			return null;
		}
		
		switch(COLS[col]) {
			case key: return catalogRepositoryEntryRow.getKey();
			case type: return catalogRepositoryEntryRow;
			case displayName: return catalogRepositoryEntryRow.getSelectLink();
			case externalId: return catalogRepositoryEntryRow.getExternalId();
			case externalRef: return catalogRepositoryEntryRow.getExternalRef();
			case lifecycleLabel: return catalogRepositoryEntryRow.getLifecycleLabel();
			case lifecycleSoftkey: return catalogRepositoryEntryRow.getLifecycleSoftKey();
			case lifecycleStart: return catalogRepositoryEntryRow.getLifecycleStart();
			case lifecycleEnd: return catalogRepositoryEntryRow.getLifecycleEnd();
			case location: return catalogRepositoryEntryRow.getLocation();
			case educationalType: return catalogRepositoryEntryRow.getEducationalType();
			case offers: return catalogRepositoryEntryRow;
			case details: return catalogRepositoryEntryRow.getDetailsLink();
			case start: return catalogRepositoryEntryRow.getStartLink();
		}
		return null;
	}
	
	public enum CatalogRepositoryEntryCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		type("table.header.typeimg"),
		displayName("table.header.displayname"),
		externalId("table.header.externalid"),
		externalRef("table.header.externalref"),
		lifecycleLabel("table.header.lifecycle.label"),
		lifecycleSoftkey("table.header.lifecycle.softkey"),
		lifecycleStart("table.header.lifecycle.start"),
		lifecycleEnd("table.header.lifecycle.end"),
		location("table.header.location"),
		educationalType("table.header.educational.type"),
		offers("table.header.offers"),
		details("table.header.details"),
		start("table.header.start");
		
		private final String i18nHeaderKey;
		
		private CatalogRepositoryEntryCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return this != educationalType
					&& this != offers
					&& this != details
					&& this != start;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}
}
