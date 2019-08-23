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

package org.olat.admin.user;

import org.olat.core.CoreSpringFactory;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.login.LoginModule;
import org.olat.registration.RegistrationManager;
import org.olat.registration.TemporaryKey;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Form to send a email to the user with a link to change its password.
 * 
 * <P>
 * Initial Date:  26 mai 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class SendTokenToUserForm extends FormBasicController {
	
	private final Identity user;
	private TextElement mailText;
	private UserChangePasswordMailUtil util;
	
	private String dummyKey;
	
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RegistrationManager registrationManager;
	@Autowired
	private LoginModule loginModule;

	public SendTokenToUserForm(UserRequest ureq, WindowControl wControl, Identity treatedIdentity) {
		super(ureq, wControl);
		user = treatedIdentity;
		util = (UserChangePasswordMailUtil) CoreSpringFactory.getBean(UserChangePasswordMailUtil.class);
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("form.token.new.title");
		setFormDescription("form.token.new.description");
		
		String initialText = generateMailText();
		mailText = uifactory.addTextAreaElement("mailtext", "form.token.new.text", 4000, 12, 255, false, false, initialText, formLayout);
		
		uifactory.addFormSubmitButton("submit", "form.token.new.title", formLayout);
	}
	
	public String getMailText() {
		return mailText.getValue();
	}
	
	public void setMailText(String text) {
		mailText.setValue(text);
	}

	@Override
	protected void doDispose() {
		//auto disposed by basic controller
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String text = mailText.getValue();
		sendToken(ureq, text); // This should replace "text" with a new value where dummyKey is replaced with temporary token
		mailText.setValue(text);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	public FormItem getInitialFormItem() {
		return flc;
	}

	private String generateMailText() {
		try {
			// OLATNG-5: extracted reusable code into UserChangePasswordMailUtil.generateMailText() so it can be used elsewhere
			return util.generateMailText(user);
		} catch (UserHasNoEmailException e) {
			return "This function is not available for users without an email-adress!";
		}
	}
	
	private void sendToken(UserRequest ureq, String text) {
		try {
			// OLATNG-5: extracted reusable code into UserChangePasswordMailUtil.sendTokenByMail() so it can be used elsewhere
			MailerResult result = util.sendTokenByMail(ureq, user, text);
			if (result.getReturnCode() == 0) {
				showInfo("email.sent");
			} else {
				showError("error.send.changepasswd.link", new String[]{ String.valueOf(result.getReturnCode()) });
			}
		} catch(UserChangePasswordException e) {
			showWarning("changeuserpwd.failed");
		} catch(UserHasNoEmailException e) {
			showWarning("changeuserpwd.failed");
		}
	}

}
