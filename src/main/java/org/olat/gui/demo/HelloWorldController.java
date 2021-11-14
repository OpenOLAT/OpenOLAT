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

package org.olat.gui.demo;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.dev.controller.SourceViewController;
import org.olat.user.UserManager;

/**
 * 
 * Description:<br>
 * The famous "hello world" example the OLAT style
 * 
 * <P>
 * Initial Date: 29.08.2007 <br>
 * 
 * @author guido
 */
public class HelloWorldController extends BasicController {

	private VelocityContainer myContent = createVelocityContainer("helloworld");
	private VelocityContainer newsVc = createVelocityContainer("hello");

	private String myString = "Hello World!";
	private Panel panel = new Panel("panel");
	private Link link;
	private Link button;
	
	private final UserManager userManager;

	public HelloWorldController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		
		// we pass a variable to the velocity container
		// which can be accessed in our helloworld.html file
		myContent.contextPut("myContentVariable", myString);

		// links and buttons are also components
		link = LinkFactory.createLink("sayhello_i18n_key", myContent, this);
		button = LinkFactory.createButton("sayhello_i18n_key2", myContent, this);

		// panels are content holders that are initially empty and can be filled
		// with different contents
		// the panel itself stays in the layout and if you are in AJAX mode only the
		// new content gets sent and replaced by DOM replacement.
		myContent.put("panel", panel);
		panel.setContent(null);
		
		//add sourceview control for easy access of sourcecode from browser for learing reason
		Controller sourceView = new SourceViewController(ureq, wControl, this.getClass(), myContent);
		myContent.put("sourceview", sourceView.getInitialComponent());
		

		// our velocity contrainer will be the first component to display
		// when somebody decides to render the GUI of this controller.
		putInitialPanel(myContent);
	}

	/**
	 * This dispatches component events...
	 * 
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		// first check, which component this event comes from...
		if (source == link) {
			// OK, we have to say hello... do so.
			// logging writes a message to the olat.log file
			logInfo("Someone asked us to say hello... so we do.");
			// we say hello to the and display the userName which is part of the
			// user identity and stored in the user session
			String fullName = userManager.getUserDisplayName(getIdentity());
			getWindowControl().setInfo("Hi, your name is " + fullName);
		} else if (source == button) {
			// someone pressed the button
			panel.setContent(newsVc);
		}
	}

	/**
	 * This dispatches controller events...
	 * 
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
	// at this time, we do not have any other controllers we'd like to listen for
	// events to...

	// If you have a formular or a table component in your velocity file the
	// events (like clicking an element in the table)
	// this method gets called and the event can be handled
	}

	@Override
	protected void doDispose() {
	// use this method to finish thing at the end of the lifetime of this
	// controller
	// like closing files or connections...
	// this method does no get called automatically, you have to maintain the
	// controller chain
	// and make sure that you call dispose on the place where you create the
	// controller
	}

}
