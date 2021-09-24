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

import org.olat.core.commons.services.mark.Mark;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFilterTabPreset;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupStatusEnum;
import org.olat.group.model.BusinessGroupQueryParams;
import org.olat.group.model.StatisticsBusinessGroupRow;
import org.olat.group.ui.main.BusinessGroupListFlexiTableModel.Cols;

/**
 * 
 * Generic controller to select a business group
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SelectBusinessGroupController extends AbstractBusinessGroupListController {
	
	private FlexiFilterTabPreset bookmarkTab;
	private FlexiFilterTabPreset ownedGroupsTab;
	private FlexiFilterTabPreset courseGroupsTab;

	public SelectBusinessGroupController(UserRequest ureq, WindowControl wControl, BusinessGroupViewFilter filter, Object uobject) {
		super(ureq, wControl, "group_list", false, "sel-search", true, uobject);
		setFilter(filter);
		
		selectFilterTab(ureq, bookmarkTab);
		if(isEmpty()) {
			selectFilterTab(ureq, ownedGroupsTab);
		}
	}

	@Override
	protected boolean canCreateBusinessGroup() {
		return false;
	}
	
	@Override
	protected void initButtons(FormItemContainer formLayout, UserRequest ureq) {
		initButtons(formLayout, ureq, false, true, false);
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

		//stats
		DefaultFlexiColumnModel tutorColumnModel = new DefaultFlexiColumnModel(true, Cols.tutorsCount);
		tutorColumnModel.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		tutorColumnModel.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(tutorColumnModel);
		
		DefaultFlexiColumnModel participantsColumnModel = new DefaultFlexiColumnModel(true, Cols.participantsCount);
		participantsColumnModel.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		participantsColumnModel.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(participantsColumnModel);
		
		DefaultFlexiColumnModel freePlacesColumnModel = new DefaultFlexiColumnModel(true, Cols.freePlaces.i18nHeaderKey(),
				Cols.freePlaces.ordinal(), true, Cols.freePlaces.name(), FlexiColumnModel.ALIGNMENT_LEFT,
				new TextFlexiCellRenderer(EscapeMode.none));
		freePlacesColumnModel.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		freePlacesColumnModel.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(freePlacesColumnModel);
		
		DefaultFlexiColumnModel waitingListColumnModel = new DefaultFlexiColumnModel(true, Cols.waitingListCount);
		waitingListColumnModel.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		waitingListColumnModel.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(waitingListColumnModel);
		
		//actions
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), TABLE_ACTION_SELECT));
		return columnsModel;
	}
	

	@Override
	protected void initFilterTabs() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();

		bookmarkTab = FlexiFilterTabPreset.presetWithImplicitFilters("Bookmarks", translate("marked.groups"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(BGSearchFilter.MARKED, "marked")));
		bookmarkTab.setElementCssClass("o_sel_group_bookmarked_groups");
		tabs.add(bookmarkTab);
		
		ownedGroupsTab = FlexiFilterTabPreset.presetWithImplicitFilters("MyGroups", translate("owned.groups.2"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(BGSearchFilter.ROLE, "all")));
		ownedGroupsTab.setElementCssClass("o_sel_group_groups");
		tabs.add(ownedGroupsTab);
		
		courseGroupsTab = FlexiFilterTabPreset.presetWithImplicitFilters("Courses", translate("course.groups"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(BGSearchFilter.AUTHOR, "conn")));
		courseGroupsTab.setElementCssClass("o_sel_group_courses");
		tabs.add(courseGroupsTab);
		
		FlexiFilterTabPreset searchTab = new FlexiFilterTabPreset("Search", translate("search.generic"), TabSelectionBehavior.clear);
		searchTab.setElementCssClass("o_sel_group_search_groups");
		searchTab.setPosition(FlexiFilterTabPosition.right);
		searchTab.setLargeSearch(true);
		searchTab.setFiltersExpanded(true);
		tabs.add(searchTab);

		tableEl.setFilterTabs(true, tabs);
	}
	
	@Override
	protected void initFilters() {
		tableEl.setSearchEnabled(true);

		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		// external id
		filters.add(new FlexiTableTextFilter(translate("cif.id"), BGSearchFilter.ID.name(), admin));
		// bookmarks
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
		// author connection of groups to courses
		SelectionValues authorValues = new SelectionValues();
		authorValues.add(SelectionValues.entry("conn", translate("course.groups")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("course.groups"), BGSearchFilter.AUTHOR.name(), authorValues, true));

		if(admin) {
			// coaches
			filters.add(new FlexiTableTextFilter(translate("cif.owner"), BGSearchFilter.COACH.name(), false));
			// description
			filters.add(new FlexiTableTextFilter(translate("cif.description"), BGSearchFilter.DESCRIPTION.name(), false));
			// course title
			filters.add(new FlexiTableTextFilter(translate("cif.coursetitle"), BGSearchFilter.COURSETITLE.name(), false));
			
			// published
			SelectionValues yesNoValues = new SelectionValues();
			yesNoValues.add(SelectionValues.entry("all", translate("search.all")));
			yesNoValues.add(SelectionValues.entry("yes", translate("search.yes")));
			yesNoValues.add(SelectionValues.entry("no", translate("search.no")));
			filters.add(new FlexiTableSingleSelectionFilter(translate("search.open"), BGSearchFilter.OPEN.name(), yesNoValues, true));
			//resources
			filters.add(new FlexiTableSingleSelectionFilter(translate("search.resources"), BGSearchFilter.RESOURCES.name(), yesNoValues, true));
			// last visit
			filters.add(new FlexiTableTextFilter(translate("search.last.usage"), BGSearchFilter.LASTVISIT.name(), false));	
		}
		
		tableEl.setFilters(true, filters, true, false);
	}

	@Override
	protected List<BGTableItem> searchTableItems(BusinessGroupQueryParams params) {
		List<StatisticsBusinessGroupRow> rows = businessGroupService.findBusinessGroupsForSelection(params, getIdentity());
		List<BGTableItem> items = new ArrayList<>(rows.size());
		for(StatisticsBusinessGroupRow row:rows) {
			FormLink markLink = uifactory.addFormLink("mark_" + row.getKey(), "mark", "", null, null, Link.NONTRANSLATED);
			markLink.setIconLeftCSS(row.isMarked() ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
			
			BGTableItem item = new BGTableItem(row, markLink, Boolean.FALSE, Boolean.FALSE);
			item.setNumOfOwners(row.getNumOfCoaches());
			item.setNumOfParticipants(row.getNumOfParticipants());
			item.setNumWaiting(row.getNumWaiting());
			item.setNumOfPendings(row.getNumPending());
			items.add(item);
		}
		return items;
	}

	@Override
	protected BusinessGroupQueryParams getDefaultSearchParams() {
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		params.setTechnicalTypes(List.of(BusinessGroup.BUSINESS_TYPE));
		params.setGroupStatus(List.of(BusinessGroupStatusEnum.active));
		return params;
	}
	
	@Override
	protected void changeFilterTab(UserRequest ureq, FlexiFiltersTab tab) {
		//
	}

	protected void updateSearch(UserRequest ureq) {
		doSearch(ureq, (FlexiFiltersTab)null);
	}
}
