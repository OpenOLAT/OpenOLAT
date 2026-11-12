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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter.DateRange;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.modules.coach.CoachingRoles;
import org.olat.modules.coach.manager.CoachingDAO;
import org.olat.modules.coach.model.CourseStatEntry;
import org.olat.modules.coach.model.CoursesStatisticsParams;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
	
	private boolean coursesStatisticsLoaded = false;
	private boolean coursesCompletionLoaded = false;
	private boolean coursesReferencesLoaded = false;
	
	private final Identity identity;
	private final List<CoachingRoles> roles;
	private final List<CoachingRoles> rolesAsOwner;
	private final List<CoachingRoles> rolesAsCoaches;
	private final CurriculumElement element;
	private final CoursesListUIFactory uifactory;
	private final CoursesStatisticsParams coursesStatisticsParams;
	
	@Autowired
	private CoachingDAO coachingDao;
	@Autowired
	private MarkManager markManager;
	
	public CoursesTableDataModel(FlexiTableColumnModel columnsModel, List<CoachingRoles> roles,
			CoursesStatisticsParams coursesStatisticsParams, CurriculumElement element,
			Identity identity,  Locale locale, CoursesListUIFactory controller) {
		super(columnsModel);
		CoreSpringFactory.autowireObject(this);
		this.roles = roles;
		this.locale = locale;
		this.identity = identity;
		this.element = element;
		this.uifactory = controller;
		this.coursesStatisticsParams = coursesStatisticsParams;
		rolesAsOwner = CoachingRoles.intersect(roles, List.of(CoachingRoles.owner));
		rolesAsCoaches = CoachingRoles.intersect(roles, List.of(CoachingRoles.coach, CoachingRoles.mastercoach));
	}
	
	public int getIndexOfObject(CourseStatEntryRow entry) {
		return getObjects().indexOf(entry);
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null && backupList != null) {
			ensureLoaded(orderBy.getKey());
			super.setObjects(new CoursesTableSortDelegate(orderBy, this, locale).sort());
		}
	}

	private void ensureLoaded(String column) {
		if(!coursesStatisticsLoaded && (Columns.participants.name().equals(column) || Columns.participantsVisited.name().equals(column)
				|| Columns.participantsNotVisited.name().equals(column) || Columns.lastVisit.name().equals(column)
				|| Columns.certificates.name().equals(column))) {
			List<CourseStatEntry> entries = backupList.stream().map(CourseStatEntryRow::getEntry).toList();
			loadCoursesStatistics(entries, true, true, false, false, false);
			coursesStatisticsLoaded = true;
		} else if(!coursesCompletionLoaded && (Columns.completion.name().equals(column) || Columns.averageScore.name().equals(column)
				|| Columns.statusPassed.name().equals(column) || Columns.statusNotPassed.name().equals(column)
				|| Columns.successStatus.name().equals(column) || Columns.statusUndefined.name().equals(column))) {
			List<CourseStatEntry> entries = backupList.stream().map(CourseStatEntryRow::getEntry).toList();
			loadCoursesStatistics(entries, false, false, true, true, false);
			coursesCompletionLoaded = true;
		} else if(!coursesReferencesLoaded && (Columns.references.name().equals(column))) {
			List<CourseStatEntry> entries = backupList.stream().map(CourseStatEntryRow::getEntry).toList();
			loadCoursesStatistics(entries, false, false, false, false, true);
			coursesReferencesLoaded = true;
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
			List<String> educationalTypesList = getFilteredList(filters, CourseListController.FILTER_EDUCATIONAL_TYPE);
			Set<String> educationalTypes = educationalTypesList == null ? Set.of() : Set.copyOf(educationalTypesList);
			List<String> resourcesTypesList = getFilteredList(filters, CourseListController.FILTER_RESOURCE_TYPE);
			if(resourcesTypesList != null && resourcesTypesList.contains("others")) {
				resourcesTypesList = new ArrayList<>(resourcesTypesList);
				resourcesTypesList.remove("others");
				resourcesTypesList.addAll(RepositoryHandlerFactory.getInstance().getOtherTypes());
			}
			final Set<String> resourcesTypes = resourcesTypesList == null ? Set.of() : Set.copyOf(resourcesTypesList);
			List<String> coachingRolesList = getFilteredList(filters, CourseListController.FILTER_ROLES);
			final Set<CoachingRoles> coachingRoles = coachingRolesList == null
					? Set.of()
					: coachingRolesList.stream().map(r -> CoachingRoles.valueOf(r)).collect(Collectors.toSet());
			final List<String> curriculumsKeysList = getFilteredList(filters, CourseListController.FILTER_CURRICULUM);
			final Set<String> curriculumsKeys = curriculumsKeysList == null ? Set.of() : Set.copyOf(curriculumsKeysList);
			final LocalDateTime now = DateUtils.toLocalDateTime(new Date());
			final String lastVisit = getFiltered(filters, CourseListController.FILTER_LAST_VISIT);
			final Boolean marked = getFilteredOneClick(filters, CourseListController.FILTER_MARKED);
			final Boolean notVisited = getFilteredOneClick(filters, CourseListController.FILTER_NOT_VISITED);
			final Boolean withParticipants = getFilteredOneClick(filters, CourseListController.FILTER_WITH_PARTICIPANTS);	
			final Boolean withoutParticipants = getFilteredOneClick(filters, CourseListController.FILTER_WITHOUT_PARTICIPANTS);	
			final DateRange executionPeriod = getFilterRange(filters);
			
			if(notVisited != null || withParticipants != null || withoutParticipants != null
					|| StringHelper.containsNonWhitespace(lastVisit)) {
				ensureLoaded(Columns.participants.name());
			}
			if(StringHelper.containsNonWhitespace(certificates)) {
				ensureLoaded(Columns.certificates.name());
			}
			if(assessment != null && !assessment.isEmpty()) {
				ensureLoaded(Columns.statusPassed.name());// Equivalent
			}
			if(curriculumsKeysList != null && !curriculumsKeysList.isEmpty()) {
				ensureLoaded(Columns.references.name());
			}
			
			List<CourseStatEntryRow> filteredRows = new ArrayList<>(backupList.size());
			for(CourseStatEntryRow row:backupList) {
				boolean accept = accept(loweredSearchString, row)
						&& acceptAssessment(assessment, row)
						&& acceptStatus(status, row)
						&& acceptRoles(coachingRoles, row)
						&& acceptResourceType(resourcesTypes, row)
						&& acceptEducationalType(educationalTypes, row)
						&& acceptMarked(marked, row)
						&& acceptNotVisited(notVisited, row)
						&& acceptWithParticipants(withParticipants, row)
						&& acceptWithoutParticipants(withoutParticipants, row)
						&& acceptDateRange(executionPeriod, row)
						&& acceptCertificates(certificates, row)
						&& acceptLastVisit(lastVisit, now, row)
						&& acceptCurriculums(curriculumsKeys, row);
				if(accept) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupList);
		}
	}
	
	private boolean accept(String searchString, CourseStatEntryRow entry) {
		if(searchString == null) return true;
		return StringHelper.searchFuzzy(entry.getDisplayName(), searchString)
				|| StringHelper.searchFuzzy(entry.getExternalRef(), searchString)
				|| StringHelper.searchFuzzy(entry.getExternalId(), searchString);
	}

	private boolean acceptStatus(List<String> refs, CourseStatEntryRow entry) {
		if(refs == null || refs.isEmpty()) return true;
		return refs.stream()
				.anyMatch(ref -> entry.getStatus() != null && ref.equals(entry.getStatus().name()));
	}
	
	private boolean acceptEducationalType(Set<String> types, CourseStatEntryRow entry) {
		if(types == null || types.isEmpty()) return true;
		
		String entryType = entry.getEducationalType() == null ? null : entry.getEducationalType().getKey().toString();
		return entryType != null && types.contains(entryType);
	}

	private boolean acceptRoles(Set<CoachingRoles> roles, CourseStatEntryRow entry) {
		if(roles == null || roles.isEmpty()) return true;
		return roles.contains(entry.getRole());
	}
	
	private boolean acceptResourceType(Set<String> types, CourseStatEntryRow entry) {
		if(types == null || types.isEmpty()) return true;
		return types.contains(entry.getResourceType());
	}
	
	private boolean acceptCurriculums(Set<String> curriculumsKeys, CourseStatEntryRow entry) {
		if(curriculumsKeys == null || curriculumsKeys.isEmpty()) return true;
		
		List<Long> entryKeys = entry.getCurriculumsKeys();
		if(entryKeys == null || entryKeys.isEmpty()) {
			return false;
		}
		for(Long entryKey:entryKeys) {
			if(curriculumsKeys.contains(entryKey.toString())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean acceptAssessment(List<String> refs, CourseStatEntryRow entry) {
		if(refs == null || refs.isEmpty()) return true;
		
		return refs.stream().anyMatch(ref -> (switch(ref) {
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
		}));
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
			return row.getLifecycleStart() != null && begin.compareTo(row.getLifecycleStart()) <= 0
					&& row.getLifecycleEnd() != null && end.compareTo(row.getLifecycleEnd()) >= 0;
		}
		if(begin != null) {
			return row.getLifecycleStart() != null && begin.compareTo(row.getLifecycleStart()) <= 0;
		}
		if( end != null) {
			return row.getLifecycleEnd() != null && end.compareTo(row.getLifecycleEnd()) >= 0;
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
			case key -> row.getKey();
			case mark -> row.getMarkLink();
			case resourceType -> row;
			case technicalType -> row.getTechnicalType();
			case name -> row.getDisplayName();
			case externalId -> row.getExternalId();
			case externalRef -> row.getExternalRef();
			case lifecycleStart -> row.getLifecycleStart();
			case lifecycleEnd -> row.getLifecycleEnd();
			case access -> row.getStatus();
			case participants -> row.getParticipants();
			case participantsVisited -> row.getParticipantsVisited();
			case participantsNotVisited -> row.getParticipantsNotVisited();
			case lastVisit -> row.getLastVisit();
			case completion -> row.isShowStatistics() && row.isCourse() ? row.getAverageCompletion() : null;
			case successStatus -> row.isShowStatistics() && row.isCourse() ? row.getSuccessStatus() : null;
			case statusPassed -> row.isShowStatistics() && row.isCourse() ? row.getStatusPassed() : null;
			case statusNotPassed -> row.isShowStatistics() && row.isCourse() ? row.getStatusNotPassed() : null;
			case statusUndefined -> row.isShowStatistics() && row.isCourse() ? row.getStatusUndefined() : null;
			case averageScore -> row.getAverageScore();
			case certificates -> row.isShowStatistics() && row.isCourse()  && row.getCertificates().numOfCoursesWithCertificates() > 0
					? row.getCertificates().numOfCertificates() : "";
			case tools -> Boolean.TRUE;
			case taxonomyLevels -> row.getTaxonomyLevelsLink();
			case references -> row.getReferencesLink();
			case educationalType -> row.getEducationalType();
			case activity -> row.getMyActivity();
			default -> "ERROR";
		};
	}
	
	@Override
	public void setObjects(List<CourseStatEntryRow> objects) {
		this.backupList = objects;
		super.setObjects(objects);
	}
	
	public void loadModel() {
		if(backupList == null) {
			List<CourseStatEntryRow> list = loadRows();
			setObjects(list);
		}
	}
	
	@Override
	public void preload(int firstRow, int maxResults) {
		loadModel();
		if(maxResults == -1) {
			maxResults = getRowCount() - firstRow;
		}
		preloadRows(firstRow, maxResults);
	}

	private List<CourseStatEntryRow> loadRows() {
		List<Mark> marks = markManager.getMarks(identity, List.of("RepositoryEntry"));
		Set<Long> markedKeys = marks.stream()
				.map(Mark::getOLATResourceable)
				.map(OLATResourceable::getResourceableId)
				.collect(Collectors.toSet());
		
		List<CourseStatEntryRow> rows = new ArrayList<>();
		List<CourseStatEntry> entries = element != null
			? coachingDao.loadCoursesByElement(identity, element, coursesStatisticsParams.runtimeTypes())
			: coachingDao.loadCoursesAndDeduplicateByRole(identity, roles, coursesStatisticsParams.runtimeTypes());
		for(CourseStatEntry entry:entries) {
			RepositoryEntryEducationalType educationalType = uifactory.getEducationalType(entry);
			boolean showStatistics = coursesStatisticsParams.runtimeTypesGroup().loadStatistics();
			rows.add(new CourseStatEntryRow(entry, markedKeys.contains(entry.getRepoKey()), educationalType, showStatistics));
		}
		return rows;
	}
	
	private void preloadRows(int firstResult, int maxResults) {
		List<CourseStatEntryRow> rows = new ArrayList<>(maxResults);
		List<CourseStatEntry> entries = new ArrayList<>(maxResults);
		List<CourseStatEntryRow> entriesRows = getObjects();
		for(int i=firstResult; i<entriesRows.size() && i<(firstResult + maxResults); i++) {
			CourseStatEntryRow backupRow = entriesRows.get(i);
			rows.add(backupRow);
			entries.add(backupRow.getEntry());
		}
		loadCoursesStatistics(entries, true, true, true, true, true); 
		
		uifactory.forgeRows(rows);
	}
	
	private void loadCoursesStatistics(List<CourseStatEntry> entries, boolean withStatistics,
			boolean withCertificates, boolean withStatements, boolean withCompletions, boolean withReferences) {
		withStatistics &= coursesStatisticsParams.withStatistics();
		withCertificates &= coursesStatisticsParams.withCertificates();
		withStatements &= coursesStatisticsParams.withStatements();
		withCompletions &= coursesStatisticsParams.withCompletions();
		withReferences &= coursesStatisticsParams.withReferences();

		if(element != null) {
			loadCoursesStatistics(element, entries, withStatistics, withCertificates, withStatements, withCompletions);
		} else {
			loadCoursesStatistics(rolesAsOwner, entries, withStatistics, withCertificates, withStatements, withCompletions, withReferences);
			loadCoursesStatistics(rolesAsCoaches, entries, withStatistics, withCertificates, withStatements, withCompletions, withReferences);
		}
	}
	
	private void loadCoursesStatistics(CurriculumElement curriculumElement, List<CourseStatEntry> statistics,
			boolean withStatistics, boolean withCertificates, boolean withStatements, boolean withCompletions) {

		Map<Long,CourseStatEntry> mapAs = statistics.stream()
				.collect(Collectors.toMap(CourseStatEntry::getRepoKey, se -> se, (u, _) -> u));
		if(!mapAs.isEmpty()) {
			if(withStatistics || withCertificates) {
				coachingDao.loadCoursesStatistics(curriculumElement, identity, roles, withCertificates, mapAs);
			}
			if(withStatements || withCompletions) {
				coachingDao.loadCoursesStatementsAndCompletions(curriculumElement, identity, roles, withStatements, withCompletions, mapAs);
			}
		}
	}
	
	private void loadCoursesStatistics(List<CoachingRoles> roles, List<CourseStatEntry> statistics,
			boolean withStatistics, boolean withCertificates, boolean withStatements, boolean withCompletions,
			boolean withReferences) {
		if(roles.isEmpty()) return;
		
		Map<Long,CourseStatEntry> mapAs = statistics.stream()
				.filter(se -> roles.contains(se.getRole()))
				.collect(Collectors.toMap(CourseStatEntry::getRepoKey, se -> se, (u, _) -> u));
		if(!mapAs.isEmpty()) {
			if(!coursesStatisticsLoaded && (withStatistics || withCertificates)) {
				coachingDao.loadCoursesStatistics(null, identity, roles, withCertificates, mapAs);
			}
			if(!coursesCompletionLoaded && (withStatements || withCompletions)) {
				coachingDao.loadCoursesStatementsAndCompletions(null, identity, roles, withStatements, withCompletions, mapAs);
			}
			if(!coursesReferencesLoaded && withReferences) {
				coachingDao.loadCoursesReferences(identity, roles, mapAs);
			}
		}
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
		tools("action.more"),
		taxonomyLevels("table.header.num.of.levels"),
		resourceType("table.header.typeimg"),
		references("table.header.references"),
		educationalType("table.header.educational.type"),
		activity(""),
		relevance(""),
		newest("");
		
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
			return this != assessmentTool
					&& this != infos;
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
	
	public interface CoursesListUIFactory {

		RepositoryEntryEducationalType getEducationalType(CourseStatEntry entry);

		void forgeRows(List<CourseStatEntryRow> rows);
		
	}
}
