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
* <p>
*/ 

package org.olat.user;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.servlets.WebDAVManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.ui.ChangeIMSettingsController;
import org.olat.ldap.LDAPLoginModule;
import org.olat.ldap.ui.LDAPAuthenticationController;
import org.olat.registration.DisclaimerController;
import org.olat.registration.RegistrationModule;

/**
 * Initial Date:  Jul 29, 2003
 *
 * @author Sabina Jeger
 * 
 */
public class PersonalSettingsController extends BasicController {
	
	private TabbedPane userConfig;
	
	private Controller ucsc;
	private Controller pwdc;
	private Controller hpec; 
	private Controller cimsc;
	private Controller pwdav;
	

	/**
	 * @param ureq
	 * @param wControl
	 */
	public PersonalSettingsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
			BaseSecurity mgr = BaseSecurityManager.getInstance();
			if (!mgr.isIdentityPermittedOnResourceable(
					ureq.getIdentity(), 
					Constants.PERMISSION_ACCESS, 
					OresHelper.lookupType(this.getClass())))
				throw new OLATSecurityException("Insufficient permissions to access PersonalSettingsController");

			userConfig = new TabbedPane("userConfig", ureq.getLocale());

			hpec = new ProfileAndHomePageEditController(ureq, getWindowControl(), (Identity)DBFactory.getInstance().loadObject(ureq.getIdentity()), false);
			listenTo(hpec);
			userConfig.addTab(translate("tab.profile"), hpec.getInitialComponent());
			
			ucsc = new ChangePrefsController(ureq, getWindowControl(), (Identity)DBFactory.getInstance().loadObject(ureq.getIdentity()));
			listenTo(ucsc);	
			userConfig.addTab(translate("tab.prefs"), ucsc.getInitialComponent());
			
			if(canChangePassword()) {
				pwdc = new ChangePasswordController(ureq, getWindowControl());
				listenTo(pwdc);
				userConfig.addTab(translate("tab.pwd"), pwdc.getInitialComponent());
			}
			
			if(WebDAVManager.getInstance().isEnabled()) {
				pwdav = new WebDAVPasswordController(ureq, getWindowControl());
				userConfig.addTab(translate("tab.pwdav"), pwdav.getInitialComponent());
			}
			
			if(InstantMessagingModule.isEnabled()){
				cimsc = new ChangeIMSettingsController(ureq, getWindowControl(), (Identity)DBFactory.getInstance().loadObject(ureq.getIdentity()));
				listenTo(cimsc);
				userConfig.addTab(translate("tab.im"), cimsc.getInitialComponent());
			}

			// Show read only display of disclaimer so user sees what he accepted if disclaimer enabled
			if (RegistrationModule.isDisclaimerEnabled()) {
				Controller disclaimerCtr = new DisclaimerController(ureq, getWindowControl(), true);
				listenTo(disclaimerCtr);
				userConfig.addTab(translate("tab.disclaimer"), disclaimerCtr.getInitialComponent());
			}
			
			putInitialPanel(userConfig);
			
			logDebug("PersonalSettingsController constructed, set velocity page to index.html", PersonalSettingsController.class.getSimpleName());	
	}
	
	private boolean canChangePassword() {
		//check if LDAP is enabled + cannot propagate password on LDAP Server
		if(LDAPLoginModule.isLDAPEnabled() && !LDAPLoginModule.isPropagatePasswordChangedOnLdapServer()) {
			//check if the user has a LDAP Authentication 
			Authentication auth = BaseSecurityManager.getInstance().findAuthentication(getIdentity(), LDAPAuthenticationController.PROVIDER_LDAP);
			return auth == null;//if not in LDAP -> can change his password
		}
		return true;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// nothing to do here.
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		// nothing to be done
	}
		
	/** 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		//
	}
}