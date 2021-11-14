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
package org.olat.core.commons.services.sms.ui;

import org.olat.core.commons.services.sms.SimpleMessageException;
import org.olat.core.commons.services.sms.SimpleMessageModule;
import org.olat.core.commons.services.sms.SimpleMessageService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.login.SupportsAfterLoginInterceptor;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SMSPhoneController extends FormBasicController implements SupportsAfterLoginInterceptor {
	
	private FormLink dontActivateButton;
	private TextElement phoneEl, tokenEl;
	
	private String sentToken;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private SimpleMessageModule messageModule;
	@Autowired
	private SimpleMessageService messageService;
	
	public SMSPhoneController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		phoneEl = uifactory.addTextElement("sms.phone.number", "sms.phone.number", 32, "", formLayout);
		phoneEl.setPlaceholderKey("sms.phone.number.hint", null);
		phoneEl.setExampleKey("sms.phone.number.example", null);
		phoneEl.setFocus(true);
		
		tokenEl = uifactory.addTextElement("sms.token.number", "sms.token.number", 16, "", formLayout);
		tokenEl.setExampleKey("sms.token.number.explain", null);
		tokenEl.setFocus(true);
		tokenEl.setVisible(false);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("sms.send", buttonsCont);
		dontActivateButton = uifactory.addFormLink("dont.activate", buttonsCont, Link.BUTTON);
	}

	@Override
	public boolean isUserInteractionRequired(UserRequest ureq) {
		return messageModule.isEnabled()
				&& messageModule.isResetPasswordEnabled() && messageModule.isAskByFirstLogin()
				&& !ureq.getUserSession().getRoles().isGuestOnly()
				&& !ureq.getUserSession().getRoles().isInvitee()
				&& !messageService.validate(ureq.getIdentity().getUser().getProperty(UserConstants.SMSTELMOBILE, getLocale()));
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		phoneEl.clearError();
		if(phoneEl.isVisible()) {
			if(!StringHelper.containsNonWhitespace(phoneEl.getValue())) {
				phoneEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			} else if(!messageService.validate(phoneEl.getValue())) {
				phoneEl.setErrorKey("error.phone.invalid", null);
				allOk &= false;
			}
		}
		
		tokenEl.clearError();
		if(tokenEl.isVisible()) {
			String tokenValue = tokenEl.getValue();
			if(!StringHelper.containsNonWhitespace(tokenValue)) {
				tokenEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			} else if(sentToken == null || !sentToken.equals(tokenValue)) {
				tokenEl.setErrorKey("error.invalid.token", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(phoneEl.isVisible()) {
			try {
				phoneEl.setVisible(false);
				tokenEl.setVisible(true);
				
				sentToken = messageService.generateToken();
				String msg = translate("sms.token", new String[]{ sentToken });
				messageService.sendMessage(msg, phoneEl.getValue(), getIdentity());
			} catch (SimpleMessageException e) {
				phoneEl.setVisible(true);
				tokenEl.setVisible(false);
				phoneEl.setErrorKey("error.phone.invalid", null);
			}
		} else if(tokenEl.isVisible()) {
			User user = getIdentity().getUser();
			user.setProperty(UserConstants.SMSTELMOBILE, phoneEl.getValue());
			userManager.updateUserFromIdentity(getIdentity());
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == dontActivateButton) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}
}