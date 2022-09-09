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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.modules.forms.model.xml.Choice;
import org.olat.modules.forms.model.xml.ChoiceSelectedCondition;
import org.olat.modules.forms.model.xml.MultipleChoice;
import org.olat.modules.forms.model.xml.Rule;
import org.olat.modules.forms.rules.RuleAware;
import org.olat.modules.forms.rules.RulesEngine;
import org.olat.modules.forms.ui.model.EvaluationFormResponseController;
import org.olat.modules.forms.ui.model.Progress;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MultipleChoiceController extends FormBasicController implements EvaluationFormResponseController, RuleAware {
	
	private static final String OTHERS_KEY = "multiple.choice.others";

	private MultipleSelectionElement multipleChoiceEl;
	private TextElement otherEl;
	
	private MultipleChoice multipleChoice;
	private List<EvaluationFormResponse> multipleChoiceResponses;
	private boolean validationEnabled = true;
	private RulesEngine rulesEngine;
	private Map<ChoiceSelectedCondition, Rule> selectedConditionToRule;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	
	public MultipleChoiceController(UserRequest ureq, WindowControl wControl, MultipleChoice multipleChoice) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.multipleChoice = multipleChoice;
		initForm(ureq);
	}
	
	public MultipleChoiceController(UserRequest ureq, WindowControl wControl, MultipleChoice multipleChoice, Form rootForm) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, rootForm);
		this.multipleChoice = multipleChoice;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		updateForm();
	}

	void updateForm() {
		if (multipleChoiceEl != null) {
			flc.remove(multipleChoiceEl);
		}
		String name = "mc_" + CodeHelper.getRAMUniqueID();
		List<Choice> choices = multipleChoice.getChoices().asList();
		int numberOfChoices = choices.size() + (multipleChoice.isWithOthers()? 1: 0);
		String[] keys = new String[numberOfChoices];
		String[] values = new String[numberOfChoices];
		for (int i = 0; i<choices.size(); i++) {
			Choice coice = choices.get(i);
			keys[i] = coice.getId();
			values[i] = coice.getValue();
		}
		if (multipleChoice.isWithOthers()) {
			keys[keys.length - 1] = OTHERS_KEY;
			values[values.length -1] = translate(OTHERS_KEY);
		}
		switch (multipleChoice.getPresentation()) {
			case HORIZONTAL:
				multipleChoiceEl = uifactory.addCheckboxesHorizontal(name, null, flc, keys, values);
				break;
			case DROPDOWN:
				multipleChoiceEl = uifactory.addCheckboxesDropdown(name, null, flc, keys, values);
				break;
			default:
				multipleChoiceEl = uifactory.addCheckboxesVertical(name, null, flc, keys, values, 1);
		}
		multipleChoiceEl.addActionListener(FormEvent.ONCHANGE);
		
		if (otherEl != null) {
			flc.remove(otherEl);
		}
		otherEl = uifactory.addTextElement("mc_other_" + CodeHelper.getRAMUniqueID(), null, 1000, "", flc);
		otherEl.setElementCssClass("o_evaluation_mc_other");
		showHideOthers();
	}

	@Override
	public void initRulesEngine(RulesEngine rulesEngine) {
		this.rulesEngine = rulesEngine;
		selectedConditionToRule = rulesEngine.getRules().stream()
				.filter(rule -> multipleChoice.getId().equals(rule.getCondition().getElementId()))
				.filter(rule -> rule.getCondition() instanceof ChoiceSelectedCondition)
				.collect(Collectors.toMap(rule -> (ChoiceSelectedCondition)rule.getCondition(), Function.identity()));
		fireChoiceSelectedCondition();
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof ChangePartEvent) {
			ChangePartEvent cpe = (ChangePartEvent)event;
			multipleChoice = (MultipleChoice)cpe.getElement();
			updateForm();
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == multipleChoiceEl) {
			showHideOthers();
			fireChoiceSelectedCondition();
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void showHideOthers() {
		boolean othersSelected = multipleChoiceEl.getSelectedKeys().contains(OTHERS_KEY);
		otherEl.setVisible(othersSelected);
		
		// Remove error if others field is shown
		multipleChoiceEl.clearError();
		otherEl.clearError();
	}

	@Override
	public void setValidationEnabled(boolean enabled) {
		this.validationEnabled = enabled;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		multipleChoiceEl.clearError();
		if (!validationEnabled) return true;
		
		boolean allOk = super.validateFormLogic(ureq);
		
		if (multipleChoice.isMandatory() && !isOneSelected() && !isOtherFilledIn()) {
			if (otherEl.isVisible()) {
				otherEl.setErrorKey("form.legende.mandatory", null);
			} else {
				multipleChoiceEl.setErrorKey("form.legende.mandatory", null);
			}
			allOk = false;
		}
		
		return allOk;
	}
	
	private boolean isOneSelected() {
		return multipleChoiceEl.isAtLeastSelected(2)
				// not only the other key selected
				|| (multipleChoiceEl.isAtLeastSelected(1) && !multipleChoiceEl.getSelectedKeys().contains(OTHERS_KEY));
	}

	private boolean isOtherFilledIn() {
		return otherEl != null && otherEl.isVisible() && StringHelper.containsNonWhitespace(otherEl.getValue());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		multipleChoiceEl.setEnabled(!readOnly);
		otherEl.setEnabled(!readOnly);
	}

	@Override
	public boolean hasResponse() {
		// Multiple choice is always optional.
		return true;
	}

	@Override
	public void initResponse(UserRequest ureq, EvaluationFormSession session, EvaluationFormResponses responses) {
		multipleChoiceResponses = responses.getResponses(session, multipleChoice.getId());
		for (EvaluationFormResponse response: multipleChoiceResponses) {
			String key = response.getStringuifiedResponse();
			if (multipleChoiceEl.getKeys().contains(key)) {
				multipleChoiceEl.select(key, true);
			} else if (multipleChoice.isWithOthers()) {
				multipleChoiceEl.select(OTHERS_KEY, true);
				otherEl.setValue(key);
			}
		}
		showHideOthers();
	}

	@Override
	public void saveResponse(UserRequest ureq, EvaluationFormSession session) {
		evaluationFormManager.deleteResponses(multipleChoiceResponses);

		Collection<String> selectedChoises = new ArrayList<>(multipleChoiceEl.getSelectedKeys());
		replaceOthersKeyWithValue(selectedChoises);
		multipleChoiceResponses = new ArrayList<>();
		for (String choice: selectedChoises) {
			EvaluationFormResponse response = evaluationFormManager.createStringResponse(multipleChoice.getId(), session, choice);
			multipleChoiceResponses.add(response);
		}
	}

	private void replaceOthersKeyWithValue(Collection<String> selectedChoises) {
		if (selectedChoises.contains(OTHERS_KEY)) {
			selectedChoises.remove(OTHERS_KEY);
			String otherValue = otherEl.getValue();
			if (StringHelper.containsNonWhitespace(otherValue)) {
				selectedChoises.add(otherValue);
			}
		}
	}
	
	@Override
	public void deleteResponse(EvaluationFormSession session) {
		if (multipleChoiceResponses != null) {
			multipleChoiceResponses.forEach(response -> evaluationFormManager.deleteResponse(response));
			multipleChoiceResponses = new ArrayList<>(1);
		}
	}

	@Override
	public Progress getProgress() {
		int current = multipleChoiceResponses.isEmpty()? 0: 1;
		return Progress.of(current, 1);
	}
	
	private void fireChoiceSelectedCondition() {
		for (Map.Entry<ChoiceSelectedCondition, Rule> conditionToRule: selectedConditionToRule.entrySet()) {
			boolean fulfilled = multipleChoiceEl.isAtLeastSelected(1)
					&& multipleChoiceEl.getSelectedKeys().contains(conditionToRule.getKey().getChoiceId());
			rulesEngine.fulfilledChanged(conditionToRule.getValue(), fulfilled);
		}
	}

}
