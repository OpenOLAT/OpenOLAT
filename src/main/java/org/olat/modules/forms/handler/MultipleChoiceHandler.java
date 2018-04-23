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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.forms.model.xml.Choice;
import org.olat.modules.forms.model.xml.Choices;
import org.olat.modules.forms.model.xml.MultipleChoice;
import org.olat.modules.forms.ui.MultipleChoiceCompareController;
import org.olat.modules.forms.ui.MultipleChoiceController;
import org.olat.modules.forms.ui.MultipleChoiceEditorController;
import org.olat.modules.forms.ui.model.CompareResponse;
import org.olat.modules.forms.ui.model.EvaluationFormExecutionElement;
import org.olat.modules.forms.ui.model.EvaluationFormResponseController;
import org.olat.modules.forms.ui.model.EvaluationFormResponseControllerElement;
import org.olat.modules.portfolio.ui.editor.PageElement;
import org.olat.modules.portfolio.ui.editor.PageElementRenderingHints;
import org.olat.modules.portfolio.ui.editor.PageRunElement;
import org.olat.modules.portfolio.ui.editor.SimpleAddPageElementHandler;

/**
 * 
 * Initial date: 23.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MultipleChoiceHandler  implements EvaluationFormElementHandler, SimpleAddPageElementHandler {

	@Override
	public String getType() {
		return MultipleChoice.TYPE;
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_eva_mc";
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element,
			PageElementRenderingHints options) {
		if (element instanceof MultipleChoice) {
			MultipleChoice multipleChoice = (MultipleChoice) element;
			EvaluationFormResponseController ctrl = new MultipleChoiceController(ureq, wControl, multipleChoice);
			return new EvaluationFormResponseControllerElement(ctrl);
		}
		return null;
	}

	@Override
	public Controller getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof MultipleChoice) {
			MultipleChoice multipleChoice = (MultipleChoice) element;
			return new MultipleChoiceEditorController(ureq, wControl, multipleChoice);
		}
		return null;
	}

	@Override
	public PageElement createPageElement(Locale locale) {
		MultipleChoice multipleChoice = new MultipleChoice();
		multipleChoice.setId(UUID.randomUUID().toString());
		multipleChoice.setWithOthers(false);
		
		Translator translator = Util.createPackageTranslator(MultipleChoiceEditorController.class, locale);
		String value = translator.translate("choice.example");
		Choice choice = new Choice();
		choice.setId(UUID.randomUUID().toString());
		choice.setValue(value);
		Choices choices = new Choices();
		choices.addNotPresent(choice);
		multipleChoice.setChoices(choices);
		return multipleChoice;
	}

	@Override
	public EvaluationFormExecutionElement getExecutionElement(UserRequest ureq, WindowControl wControl, Form rootForm,
			PageElement element) {
		if (element instanceof MultipleChoice) {
			MultipleChoice multipleChoice = (MultipleChoice) element;
			EvaluationFormResponseController ctrl = new MultipleChoiceController(ureq, wControl, multipleChoice, rootForm);
			return new EvaluationFormResponseControllerElement(ctrl);
		}
		return null;
	}

	@Override
	public List<String> getCompareResponseIdentifiers(PageElement element) {
		if (element instanceof MultipleChoice) {
			MultipleChoice multipleChoice = (MultipleChoice) element;
			return Arrays.asList(multipleChoice.getId());
		}
		return Collections.emptyList();
	}

	@Override
	public Component getCompareComponent(UserRequest ureq, WindowControl windowControl, PageElement element,
			List<CompareResponse> compareResponses) {
		if (element instanceof MultipleChoice) {
			MultipleChoice multipleChoice = (MultipleChoice) element;
			Controller ctrl = new MultipleChoiceCompareController(ureq, windowControl, multipleChoice, compareResponses);
			return ctrl.getInitialComponent();
		}
		return null;
	}

}
