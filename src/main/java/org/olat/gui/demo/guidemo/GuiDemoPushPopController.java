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

package org.olat.gui.demo.guidemo;

import java.util.Stack;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

public class GuiDemoPushPopController extends BasicController {
	
	private final VelocityContainer vcMain;
	private final Stack<Component> windowStack = new Stack<>();
	private final Link pushButton;
	private final Link popButton;
	
	public GuiDemoPushPopController(UserRequest ureq, WindowControl wControl) {
		super(ureq,wControl);
		
		vcMain = this.createVelocityContainer("guidemo-pushpop");
		pushButton = LinkFactory.createButton("guidemo.window.control.push", vcMain, this);
		popButton = LinkFactory.createButton("guidemo.window.control.pop", vcMain, this);
		
		vcMain.contextPut("stack", getStackHTMLRepresentation());		
		LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, vcMain, null);
		listenTo(layoutCtr);
		
		this.putInitialPanel(layoutCtr.getInitialComponent());
	}

	public void event(UserRequest ureq, Component source, Event event) {
		if (source == pushButton){			
			VelocityContainer container = this.createVelocityContainer("guidemo-pushpop");
			LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, container, null);
			listenTo(layoutCtr);
			windowStack.push(layoutCtr.getInitialComponent());
			
			container.put("guidemo.window.control.push", pushButton);
			container.put("guidemo.window.control.pop", popButton);
			container.contextPut("stack", getStackHTMLRepresentation());
			getWindowControl().pushToMainArea(container);
		}
		else if (source == popButton){
			if (windowStack.isEmpty()) {
				fireEvent(ureq, Event.DONE_EVENT);
			} else {
				getWindowControl().pop();
				windowStack.pop();
			}
		}
	}

	private String getStackHTMLRepresentation() {
		StringBuilder result = new StringBuilder();
		result.append("Current window stack:<br /><br />");
		for (int i = windowStack.size(); i > 0; i--) {
			Component component = windowStack.get(i-1);
			result.append("Stack position " + i + ": " + component.getComponentName() + "<br />");
		}
		return result.toString();
	}
}
