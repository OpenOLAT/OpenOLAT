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
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.PageElementRenderingHints;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.ceditor.ui.PageRunControllerElement;
import org.olat.modules.forms.model.xml.TextInput;
import org.olat.modules.forms.ui.TextInputController;
import org.olat.modules.forms.ui.TextInputEditorController;
import org.olat.modules.forms.ui.model.EvaluationFormExecutionElement;
import org.olat.modules.forms.ui.model.EvaluationFormResponseController;
import org.olat.modules.forms.ui.model.EvaluationFormResponseControllerElement;
import org.olat.modules.forms.ui.model.ExecutionIdentity;

/**
 * 
 * Initial date: 12 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TextInputHandler implements EvaluationFormElementHandler, SimpleAddPageElementHandler {
	
	private final boolean restrictedEdit;
	
	public TextInputHandler(boolean restrictedEdit) {
		this.restrictedEdit = restrictedEdit;
	}
	
	@Override
	public String getType() {
		return "formtextinput";
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_textinput";
	}
	
	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.questionType;
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element, PageElementRenderingHints hints) {
		if(element instanceof TextInput) {
			Controller ctrl = new TextInputController(ureq, wControl, (TextInput)element, false);
			return new PageRunControllerElement(ctrl);
		}
		return null;
	}

	@Override
	public PageElementEditorController getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof TextInput) {
			return new TextInputEditorController(ureq, wControl, (TextInput)element, restrictedEdit);
		}
		return null;
	}

	@Override
	public PageElement createPageElement(Locale locale) {
		TextInput part = new TextInput();
		part.setId(UUID.randomUUID().toString());
		part.setMandatory(false);
		part.setNumeric(false);
		part.setDate(false);
		part.setSingleRow(false);
		part.setRows(12);
		return part;
	}

	@Override
	public EvaluationFormExecutionElement getExecutionElement(UserRequest ureq, WindowControl wControl, Form rootForm,
			PageElement element, ExecutionIdentity executionIdentity) {
		if (element instanceof TextInput) {
			TextInput textInput = (TextInput) element;
			EvaluationFormResponseController ctrl = new TextInputController(ureq, wControl, textInput, rootForm);
			return new EvaluationFormResponseControllerElement(ctrl);
		}
		return null;
	}

}
