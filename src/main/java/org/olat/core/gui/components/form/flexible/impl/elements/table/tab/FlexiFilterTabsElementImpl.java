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
package org.olat.core.gui.components.form.flexible.impl.elements.table.tab;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiFilterButton;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiFiltersElementImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 12 juil. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiFilterTabsElementImpl extends FormItemImpl implements FormItemCollection, ControllerEventListener, ComponentEventListener {
	
	private final FlexiFilterTabsComponent component;
	
	private List<FlexiFiltersTab> tabs;
	private final List<FlexiFilterTabPreset> customTabs = new ArrayList<>();
	private FlexiFiltersTab selectedTab;
	private final FormLink removeFiltersButton;
	private final FlexiTableElementImpl tableEl;
	private final Map<String,FormItem> components;
	
	public FlexiFilterTabsElementImpl(String name, FlexiTableElementImpl tableEl, Translator translator) {
		super(name);
		this.tableEl = tableEl;
		component = new FlexiFilterTabsComponent(this, translator);
		component.setDomReplacementWrapperRequired(false);

		String dispatchId = component.getDispatchID();
		removeFiltersButton = new FormLinkImpl(dispatchId.concat("_rmrFiltersButton"), "rmrFiltersButton", "remove.active.filters", Link.LINK);
		removeFiltersButton.setDomReplacementWrapperRequired(false);
		removeFiltersButton.setIconLeftCSS("o_icon o_icon_delete_item");
		removeFiltersButton.setElementCssClass("o_table_remove_filters");
		removeFiltersButton.setTranslator(translator);
		components = Map.of("rmrFiltersButton", removeFiltersButton);
	}
	
	protected FlexiTableElementImpl getTableEl() {
		return tableEl;
	}
	
	protected boolean isFiltersExpanded() {
		FlexiFiltersElementImpl filtersEl = tableEl.getFiltersElement();
		return filtersEl != null && filtersEl.isEnabled() && filtersEl.isExpanded();
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
	
	protected FormLink getRemoveFiltersButton() {
		return removeFiltersButton;
	}
	
	public List<FlexiFiltersTab> getFilterTabs() {
		return tabs;
	}

	public void setFilterTabs(List<FlexiFiltersTab> tabs) {
		this.tabs = new ArrayList<>(tabs);
	}
	
	public List<FlexiFilterTabPreset> getCustomFilterTabs() {
		return customTabs;
	}
	
	public void addCustomFilterTab(FlexiFilterTabPreset customPreset) {
		customTabs.add(customPreset);
		component.setDirty(true);
	}
	
	public FlexiFiltersTab getSelectedTab() {
		return selectedTab;
	}
	
	public void setSelectedTab(FlexiFiltersTab selectedTab) {
		this.selectedTab = selectedTab;
		component.setDirty(true);
	}
	
	public void removeSelectedTab(FlexiFiltersTab tab) {
		customTabs.remove(tab);
		component.setDirty(true);
	}
	
	public FlexiFiltersTab getFilterTabById(String id) {
		if(id == null) return null;
		
		for(FlexiFilterTabPreset preset:customTabs) {
			if(id.equals(preset.getId())) {
				return preset;
			}
		}
		
		if(tabs != null) {
			for(FlexiFiltersTab tab:tabs) {
				if(id.equals(tab.getId())) {
					return tab;
				}
			}
		}
		return null;
	}
	
	public List<String> getImplicitFiltersOfSelectedTab() {
		if(selectedTab instanceof FlexiFiltersPreset) {
			FlexiFiltersPreset preset = (FlexiFiltersPreset)selectedTab;
			return preset.getImplicitFilters();	
		}
		return null;
	}
	
	@Override
	public void evalFormRequest(UserRequest ureq) {
		Form form = getRootForm();
		String dispatchuri = form.getRequestParameter("dispatchuri");
		
		if(removeFiltersButton != null
				&& removeFiltersButton.getFormDispatchId().equals(dispatchuri)) {
			component.fireEvent(ureq, new RemoveFiltersEvent());
		} else if(getFormDispatchId().equals(dispatchuri)) {
			String selectTabId = form.getRequestParameter("tab");
			if(selectTabId != null) {
				FlexiFiltersTab tab = getTabById(selectTabId);
				if(tab != null) {
					setSelectedTab(tab);
					component.fireEvent(ureq, new SelectFilterTabEvent(tab));
					component.setDirty(true);
				}
			}
		}
	}
	
	private FlexiFiltersTab getTabById(String id) {
		if(!StringHelper.containsNonWhitespace(id)) {
			return null;
		}
		
		for(FlexiFiltersTab tab:tabs) {
			if(tab.getId().equals(id)) {
				return tab;
			}
		}
		
		for(FlexiFiltersTab tab:customTabs) {
			if(tab.getId().equals(id)) {
				return tab;
			}
		}
		return null;
	}
	
	@Override
	public void dispatchEvent(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		//
	}

	@Override
	public void reset() {
		//
	}


	@Override
	protected Component getFormItemComponent() {
		return component;
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
	protected void rootFormAvailable() {
		for(FormItem item:getFormItems()) {
			rootFormAvailable(item);
		}
	}
	
	private final void rootFormAvailable(FormItem item) {
		if(item != null && item.getRootForm() != getRootForm()) {
			item.setRootForm(getRootForm());
		}
	}
	

}
