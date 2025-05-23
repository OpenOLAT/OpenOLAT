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
package org.olat.user.ui.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.admin.user.UserAdminController;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembershipHistory;
import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter.DateRange;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.panel.EmptyPanelItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationNameComparator;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.ui.component.GroupMembershipStatusRenderer;
import org.olat.user.UserManager;
import org.olat.user.ui.admin.UserRoleHistoryTableModel.UserRoleHistoryCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class UserRoleHistoryController extends FormBasicController {

	private FlexiFiltersTab allTab;
	
	protected static final String FILTER_DATE = "date";
	protected static final String FILTER_ROLE = "role";
	protected static final String FILTER_ORGANISATION = "organisation";
	
	private static final String ALL_TAB_ID = "all";
	private static final String LAST_7_DAYS_TAB_ID = "7days";
	private static final String LAST_4_WEEKS_TAB_ID = "4weeks";
	private static final String LAST_12_MONTHS_TAB_ID = "12months";
	
	private FlexiTableElement tableEl;
	private EmptyPanelItem emptyHistoryEl;
	private UserRoleHistoryTableModel tableModel;
	
	private int counter = 0;
	protected final Identity editedIdentity;

	private NoteCalloutController noteCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	protected OrganisationService organisationService;
	
	protected UserRoleHistoryController(UserRequest ureq, WindowControl wControl, Identity editedIdentity) {
		super(ureq, wControl, "user_role_history", Util
				.createPackageTranslator(UserAdminController.class, ureq.getLocale()));
		this.editedIdentity = editedIdentity;
		
		initForm(ureq);
		tableEl.setSelectedFilterTab(ureq, allTab);
		loadModel(true);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		emptyHistoryEl = uifactory.addEmptyPanel("empty.history", null, formLayout);
		emptyHistoryEl.setElementCssClass("o_sel_empty_history");
		emptyHistoryEl.setTitle(translate("membership.no.history.title"));
		emptyHistoryEl.setIconCssClass("o_icon o_icon-lg o_icon_user");
		emptyHistoryEl.setVisible(false);

		initTable(formLayout, ureq);
		initFilters();
		initFiltersPresets();
	}
	
	private void initTable(FormItemContainer formLayout, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, UserRoleHistoryCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserRoleHistoryCols.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserRoleHistoryCols.role,
				new OrganisationRoleCellRenderer(getLocale())));
		if(organisationModule.isEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserRoleHistoryCols.organisation));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserRoleHistoryCols.activity));
		
		GroupMembershipStatusRenderer memberStatusRenderer = new GroupMembershipStatusRenderer(getLocale());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserRoleHistoryCols.previousStatus,
				memberStatusRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserRoleHistoryCols.status,
				memberStatusRenderer));
		DefaultFlexiColumnModel noteCol = new DefaultFlexiColumnModel(UserRoleHistoryCols.note);
		noteCol.setIconHeader("o_icon o_icon_notes");
		columnsModel.addFlexiColumnModel(noteCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserRoleHistoryCols.actor));
		
		tableModel = new UserRoleHistoryTableModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setSearchEnabled(true);
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(UserRoleHistoryCols.creationDate.name(), false));
		tableEl.setSortSettings(sortOptions);
		
		tableEl.setAndLoadPersistedPreferences(ureq, "user-roles-v1");
	}
	
	protected void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		SelectionValues rolesValues = new SelectionValues();
		OrganisationRoles[] roles =  OrganisationRoles.valuesWithoutGuestAndInvitee();
		for(int i=roles.length; i-->0; ) {
			OrganisationRoles role = roles[i];
			rolesValues.add(SelectionValues.entry(role.name(), translate("role." + role.name())));
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.role"), FILTER_ROLE, rolesValues, true));
		
		if(organisationModule.isEnabled()) {
			List<Organisation> organisations = organisationService.getOrganisations();
			if(organisations.size() > 1) {
				Collections.sort(organisations, new OrganisationNameComparator(getLocale()));
			}
			SelectionValues organisationsValues = new SelectionValues();
			for(Organisation organisation:organisations) {
				organisationsValues.add(SelectionValues.entry(organisation.getKey().toString(), StringHelper.escapeHtml(organisation.getDisplayName())));
			}
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.organisations"), FILTER_ORGANISATION, organisationsValues, true));
		}
		
		FlexiTableDateRangeFilter dateFilter = new FlexiTableDateRangeFilter(translate("filter.date"), FILTER_DATE,
				true, true, translate("filter.date.range.label"), translate("filter.date.to"), getLocale());
		filters.add(dateFilter);

		tableEl.setFilters(true, filters, false, false);
	}
	
	protected void initFiltersPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.clear, List.of());
		tabs.add(allTab);

		Date now = DateUtils.getStartOfDay(new Date());
		Date end = DateUtils.getEndOfDay(new Date());
		
		DateRange last7Days = new DateRange();
		last7Days.setStart(DateUtils.addDays(now, -7));
		last7Days.setEnd(end);
		FlexiFiltersTab last7DaysTab = FlexiFiltersTabFactory.tabWithImplicitFilters(LAST_7_DAYS_TAB_ID, translate("filter.last.7.days"),
				TabSelectionBehavior.clear, List.of(
						FlexiTableFilterValue.valueOf(FILTER_DATE, last7Days)));
		tabs.add(last7DaysTab);
		
		DateRange last4Weeks = new DateRange();
		last4Weeks.setStart(DateUtils.addWeeks(now, -4));
		last4Weeks.setEnd(end);
		FlexiFiltersTab last4WeeksTab = FlexiFiltersTabFactory.tabWithImplicitFilters(LAST_4_WEEKS_TAB_ID, translate("filter.last.4.weeks"),
				TabSelectionBehavior.clear, List.of(
						FlexiTableFilterValue.valueOf(FILTER_DATE, last4Weeks)));
		tabs.add(last4WeeksTab);

		DateRange last12Months = new DateRange();
		last12Months.setStart(DateUtils.addMonth(now, -12));
		last12Months.setEnd(end);
		FlexiFiltersTab last12MonthsTab = FlexiFiltersTabFactory.tabWithImplicitFilters(LAST_12_MONTHS_TAB_ID, translate("filter.last.12.months"),
				TabSelectionBehavior.clear, List.of(
						FlexiTableFilterValue.valueOf(FILTER_DATE, last12Months)));
		tabs.add(last12MonthsTab);
		
		tableEl.setFilterTabs(true, tabs);
	}

	protected void reloadModel() {
		loadModel(false);
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(false, false, true);
	}
	
	private void loadModel(boolean reset) {
		List<Organisation> organisations = organisationService.getOrganisations();
		Map<Group,Organisation> groupToOrganisations = getGroupsToOrganisationsMap(organisations);
		
		List<GroupMembershipHistory> membershipsHistory = organisationService.getMembershipHistory(editedIdentity);
		
		List<UserRoleHistoryRow> rows = new ArrayList<>();
		for(GroupMembershipHistory point:membershipsHistory) {
			rows.add(forgeRow(point, groupToOrganisations));
		}
		
		Collections.sort(rows, new UserRoleHistoryRowComparator());
		fillPreviousValue(rows);
		tableModel.setObjects(rows);
		tableEl.reset(reset, reset, true);
	}
	
	private Map<Group,Organisation> getGroupsToOrganisationsMap(List<Organisation> organisations) {
		Map<Group,Organisation> groupToOrganisations = new HashMap<>();
		if(organisations != null && !organisations.isEmpty()) {
			for(Organisation organisation:organisations) {
				groupToOrganisations.put(organisation.getGroup(), organisation);
			}
		}
		return groupToOrganisations;
	}
	
	private void fillPreviousValue(List<UserRoleHistoryRow> rows) {
		Map<HistoryRecord,UserRoleHistoryRow> lastRecords = new HashMap<>();
		for(int i=rows.size(); i-->0; ) {
			UserRoleHistoryRow row = rows.get(i);
			HistoryRecord detailsRecord = new HistoryRecord(row.getGroup(), row.getIdentity().getKey(), row.getRole());
			
			UserRoleHistoryRow previousRow = lastRecords.get(detailsRecord);
			if(previousRow != null) {
				row.setPreviousStatus(previousRow.getStatus());
			}
			lastRecords.put(detailsRecord, row);
		}
	}
	
	private void filterModel() {
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(true, true, true);
	}
	
	private UserRoleHistoryRow forgeRow(GroupMembershipHistory point, Map<Group,Organisation> groupToOrganisations) {
		Identity user = point.getIdentity();
		String userDisplayName = userManager.getUserDisplayName(user);
		Organisation organisation = groupToOrganisations.get(point.getGroup());
		String organisationName = organisation == null ? null : organisation.getDisplayName();
		Long organisationKey = organisation == null ? null : organisation.getKey();
		UserRoleHistoryRow row = new UserRoleHistoryRow(user, userDisplayName,
				organisationName, organisationKey, point);
		row.setActivity(toActivityString(row));
		
		Identity actor = point.getCreator();
		if(actor != null) {
			String actorDisplayName = userManager.getUserDisplayName(actor);
			row.setActorDisplayName(actorDisplayName);
		}
		
		if(StringHelper.containsNonWhitespace(point.getAdminNote())) {
			FormLink noteLink = uifactory.addFormLink("note_" + (++counter), "note", "", null, flc, Link.LINK | Link.NONTRANSLATED);
			noteLink.setDomReplacementWrapperRequired(false);
			noteLink.setIconLeftCSS("o_icon o_icon_notes");
			noteLink.setTitle(translate("note"));
			noteLink.setUserObject(row);
			row.setNoteLink(noteLink);
		}
		
		return row;
	}
	
	private String toActivityString(UserRoleHistoryRow row) {
		GroupMembershipStatus status = row.getStatus();
		return switch(status) {
			case active -> translate("activity.active");
			case cancel -> translate("activity.cancelled");
			case cancelWithFee -> translate("activity.cancelled.with.fee");
			case reservation -> translate("activity.reservation");
			default -> translate("membership.".concat(status.name()));
		};
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(noteCtrl == source) {
			calloutCtrl.deactivate();
			cleanUp();
		} else if(calloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(noteCtrl);
		calloutCtrl = null;
		noteCtrl = null;
	}
	
	

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		if(fiSrc instanceof FormLink link && "note".equals(link.getCmd())) {
			// Do nothing
		} else {
			super.propagateDirtinessToContainer(fiSrc, event);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof FlexiTableSearchEvent || event instanceof FlexiTableFilterTabEvent) {
				filterModel();
			}
		} else if(source instanceof FormLink link && "note".equals(link.getCmd())
				&& link.getUserObject() instanceof UserRoleHistoryRow row) {
			doOpenNote(ureq, link, row);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenNote(UserRequest ureq, FormLink link, UserRoleHistoryRow row) {
		StringBuilder sb = Formatter.stripTabsAndReturns(row.getAdminNote());
		String note = sb == null ? "" : sb.toString();
		noteCtrl = new NoteCalloutController(ureq, getWindowControl(), note);
		listenTo(noteCtrl);
		
		String title = translate("note");
		CalloutSettings settings = new CalloutSettings(title);
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				noteCtrl.getInitialComponent(), link.getFormDispatchId(), title, true, "", settings);
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private static class UserRoleHistoryRowComparator implements Comparator<UserRoleHistoryRow> {

		@Override
		public int compare(UserRoleHistoryRow o1, UserRoleHistoryRow o2) {
			Date c1 = o1.getDate();
			Date c2 = o2.getDate();
			return c2.compareTo(c1);
		}
	}
	
	private record HistoryRecord(Group group, Long identityKey, OrganisationRoles role) {

		@Override
		public int hashCode() {
			return ((group == null) ? 324 : group.hashCode())
					+ ((identityKey == null) ? -890 : identityKey.hashCode())
					+ ((role == null) ? 167253 : role.hashCode());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj instanceof HistoryRecord historyRecord) {
				return (group != null && group.equals(historyRecord.group))
						&& (identityKey != null && identityKey.equals(historyRecord.identityKey))
						&& (role != null && role.equals(historyRecord.role));
			}
			return false;
		}
	}
	
	private static class NoteCalloutController extends FormBasicController {

		private final String note;
		
		public NoteCalloutController(UserRequest ureq, WindowControl wControl, String note) {
			super(ureq, wControl, "note");
			this.note = note;
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			if(formLayout instanceof FormLayoutContainer layoutCont) {
				layoutCont.contextPut("note", note);
			}
		}

		@Override
		protected void formCancelled(UserRequest ureq) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
}
