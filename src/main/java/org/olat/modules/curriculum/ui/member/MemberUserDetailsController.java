/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.member;

import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.user.UserInfoController;
import org.olat.user.UserInfoProfile;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberUserDetailsController extends UserInfoController {
	
	public static final String usageIdentifyer = AbstractMembersController.usageIdentifyer;
	
	private final Identity member;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;

	public MemberUserDetailsController(UserRequest ureq, WindowControl wControl, Form mainForm,
			Identity member, UserInfoProfileConfig profileConfig, UserInfoProfile profile) {
		super(ureq, wControl, mainForm, profileConfig, profile);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.member = member;
		initForm(ureq);
	}
	
	@Override
	protected void initFormItems(FormLayoutContainer itemsCont, Controller listener, UserRequest ureq) {
		super.initFormItems(itemsCont, listener, ureq);

		User user = member.getUser();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		for(UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
			String name = userPropertyHandler.getName();
			if(UserConstants.NICKNAME.equals(name) || UserConstants.EMAIL.equals(name)
					|| UserConstants.FIRSTNAME.equals(name) || UserConstants.LASTNAME.equals(name) ) {
				continue;
			}
			
			String value = userPropertyHandler.getUserProperty(user, getLocale());
			if(StringHelper.containsNonWhitespace(value)) {
				String i18nLabel = userPropertyHandler.i18nFormElementLabelKey();
				uifactory.addStaticTextElement(name, i18nLabel, value, itemsCont);
			}
		}
	}
}
