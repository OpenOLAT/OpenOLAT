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
	private TextElement passwordEl;
	private TextElement confirmPasswordEl;
	private StaticTextElement usernamesStaticEl;
	private StaticTextElement passwordStaticEl;
	private FormLayoutContainer accessDataFlc;
	private FormLayoutContainer buttonGroupLayout;
	
	@Autowired
	private UserModule userModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private WebDAVAuthManager webDAVAuthManager;
	
	public WebDAVPasswordController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "pwdav", Util.createPackageTranslator(FolderRunController.class, ureq.getLocale()));
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("pwdav.title");
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutContainer = (FormLayoutContainer)formLayout;
			layoutContainer.contextPut("webdavhttp", FolderManager.getWebDAVHttp());
			layoutContainer.contextPut("webdavhttps", FolderManager.getWebDAVHttps());
			
			accessDataFlc = FormLayoutContainer.createDefaultFormLayout("flc_access_data", getTranslator());
			layoutContainer.add(accessDataFlc);

			boolean hasOlatToken = false;
			boolean hasWebDAVToken = false;
			List<Authentication> authentications = securityManager.getAuthentications(ureq.getIdentity());
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

				passwordEl = uifactory.addPasswordElement("pwdav.password.2", "pwdav.password", 64, "", accessDataFlc);
				passwordEl.setVisible(false);
				passwordEl.setMandatory(true);
				confirmPasswordEl = uifactory.addPasswordElement("pwdav.password.confirm", "pwdav.password.confirm", 64, "", accessDataFlc);
				confirmPasswordEl.setVisible(false);
				confirmPasswordEl.setMandatory(true);

				buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
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
		StringBuilder sb = new StringBuilder();
		sb.append(getIdentity().getName());
		if(userModule.isEmailUnique()) {
			if(StringHelper.containsNonWhitespace(getIdentity().getUser().getEmail())) {
				sb.append(", ").append(getIdentity().getUser().getEmail());
			}
			if(StringHelper.containsNonWhitespace(getIdentity().getUser().getInstitutionalEmail())) {
				sb.append(", ").append(getIdentity().getUser().getInstitutionalEmail());
			}
		}

		for(Authentication auth : authentications) {
			if(WebDAVAuthManager.PROVIDER_WEBDAV.equals(auth.getProvider())
					|| WebDAVAuthManager.PROVIDER_WEBDAV_EMAIL.equals(auth.getProvider())
					|| WebDAVAuthManager.PROVIDER_WEBDAV_INSTITUTIONAL_EMAIL.equals(auth.getProvider())
					|| WebDAVAuthManager.PROVIDER_HA1.equals(auth.getProvider())
					|| WebDAVAuthManager.PROVIDER_HA1_EMAIL.equals(auth.getProvider())
					|| WebDAVAuthManager.PROVIDER_HA1_INSTITUTIONAL_EMAIL.equals(auth.getProvider())) {
				String authUsername = auth.getAuthusername();
				if(sb.indexOf(authUsername) < 0) {
					sb.append(", ").append(authUsername);
				}
			}
		}
		
		return sb.toString();		
	}
	
	@Override
	protected void doDispose() {
		//auto-disposed
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		if(passwordEl.isVisible()) {
			String password = passwordEl.getValue();
			boolean valid = UserManager.getInstance().syntaxCheckOlatPassword(passwordEl.getValue());
			if(StringHelper.containsNonWhitespace(password) && valid) {
				passwordEl.clearError();
			} else if (!valid){
				passwordEl.setErrorKey("form.checkPassword", null);
				allOk = false;
			} else {
				passwordEl.setErrorKey("error.password.empty", null);
				allOk = false;
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
			if(webDAVAuthManager.changePassword(ureq.getIdentity(), ureq.getIdentity(), newPassword)) {
				showInfo("pwdav.password.successful");
				toogleChangePassword(ureq);
			} else {
				showError("pwdav.password.failed");
			}
		}
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
		passwordEl.setVisible(visible);
		confirmPasswordEl.setVisible(visible);
		
		Authentication auth = securityManager.findAuthentication(ureq.getIdentity(), WebDAVAuthManager.PROVIDER_WEBDAV);
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
