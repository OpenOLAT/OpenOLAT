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

package org.olat.core.gui.control;

import org.olat.core.commons.fullWebApp.LockResourceInfos;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public abstract class DefaultChiefController extends DefaultController implements ChiefController {
	private Window window;
	private WindowControl windowControl;
	private final ScreenMode screenMode = new ScreenMode();
	
	public DefaultChiefController() {
		super(null);
		// nothing to do
	}

	/**
	 * Gets the window.
	 * @return the window
	 */
	@Override
	public Window getWindow() {
		return window;
	}

	/**
	 * Sets the window.
	 * 
	 * @param window The window to set
	 */
	protected void setWindow(Window window) {
		this.window = window;
	}

	@Override
	public boolean wishAsyncReload(UserRequest ureq, boolean erase) {
		return false;
	}

	@Override
	public boolean wishReload(UserRequest ureq, boolean erase) {
		return false;
	}
	
	@Override
	public void resetReload() {
		//
	}

	@Override
	public ScreenMode getScreenMode() {
		return screenMode;
	}
	
	@Override
	public final LockResourceInfos getLockResourceInfos() {
		return null;
	}

	@Override
	public LockResourceInfos getLastUnlockedResourceInfos() {
		return null;
	}

	@Override
	public final void lockResource(OLATResourceable resource) {
		//
	}

	@Override
	public final void hardLockResource(LockResourceInfos lockInfos) {
		//
	}

	@Override
	public abstract void event(UserRequest ureq, Component source, Event event);

	@Override
	public void addControllerListener(ControllerEventListener el) {
		throw new AssertException("cannot listen to a chiefcontroller");
	}

	@Override
	protected void setInitialComponent(Component initialComponent) {
		throw new AssertException("please use getWindow().setContentPane() instead!");
	}

	@Override
	public Component getInitialComponent() {
		throw new AssertException("please use getWindow().getContentPane() instead!");
	}

	/**
	 * overrides the method in DefaultController since here we need the original WindowControl
	 * @see org.olat.core.gui.control.ChiefController#getWindowControl()
	 */
	@Override
	public WindowControl getWindowControl() {
		return windowControl;
	}

	protected void setWindowControl(WindowControl windowControl) {
		this.windowControl = windowControl;
	}
}