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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.choice.Choice;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFilterTabsElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.RemoveFiltersEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings.CalloutOrientation;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 12 juil. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiFiltersElementImpl extends FormItemImpl implements FormItemCollection, ControllerEventListener, ComponentEventListener {

	private final WindowControl wControl;
	private final DropdownItem moreMenu;
	private final FormLink addFiltersButton;
	private final FormLink resetFiltersButton;
	private final FormLink collpaseFiltersButton;
	private final FormLink saveFilterLink;
	private final FormLink updateFilterLink;
	private final FormLink deleteFilterLink;
	private final FlexiFiltersComponent component;
	private final List<FlexiFilterButton> filterButtons = new ArrayList<>();
	private Map<String,FormItem> components = new HashMap<>();
	private final FlexiTableElementImpl tableEl;

	private Controller filterCtrl;
	private CloseableModalController cmc;
	private SaveFilterController saveCtrl;
	private UpdateFilterController updateCtrl;
	private ConfirmDeleteFilterController deleteCtrl;
	private CloseableCalloutWindowController filtersCallout;
	private CloseableCalloutWindowController addFiltersCallout;
	
	private int count = 0;
	private boolean alwaysExpanded;
	private boolean expanded = false;
	private boolean customPresets = true;
	
	public FlexiFiltersElementImpl(WindowControl wControl, String name, FlexiTableElementImpl tableEl, Translator translator) {
		super(name);
		this.wControl = wControl;
		this.tableEl = tableEl;
		setTranslator(translator);
		component = new FlexiFiltersComponent(this, translator);
		component.setDomReplacementWrapperRequired(false);
		
		String dispatchId = component.getDispatchID();
		
		addFiltersButton = new FormLinkImpl(dispatchId.concat("_addFiltersButton"), "rAddFiltersButton", "add.filters", Link.BUTTON);
		addFiltersButton.setElementCssClass("o_table_add_filter");
		addFiltersButton.setDomReplacementWrapperRequired(false);
		addFiltersButton.setIconRightCSS("o_icon o_icon-fw o_icon_caret");
		addFiltersButton.setTranslator(translator);
		components.put("rAddFiltersDropDown", addFiltersButton);
		
		resetFiltersButton = new FormLinkImpl(dispatchId.concat("_resetFiltersButton"), "rResetFiltersButton", "reset.filters", Link.BUTTON);
		resetFiltersButton.setDomReplacementWrapperRequired(false);
		resetFiltersButton.setTranslator(translator);
		resetFiltersButton.setGhost(true);
		components.put("rResetFiltersDropDown", resetFiltersButton);
		
		collpaseFiltersButton = new FormLinkImpl(dispatchId.concat("_collapseFiltersButton"), "rCollapseFiltersButton", "filters.collapse", Link.BUTTON);
		collpaseFiltersButton.setElementCssClass("o_button_details");
		collpaseFiltersButton.setDomReplacementWrapperRequired(false);
		collpaseFiltersButton.setIconLeftCSS("o_icon o_icon-fw o_icon_details_expand");
		collpaseFiltersButton.setTitle("filters.collapse");
		collpaseFiltersButton.setTranslator(translator);
		components.put("rCollpaseFiltersButton", collpaseFiltersButton);
		
		saveFilterLink = new FormLinkImpl(dispatchId.concat("_saveFilterLink"), "rSaveFilterLink", "custom.filter.save", Link.LINK);
		saveFilterLink.setDomReplacementWrapperRequired(false);
		saveFilterLink.setIconLeftCSS("o_icon o_icon-fw o_icon_save");
		saveFilterLink.setTranslator(translator);
		components.put("rSaveFilterLink", saveFilterLink);
		
		updateFilterLink = new FormLinkImpl(dispatchId.concat("_updateFilterLink"), "rUpdateFilterLink", "custom.filter.update", Link.LINK);
		updateFilterLink.setDomReplacementWrapperRequired(false);
		updateFilterLink.setIconLeftCSS("o_icon o_icon-fw o_icon_update");
		updateFilterLink.setTranslator(translator);
		components.put("rUpdateFilterLink", updateFilterLink);
		
		deleteFilterLink = new FormLinkImpl(dispatchId.concat("_deleteFilterLink"), "rDeleteFilterLink", "custom.filter.delete", Link.LINK);
		deleteFilterLink.setDomReplacementWrapperRequired(false);
		deleteFilterLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
		deleteFilterLink.setTranslator(translator);
		components.put("rDeleteFilterLink", deleteFilterLink);
		
		moreMenu = new DropdownItem(dispatchId.concat("_moreMenu"), "", component.getTranslator());
		moreMenu.setCarretIconCSS("o_icon o_icon_commands");
		moreMenu.setOrientation(DropdownOrientation.right);
		moreMenu.setExpandContentHeight(true);
		moreMenu.setEmbbeded(true);
		moreMenu.setButton(true);
	}

	public boolean isExpanded() {
		return expanded || alwaysExpanded;
	}
	
	public boolean isLargeSearch() {
		return tableEl.isSearchEnabled() && tableEl.isSearchLarge();
	}

	public void expand(boolean expand) {
		this.expanded = expand;
		if(this.expanded) {
			collpaseFiltersButton.setIconLeftCSS("o_icon o_icon-fw o_icon_details_collaps");
			collpaseFiltersButton.setI18nKey("filters.collapse");
			collpaseFiltersButton.setTitle("filters.collapse");

		} else {
			collpaseFiltersButton.setIconLeftCSS("o_icon o_icon-fw o_icon_details_expand");
			collpaseFiltersButton.setI18nKey("filters.expand");
			collpaseFiltersButton.setTitle("filters.expand");
		}
		component.setDirty(true);
	}

	public boolean isAlwaysExpanded() {
		return alwaysExpanded;
	}

	public void setAlwaysExpanded(boolean alwaysExpanded) {
		this.alwaysExpanded = alwaysExpanded;
		component.setDirty(true);
	}

	public boolean isCustomPresets() {
		return customPresets;
	}

	public void setCustomPresets(boolean customPresets) {
		this.customPresets = customPresets;
	}

	public boolean isTabsEnabled() {
		FlexiFilterTabsElementImpl tabsEl = tableEl.getFilterTabsElement();
		return tabsEl != null && tabsEl.isVisible();
	}

	protected FormLink getAddFiltersButton() {
		return addFiltersButton;
	}
	
	protected FormLink getResetFiltersButton() {
		return resetFiltersButton;
	}
	
	protected FormLink getCollpaseFiltersButton() {
		return collpaseFiltersButton;
	}
	
	protected DropdownItem getMoreMenu() {
		if(moreMenu.size() == 0) {
			moreMenu.addElement(saveFilterLink);
			moreMenu.addElement(deleteFilterLink);
			moreMenu.addElement(updateFilterLink);
		}
		if(tableEl.getFilterTabsElement() != null) {
			saveFilterLink.setVisible(customPresets);
			FlexiFiltersTab selectTab = tableEl.getFilterTabsElement().getSelectedTab();
			boolean show = selectTab != null && selectTab.getId().startsWith("custom_") && customPresets;
			deleteFilterLink.setVisible(show);
			updateFilterLink.setVisible(show);
			moreMenu.setVisible(customPresets);
		} else {
			moreMenu.setVisible(false);
		}
		return moreMenu;
	}
	
	protected void setDirtySubComponents(boolean dirty) {
		addFiltersButton.getComponent().setDirty(dirty);
		saveFilterLink.getComponent().setDirty(dirty);
		deleteFilterLink.getComponent().setDirty(dirty);
		updateFilterLink.getComponent().setDirty(dirty);
		resetFiltersButton.getComponent().setDirty(dirty);
		collpaseFiltersButton.getComponent().setDirty(dirty);
		moreMenu.getComponent().setDirty(dirty);
	}
	
	public List<FlexiFilterButton> getFiltersButtons() {
		for(FlexiFilterButton filterButton:filterButtons) {
			boolean visible = filterButton.isEnabled() && !filterButton.isImplicit();
			filterButton.getButton().setVisible(visible);
		}
		return filterButtons;
	}
	
	public List<FlexiTableFilter> getSelectedFilters() {
		List<FlexiTableFilter> selectedFilters = new ArrayList<>();
		for(FlexiFilterButton filterItem:filterButtons) {
			FlexiTableExtendedFilter filter = filterItem.getFilter();
			if(filter.isSelected()) {
				selectedFilters.add((FlexiTableFilter)filter);
			}
		}
		return selectedFilters;
	}
	
	public List<FlexiTableFilter> getFilters() {
		List<FlexiTableFilter> selectedFilters = new ArrayList<>();
		for(FlexiFilterButton filterItem:filterButtons) {
			selectedFilters.add((FlexiTableFilter)filterItem.getFilter());
		}
		return selectedFilters;
	}
	
	public List<FlexiTableFilter> getEnabledFilters() {
		List<FlexiTableFilter> selectedFilters = new ArrayList<>();
		for(FlexiFilterButton filterButton:filterButtons) {
			if(filterButton.isEnabled()) {
				selectedFilters.add((FlexiTableFilter)filterButton.getFilter());
			}
		}
		return selectedFilters;
	}
	
	public List<FlexiTableFilter> getAllFilters() {
		List<FlexiTableFilter> selectedFilters = new ArrayList<>(filterButtons.size());
		for(FlexiFilterButton filterButton:filterButtons) {
			selectedFilters.add((FlexiTableFilter)filterButton.getFilter());
		}
		return selectedFilters;
	}
	
	protected boolean hasFilterChanges() {
		FlexiFiltersElementImpl filtersEl = tableEl.getFiltersElement();
		if(filtersEl == null) {
			return false;
		}
		
		List<FlexiFilterButton> filterButtons = filtersEl.getFiltersButtons();
		for(FlexiFilterButton filterButton:filterButtons) {
			if(!filterButton.isImplicit() && filterButton.isChanged()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Save the current filters settings to a preset.
	 * 
	 * @param preset The target to save the settings to
	 * @param implicit true to save implicit list too
	 */
	public void saveCurrentSettingsTo(FlexiFiltersTabImpl preset, boolean implicit) {
		List<String> enabledFilters = new ArrayList<>();
		List<String> implicitFilters = new ArrayList<>();
		List<FlexiTableFilterValue> filterValues = new ArrayList<>();
		
		for(FlexiFilterButton filterButton:getFiltersButtons()) {
			filterButton.setChanged(false);
			
			FlexiTableExtendedFilter filter = filterButton.getFilter();
			if(filterButton.isEnabled()) {
				enabledFilters.add(filter.getFilter());
			}
			if(implicit && filterButton.isImplicit()) {
				implicitFilters.add(filter.getFilter());
			}
			
			Serializable val;
			if(filter instanceof FlexiTableMultiSelectionFilter) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				val = values == null ? null : new ArrayList<>(values);
			} else {
				val = filter.getValue();
			}
			filterValues.add(new FlexiTableFilterValue(filter.getFilter(), val));
		}
		
		preset.setImplicitFilters(implicitFilters);
		preset.setEnabledFilters(enabledFilters);
		preset.setDefaultFiltersValues(filterValues);
	}
	
	public void setFilters(List<FlexiTableExtendedFilter> filters) {
		filterButtons.clear();
		for(FlexiTableExtendedFilter filter:filters) {
			boolean enabled = filter.isDefaultVisible();
			FlexiFilterButton filterButton = forgeFormLink(filter, enabled);
			if (filter.isSelected()) {
				filterButton.setChanged(true);
			}
			filterButtons.add(filterButton);
		}
		component.setDirty(true);
	}
	
	/**
	 * Flag some filters as implicit.
	 */
	public void setImplicitFilters(List<String> filters) {
		for(FlexiFilterButton filterButton:getFiltersButtons()) {
			filterButton.setImplicit(filters != null && filters.contains(filterButton.getFilter().getFilter()));
		}
	}
	
	private FlexiFilterButton forgeFormLink(FlexiTableExtendedFilter filter, boolean enabled) {
		String dispatchId = component.getDispatchID();
		String id = dispatchId + "_filterButton-" + (count++);

		String label;
		String title;
		if(filter.isSelected()) {
			label = filter.getDecoratedLabel(true);
			title = filter.getDecoratedLabel(false);
		} else {
			label = title = filter.getLabel();
		}
		
		FormLink button = new FormLinkImpl(id, id, label, Link.BUTTON | Link.NONTRANSLATED);
		button.setDomReplacementWrapperRequired(false);
		button.setTranslator(translator);
		button.setIconRightCSS("o_icon o_icon-fw o_icon_caret");
		button.setVisible(enabled);
		button.setTitle(title);
		components.put(id, button);
		rootFormAvailable(button);
		FlexiFilterButton filterButton = new FlexiFilterButton(button, filter, enabled);
		setFilterButtonCssClass(filterButton, filter.isSelected());
		return filterButton;
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		Form form = getRootForm();
		String dispatchuri = form.getRequestParameter("dispatchuri");
		if(addFiltersButton != null
				&& addFiltersButton.getFormDispatchId().equals(dispatchuri)) {
			doOpenAddFilter(ureq, addFiltersButton);
		} else if(resetFiltersButton != null
				&& resetFiltersButton.getFormDispatchId().equals(dispatchuri)) {
			component.fireEvent(ureq, new RemoveFiltersEvent());
		} else if(collpaseFiltersButton != null
				&& collpaseFiltersButton.getFormDispatchId().equals(dispatchuri)) {
			expand(!expanded);
			component.fireEvent(ureq, new ExpandFiltersEvent(expanded));
		} else if(saveFilterLink != null
				&& saveFilterLink.getFormDispatchId().equals(dispatchuri)) {
			doSaveFilter(ureq);
		} else if(updateFilterLink != null
				&& updateFilterLink.getFormDispatchId().equals(dispatchuri)) {
			doUpdateFilter(ureq);
		} else if(deleteFilterLink != null
				&& deleteFilterLink.getFormDispatchId().equals(dispatchuri)) {
			doDeleteFilter(ureq);
		} else {
			for(FlexiFilterButton filterButton:filterButtons) {
				if(filterButton.getButton().getFormDispatchId().equals(dispatchuri)) {
					
					doOpenFilter(ureq, filterButton.getButton(), filterButton.getFilter());
				}
			}
		}
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Component source, Event event) {
		if(source instanceof Choice) {
			if(Choice.EVNT_VALIDATION_OK.equals(event)) {
				Choice visibleColsChoice = (Choice)source;
				setCustomizedFilters(visibleColsChoice);
			} else if(Choice.EVNT_FORM_RESETED.equals(event)) {
				resetCustomizedFilters();
			}
			if(addFiltersCallout != null) {
				addFiltersCallout.deactivate();
				cleanUp();
			}
		}
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if(filterCtrl == source) {
			if(event instanceof ChangeValueEvent) {
				ChangeValueEvent cve = (ChangeValueEvent)event;
				doApplyFilterValue(ureq, cve.getFilter(), cve.getValue());
				filtersCallout.deactivate();
				cleanUp();
			} else if(event == Event.CANCELLED_EVENT) {
				filtersCallout.deactivate();
				cleanUp();
			}
		} else if(saveCtrl == source) {
			if(event == Event.DONE_EVENT) {
				component.fireEvent(ureq, new SaveCurrentPresetEvent(saveCtrl.getName()));
			}
			cmc.deactivate();
			cleanUp();
		} else if(updateCtrl == source) {
			if(event == Event.DONE_EVENT) {
				component.fireEvent(ureq, new UpdateCurrentPresetEvent());
			}
			cmc.deactivate();
			cleanUp();
		}  else if(deleteCtrl == source) {
			if(event == Event.DONE_EVENT) {
				component.fireEvent(ureq, new DeleteCurrentPresetEvent());
			}
			cmc.deactivate();
			cleanUp();
		} else if(filtersCallout == source) {
			cleanUp();
		} else if(addFiltersCallout == source) {
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		cmc = cleanUp(cmc);
		saveCtrl = cleanUp(saveCtrl);
		filterCtrl = cleanUp(filterCtrl);
		filtersCallout = cleanUp(filtersCallout);
		addFiltersCallout = cleanUp(addFiltersCallout);
	}
	
	private <T extends Controller> T cleanUp(T ctrl) {
		if(ctrl != null) {
			ctrl.removeControllerListener(this);
			ctrl = null;
		}
		return ctrl;
	}

	@Override
	public Iterable<FormItem> getFormItems() {
		return new ArrayList<>(components.values());
	}

	@Override
	public FormItem getFormComponent(String name) {
		return components.get(name);
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
		rootFormAvailable(addFiltersButton);
		rootFormAvailable(resetFiltersButton);
		rootFormAvailable(collpaseFiltersButton);
		
		rootFormAvailable(moreMenu);
		rootFormAvailable(saveFilterLink);
		rootFormAvailable(updateFilterLink);
		rootFormAvailable(deleteFilterLink);
		for(FormItem item:getFormItems()) {
			rootFormAvailable(item);
		}
	}
	
	private final void rootFormAvailable(FormItem item) {
		if(item != null && item.getRootForm() != getRootForm()) {
			item.setRootForm(getRootForm());
		}
	}

	@Override
	public void reset() {
		//
	}
	
	public void setFiltersValues(List<String> enabledFilters, List<String> implicitFilters, List<FlexiTableFilterValue> values, boolean reset) {
		for(FlexiFilterButton filterButton:filterButtons) {
			filterButton.setChanged(false);
			FlexiTableExtendedFilter filter = filterButton.getFilter();
			boolean implicit = (implicitFilters != null && implicitFilters.contains(filterButton.getFilter().getFilter()));
			filterButton.setImplicit(implicit);
			if(enabledFilters != null) {
				filterButton.setEnabled(enabledFilters.contains(filter.getFilter()));
			}
			
			boolean resetFilter = reset;
			if(values != null) {
				for(FlexiTableFilterValue value:values) {
					if(value.getFilter().equals(filter.getFilter())) {
						filter.setValue(value.getValue());
						decorateFilterButton(filter, filterButton);
						resetFilter = false;
					}
				}
			}
			
			if(resetFilter) {
				filter.reset();
				filterButton.getButton().getComponent().setCustomDisplayText(filter.getLabel());
				setFilterButtonCssClass(filterButton, filter.isSelected());
			}
		}

		component.setDirty(true);
	}
	
	private void doOpenFilter(UserRequest ureq, FormItem button, FlexiTableExtendedFilter filter) {
		filterCtrl = filter.getController(ureq, wControl, component.getTranslator());
		filterCtrl.addControllerListener(this);

		filtersCallout = new CloseableCalloutWindowController(ureq, wControl, filterCtrl.getInitialComponent(),
				button.getFormDispatchId(), "", true, "", new CalloutSettings(false, CalloutOrientation.bottom, true, null));
		filtersCallout.addControllerListener(this);
		filtersCallout.activate();
	}
	
	private void doApplyFilterValue(UserRequest ureq, FlexiTableExtendedFilter filter, Object value) {
		filter.setValue(value);
		for(FlexiFilterButton filterButton:filterButtons) {
			if(filterButton.getFilter() == filter) {
				filterButton.setChanged(true);
				decorateFilterButton(filter, filterButton);
			}
		}
		component.fireEvent(ureq, new ChangeFilterEvent(filter));
	}
	
	private void decorateFilterButton(FlexiTableExtendedFilter filter, FlexiFilterButton filterButton) {
		String label;
		String title;
		if(filter.isSelected()) {
			label = filter.getDecoratedLabel(true);
			title = filter.getDecoratedLabel(false);
		} else {
			label = title = filter.getLabel();
		}
		filterButton.getButton().getComponent().setCustomDisplayText(label);
		filterButton.getButton().getComponent().setTitle(title);
		setFilterButtonCssClass(filterButton, filter.isSelected());
	}
	
	private void setFilterButtonCssClass(FlexiFilterButton filterButton, boolean active) {
		if(active) {
			filterButton.getButton().getComponent().setElementCssClass("o_table_filter o_filter_active");
		} else {
			filterButton.getButton().getComponent().setElementCssClass("o_table_filter");	
		}
	}
	
	private void doSaveFilter(UserRequest ureq) {
		saveCtrl = new SaveFilterController(ureq, wControl);
		saveCtrl.addControllerListener(this);

		String title = component.getTranslator().translate("custom.filter.save.title");
		cmc = new CloseableModalController(wControl, "close", saveCtrl.getInitialComponent(), true, title, true);
		cmc.activate();
		cmc.addControllerListener(this);
	}
	
	private void doUpdateFilter(UserRequest ureq) {
		updateCtrl = new UpdateFilterController(ureq, wControl);
		updateCtrl.addControllerListener(this);

		String title = component.getTranslator().translate("custom.filter.update.title");
		cmc = new CloseableModalController(wControl, "close", updateCtrl.getInitialComponent(), true, title, true);
		cmc.activate();
		cmc.addControllerListener(this);
	}
	
	private void doDeleteFilter(UserRequest ureq) {
		FlexiFilterTabsElementImpl tabEl = tableEl.getFilterTabsElement();
		if(tabEl != null && tabEl.getSelectedTab() != null) {
			String name = tabEl.getSelectedTab().getLabel();
			deleteCtrl = new ConfirmDeleteFilterController(ureq, wControl, name);
			deleteCtrl.addControllerListener(this);
	
			String title = component.getTranslator().translate("custom.filter.delete.title");
			cmc = new CloseableModalController(wControl, "close", deleteCtrl.getInitialComponent(), true, title, true);
			cmc.activate();
			cmc.addControllerListener(this);
		}
	}
	
	private void doOpenAddFilter(UserRequest ureq, FormLink customButton) {
		Choice choice = getFilterListAndTheirVisibility();
		addFiltersCallout = new CloseableCalloutWindowController(ureq, wControl, choice,
				customButton, null, true, "o_sel_flexi_custom_callout");
		addFiltersCallout.activate();
		addFiltersCallout.addControllerListener(this);
	}
	
	private void setCustomizedFilters(Choice visibleColsChoice) {
		List<Integer> chosenCols = visibleColsChoice.getSelectedRows();
		if(!chosenCols.isEmpty()) {
			VisibleFlexiFiltersModel model = (VisibleFlexiFiltersModel)visibleColsChoice.getModel();
			for(int i=model.getRowCount(); i-->0; ) {
				boolean enabled = chosenCols.contains(Integer.valueOf(i));
				model.getObject(i).setEnabled(enabled);
			}
		}
		component.setDirty(true);
	}
	
	public void resetCustomizedFilters() {
		for(FlexiFilterButton filterButton:filterButtons) {
			FlexiTableExtendedFilter filter = filterButton.getFilter();
			filter.reset();
			String label = filter.getLabel();
			filterButton.getButton().setI18nKey(label);
			filterButton.getButton().setTitle(label);
			filterButton.setEnabled(filter.isDefaultVisible());
			filterButton.getButton().setVisible(filterButton.isEnabled());
			filterButton.setChanged(false);
			setFilterButtonCssClass(filterButton, filter.isSelected());
		}
		component.setDirty(true);
	}
	
	private Choice getFilterListAndTheirVisibility() {
		Choice choice = new Choice("filterchoice", component.getTranslator());
		choice.setModel(new VisibleFlexiFiltersModel(filterButtons));
		choice.addListener(this);
		choice.setEscapeHtml(false);
		choice.setCancelKey("cancel");
		choice.setSubmitKey("save");
		choice.setResetKey("reset");
		choice.setElementCssClass("o_table_config");
		return choice;
	}
}
