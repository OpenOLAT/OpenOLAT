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
package org.olat.group.ui.edit;

import org.olat.collaboration.CollaborationToolsSettingsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.group.BusinessGroup;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupToolsController extends BasicController {
	
	private final VelocityContainer mainVC;
	private final CollaborationToolsSettingsController toolsController;
	
	public BusinessGroupToolsController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("tab_bgCollabTools");
		toolsController = new CollaborationToolsSettingsController(ureq, getWindowControl(), businessGroup);
		// we are listening on CollaborationToolsSettingsController events
		// which are just propagated to our attached controllerlistener...
		// e.g. the BusinessGroupMainRunController, updating the MenuTree
		// if a CollaborationToolsSetting has changed... so far this means
		// enabling/disabling a Tool within the tree.
		listenTo(toolsController);
		mainVC.put("collabTools", toolsController.getInitialComponent());
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == toolsController) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}
}
