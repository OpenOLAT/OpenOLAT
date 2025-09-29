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

import static org.olat.repository.ui.list.ImplementationsListController.*;

import java.util.ArrayList;
import java.util.Date;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter.DateRange;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElementStatus;

/**
 * 
 * Initial date: 23 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImplementationsListDataModel extends DefaultFlexiTableDataModel<ImplementationRow>
implements SortableFlexiTableDataModel<ImplementationRow>, FilterableFlexiTableModel {
	
	private static final ImplementationsCols[] COLS = ImplementationsCols.values();

	private final Locale locale;
	private List<ImplementationRow> backups;
	
	public ImplementationsListDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	public boolean hasMarked() {
		if(backups == null || backups.isEmpty()) return false;
		return backups.stream().anyMatch(ImplementationRow::isMarked);
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<ImplementationRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(StringHelper.containsNonWhitespace(searchString) || !filters.isEmpty()) {
			final String loweredSearchString = searchString == null || !StringHelper.containsNonWhitespace(searchString)
					? null : searchString.toLowerCase();
			
			final List<String> status = getFilteredList(filters, FILTER_STATUS);
			final Set<CurriculumElementStatus> statusSet = status == null
					? Set.of()
					: status.stream().map(CurriculumElementStatus::valueOf).collect(Collectors.toSet());
			final Boolean marked = getFilteredOneClick(filters, FILTER_MARKED);
			final List<String> curriculumKeys = getFilteredList(filters, FILTER_CURRICULUM);
			final DateRange period = getFilteredDateRange(filters, FILTER_PERIOD);
			
			List<ImplementationRow> filteredRows = new ArrayList<>(backups.size());
			for(ImplementationRow row:backups) {
				boolean accept = accept(loweredSearchString, row)
						&& acceptMarked(marked, row)
						&& acceptStatus(statusSet, row)
						&& acceptCurriculum(curriculumKeys, row)
						&& acceptDateRange(period, row);
				if(accept) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backups);
		}
	}
	
	private boolean accept(String searchValue, ImplementationRow implementationRow) {
		if(searchValue == null) return true;
		return accept(searchValue, implementationRow.getIdentifier())
				|| accept(searchValue, implementationRow.getDisplayName())
				|| accept(searchValue, implementationRow.getCurriculum().getIdentifier())
				|| accept(searchValue, implementationRow.getCurriculum().getDisplayName());
	}
	
	private boolean accept(String searchValue, String val) {
		return val != null && val.toLowerCase().contains(searchValue);
	}
	
	private boolean acceptMarked(Boolean marked, ImplementationRow implementationRow) {
		if(marked == null) return true;
		return Boolean.TRUE.equals(marked) && implementationRow.isMarked();
	}
	
	private boolean acceptStatus(Set<CurriculumElementStatus> status, ImplementationRow implementationRow) {
		if(status == null || status.isEmpty()) return true;
		return status.contains(implementationRow.getStatus());
	}
	
	private boolean acceptCurriculum(List<String> curriculumKeys, ImplementationRow implementationRow) {
		if(curriculumKeys == null || curriculumKeys.isEmpty()) return true;
		return curriculumKeys.contains(implementationRow.getCurriculum().getKey().toString());
	}
	
	private boolean acceptDateRange(DateRange dateRange, ImplementationRow row) {
		if(dateRange == null ||
				(dateRange.getStart() == null && dateRange.getEnd() == null)) {
			return true;
		}
		Date begin = dateRange.getStart();
		Date end = dateRange.getEnd();
		if(begin != null && end != null) {
			return row.getBeginDate() != null && begin.compareTo(row.getBeginDate()) <= 0
					&& row.getEndDate() != null && end.compareTo(row.getEndDate()) >= 0;
		}
		if(begin != null) {
			return row.getBeginDate() != null && begin.compareTo(row.getBeginDate()) <= 0;
		}
		if( end != null) {
			return row.getEndDate() != null && end.compareTo(row.getEndDate()) >= 0;
		}
		return false;
	}
	
	private Boolean getFilteredOneClick(List<FlexiTableFilter> filters, String id) {
    	FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, id);
		if(filter instanceof FlexiTableExtendedFilter extendedFilter) {
			String filterValue = extendedFilter.getValue();
			if (id.equals(filterValue)) {
				return Boolean.TRUE;
			}
		}
		return null;
	}
	
	private List<String> getFilteredList(List<FlexiTableFilter> filters, String filterName) {
    	FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, filterName);
		if(filter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			return filterValues != null && !filterValues.isEmpty() ? filterValues : null;
		}
		return null;
	}
	
	private DateRange getFilteredDateRange(List<FlexiTableFilter> filters, String filterName) {
		FlexiTableFilter pFilter = FlexiTableFilter.getFilter(filters, filterName);
		if (pFilter instanceof FlexiTableDateRangeFilter dateRangeFilter) {
			DateRange dateRange = dateRangeFilter.getDateRange();
			if(dateRange != null) {
				return dateRange;
			}
		}
		return null;
	}
	
	public ImplementationRow getObjectByKey(Long key) {
		return backups.stream()
				.filter(r -> key.equals(r.getKey()))
				.findFirst().orElse(null);
	}

	@Override
	public Object getValueAt(int row, int col) {
		ImplementationRow implementationRow = getObject(row);
		return getValueAt(implementationRow, col);
	}

	@Override
	public Object getValueAt(ImplementationRow row, int col) {
		return switch(COLS[col]) {
			case key -> row.getKey();
			case displayName -> row.getDisplayName();
			case externalRef -> row.getIdentifier();
			case curriculum -> row.getCurriculum();
			case lifecycleStart -> row.getBeginDate();
			case lifecycleEnd -> row.getEndDate();
			case elementStatus -> row.getStatus();
			case mark -> row.getMarkLink();
			default -> "ERROR";
		};
	}	
	
	@Override
	public void setObjects(List<ImplementationRow> objects) {
		super.setObjects(objects);
		backups = objects;
	}
	
	public enum ImplementationsCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		displayName("cif.title"),
		externalRef("table.header.externalref"),
		curriculum("table.header.curriculum"),
		lifecycleStart("table.header.lifecycle.start"),
		lifecycleEnd("table.header.lifecycle.end"),
		elementStatus("table.header.status"),
		mark("table.header.mark");
		
		private final String i18nKey;
		
		private ImplementationsCols(String i18nKey) {
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
