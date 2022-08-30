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
package org.olat.modules.forms.handler;

import java.util.Locale;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.CloneElementHandler;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementRenderingHints;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.forms.model.xml.Choice;
import org.olat.modules.forms.model.xml.Choices;
import org.olat.modules.forms.model.xml.SingleChoice;
import org.olat.modules.forms.model.xml.SingleChoice.Presentation;
import org.olat.modules.forms.ui.SingleChoiceController;
import org.olat.modules.forms.ui.SingleChoiceEditorController;
import org.olat.modules.forms.ui.SingleChoiceInspectorController;
import org.olat.modules.forms.ui.model.EvaluationFormExecutionElement;
import org.olat.modules.forms.ui.model.EvaluationFormResponseController;
import org.olat.modules.forms.ui.model.EvaluationFormResponseControllerElement;
import org.olat.modules.forms.ui.model.ExecutionIdentity;


/**
 * 
 * Initial date: 10.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SingleChoiceHandler implements EvaluationFormElementHandler, SimpleAddPageElementHandler, CloneElementHandler {
	
	private final boolean restrictedEdit;
	
	public SingleChoiceHandler(boolean restrictedEdit) {
		this.restrictedEdit = restrictedEdit;
	}

	@Override
	public String getType() {
		return SingleChoice.TYPE;
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_eva_sc";
	}
	
	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.questionType;
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element,
			PageElementRenderingHints options) {
		if (element instanceof SingleChoice) {
			SingleChoice singleChoice = (SingleChoice) element;
			EvaluationFormResponseController ctrl = new SingleChoiceController(ureq, wControl, singleChoice);
			return new EvaluationFormResponseControllerElement(ctrl);
		}
		return null;
	}

	@Override
	public Controller getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof SingleChoice) {
			SingleChoice singleChoice = (SingleChoice) element;
			return new SingleChoiceEditorController(ureq, wControl, singleChoice, restrictedEdit);
		}
		return null;
	}
	
	@Override
	public PageElementInspectorController getInspector(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof SingleChoice) {
			SingleChoice singleChoice = (SingleChoice) element;
			return new SingleChoiceInspectorController(ureq, wControl, singleChoice, restrictedEdit);
		}
		return null;
	}

	@Override
	public PageElement createPageElement(Locale locale) {
		SingleChoice singleChoice = new SingleChoice();
		singleChoice.setId(UUID.randomUUID().toString());
		singleChoice.setMandatory(false);
		singleChoice.setPresentation(Presentation.VERTICAL);
		
		Translator translator = Util.createPackageTranslator(SingleChoiceEditorController.class, locale);
		String value = translator.translate("choice.example");
		Choice choice = new Choice();
		choice.setId(UUID.randomUUID().toString());
		choice.setValue(value);
		Choices choices = new Choices();
		choices.addNotPresent(choice);
		singleChoice.setChoices(choices);
		return singleChoice;
	}

	@Override
	public PageElement clonePageElement(PageElement element) {
		if (element instanceof SingleChoice) {
			SingleChoice singleChoice = (SingleChoice)element;
			SingleChoice clone = new SingleChoice();
			clone.setId(UUID.randomUUID().toString());
			clone.setMandatory(singleChoice.isMandatory());
			clone.setName(singleChoice.getName());
			clone.setPresentation(singleChoice.getPresentation());
			clone.setChoices(new Choices());
			for (Choice choice : singleChoice.getChoices().asList()) {
				Choice clonedChoice = new Choice();
				clonedChoice.setId(UUID.randomUUID().toString());
				clonedChoice.setValue(choice.getValue());
				clone.getChoices().addNotPresent(clonedChoice);
			}
			return clone;
		}
		return null;
	}

	@Override
	public EvaluationFormExecutionElement getExecutionElement(UserRequest ureq, WindowControl wControl, Form rootForm,
			PageElement element, ExecutionIdentity executionIdentity) {
		if (element instanceof SingleChoice) {
			SingleChoice singleChoice = (SingleChoice) element;
			EvaluationFormResponseController ctrl = new SingleChoiceController(ureq, wControl, singleChoice, rootForm);
			return new EvaluationFormResponseControllerElement(ctrl);
		}
		return null;
	}

}
