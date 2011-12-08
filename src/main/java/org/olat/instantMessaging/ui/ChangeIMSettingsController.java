/**
* OLAT - Online Learning and Training<br />
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br />
* you may not use this file except in compliance with the License.<br />
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br />
* software distributed under the License is distributed on an "AS IS" BASIS, <br />
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br />
* See the License for the specific language governing permissions and <br />
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br />
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.instantMessaging.ui;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.instantMessaging.ClientManager;
import org.olat.instantMessaging.ImPreferences;
import org.olat.instantMessaging.ImPrefsManager;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.user.UserManager;

/**
 * Initial Date:  August 08, 2005
 *
 * @author Alexander Schneider
 */

public class ChangeIMSettingsController extends BasicController {
	private VelocityContainer myContent;
	
	private Identity changeableIdentity;
	
	private OnlineListForm onlineListForm;
	private RosterForm rosterForm;
	
	private ImPrefsManager ipm;
	
	/**
	 * Constructor for the change instant messaging controller
	 * @param ureq The user request
	 * @param wControl The current window controller
	 * @param changeableIdentity
	 */
	public ChangeIMSettingsController(UserRequest ureq, WindowControl wControl, Identity changeableIdentity) {
		super(ureq, wControl);
		
		this.changeableIdentity = changeableIdentity;

		myContent = createVelocityContainer("imsettings");
		
		ipm = ImPrefsManager.getInstance();
		ImPreferences imPrefs = ipm.loadOrCreatePropertiesFor(changeableIdentity);
		
		onlineListForm = new OnlineListForm(ureq, wControl, imPrefs);
		listenTo(onlineListForm);
		myContent.put("onlinelistform", onlineListForm.getInitialComponent());
		
		rosterForm = new RosterForm(ureq, wControl, imPrefs);
		listenTo(rosterForm);
		myContent.put("rosterform", rosterForm.getInitialComponent());
		//fxdiff: hide external server info. see FXOLAT-46
		myContent.contextPut("hideExternalClientInfo", InstantMessagingModule.getAdapter().getConfig().isHideExternalClientInfo());
		
		myContent.contextPut("chatusername", InstantMessagingModule.getAdapter().getUserJid(changeableIdentity.getName()));
		Authentication auth = BaseSecurityManager.getInstance().findAuthentication(changeableIdentity, ClientManager.PROVIDER_INSTANT_MESSAGING);
		if (auth == null) {
			// somehow this is a messed up user. happens sometimes with the default users when IM server is not running at first startup
			logError("Could not find authentication for identity::" + changeableIdentity.getName() + " and provider::" + ClientManager.PROVIDER_INSTANT_MESSAGING + "; Please fix this users Instant Messaging password manually", null);
		} else {
			myContent.contextPut("password", auth.getCredential());
		}
		
		putInitialPanel(myContent);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}
		
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == onlineListForm) {
			if (event == Event.DONE_EVENT) {
			    
			    changeableIdentity = (Identity)DBFactory.getInstance().loadObject(changeableIdentity);

					ImPreferences imPrefs = ipm.loadOrCreatePropertiesFor(changeableIdentity);
			    onlineListForm.updateImPreferencesFromFormData(imPrefs);
			    ipm.updatePropertiesFor(changeableIdentity, imPrefs);
			    
			    UserManager um = UserManager.getInstance();
			    um.updateUserFromIdentity(changeableIdentity);
			   
				fireEvent(ureq, Event.DONE_EVENT);
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			} 
		} else if (source == rosterForm) {
			if (event == Event.DONE_EVENT) {
					ImPreferences imPrefs = ipm.loadOrCreatePropertiesFor(changeableIdentity);
			    rosterForm.updateImPreferencesFromFormData(imPrefs);
			    ipm.updatePropertiesFor(changeableIdentity, imPrefs);
			    fireEvent(ureq, Event.DONE_EVENT);
			} else if (event == Event.CANCELLED_EVENT) {
				// Form is cancelled
				fireEvent(ureq, Event.CANCELLED_EVENT);
			} 
		}
	}
	
	protected void doDispose() {
		//
	}

}
