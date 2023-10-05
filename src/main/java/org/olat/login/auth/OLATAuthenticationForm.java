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
package org.olat.login.auth;

import org.olat.basesecurity.AuthHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.login.LoginEvent;
import org.olat.login.LoginModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATAuthenticationForm extends FormBasicController {
	
	private TextElement loginEl;
	private TextElement passEl;
	private FormLink backButton;
	private FormSubmit loginButton;

	@Autowired
	private LoginModule loginModule;
	@Autowired
	private OLATAuthManager olatAuthenticationSpi;
	
	/**
	 * Login form used by the OLAT Authentication Provider
	 * @param name
	 */
	public OLATAuthenticationForm(UserRequest ureq, WindowControl wControl, String id, Translator translator) {
		super(ureq, wControl, id, "login");
		setTranslator(translator);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("login.form");
		setFormDescription("login.intro");
	
		loginEl = uifactory.addTextElement(mainForm.getFormId() + "_name", "lf_login", "lf.login", 128, "", formLayout);
		loginEl.setAutocomplete("username");
		loginEl.setDisplaySize(20);
		loginEl.setFocus(true);
		
		passEl  = uifactory.addPasswordElement(mainForm.getFormId() + "_pass", "lf_pass",  "lf.pass", 128, "", formLayout);
		passEl.setAutocomplete("current-password");
		passEl.setDisplaySize(20);
		passEl.setVisible(false);
		
		loginButton = uifactory.addFormSubmitButton(mainForm.getFormId() + "_button", "login.button", "next", null, formLayout);
		backButton = uifactory.addFormLink("back", formLayout, Link.BUTTON);
		backButton.setVisible(false);
		
		// turn off the dirty message when leaving the login form without loggin in (e.g. pressing guest login)
		flc.getRootForm().setHideDirtyMarkingMessage(true);
		flc.getRootForm().setCsrfProtection(false);
	}
	
	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean valid = true;
		loginEl.clearError();
		//only POST is allowed
		if(!"POST".equals(ureq.getHttpReq().getMethod())) {
			loginEl.setErrorKey("error.post.method.mandatory");
			valid = false;
		}
		valid &= !loginEl.isEmpty("lf.error.loginempty");
		
		passEl.clearError();
		if(passEl.isVisible()) {
			valid &= !passEl.isEmpty("lf.error.passempty");
		}
		return valid;
	}

	@Override
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		if(backButton == source) {
			doBack(ureq);
		} else if(!loginEl.isEmpty() && !passEl.isEmpty()) {
			flc.getRootForm().submit(ureq);
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		if(passEl.isVisible()) {
			doLogin(ureq);
		} else {
			doStart();
			fireEvent(ureq, new LoginEvent());
		}
	}
	
	private void doStart() {
		clearError();
		
		loginEl.setEnabled(false);
		passEl.setVisible(true);
		passEl.setFocus(true);
		loginButton.setI18nKey("login.button", null);
		backButton.setVisible(true);
		
		Command focusCommand = FormJSHelper.getFormFocusCommand(flc.getRootForm().getFormName(), passEl.getForId());						
		getWindowControl().getWindowBackOffice().sendCommandTo(focusCommand);
	}
	
	private void doBack(UserRequest ureq) {
		clearError();
		loginEl.setEnabled(true);
		loginEl.setValue("");
		passEl.setValue("");
		passEl.setVisible(false);
		passEl.setFocus(false);
		loginButton.setI18nKey("next", null);
		backButton.setVisible(false);
		fireEvent(ureq, Event.BACK_EVENT);
	}
	
	private void doLogin(UserRequest ureq) {
		clearError();
		
		String login = loginEl.getValue();
		String pass = passEl.getValue();	
		if (loginModule.isLoginBlocked(login)) {
			// do not proceed when blocked
			setError("login.blocked", loginModule.getAttackPreventionTimeoutMin().toString());
			getLogger().info(Tracing.M_AUDIT, "Login attempt on already blocked login for {}. IP::{}", login, ureq.getHttpReq().getRemoteAddr());
			return;
		}

		AuthenticationStatus status = new AuthenticationStatus();
		Identity authenticatedIdentity = olatAuthenticationSpi.authenticate(null, login, pass, status);
		if(status.getStatus() == AuthHelper.LOGIN_INACTIVE) {
			setError("login.error.inactive", WebappHelper.getMailConfig("mailSupport"));
		} else if (authenticatedIdentity == null) {
			if (loginModule.registerFailedLoginAttempt(login)) {
				getLogger().info(Tracing.M_AUDIT, "Too many failed login attempts for {}. Login blocked. IP::{}", login, ureq.getHttpReq().getRemoteAddr());
				setError("login.blocked", loginModule.getAttackPreventionTimeoutMin().toString());
			} else {
				setError("login.error", WebappHelper.getMailConfig("mailReplyTo"));
			}
		} else {
			try {
				String language = authenticatedIdentity.getUser().getPreferences().getLanguage();
				UserSession usess = ureq.getUserSession();
				if(StringHelper.containsNonWhitespace(language)) {
					usess.setLocale(I18nManager.getInstance().getLocaleOrDefault(language));
				}

				
			} catch (Exception e) {
				logError("Cannot set the user language", e);
			}
			
			loginModule.clearFailedLoginAttempts(login);
			fireEvent(ureq, new AuthenticationEvent(authenticatedIdentity));
		}
	}
	
	private void clearError() {
		flc.contextRemove("error");
	}
	
	private void setError(String i18nKey, String... args) {
		flc.contextPut("error", translate(i18nKey, args));
	}
}
