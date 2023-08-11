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
package org.olat.login.webauthn.ui;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmDeletePasskeyController extends FormBasicController {
	
	private final PasskeyRow passkey;
	
	@Autowired
	private BaseSecurity securityManager;
	
	public ConfirmDeletePasskeyController(UserRequest ureq, WindowControl wControl, PasskeyRow passkey) {
		super(ureq, wControl, "confirm_delete");
		this.passkey = passkey;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			String msg = translate("confirm.delete.passkey");
			layoutCont.contextPut("msg", msg);
		}
		
		FormSubmit submit = uifactory.addFormSubmitButton("delete.passkey", formLayout);
		submit.setElementCssClass("btn btn-default btn-danger");
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		securityManager.deleteAuthentication(passkey.getAuthentication());
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
}
