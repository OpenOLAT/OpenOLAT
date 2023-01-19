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
package org.olat.user.ui.admin.authentication;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
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
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.login.auth.AuthenticationProviderSPI;
import org.olat.login.validation.ValidationDescription;
import org.olat.login.validation.ValidationResult;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 août 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserAuthenticationEditController extends FormBasicController {
	
	private TextElement loginEl;
	
	private Authentication authentication;
	private final AuthenticationProviderSPI provider;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	
	public UserAuthenticationEditController(UserRequest ureq, WindowControl wControl, Authentication authentication,
			AuthenticationProviderSPI provider) {
		super(ureq, wControl, Util.createPackageTranslator(UserManager.class, ureq.getLocale()));
		this.provider = provider;
		this.authentication = authentication;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		uifactory.addStaticTextElement("provider", authentication.getProvider(), formLayout);
		loginEl = uifactory.addTextElement("login", "login", 500, authentication.getAuthusername(), formLayout);
		loginEl.setMandatory(true);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		loginEl.clearError();
		if(!StringHelper.containsNonWhitespace(loginEl.getValue())) {
			loginEl.setErrorKey("form.legende.mandatory");
			allOk = false;
		} else {
			ValidationResult result = provider.validateAuthenticationUsername(loginEl.getValue(), authentication.getIdentity());
			if(!result.isValid()) {
				ValidationDescription descr = result.getInvalidDescriptions().get(0);
				String text = descr.getText(getLocale());
				loginEl.setErrorKey("error.username.invalid", text);
				allOk = false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String newUsername = loginEl.getValue();
		String oldValue = authentication.getAuthusername();
		if(provider.changeAuthenticationUsername(authentication, newUsername)) {
			Identity identity = securityManager.loadIdentityByKey(authentication.getIdentity().getKey());
			User user = identity.getUser();
			if(oldValue.equals(user.getProperty(UserConstants.NICKNAME, getLocale()))) {
				user.setProperty(UserConstants.NICKNAME, newUsername);
				userManager.updateUser(identity, user);
			}	
		}
		dbInstance.commit();
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
