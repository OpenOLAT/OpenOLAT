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

package org.olat.core.gui.control.generic.docking;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.info.WindowControlInfo;
import org.olat.core.id.context.BusinessControl;

/**
 * Initial Date: 05.01.2006
 * Allows to undock/dock a controller out of its original screen part into a new browser window.
 * @author Felix Jost
 */
public class DockController extends BasicController {
	private static final String CMD_UNDOCK = "ud";

	private VelocityContainer mainVC;
	private Panel panel;
	private DockLayoutControllerCreatorCallback layoutCreator;
	
	Controller controller;
	DelegatingWControl delegWControl;
	private final boolean disposeWhenFloating;

	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param disposeWhenFloating if true, the floating window will be disposed when the controller where it was originally created is disposed. if false, the undocked window will live independently.
	 * @param controllerCreator the creator of the controller to be made undockable
	 * @param afterDockingCallback the callback to be called when docking process is finished or NULL
	 * @param the layout creator used to wrap the content controller
	 */
	public DockController(UserRequest ureq, WindowControl wControl, boolean disposeWhenFloating, ControllerCreator controllerCreator, DockLayoutControllerCreatorCallback layoutCreator) {
		super(ureq, wControl);
		this.disposeWhenFloating = disposeWhenFloating;
		this.layoutCreator = layoutCreator;
		mainVC = createVelocityContainer("docked");
		/*
		 * delegate window control to new basis window (clientside new browserwindow)
		 * -> take controller out of main window and set the controller into the new window
		 * -> info / warn / error message, push, pop is then relative to the new window
		 */
		delegWControl = new DelegatingWControl();
		delegWControl.setDelegate(getWindowControl());

		Controller c = controllerCreator.createController(ureq, delegWControl);
		// don't use auto dispose feature from basic controller, this is a special case
		this.controller = c;
		mainVC.put("controllerComp", c.getInitialComponent());		
		mainVC.contextPut("winid", "w" + mainVC.getDispatchID());
		
		panel = putInitialPanel(mainVC);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == mainVC) {
			if (event.getCommand().equals(CMD_UNDOCK)) {
				panel.setContent(null);
				// pop up in new browser window
				ControllerCreator undockControllerCreator = new ControllerCreator() {
					public Controller createController(UserRequest lureq,WindowControl lwControl) {
						delegWControl.setDelegate(lwControl);
						LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, delegWControl, null, null, controller.getInitialComponent(), null);
						layoutCtr.addDisposableChildController(controller); // cleanup on layout controller dispose
						return layoutCtr;
					}
				};
				
				ControllerCreator newWindowContent;
				if(layoutCreator != null){
					//wrap with layout
					newWindowContent = layoutCreator.createLayoutControllerCreator(ureq, undockControllerCreator);
				}else{
					//use default layout
					newWindowContent = undockControllerCreator; 					
				}
				openInNewBrowserWindow(ureq, newWindowContent);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		if (disposeWhenFloating) {
			if (controller != null) controller.dispose();
		} // else nothing to do
	}
	//fxdiff BAKS-7 Resume function
	public Controller getController() {
		return controller;
	}

}

class DelegatingWControl implements WindowControl {
	private WindowControl origWCon;

	void setDelegate(WindowControl delegWCon) {
		origWCon = delegWCon;
	}

	/**
	 * @see org.olat.core.gui.control.WindowControl#getWindowControlInfo()
	 */
	public WindowControlInfo getWindowControlInfo() {
		return origWCon.getWindowControlInfo();
	}

	/**
	 * @see org.olat.core.gui.control.WindowControl#makeFlat()
	 */
	public void makeFlat() {
		origWCon.makeFlat();
	}

	/**
	 * @see org.olat.core.gui.control.WindowControl#pop()
	 */
	public void pop() {
		origWCon.pop();
	}

	/**
	 * @see org.olat.core.gui.control.WindowControl#pushAsModalDialog(java.lang.String,
	 *      org.olat.core.gui.components.Component)
	 */
	public void pushAsModalDialog(Component comp) {
		origWCon.pushAsModalDialog(comp);
	}

	@Override
	public void pushAsCallout(Component comp, String targetId) {
		origWCon.pushAsCallout(comp, targetId);
	}

	/**
	 * @see org.olat.core.gui.control.WindowControl#pushToMainArea(org.olat.core.gui.components.Component)
	 */
	public void pushToMainArea(Component comp) {
		origWCon.pushToMainArea(comp);
	}

	/**
	 * @see org.olat.core.gui.control.WindowControl#setError(java.lang.String)
	 */
	public void setError(String string) {
		origWCon.setError(string);
	}

	/**
	 * @see org.olat.core.gui.control.WindowControl#setInfo(java.lang.String)
	 */
	public void setInfo(String string) {
		origWCon.setInfo(string);
	}

	/**
	 * @see org.olat.core.gui.control.WindowControl#setWarning(java.lang.String)
	 */
	public void setWarning(String string) {
		origWCon.setWarning(string);
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.WindowControl#getBusinessControl()
	 */
	public BusinessControl getBusinessControl() {
		return origWCon.getBusinessControl();
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.WindowControl#getWindowBackOffice()
	 */
	public WindowBackOffice getWindowBackOffice() {
		return origWCon.getWindowBackOffice();
	}

}