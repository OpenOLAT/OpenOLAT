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
package org.olat.group.ui.main;

import java.util.ArrayList;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableSingleSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableTextFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFilterTabPosition;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupStatusEnum;
import org.olat.group.model.BusinessGroupQueryParams;
import org.olat.group.model.StatisticsBusinessGroupRow;
import org.olat.group.ui.main.BusinessGroupListFlexiTableModel.Cols;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupListController extends AbstractBusinessGroupListController {
	
	private FlexiFiltersTab bookmarkTab;
	private FlexiFiltersTab myGroupsTab;
	private FlexiFiltersTab inactiveGroupsTab;
	private FlexiFiltersTab openGroupsTab;
	
	private DefaultFlexiColumnModel leaveCol;
	private DefaultFlexiColumnModel deleteCol;
	private DefaultFlexiColumnModel freePlacesCol;
	private DefaultFlexiColumnModel accessControlLaunchCol;
	
	public BusinessGroupListController(UserRequest ureq, WindowControl wControl, String prefsKey) {
		super(ureq, wControl, "group_list", false, prefsKey, false, null);
	}
	
	public FlexiFiltersTab getBookmarkTab() {
		return bookmarkTab;
	}
	
	public FlexiFiltersTab getMyGroupsTab() {
		return myGroupsTab;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries != null && !entries.isEmpty()) {
			ContextEntry entry = entries.get(0);
			String tabName = entry.getOLATResourceable().getResourceableTypeName();
			if(tableEl.getSelectedFilterTab() == null || !tableEl.getSelectedFilterTab().getId().equals(tabName)) {
				FlexiFiltersTab tab = tableEl.getFilterTabById(tabName);
				if(tab != null) {
					selectFilterTab(ureq, tab);
				} else {
					selectFilterTab(ureq, myGroupsTab);
				}
			} else {
				tableEl.addToHistory(ureq);
			}
		}
	}
	
	@Override
	protected FlexiTableColumnModel initColumnModel() {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		//mark
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.mark));
		//group name
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.name.i18nHeaderKey(), Cols.name.ordinal(), TABLE_ACTION_LAUNCH,
				true, Cols.name.name(), new StaticFlexiCellRenderer(TABLE_ACTION_LAUNCH, new BusinessGroupNameCellRenderer())));
		//id and reference
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.key));
		if(groupModule.isManagedBusinessGroups()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.externalId));
		}
		//description
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.description.i18nHeaderKey(), Cols.description.ordinal(),
				false, null, FlexiColumnModel.ALIGNMENT_LEFT, new TextFlexiCellRenderer(EscapeMode.antisamy)));
		//courses
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.resources.i18nHeaderKey(), Cols.resources.ordinal(),
				true, Cols.resources.name(), FlexiColumnModel.ALIGNMENT_LEFT, new BGResourcesCellRenderer(flc)));
		//access
		freePlacesCol = new DefaultFlexiColumnModel(Cols.freePlaces.i18nHeaderKey(), Cols.freePlaces.ordinal(), TABLE_ACTION_LAUNCH,
				true, Cols.freePlaces.name(), new TextFlexiCellRenderer(EscapeMode.none));
		columnsModel.addFlexiColumnModel(freePlacesCol);
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.accessTypes.i18nHeaderKey(), Cols.accessTypes.ordinal(),
				true, Cols.accessTypes.name(), FlexiColumnModel.ALIGNMENT_LEFT, new BGAccessControlledCellRenderer()));
		//launch dates
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.createionDate.i18nHeaderKey(), Cols.createionDate.ordinal(),
				true, Cols.createionDate.name(), FlexiColumnModel.ALIGNMENT_LEFT, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.firstTime.i18nHeaderKey(), Cols.firstTime.ordinal(),
				true, Cols.firstTime.name(), FlexiColumnModel.ALIGNMENT_LEFT, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.lastTime.i18nHeaderKey(), Cols.lastTime.ordinal(),
				true, Cols.lastTime.name(), FlexiColumnModel.ALIGNMENT_LEFT, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.lastUsage));
		//roles
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.role.i18nHeaderKey(), Cols.role.ordinal(),
				true, Cols.role.name(), FlexiColumnModel.ALIGNMENT_LEFT, new BGRoleCellRenderer(getLocale())));
		
		DefaultFlexiColumnModel tutorColumnModel = new DefaultFlexiColumnModel(false, Cols.tutorsCount);
		tutorColumnModel.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		tutorColumnModel.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(tutorColumnModel);
		
		DefaultFlexiColumnModel participantsColumnModel = new DefaultFlexiColumnModel(false, Cols.participantsCount);
		participantsColumnModel.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		participantsColumnModel.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(participantsColumnModel);
		
		DefaultFlexiColumnModel waitingListColumnModel = new DefaultFlexiColumnModel(false, Cols.waitingListCount);
		waitingListColumnModel.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		waitingListColumnModel.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(waitingListColumnModel);
		
		//actions
		accessControlLaunchCol = new DefaultFlexiColumnModel(Cols.accessControlLaunch);
		columnsModel.addFlexiColumnModel(accessControlLaunchCol);
		
		leaveCol = new DefaultFlexiColumnModel(Cols.allowLeave.i18nHeaderKey(), Cols.allowLeave.ordinal(), TABLE_ACTION_LEAVE,
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("table.header.leave"), TABLE_ACTION_LEAVE), null));
		columnsModel.addFlexiColumnModel(leaveCol);
		
		deleteCol = new DefaultFlexiColumnModel(Cols.allowDelete.i18nHeaderKey(), Cols.allowDelete.ordinal(), TABLE_ACTION_SOFT_DELETE,
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("table.header.delete"), TABLE_ACTION_SOFT_DELETE), null));
		columnsModel.addFlexiColumnModel(deleteCol);
		
		return columnsModel;
	}

	@Override
	protected void initButtons(FormItemContainer formLayout, UserRequest ureq) {
		initButtons(formLayout, ureq, true, false, true, true);
		// no public
	}
	
	@Override
	protected void initFilterTabs() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();

		bookmarkTab = FlexiFiltersTabFactory.tabWithImplicitFilters("Bookmarks", translate("marked.groups"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(BGSearchFilter.MARKED, "marked"),
						FlexiTableFilterValue.valueOf(BGSearchFilter.STATUS, List.of("active", "inactive"))));
		bookmarkTab.setElementCssClass("o_sel_group_bookmarked_groups");
		tabs.add(bookmarkTab);
		
		myGroupsTab = FlexiFiltersTabFactory.tabWithImplicitFilters("MyGroups", translate("my.groups"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(BGSearchFilter.ROLE, "all"),
						FlexiTableFilterValue.valueOf(BGSearchFilter.STATUS, List.of("active"))));
		myGroupsTab.setElementCssClass("o_sel_group_my_groups");
		tabs.add(myGroupsTab);
		
		inactiveGroupsTab = FlexiFiltersTabFactory.tabWithImplicitFilters("MyInactiveGroups", translate("my.inactivegroups"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(BGSearchFilter.ROLE, "all"),
						FlexiTableFilterValue.valueOf(BGSearchFilter.STATUS, List.of("inactive"))));
		inactiveGroupsTab.setElementCssClass("o_sel_group_inactive_groups");
		tabs.add(inactiveGroupsTab);
		
		openGroupsTab = FlexiFiltersTabFactory.tabWithImplicitFilters("OpenGroups", translate("open.groups"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(BGSearchFilter.OPEN, "yes"),
						FlexiTableFilterValue.valueOf(BGSearchFilter.ROLE, "none"),
						FlexiTableFilterValue.valueOf(BGSearchFilter.STATUS, List.of("active"))));
		openGroupsTab.setElementCssClass("o_sel_group_open_groups");
		tabs.add(openGroupsTab);
		
		FlexiFiltersTab searchTab = FlexiFiltersTabFactory.tab("Search", translate("search.generic"), TabSelectionBehavior.clear);
		searchTab.setElementCssClass("o_sel_group_search_groups");
		searchTab.setPosition(FlexiFilterTabPosition.right);
		searchTab.setLargeSearch(true);
		searchTab.setFiltersExpanded(true);
		tabs.add(searchTab);

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSearchEnabled(true);
		tableEl.setExtendedSearch(null);
	}
	
	@Override
	protected void initFilters() {
		tableEl.setSearchEnabled(true);

		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		// external id
		filters.add(new FlexiTableTextFilter(translate("cif.id"), BGSearchFilter.ID.name(), admin));

		SelectionValues bookmarkValues = new SelectionValues();
		bookmarkValues.add(SelectionValues.entry("mark", translate("cif.bookmarks")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("cif.bookmarks"), BGSearchFilter.MARKED.name(), bookmarkValues, true));
		
		// roles
		SelectionValues roleValues = new SelectionValues();
		if(admin) {
			roleValues.add(SelectionValues.entry("none", translate("search.none")));
		}
		roleValues.add(SelectionValues.entry("all", translate("search.all")));
		roleValues.add(SelectionValues.entry("owner", translate("search.owner")));
		roleValues.add(SelectionValues.entry("attendee", translate("search.attendee")));
		roleValues.add(SelectionValues.entry("waiting", translate("search.waiting")));
		filters.add(new FlexiTableSingleSelectionFilter(translate("search.roles"), BGSearchFilter.ROLE.name(), roleValues, true));
		
		// status
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(BusinessGroupStatusEnum.active.name(), translate("status.active")));
		statusValues.add(SelectionValues.entry(BusinessGroupStatusEnum.inactive.name(), translate("status.inactive")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("search.status"), BGSearchFilter.STATUS.name(), statusValues, true));
		
		// published
		SelectionValues yesNoValues = new SelectionValues();
		yesNoValues.add(SelectionValues.entry("all", translate("search.all")));
		yesNoValues.add(SelectionValues.entry("yes", translate("search.yes")));
		yesNoValues.add(SelectionValues.entry("no", translate("search.no")));
		filters.add(new FlexiTableSingleSelectionFilter(translate("search.open"), BGSearchFilter.OPEN.name(), yesNoValues, true));

		// description
		filters.add(new FlexiTableTextFilter(translate("cif.description"), BGSearchFilter.DESCRIPTION.name(), false));
		// course title
		filters.add(new FlexiTableTextFilter(translate("cif.coursetitle"), BGSearchFilter.COURSETITLE.name(), false));
		
		if(admin) {
			// coaches
			filters.add(new FlexiTableTextFilter(translate("cif.owner"), BGSearchFilter.COACH.name(), false));

			// managed group
			if(managedEnable) {
				filters.add(new FlexiTableSingleSelectionFilter(translate("search.managed"), BGSearchFilter.MANAGED.name(), yesNoValues, true));
			}

			//resources
			filters.add(new FlexiTableSingleSelectionFilter(translate("search.resources"), BGSearchFilter.RESOURCES.name(), yesNoValues, true));
			
			// orphans
			SelectionValues headlessValues = new SelectionValues();
			headlessValues.add(SelectionValues.entry("headless", translate("search.headless")));
			filters.add(new FlexiTableMultiSelectionFilter(translate("search.headless"), BGSearchFilter.HEADLESS.name(), headlessValues, true));
			
			// last visit
			FlexiTableTextFilter lastUsageEl = new FlexiTableTextFilter(translate("search.last.usage"), BGSearchFilter.LASTVISIT.name(), false);
			lastUsageEl.setTextAddOn("search.last.usage.days");
			filters.add(lastUsageEl);	
		}
		
		tableEl.setFilters(true, filters, true, false);
	}

	@Override
	protected BusinessGroupQueryParams getDefaultSearchParams() {
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		params.setTechnicalTypes(List.of(BusinessGroup.BUSINESS_TYPE));
		return params;
	}
	
	@Override
	protected void changeFilterTab(UserRequest ureq, FlexiFiltersTab tab) {
		boolean openTab = (tab == openGroupsTab);
		// special for public groups
		accessControlLaunchCol.setAlwaysVisible(openTab);
		tableEl.setColumnModelVisible(accessControlLaunchCol, openTab);
		freePlacesCol.setAlwaysVisible(openTab);
		tableEl.setColumnModelVisible(freePlacesCol, openTab);
		
		leaveCol.setAlwaysVisible(!openTab);
		tableEl.setColumnModelVisible(leaveCol, !openTab);
		deleteCol.setAlwaysVisible(!openTab);
		tableEl.setColumnModelVisible(deleteCol, !openTab);
	}

	@Override
	protected List<BGTableItem> searchTableItems(BusinessGroupQueryParams params) {
		List<StatisticsBusinessGroupRow> rows = businessGroupService.findBusinessGroupsWithMemberships(params, getIdentity());
		List<BGTableItem> items = new ArrayList<>(rows.size());
		for(StatisticsBusinessGroupRow row:rows) {
			BusinessGroupMembership membership = row.getMember();
			boolean allowLeave =  membership != null;
			boolean allowDelete = isAdmin() || (membership != null && membership.isOwner());
			
			FormLink markLink = uifactory.addFormLink("mark_" + row.getKey(), "mark", "", null, null, Link.NONTRANSLATED);
			markLink.setIconLeftCSS(row.isMarked() ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);

			BGTableItem item = new BGTableItem(row, markLink, allowLeave, allowDelete);
			item.setNumOfOwners(row.getNumOfCoaches());
			item.setNumOfParticipants(row.getNumOfParticipants());
			item.setNumWaiting(row.getNumWaiting());
			item.setNumOfPendings(row.getNumPending());
			addAccessLink(item);
			items.add(item);
		}
		return items;
	}
	
	private void addAccessLink(BGTableItem item) {
		String action;
		BusinessGroupMembership membership = item.getMembership();
		if(membership != null && membership.isOwner()) {
			return;
		} else if(membership != null && (membership.isParticipant() || membership.isWaiting())) {
			action = TABLE_ACTION_LEAVE;
		} else if(item.isFull() && !item.isWaitingListEnabled()) {
			action = null;
		} else {
			action = TABLE_ACTION_ACCESS;
		}
		
		String i18nKey;
		if (membership != null && membership.isParticipant()) {
			i18nKey = "table.header.leave";
		} else if (membership != null && membership.isWaiting()) {
			i18nKey = "table.header.leave.waiting";
		} else if(item.isFull()) {
			if(item.isWaitingListEnabled()) {
				i18nKey = "table.access.waitingList";
			} else {
				i18nKey = "table.header.group.full";
			}
		} else if(item.isWaitingListEnabled()) {
			if(item.isFull()) {
				i18nKey = "table.access.waitingList";
			}	else {
				i18nKey = "table.access";
			}
		} else {
			i18nKey = "table.access";
		}
		
		FormLink accessLink = uifactory.addFormLink("open_" + item.getBusinessGroupKey(), action, i18nKey,
				null, null, Link.LINK);
		accessLink.setElementCssClass("o_sel_group_access");
		if(action == null) {
			accessLink.setEnabled(false);
		}
		accessLink.setUserObject(item);
		item.setAccessLink(accessLink);
	}
	
	@Override
	protected void doLaunch(UserRequest ureq, BusinessGroup group) {	
		if(tableEl.getSelectedFilterTab() != openGroupsTab
				|| businessGroupService.isIdentityInBusinessGroup(getIdentity(), group)) {
			if(isAdmin()) {
				ureq.getUserSession().putEntry("wild_card_" + group.getKey(), Boolean.TRUE);
			}
			super.doLaunch(ureq, group);
		} else if(tableEl.getSelectedFilterTab() == openGroupsTab) {
			String businessPath = "[GroupCard:" + group.getKey() + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} 
	}
}