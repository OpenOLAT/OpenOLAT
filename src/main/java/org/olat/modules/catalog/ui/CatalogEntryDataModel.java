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
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiBusinessPathModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * 
 * Initial date: 24 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogEntryDataModel extends DefaultFlexiTableDataModel<CatalogEntryRow>
		implements SortableFlexiTableDataModel<CatalogEntryRow>, FlexiBusinessPathModel {
	
	static final String SORT_BY_PRIORITY = "sort.by.priority";
	static final CatalogEntryCols[] COLS = CatalogEntryCols.values();
	
	private final Locale locale;
	
	public CatalogEntryDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	public CatalogEntryRow getObjectByResourceKey(Long key) {
		List<CatalogEntryRow> rows = getObjects();
		for (CatalogEntryRow row: rows) {
			if (row != null && row.getOlatResource().getKey().equals(key)) {
				return row;
			}
		}
		return null;
	}
	
	public CatalogEntryRow getObjectByRepositoryEntryKey(Long key) {
		List<CatalogEntryRow> rows = getObjects();
		for (CatalogEntryRow row: rows) {
			if (row != null && row.getRepositoryEntryKey() != null && row.getRepositoryEntryKey().equals(key)) {
				return row;
			}
		}
		return null;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		List<CatalogEntryRow> rows =  new CatalogEntryRowSortDelegate(orderBy, this, locale).sort();
		super.setObjects(rows);
	}
	
	@Override
	public String getUrl(Component source, Object object, String action) {
		if (action == null) {
			return null;
		}
		
		if (object instanceof CatalogEntryRow catalogEntryRow) {
			if (CatalogEntryListController.CMD_DETAILS.equals(action)) {
				return catalogEntryRow.getInfoUrl();
			}
			if (CatalogEntryListController.CMD_TITLE.equals(action)) {
				return catalogEntryRow.getInfoUrl();
			}
			if ("book".equals(action)) {
				return catalogEntryRow.getInfoUrl();
			}
			if ("start".equals(action)) {
				return catalogEntryRow.getStartUrl();
			}
		}
		return null;
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CatalogEntryRow category = getObject(row);
		return getValueAt(category, col);
	}

	@Override
	public Object getValueAt(CatalogEntryRow catalogEntryRow, int col) {
		switch(COLS[col]) {
			case key: return catalogEntryRow.getOlatResource().getKey();
			case type: return catalogEntryRow;
			case title: return catalogEntryRow.getTitle();
			case externalId: return catalogEntryRow.getExternalId();
			case externalRef: return catalogEntryRow.getExternalRef();
			case lifecycleLabel: return catalogEntryRow.getLifecycleLabel();
			case lifecycleSoftkey: return catalogEntryRow.getLifecycleSoftKey();
			case lifecycleStart: return catalogEntryRow.getLifecycleStart();
			case lifecycleEnd: return catalogEntryRow.getLifecycleEnd();
			case location: return catalogEntryRow.getLocation();
			case author: return catalogEntryRow.getAuthors();
			case mainLanguage: return catalogEntryRow.getMainLanguage();
			case expenditureOfWork: return catalogEntryRow.getExpenditureOfWork();
			case educationalType: return catalogEntryRow.getEducationalType();
			case taxonomyLevels: return catalogEntryRow.getTaxonomyLevelTags();
			case offers: return catalogEntryRow.getAccessInfo();
			case availability: return catalogEntryRow;
			case details: return catalogEntryRow;
			case detailsSmall: return catalogEntryRow;
			case start: return catalogEntryRow.getStartLink();
			case startSmall: return catalogEntryRow.getStartLink();
			case certificate: return catalogEntryRow.isCertificate();
			case creditPoints: return catalogEntryRow.getCreditPointAmount();
		}
		return null;
	}
	
	public enum CatalogEntryCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		type("table.header.typeimg"),
		title("table.header.title"),
		externalId("table.header.externalid"),
		externalRef("table.header.externalref"),
		lifecycleLabel("table.header.lifecycle.label"),
		lifecycleSoftkey("table.header.lifecycle.softkey"),
		lifecycleStart("table.header.lifecycle.start"),
		lifecycleEnd("table.header.lifecycle.end"),
		location("table.header.location"),
		author("table.header.author"),
		mainLanguage("table.header.main.language"),
		expenditureOfWork("table.header.expenditure.of.work"),
		educationalType("table.header.educational.type"),
		taxonomyLevels("table.header.taxonomy.levels"),
		offers("table.header.offers"),
		availability("table.header.availability"),
		details("table.header.learn.more"),
		detailsSmall("table.header.learn.more"),
		start("table.header.start"),
		startSmall("table.header.start"),
		certificate("table.header.certificate"),
		creditPoints("table.header.credit.points");
		
		private final String i18nHeaderKey;
		
		private CatalogEntryCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return this != type
					&& this != educationalType
					&& this != taxonomyLevels
					&& this != offers
					&& this != availability
					&& this != details
					&& this != detailsSmall
					&& this != start
					&& this != startSmall;
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
