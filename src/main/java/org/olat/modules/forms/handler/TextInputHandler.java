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
import org.olat.modules.forms.model.xml.TextInput;
import org.olat.modules.forms.ui.TextInputCompareController;
import org.olat.modules.forms.ui.TextInputController;
import org.olat.modules.forms.ui.TextInputEditorController;
import org.olat.modules.forms.ui.model.CompareResponse;
import org.olat.modules.forms.ui.model.EvaluationFormExecutionElement;
import org.olat.modules.forms.ui.model.EvaluationFormResponseController;
import org.olat.modules.forms.ui.model.EvaluationFormResponseControllerElement;
import org.olat.modules.portfolio.ui.editor.PageElement;
import org.olat.modules.portfolio.ui.editor.PageElementEditorController;
import org.olat.modules.portfolio.ui.editor.PageElementRenderingHints;
import org.olat.modules.portfolio.ui.editor.PageRunControllerElement;
import org.olat.modules.portfolio.ui.editor.PageRunElement;
import org.olat.modules.portfolio.ui.editor.SimpleAddPageElementHandler;

/**
 * 
 * Initial date: 12 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TextInputHandler implements EvaluationFormElementHandler, SimpleAddPageElementHandler {
	
	@Override
	public String getType() {
		return "formtextinput";
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_textinput";
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element, PageElementRenderingHints hints) {
		if(element instanceof TextInput) {
			Controller ctrl = new TextInputController(ureq, wControl, (TextInput)element);
			return new PageRunControllerElement(ctrl);
		}
		return null;
	}

	@Override
	public PageElementEditorController getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof TextInput) {
			return new TextInputEditorController(ureq, wControl, (TextInput)element);
		}
		return null;
	}

	@Override
	public PageElement createPageElement(Locale locale) {
		TextInput part = new TextInput();
		part.setId(UUID.randomUUID().toString());
		part.setNumeric(false);
		part.setSingleRow(false);
		part.setRows(12);
		return part;
	}

	@Override
	public EvaluationFormExecutionElement getExecutionElement(UserRequest ureq, WindowControl wControl, Form rootForm,
			PageElement element) {
		if (element instanceof TextInput) {
			TextInput textInput = (TextInput) element;
			EvaluationFormResponseController ctrl = new TextInputController(ureq, wControl, textInput, rootForm);
			return new EvaluationFormResponseControllerElement(ctrl);
		}
		return null;
	}

	@Override
	public List<String> getCompareResponseIdentifiers(PageElement element) {
		if (element instanceof TextInput) {
			TextInput textInput = (TextInput) element;
			return Arrays.asList(textInput.getId());
		}
		return Collections.emptyList();
	}

	@Override
	public Component getCompareComponent(UserRequest ureq, WindowControl windowControl, PageElement element,
			List<CompareResponse> compareResponses) {
		if (element instanceof TextInput) {
			TextInput textInput = (TextInput) element;
			Controller ctrl = new TextInputCompareController(ureq, windowControl, textInput, compareResponses);
			return ctrl.getInitialComponent();
		}
		return null;
	}
}
