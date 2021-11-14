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
package org.olat.user.ui.admin;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;
import org.olat.user.UserModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserAccountDeletionSettingsController extends FormBasicController {
	
	private MultipleSelectionElement requestDeleteEl;
	private TextElement emailEl;
	
	@Autowired
	private UserModule userModule;
	
	public UserAccountDeletionSettingsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("allow.request.delete.account.time");
		
		String[] deleteKeys = new String[] { "anytime", "disclaimer" };
		String[] deleteValues = new String[] { translate("allow.request.delete.account.anytime"), translate("allow.request.delete.account.disclaimer") };
		
		requestDeleteEl = uifactory.addCheckboxesVertical("allow.request.delete.account", formLayout, deleteKeys, deleteValues, 1);
		requestDeleteEl.addActionListener(FormEvent.ONCHANGE);
		if(userModule.isAllowRequestToDeleteAccount()) {
			requestDeleteEl.select(deleteKeys[0], true);
		}
		if(userModule.isAllowRequestToDeleteAccountDisclaimer()) {
			requestDeleteEl.select(deleteKeys[1], true);
		}
		
		String email = userModule.getMailToRequestAccountDeletion();
		emailEl = uifactory.addTextElement("allow.request.delete.account.mail", 256, email, formLayout);
		emailEl.setVisible(requestDeleteEl.isAtLeastSelected(1));
		
		FormLayoutContainer layoutCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(layoutCont);
		uifactory.addFormSubmitButton("save", layoutCont);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		emailEl.clearError();
		requestDeleteEl.clearError();
		if(requestDeleteEl.isAtLeastSelected(1)) {
			if(!StringHelper.containsNonWhitespace(emailEl.getValue())) {
				emailEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			} else if(!MailHelper.isValidEmailAddress(emailEl.getValue())) {
				emailEl.setErrorKey("error.mail.not.valid", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(requestDeleteEl == source) {
			emailEl.setVisible(requestDeleteEl.isAtLeastSelected(1));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		userModule.setAllowRequestToDeleteAccount(requestDeleteEl.isSelected(0));
		userModule.setAllowRequestToDeleteAccountDisclaimer(requestDeleteEl.isSelected(1));
		if(requestDeleteEl.isAtLeastSelected(1)) {
			userModule.setMailToRequestAccountDeletion(emailEl.getValue());
		} else {
			userModule.setMailToRequestAccountDeletion("");
		}
	}
}
