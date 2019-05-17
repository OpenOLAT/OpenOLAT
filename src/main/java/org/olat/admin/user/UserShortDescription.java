/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.admin.user;

import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Container for userProperty list, configurable in olat_userconfig.xml.
 * 
 * <P>
 * Initial Date:  15.01.2008 <br>
 * @author Lavinia Dumitrescu
 */
public class UserShortDescription extends BasicController {
	
	private static final String usageIdentifyer = UserShortDescription.class.getCanonicalName();

	private final VelocityContainer mainVC;
	private final boolean isAdministrativeUser;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;

	public UserShortDescription(UserRequest ureq, WindowControl wControl, Identity identity) {
		super(ureq, wControl);
		
		String usernameLabel = translate("table.user.login");
		//use the PropertyHandlerTranslator for the velocityContainer
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		mainVC = createVelocityContainer("userShortDescription");
		mainVC.setDomReplacementWrapperRequired(false); // we provide our own DOM replacement ID
		
		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);	
		boolean alreadyDefinedUsername = false;
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		for(UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
			if(UserConstants.USERNAME.equals(userPropertyHandler.getName())) {
				alreadyDefinedUsername = true;
			}
		}
		
		mainVC.contextPut("userPropertyHandlers", userPropertyHandlers);
		mainVC.contextPut("user", identity.getUser());			
		mainVC.contextPut("identityKey", identity.getKey());
		mainVC.contextPut("usernamePosition", "top");
		mainVC.contextPut("locale", getLocale());
		if(!alreadyDefinedUsername && (getIdentity().equals(identity) || isAdministrativeUser)) {
			mainVC.contextPut("username", identity.getName());
		}
		mainVC.contextPut("usernameLabel", usernameLabel);
		
		putInitialPanel(mainVC);
	}
	
	/**
	 * Set the position of the username / identity key if you
	 * have the permission to see them.
	 */
	public void setUsernameAtTop() {
		mainVC.contextPut("usernamePosition", "top");
	}
	
	/**
	 * Set the position of the username / identity key if you
	 * have the permission to see them.
	 */
	public void setUsernameAtBottom() {
		mainVC.contextPut("usernamePosition", "bottom");
	}

	@Override
	protected void doDispose() {
		// nothing to dispose		
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// No event expected		
	}
}
