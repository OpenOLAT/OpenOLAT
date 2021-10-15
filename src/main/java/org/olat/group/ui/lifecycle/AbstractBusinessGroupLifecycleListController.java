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
package org.olat.group.ui.lifecycle;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupStatusEnum;
import org.olat.group.model.BusinessGroupQueryParams;
import org.olat.group.model.BusinessGroupQueryParams.LifecycleSyntheticStatus;
import org.olat.group.model.StatisticsBusinessGroupRow;
import org.olat.group.ui.main.AbstractBusinessGroupListController;
import org.olat.group.ui.main.BGAccessControlledCellRenderer;
import org.olat.group.ui.main.BGResourcesCellRenderer;
import org.olat.group.ui.main.BGTableItem;
import org.olat.group.ui.main.BusinessGroupListFlexiTableModel.Cols;
import org.olat.group.ui.main.BusinessGroupNameCellRenderer;

/**
 * 
 * Initial date: 13 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractBusinessGroupLifecycleListController extends AbstractBusinessGroupListController {

	public AbstractBusinessGroupLifecycleListController(UserRequest ureq, WindowControl wControl, String prefsKey) {
		super(ureq, wControl, "group_list", false, prefsKey, false, null);
	}
	
	@Override
	protected FlexiTableColumnModel initColumnModel() {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		//id and reference
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.key));
		//group name
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.name.i18nHeaderKey(), Cols.name.ordinal(), TABLE_ACTION_LAUNCH,
				true, Cols.name.name(), new StaticFlexiCellRenderer(TABLE_ACTION_LAUNCH, new BusinessGroupNameCellRenderer())));
		
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
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.accessTypes.i18nHeaderKey(), Cols.accessTypes.ordinal(),
				true, Cols.accessTypes.name(), FlexiColumnModel.ALIGNMENT_LEFT, new BGAccessControlledCellRenderer()));
		//launch dates
		DateFlexiCellRenderer dateCellRenderer = new DateFlexiCellRenderer(getLocale());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.createionDate.i18nHeaderKey(), Cols.createionDate.ordinal(),
				true, Cols.createionDate.name(), FlexiColumnModel.ALIGNMENT_LEFT, dateCellRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.firstTime.i18nHeaderKey(), Cols.firstTime.ordinal(),
				true, Cols.firstTime.name(), FlexiColumnModel.ALIGNMENT_LEFT, dateCellRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.lastTime.i18nHeaderKey(), Cols.lastTime.ordinal(),
				true, Cols.lastTime.name(), FlexiColumnModel.ALIGNMENT_LEFT, dateCellRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.lastUsage));
		
		initStatusColumnModel(columnsModel);

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

		return columnsModel;
	}
	
	protected void initStatusColumnModel(FlexiTableColumnModel columnsModel) {
		DefaultFlexiColumnModel statusCol = new DefaultFlexiColumnModel(Cols.status, new BusinessGroupStatusCellRenderer(getTranslator()));
		statusCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(statusCol);
	}

	@Override
	protected void initButtons(FormItemContainer formLayout, UserRequest ureq) {
		initButtons(formLayout, ureq, false, false, false, true);
	}
	
	@Override
	protected void initFilterTabs() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		initFilterTabs(tabs);
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSearchEnabled(true);
		tableEl.setExtendedSearch(null);
	}
	
	protected abstract void initFilterTabs(List<FlexiFiltersTab> tabs);
	
	@Override
	protected void initFilters() {
		tableEl.setSearchEnabled(true);

		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		// external id
		filters.add(new FlexiTableTextFilter(translate("cif.id"), BGSearchFilter.ID.name(), admin));
		
		// life cycle, hidden
		SelectionValues lifecycleValues = new SelectionValues();
		lifecycleValues.add(SelectionValues.entry(LifecycleSyntheticStatus.ACTIVE.name(), LifecycleSyntheticStatus.ACTIVE.name()));
		lifecycleValues.add(SelectionValues.entry(LifecycleSyntheticStatus.ACTIVE_LONG.name(), LifecycleSyntheticStatus.ACTIVE_LONG.name()));
		lifecycleValues.add(SelectionValues.entry(LifecycleSyntheticStatus.ACTIVE_RESPONSE_DELAY.name(), LifecycleSyntheticStatus.ACTIVE_RESPONSE_DELAY.name()));
		lifecycleValues.add(SelectionValues.entry(LifecycleSyntheticStatus.TO_START_INACTIVATE.name(), LifecycleSyntheticStatus.TO_START_INACTIVATE.name()));
		lifecycleValues.add(SelectionValues.entry(LifecycleSyntheticStatus.TO_INACTIVATE.name(), LifecycleSyntheticStatus.TO_INACTIVATE.name()));

		lifecycleValues.add(SelectionValues.entry(LifecycleSyntheticStatus.INACTIVE.name(), LifecycleSyntheticStatus.INACTIVE.name()));
		lifecycleValues.add(SelectionValues.entry(LifecycleSyntheticStatus.INACTIVE_LONG.name(), LifecycleSyntheticStatus.INACTIVE_LONG.name()));
		lifecycleValues.add(SelectionValues.entry(LifecycleSyntheticStatus.INACTIVE_RESPONSE_DELAY.name(), LifecycleSyntheticStatus.INACTIVE_RESPONSE_DELAY.name()));
		lifecycleValues.add(SelectionValues.entry(LifecycleSyntheticStatus.TO_START_SOFT_DELETE.name(), LifecycleSyntheticStatus.TO_START_SOFT_DELETE.name()));
		lifecycleValues.add(SelectionValues.entry(LifecycleSyntheticStatus.TO_SOFT_DELETE.name(), LifecycleSyntheticStatus.TO_SOFT_DELETE.name()));
		
		lifecycleValues.add(SelectionValues.entry(LifecycleSyntheticStatus.SOFT_DELETE.name(), LifecycleSyntheticStatus.SOFT_DELETE.name()));
		lifecycleValues.add(SelectionValues.entry(LifecycleSyntheticStatus.SOFT_DELETE_LONG.name(), LifecycleSyntheticStatus.SOFT_DELETE_LONG.name()));
		lifecycleValues.add(SelectionValues.entry(LifecycleSyntheticStatus.TO_DELETE.name(), LifecycleSyntheticStatus.TO_DELETE.name()));
		
		filters.add(new FlexiTableSingleSelectionFilter(translate("filter.lifecycle.label"), BGSearchFilter.LIFECYCLE.name(), lifecycleValues, true));
		
		
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
		
		tableEl.setFilters(true, filters, false, false);
	}

	@Override
	protected BusinessGroupQueryParams getDefaultSearchParams() {
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		params.setTechnicalTypes(List.of(groupModule.getGroupLifecycleTypes()));
		return params;
	}
	
	@Override
	protected List<BGTableItem> searchTableItems(BusinessGroupQueryParams params) {
		List<StatisticsBusinessGroupRow> rows = businessGroupService.findBusinessGroupsWithMemberships(params, getIdentity());
		List<BGTableItem> items = new ArrayList<>(rows.size());
		for(StatisticsBusinessGroupRow row:rows) {
			BusinessGroupMembership membership = row.getMember();
			boolean allowLeave =  membership != null;
			boolean allowDelete = isAdmin() || (membership != null && membership.isOwner());

			BGTableItem item = new BGTableItem(row, null, allowLeave, allowDelete);
			item.setNumOfOwners(row.getNumOfCoaches());
			item.setNumOfParticipants(row.getNumOfParticipants());
			item.setNumWaiting(row.getNumWaiting());
			item.setNumOfPendings(row.getNumPending());
			
			if(item.getGroupStatus() == BusinessGroupStatusEnum.active) {
				item.setPlannedInactivationDate(businessGroupLifecycleManager.getInactivationDate(row));
			} else if(item.getGroupStatus() == BusinessGroupStatusEnum.inactive) {
				item.setPlannedInactivationDate(businessGroupLifecycleManager.getInactivationDate(row));
				item.setPlannedSoftDeleteDate(businessGroupLifecycleManager.getSoftDeleteDate(row));
			} else if(item.getGroupStatus() == BusinessGroupStatusEnum.trash) {
				item.setPlannedInactivationDate(businessGroupLifecycleManager.getInactivationDate(row));
				item.setPlannedSoftDeleteDate(businessGroupLifecycleManager.getSoftDeleteDate(row));
				item.setPlannedDeletionDate(businessGroupLifecycleManager.getDefinitiveDeleteDate(row));
			}

			items.add(item);
		}
		return items;
	}
}
