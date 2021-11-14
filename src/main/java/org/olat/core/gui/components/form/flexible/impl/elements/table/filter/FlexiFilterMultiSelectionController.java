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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 14 juil. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiFilterMultiSelectionController extends FormBasicController {
	
	private FormLink clearButton;
	private FormLink updateButton;
	private TextElement quickSearchEl;
	private FormLink quickSearchButton;
	private FormLink resetQuickSearchButton;
	
	private MultipleSelectionElement listEl;
	
	private final List<String> preselectedKeys;
	private final FlexiTableMultiSelectionFilter filter;
	
	private final Set<String> selectedKeys = new HashSet<>(); 
	
	public FlexiFilterMultiSelectionController(UserRequest ureq, WindowControl wControl,
			FlexiTableMultiSelectionFilter filter, List<String> preselectedKeys) {
		super(ureq, wControl, "field_list", Util.createPackageTranslator(FlexiTableElementImpl.class, ureq.getLocale()));
		this.filter = filter;
		this.preselectedKeys = preselectedKeys;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues availableValues = filter.getSelectionValues();
		String[] keys = availableValues.keys();
		
		if(keys.length > 15) {
			quickSearchEl = uifactory.addTextElement("quicksearch", null, 32, "", formLayout);
			quickSearchEl.setDomReplacementWrapperRequired(false);
			quickSearchEl.addActionListener(FormEvent.ONKEYUP);
			
			quickSearchButton = uifactory.addFormLink("quickSearchButton", "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
			quickSearchButton.setIconLeftCSS("o_icon o_icon_search");
			quickSearchButton.setDomReplacementWrapperRequired(false);
			
			resetQuickSearchButton = uifactory.addFormLink("resetQuickSearch", "", null, formLayout, Link.LINK | Link.NONTRANSLATED);
			resetQuickSearchButton.setElementCssClass("btn o_reset_filter_search");
			resetQuickSearchButton.setIconLeftCSS("o_icon o_icon_remove_filters");
			resetQuickSearchButton.setDomReplacementWrapperRequired(false);
		}
		
		listEl = uifactory.addCheckboxesVertical("list", null, formLayout, keys, availableValues.values(), availableValues.icons(), 1);
		listEl.addActionListener(FormEvent.ONCHANGE);
		listEl.setEscapeHtml(false);
		
		if(preselectedKeys != null && !preselectedKeys.isEmpty()) {
			for(String selectedKey:preselectedKeys) {
				if(availableValues.containsKey(selectedKey)) {
					listEl.select(selectedKey, true);
				}
			}
		}
		
		((FormLayoutContainer)formLayout).contextPut("numOfItems", Integer.valueOf(keys.length));
		
		updateButton = uifactory.addFormLink("update", formLayout, Link.BUTTON_SMALL);
		updateButton.setElementCssClass("o_sel_flexiql_update");
		clearButton = uifactory.addFormLink("clear", formLayout, Link.LINK);
		clearButton.setElementCssClass("o_filter_clear");
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(listEl == source) {
			doSelectItem(ureq);
		} else if(clearButton == source) {
			doClear(ureq);
		} else if(updateButton == source) {
			doUpdate(ureq);
		} else if(quickSearchEl == source) {
			doQuickSearch();
		} else if(quickSearchButton == source) {
			doQuickSearch();
		} else if(resetQuickSearchButton == source) {
			doResetQuickSearch();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem source, FormEvent fe) {
		if(source == clearButton || source == listEl || source == quickSearchButton) {
			super.propagateDirtinessToContainer(source, fe);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doUpdate(ureq);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doUpdate(UserRequest ureq) {
		selectedKeys.addAll(listEl.getSelectedKeys());
		fireEvent(ureq, new ChangeValueEvent(filter, new ArrayList<>(selectedKeys)));
	}
	
	private void doSelectItem(UserRequest ureq) {
		if(quickSearchEl == null || !StringHelper.containsNonWhitespace(quickSearchEl.getValue())) {
			selectedKeys.clear();
		}
		selectedKeys.addAll(listEl.getSelectedKeys());
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doResetQuickSearch() {
		Set<String> allSelectedKeys = new HashSet<>();
		allSelectedKeys.addAll(listEl.getSelectedKeys());
		allSelectedKeys.addAll(selectedKeys);
		SelectionValues availableValues = filter.getSelectionValues();
		listEl.setKeysAndValues(availableValues.keys(), availableValues.values(), null, availableValues.icons());
		if(!allSelectedKeys.isEmpty()) {
			for(String selectedKey:allSelectedKeys) {
				listEl.select(selectedKey, true);
			}
		}
		
		if(quickSearchEl != null) {
			quickSearchEl.setValue("");
		}
	}
	
	private void doClear(UserRequest ureq) {
		listEl.uncheckAll();
		SelectionValues availableValues = filter.getSelectionValues();
		listEl.setKeysAndValues(availableValues.keys(), availableValues.values(), null, availableValues.icons());
		selectedKeys.clear();
		if(quickSearchEl != null) {
			quickSearchEl.setValue("");
		}
		fireEvent(ureq, new ChangeValueEvent(filter, null));
	}
	
	private void doQuickSearch() {
		String searchText = quickSearchEl.getValue().toLowerCase();
		quickSearchEl.getComponent().setDirty(false);

		SelectionValues availableValues = filter.getSelectionValues();
		String[] keys = availableValues.keys();
		String[] values = availableValues.values();
		String[] icons = availableValues.icons();
		if(StringHelper.containsNonWhitespace(searchText)) {
			SelectionValues filtered = new SelectionValues();
			for(int i=0; i<keys.length; i++) {
				String value = values[i].toLowerCase();
				if(value.contains(searchText)) {
					String icon = icons == null || icons.length >= i ? null : icons[i];
					filtered.add(new SelectionValue(keys[i], values[i], null, icon, null, true));
				}
			}
			listEl.setKeysAndValues(filtered.keys(), filtered.values(), null, filtered.icons());
		} else {
			listEl.setKeysAndValues(keys, values, null, icons);
		}
		listEl.getComponent().setDirty(true);
	}
}
