/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.login.auth;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;

/**
 * Initial Date:  08.07.2003
 *
 * @author Mike Stock
 */
public class OLATAuthentcationForm extends FormBasicController {

	private TextElement login;
	private TextElement pass;
	/**
	 * Login form used by the OLAT Authentication Provider
	 * @param name
	 */
	public OLATAuthentcationForm(UserRequest ureq, WindowControl wControl, String id, Translator translator) {
		super(ureq, wControl, id, FormBasicController.LAYOUT_VERTICAL);
		setTranslator(translator);
		initForm(ureq);
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean valid = true;
		login.clearError();
		//only POST is allowed
		if(!"POST".equals(ureq.getHttpReq().getMethod())) {
			login.setErrorKey("error.post.method.mandatory", null);
			valid = false;
		}
		valid &= !login.isEmpty("lf.error.loginempty");
		valid &= !pass.isEmpty("lf.error.passempty");
		return valid;
	}

	/**
	 * @return Login field value.
	 */
	public String getLogin() {
		return login.getValue();
	}

	/**
	 * @return Password filed value.
	 */
	public String getPass() {
		return pass.getValue();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("login.form");
		setFormDescription("login.intro");
	
		FormLayoutContainer loginWrapper = FormLayoutContainer.createInputGroupLayout("loginWrapper", getTranslator(), "<i class='o_icon o_icon-fw o_icon_user'> </i>", null);
		formLayout.add(loginWrapper);
		login = uifactory.addTextElement(mainForm.getFormId() + "_name", "lf_login", "lf.login", 128, "", loginWrapper);
		login.setPlaceholderKey("lf.login", null);
		login.setAutocomplete("username");
		login.setFocus(true);
		
		FormLayoutContainer passWrapper = FormLayoutContainer.createInputGroupLayout("passWrapper", getTranslator(), "<i class='o_icon o_icon-fw o_icon_password'> </i>", null);
		formLayout.add(passWrapper);
		pass  = uifactory.addPasswordElement(mainForm.getFormId() + "_pass", "lf_pass",  "lf.pass", 128, "", passWrapper);
		pass.setAutocomplete("current-password");
		pass.setPlaceholderKey("lf.pass", null);

		login.setDisplaySize(20);
		pass.setDisplaySize(20);
		
		uifactory.addFormSubmitButton(mainForm.getFormId() + "_button", "login.button", "login.button", null, formLayout);
		
		// turn off the dirty message when leaving the login form without loggin in (e.g. pressing guest login)
		flc.getRootForm().setHideDirtyMarkingMessage(true);
		flc.getRootForm().setCsrfProtection(false);
	}

	@Override
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		if(!login.isEmpty() && !pass.isEmpty()) {
			flc.getRootForm().submit(ureq);
		}
	}
}
