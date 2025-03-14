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
package org.olat.repository.ui.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.util.StringHelper;
import org.olat.repository.ui.list.DefaultRepositoryEntryDataSource.FilterButton;

/**
 * 
 * Initial date: 13 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class InPreparationDataModel extends DefaultFlexiTableDataModel<InPreparationRow>
implements SortableFlexiTableDataModel<InPreparationRow>, FilterableFlexiTableModel {
	
	private static final InPreparationCols[] COLS = InPreparationCols.values();
	
	private final Locale locale;
	private List<InPreparationRow> backups;
	
	public InPreparationDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<InPreparationRow> rows = new InPreparationSortDelegate(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(StringHelper.containsNonWhitespace(searchString)
				|| (filters != null && !filters.isEmpty() && filters.get(0) != null)) {
			
			String authors = null;
			List<Long> educationalTypesKeys = List.of();
			String lowerSearchString = searchString.toLowerCase();
			Long searchKey = StringHelper.isLong(lowerSearchString) ? Long.valueOf(lowerSearchString) : null;
			
			FlexiTableFilter authorsFilter = FlexiTableFilter.getFilter(filters, FilterButton.AUTHORS.name());
			if (authorsFilter != null) {
				authors = authorsFilter.getValue() == null ? null: authorsFilter.getValue().toLowerCase();
			}
			
			FlexiTableFilter educationTypesFilter = FlexiTableFilter.getFilter(filters, FilterButton.EDUCATIONALTYPE.name());
			if (educationTypesFilter instanceof FlexiTableExtendedFilter extendedFilter) {
				List<String> filterValues = extendedFilter.getValues();
				if(filterValues != null && !filterValues.isEmpty()) {
					educationalTypesKeys = filterValues.stream()
							.map(Long::valueOf).toList();
				}
			}
			
			List<InPreparationRow> filteredRows = new ArrayList<>();
			for(InPreparationRow row:backups) {
				if((quickSearch(lowerSearchString, row) || searchKey(searchKey, row))
						&& acceptEducationalTypes(educationalTypesKeys, row)
						&& acceptAuthors(authors, row)) {
					filteredRows.add(row);
				}
			}

			super.setObjects(filteredRows);
		} else {
			super.setObjects(backups);
		}
	}
	
	private boolean acceptAuthors(String author, InPreparationRow row) {
		if(!StringHelper.containsNonWhitespace(author)) return true;
		return row.getAuthors() != null
				&& row.getAuthors().toLowerCase().contains(author);		
	}
	
	private boolean acceptEducationalTypes(List<Long> educationalTypesKeys, InPreparationRow row) {
		if(educationalTypesKeys == null || educationalTypesKeys.isEmpty()) return true;
		return row.getEducationalType() != null
				&& educationalTypesKeys.contains(row.getEducationalType().getKey());		
	}
	
	private boolean searchKey(Long searchKey, InPreparationRow row) {
		if(searchKey == null) return false;
		return (row.getCurriculumElementKey() != null && row.getCurriculumElementKey().equals(searchKey))
				|| (row.getRepositoryEntryKey() != null && row.getRepositoryEntryKey().equals(searchKey));
	}
	
	private boolean quickSearch(String searchString, InPreparationRow row) {
		if(!StringHelper.containsNonWhitespace(searchString)) {
			return true;
		}
		return (row.getDisplayName() != null && row.getDisplayName().toLowerCase().contains(searchString))
				|| (row.getExternalId() != null && row.getExternalId().toLowerCase().contains(searchString))
				|| (row.getExternalRef() != null && row.getExternalRef().toLowerCase().contains(searchString))
				|| (row.getAuthors() != null && row.getAuthors().toLowerCase().contains(searchString));
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		InPreparationRow entryRow = getObject(row);
		return getValueAt(entryRow, col);
	}

	@Override
	public Object getValueAt(InPreparationRow row, int col) {
		return switch(COLS[col]) {
			case key -> (row.getRepositoryEntryKey() == null ? row.getCurriculumElementKey() : row.getRepositoryEntryKey());
			case creationDate -> row.getCreationDate();
			case lastModified -> row.getLastModified();
			case displayName -> row.getDisplayName();
			case externalId -> row.getExternalId();
			case externalRef -> row.getExternalRef();
			case lifecycleLabel -> row.getLifecycleLabel();
			case lifecycleSoftkey -> row.getLifecycleSoftKey();
			case lifecycleStart -> row.getLifecycleStart();
			case lifecycleEnd -> row.getLifecycleEnd();
			case authors -> row.getAuthors();
			case location -> row.getLocation();
			case educationalType -> row.getEducationalType();
			case details -> row.getDetailsLink();
			case detailsSmall -> row.getDetailsSmallLink();
			case type -> row;
			default -> "ERROR";
		};
	}
	
	@Override
	public void setObjects(List<InPreparationRow> objects) {
		super.setObjects(objects);
		backups = objects;
	}

	public enum InPreparationCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		creationDate("cif.displayname"),
		lastModified("cif.displayname"),
		displayName("cif.displayname"),
		externalId("table.header.externalid"),
		externalRef("table.header.externalref"),
		lifecycleLabel("table.header.lifecycle.label"),
		lifecycleSoftkey("table.header.lifecycle.softkey"),
		lifecycleStart("table.header.lifecycle.start"),
		lifecycleEnd("table.header.lifecycle.end"),
		authors("table.header.authors"),
		location("table.header.location"),
		educationalType("table.header.educational.type"),
		details("table.header.learn.more"),
		detailsSmall("table.header.learn.more"),
		type("table.header.typeimg");
		
		private final String i18nKey;
		
		private InPreparationCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != details && this != detailsSmall;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
