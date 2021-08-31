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
import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSort;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 23 août 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class FlexiFiltersAndSettingsController extends FormBasicController {
	
	private int count = 0;
	private SingleSelection renderTypeEl;
	private FormLink sortLink;
	private FormLink resetFilterLink;
	private FormLink customizeColumnsLink;
	private final FlexiTableElementImpl tableEl;
	private List<FormLink> filterLinks = new ArrayList<>();
	
	FlexiFiltersAndSettingsController(UserRequest ureq, WindowControl wControl, FlexiTableElementImpl tableEl) {
		super(ureq, wControl, "filters_settings", Util.createPackageTranslator(FlexiTableElementImpl.class, ureq.getLocale()));
		this.tableEl = tableEl;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		resetFilterLink = uifactory.addFormLink("reset.filters", "reset.filters", null, formLayout, Link.BUTTON);
		resetFilterLink.setElementCssClass("o_table_reset_filters");
		resetFilterLink.getComponent().setSuppressDirtyFormWarning(true);
		
		sortLink = uifactory.addFormLink("sort.settings", "sort.settings", null, formLayout, Link.LINK);
		sortLink.setElementCssClass("o_table_sort");
		sortLink.getComponent().setSuppressDirtyFormWarning(true);
		List<FlexiTableSort> sorts = tableEl.getSorts();
		sortLink.setVisible(sorts != null && !sorts.isEmpty());
		updateSort(getTableOrderBy());
		
		FlexiFiltersElementImpl filtersEl = tableEl.getFiltersElement();
		if(filtersEl != null) {
			List<FlexiFilterButton> filtersButtons = filtersEl.getFiltersButtons();
			for(FlexiFilterButton filterButton:filtersButtons) {
				if(filterButton.isImplicit()) {
					continue;
				}
				
				String id = "filter_lnk_" + (count++);
				FlexiTableExtendedFilter filter = filterButton.getFilter();
				String label = filter.isSelected() ? filter.getDecoratedLabel(true) : filter.getLabel();
				FormLink link = uifactory.addFormLink(id, label, null, formLayout, Link.LINK | Link.NONTRANSLATED);
				link.setIconRightCSS("o_icon o_icon-fw o_icon_start");
				link.setUserObject(filterButton);
				filterLinks.add(link);
			}
			
			if(formLayout instanceof FormLayoutContainer) {
				FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
				layoutCont.contextPut("filters", filterLinks);
			}
		}
		
		// customization
		if(tableEl.isCustomizeColumns()) {
			customizeColumnsLink = uifactory.addFormLink("customize.columns", "customize.columns", null, formLayout, Link.LINK);
			customizeColumnsLink.setIconRightCSS("o_icon o_icon-fw o_icon_start");
		}

		// render types
		if(tableEl.getAvailableRendererTypes() != null && tableEl.getAvailableRendererTypes().length > 1) {
			SelectionValues rendererTypes = new SelectionValues();
			for(FlexiTableRendererType type:tableEl.getAvailableRendererTypes()) {
				rendererTypes.add(SelectionValues.entry(type.name(), translate("view." + type.name())));
			}
			
			renderTypeEl = uifactory.addButtonGroupSingleSelectHorizontal("render.types", formLayout, rendererTypes);
			renderTypeEl.setElementCssClass("o_table_render_types");
			
			if(tableEl.getRendererType() != null && rendererTypes.containsKey(tableEl.getRendererType().name())) {
				renderTypeEl.select(tableEl.getRendererType().name(), true);
			} else {
				renderTypeEl.select(tableEl.getAvailableRendererTypes()[0].name(), true);
			}
		}

		uifactory.addFormSubmitButton("settings.done", formLayout);
	}
	
	protected void updateSort(SortKey sortKey) {
		String name = null;
		if(tableEl.getSorts() != null) {
			for(FlexiTableSort sort:tableEl.getSorts()) {
				if(sort.getSortKey().getKey().equals(sortKey.getKey())) {
					name = translate("sort.settings.with", sort.getLabel());
				}
			}
		}
		if(name == null) {
			name = translate("sort.settings");
		}
		sortLink.getComponent().setCustomDisplayText(name);
		
		String sortIcon = null;
		if(sortKey != null && sortKey.isAsc()) {
			sortIcon = "o_icon o_icon_sort_asc";
		} else if(sortKey != null && !sortKey.isAsc()) {
			sortIcon = "o_icon o_icon_sort_desc";
		}
		sortLink.getComponent().setIconRightCSS(sortIcon);
		
	}
	
	protected void updateLink(FlexiTableExtendedFilter filter, Object value) {
		FormLink link = getFilterLinkByFilter(filter);
		if(link != null) {
			String label = value == null ? filter.getLabel() : filter.getDecoratedLabel(value, true);
			link.getComponent().setCustomDisplayText(label);
		}
	}
	
	private FormLink getFilterLinkByFilter(FlexiTableExtendedFilter filter) {
		for(FormLink link:filterLinks) {
			FlexiFilterButton filterButton = (FlexiFilterButton)link.getUserObject();
			if(filterButton.getFilter().getFilter().equals(filter.getFilter())) {
				return link;
			}	
		}
		return null;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == customizeColumnsLink) {
			fireEvent(ureq, new CustomizeColumnsEvent());
		} else if(source == resetFilterLink) {
			fireEvent(ureq, new FiltersAndSettingsEvent(FiltersAndSettingsEvent.FILTERS_RESET));
		} else if(source == sortLink) {
			fireEvent(ureq, new CustomizeSortEvent());
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if(link.getUserObject() instanceof FlexiFilterButton) {
				FlexiFilterButton filterButton = (FlexiFilterButton)link.getUserObject();
				fireEvent(ureq, new SelectFilterEvent(filterButton));
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		FlexiTableRendererType renderType = null;
		if(renderTypeEl != null && renderTypeEl.isOneSelected()) {
			renderType = FlexiTableRendererType.valueOf(renderTypeEl.getSelectedKey());
		}
		
		fireEvent(ureq, new FiltersAndSettingsEvent(renderType));
	}
	
	protected SortKey getTableOrderBy() {
		return (tableEl.getOrderBy() != null && tableEl.getOrderBy().length > 0) ? tableEl.getOrderBy()[0] : null;
	}
}
