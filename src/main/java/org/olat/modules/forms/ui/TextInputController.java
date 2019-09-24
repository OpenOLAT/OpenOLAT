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

import java.math.BigDecimal;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
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
	
	private TextElement singleRowEl;
	private TextAreaElement multiRowEl;
	
	private final TextInput textInput;
	private boolean singleRow;
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
		singleRowEl = uifactory.addTextElement("textinput_" + CodeHelper.getRAMUniqueID(), null, 1000, null, formLayout);

		multiRowEl = uifactory.addTextAreaElement("textinput_" + CodeHelper.getRAMUniqueID(), null, 56000, -1, 72, false, true, "", formLayout);
		
		update();
	}
	
	public void update() {
		singleRow = textInput.isNumeric() || textInput.isSingleRow();
		
		int rows = 12;
		if(textInput.getRows() > 0) {
			rows = textInput.getRows();
		}
		multiRowEl.setRows(rows);
		
		singleRowEl.setVisible(singleRow);
		multiRowEl.setVisible(!singleRow);
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
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (textInput.isNumeric()) {
			String val = singleRowEl.getValue();
			if(StringHelper.containsNonWhitespace(val)) {
				try {
					Double.parseDouble(val);
				} catch (NumberFormatException e) {
					singleRowEl.setErrorKey("error.no.number", null);
					allOk = false;
				}
			}
		}
		
		return allOk;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		singleRowEl.setEnabled(!readOnly);
		int rows = readOnly? -1: textInput.getRows();
		multiRowEl.setRows(rows);
		multiRowEl.setEnabled(!readOnly);
	}

	@Override
	public boolean hasResponse() {
		return true;
	}

	@Override
	public void initResponse(EvaluationFormSession session, EvaluationFormResponses responses) {
		response = responses.getResponse(session, textInput.getId());
		if (response != null) {
			if (singleRow) {
				singleRowEl.setValue(response.getStringuifiedResponse());
			} else {
				multiRowEl.setValue(response.getStringuifiedResponse());
			}
		}
	}

	@Override
	public void saveResponse(EvaluationFormSession session) {
		String valueToSave = getValueToSave();
		if (StringHelper.containsNonWhitespace(valueToSave)) {
			if (textInput.isNumeric()) {
				BigDecimal value = new BigDecimal(valueToSave);
				if (response == null) {
					response = evaluationFormManager.createNumericalResponse(textInput.getId(), session, value);
				} else {
					response = evaluationFormManager.updateNumericalResponse(response, value);
				}
			} else {
				if (response == null) {
					response = evaluationFormManager.createStringResponse(textInput.getId(), session, valueToSave);
				} else {
					response = evaluationFormManager.updateStringResponse(response, valueToSave);
				}
			}
		} else if (response != null) {
			// If all text is deleted by the user, the response should be deleted as well.
			evaluationFormManager.deleteResponse(response);
			response = null;
		}
	}

	private String getValueToSave() {
		if (singleRow) {
			return singleRowEl.getValue();
		}
		return multiRowEl.getValue();
	}
}
