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
package org.olat.modules.forms.ui;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.modules.forms.model.xml.Choice;
import org.olat.modules.forms.model.xml.ChoiceSelectedCondition;
import org.olat.modules.forms.model.xml.Rule;
import org.olat.modules.forms.model.xml.SingleChoice;
import org.olat.modules.forms.rules.RuleAware;
import org.olat.modules.forms.rules.RulesEngine;
import org.olat.modules.forms.ui.model.EvaluationFormResponseController;
import org.olat.modules.forms.ui.model.Progress;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SingleChoiceController extends FormBasicController implements EvaluationFormResponseController, RuleAware {

	private SingleSelection singleChoiceEl;
	
	private final SingleChoice singleChoice;
	private boolean validationEnabled = true;
	private RulesEngine rulesEngine;
	private Map<ChoiceSelectedCondition, Rule> selectedConditionToRule;

	private EvaluationFormResponse response;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	
	public SingleChoiceController(UserRequest ureq, WindowControl wControl, SingleChoice singleChoice) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.singleChoice = singleChoice;
		initForm(ureq);
	}
	
	public SingleChoiceController(UserRequest ureq, WindowControl wControl, SingleChoice singleChoice, Form rootForm) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, rootForm);
		this.singleChoice = singleChoice;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		updateForm();
	}

	void updateForm() {
		if (singleChoiceEl != null) {
			flc.remove(singleChoiceEl);
		}
		String name = "sc_" + CodeHelper.getRAMUniqueID();
		List<Choice> choices = singleChoice.getChoices().asList();
		String[] keys = new String[choices.size()];
		String[] values = new String[choices.size()];
		for (int i = 0; i<choices.size(); i++) {
			Choice coice = choices.get(i);
			keys[i] = coice.getId();
			values[i] = coice.getValue();
		}
		switch (singleChoice.getPresentation()) {
			case HORIZONTAL:
				singleChoiceEl = uifactory.addRadiosHorizontal(name, null, flc, keys, values);
				break;
			case VERTICAL:
				singleChoiceEl = uifactory.addRadiosVertical(name, null, flc, keys, values);
				break;
			default:
				singleChoiceEl = uifactory.addDropdownSingleselect(name, null, flc, keys, values);
		}
		singleChoiceEl.setAllowNoSelection(true);
	}

	@Override
	public void initRulesEngine(RulesEngine rulesEngine) {
		this.rulesEngine = rulesEngine;
		selectedConditionToRule = rulesEngine.getRules().stream()
				.filter(rule -> singleChoice.getId().equals(rule.getCondition().getElementId()))
				.filter(rule -> rule.getCondition() instanceof ChoiceSelectedCondition)
				.collect(Collectors.toMap(rule -> (ChoiceSelectedCondition)rule.getCondition(), Function.identity()));
		if (!selectedConditionToRule.isEmpty()) {
			singleChoiceEl.addActionListener(FormEvent.ONCHANGE);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == singleChoiceEl) {
			fireChoiceSelectedCondition();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	public void setValidationEnabled(boolean enabled) {
		this.validationEnabled = enabled;
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		singleChoiceEl.clearError();
		if (!validationEnabled) return true;
		
		boolean allOk = super.validateFormLogic(ureq);
		
		if (singleChoice.isMandatory() && !singleChoiceEl.isOneSelected()) {
			singleChoiceEl.setErrorKey("form.legende.mandatory", null);
			allOk = false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void setReadOnly(boolean readOnly) {
		singleChoiceEl.setEnabled(!readOnly);
	}

	@Override
	public boolean hasResponse() {
		return response != null && StringHelper.containsNonWhitespace(response.getStringuifiedResponse());
	}

	@Override
	public void initResponse(UserRequest ureq, EvaluationFormSession session, EvaluationFormResponses responses) {
		response = responses.getResponse(session, singleChoice.getId());
		if (response != null) {
			for (Choice choice: singleChoice.getChoices().asList()) {
				if (choice.getId().equals(response.getStringuifiedResponse())) {
					singleChoiceEl.select(choice.getId(), true);
				}
			}
		}
		fireChoiceSelectedCondition();
	}

	@Override
	public void saveResponse(UserRequest ureq, EvaluationFormSession session) {
		if (singleChoiceEl.isOneSelected()) {
			String stringValue = singleChoiceEl.getSelectedKey();
			if (response == null) {
				response = evaluationFormManager.createStringResponse(singleChoice.getId(), session, stringValue);
			} else {
				response = evaluationFormManager.updateStringResponse(response, stringValue);
			}
		} else if (response != null) {
			evaluationFormManager.deleteResponse(response);
			response = null;
		}
	}
	
	@Override
	public void deleteResponse(EvaluationFormSession session) {
		if (response != null) {
			evaluationFormManager.deleteResponse(response);
			response = null;
		}
	}

	@Override
	public Progress getProgress() {
		int current = hasResponse()? 1: 0;
		return Progress.of(current, 1);
	}
	
	private void fireChoiceSelectedCondition() {
		for (Map.Entry<ChoiceSelectedCondition, Rule> conditionToRule: selectedConditionToRule.entrySet()) {
			boolean fulfilled = singleChoiceEl.isOneSelected()
					&& conditionToRule.getKey().getChoiceId().equals(singleChoiceEl.getSelectedKey());
			rulesEngine.fulfilledChanged(conditionToRule.getValue(), fulfilled);
		}
	}
	
}
