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
public class BusinessGroupSoftDeleteListController extends AbstractBusinessGroupLifecycleListController {

	private FlexiFiltersTab softDeleteTab;
	private FlexiFiltersTab softDeleteLongTab;
	private FlexiFiltersTab toDeleteTab;
	
	private DefaultFlexiColumnModel actionColumn;
	
	public BusinessGroupSoftDeleteListController(UserRequest ureq, WindowControl wControl, String prefsKey) {
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
					selectFilterTab(ureq, softDeleteTab);
				}
			} else {
				tableEl.addToHistory(ureq);
			}
		} else if(tableEl.getSelectedFilterTab() == null) {
			selectFilterTab(ureq, softDeleteTab);
		}
	}
	
	@Override
	protected void initStatusColumnModel(FlexiTableColumnModel columnsModel) {
		super.initStatusColumnModel(columnsModel);
		DefaultFlexiColumnModel plannedCol = new DefaultFlexiColumnModel(Cols.plannedDeletionDate, new DateFlexiCellRenderer(getLocale()));
		plannedCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(plannedCol);
	}

	@Override
	protected FlexiTableColumnModel initColumnModel() {
		FlexiTableColumnModel columnsModel = super.initColumnModel();
		String i18nKey = "table.header.defintively.delete";
		actionColumn = new DefaultFlexiColumnModel(i18nKey, translate(i18nKey), TABLE_ACTION_DEFINITIVELY_DELETE);
		columnsModel.addFlexiColumnModel(actionColumn);
		return columnsModel;
	}
	
	@Override
	protected void initFilterTabs(List<FlexiFiltersTab> tabs) {
		boolean automatic = groupModule.isAutomaticGroupDefinitivelyDeleteEnabled();
		
		softDeleteTab = FlexiFiltersTabFactory.tabWithImplicitFilters("SoftDeleted", translate("admin.groups.soft.delete.preset"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(BGSearchFilter.LIFECYCLE, LifecycleSyntheticStatus.SOFT_DELETE.name())));
		tabs.add(softDeleteTab);
		
		softDeleteLongTab = FlexiFiltersTabFactory.tabWithImplicitFilters("LongSoftDeleted", translate("admin.groups.soft.delete.long.preset"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(BGSearchFilter.LIFECYCLE, LifecycleSyntheticStatus.SOFT_DELETE_LONG.name())));
		tabs.add(softDeleteLongTab);
		
		if(!automatic) {
			toDeleteTab = FlexiFiltersTabFactory.tabWithImplicitFilters("ToDelete", translate("admin.groups.to.delete.preset"),
					TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(BGSearchFilter.LIFECYCLE, LifecycleSyntheticStatus.TO_DELETE.name())));
			tabs.add(toDeleteTab);
		}
	}
	
	@Override
	protected void changeFilterTab(UserRequest ureq, FlexiFiltersTab tab) {
		boolean actionVisible = (tab == toDeleteTab);
		actionColumn.setAlwaysVisible(actionVisible);
		tableEl.setColumnModelVisible(actionColumn, actionVisible);
	}
	
	@Override
	protected void initButtons(FormItemContainer formLayout, UserRequest ureq) {
		super.initButtons(formLayout, ureq);
		
		definitivelyDeleteButton = uifactory.addFormLink("table.definitively.delete", TABLE_ACTION_DEFINITIVELY_DELETE, "table.definitively.delete", null, formLayout, Link.BUTTON);
		tableEl.addBatchButton(definitivelyDeleteButton);
		
		restoreButton = uifactory.addFormLink("table.restore", TABLE_ACTION_DEFINITIVELY_DELETE, "table.restore", null, formLayout, Link.BUTTON);
		tableEl.addBatchButton(restoreButton);

		inactivateButton = uifactory.addFormLink("table.inactivate", TABLE_ACTION_REACTIVATE, "table.inactivate", null, formLayout, Link.BUTTON);
		tableEl.addBatchButton(inactivateButton);
	}
}
