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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.model.xml.TextInput;
import org.olat.modules.forms.ui.model.EvaluationFormResponseController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TextInputController extends FormBasicController implements EvaluationFormResponseController {
	
	private TextAreaElement textEl;
	
	private final TextInput textInput;
	private EvaluationFormResponse response;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	
	public TextInputController(UserRequest ureq, WindowControl wControl, TextInput textInput) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.textInput = textInput;
		initForm(ureq);
	}
	
	public TextInputController(UserRequest ureq, WindowControl wControl, TextInput textInput, Form rootForm) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, rootForm);
		this.textInput = textInput;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		int rows = 12;
		if(textInput.getRows() > 0) {
			rows = textInput.getRows();
		}
		
		textEl = uifactory.addTextAreaElement("textinput_" + CodeHelper.getRAMUniqueID(), null, 56000, rows, 72, true, "", formLayout);
	}
	
	public void update() {
		int rows = 12;
		if(textInput.getRows() > 0) {
			rows = textInput.getRows();
		}
		textEl.setRows(rows);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	public void setReadOnly(boolean readOnly) {
		textEl.setEnabled(!readOnly);
	}

	@Override
	public boolean hasResponse() {
		return response != null && StringHelper.containsNonWhitespace(response.getStringuifiedResponse());
	}

	@Override
	public void loadResponse(EvaluationFormSession session) {
		response = evaluationFormManager.loadResponse(textInput.getId(), session);
		if (response != null) {
			textEl.setValue(response.getStringuifiedResponse());
		}
	}

	@Override
	public void saveResponse(EvaluationFormSession session) {
		if (StringHelper.containsNonWhitespace(textEl.getValue())) {
			String stringValue = textEl.getValue();
			if (response == null) {
				response = evaluationFormManager.createStringResponse(textInput.getId(), session, stringValue);
			} else {
				response = evaluationFormManager.updateResponse(response, stringValue);
			}
		} else if (response != null) {
			// If all text is deleted by the user, the response should be deleted as well.
			evaluationFormManager.deleteResponse(response.getKey());
			response = null;
		}
	}
}
