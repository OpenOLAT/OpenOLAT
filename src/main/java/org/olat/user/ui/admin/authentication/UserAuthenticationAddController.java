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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.login.auth.AuthenticationProviderSPI;
import org.olat.login.validation.ValidationDescription;
import org.olat.login.validation.ValidationResult;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserAuthenticationAddController extends FormBasicController {
	
	private TextElement loginEl;
	private SingleSelection providerEl;
	
	private final Identity changeableIdentity;
	private final Map<String, AuthenticationProviderSPI> providers;
	private final Map<String, AuthenticationProviderSPI> nameToProviders = new HashMap<>();
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	
	public UserAuthenticationAddController(UserRequest ureq, WindowControl wControl, Identity changeableIdentity) {
		super(ureq, wControl, Util.createPackageTranslator(UserManager.class, ureq.getLocale()));
		this.changeableIdentity = changeableIdentity;
		providers = CoreSpringFactory.getBeansOfType(AuthenticationProviderSPI.class);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormWarning("warning.add.authentication");
		
		SelectionValues providersKeysValues = new SelectionValues();
		for(AuthenticationProviderSPI provider:providers.values()) {
			List<String> providerNames = provider.getProviderNames();
			for(String providerName:providerNames) {
				if(provider.canAddAuthenticationUsername(providerName)) {
					providersKeysValues.add(SelectionValues.entry(providerName, providerName));
					nameToProviders.put(providerName, provider);
				}
			}
		}
		providerEl = uifactory.addDropdownSingleselect("form.provider", "form.provider", formLayout,
				providersKeysValues.keys(), providersKeysValues.values());
		
		loginEl = uifactory.addTextElement("login", "login", 500, "", formLayout);
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
		providerEl.clearError();
		if(!providerEl.isOneSelected()) {
			providerEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		if(!StringHelper.containsNonWhitespace(loginEl.getValue())) {
			loginEl.setErrorKey("form.legende.mandatory", null);
			allOk = false;
		} else if(providerEl.isOneSelected()) {
			String provider = providerEl.getSelectedKey();
			allOk &= validateAuthentication(provider, loginEl.getValue());
		}
		
		return allOk;
	}
	
	private boolean validateAuthentication(String provider, String login) {
		Authentication auth = securityManager
				.findAuthenticationByAuthusername(login, provider, BaseSecurity.DEFAULT_ISSUER);
		if(auth != null) {
			providerEl.setErrorKey("error.auth.exists", false, provider, login);
			return false;
		}

		auth = securityManager.findAuthentication(changeableIdentity, provider, BaseSecurity.DEFAULT_ISSUER);
		if(auth != null) {
			providerEl.setErrorKey("error.provider.exists", false, provider, login);
			return false;
		}
		
		AuthenticationProviderSPI providerSpi = nameToProviders.get(provider);
		if(providerSpi == null) {
			providerEl.setErrorKey("form.legende.mandatory", null);
			return false;
		}
		
		ValidationResult result = providerSpi.validateAuthenticationUsername(login, changeableIdentity);
		if(!result.isValid()) {
			ValidationDescription descr = result.getInvalidDescriptions().get(0);
			String text = descr.getText(getLocale());
			loginEl.setErrorKey("error.username.invalid", new String[] { text });
			return false;
		}
		return true;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String provider = providerEl.getSelectedKey();
		String username = loginEl.getValue();
		securityManager.createAndPersistAuthentication(changeableIdentity, provider, BaseSecurity.DEFAULT_ISSUER, username, null, null);
		
		dbInstance.commit();
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
