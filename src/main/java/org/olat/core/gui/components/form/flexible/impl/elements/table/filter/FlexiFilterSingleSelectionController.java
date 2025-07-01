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
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 16 juil. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiFilterSingleSelectionController extends FlexiFilterExtendedController {
	
	private SingleSelection listEl;
	
	private final String preselectedKey;
	private final FlexiTableSingleSelectionFilter filter;
	
	public FlexiFilterSingleSelectionController(UserRequest ureq, WindowControl wControl,
			Form form, FlexiTableSingleSelectionFilter filter, String preselectedKey) {
		super(ureq, wControl, LAYOUT_CUSTOM, "field_list_unlimited", form);
		setTranslator(Util.createPackageTranslator(FlexiTableElementImpl.class, getLocale()));
		this.filter = filter;
		this.preselectedKey = preselectedKey;
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
		listEl.addActionListener(FormEvent.ONCHANGE);
		if(StringHelper.containsNonWhitespace(preselectedKey) &&availableValues.containsKey(preselectedKey)) {
			listEl.select(preselectedKey, true);
		}
		updateClearButtonUI(ureq, listEl.isOneSelected());
		
		((FormLayoutContainer)formLayout).contextPut("numOfItems", Integer.valueOf(keys.length));
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem source, FormEvent fe) {
		if(source == listEl) {
			super.propagateDirtinessToContainer(source, fe);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == listEl) {
			updateClearButtonUI(ureq, listEl.isOneSelected());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void doUpdate(UserRequest ureq) {
		if(listEl.isOneSelected()) {
			fireEvent(ureq, new ChangeValueEvent(filter, listEl.getSelectedKey()));
		} else {
			fireEvent(ureq, new ChangeValueEvent(filter, null));
		}
	}
	
	@Override
	public void doClear(UserRequest ureq) {
		if(listEl.isOneSelected()) {
			listEl.select(listEl.getSelectedKey(), false);
		}
		fireEvent(ureq, new ChangeValueEvent(filter, null));
	}
}
