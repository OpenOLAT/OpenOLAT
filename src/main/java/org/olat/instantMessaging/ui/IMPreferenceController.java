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
* <p>
*/ 

package org.olat.instantMessaging.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.instantMessaging.ImPreferences;
import org.olat.instantMessaging.InstantMessagingService;

/**
 * Initial Date:  August 08, 2005
 *
 * @author Alexander Schneider
 */

public class IMPreferenceController extends BasicController {
	private VelocityContainer myContent;
	
	private Identity changeableIdentity;
	
	private IMPreferencesOnlineController onlineListForm;
	private IMPreferenceRosterController rosterForm;
	
	private final InstantMessagingService imService;
	
	/**
	 * Constructor for the change instant messaging controller
	 * @param ureq The user request
	 * @param wControl The current window controller
	 * @param changeableIdentity
	 */
	public IMPreferenceController(UserRequest ureq, WindowControl wControl, Identity changeableIdentity) {
		super(ureq, wControl);
		
		this.changeableIdentity = changeableIdentity;
		imService = CoreSpringFactory.getImpl(InstantMessagingService.class);

		myContent = createVelocityContainer("imsettings");
		
		
		ImPreferences imPrefs = imService.getImPreferences(changeableIdentity);
		
		onlineListForm = new IMPreferencesOnlineController(ureq, wControl, imPrefs);
		listenTo(onlineListForm);
		myContent.put("onlinelistform", onlineListForm.getInitialComponent());
		
		rosterForm = new IMPreferenceRosterController(ureq, wControl, imPrefs);
		listenTo(rosterForm);
		myContent.put("rosterform", rosterForm.getInitialComponent());

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
			  imService.updateImPreferences(changeableIdentity, true, true);
			   
				fireEvent(ureq, Event.DONE_EVENT);
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			} 
		} else if (source == rosterForm) {
			if (event == Event.DONE_EVENT) {
			   imService.updateImPreferences(changeableIdentity, true, true); 
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
