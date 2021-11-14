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

import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
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
 * Initial date: 1 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserNickNameEditController extends FormBasicController {

	private TextElement nickNameEl;
	private MultipleSelectionElement changeProvidersEl;
	
	private Identity changeableIdentity;
	private List<UserAuthenticationRow> manageableAuthentications;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	
	public UserNickNameEditController(UserRequest ureq, WindowControl wControl, Identity changeableIdentity, List<UserAuthenticationRow> authentications) {
		super(ureq, wControl, Util.createPackageTranslator(UserManager.class, ureq.getLocale()));
		this.changeableIdentity = changeableIdentity;
		manageableAuthentications = getManageableProviders(authentications);
		initForm(ureq);
	}
	
	private List<UserAuthenticationRow> getManageableProviders(List<UserAuthenticationRow> auths) {
		return auths.stream()
			.filter(auth -> auth.getProvider() != null)
			.filter(auth -> auth.getProvider()
				.canChangeAuthenticationUsername(auth.getAuthentication().getProvider()))
			.collect(Collectors.toList());
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String nickName = changeableIdentity.getUser().getProperty(UserConstants.NICKNAME, getLocale());
		nickNameEl = uifactory.addTextElement("username", 255, nickName, formLayout);
		
		SelectionValues providers = new SelectionValues();
		providers.add(SelectionValues.entry("change.providers.on", translate("change.providers.on")));
		changeProvidersEl = uifactory.addCheckboxesHorizontal("change.providers", formLayout, providers.keys(), providers.values());
		changeProvidersEl.select("change.providers.on", true);
		changeProvidersEl.setVisible(!manageableAuthentications.isEmpty());
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		nickNameEl.clearError();
		if(!StringHelper.containsNonWhitespace(nickNameEl.getValue())) {
			nickNameEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(!isNickNameUnique(nickNameEl.getValue())) {
			Identity identity = securityManager.findIdentityByNickName(nickNameEl.getValue());
			String val = nickNameEl.getValue();
			if(identity != null) {
				val = userManager.getUserDisplayName(identity);
			}
			nickNameEl.setErrorKey("general.error.unique", new String[] { val } );
			allOk &= false;
		} else if(changeProvidersEl.isVisible() && changeProvidersEl.isSelected(0)){
			for(UserAuthenticationRow auth:manageableAuthentications) {
				AuthenticationProviderSPI provider = auth.getProvider();
				ValidationResult result = provider.validateAuthenticationUsername(nickNameEl.getValue(), changeableIdentity);
				if(!result.isValid()) {
					ValidationDescription descr = result.getInvalidDescriptions().get(0);
					String text = descr.getText(getLocale());
					nickNameEl.setErrorKey("error.username.invalid", new String[] { text });
					allOk &= false;
				}
			}
		}
		
		return allOk;
	}
	
	private boolean isNickNameUnique(String val) {
		Identity identity = securityManager.findIdentityByNickName(val);
		return identity == null || identity.equals(changeableIdentity);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String nickName = nickNameEl.getValue();
		changeableIdentity = securityManager.loadIdentityByKey(changeableIdentity.getKey());
		changeableIdentity.getUser().setProperty(UserConstants.NICKNAME, nickName);
		if(userManager.updateUserFromIdentity(changeableIdentity)) {
			if(changeProvidersEl.isVisible() && changeProvidersEl.isSelected(0)) {
				for(UserAuthenticationRow auth:manageableAuthentications) {
					auth.getProvider().changeAuthenticationUsername(auth.getAuthentication(), nickName);
				}
			}
		}
		dbInstance.commit();
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
