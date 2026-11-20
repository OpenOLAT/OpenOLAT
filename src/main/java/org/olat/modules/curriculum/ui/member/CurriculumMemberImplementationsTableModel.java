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
package org.olat.modules.curriculum.ui.member;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiBusinessPathModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter.DateRange;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumRoles;

/**
 * 
 * Initial date: 19 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumMemberImplementationsTableModel extends DefaultFlexiTableDataModel<CurriculumMemberImplementationRow>
implements SortableFlexiTableDataModel<CurriculumMemberImplementationRow>, FilterableFlexiTableModel, FlexiBusinessPathModel {
	
	private static final ImplementationCols[] COLS = ImplementationCols.values();
	private final Locale locale;
	
	private List<CurriculumMemberImplementationRow> backupRows;
	
	public CurriculumMemberImplementationsTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			super.setObjects(new CurriculumMemberImplementationsTableModelSortDelegate(orderBy, this, locale).sort());
		}
	}
	
	@Override
	public void filter(String quickSearch, List<FlexiTableFilter> filters) {
		if(filters != null && (StringHelper.containsNonWhitespace(quickSearch) || (!filters.isEmpty() && filters.get(0) != null))) {
			String searchString = quickSearch == null ? null : quickSearch.toLowerCase();
			DateRange period = getFilterRange(filters);
			List<CurriculumRoles> roles = getFilterByRoles(filters);
			List<Long> typesKeys = getFilterByKeys(filters, CurriculumMemberImplementationsController.FILTER_TYPE);
			List<Long> curriculumsKeys = getFilterByKeys(filters, CurriculumMemberImplementationsController.FILTER_CURRICULUMS);
			List<CurriculumElementStatus> status = getFilterByStatus(filters);
			List<CurriculumMemberImplementationRow> filteredRows = backupRows.stream()
					.filter(row -> acceptSearch(row, searchString)
							&& acceptCurriculums(row, curriculumsKeys)
							&& acceptDateRange(row, period)
							&& acceptTypes(row, typesKeys)
							&& acceptStatus(row, status)
							&& acceptRoles(row, roles))
					.toList();
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupRows);
		}
	}
	
	private List<CurriculumRoles> getFilterByRoles(List<FlexiTableFilter> filters) {
		FlexiTableFilter rolesFilter = FlexiTableFilter.getFilter(filters, CurriculumMemberImplementationsController.FILTER_ROLES);
		if (rolesFilter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			if(filterValues != null && !filterValues.isEmpty()) {
				return filterValues.stream()
						.map(CurriculumRoles::valueOf)
						.toList();
			}
		}
		return List.of();
	}
	
	private List<Long> getFilterByKeys(List<FlexiTableFilter> filters, String filterId) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, filterId);
		if (filter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			if(filterValues != null && !filterValues.isEmpty()) {
				return filterValues.stream()
						.filter(key -> StringHelper.isLong(key))
						.map(Long::valueOf)
						.toList();
			}
		}
		return List.of();
	}
	
	private List<CurriculumElementStatus> getFilterByStatus(List<FlexiTableFilter> filters) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, CurriculumMemberImplementationsController.FILTER_STATUS);
		if (filter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			if(filterValues != null && !filterValues.isEmpty()) {
				return filterValues.stream()
						.map(CurriculumElementStatus::valueOf).toList();
			}
		}
		return List.of();
	}
	
	private DateRange getFilterRange(List<FlexiTableFilter> filters) {
		FlexiTableFilter pFilter = FlexiTableFilter.getFilter(filters, CurriculumMemberImplementationsController.FILTER_PERIOD);
		if (pFilter instanceof FlexiTableDateRangeFilter dateRangeFilter) {
			return dateRangeFilter.getDateRange();
		}
		return null;
	}
	
	private boolean acceptSearch(CurriculumMemberImplementationRow row, String searchString) {
		if(searchString == null) return true;
		return acceptSearch(row.getCurriculumElement().getDisplayName(), searchString)
				|| acceptSearch(row.getCurriculumElement().getIdentifier(), searchString)
				|| acceptSearch(row.getCurriculumElement().getExternalId(), searchString)
				|| acceptSearch(row.getCurriculumElementTypeName(), searchString);
	}
	
	private boolean acceptSearch(String row, String searchString) {
		return row != null && row.toLowerCase().contains(searchString);
	}
	
	private boolean acceptStatus(CurriculumMemberImplementationRow row, List<CurriculumElementStatus> statusList) {
		return statusList.isEmpty()
				|| (row.getStatus() != null && statusList.contains(row.getStatus()));
	}
	
	private boolean acceptCurriculums(CurriculumMemberImplementationRow row, List<Long> curriculumsKeys) {
		if(curriculumsKeys == null || curriculumsKeys.isEmpty()) return true;
		return row.getCurriculum() != null
				&& curriculumsKeys.contains(row.getCurriculum().getKey());
	}
	
	private boolean acceptTypes(CurriculumMemberImplementationRow row, List<Long> typesKeys) {
		if(typesKeys == null || typesKeys.isEmpty()) return true;
		return row.getCurriculumElementType() != null
				&& typesKeys.contains(row.getCurriculumElementType().getKey());
	}
	
	private boolean acceptRoles(CurriculumMemberImplementationRow row, List<CurriculumRoles> roles) {
		if(roles == null || roles.isEmpty()) return true;
		return roles.stream()
				.filter(r -> row.getImplementationRoles().contains(r))
				.findFirst()
				.isPresent();
	}
	
	private boolean acceptDateRange(CurriculumMemberImplementationRow row, DateRange range) {
		if(range == null) return true;
		
		Date begin = range.getStart();
		Date end = range.getEnd();
		if(begin == null && end == null) return true;
		
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
	
	@Override
	public String getUrl(Component source, Object object, String action) {
		if(CurriculumMemberImplementationsController.SELECT_CURRICULUM_CMD.equals(action)
				&& object instanceof CurriculumMemberImplementationRow row) {
			return row.getCurriculumBusinessPath();
		} else if(CurriculumMemberImplementationsController.SELECT_CMD.equals(action)
				&& object instanceof CurriculumMemberImplementationRow row) {
			return row.getImplementationUrl();
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		CurriculumMemberImplementationRow implementationRow = getObject(row);
		return getValueAt(implementationRow, col);
	}

	@Override
	public Object getValueAt(CurriculumMemberImplementationRow row, int col) {
		return switch(COLS[col]) {
			case key -> row.getCurriculumElement().getKey();
			case displayName -> row.getCurriculumElement().getDisplayName();
			case structure -> row.getStructureLink();
			case identifier -> row.getCurriculumElement().getIdentifier();
			case externalId -> row.getCurriculumElement().getExternalId();
			case curriculum -> row.getCurriculum().getDisplayName();
			case beginDate -> row.getCurriculumElement().getBeginDate();
			case endDate -> row.getCurriculumElement().getEndDate();
			case type -> row.getCurriculumElementTypeName();
			case status -> row.getCurriculumElement().getElementStatus();
			case roles -> row.getImplementationRoles();
			case allRoles -> row.getAllRoles();
			case calendars -> row.getCalendarsLink();
			case start -> row.getOpenLink();
			default -> "ERROR";
		};
	}
	
	@Override
	public void setObjects(List<CurriculumMemberImplementationRow> objects) {
		backupRows = new ArrayList<>(objects);
		super.setObjects(objects);
	}
	
	public enum ImplementationCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		displayName("table.header.curriculum.element.display.name"),
		structure("table.header.structure"),
		identifier("table.header.external.ref"),
		externalId("table.header.external.id"),
		curriculum("table.header.curriculum"),
		beginDate("table.header.begin.date"),
		endDate("table.header.end.date"),
		type("table.header.type"),
		status("table.header.status"),
		roles("table.header.role"),
		allRoles("table.header.all.roles"),
		calendars("table.header.calendars"),
		start("open.new.tab"),
		relevance("");

		private final String i18nHeaderKey;
		
		private ImplementationCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return this != start && this != calendars && this != allRoles && this != structure;
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
