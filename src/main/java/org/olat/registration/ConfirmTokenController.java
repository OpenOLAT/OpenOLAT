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
package org.olat.registration;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 6 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmTokenController extends FormBasicController {
	
	private TextElement tokenEl;
	
	private final String token;
	private final Identity recipient;
	
	public ConfirmTokenController(UserRequest ureq, WindowControl wControl, Identity recipient, String token) {
		super(ureq, wControl);
		this.recipient = recipient;
		this.token = token;
		initForm(ureq);
	}
	
	public Identity getRecipient() {
		return recipient;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		tokenEl = uifactory.addTextElement("token", "pw.change.confirm.token", 32, "", formLayout);
		tokenEl.setFocus(true);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("pw.change.confirm", buttonsCont);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(StringHelper.containsNonWhitespace(tokenEl.getValue())) {
			if(!tokenEl.getValue().equals(token)) {
				tokenEl.setErrorKey("error.pw.change.confirm.token", null);
				allOk &= false;
			}
		} else {
			tokenEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
