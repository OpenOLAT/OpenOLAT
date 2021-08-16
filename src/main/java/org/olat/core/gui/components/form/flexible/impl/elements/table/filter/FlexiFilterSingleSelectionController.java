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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 16 juil. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiFilterSingleSelectionController extends FormBasicController {
	
	private FormLink clearButton;
	private FormLink updateButton;
	private SingleSelection listEl;
	
	private final FlexiTableSingleSelectionFilter filter;
	
	public FlexiFilterSingleSelectionController(UserRequest ureq, WindowControl wControl, FlexiTableSingleSelectionFilter filter) {
		super(ureq, wControl, "field_list", Util.createPackageTranslator(FlexiTableElementImpl.class, ureq.getLocale()));
		this.filter = filter;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues availableValues = filter.getSelectionValues();
		String[] keys = availableValues.keys();

		listEl = uifactory.addRadiosVertical("list", null, formLayout, keys, availableValues.values());
		listEl.setAllowNoSelection(true);
		listEl.addActionListener(FormEvent.ONCHANGE);
		listEl.setEscapeHtml(false);
		String preselectedKey = filter.getValue();
		if(StringHelper.containsNonWhitespace(preselectedKey) &&availableValues.containsKey(preselectedKey)) {
			listEl.select(preselectedKey, true);
		}
		
		((FormLayoutContainer)formLayout).contextPut("numOfItems", Integer.valueOf(keys.length));
		
		updateButton = uifactory.addFormLink("update", formLayout, Link.BUTTON_SMALL);
		updateButton.setElementCssClass("o_sel_flexiql_update");
		clearButton = uifactory.addFormLink("clear", formLayout, Link.LINK);
		clearButton.setElementCssClass("o_filter_clear");
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(listEl == source) {
			doSelectItem(ureq);
		} else if(clearButton == source) {
			doClear(ureq);
		} else if(updateButton == source) {
			doUpdate(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem source, FormEvent fe) {
		if(source == clearButton || source == listEl) {
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
		if(listEl.isOneSelected()) {
			filter.setValue(listEl.getSelectedKey());
			fireEvent(ureq, new ChangeFilterEvent(filter));
		} else {
			filter.setValue(null);
		}
	}
	
	private void doSelectItem(UserRequest ureq) {
		if(listEl.isOneSelected()) {
			filter.setValue(listEl.getSelectedKey());
		} else {
			filter.setValue(null);
		}
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doClear(UserRequest ureq) {
		if(listEl.isOneSelected()) {
			filter.setValue(null);
			listEl.select(listEl.getSelectedKey(), false);
		}
		fireEvent(ureq, new ChangeFilterEvent(filter));
	}
}
