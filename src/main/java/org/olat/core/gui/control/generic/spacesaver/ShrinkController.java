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

package org.olat.core.gui.control.generic.spacesaver;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.util.Util;

/**
 * enclosing_type Description: <br>
 * this controller takes a component in its contructor and wraps a velocity
 * container around it with a single link/button (with a userdefined
 * displayname) which closes the dialog. <br>
 * Important: the method getMainComponent is overridden and throws an Exception,
 * since there is a different method to be used: activate(WindowController
 * wControl). This reason is the this controller is intended to be used only as
 * "a popup"/modal dialog (since it offers the 'close' button) and after
 * clicking that button, it should disappear by itself. Therefore you can only
 * use it in conjunction with a WindowsController.
 * 
 * @author Felix Jost
 */
public class ShrinkController extends DefaultController {
	private boolean isLarge;
	private Component shrinkcontent;

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(ShrinkController.class);

	private VelocityContainer myContent;

	/**
	 * @param ureq
	 * @param initiallyExpanded
	 * @param shrinkcontent
	 * @param title the name of the link to click (e.g "Details")
	 */
	public ShrinkController(boolean initiallyExpanded, Component shrinkcontent, String title) {
		super(null);
		this.shrinkcontent = shrinkcontent;
		isLarge = initiallyExpanded;
		shrinkcontent.setVisible(initiallyExpanded);
		myContent = new VelocityContainer("shrinkwrapper", VELOCITY_ROOT + "/index.html", null,	this);
		myContent.contextPut("title", title);
		
		myContent.put("shrinkcontent", shrinkcontent);
		setInitialComponent(myContent);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == myContent) { // links
			if (event.getCommand().equals("switch")) {
				isLarge = !isLarge;
				updateUI();
			}
		}
	}
	
	private void updateUI() {
		myContent.contextPut("islarge", isLarge? Boolean.TRUE : Boolean.FALSE);
		shrinkcontent.setVisible(isLarge);
	}

}