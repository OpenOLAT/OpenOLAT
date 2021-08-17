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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.choice.Choice;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 12 juil. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiFiltersElementImpl extends FormItemImpl implements FormItemCollection, ControllerEventListener, ComponentEventListener {

	private final WindowControl wControl;
	private final FormLink addFiltersButton;
	private final FormLink resetFiltersButton;
	private final FormLink collpaseFiltersButton;
	private final FlexiFiltersComponent component;
	private final List<FormLink> filterButtons = new ArrayList<>();
	private Map<String,FormItem> components = new HashMap<>();
	
	private Controller filterCtrl;
	private CloseableCalloutWindowController filtersCallout;
	private CloseableCalloutWindowController addFiltersCallout;
	
	private int count = 0;
	private boolean alwaysOn;
	private boolean expanded = true;
	private Set<String> enabledFilters = new HashSet<>();
	
	public FlexiFiltersElementImpl(WindowControl wControl, String name, Translator translator) {
		super(name);
		this.wControl = wControl;
		component = new FlexiFiltersComponent(this, translator);
		
		String dispatchId = component.getDispatchID();
		
		addFiltersButton = new FormLinkImpl(dispatchId.concat("_addFiltersButton"), "rAddFiltersButton", "add.filters", Link.BUTTON);
		addFiltersButton.setDomReplacementWrapperRequired(false);
		addFiltersButton.setIconRightCSS("o_icon o_icon-fw o_icon_caret");
		addFiltersButton.setTranslator(translator);
		components.put("rAddFiltersDropDown", addFiltersButton);
		
		resetFiltersButton = new FormLinkImpl(dispatchId.concat("_resetFiltersButton"), "rResetFiltersButton", "reset.filters", Link.BUTTON);
		resetFiltersButton.setDomReplacementWrapperRequired(false);
		resetFiltersButton.setTranslator(translator);
		components.put("rResetFiltersDropDown", resetFiltersButton);
		
		collpaseFiltersButton = new FormLinkImpl(dispatchId.concat("_collpaseFiltersButton"), "rCollpaseFiltersButton", "collpase.filters", Link.BUTTON);
		collpaseFiltersButton.setElementCssClass("o_button_details");
		collpaseFiltersButton.setDomReplacementWrapperRequired(false);
		collpaseFiltersButton.setIconLeftCSS("o_icon o_icon-fw o_icon_details_collaps");
		collpaseFiltersButton.setTranslator(translator);
		components.put("rCollpaseFiltersButton", collpaseFiltersButton);
	}
	
	public boolean isExpanded() {
		return expanded || alwaysOn;
	}

	public void expand(boolean expanded) {
		this.expanded = expanded;
		if(this.expanded) {
			collpaseFiltersButton.setIconLeftCSS("o_icon o_icon-fw o_icon_details_collaps");
		} else {
			collpaseFiltersButton.setIconLeftCSS("o_icon o_icon-fw o_icon_details_expand");
		}
		component.setDirty(true);
	}

	public boolean isAlwaysOn() {
		return alwaysOn;
	}

	public void setAlwaysOn(boolean alwaysOn) {
		this.alwaysOn = alwaysOn;
		component.setDirty(true);
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
	
	protected List<FormLink> getFiltersButtons() {
		for(FormLink filterButton:filterButtons) {
			FlexiTableExtendedFilter filter = (FlexiTableExtendedFilter)filterButton.getUserObject();
			filterButton.setVisible(enabledFilters.contains(filter.getFilter()) || filter.isAlwaysVisible());
		}
		
		return filterButtons;
	}
	
	public List<FlexiTableFilter> getSelectedFilters() {
		List<FlexiTableFilter> selectedFilters = new ArrayList<>();
		for(FormItem filterItem:filterButtons) {
			FlexiTableExtendedFilter filter = (FlexiTableExtendedFilter)filterItem.getUserObject();
			if(filter.isSelected()) {
				selectedFilters.add((FlexiTableFilter)filter);
			}
		}
		return selectedFilters;
	}
	
	public List<FlexiTableFilter> getVisibleFilters() {
		List<FlexiTableFilter> selectedFilters = new ArrayList<>();
		for(FormItem filterItem:filterButtons) {
			FlexiTableExtendedFilter filter = (FlexiTableExtendedFilter)filterItem.getUserObject();
			if(enabledFilters.contains(filter.getFilter())) {
				selectedFilters.add((FlexiTableFilter)filter);
			}
		}
		return selectedFilters;
	}
	
	public List<FlexiTableFilter> getAllFilters() {
		List<FlexiTableFilter> selectedFilters = new ArrayList<>(filterButtons.size());
		for(FormItem filterItem:filterButtons) {
			selectedFilters.add((FlexiTableFilter)filterItem.getUserObject());
		}
		return selectedFilters;
	}
	
	public void setFilters(List<FlexiTableExtendedFilter> filters) {
		this.filterButtons.clear();
		enabledFilters.clear();
		for(FlexiTableExtendedFilter filter:filters) {
			boolean enabled = filter.isVisible() || filter.isAlwaysVisible();
			if(enabled) {
				enabledFilters.add(filter.getFilter());
			}
			this.filterButtons.add(forgeFormLink(filter, enabled));
		}
		component.setDirty(true);
	}
	
	private FormLink forgeFormLink(FlexiTableExtendedFilter filter, boolean enabled) {
		String dispatchId = component.getDispatchID();
		String id = dispatchId + "_filterButton-" + (count++);
		String label;
		if(filter.isSelected()) {
			label = filter.getDecoratedLabel();
		} else {
			label = filter.getLabel();
		}
		FormLink filterButton = new FormLinkImpl(id, id, label, Link.BUTTON | Link.NONTRANSLATED);
		filterButton.setDomReplacementWrapperRequired(false);
		filterButton.setTranslator(translator);
		filterButton.setIconRightCSS("o_icon o_icon_caret");
		filterButton.setUserObject(filter);
		filterButton.setVisible(enabled);
		components.put(id, filterButton);
		rootFormAvailable(filterButton);
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
			resetCustomizedFilters();
		} else if(collpaseFiltersButton != null
				&& collpaseFiltersButton.getFormDispatchId().equals(dispatchuri)) {
			expand(!expanded);
			component.fireEvent(ureq, new ExpandFiltersEvent(expanded));
		} else {
			for(FormItem filter:filterButtons) {
				if(filter.getFormDispatchId().equals(dispatchuri)) {
					doOpenFilter(ureq, filter, (FlexiTableExtendedFilter)filter.getUserObject());
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
			if(event instanceof ChangeFilterEvent) {
				doFilter(ureq, ((ChangeFilterEvent)event).getFilter());
				filtersCallout.deactivate();
				cleanUp();
			} else if(event == Event.CANCELLED_EVENT) {
				filtersCallout.deactivate();
				cleanUp();
			}
		} else if(filtersCallout == source) {
			cleanUp();
		} else if(addFiltersCallout == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
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
	
	public void setFiltersValues(List<FlexiTableFilterValue> values, boolean reset) {
		for(FormLink filterItem:filterButtons) {
			FlexiTableExtendedFilter filter = (FlexiTableExtendedFilter)filterItem.getUserObject();
			
			boolean resetFilter = reset;
			
			for(FlexiTableFilterValue value:values) {
				if(value.getFilter().equals(filter.getFilter())) {
					filter.setValue(value.getValue());
					if(filter.isSelected()) {
						filterItem.getComponent().setCustomDisplayText(filter.getDecoratedLabel());
					} else {
						filterItem.getComponent().setCustomDisplayText(filter.getLabel());
					}
					
					resetFilter = false;
				}
			}
			
			if(resetFilter) {
				filter.reset();
				filterItem.getComponent().setCustomDisplayText(filter.getLabel());
			}
		}

		component.setDirty(true);
	}
	
	private void doOpenFilter(UserRequest ureq, FormItem button, FlexiTableExtendedFilter filter) {
		filterCtrl = filter.getController(ureq, wControl);
		filterCtrl.addControllerListener(this);
		
		filtersCallout = new CloseableCalloutWindowController(ureq, wControl, filterCtrl.getInitialComponent(),
				button.getFormDispatchId(), "", true, "", new CalloutSettings(false));
		filtersCallout.addControllerListener(this);
		filtersCallout.activate();
	}
	
	private void doFilter(UserRequest ureq, FlexiTableExtendedFilter filter) {
		for(FormLink filterItem:filterButtons) {
			if(filterItem.getUserObject() == filter) {
				if(filter.isSelected()) {
					filterItem.getComponent().setCustomDisplayText(filter.getDecoratedLabel());
				} else {
					filterItem.getComponent().setCustomDisplayText(filter.getLabel());
				}
			}
		}
		component.fireEvent(ureq, new ChangeFilterEvent(filter));
	}
	
	private void doOpenAddFilter(UserRequest ureq, FormLink customButton) {
		Choice choice = getFilterListAndTheirVisibility();
		addFiltersCallout = new CloseableCalloutWindowController(ureq, wControl, choice,
				customButton, "Customize", true, "o_sel_flexi_custom_callout");
		addFiltersCallout.activate();
		addFiltersCallout.addControllerListener(this);
	}
	
	private void setCustomizedFilters(Choice visibleColsChoice) {
		enabledFilters.clear();
		
		List<Integer> chosenCols = visibleColsChoice.getSelectedRows();
		if(!chosenCols.isEmpty()) {
			VisibleFlexiFiltersModel model = (VisibleFlexiFiltersModel)visibleColsChoice.getModel();
			for(Integer chosenCol:chosenCols) {
				enabledFilters.add(model.getObject(chosenCol.intValue()).getFilter());
			}
		}
		
		for(FormItem filterItem:filterButtons) {
			FlexiTableExtendedFilter filter = (FlexiTableExtendedFilter)filterItem.getUserObject();
			boolean enabled = enabledFilters.contains(filter.getFilter()) || filter.isAlwaysVisible();
			if(enabled) {
				enabledFilters.add(filter.getFilter());
			}
			filterItem.setVisible(enabled);
		}
		
		component.setDirty(true);
	}
	
	public void resetCustomizedFilters() {
		enabledFilters.clear();
		
		for(FormItem filterItem:filterButtons) {
			FlexiTableExtendedFilter filter = (FlexiTableExtendedFilter)filterItem.getUserObject();
			filter.reset();
			
			boolean defEnabled = filter.isVisible() || filter.isAlwaysVisible();
			if(defEnabled) {
				enabledFilters.add(filter.getFilter());
			}
			filterItem.setVisible(defEnabled);
		}
		component.setDirty(true);
	}
	
	private Choice getFilterListAndTheirVisibility() {
		Choice choice = new Choice("filterchoice", component.getTranslator());
		List<FlexiTableFilter> allFilters = getAllFilters();
		List<FlexiTableFilter> selectedFilters = getVisibleFilters();
		choice.setModel(new VisibleFlexiFiltersModel(allFilters, selectedFilters));
		choice.addListener(this);
		choice.setEscapeHtml(false);
		choice.setCancelKey("cancel");
		choice.setSubmitKey("save");
		choice.setResetKey("reset");
		choice.setElementCssClass("o_table_config");
		return choice;
	}
}
