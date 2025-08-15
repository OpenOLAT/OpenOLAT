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
import org.olat.course.certificate.CertificateLight;
import org.olat.modules.assessment.ui.component.LearningProgressCompletionCellRenderer.CompletionPassed;
import org.olat.modules.coach.model.EfficiencyStatementEntry;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  8 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class CoursesIdentityTableDataModel extends DefaultFlexiTableDataModel<CourseIdentityRow>
implements SortableFlexiTableDataModel<CourseIdentityRow>, FilterableFlexiTableModel {
	
	private static final Columns[] COLS = Columns.values();

	private final Locale locale;
	private List<CourseIdentityRow> backupList;
	
	public CoursesIdentityTableDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	public void putCertificate(CertificateLight certificate) {
		List<CourseIdentityRow> rows = this.getObjects();
		for(CourseIdentityRow row:rows) {
			if(row.getRepositoryEntryResourceKey().equals(certificate.getOlatResourceKey())) {
				row.setCertificate(certificate);
				break;
			}
		} 
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CourseIdentityRow> views = new CoursesIdentityTableSortDelegate(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(StringHelper.containsNonWhitespace(searchString) || !filters.isEmpty()) {
			final String loweredSearchString = searchString == null || !StringHelper.containsNonWhitespace(searchString)
					? null : searchString.toLowerCase();

			final Boolean marked = getFilteredOneClick(filters, CoursesIdentityController.FILTER_MARKED);
			final List<String> status = getFilteredList(filters, CoursesIdentityController.FILTER_STATUS);
			final String certificates = getFiltered(filters, CoursesIdentityController.FILTER_CERTIFICATES);
			final List<String> assessment = getFilteredList(filters, CoursesIdentityController.FILTER_ASSESSMENT);
			final String lastVisit = getFiltered(filters, CoursesIdentityController.FILTER_LAST_VISIT);
			final DateRange executionPeriod = getFilterRange(filters);

			final Date now = new Date();
			final LocalDateTime localNow = DateUtils.toLocalDateTime(now);
			final List<CourseIdentityRow> filteredRows = new ArrayList<>(backupList.size());
			for(CourseIdentityRow row:backupList) {
				boolean accept = accept(loweredSearchString, row)
						&& acceptStatus(status, row)
						&& acceptMarked(marked, row)
						&& acceptAssessment(assessment, row)
						&& acceptDateRange(executionPeriod, row)
						&& acceptLastVisit(lastVisit, localNow, row)
						&& acceptCertificates(certificates, now, row);
				if(accept) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupList);
		}
	}
	
	private boolean accept(String searchValue, CourseIdentityRow entry) {
		if(searchValue == null) return true;
		return accept(searchValue, entry.getRepositoryEntryDisplayname())
				|| accept(searchValue, entry.getRepositoryEntryExternalRef())
				|| accept(searchValue, entry.getRepositoryEntryExternalId());
	}
	
	private boolean accept(String searchValue, String val) {
		return val != null && val.toLowerCase().contains(searchValue);
	}
	
	private boolean acceptMarked(Boolean marked, CourseIdentityRow entry) {
		if(marked == null) return true;
		return Boolean.TRUE.equals(marked) && entry.isMarked();
	}
	
	private boolean acceptAssessment(List<String> refs, CourseIdentityRow entry) {
		if(refs == null || refs.isEmpty()) return true;
		
		return refs.stream().anyMatch(ref -> {
			return switch(ref) {
				// Passed
				case CoursesIdentityController.ASSESSMENT_PASSED
					-> Boolean.TRUE.equals(entry.getAssessmentEntryPassed());
				// Not passed
				case CoursesIdentityController.ASSESSMENT_NOT_PASSED
					-> Boolean.FALSE.equals(entry.getAssessmentEntryPassed());
				default -> false;
			};
		});
	}
	
	private boolean acceptStatus(List<String> refs, CourseIdentityRow entry) {
		if(refs == null || refs.isEmpty()) return true;
		return refs.stream()
				.anyMatch(ref -> entry.getRepositoryEntryStatus() != null && ref.equals(entry.getRepositoryEntryStatus().name()));
	}
	
	private boolean acceptCertificates(String ref, Date now, CourseIdentityRow entry) {
		if(ref == null) return true;

		CertificateLight certificate = entry.getCertificate();
		Date nextRecertification = certificate == null ? null : certificate.getNextRecertificationDate();
		boolean valid = nextRecertification == null || nextRecertification.after(now);
		
		return (CourseListController.CERTIFICATES_WITH.equals(ref) && certificate != null && valid)
				|| (CourseListController.CERTIFICATES_WITHOUT.equals(ref) && certificate == null)
				|| (CourseListController.CERTIFICATES_INVALID.equals(ref) && certificate != null && !valid);
	}
	
	private boolean acceptDateRange(DateRange range, CourseIdentityRow row) {
		if(range == null) return true;
		
		Date begin = range.getStart();
		Date end = range.getEnd();
		if(begin == null && end == null) return true;
		
		if(begin != null && end != null) {
			return row.getLifecycleValidFrom() != null && begin.compareTo(row.getLifecycleValidFrom()) <= 0
					&& row.getLifecycleValidTo() != null && end.compareTo(row.getLifecycleValidTo()) >= 0;
		}
		if(begin != null) {
			return row.getLifecycleValidFrom() != null && begin.compareTo(row.getLifecycleValidFrom()) <= 0;
		}
		if( end != null) {
			return row.getLifecycleValidTo() != null && end.compareTo(row.getLifecycleValidTo()) >= 0;
		}
		return false;
	}
	
	private boolean acceptLastVisit(String ref, LocalDateTime now, CourseIdentityRow entry) {
		if(ref == null) return true;
		if(entry.getLastVisit() == null) return false;
		
		LocalDateTime lastVisit = DateUtils.toLocalDateTime(entry.getLastVisit());
		if(AbstractParticipantsListController.VISIT_LESS_1_DAY.equals(ref)) {
			return ChronoUnit.HOURS.between(lastVisit, now) < 24;
		}
		if(AbstractParticipantsListController.VISIT_LESS_1_WEEK.equals(ref)) {
			return ChronoUnit.WEEKS.between(lastVisit, now) < 1;
		}
		if(AbstractParticipantsListController.VISIT_LESS_4_WEEKS.equals(ref)) {
			return ChronoUnit.WEEKS.between(lastVisit, now) < 4;
		}
		if(AbstractParticipantsListController.VISIT_LESS_12_MONTHS.equals(ref)) {
			return ChronoUnit.MONTHS.between(lastVisit, now) < 12;
		}
		if(AbstractParticipantsListController.VISIT_MORE_12_MONTS.equals(ref)) {
			return ChronoUnit.MONTHS.between(lastVisit, now) >= 12;
		}
		return false;
	}
	
	private DateRange getFilterRange(List<FlexiTableFilter> filters) {
		FlexiTableFilter pFilter = FlexiTableFilter.getFilter(filters, CoursesIdentityController.FILTER_PERIOD);
		if (pFilter instanceof FlexiTableDateRangeFilter dateRangeFilter) {
			return dateRangeFilter.getDateRange();
		}
		return null;
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
	
	protected int getIndexOf(EfficiencyStatementEntry entry) {
		List<CourseIdentityRow> rows = getObjects();
		for(int i=0; i<rows.size(); i++) {
			if(rows.get(i).getRepositoryEntryKey().equals(entry.getCourse().getKey())) {
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CourseIdentityRow entry = getObject(row);
		return getValueAt(entry, col);
	}
	
	@Override
	public Object getValueAt(CourseIdentityRow row, int col) {
		return switch(COLS[col]) {
			case mark ->  row.getMarkLink();
			case repoKey -> row.getRepositoryEntryKey(); 
			case repoName -> row.getRepositoryEntryDisplayname();
			case repoExternalId -> row.getRepositoryEntryExternalId();
			case repoExternalRef -> row.getRepositoryEntryExternalRef();
			case technicalType -> row.getRepositoryEntryTechnicalType();
			case lifecycleLabel -> row.getLifecycleLabel();
			case lifecycleSoftkey -> row.getLifecycleSoftKey();
			case lifecycleStart -> row.getLifecycleValidFrom();
			case lifecycleEnd -> row.getLifecycleValidTo();
			case access -> row.getRepositoryEntryStatus();
			case lastVisit -> row.getLastVisit();
			case completion -> row.getCompletionPassed();
			case score -> row.getStatementEntry().getScore();
			case passed -> row.getAssessmentEntryPassed();
			case numberAssessments -> row.getNumberAssessment();
			case certificate -> row.getCertificate();
			case certificateValidity -> row.getCertificategNextRecertificationDate();
			case plannedLectures -> row.getTotalPersonalPlannedLectures();
			case attendedLectures -> row.getTotalAttendedLectures();
			case unauthorizedAbsenceLectures, absentLectures -> row.getTotalAbsentLectures();
			case authorizedAbsenceLectures ->  row.getTotalAuthorizedAbsentLectures();
			case dispensedLectures ->  row.getTotalDispensationLectures();
			case rateWarning, lecturesProgress -> row.getLectureBlockStatistics();
			case rate -> row.getLecturesRequiredRate();
			case lastModification -> row.getStatementEntry().getLastModified();
			case lastUserModified -> row.getStatementEntry().getLastUserModified();
			case lastCoachModified -> row.getStatementEntry().getLastCoachModified();
			case tools -> Boolean.TRUE;
			default -> "ERROR";
		};
	}
	
	@Override
	public void setObjects(List<CourseIdentityRow> objects) {
		this.backupList = objects;
		super.setObjects(objects);
	}
	
	public enum Columns implements FlexiSortableColumnDef {
		repoKey("table.header.course.key"),
		repoName("table.header.course.name"),
		repoExternalId("table.header.course.externalId"),
		repoExternalRef("table.header.course.externalRef"),
		technicalType("table.header.technical.type"),
		lifecycleLabel("table.header.lifecycle.label"),
		lifecycleSoftkey("table.header.lifecycle.softkey"),
		lifecycleStart("table.header.lifecycle.start"),
		lifecycleEnd("table.header.lifecycle.end"),
		access("table.header.course.access"),
		lastVisit("table.header.last.visit"),
		completion("table.header.learning.progress"),
		score("table.header.score"),
		passed("table.header.passed"),
		certificate("table.header.certificate"),
		certificateValidity("table.header.certificate.validity"),
		numberAssessments("table.header.number.assessments"),
		lastModification("table.header.lastScoreDate"),
		lastUserModified("table.header.lastUserModificationDate"),
		lastCoachModified("table.header.lastCoachModificationDate"),
		lecturesProgress("table.header.lecture.progress"),
		plannedLectures("table.header.number.planned.lectures"),
		attendedLectures("table.header.number.attended.lectures"),
		absentLectures("table.header.number.absent.lectures"),
		unauthorizedAbsenceLectures("table.header.number.unauthorized.absence"),
		authorizedAbsenceLectures("table.header.number.authorized.absence"),
		dispensedLectures("table.header.number.dispensation"),
		rateWarning("table.header.rate.warning"),
		rate("table.header.rate"),
		mark("table.header.mark"),
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
			return null;
		}
	}
	
	public static record CompletionPassedImpl(Double completion, Boolean passed)
	implements CompletionPassed {

		@Override
		public Double getCompletion() {
			return completion;
		}
		
		@Override
		public Boolean getPassed() {
			return passed;
		}
	}
}
