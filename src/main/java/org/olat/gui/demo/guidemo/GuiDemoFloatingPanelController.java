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

import org.olat.admin.user.UserSearchController;
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
import org.olat.core.gui.control.floatingresizabledialog.FloatingResizableDialogController;
import org.olat.core.gui.dev.controller.SourceViewController;

public class GuiDemoFloatingPanelController extends BasicController {
	
	private VelocityContainer panelVc = createVelocityContainer("panel");
	private VelocityContainer openerVc = createVelocityContainer("opener");
	private VelocityContainer localContent = createVelocityContainer("localContent");
	private Panel panel = new Panel("panel");
	private Link open;
	private Link open2;
	private Link contentLink;
	private FloatingResizableDialogController dialog;
	
	public GuiDemoFloatingPanelController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		panel.setContent(openerVc);
		open = LinkFactory.createLink("open", openerVc, this);
		open2 = LinkFactory.createLink("open2", openerVc, this);
		
		//add source view control
    Controller sourceview = new SourceViewController(ureq, wControl, this.getClass(), openerVc);
    openerVc.put("sourceview", sourceview.getInitialComponent());
		
		putInitialPanel(panel);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == open) {
			UserSearchController userSearch = new UserSearchController(ureq, getWindowControl(), true);
			dialog = new FloatingResizableDialogController(ureq, getWindowControl(), userSearch.getInitialComponent(),
					"Your title", null, 350, 350, 400, 200, true, false, false, null);
			dialog.addControllerListener(this);
			panelVc.put("panel", dialog.getInitialComponent());
			panel.setContent(panelVc);
		} else if (source == open2) {
			dialog = new FloatingResizableDialogController(ureq, getWindowControl(), localContent,
					"Your title", null, 350, 350, 400, 200, true, false, false, null);
			dialog.addControllerListener(this);
			panelVc.put("panel", dialog.getInitialComponent());
			contentLink = LinkFactory.createLink("link4", localContent, this);
			panel.setContent(panelVc);
		} else if (source == contentLink) {
			getWindowControl().setInfo("Congratulations! You won a trip to Lorem Ipsum.");
		}
		
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == dialog) {
			if (event == Event.DONE_EVENT) {
				panel.setContent(openerVc);
			}
		}
	}

}
