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

import org.olat.core.commons.services.sms.SimpleMessageException;
import org.olat.core.commons.services.sms.SimpleMessageService;
import org.olat.core.commons.services.sms.ui.SMSPhoneController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SendMessageController extends FormBasicController {
	
	private final String sentToken;
	private final Identity recipient;
	
	@Autowired
	private SimpleMessageService messageService;
	
	public SendMessageController(UserRequest ureq, WindowControl wControl, Identity recipient) {
		super(ureq, wControl, "send_message", Util.createPackageTranslator(SMSPhoneController.class, ureq.getLocale()));
		this.recipient = recipient;
		sentToken = messageService.generateToken();
		initForm(ureq);
	}
	
	public Identity getRecipient() {
		return recipient;
	}
	
	public String getSentToken() {
		return sentToken;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uifactory.addFormSubmitButton("send.sms", "pw.change.sms", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		try {
			String msg = translate("sms.token", new String[]{ sentToken });
			messageService.sendMessage(msg, recipient);
			fireEvent(ureq, Event.DONE_EVENT);
		} catch (SimpleMessageException e) {
			showWarning("warning.message.not.send");
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
