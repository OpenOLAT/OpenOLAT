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
* Initial code contributed and copyrighted by<br>
* JGS goodsolutions GmbH, http://www.goodsolutions.ch
* <p>
*/
package org.olat.gui.demo.guidemo.error;

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
import org.olat.core.logging.AssertException;

/**
 * Description:<br>
 * demo controller which can deliberately cause different kind of errors.<br>
 * mainly useful for testing error handling in standard and ajax-mode.<br>
 * 
 * 
 * @author Felix Jost
 */
public class ErrorDemoController extends BasicController {
	private VelocityContainer myContent;
	private Panel mainPanel;

	private Link npeLink;
	private Link assertLink;
	private Link renderLink;

	/**
	 * @param ureq
	 * @param wControl
	 */
	public ErrorDemoController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		myContent = createVelocityContainer("index");
		npeLink = LinkFactory.createLink("command.npe", myContent, this);
		assertLink = LinkFactory.createLink("command.assert", myContent, this);
		renderLink = LinkFactory.createLink("command.render", myContent, this);
		mainPanel = new Panel("homesitepanel");
		mainPanel.setContent(myContent);
		
		//add source view control
	    Controller sourceview = new SourceViewController(ureq, wControl, this.getClass(), myContent);
	    myContent.put("sourceview", sourceview.getInitialComponent());
		
		putInitialPanel(mainPanel);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == npeLink){
			// cause a nullpointer exception
			String s = new String();
			s = null;
			getWindowControl().setInfo("get class of " + s.getClass().getName());
		} else if (source == assertLink){
			// create a assertexception sample
			try {
				// cause a nullpointer exception
				String s = new String();
				s = null;
				Class c = s.getClass();
				getWindowControl().setInfo("get class of "+c.getName());
			} catch (Exception e) {
				throw new AssertException("this is an assertException with a stacktrace from a NPE", e);
			}
		} else if (source == renderLink){
			myContent.contextPut("obj", this);
		}
	}

	public void doit() {
		throw new AssertException("called while rendering, throwing an assertexception now");
	}
	
	protected void event(UserRequest ureq, Controller source, Event event) {
		// nothing to do
	}
}
