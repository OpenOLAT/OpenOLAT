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

package org.olat.core.gui.control.generic.clone;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.logging.Tracing;

/**
 * Initial Date: 05.01.2006
 * 
 * @author Felix Jost
 */
public class CloneController extends BasicController{
	private static final Logger log = Tracing.createLoggerFor(CloneController.class);

	private static final String CMD_CLONE = "cl";
	private VelocityContainer mainVC;

	private CloneableController readyToCloneC;
	private CloneLayoutControllerCreatorCallback layoutCreator;

	/**
	 * @param ureq UserRequest
	 * @param wControl windowControl
	 * @param readyToCloneC the controller which will be cloned when the user
	 *          shows the "open in popup" button/icon
	 * @param useMinimalLayout if true, the popupwindow will only have a "close"
	 *          icon, if false: the popup window will be a normal popup window
	 *          with olat headers/footers
	 * @param the layout creator used to wrap the content controller
	 */
	public CloneController(UserRequest ureq, WindowControl wControl, CloneableController readyToCloneC, CloneLayoutControllerCreatorCallback layoutCreator) {
		super(ureq, wControl);
		this.readyToCloneC = readyToCloneC;
		this.layoutCreator = layoutCreator;

		mainVC = createVelocityContainer("offerclone");
		mainVC.put("cloneableCC", readyToCloneC.getInitialComponent());

		mainVC.contextPut("winid", "w"+mainVC.getDispatchID());
		putInitialPanel(mainVC);
	}


	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == mainVC) {
			if (event.getCommand().equals(CMD_CLONE)) { // clone request
				ControllerCreator cloneControllerCreator = (lureq, lwControl) -> {
					return readyToCloneC.cloneController(lureq, lwControl);					
				};
				
				ControllerCreator newWindowContent;
				if(layoutCreator != null){
					//wrap with layout
					newWindowContent = layoutCreator.createLayoutControllerCreator(ureq, cloneControllerCreator);
				}else{
					//use default layout
					newWindowContent = cloneControllerCreator; 					
				}
				//open in new window
				openInNewBrowserWindow(ureq, newWindowContent);
			}
		}
	}

	@Override
	protected void doDispose() {
		// delete the initial controller, but -not- the clones (they appear in a
		// independent browser window)
		if (readyToCloneC != null) readyToCloneC.dispose();
	}

}
