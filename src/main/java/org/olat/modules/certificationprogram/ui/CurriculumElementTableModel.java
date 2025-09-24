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
package org.olat.modules.certificationprogram.ui;

import static org.olat.modules.certificationprogram.ui.CertificationProgramCurriculumElementListController.FILTER_ELEMENT_STATUS;
import static org.olat.modules.certificationprogram.ui.CertificationProgramCurriculumElementListController.FILTER_WITH_CONTENT;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElementStatus;

/**
 * 
 * Initial date: 29 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementTableModel extends DefaultFlexiTableDataModel<CurriculumElementRow>
implements SortableFlexiTableDataModel<CurriculumElementRow>, FilterableFlexiTableModel {
	
	private static final CurriculumElementCols[] COLS = CurriculumElementCols.values();

	private final Locale locale;
	private List<CurriculumElementRow> backupList;
	
	public CurriculumElementTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(StringHelper.containsNonWhitespace(searchString) || !filters.isEmpty()) {
			final String loweredSearchString = searchString == null || !StringHelper.containsNonWhitespace(searchString)
					? null : searchString.toLowerCase();

			final List<String> status = getFilteredList(filters, FILTER_ELEMENT_STATUS);
			final boolean withContent = isFilterSelected(filters, FILTER_WITH_CONTENT);
			final Set<CurriculumElementStatus> statusSet = status == null
					? Set.of()
					: status.stream().map(CurriculumElementStatus::valueOf).collect(Collectors.toSet());
			
			List<CurriculumElementRow> filteredRows = new ArrayList<>(backupList.size());
			for(CurriculumElementRow row:backupList) {
				boolean accept = accept(loweredSearchString, row)
						&& acceptStatus(statusSet, row)
						&& acceptWitContent(withContent, row);
				if(accept) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupList);
		}
	}
	
	private boolean accept(String searchValue, CurriculumElementRow elementRow) {
		if(searchValue == null) return true;
		return accept(searchValue, elementRow.getDisplayName())
				|| accept(searchValue, elementRow.getIdentifier());
	}
	
	private boolean accept(String searchValue, String val) {
		return val != null && val.toLowerCase().contains(searchValue);
	}
	
	private boolean acceptStatus(Set<CurriculumElementStatus> status, CurriculumElementRow elementRow) {
		if(status == null || status.isEmpty()) return true;
		return status.contains(elementRow.getElementStatus());
	}
	
	private List<String> getFilteredList(List<FlexiTableFilter> filters, String filterName) {
    	FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, filterName);
		if(filter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			return filterValues != null && !filterValues.isEmpty() ? filterValues : null;
		}
		return null;
	}
	
	private boolean acceptWitContent(boolean withContent, CurriculumElementRow elementRow) {
		if(!withContent) return true;
		return elementRow.getNumOfResources() > 0;
	}
	
	private boolean isFilterSelected(List<FlexiTableFilter> filters, String id) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, id);
		if (filter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)filter).getValues();
			return filterValues != null && filterValues.contains(id);
		}
		return false;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CurriculumElementRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CurriculumElementRow elementRow = getObject(row);
		return getValueAt(elementRow, col);
	}

	@Override
	public Object getValueAt(CurriculumElementRow row, int col) {
		return switch(COLS[col]) {
			case key -> row.getKey();		
			case identifier -> row.getIdentifier();
			case displayName -> row.getDisplayName();
			case externalId -> row.getExternalId();
			case curriculum -> row.getCurriculumDisplayName();
			case beginDate -> row.getBeginDate();
			case endDate -> row.getEndDate();
			case type -> row.getCurriculumElementTypeDisplayName();
			case status -> row.getElementStatus();
			case resources -> row.getResources();
			case numOfParticipants -> Long.valueOf(row.getNumOfParticipants());
			case numOfCertifiedParticipants -> Long.valueOf(row.getNumOfCertifiedParticipants());
			case tools -> Boolean.TRUE;
			default -> "ERROR";
		};
	}
	
	@Override
	public void setObjects(List<CurriculumElementRow> objects) {
		this.backupList = objects;
		super.setObjects(objects);
	}
	
	public enum CurriculumElementCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		displayName("table.header.displayname"),
		identifier("table.header.identifier"),
		externalId("table.header.external.id"),
		curriculum("table.header.curriculum"),
		beginDate("table.header.begin.date"),
		endDate("table.header.end.date"),
		type("table.header.type"),
		status("table.header.status"),
		resources("table.header.resources"),
		numOfParticipants("table.header.num.of.participants"),
		numOfCertifiedParticipants("table.header.users.certified"),
		tools("action.more");
		
		private final String i18nHeaderKey;
		
		private CurriculumElementCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return true;
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
