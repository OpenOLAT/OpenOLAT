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
package org.olat.modules.forms.rules;

import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.ChoiceSelectedCondition;
import org.olat.modules.forms.model.xml.Condition;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.MultipleChoice;
import org.olat.modules.forms.model.xml.SingleChoice;
import org.olat.modules.forms.rules.ui.ChoiceConditionFragement;
import org.olat.modules.forms.rules.ui.ConditionEditorFragment;

/**
 * 
 * Initial date: 6 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ChoiceSelectedHandler implements ConditionHandler {

	@Override
	public String getI18nKey() {
		return "condition.choice";
	}
	
	@Override
	public String getConditionType() {
		return ChoiceSelectedCondition.TYPE;
	}

	@Override
	public boolean accepts(AbstractElement element) {
		return SingleChoice.TYPE.equals(element.getType()) || MultipleChoice.TYPE.equals(element.getType()) ;
	}

	@Override
	public ConditionEditorFragment getEditorFragment(FormUIFactory uifactory, Condition condition, Form form) {
		ChoiceSelectedCondition choiceCondition = condition instanceof ChoiceSelectedCondition
				? (ChoiceSelectedCondition)condition
				: null;
		return new ChoiceConditionFragement(uifactory, choiceCondition, form);
	}

	@Override
	public boolean conditionsAvailable(Form form) {
		for (AbstractElement element : form.getElements()) {
			if (element instanceof SingleChoice) {
				SingleChoice singleChoice = (SingleChoice)element;
				if (!singleChoice.getChoices().asList().isEmpty()) {
					return true;
				}
			} else if (element instanceof MultipleChoice) {
				MultipleChoice multipleChoice = (MultipleChoice)element;
				if (!multipleChoice.getChoices().asList().isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

}
