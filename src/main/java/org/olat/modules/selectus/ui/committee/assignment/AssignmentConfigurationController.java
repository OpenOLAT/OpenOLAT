/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.committee.assignment;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.AssignmentMethods;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.committee.assignment.AssignmentsData.Spreading;

/**
 * 
 * Initial date: 24 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssignmentConfigurationController extends StepFormBasicController {
	
	private static final String[] assignmentMethodsKeys = new String[] {
			AssignmentMethods.manual.name(), AssignmentMethods.automatic.name()
		};
	private static final String[] assignmentSpreadingMethodsKeys = new String[] {
			Spreading.additional.name(), Spreading.total.name()
		};
	
	private TextElement maximumAssignmentsEl;
	private TextElement additionalAssignmentsEl;
	private SingleSelection assignmentMethodEl;
	private SingleSelection assignmentSpreadingMethodEl;
	private FormLayoutContainer numberForm;
	
	private AssignmentsData data;
	
	public AssignmentConfigurationController(UserRequest ureq, WindowControl wControl,
			StepsRunContext runContext, Form form, AssignmentsData data) {
		super(ureq, wControl, form, runContext, LAYOUT_DEFAULT, null);
		this.data = data;
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("assignment.configuration.title");
		
		String[] assignmentMethodsValues = new String[] {
			data.getApplications().size() == 1 ? translate("assignments.method.all") : translate("assignments.method.all.plural"),
			data.getApplications().size() == 1 ? translate("assignments.method.subset") : translate("assignments.method.subset.plural")
		}; 
		assignmentMethodEl = uifactory.addRadiosVertical("assignments.method", formLayout,
				assignmentMethodsKeys, assignmentMethodsValues);
		assignmentMethodEl.addActionListener(FormEvent.ONCHANGE);
		
		boolean methodFound = false;
		if(data.getAssignmentMethod() != null) {
			for(String key:assignmentMethodsKeys) {
				if(key.equals(data.getAssignmentMethod().name())) {
					assignmentMethodEl.select(key, true);
					methodFound = true;
				}
			}	
		}
		if(!methodFound) {
			assignmentMethodEl.select(assignmentMethodsKeys[1], true);
		}
		
		String page = velocity_root + "/assignments_numbers.html";
		numberForm = FormLayoutContainer.createCustomFormLayout("assignments.numbers", getTranslator(), page);
		numberForm.setLabel("assignments.numbers", null);
		numberForm.setHelpTextKey("assignments.numbers.help", null);
		numberForm.setRootForm(mainForm);
		formLayout.add(numberForm);
		String max = data.getMaximumAssignments() == null ? "" : data.getMaximumAssignments().toString();
		maximumAssignmentsEl = uifactory.addTextElement("assignmentsmax", "assignments.max", 8, max, numberForm);
		maximumAssignmentsEl.setDomReplacementWrapperRequired(false);
		maximumAssignmentsEl.setDisplaySize(4);
		String add = data.getAdditionalAssignments() == null ? "" : data.getAdditionalAssignments().toString();
		additionalAssignmentsEl = uifactory.addTextElement("assignmentsadditional", "assignments.additional", 8, add, numberForm);
		additionalAssignmentsEl.setDomReplacementWrapperRequired(false);
		additionalAssignmentsEl.setDisplaySize(4);
		
		String[] assignmentSpreadingMethodsValues = new String[] {
			translate("assignments.spreading.additional"), translate("assignments.spreading.total")
		};
		assignmentSpreadingMethodEl = uifactory.addRadiosVertical("assignments.spreading.method", formLayout,
				assignmentSpreadingMethodsKeys, assignmentSpreadingMethodsValues);
		assignmentSpreadingMethodEl.setHelpTextKey("assignments.spreading.help", null);
		boolean spreadingFound = false;
		if(data.getSpreading() != null) {
			for(String key:assignmentSpreadingMethodsKeys) {
				if(key.equals(data.getSpreading().name())) {
					assignmentSpreadingMethodEl.select(key, true);
					spreadingFound = true;
				}
			}
		}
		if(!spreadingFound) {
			assignmentSpreadingMethodEl.select(assignmentSpreadingMethodsKeys[0], true);
		}
	}
	
	private void updateUI() {
		if(assignmentMethodEl.isOneSelected()) {
			boolean automatic = AssignmentMethods.automatic.name().equals(assignmentMethodEl.getSelectedKey());
			numberForm.setVisible(automatic);
			assignmentSpreadingMethodEl.setVisible(automatic);
		}
	}

	@Override
	protected void doDispose() {
		mainForm.removeSubFormListener(this);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);	
		allOk &= validateForm();
		return allOk;
	}
	
	private boolean validateForm() {
		boolean allOk = true;
		
		maximumAssignmentsEl.clearError();
		additionalAssignmentsEl.clearError();
		if(numberForm.isVisible()) {
			allOk &= RecruitingHelper.validateIntegerElement(maximumAssignmentsEl, false);
			allOk &= RecruitingHelper.validateIntegerElement(additionalAssignmentsEl, false);
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(assignmentMethodEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	public void back() {
		if(validateForm()) {
			commitData();
		}
		super.back();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		commitData();
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private void commitData() {
		AssignmentMethods method = AssignmentMethods.valueOf(assignmentMethodEl.getSelectedKey());
		data.setAssignmentMethod(method);
		
		if(method == AssignmentMethods.automatic) {
			Spreading spreading = Spreading.valueOf(assignmentSpreadingMethodEl.getSelectedKey());
			data.setSpreading(spreading);
			
			if(StringHelper.containsNonWhitespace(maximumAssignmentsEl.getValue())
					&& StringHelper.isLong(maximumAssignmentsEl.getValue())) {
				data.setMaximumAssignments(Integer.valueOf(maximumAssignmentsEl.getValue()));
			} else {
				data.setMaximumAssignments(null);
			}
			
			if(StringHelper.containsNonWhitespace(additionalAssignmentsEl.getValue())
					&& StringHelper.isLong(additionalAssignmentsEl.getValue())) {
				data.setAdditionalAssignments(Integer.valueOf(additionalAssignmentsEl.getValue()));
			} else {
				data.setAdditionalAssignments(null);
			}
		} else {
			data.setSpreading(null);
			data.setMaximumAssignments(null);
		}
	}
}