/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.scope;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.scope.DateScopeDropdown.DateScopeOption;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 10 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class DropdownDateScopeController extends FormBasicController {

	public static final Event RESET_EVENT = new Event("resetscope");
	
	private SingleSelection dateScopeEl;
	private FormLink resetLink;

	private final String label;
	private final DateScopeDropdown dropdownScope;

	protected DropdownDateScopeController(UserRequest ureq, WindowControl wControl, DateScopeDropdown dropdownScope) {
		super(ureq, wControl);
		this.label = dropdownScope.getDropdownLabel();
		this.dropdownScope = dropdownScope;
		initForm(ureq);
	}
	
	public DateScopeDropdown getDropdownScope() {
		return dropdownScope;
	}

	public DateScopeOption getSelectedOption() {
		if(dateScopeEl.isOneSelected()) {
			String selectedScopeKey = dateScopeEl.getSelectedKey();
			for(DateScopeOption dateScope:dropdownScope.getOptions()) {
				if(selectedScopeKey.equals(dateScope.scope().getKey())) {
					return dateScope;
				}
			}
		}
		return null;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_scopes_dropdown");
		
		SelectionValues scopePK = new SelectionValues();
		for(DateScopeOption option:dropdownScope.getOptions()) {
			String displayName = option.displayName();
			scopePK.add(SelectionValues.entry(option.scope().getKey(), displayName));
		}
		dateScopeEl = uifactory.addDropdownSingleselect("date.scopes.dropdown", null, formLayout, scopePK.keys(), scopePK.values());
		dateScopeEl.setLabel("date.scope.dropdown", new String[] { label });
		if (dropdownScope.getPreselectedOption() != null
				&& scopePK.containsKey(dropdownScope.getPreselectedOption().scope().getKey())) {
			dateScopeEl.select(dropdownScope.getPreselectedOption().scope().getKey(), true);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("date.scope.custom.set", buttonsCont);
		resetLink = uifactory.addFormLink("date.scope.custom.reset", buttonsCont, Link.BUTTON);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == resetLink) {
			fireEvent(ureq, RESET_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		dateScopeEl.clearError();
		if (!dateScopeEl.isOneSelected()) {
			dateScopeEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}
}
