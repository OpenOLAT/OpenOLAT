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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFilterTabPreset;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.group.model.BusinessGroupQueryParams.LifecycleSyntheticStatus;

/**
 * 
 * Initial date: 14 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupInactiveListController extends AbstractBusinessGroupLifecycleListController {

	private FlexiFilterTabPreset inactiveTab;
	private FlexiFilterTabPreset inactiveLongTab;
	private FlexiFilterTabPreset responseDelayTab;
	private FlexiFilterTabPreset toSoftDeleteTab;
	
	private DefaultFlexiColumnModel actionColumn;
	
	public BusinessGroupInactiveListController(UserRequest ureq, WindowControl wControl, String prefsKey) {
		super(ureq, wControl, prefsKey);
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
					selectFilterTab(ureq, inactiveTab);
				}
			} else {
				tableEl.addToHistory(ureq);
			}
		} else if(tableEl.getSelectedFilterTab() == null) {
			selectFilterTab(ureq, inactiveTab);
		}
	}
	
	@Override
	protected FlexiTableColumnModel initColumnModel() {
		FlexiTableColumnModel columnsModel = super.initColumnModel();
		boolean withMail = groupModule.getNumberOfDayBeforeDeactivationMail() > 0;
		String action = withMail ? TABLE_ACTION_START_SOFT_DELETE : TABLE_ACTION_SOFT_DELETE;
		String i18nKey = withMail ? "table.header.start.soft.delete" : "table.header.soft.delete";
		actionColumn = new DefaultFlexiColumnModel(i18nKey, translate(i18nKey), action);
		columnsModel.addFlexiColumnModel(actionColumn);
		return columnsModel;
	}
	
	@Override
	protected void initFilterTabs(List<FlexiFiltersTab> tabs) {
		boolean automatic = groupModule.isAutomaticGroupSoftDeleteEnabled();
		boolean withMail = groupModule.getNumberOfDayBeforeSoftDeleteMail() > 0;
		
		// auto + mail:      active, long, responseDelay
		// auto + no mail:   active, long
		// manual + mail:    active, long, to soft delete, response delay
		// manual + no mail: active, long, to soft delete
		
		inactiveTab = FlexiFilterTabPreset.presetWithImplicitFilters("Inactive", translate("admin.groups.inactive.preset"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(BGSearchFilter.LIFECYCLE, LifecycleSyntheticStatus.INACTIVE.name())));
		tabs.add(inactiveTab);
		
		inactiveLongTab = FlexiFilterTabPreset.presetWithImplicitFilters("LongInactive", translate("admin.groups.inactive.long.preset"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(BGSearchFilter.LIFECYCLE, LifecycleSyntheticStatus.INACTIVE_LONG.name())));
		tabs.add(inactiveLongTab);
		
		if(!automatic) {
			String action = withMail ? LifecycleSyntheticStatus.TO_START_SOFT_DELETE.name() : LifecycleSyntheticStatus.TO_SOFT_DELETE.name();
			toSoftDeleteTab = FlexiFilterTabPreset.presetWithImplicitFilters("ToSoftDelete", translate("admin.groups.to.soft.delete.preset"),
					TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(BGSearchFilter.LIFECYCLE, action)));
			tabs.add(toSoftDeleteTab);
		}
		
		if(withMail) {
			responseDelayTab= FlexiFilterTabPreset.presetWithImplicitFilters("SoftDeleteDelay", translate("admin.groups.inactive.response.delay.preset"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(BGSearchFilter.LIFECYCLE, LifecycleSyntheticStatus.INACTIVE_RESPONSE_DELAY.name())));
			tabs.add(responseDelayTab);
		}
	}
	
	@Override
	protected void changeFilterTab(UserRequest ureq, FlexiFiltersTab tab) {
		boolean actionVisible = (tab == toSoftDeleteTab);
		actionColumn.setAlwaysVisible(actionVisible);
		tableEl.setColumnModelVisible(actionColumn, actionVisible);
	}
}
