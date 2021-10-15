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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.group.model.BusinessGroupQueryParams.LifecycleSyntheticStatus;
import org.olat.group.ui.main.BusinessGroupListFlexiTableModel.Cols;

/**
 * 
 * Initial date: 14 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupActiveListController extends AbstractBusinessGroupLifecycleListController {

	private FlexiFiltersTab activeTab;
	private FlexiFiltersTab notActiveTab;
	private FlexiFiltersTab responseDelayTab;
	private FlexiFiltersTab toInactivateTab;
	
	private DefaultFlexiColumnModel actionColumn;
	
	public BusinessGroupActiveListController(UserRequest ureq, WindowControl wControl, String prefsKey) {
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
					selectFilterTab(ureq, activeTab);
				}
			} else {
				tableEl.addToHistory(ureq);
			}
		} else if(tableEl.getSelectedFilterTab() == null) {
			selectFilterTab(ureq, activeTab);
		}
	}
	
	@Override
	protected void initStatusColumnModel(FlexiTableColumnModel columnsModel) {
		super.initStatusColumnModel(columnsModel);
		DefaultFlexiColumnModel plannedCol = new DefaultFlexiColumnModel(Cols.plannedInactivationDate, new DateFlexiCellRenderer(getLocale()));
		plannedCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(plannedCol);
	}
	
	@Override
	protected FlexiTableColumnModel initColumnModel() {
		FlexiTableColumnModel columnsModel = super.initColumnModel();
		
		boolean withMail = groupModule.getNumberOfDayBeforeDeactivationMail() > 0;
		String action = withMail ? TABLE_ACTION_START_INACTIVATE : TABLE_ACTION_INACTIVATE;
		String i18nKey = withMail ? "table.header.start.inactivate" : "table.header.inactivate";
		actionColumn = new DefaultFlexiColumnModel(i18nKey, translate(i18nKey), action);
		columnsModel.addFlexiColumnModel(actionColumn);
		return columnsModel;
	}

	@Override
	protected void initFilterTabs(List<FlexiFiltersTab> tabs) {
		boolean automaticInactivation = groupModule.isAutomaticGroupInactivationEnabled();
		boolean withMail = groupModule.getNumberOfDayBeforeDeactivationMail() > 0;
		
		// auto + mail:      active, long, responseDelay
		// auto + no mail:   active, long
		// manual + mail:    active, long, to inactivate, response delay
		// manual + no mail: active, long, to inactivate
		
		activeTab = FlexiFiltersTabFactory.tabWithImplicitFilters("Active", translate("admin.groups.active.preset"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(BGSearchFilter.LIFECYCLE, LifecycleSyntheticStatus.ACTIVE.name())));
		tabs.add(activeTab);
		
		notActiveTab = FlexiFiltersTabFactory.tabWithImplicitFilters("NotActive", translate("admin.groups.not.active.preset"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(BGSearchFilter.LIFECYCLE, LifecycleSyntheticStatus.ACTIVE_LONG.name())));
		tabs.add(notActiveTab);

		if(!automaticInactivation) {
			String action = withMail ? LifecycleSyntheticStatus.TO_START_INACTIVATE.name() : LifecycleSyntheticStatus.TO_INACTIVATE.name();
			toInactivateTab = FlexiFiltersTabFactory.tabWithImplicitFilters("ToInactivate", translate("admin.groups.to.inactive.preset"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(BGSearchFilter.LIFECYCLE, action)));
			tabs.add(toInactivateTab);
		}
		
		if(withMail) {
			responseDelayTab= FlexiFiltersTabFactory.tabWithImplicitFilters("ResponseDelay", translate("admin.groups.response.delay.preset"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(BGSearchFilter.LIFECYCLE, LifecycleSyntheticStatus.ACTIVE_RESPONSE_DELAY.name())));
			tabs.add(responseDelayTab);
		}
	}
	
	@Override
	protected void changeFilterTab(UserRequest ureq, FlexiFiltersTab tab) {
		boolean actionVisible = (tab == toInactivateTab);
		actionColumn.setAlwaysVisible(actionVisible);
		tableEl.setColumnModelVisible(actionColumn, actionVisible);
	}
	
	@Override
	protected void initButtons(FormItemContainer formLayout, UserRequest ureq) {
		super.initButtons(formLayout, ureq);

		boolean withMail = groupModule.getNumberOfDayBeforeDeactivationMail() > 0;
		if(withMail && !groupModule.isAutomaticGroupInactivationEnabled()) {
			startInactivateButton = uifactory.addFormLink("table.start.inactivate", TABLE_ACTION_START_INACTIVATE, "table.start.inactivate", null, formLayout, Link.BUTTON);
			tableEl.addBatchButton(startInactivateButton);
		
			cancelInactivateButton = uifactory.addFormLink("table.cancel.inactivate", TABLE_ACTION_CANCEL_INACTIVATE, "table.cancel.inactivate", null, formLayout, Link.BUTTON);
			tableEl.addBatchButton(cancelInactivateButton);
		}
		
		inactivateButton = uifactory.addFormLink("table.inactivate", TABLE_ACTION_INACTIVATE, "table.inactivate", null, formLayout, Link.BUTTON);
		tableEl.addBatchButton(inactivateButton);
		// no public
	}
}
