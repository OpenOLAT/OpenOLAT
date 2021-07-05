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
package org.olat.modules.forms.rules.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;
import static org.olat.core.util.ArrayHelper.emptyStrings;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.ui.PageEditorUIFactory;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.ChoiceSelectedCondition;
import org.olat.modules.forms.model.xml.Condition;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.MultipleChoice;
import org.olat.modules.forms.model.xml.SingleChoice;
import org.olat.modules.forms.rules.ConditionEditorFragment;

/**
 * 
 * Initial date: 8 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ChoiceConditionFragement implements ConditionEditorFragment {

	private Translator translator;
	private FormLayoutContainer ruleCont;
	private SingleSelection elementEl;
	private SingleSelection choiceEl;
	
	private final FormUIFactory uifactory;
	private ChoiceSelectedCondition condition;
	private final Form form;

	public ChoiceConditionFragement(FormUIFactory uifactory, ChoiceSelectedCondition condition, Form form) {
		this.uifactory = uifactory;
		this.condition = condition;
		this.form = form;
	}

	@Override
	public FormItem initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		this.translator = formLayout.getTranslator();
		
		String id = Long.toString(CodeHelper.getRAMUniqueID());
		String page = Util.getPackageVelocityRoot(this.getClass()) + "/choice.html";
		ruleCont = FormLayoutContainer.createCustomFormLayout("choice." + id, formLayout.getTranslator(), page);
		ruleCont.setRootForm(formLayout.getRootForm());
		formLayout.add(ruleCont);
		ruleCont.getFormItemComponent().contextPut("id", id);
	
		SelectionValues conditionKV = new SelectionValues();
		for (AbstractElement element : form.getElements()) {
			if (element instanceof SingleChoice) {
				SingleChoice singleChoice = (SingleChoice)element;
				String value = StringHelper.containsNonWhitespace(singleChoice.getName())
						? singleChoice.getName()
						: PageEditorUIFactory.formatUntitled(translator, singleChoice.getId());
				value = Formatter.truncate(value, 23);
				conditionKV.add(SelectionValues.entry(singleChoice.getId(), value));
			} else if (element instanceof MultipleChoice) {
				MultipleChoice multipleChoice = (MultipleChoice)element;
				String value = StringHelper.containsNonWhitespace(multipleChoice.getName())
						? multipleChoice.getName()
						: PageEditorUIFactory.formatUntitled(translator, multipleChoice.getId());
				value = Formatter.truncate(value, 23);
				conditionKV.add(SelectionValues.entry(multipleChoice.getId(), value));
			}
		}
		elementEl = uifactory.addDropdownSingleselect("element." + id, null, ruleCont, conditionKV.keys(),
				conditionKV.values(), null);
		elementEl.setDomReplacementWrapperRequired(false);
		elementEl.addActionListener(FormEvent.ONCHANGE);
		if (condition != null) {
			String elementId = condition.getElementId();
			if (Arrays.asList(elementEl.getKeys()).contains(elementId)) {
				elementEl.select(elementId, true);
			} else {
				elementEl.enableNoneSelection(translator.translate("element.deleted"));
				ruleCont.setErrorKey("error.element.not.available", null);
			}
		} else if (elementEl.getKeys().length > 0) {
			elementEl.select(elementEl.getKeys()[0], true);
		}
		
		choiceEl = uifactory.addDropdownSingleselect("choice." + id, null, ruleCont, emptyStrings(), emptyStrings(), null);
		choiceEl.setDomReplacementWrapperRequired(false);
		choiceEl.addActionListener(FormEvent.ONCHANGE);
		updateChoiceUI();
		if (condition != null) {
			String choiceKey = condition.getChoiceId();
			if (Arrays.asList(choiceEl.getKeys()).contains(choiceKey)) {
				choiceEl.select(choiceKey, true);
			} else {
				choiceEl.enableNoneSelection(translator.translate("element.deleted"));
				ruleCont.setErrorKey("error.element.not.available", null);
			}
		}
		
		if (elementEl.getKeys().length == 0) {
			ruleCont = FormLayoutContainer.createBareBoneFormLayout("choice." + id, formLayout.getTranslator());
			ruleCont.setRootForm(formLayout.getRootForm());
			formLayout.add(ruleCont);
			ruleCont.getFormItemComponent().contextPut("id", id);
			uifactory.addStaticTextElement("element." + id, null,
					formLayout.getTranslator().translate("no.condition.available"), ruleCont);
		}
		
		return ruleCont;
	}
	
	private void updateChoiceUI() {
		String elementKey = elementEl.isOneSelected()? elementEl.getSelectedKey(): null;
		SelectionValues choiceKV = getChoiceKV(elementKey);
		choiceEl.setKeysAndValues(choiceKV.keys(), choiceKV.values(), null);
	}
	
	private SelectionValues getChoiceKV(String elementKey) {
		SelectionValues choiceKV = new SelectionValues();
		if (elementKey != null) {
			Optional<AbstractElement> element = form.getElements().stream().filter(e -> elementKey.equals(e.getId())).findFirst();
			if (element.isPresent()) {
				AbstractElement choiceElement = element.get();
				if (choiceElement instanceof SingleChoice) {
					((SingleChoice)choiceElement).getChoices().asList().stream()
							.forEach(choice -> choiceKV.add(entry( 
									choice.getId(),
									Formatter.truncate(choice.getValue(), 23))));
					choiceEl.setKeysAndValues(choiceKV.keys(), choiceKV.values(), null);
				} else if (choiceElement instanceof MultipleChoice) {
					((MultipleChoice)choiceElement).getChoices().asList().stream()
							.forEach(choice -> choiceKV.add(entry(
									choice.getId(), 
									Formatter.truncate(choice.getValue(), 23))));
				}
			}
		}
		return choiceKV;
	}

	@Override
	public void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == elementEl) {
			ruleCont.clearError();
			elementEl.disableNoneSelection();
			choiceEl.disableNoneSelection();
			updateChoiceUI();
		} else if (source == choiceEl) {
			ruleCont.clearError();
			choiceEl.disableNoneSelection();
		}
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		ruleCont.clearError();
		if (!elementEl.isOneSelected() || !choiceEl.isOneSelected()) {
			ruleCont.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	public Condition getCondition() {
		if (condition == null) {
			condition = new ChoiceSelectedCondition();
			condition.setId(UUID.randomUUID().toString());
		}
		
		condition.setElementId(elementEl.getSelectedKey());
		condition.setChoiceId(choiceEl.getSelectedKey());
		
		return condition;
	}

}
