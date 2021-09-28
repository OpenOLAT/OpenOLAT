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
package org.olat.core.gui.components.form.flexible.impl.elements.table.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.choice.Choice;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.VisibleFlexiColumnsModel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 24 août 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiFiltersAndSettingsDialogController extends BasicController {
	
	private Choice choice;
	private final VelocityContainer mainVC;
	
	private SortKey sortKey;
	private Choice customizedColumnsChoice;
	private boolean resetCustomizedColumns = false;
	private List<FilterToValue> filterValues = new ArrayList<>();
	private final FlexiTableElementImpl tableEl;
	
	private Controller filterCtrl;
	private FlexiSortController sortCtrl;
	private final FlexiFiltersAndSettingsController filtersAndSettingsCtrl;
	
	public FlexiFiltersAndSettingsDialogController(UserRequest ureq, WindowControl wControl, FlexiTableElementImpl tableEl) {
		super(ureq, wControl, Util.createPackageTranslator(FlexiTableElementImpl.class, ureq.getLocale()));
		this.tableEl = tableEl;
		
		mainVC = createVelocityContainer("filters_settings_wrapper");
		
		filtersAndSettingsCtrl = new FlexiFiltersAndSettingsController(ureq, wControl, tableEl);
		listenTo(filtersAndSettingsCtrl);
		
		mainVC.put("panel", filtersAndSettingsCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == choice) {
			if(Choice.EVNT_VALIDATION_OK.equals(event)) {
				setCustomizedColumns((Choice)source);
			} else if(Choice.EVNT_FORM_RESETED.equals(event)) {
				resetCustomizedColumns();
			}
			closeSubPanel();
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == filtersAndSettingsCtrl) {
			if(event instanceof FiltersAndSettingsEvent) {
				doSetSettings(ureq, (FiltersAndSettingsEvent)event);
			} else if(event instanceof SelectFilterEvent) {
				doSelectFilter(ureq, ((SelectFilterEvent)event).getFilterButton());
			} else if(event instanceof CustomizeColumnsEvent) {
				doCustomizeColumns();
			} else if(event instanceof CustomizeSortEvent) {
				doCustomizeSort(ureq);
			}
		} else if(source == filterCtrl) {
			if(event instanceof ChangeValueEvent) {
				ChangeValueEvent cve = (ChangeValueEvent)event;
				doApplyFilterValue(cve.getFilter(), cve.getValue());
				closeSubPanel();
			} 
		} else if(source == sortCtrl) {
			if(event instanceof CustomizeSortEvent) {
				sortKey = ((CustomizeSortEvent)event).getSortKey();
				filtersAndSettingsCtrl.updateSort(sortKey);
			}
			closeSubPanel();
		}
	}
	
	private void closeSubPanel() {
		if(choice != null) {
			choice.removeListener(this);
			mainVC.remove("columns");
			choice = null;
		}
		
		removeAsListenerAndDispose(filterCtrl);
		filterCtrl = null;
		mainVC.remove("filter");
		
		this.removeAsListenerAndDispose(sortCtrl);
		sortCtrl = null;
		mainVC.remove("sort");
	}
	
	private void doSetSettings(UserRequest ureq, FiltersAndSettingsEvent event) {
		event.setSortKey(sortKey);
		event.setCustomizedColumns(customizedColumnsChoice);
		event.setResetCustomizedColumns(resetCustomizedColumns);
		
		List<FlexiTableFilterValue> values = new ArrayList<>();
		for(FilterToValue filterValue:filterValues) {
			values.add(new FlexiTableFilterValue(filterValue.getFilter().getFilter(), (Serializable)filterValue.getValue()));
		}
		event.setFilterValues(values);
		fireEvent(ureq, event);
	}
	
	private void doSelectFilter(UserRequest ureq, FlexiFilterButton filterButton) {
		FilterToValue filterToValue = getFilterToValue(filterButton.getFilter());
		if(filterToValue == null) {
			filterCtrl = filterButton.getFilter().getController(ureq, getWindowControl(), tableEl.getComponent().getTranslator());
		} else {
			filterCtrl = filterButton.getFilter().getController(ureq, getWindowControl(), tableEl.getComponent().getTranslator(), filterToValue.getValue());
		}
		listenTo(filterCtrl);
		mainVC.put("filter", filterCtrl.getInitialComponent());
		mainVC.contextPut("filterLabel", filterButton.getFilter().getLabel());
	}
	
	private FilterToValue getFilterToValue(FlexiTableExtendedFilter filter) {
		for(int i=filterValues.size(); i-->0; ) {
			FilterToValue currentValue = filterValues.get(i);
			if(currentValue.getFilter() == filter) {
				return currentValue;
			}
		}
		return null;
	}
	
	private void doApplyFilterValue(FlexiTableExtendedFilter filter, Object value) {
		filtersAndSettingsCtrl.updateLink(filter, value);
		
		for(int i=filterValues.size(); i-->0; ) {
			FilterToValue currentValue = filterValues.get(i);
			if(currentValue.getFilter() == filter) {
				filterValues.set(i, new FilterToValue(filter, value));
				return;
			}
		}
		filterValues.add(new FilterToValue(filter, value));
	}
	
	private void doCustomizeColumns() {
		choice = new Choice("colchoice", getTranslator());
		Set<Integer> enabledColumnIndex = tableEl.getEnabledColumnIndex();
		FlexiTableColumnModel colModel = tableEl.getTableDataModel().getTableColumnModel();
		choice.setModel(new VisibleFlexiColumnsModel(colModel, enabledColumnIndex, tableEl.getTranslator()));
		choice.addListener(this);
		choice.setEscapeHtml(false);
		choice.setCancelKey("cancel");
		choice.setSubmitKey("save");
		choice.setResetKey("reset");
		choice.setElementCssClass("o_table_config");
		
		mainVC.put("columns", choice);
	}
	
	private void doCustomizeSort(UserRequest ureq) {
		SortKey selectedSortKey;
		if(sortKey == null) {
			selectedSortKey = filtersAndSettingsCtrl.getTableOrderBy();
		} else {
			selectedSortKey = sortKey;
		}
		
		sortCtrl = new FlexiSortController(ureq, getWindowControl(), tableEl, selectedSortKey);
		listenTo(sortCtrl);
		
		mainVC.put("sort", sortCtrl.getInitialComponent());
	}
	
	private void setCustomizedColumns(Choice visibleColsChoice) {
		resetCustomizedColumns = false;
		customizedColumnsChoice = visibleColsChoice;
	}
	
	private void resetCustomizedColumns() {
		resetCustomizedColumns = true;
		customizedColumnsChoice = null;
	}
	
	private static class FilterToValue {
		
		private final FlexiTableExtendedFilter filter;
		private final Object value;
		
		public FilterToValue(FlexiTableExtendedFilter filter, Object value) {
			this.filter = filter;
			this.value = value;
		}
		
		public Object getValue() {
			return value;
		}
		
		public FlexiTableExtendedFilter getFilter() {
			return filter;
		}
	}
}
