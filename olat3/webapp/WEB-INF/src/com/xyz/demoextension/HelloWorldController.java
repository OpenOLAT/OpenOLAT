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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br />
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br />
 * See the License for the specific language governing permissions and <br />
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br />
 * University of Zurich, Switzerland.
 * <p>
 */
package com.xyz.demoextension;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;


public class HelloWorldController extends BasicController {

	private VelocityContainer startPage;
	
	private String myString = "Hello World";
	private Link sayHelloLink;
	private Link sayHelloButtonXSmall;
	private Link sayHelloButtonSmall;
	private Link sayHelloButton;
	private Panel helloWorldPanel;
	

	public HelloWorldController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		// if you need WindowControl, you -MUST- use WindowControl w2 = getWindowControl(); do not use variable wControl!
		// the windowcontrol is used e.g. for displaying info or error messages or to
		// push components to the main window. See further down for an example
		
		//create a panel where content is pushed and taken away.
		helloWorldPanel = new Panel("Hello World Panel");
		
		// create a new VelocityContainer that display helloworld.html, has its translator
		// for any i18n specifics in the container and registers this HelloWorldController
		// as its event dispatcher
		startPage = createVelocityContainer("helloworld");
		// create a link. In the velocity template helloworld.html it's rendered by $r.render("say.hello"). "say.hello" is the
		// name of the component, the command and the i18n key.
		sayHelloLink = LinkFactory.createLink("say.hello", startPage, this);
		
		sayHelloButtonXSmall = LinkFactory.createButtonXSmall("say.hello.xsmall", startPage, this);
		sayHelloButtonSmall = LinkFactory.createButtonSmall("say.hello.small", startPage, this);
		sayHelloButton = LinkFactory.createButton("say.hello", startPage, this);
		
		//we pass a variable 
		startPage.contextPut("myContentVariable", myString );
		
		// display image via css
		// - put the image under _static/css/img
		// - put the css file under _static/css
		// - add to css file: .your_css_class { background: url(img/your_image.png) }
		// - add to your velocity-template: <div class="your_css_class"></div>
		JSAndCSSComponent demoext = new JSAndCSSComponent("demoext", this.getClass(), null, "demoext.css", true);
		startPage.put("demoext", demoext);
		
		// our velocity container will be the first component to display
		// when somebody decides to render the GUI of this controller.
		helloWorldPanel.setContent(startPage);
		
		putInitialPanel(helloWorldPanel);
	}
	
	/**
	 * This dispatches component events...
	 * 
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		// first check, which component this event comes from...
		if (source == sayHelloLink) {
			// ok, we have to say hello... do so.
			logInfo("Someone asked us to say hello... so we do.", null);
			//we say hello to the and display the username which is part of the user identity and stored in the user session
			getWindowControl().setInfo("Hi "+ureq.getIdentity().getName());
		} else if (source == sayHelloButtonXSmall) {
			logInfo("Someone asked us to say hello... so we do.", null);
			getWindowControl().setInfo("Hi "+ureq.getIdentity().getName()+" (ButtonXSmall)");
		} else if (source == sayHelloButtonSmall) {
			logInfo("Someone asked us to say hello... so we do.", null);
			getWindowControl().setInfo("Hi "+ureq.getIdentity().getName()+" (ButtonSmall)");
		} else if (source == sayHelloButton) {
			logInfo("Someone asked us to say hello... so we do.", null);
			getWindowControl().setInfo("Hi "+ureq.getIdentity().getName()+" (ButtonDefault)");
		}
	}

	/**
	 * This dispatches controller events...
	 * 
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		// at this time, we do not have any other controllers we'd like to listen for events to...
		
		// If you have a form or a table component in your velocity file the events (like clicking an element in the table)
		// this method gets called and the event can be handled
		
	}

	protected void doDispose() {
		// this is just to help the Java Garbage Collector or other stuff to clean up before it gets destroyed
		startPage = null;
	}


}
