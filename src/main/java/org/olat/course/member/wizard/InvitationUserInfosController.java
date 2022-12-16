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
package org.olat.course.member.wizard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Invitation;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.course.member.wizard.InvitationContext.TransientInvitation;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Only for a single user.
 * 
 * Initial date: 5 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationUserInfosController extends StepFormBasicController {
	
	private final String USERPROPERTIES_FORM_IDENTIFIER = Invitation.class.getName();
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	
	private final TransientInvitation transientInvitation;
	private Map<String,FormItem> propFormItems = new HashMap<>();
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	public InvitationUserInfosController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, TransientInvitation transientInvitation) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.transientInvitation = transientInvitation;
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USERPROPERTIES_FORM_IDENTIFIER, isAdministrativeUser);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_external_user_data");
		
		User existingUser = transientInvitation.getIdentity() == null ? null : transientInvitation.getIdentity().getUser();
		if(existingUser != null && !transientInvitation.isIdentityInviteeOnly()) {
			setFormWarning("warn.user.already.exists");
		}
		
		boolean first = true;
		
		// Add all available user fields to this form
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler != null) {
				FormItem fi = userPropertyHandler.addFormItem(getLocale(), existingUser, USERPROPERTIES_FORM_IDENTIFIER, false, formLayout);
				fi.setElementCssClass("o_sel_user_" + userPropertyHandler.getName());
				propFormItems.put(userPropertyHandler.getName(), fi);
				
				if(first) {
					fi.setFocus(true);
					first = false;
				}
				if(existingUser != null) {
					fi.setEnabled(false);
				} else if(fi instanceof TextElement) {
					if(UserConstants.EMAIL.equals(userPropertyHandler.getName())) {
						((TextElement)fi).setValue(transientInvitation.getEmail());
						fi.setEnabled(false);
					} else if(UserConstants.FIRSTNAME.equals(userPropertyHandler.getName())) {
						((TextElement)fi).setValue(transientInvitation.getFirstName());
					} else if(UserConstants.LASTNAME.equals(userPropertyHandler.getName())) {
						((TextElement)fi).setValue(transientInvitation.getLastName());
					}
				}
			}
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		Identity identity = transientInvitation.getIdentity();
		User user = identity == null ? null : identity.getUser();
	
		// validate special rules for each user property
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			//we assume here that there are only textElements for the user properties
			FormItem formItem = flc.getFormComponent(userPropertyHandler.getName());
			formItem.clearError();
			if(!userPropertyHandler.isValid(user, formItem, null) || formItem.hasError()) {
				allOk &= false;
			}
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			
			String propName = userPropertyHandler.getName();
			FormItem ui = propFormItems.get(propName);
			String uiValue = userPropertyHandler.getStringValue(ui);
			if(UserConstants.FIRSTNAME.equals(propName)) {
				transientInvitation.setFirstName(uiValue);
			} else if(UserConstants.LASTNAME.equals(propName)) {
				transientInvitation.setLastName(uiValue);
			} else if(!UserConstants.EMAIL.equals(propName)) {
				transientInvitation.getAdditionalInfos().getUserAttributes().put(propName, uiValue);
			}
		}
		fireEvent (ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
