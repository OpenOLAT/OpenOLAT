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
package org.olat.user.propertyhandlers.ui;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.services.sms.SimpleMessageException;
import org.olat.core.commons.services.sms.SimpleMessageService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SmsPhoneSendController extends FormBasicController {
	
	private TextElement newPhoneEl;
	
	private final String sentToken;
	private final User userToChange;
	private final UserPropertyHandler handler;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private SimpleMessageService messageService;
	
	public SmsPhoneSendController(UserRequest ureq, WindowControl wControl, UserPropertyHandler handler, User userToChange) {
		super(ureq, wControl);
		setTranslator(userManager.getPropertyHandlerTranslator(Util.createPackageTranslator(SmsPhoneSendController.class, getLocale())));
		this.userToChange = userToChange;
		this.handler = handler;
		sentToken = messageService.generateToken();
		initForm(ureq);
	}
	
	public String getSentToken() {
		return sentToken;
	}
	
	public String getPhone() {
		return newPhoneEl.getValue();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("sms.change.number.descr");
		
		String i18nLabel = handler.i18nFormElementLabelKey();
		newPhoneEl = uifactory.addTextElement("sms.new.phone", i18nLabel, 32, "", formLayout);
		newPhoneEl.setPlaceholderKey("sms.change.number.hint", null);
		newPhoneEl.setExampleKey("sms.phone.number.example", null);
		newPhoneEl.setFocus(true);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("sms.send", "sms.send", buttonsCont);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		newPhoneEl.clearError();
		if(!StringHelper.containsNonWhitespace(newPhoneEl.getValue())) {
			newPhoneEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(!messageService.validate(newPhoneEl.getValue())) {
			newPhoneEl.setErrorKey("error.phone.invalid", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		try {
			String msg = translate("sms.token", new String[]{ sentToken });
			String phone = newPhoneEl.getValue();
			
			Identity recipient = securityManager.findIdentityByUser(userToChange);
			messageService.sendMessage(msg, phone, recipient);
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
