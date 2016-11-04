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
package org.olat.modules.portfolio.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderDeliveryOptions;
import org.olat.modules.portfolio.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderDeliveryOptionsController extends FormBasicController implements Activateable2 {
	
	private static final String[] onKeys = new String[] { "on" };
	private static final String[] onValues = new String[] { "" };
	
	private MultipleSelectionElement newEntriesEl;
	private MultipleSelectionElement deleteBinderEl;
	
	private final Binder binder;
	private final BinderDeliveryOptions deliveryOptions;
	
	@Autowired
	private PortfolioService portfolioService;
	
	 
	public BinderDeliveryOptionsController(UserRequest ureq, WindowControl wControl, Binder binder) {
		super(ureq, wControl);
		
		this.binder = binder;
		deliveryOptions = portfolioService.getDeliveryOptions(binder.getOlatResource());
		 
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("Portfolio template: Administration and editing#configuration");
		
		newEntriesEl = uifactory.addCheckboxesHorizontal("canAddEntries", "allow.new.entries", formLayout, onKeys, onValues);
		if(deliveryOptions.isAllowNewEntries()) {
			newEntriesEl.select(onKeys[0], true);
		}
		
		deleteBinderEl = uifactory.addCheckboxesHorizontal("canDeleteBinder", "allow.delete.binder", formLayout, onKeys, onValues);
		deleteBinderEl.addActionListener(FormEvent.ONCHANGE);
		if(deliveryOptions.isAllowDeleteBinder()) {
			deleteBinderEl.select(onKeys[0], true);
		}
		
		FormLayoutContainer buttonsLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsLayout.setRootForm(mainForm);
		formLayout.add(buttonsLayout);
		uifactory.addFormSubmitButton("save", buttonsLayout);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == deleteBinderEl) {
			if(deleteBinderEl.isAtLeastSelected(1)) {
				showWarning("allow.delete.binder.warning");
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean allowNewEntries = newEntriesEl.isAtLeastSelected(1);
		deliveryOptions.setAllowNewEntries(allowNewEntries);
		boolean allowDeleteBinder = deleteBinderEl.isAtLeastSelected(1);
		deliveryOptions.setAllowDeleteBinder(allowDeleteBinder);
		portfolioService.setDeliveryOptions(binder.getOlatResource(), deliveryOptions);
	}
}
