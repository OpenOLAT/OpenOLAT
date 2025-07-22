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
package org.olat.modules.coach.ui;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter.DateRange;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  8 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CoursesTableDataModel extends DefaultFlexiTableDataModel<CourseStatEntryRow>
	implements SortableFlexiTableDataModel<CourseStatEntryRow>, FilterableFlexiTableModel {

	private final Locale locale;
	private List<CourseStatEntryRow> backupList;
	
	private static final Columns[] COLS = Columns.values();

	public CoursesTableDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}
	
	public int getIndexOfObject(CourseStatEntryRow entry) {
		return getObjects().indexOf(entry);
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			super.setObjects(new CoursesTableSortDelegate(orderBy, this, locale).sort());
		}
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(StringHelper.containsNonWhitespace(searchString) || !filters.isEmpty()) {
			final String loweredSearchString = searchString == null || !StringHelper.containsNonWhitespace(searchString)
					? null : searchString.toLowerCase();

			final String certificates = getFiltered(filters, CourseListController.FILTER_CERTIFICATES);
			final List<String> assessment = getFilteredList(filters, CourseListController.FILTER_ASSESSMENT);
			final List<String> status = getFilteredList(filters, CourseListController.FILTER_STATUS);
			final LocalDateTime now = DateUtils.toLocalDateTime(new Date());
			final String lastVisit = getFiltered(filters, CourseListController.FILTER_LAST_VISIT);
			final Boolean marked = getFilteredOneClick(filters, CourseListController.FILTER_MARKED);
			final Boolean notVisited = getFilteredOneClick(filters, CourseListController.FILTER_NOT_VISITED);
			final Boolean withParticipants = getFilteredOneClick(filters, CourseListController.FILTER_WITH_PARTICIPANTS);	
			final Boolean withoutParticipants = getFilteredOneClick(filters, CourseListController.FILTER_WITHOUT_PARTICIPANTS);	
			final DateRange executionPeriod = getFilterRange(filters);
			
			List<CourseStatEntryRow> filteredRows = new ArrayList<>(backupList.size());
			for(CourseStatEntryRow row:backupList) {
				boolean accept = accept(loweredSearchString, row)
						&& acceptAssessment(assessment, row)
						&& acceptStatus(status, row)
						&& acceptMarked(marked, row)
						&& acceptNotVisited(notVisited, row)
						&& acceptWithParticipants(withParticipants, row)
						&& acceptWithoutParticipants(withoutParticipants, row)
						&& acceptDateRange(executionPeriod, row)
						&& acceptCertificates(certificates, row)
						&& acceptLastVisit(lastVisit, now, row);
				if(accept) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupList);
		}
	}
	
	private boolean accept(String searchValue, CourseStatEntryRow entry) {
		if(searchValue == null) return true;
		return accept(searchValue, entry.getRepoDisplayName())
				|| accept(searchValue, entry.getRepoExternalRef())
				|| accept(searchValue, entry.getRepoExternalId());
	}
	
	private boolean accept(String searchValue, String val) {
		return val != null && val.toLowerCase().contains(searchValue);
	}

	private boolean acceptStatus(List<String> refs, CourseStatEntryRow entry) {
		if(refs == null || refs.isEmpty()) return true;
		return refs.stream()
				.anyMatch(ref -> entry.getRepoStatus() != null && ref.equals(entry.getRepoStatus().name()));
	}
	
	private boolean acceptAssessment(List<String> refs, CourseStatEntryRow entry) {
		if(refs == null || refs.isEmpty()) return true;
		
		return refs.stream().anyMatch(ref -> {
			return switch(ref) {
				// Passed
				case CourseListController.ASSESSMENT_PASSED_NONE
					-> entry.getSuccessStatus().numPassed() == 0;
				case CourseListController.ASSESSMENT_PASSED_PARTIALLY
					-> entry.getSuccessStatus().numPassed() > 0
						&& (entry.getSuccessStatus().numFailed() > 0 || entry.getSuccessStatus().numUndefined() > 0);
				case CourseListController.ASSESSMENT_PASSED_ALL
					-> entry.getSuccessStatus().numPassed() > 0
						&& entry.getSuccessStatus().numFailed() == 0 && entry.getSuccessStatus().numUndefined() == 0;
				
				// Not passed
				case CourseListController.ASSESSMENT_NOT_PASSED_NONE
					-> entry.getSuccessStatus().numFailed() == 0;
				case CourseListController.ASSESSMENT_NOT_PASSED_PARTIALLY
					-> entry.getSuccessStatus().numFailed() > 0
						&& (entry.getSuccessStatus().numPassed() > 0 || entry.getSuccessStatus().numUndefined() > 0);
				case CourseListController.ASSESSMENT_NOT_PASSED_ALL
					-> entry.getSuccessStatus().numFailed() > 0
						&& entry.getSuccessStatus().numPassed() == 0 && entry.getSuccessStatus().numUndefined() == 0;
				default -> false;
			};
		});
	}
	
	private boolean acceptLastVisit(String ref, LocalDateTime now, CourseStatEntryRow entry) {
		if(ref == null) return true;
		if(entry.getLastVisit() == null) return false;
		
		LocalDateTime lastVisit = DateUtils.toLocalDateTime(entry.getLastVisit());
		if(CourseListController.VISIT_LESS_1_DAY.equals(ref)) {
			return ChronoUnit.HOURS.between(lastVisit, now) < 24;
		}
		if(CourseListController.VISIT_LESS_1_WEEK.equals(ref)) {
			return ChronoUnit.WEEKS.between(lastVisit, now) < 1;
		}
		if(CourseListController.VISIT_LESS_4_WEEKS.equals(ref)) {
			return ChronoUnit.WEEKS.between(lastVisit, now) < 4;
		}
		if(CourseListController.VISIT_LESS_12_MONTHS.equals(ref)) {
			return ChronoUnit.MONTHS.between(lastVisit, now) < 12;
		}
		if(CourseListController.VISIT_MORE_12_MONTS.equals(ref)) {
			return ChronoUnit.MONTHS.between(lastVisit, now) >= 12;
		}
		return false;
	}
	
	private boolean acceptCertificates(String ref, CourseStatEntryRow entry) {
		if(ref == null) return true;
		
		return (CourseListController.CERTIFICATES_WITH.equals(ref) && entry.getCertificates().numOfCertificates() > 0)
				|| (CourseListController.CERTIFICATES_WITHOUT.equals(ref) && entry.getCertificates().numOfCertificates() == 0)
				|| (CourseListController.CERTIFICATES_INVALID.equals(ref) && entry.getCertificates().numOfCoursesWithInvalidCertificates() > 0);
	}
	
	private String getFiltered(List<FlexiTableFilter> filters, String filterName) {
    	FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, filterName);
		if(filter instanceof FlexiTableExtendedFilter extendedFilter) {
			String filterValue = extendedFilter.getValue();
			if (StringHelper.containsNonWhitespace(filterValue)) {
				return filterValue;
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
	
	private boolean acceptMarked(Boolean marked, CourseStatEntryRow entry) {
		if(marked == null) return true;
		return Boolean.TRUE.equals(marked) && entry.isMarked();
	}
	
	private boolean acceptNotVisited(Boolean notVisited, CourseStatEntryRow entry) {
		if(notVisited == null) return true;
		return Boolean.TRUE.equals(notVisited) && entry.getParticipantsNotVisited() > 0;
	}
	
	private boolean acceptWithParticipants(Boolean withParticipants, CourseStatEntryRow entry) {
		if(withParticipants == null) return true;
		return Boolean.TRUE.equals(withParticipants) && entry.getParticipants() > 0;
	}
	
	private boolean acceptWithoutParticipants(Boolean withoutParticipants, CourseStatEntryRow entry) {
		if(withoutParticipants == null) return true;
		return Boolean.TRUE.equals(withoutParticipants) && entry.getParticipants() == 0;
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
	
	private boolean acceptDateRange(DateRange range, CourseStatEntryRow row) {
		if(range == null) return true;
		
		Date begin = range.getStart();
		Date end = range.getEnd();
		if(begin == null && end == null) return true;
		
		if(begin != null && end != null) {
			return row.getLifecycleStartDate() != null && begin.compareTo(row.getLifecycleStartDate()) <= 0
					&& row.getLifecycleEndDate() != null && end.compareTo(row.getLifecycleEndDate()) >= 0;
		}
		if(begin != null) {
			return row.getLifecycleStartDate() != null && begin.compareTo(row.getLifecycleStartDate()) <= 0;
		}
		if( end != null) {
			return row.getLifecycleEndDate() != null && end.compareTo(row.getLifecycleEndDate()) >= 0;
		}
		return false;
	}
	
	private DateRange getFilterRange(List<FlexiTableFilter> filters) {
		FlexiTableFilter pFilter = FlexiTableFilter.getFilter(filters, CourseListController.FILTER_PERIOD);
		if (pFilter instanceof FlexiTableDateRangeFilter dateRangeFilter) {
			return dateRangeFilter.getDateRange();
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		CourseStatEntryRow c = getObject(row);
		return getValueAt(c, col);
	}

	@Override
	public Object getValueAt(CourseStatEntryRow row, int col) {
		return switch(COLS[col]) {
			case key -> row.getRepoKey();
			case mark -> row.getMarkLink();
			case technicalType -> row.getRepoTechnicalType();
			case name -> row.getRepoDisplayName();
			case externalId -> row.getRepoExternalId();
			case externalRef -> row.getRepoExternalRef();
			case lifecycleStart -> row.getLifecycleStartDate();
			case lifecycleEnd -> row.getLifecycleEndDate();
			case access -> row.getRepoStatus();
			case participants -> row.getParticipants();
			case participantsVisited -> row.getParticipantsVisited();
			case participantsNotVisited -> row.getParticipantsNotVisited();
			case lastVisit -> row.getLastVisit();
			case completion -> row.getAverageCompletion();
			case successStatus -> row.getSuccessStatus();
			case statusPassed -> row.getStatusPassed();
			case statusNotPassed -> row.getStatusNotPassed();
			case statusUndefined -> row.getStatusUndefined();
			case averageScore -> row.getAverageScore();
			case certificates -> row.getCertificates().numOfCoursesWithCertificates() > 0
					? row.getCertificates().numOfCertificates() : "";
			case tools -> Boolean.TRUE;
			default -> "ERROR";
		};
	}
	
	@Override
	public void setObjects(List<CourseStatEntryRow> objects) {
		this.backupList = objects;
		super.setObjects(objects);
	}
	
	public enum Columns implements FlexiSortableColumnDef {
		key("table.header.course.key"),
		mark("table.header.mark"),
		technicalType("table.header.technical.type"),
		name("table.header.course.title"),
		externalId("table.header.course.externalId"),
		externalRef("table.header.course.externalRef"),
		lifecycleStart("table.header.lifecycle.start"),
		lifecycleEnd("table.header.lifecycle.end"),
		access("table.header.course.access"),
		participants("table.header.participants"),
		participantsVisited("table.header.participants.visited"),
		participantsNotVisited("table.header.participants.not.visited"),
		lastVisit("table.header.last.visit"),
		completion("table.header.completion"),
		successStatus("table.header.success.status"),
		statusPassed("table.header.status.passed"),
		statusNotPassed("table.header.status.not.passed"),
		statusUndefined("table.header.status.undefined"),
		averageScore("table.header.averageScore"),
		certificates("table.header.certificates"),
		assessmentTool("table.header.assessment.tool"),
		infos("table.header.infos"),
		tools("action.more");
		
		private final String i18nKey;
		
		private Columns(String i18nKey) {
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

		public static Columns getValueAt(int ordinal) {
			if(ordinal >= 0 && ordinal < values().length) {
				return values()[ordinal];
			}
			return name;
		}
	}
}
