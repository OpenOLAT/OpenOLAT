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
package org.olat.modules.curriculum.ui;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiBusinessPathModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.ui.component.CurriculumElementViewsRowComparator;
import org.olat.repository.RepositoryEntryStatusEnum;

/**
 * 
 * Initial date: 11 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementWithViewsDataModel extends DefaultFlexiTreeTableDataModel<CurriculumElementWithViewsRow> implements FlexiBusinessPathModel {

	private final Locale locale;
	
	public CurriculumElementWithViewsDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		filterTab(searchString, null);
	}
	
	public void filterTab(String searchString, FlexiFiltersTab tab) {
		if(StringHelper.containsNonWhitespace(searchString) || tab != null) {
			String lowerSearchString = searchString == null ? null : searchString.toLowerCase();
			Long searchKey = StringHelper.isLong(lowerSearchString) ? Long.valueOf(lowerSearchString) : null;
			List<CurriculumElementStatus> statusList = filteredStatus(tab);
			List<RepositoryEntryStatusEnum> entryStatusList = filteredEntryStatus(tab);
			
			List<CurriculumElementWithViewsRow> filteredRows = backupRows.stream()
				.filter(row -> (quickSearch(lowerSearchString, row) || searchKey(searchKey, row)))
				.filter(row -> filterStatus(row, statusList, entryStatusList))
				.collect(Collectors.toList());

			reconstructParentLine(filteredRows);
			Collections.sort(filteredRows, new CurriculumElementViewsRowComparator(locale));
			setFilteredObjects(filteredRows);
			
			// Open all filtered
			openedRows.clear();
			for(CurriculumElementWithViewsRow currentRow:filteredRows) {
				if(currentRow.getParent() != null) {
					openedRows.add(currentRow.getParent());
				} else if(currentRow.getParent() == null) {
					openedRows.add(currentRow);
				}
			}
		} else {
			setUnfilteredObjects();
		}
	}
	
	private List<CurriculumElementStatus> filteredStatus(FlexiFiltersTab tab) {
		if(tab == null) return List.of();
		return switch(tab.getId()) {
			case CurriculumElementListController.ALL_TAB -> CurriculumElementListController.ALL_STATUS;
			case CurriculumElementListController.ACTIVE_TAB -> CurriculumElementListController.ACTIVE_STATUS;
			case CurriculumElementListController.PREPARATION_TAB -> CurriculumElementListController.PREPARATION_STATUS;
			case CurriculumElementListController.FINISHED_TAB -> CurriculumElementListController.FINISHED_STATUS;
			default -> List.of();
		};
	}
	
	private List<RepositoryEntryStatusEnum> filteredEntryStatus(FlexiFiltersTab tab) {
		if(tab == null) return List.of();
		return switch(tab.getId()) {
			case CurriculumElementListController.ALL_TAB -> CurriculumElementListController.ALL_ENTRY_STATUS;
			case CurriculumElementListController.ACTIVE_TAB -> CurriculumElementListController.ACTIVE_ENTRY_STATUS;
			case CurriculumElementListController.PREPARATION_TAB -> CurriculumElementListController.PREPARATION_ENTRY_STATUS;
			case CurriculumElementListController.FINISHED_TAB -> CurriculumElementListController.FINISHED_ENTRY_STATUS;
			default -> List.of();
		};
	}
	
	private boolean filterStatus(CurriculumElementWithViewsRow row, List<CurriculumElementStatus> statusList,
			List<RepositoryEntryStatusEnum> entryStatusList) {
		if(statusList.isEmpty()) return true;
		
		CurriculumElementStatus elementStatus = row.getCurriculumElementStatus();
		RepositoryEntryStatusEnum entryStatus = row.getEntryStatus();
		return (elementStatus != null && statusList.contains(elementStatus))
				|| (entryStatus != null && entryStatusList.contains(entryStatus));
	}
	
	private void reconstructParentLine(List<CurriculumElementWithViewsRow> rows) {
		Set<CurriculumElementWithViewsRow> rowSet = new HashSet<>(rows);
		for(int i=0; i<rows.size(); i++) {
			CurriculumElementWithViewsRow row = rows.get(i);
			for(CurriculumElementWithViewsRow parent=row.getParent(); parent != null && !rowSet.contains(parent); parent=parent.getParent()) {
				rows.add(i, parent);
				rowSet.add(parent);	
			}
		}
	}
	
	private boolean searchKey(Long searchKey, CurriculumElementWithViewsRow row) {
		if(searchKey == null) return false;
		return (row.getCurriculumElementKey() != null && row.getCurriculumElementKey().equals(searchKey))
				|| (row.getRepositoryEntryKey() != null && row.getRepositoryEntryKey().equals(searchKey));
	}
	
	private boolean quickSearch(String searchString, CurriculumElementWithViewsRow row) {
		if(!StringHelper.containsNonWhitespace(searchString)) {
			return true;
		}
		return (row.getCurriculumElementDisplayName() != null && row.getCurriculumElementDisplayName().toLowerCase().contains(searchString))
				|| (row.getCurriculumElementIdentifier() != null && row.getCurriculumElementIdentifier().toLowerCase().contains(searchString))
				|| (row.getCurriculumElementExternalId() != null && row.getCurriculumElementExternalId().toLowerCase().contains(searchString))
				|| (row.getRepositoryEntryDisplayName() != null && row.getRepositoryEntryDisplayName().toLowerCase().contains(searchString))
				|| (row.getRepositoryEntryExternalRef() != null && row.getRepositoryEntryExternalRef().toLowerCase().contains(searchString))
				|| (row.getRepositoryEntryAuthors() != null && row.getRepositoryEntryAuthors().toLowerCase().contains(searchString));
	}
	
	
	@Override
	public String getUrl(Component source, Object object, String action) {
		if("select".equals(action) && object instanceof CurriculumElementWithViewsRow row) {
			if(row.getStartUrl() != null) {
				return row.getStartUrl();
			}
			if(row.getDetailsUrl() != null) {
				return row.getDetailsUrl();
			}
		}
		return null;
	}

	@Override
	public boolean hasChildren(int row) {
		CurriculumElementWithViewsRow element = getObject(row);
		return element.hasChildren();
	}

	@Override
	public Object getValueAt(int row, int col) {
		CurriculumElementWithViewsRow curriculum = getObject(row);
		switch(ElementViewCols.values()[col]) {
			case key: return curriculum.getKey();
			case displayName: {
				String displayName;
				if(curriculum.isRepositoryEntryOnly()) {
					displayName = curriculum.getRepositoryEntryDisplayName();
				} else {
					displayName = curriculum.getCurriculumElementDisplayName();
				}
				return displayName;
			}
			case identifier: {
				String identifier;
				if(curriculum.isRepositoryEntryOnly()) {
					identifier = curriculum.getRepositoryEntryExternalRef();
				} else {
					identifier = curriculum.getCurriculumElementIdentifier();
				}
				return identifier;
			}
			case beginDate: return curriculum.getCurriculumElementBeginDate();
			case endDate: return curriculum.getCurriculumElementEndDate();
			case mark: return curriculum.getMarkLink();
			case select: return curriculum.getSelectLink();
			case details: return curriculum.getDetailsLink();
			case start: return curriculum.getStartLink();
			case calendars: return curriculum.getCalendarsLink();
			case completion: return curriculum.getCompletionItem();
			default: return "ERROR";
		}
	}
	
	public enum ElementViewCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		displayName("table.header.curriculum.element.display.name"),
		identifier("table.header.external.ref"),
		mark("table.header.mark"),
		select("table.header.entry.displayName"),
		completion("table.header.completion"),
		details("table.header.details"),
		start("table.header.start"),
		calendars("table.header.calendars"),
		beginDate("table.header.begin.date"),
		endDate("table.header.end.date");
		
		private final String i18nHeaderKey;
		
		private ElementViewCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return false;
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
