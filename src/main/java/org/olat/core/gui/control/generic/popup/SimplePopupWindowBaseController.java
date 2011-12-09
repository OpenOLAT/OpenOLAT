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
package org.olat.core.gui.control.generic.popup;

import org.olat.core.commons.chiefcontrollers.controller.simple.SimpleBaseController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;

/**
 * Description:<br>
 * TODO: patrickb Class Description for SimplePopupWindowBaseController
 * 
 * <P>
 * Initial Date:  25.07.2007 <br>
 * @author patrickb
 */
public class SimplePopupWindowBaseController extends BasicController implements PopupBrowserWindowController {


	private ControllerCreator contentControllerCreator;
	private SimpleBaseController layoutController;

	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param contentControllerCreator
	 */
	public SimplePopupWindowBaseController(UserRequest ureq, WindowControl wControl, ControllerCreator contentControllerCreator) {
		super(ureq, wControl);
		this.contentControllerCreator = contentControllerCreator;
		this.layoutController = new SimpleBaseController(ureq,wControl);
		putInitialPanel(layoutController.getInitialComponent());
	}

	/**
	 * @see org.olat.core.gui.control.generic.popup.PopupBrowserWindow#open(org.olat.core.gui.UserRequest)
	 */
	public void open(UserRequest ureq) {
		Controller contentController = contentControllerCreator.createController(ureq, getWindowControl());
		layoutController.setContentController(contentController);
		ureq.getDispatchResult().setResultingWindow(getWindowControl().getWindowBackOffice().getWindow());
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		//
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.olat.core.gui.control.generic.popup.PopupBrowserWindow#getPopupWindowControl()
	 */
	public WindowControl getPopupWindowControl() {
		return getWindowControl();
	}

}
