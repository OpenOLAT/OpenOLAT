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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.model.OrganisationWithParents;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListReceiver;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.modules.coach.model.ParticipantStatisticsEntry;
import org.olat.modules.curriculum.ui.member.ConfirmationByEnum;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 13 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ParticipantsTableDataModel extends DefaultFlexiTableDataModel<ParticipantStatisticsEntry>
	implements SortableFlexiTableDataModel<ParticipantStatisticsEntry>, FilterableFlexiTableModel, ListProvider {
	
	private static final ParticipantCols[] COLS = ParticipantCols.values();
	private static final int MAX_ENTRIES = 10;

	private List<ParticipantStatisticsEntry> backupList;
	
	private final UserManager userManager;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	public ParticipantsTableDataModel(UserManager userManager, List<UserPropertyHandler> userPropertyHandlers,
			FlexiTableColumnModel columnModel) {
		super(columnModel);
		this.userManager = userManager;
		this.userPropertyHandlers = userPropertyHandlers;
	}
	
	@Override
	public int getMaxEntries() {
		return MAX_ENTRIES;
	}
	
	@Override
	public void getResult(String searchValue, ListReceiver receiver) {
		int maxEntries = MAX_ENTRIES;
		boolean hasMore = false;
		searchValue = searchValue.toLowerCase();
		List<ParticipantStatisticsEntry> objs = getObjects();
		for (Iterator<ParticipantStatisticsEntry> it_res = objs.iterator(); (hasMore=it_res.hasNext()) && maxEntries > 0;) {
			ParticipantStatisticsEntry entry = it_res.next();
			if(accept(searchValue, entry)) {
				maxEntries--;
				String key = entry.getIdentityKey().toString();
				String displayText = StringHelper.escapeHtml(userManager.getUserDisplayName(entry, userPropertyHandlers));
				receiver.addEntry(key, null, displayText, CSSHelper.CSS_CLASS_USER);
			}
		}					
		
		if(hasMore){
			receiver.addEntry(".....",".....");
		}		
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(StringHelper.containsNonWhitespace(searchString) || !filters.isEmpty()) {
			final String loweredSearchString = searchString == null || !StringHelper.containsNonWhitespace(searchString)
					? null : searchString.toLowerCase();
			final Set<Long> organisations = getFilteredOrganisations(filters);
			final ConfirmationByEnum confirmedBy = getFilteredConfirmedBy(filters);
			final Boolean notVisited = getFilteredNotVisited(filters);
			final String certificates = getFiltered(filters, AbstractParticipantsListController.FILTER_CERTIFICATES);
			final Boolean withCourses = getFilteredWithCourses(filters);
			final Boolean withoutCourses = getFilteredWithoutCourses(filters);
			final String assessment = getFiltered(filters, AbstractParticipantsListController.FILTER_ASSESSMENT);
			LocalDateTime now = DateUtils.toLocalDateTime(new Date());
			final String lastVisit = getFiltered(filters, AbstractParticipantsListController.FILTER_LAST_VISIT);
			
			List<ParticipantStatisticsEntry> filteredRows = new ArrayList<>(backupList.size());
			for(ParticipantStatisticsEntry row:backupList) {
				boolean accept = accept(loweredSearchString, row)
						&& acceptOrganisations(organisations, row)
						&& accept(confirmedBy, row)
						&& acceptNotVisited(notVisited, row)
						&& acceptCertificates(certificates, row)
						&& acceptWithCourses(withCourses, row)
						&& acceptWithoutCourses(withoutCourses, row)
						&& acceptAssessment(assessment, row)
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
	
	private boolean acceptLastVisit(String ref, LocalDateTime now, ParticipantStatisticsEntry entry) {
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
	
	private boolean acceptAssessment(String ref, ParticipantStatisticsEntry entry) {
		if(ref == null) return true;
		
		if(AbstractParticipantsListController.ASSESSMENT_NONE.equals(ref)) {
			return entry.getSuccessStatus().numPassed() == 0;
		}
		if(AbstractParticipantsListController.ASSESSMENT_PARTIALLY.equals(ref)) {
			return entry.getSuccessStatus().numPassed() > 0
				&& (entry.getSuccessStatus().numFailed() > 0 || entry.getSuccessStatus().numUndefined() > 0);
		}
		if(AbstractParticipantsListController.ASSESSMENT_ALL.equals(ref)) {
			return entry.getSuccessStatus().numPassed() > 0
					&& entry.getSuccessStatus().numFailed() == 0
					&& entry.getSuccessStatus().numUndefined() == 0;
		}
		return false;
	}
	
	private Boolean getFilteredWithCourses(List<FlexiTableFilter> filters) {
    	FlexiTableFilter organisationsFilter = FlexiTableFilter.getFilter(filters, AbstractParticipantsListController.FILTER_WITH_COURSES);
		if(organisationsFilter instanceof FlexiTableExtendedFilter extendedFilter) {
			String filterValue = extendedFilter.getValue();
			if (AbstractParticipantsListController.FILTER_WITH_COURSES.equals(filterValue)) {
				return Boolean.TRUE;
			}
		}
		return null;
	}
	
	private boolean acceptWithCourses(Boolean with, ParticipantStatisticsEntry entry) {
		if(with == null) return true;
		
		return Boolean.TRUE.equals(with) && entry.getEntries().numOfEntries() > 0;
	}

	private Boolean getFilteredWithoutCourses(List<FlexiTableFilter> filters) {
    	FlexiTableFilter organisationsFilter = FlexiTableFilter.getFilter(filters, AbstractParticipantsListController.FILTER_WITHOUT_COURSES);
		if(organisationsFilter instanceof FlexiTableExtendedFilter extendedFilter) {
			String filterValue = extendedFilter.getValue();
			if (AbstractParticipantsListController.FILTER_WITHOUT_COURSES.equals(filterValue)) {
				return Boolean.TRUE;
			}
		}
		return null;
	}
	
	private boolean acceptWithoutCourses(Boolean without, ParticipantStatisticsEntry entry) {
		if(without == null) return true;
		
		return Boolean.TRUE.equals(without) && entry.getEntries().numOfEntries() == 0;
	}
	
	private boolean acceptCertificates(String ref, ParticipantStatisticsEntry entry) {
		if(ref == null) return true;
		
		return (AbstractParticipantsListController.CERTIFICATES_WITH.equals(ref) && entry.getCertificates().numOfCertificates() > 0)
				|| (AbstractParticipantsListController.CERTIFICATES_WITHOUT.equals(ref) && entry.getCertificates().numOfCertificates() == 0)
				|| (AbstractParticipantsListController.CERTIFICATES_INVALID.equals(ref) && entry.getCertificates().numOfCoursesWithInvalidCertificates() > 0);
	}

	private Boolean getFilteredNotVisited(List<FlexiTableFilter> filters) {
    	FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, AbstractParticipantsListController.FILTER_NOT_VISITED);
		if(filter instanceof FlexiTableExtendedFilter extendedFilter) {
			String filterValue = extendedFilter.getValue();
			if (AbstractParticipantsListController.FILTER_NOT_VISITED.equals(filterValue)) {
				return Boolean.TRUE;
			}
		}
		return null;
	}
	
	private boolean acceptNotVisited(Boolean notVisited, ParticipantStatisticsEntry entry) {
		if(notVisited == null) return true;
		
		return (Boolean.TRUE.equals(notVisited) && entry.getEntries().numOfNotVisited() > 0);
	}
	
	private ConfirmationByEnum getFilteredConfirmedBy(List<FlexiTableFilter> filters) {
    	FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, AbstractParticipantsListController.FILTER_TO_BE_CONFIRMED);
		if(filter instanceof FlexiTableExtendedFilter extendedFilter) {
			String filterValue = extendedFilter.getValue();
			if (AbstractParticipantsListController.CONFIRMED_BY_USER.equals(filterValue)) {
				return ConfirmationByEnum.PARTICIPANT;
			}
			if (AbstractParticipantsListController.CONFIRMED_BY_ADMIN.equals(filterValue)) {
				return ConfirmationByEnum.ADMINISTRATIVE_ROLE;
			}
		}
		return null;
    }
	
	private boolean accept(ConfirmationByEnum confirmedBy, ParticipantStatisticsEntry entry) {
		if(confirmedBy == null ) return true;
		
		return (ConfirmationByEnum.ADMINISTRATIVE_ROLE.equals(confirmedBy) && entry.getReservationsConfirmedByAdmin() > 0)
				|| (ConfirmationByEnum.PARTICIPANT.equals(confirmedBy) && entry.getReservationsConfirmedByUser() > 0);
	}
	
	private Set<Long> getFilteredOrganisations(List<FlexiTableFilter> filters) {
    	FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, AbstractParticipantsListController.FILTER_ORGANISATIONS);
		if(filter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				return filterValues.stream()
						.map(Long::valueOf)
						.collect(Collectors.toSet());
			}
		}
		return null;
    }
	
	private boolean acceptOrganisations(Set<Long> organisationsKeys, ParticipantStatisticsEntry entry) {
		if(organisationsKeys == null || organisationsKeys.isEmpty()) return true;
		
		List<OrganisationWithParents> organisations = entry.getOrganisations();
		return organisations == null || organisations.isEmpty()
			? false
			: organisations.stream().anyMatch(org -> organisationsKeys.contains(org.getKey()));
	}
	
	private boolean accept(String searchValue, ParticipantStatisticsEntry entry) {
		if(searchValue == null) return true;
		
		String[] userProperties = entry.getIdentityProps();
		for(int i=userProperties.length; i-->0; ) {
			String userProp = userProperties[i];
			if(userProp != null && userProp.toLowerCase().contains(searchValue)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void sort(SortKey orderBy) {
		super.setObjects(new SortableFlexiTableModelDelegate<>(orderBy, this, null).sort());
	}

	@Override
	public Object getValueAt(int row, int col) {
		ParticipantStatisticsEntry student = getObject(row);
		return getValueAt(student, col);
	}
	
	@Override
	public Object getValueAt(ParticipantStatisticsEntry statisticsEntry, int col) {
		if(col >= 0 && col < COLS.length) {
			return switch(COLS[col]) {
				case status -> statisticsEntry.getOnlineStatus();
				case courses -> statisticsEntry.getEntries().numOfEntries();
				case coursesNotAttended -> statisticsEntry.getEntries().numOfNotVisited();
				case lastVisit -> statisticsEntry.getLastVisit();
				case completion -> statisticsEntry.getAverageCompletion();
				case successStatus -> statisticsEntry.getSuccessStatus();
				case certificates -> statisticsEntry.getCertificates();
				case reservationConfirmedByUser -> statisticsEntry.getReservationsConfirmedByUser();
				case reservationConfirmedByAdmin -> statisticsEntry.getReservationsConfirmedByAdmin();
				case organisations -> statisticsEntry.getOrganisations();
				case tools -> Boolean.TRUE;
				default -> "ERROR";
			};
		}

		int propPos = col - UserListController.USER_PROPS_OFFSET;
		return statisticsEntry.getIdentityProp(propPos);
	}
	
	@Override
	public void setObjects(List<ParticipantStatisticsEntry> objects) {
		this.backupList = objects;
		super.setObjects(objects);
	}
	
	public enum ParticipantCols implements FlexiSortableColumnDef {
		status("table.header.identity.status"),
		courses("table.header.courses"),
		coursesNotAttended("table.header.courses.not.attended"),
		lastVisit("table.header.last.visit"),
		completion("table.header.completion"),
		successStatus("table.header.success.status"),
		certificates("table.header.certificates"),
		reservationConfirmedByUser("table.header.reservation.confirmed.user"),
		reservationConfirmedByAdmin("table.header.reservation.confirmed.admin"),
		organisations("table.header.organisations"),
		tools("action.more");
		
		private final String i18nKey;
		
		private ParticipantCols(String i18nKey) {
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

		public static ParticipantCols getValueAt(int ordinal) {
			if(ordinal >= 0 && ordinal < values().length) {
				return values()[ordinal];
			}
			return null;
		}
	}
}
