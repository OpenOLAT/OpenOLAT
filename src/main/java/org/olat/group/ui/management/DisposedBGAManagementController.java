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
package org.olat.group.ui.management;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * Description:<br>
 * TODO: patrickb Class Description for DisposedBGAManagementController
 * 
 * <P>
 * Initial Date:  27.04.2008 <br>
 * @author patrickb
 */
class DisposedBGAManagementController extends BasicController {

	private Link closeLink;
	private BGManagementController managementController;

	protected DisposedBGAManagementController(UserRequest ureq, WindowControl control, BGManagementController managementController) {
		super(ureq, control);
		VelocityContainer initialContent = createVelocityContainer("disposedbgmanagement");
		closeLink = LinkFactory.createButton("bgmanagement.disposed.command.close", initialContent, this);
		putInitialPanel(initialContent);
		this.managementController = managementController;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
	// TODO Auto-generated method stub

	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == closeLink){
			//lock is already released in doDispose() of managementController
			//the way to remove controller correctly is to send the DoneEvent, but 
			//source of DoneEvent must be the disposed BGManagementController Instance
			//
			//this way of coupling is only 'allowed' for Controller and its
			//DisposedController !!
			managementController.fireDoneEvent(ureq);
			dispose();
		}

	}

}
