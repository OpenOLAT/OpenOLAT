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
package org.olat.modules.grading.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.grading.GradingModule;
import org.olat.modules.grading.GradingRoles;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradingAdminConfigurationController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };

	private MultipleSelectionElement enableEl;
	private MultipleSelectionElement realCorrectionTimevisibilityEl;
	
	@Autowired
	private GradingModule gradingModule;
	
	public GradingAdminConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.title");

		String[] onValues = new String[]{ translate("on") };
		enableEl = uifactory.addCheckboxesHorizontal("grading.enabled", "grading.enabled", formLayout, onKeys, onValues);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		enableEl.select(onKeys[0], gradingModule.isEnabled());

		SelectionValues rolesKeyValues = new SelectionValues();
		rolesKeyValues.add(SelectionValues.entry(OrganisationRoles.administrator.name(), translate("grading.real.correction.time.administrator")));
		rolesKeyValues.add(SelectionValues.entry(OrganisationRoles.learnresourcemanager.name(), translate("grading.real.correction.time.learnresourcemanager")));
		rolesKeyValues.add(SelectionValues.entry(OrganisationRoles.author.name(), translate("grading.real.correction.time.author")));
		rolesKeyValues.add(SelectionValues.entry(GradingRoles.grader.name(), translate("grading.real.correction.time.grader")));
		realCorrectionTimevisibilityEl = uifactory.addCheckboxesVertical("grading.real.correction.time.visibility", "grading.real.correction.time.visibility",
				formLayout, rolesKeyValues.keys(), rolesKeyValues.values(), 1);
		List<String> roles = gradingModule.getCorrectionRealMinutesVisibility();
		for(String role:roles) {
			if(rolesKeyValues.containsKey(role)) {
				realCorrectionTimevisibilityEl.select(role, true);
			}
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		buttonsCont.setRootForm(mainForm);
		
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private void updateUI() {
		boolean enabled = enableEl.isAtLeastSelected(1);
		realCorrectionTimevisibilityEl.setVisible(enabled);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = enableEl.isAtLeastSelected(1);
		gradingModule.setEnabled(enabled);
		if(enabled) {
			Collection<String> roles = realCorrectionTimevisibilityEl.getSelectedKeys();
			gradingModule.setCorrectionRealMinutesVisibility(new ArrayList<>(roles));
		}
	}
}
