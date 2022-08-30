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
import org.olat.modules.forms.model.xml.Disclaimer;
import org.olat.modules.forms.ui.DisclaimerController;
import org.olat.modules.forms.ui.DisclaimerEditorController;
import org.olat.modules.forms.ui.model.EvaluationFormExecutionElement;
import org.olat.modules.forms.ui.model.EvaluationFormResponseController;
import org.olat.modules.forms.ui.model.EvaluationFormResponseControllerElement;
import org.olat.modules.forms.ui.model.ExecutionIdentity;

/**
 * 
 * Initial date: 09.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DisclaimerHandler implements EvaluationFormElementHandler, SimpleAddPageElementHandler, CloneElementHandler {

	private final boolean restrictedEdit;
	
	public DisclaimerHandler(boolean restrictedEdit) {
		this.restrictedEdit = restrictedEdit;
	}

	@Override
	public String getType() {
		return Disclaimer.TYPE;
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_eva_disclaimer";
	}
	
	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.organisational;
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element,
			PageElementRenderingHints options) {
		if(element instanceof Disclaimer) {
			Disclaimer disclaimer = (Disclaimer) element;
			EvaluationFormResponseController ctrl = new DisclaimerController(ureq, wControl, disclaimer);
			return new EvaluationFormResponseControllerElement(ctrl);
		}
		return null;
	}

	@Override
	public Controller getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof Disclaimer) {
			Disclaimer disclaimer = (Disclaimer) element;
			return new DisclaimerEditorController(ureq, wControl, disclaimer, restrictedEdit);
		}
		return null;
	}
	
	@Override
	public PageElementInspectorController getInspector(UserRequest ureq, WindowControl wControl, PageElement element) {
		return null;
	}

	@Override
	public EvaluationFormExecutionElement getExecutionElement(UserRequest ureq, WindowControl wControl, Form rootForm,
			PageElement element, ExecutionIdentity executionIdentity) {
		if (element instanceof Disclaimer) {
			Disclaimer disclaimer = (Disclaimer) element;
			EvaluationFormResponseController ctrl = new DisclaimerController(ureq, wControl, disclaimer, rootForm);
			return new EvaluationFormResponseControllerElement(ctrl);
		}
		return null;
	}

	@Override
	public PageElement createPageElement(Locale locale) {
		Translator translator = Util.createPackageTranslator(DisclaimerEditorController.class, locale);
		String agreement = translator.translate("disclaimer.default.agreement");
		String text = translator.translate("disclaimer.default.text");
	
		Disclaimer disclaimer = new Disclaimer();
		disclaimer.setId(UUID.randomUUID().toString());
		disclaimer.setAgreement(agreement);
		disclaimer.setText(text);
		return disclaimer;
	}

	@Override
	public PageElement clonePageElement(PageElement element) {
		if (element instanceof Disclaimer) {
			Disclaimer disclaimer = (Disclaimer)element;
			Disclaimer clone = new Disclaimer();
			clone.setId(UUID.randomUUID().toString());
			clone.setAgreement(disclaimer.getAgreement());
			clone.setText(disclaimer.getText());
			return clone;
		}
		return null;
	}

}
