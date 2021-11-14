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

package org.olat.user;

import static org.olat.login.ui.LoginUIFactory.formatDescriptionAsList;

import java.util.List;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.modules.bc.FolderManager;
import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.commons.services.webdav.manager.WebDAVAuthManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormCancel;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ldap.ui.LDAPAuthenticationController;
import org.olat.login.auth.OLATAuthManager;
import org.olat.login.validation.SyntaxValidator;
import org.olat.login.validation.ValidationResult;
import org.olat.shibboleth.ShibbolethDispatcher;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Controller to change the WebDAV password
 * 
 * <P>
 * Initial Date:  15 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class WebDAVPasswordController extends FormBasicController {

	private FormSubmit saveButton;
	private FormCancel cancelButton;
	private FormLink newButton;
	private StaticTextElement rulesEl;
	private TextElement passwordEl;
	private TextElement confirmPasswordEl;
	private StaticTextElement usernamesStaticEl;
	private StaticTextElement passwordStaticEl;
	
	private final SyntaxValidator syntaxValidator;
	
	@Autowired
	private UserModule userModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OLATAuthManager olatAuthManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private WebDAVAuthManager webDAVAuthManager;
	
	public WebDAVPasswordController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "pwdav", Util.createPackageTranslator(FolderRunController.class, ureq.getLocale()));
		syntaxValidator = olatAuthManager.createPasswordSytaxValidator();
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("pwdav.title");
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutContainer = (FormLayoutContainer)formLayout;
			layoutContainer.contextPut("webdavhttp", FolderManager.getWebDAVHttp());
			layoutContainer.contextPut("webdavhttps", FolderManager.getWebDAVHttps());
			
			FormLayoutContainer accessDataFlc = FormLayoutContainer.createDefaultFormLayout("flc_access_data", getTranslator());
			layoutContainer.add(accessDataFlc);

			boolean hasOlatToken = false;
			boolean hasWebDAVToken = false;
			List<Authentication> authentications = securityManager.getAuthentications(getIdentity());
			for(Authentication auth : authentications) {
				if(BaseSecurityModule.getDefaultAuthProviderIdentifier().equals(auth.getProvider())) {
					hasOlatToken = true;
				} else if(WebDAVAuthManager.PROVIDER_WEBDAV.equals(auth.getProvider())
						|| WebDAVAuthManager.PROVIDER_WEBDAV_EMAIL.equals(auth.getProvider())
						|| WebDAVAuthManager.PROVIDER_WEBDAV_INSTITUTIONAL_EMAIL.equals(auth.getProvider())
						|| WebDAVAuthManager.PROVIDER_HA1.equals(auth.getProvider())
						|| WebDAVAuthManager.PROVIDER_HA1_EMAIL.equals(auth.getProvider())
						|| WebDAVAuthManager.PROVIDER_HA1_INSTITUTIONAL_EMAIL.equals(auth.getProvider())) {
					hasWebDAVToken = true;
				}
			}
			
			String usernames = getUsernames(authentications);
			usernamesStaticEl = uifactory.addStaticTextElement("pwdav.username", "pwdav.username", usernames, accessDataFlc);
			
			if(hasOlatToken) {
				String passwordPlaceholder = getTranslator().translate("pwdav.password.placeholder");
				uifactory.addStaticTextElement("pwdav.password", "pwdav.password", passwordPlaceholder, accessDataFlc);
			} else {
				String passwordPlaceholderKey = hasWebDAVToken ? "pwdav.password.set" : "pwdav.password.not_set";
				String passwordPlaceholder = getTranslator().translate(passwordPlaceholderKey);
				passwordStaticEl = uifactory.addStaticTextElement("pwdav.password", "pwdav.password", passwordPlaceholder, accessDataFlc);

				String descriptions = formatDescriptionAsList(syntaxValidator.getAllDescriptions(), getLocale());
				rulesEl = uifactory.addStaticTextElement("pwdav.password.rules", null,
						translate("pwdav.password.rules", new String[] { descriptions }), accessDataFlc);
				rulesEl.setVisible(false);
				
				passwordEl = uifactory.addPasswordElement("pwdav.password.2", "pwdav.password", 5000, "", accessDataFlc);
				passwordEl.setVisible(false);
				passwordEl.setMandatory(true);
				confirmPasswordEl = uifactory.addPasswordElement("pwdav.password.confirm", "pwdav.password.confirm", 5000, "", accessDataFlc);
				confirmPasswordEl.setVisible(false);
				confirmPasswordEl.setMandatory(true);

				FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
				buttonGroupLayout.setRootForm(mainForm);
				accessDataFlc.add(buttonGroupLayout);
				
				if(hasWebDAVToken) {
					newButton = uifactory.addFormLink("pwdav.password.change", buttonGroupLayout, Link.BUTTON);
				} else {
					newButton = uifactory.addFormLink("pwdav.password.new", buttonGroupLayout, Link.BUTTON);
				}
				saveButton = uifactory.addFormSubmitButton("save", buttonGroupLayout);
				saveButton.setVisible(false);
				cancelButton = uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
				cancelButton.setVisible(false);
			}
			
			layoutContainer.put("access_data", accessDataFlc.getComponent());
		}
	}
	
	private String getUsernames(List<Authentication> authentications) {
		StringBuilder sb = new StringBuilder(64);
		for(Authentication auth : authentications) {
			if(WebDAVAuthManager.PROVIDER_WEBDAV.equals(auth.getProvider())
					|| WebDAVAuthManager.PROVIDER_WEBDAV_EMAIL.equals(auth.getProvider())
					|| WebDAVAuthManager.PROVIDER_WEBDAV_INSTITUTIONAL_EMAIL.equals(auth.getProvider())
					|| WebDAVAuthManager.PROVIDER_HA1.equals(auth.getProvider())
					|| WebDAVAuthManager.PROVIDER_HA1_EMAIL.equals(auth.getProvider())
					|| WebDAVAuthManager.PROVIDER_HA1_INSTITUTIONAL_EMAIL.equals(auth.getProvider())) {
				appendUsername(auth.getAuthusername(), sb);
			} else if(BaseSecurityModule.getDefaultAuthProviderIdentifier().equals(auth.getProvider())) {
				appendUsername(auth.getAuthusername(), sb);
				if(userModule.isEmailUnique()) {
					appendUsername(getIdentity().getUser().getEmail(), sb);
					appendUsername(getIdentity().getUser().getInstitutionalEmail(), sb);
				}
			}
		}
		
		if(sb.length() == 0) {
			String nextAuthUsername = getLogin(authentications);
			appendUsername(nextAuthUsername, sb);
			if(userModule.isEmailUnique()) {
				appendUsername(getIdentity().getUser().getEmail(), sb);
				appendUsername(getIdentity().getUser().getInstitutionalEmail(), sb);
			}
		}
		return sb.toString();
	}
	
	private void appendUsername(String authUsername, StringBuilder sb) {
		if(StringHelper.containsNonWhitespace(authUsername) && sb.indexOf(authUsername) < 0) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(authUsername);
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		if(passwordEl.isVisible()) {
			passwordEl.clearError();
			String password = passwordEl.getValue();
			ValidationResult validationResult = syntaxValidator.validate(password, getIdentity());
			if (!validationResult.isValid()) {
				String descriptions = formatDescriptionAsList(validationResult.getInvalidDescriptions(), getLocale());
				passwordEl.setErrorKey("error.password.invalid", new String[] { descriptions });
				allOk &= false;
			}
			
			String confirmation = confirmPasswordEl.getValue();
			if(password == null || password.equals(confirmation)) {
				confirmPasswordEl.clearError();
			} else {
				confirmPasswordEl.setErrorKey("error.password.nomatch", null);
				allOk = false;
			}
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(passwordEl != null && passwordEl.isVisible()) {
			String newPassword = passwordEl.getValue();
			String login = getLogin();
			if(StringHelper.containsNonWhitespace(login)
					&& webDAVAuthManager.changePassword(getIdentity(), getIdentity(), login, newPassword)) {
				showInfo("pwdav.password.successful");
				toogleChangePassword(ureq);
			} else {
				showError("pwdav.password.failed");
			}
		}
	}
	
	private String getLogin() {
		List<Authentication> authentications = securityManager.getAuthentications(getIdentity());
		return getLogin(authentications);
	}
	
	private String getLogin(List<Authentication> authentications) {
		// 1) The WebDAV authentication user name
		for(Authentication auth : authentications) {
			if(WebDAVAuthManager.PROVIDER_WEBDAV.equals(auth.getProvider()) || WebDAVAuthManager.PROVIDER_HA1.equals(auth.getProvider())) {
				return auth.getAuthusername();
			}
		}
		
		// 2) Classic known authentication user name, preferred to OAuth which has cryptic user name
		for(Authentication auth : authentications) {
			if("OLAT".equals(auth.getProvider()) || LDAPAuthenticationController.PROVIDER_LDAP.equals(auth.getProvider())
					|| ShibbolethDispatcher.PROVIDER_SHIB.equals(auth.getProvider())) {
				return auth.getAuthusername();
			}
		}
		
		// 3) All but emails ones
		for(Authentication auth : authentications) {
			if(!WebDAVAuthManager.PROVIDER_WEBDAV_EMAIL.equals(auth.getProvider())
					&& !WebDAVAuthManager.PROVIDER_WEBDAV_INSTITUTIONAL_EMAIL.equals(auth.getProvider())
					&& !WebDAVAuthManager.PROVIDER_HA1_EMAIL.equals(auth.getProvider())
					&& !WebDAVAuthManager.PROVIDER_HA1_INSTITUTIONAL_EMAIL.equals(auth.getProvider())) {
				return auth.getAuthusername();
			}
		}
		
		if(securityModule.isIdentityNameAutoGenerated()
				&& StringHelper.containsNonWhitespace(getIdentity().getUser().getNickName())) {
			return getIdentity().getUser().getNickName();
		}
		return getIdentity().getName();
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		toogleChangePassword(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		toogleChangePassword(ureq);
	}
	
	private void toogleChangePassword(UserRequest ureq) {
		boolean visible = newButton.isVisible();
		newButton.setVisible(!visible);
		passwordStaticEl.setVisible(!visible);
		saveButton.setVisible(visible);
		cancelButton.setVisible(visible);
		rulesEl.setVisible(visible);
		passwordEl.setVisible(visible);
		confirmPasswordEl.setVisible(visible);
		
		Authentication auth = securityManager.findAuthentication(ureq.getIdentity(), WebDAVAuthManager.PROVIDER_WEBDAV, BaseSecurity.DEFAULT_ISSUER);
		String passwordPlaceholderKey = auth == null ? "pwdav.password.not_set" : "pwdav.password.set";
		String passwordPlaceholder = getTranslator().translate(passwordPlaceholderKey);
		passwordStaticEl.setValue(passwordPlaceholder);
		
		String buttonPlaceholderKey = auth == null ? "pwdav.password.new" : "pwdav.password.change";
		newButton.setI18nKey(buttonPlaceholderKey);
		
		List<Authentication> authentications = securityManager.getAuthentications(ureq.getIdentity());
		String usernames = getUsernames(authentications);
		usernamesStaticEl.setValue(usernames);
		
		flc.setDirty(true);
	}
}
