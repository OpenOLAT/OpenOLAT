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
package org.olat.group.ui.lifecycle;

import java.util.Collection;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroupModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupLifecycleTypeOptionsController extends FormBasicController {
	
	private SingleSelection typeEl;
	private MultipleSelectionElement excludedEl;
	
	@Autowired
	private BusinessGroupModule businessGroupModule;
	
	public BusinessGroupLifecycleTypeOptionsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("group.type.title");
		setFormDescription("group.type.description");
		
		SelectionValues typeValues = new SelectionValues();
		typeValues.add(SelectionValues.entry(BusinessGroupLifecycleTypeEnum.all.name(), translate("group.type.all")));
		typeValues.add(SelectionValues.entry(BusinessGroupLifecycleTypeEnum.withoutResources.name(), translate("group.type.withoutResources")));
		
		typeEl = uifactory.addRadiosVertical("group.type", "group.type", formLayout, typeValues.keys(), typeValues.values());
		String selectedType = businessGroupModule.getGroupLifecycle();
		if(typeValues.containsKey(selectedType)) {
			typeEl.select(selectedType, true);
		}

		SelectionValues excludeValues = new SelectionValues();
		excludeValues.add(SelectionValues.entry("managed", translate("group.type.managed")));

		excludedEl = uifactory.addCheckboxesVertical("group.exclusion", "group.exclusion", formLayout,
				excludeValues.keys(), excludeValues.values(), 1);
		if(businessGroupModule.isGroupLifecycleExcludeManaged()) {
			excludedEl.select("managed", true);
		}
	
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String type = typeEl.getSelectedKey();
		businessGroupModule.setGroupLifecycle(type);
		Collection<String> excluded = excludedEl.getSelectedKeys();
		businessGroupModule.setGroupLifecycleExcludeManaged(excluded.contains("managed"));
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
}
