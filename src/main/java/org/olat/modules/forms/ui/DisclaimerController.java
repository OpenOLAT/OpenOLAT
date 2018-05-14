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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.CodeHelper;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.forms.model.xml.Disclaimer;
import org.olat.modules.forms.ui.model.EvaluationFormResponseController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 09.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DisclaimerController extends FormBasicController implements EvaluationFormResponseController {
	
	private static final String ACCEPTED_KEY = "disclaimer.accepted";
	private static final String ACCEPTED_DB_KEY = "accepted";
	
	private FormLink openDisclaimerLink;
	private MultipleSelectionElement disclaimerEl;
	private DialogBoxController disclaimerTextCtr;
	
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
		disclaimerEl = uifactory.addCheckboxesVertical("disclaimer_" + CodeHelper.getRAMUniqueID(), null, formLayout,
				new String[] { ACCEPTED_KEY }, new String[] { translate(ACCEPTED_KEY) }, null, null, 1);

		openDisclaimerLink = uifactory.addFormLink("disclaimer_" + CodeHelper.getRAMUniqueID(), "disclaimer.open", null,
				formLayout, Link.LINK);
		openDisclaimerLink.setElementCssClass("o_eva_disc_open");
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == openDisclaimerLink) {
			doOpenDisclaimer(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (disclaimerTextCtr == source) {
			boolean ok = DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event);
			disclaimerEl.select(ACCEPTED_KEY, ok);
		}
		super.event(ureq, source, event);
	}

	private void doOpenDisclaimer(UserRequest ureq) {
		String title = translate("disclaimer.text.title");
		disclaimerTextCtr = activateOkCancelDialog(ureq, title, disclaimer.getText(), disclaimerTextCtr);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		disclaimerEl.clearError();
		if (!disclaimerEl.isAtLeastSelected(1)) {
			disclaimerEl.setErrorKey("disclaimer.not.accepted", null);
			allOk = false;
		}
		
		return allOk & super.validateFormLogic(ureq);
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
		disclaimerEl.setEnabled(!readOnly);
	}

	@Override
	public boolean hasResponse() {
		return response != null && ACCEPTED_DB_KEY.equals(response.getStringuifiedResponse());
	}

	@Override
	public void loadResponse(EvaluationFormSessionRef session) {
		response = evaluationFormManager.loadResponse(disclaimer.getId(), session);
		boolean accepted = response != null && ACCEPTED_DB_KEY.equals(response.getStringuifiedResponse());
		disclaimerEl.select(ACCEPTED_KEY, accepted);
	}

	@Override
	public void saveResponse(EvaluationFormSession session) {
		boolean accepted = disclaimerEl.isAtLeastSelected(1);
		if (accepted && response == null) {
			response = evaluationFormManager.createStringResponse(disclaimer.getId(), session, ACCEPTED_DB_KEY);
		} else if (!accepted && response != null) {
			evaluationFormManager.deleteResponse(response);
			response = null;
		}
	}

}
