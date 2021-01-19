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
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
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
import org.olat.modules.forms.model.xml.Disclaimer;
import org.olat.modules.forms.ui.model.EvaluationFormResponseController;
import org.olat.modules.forms.ui.model.Progress;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 09.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DisclaimerController extends FormBasicController implements EvaluationFormResponseController {
	
	private static final String ACCEPTED_KEY = "disclaimer.accepted";
	private static final String[] ACCEPTED_KEYS = { ACCEPTED_KEY };
	private static final String ACCEPTED_DB_KEY = "accepted";
	
	private StaticTextElement textEl;
	private MultipleSelectionElement agreementEl;
	
	private final Disclaimer disclaimer;
	private EvaluationFormResponse response;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public DisclaimerController(UserRequest ureq, WindowControl wControl, Disclaimer disclaimer) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.disclaimer = disclaimer;
		initForm(ureq);
	}

	public DisclaimerController(UserRequest ureq, WindowControl wControl, Disclaimer disclaimer, Form rootForm) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, rootForm);
		this.disclaimer = disclaimer;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		long sufix = CodeHelper.getRAMUniqueID();

		textEl = uifactory.addStaticTextElement("agreement_" + sufix, null, disclaimer.getText(), formLayout);
		textEl.setElementCssClass("o_disclaimer o_disclaimer_content");
		boolean hasText = StringHelper.containsNonWhitespace(disclaimer.getText());
		textEl.setVisible(hasText);
		
		agreementEl = uifactory.addCheckboxesVertical("disclaimer_" + sufix, null, formLayout,
				ACCEPTED_KEYS, new String[] { disclaimer.getAgreement() }, null, null, 1);
	}
	
	void update() {
		textEl.setValue(disclaimer.getText());
		boolean hasText = StringHelper.containsNonWhitespace(disclaimer.getText());
		textEl.setVisible(hasText);
		agreementEl.setKeysAndValues(ACCEPTED_KEYS, new String[] { disclaimer.getAgreement() });
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		agreementEl.clearError();
		if (!agreementEl.isAtLeastSelected(1)) {
			agreementEl.setErrorKey("disclaimer.not.accepted", null);
			allOk = false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		agreementEl.setEnabled(!readOnly);
	}

	@Override
	public boolean hasResponse() {
		return response != null && ACCEPTED_DB_KEY.equals(response.getStringuifiedResponse());
	}

	@Override
	public void initResponse(UserRequest ureq, EvaluationFormSession session, EvaluationFormResponses responses) {
		response = responses.getResponse(session, disclaimer.getId());
		boolean accepted = response != null && ACCEPTED_DB_KEY.equals(response.getStringuifiedResponse());
		agreementEl.select(ACCEPTED_KEY, accepted);
	}

	@Override
	public void saveResponse(UserRequest ureq, EvaluationFormSession session) {
		boolean accepted = agreementEl.isAtLeastSelected(1);
		if (accepted && response == null) {
			response = evaluationFormManager.createStringResponse(disclaimer.getId(), session, ACCEPTED_DB_KEY);
		} else if (!accepted && response != null) {
			evaluationFormManager.deleteResponse(response);
			response = null;
		}
	}

	@Override
	public Progress getProgress() {
		int current = hasResponse()? 1: 0;
		return Progress.of(current, 1);
	}

}
