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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.breadcrumb.BreadCrumbController;
import org.olat.core.gui.control.generic.breadcrumb.CrumbBasicController;
import org.olat.core.gui.control.generic.breadcrumb.CrumbController;
import org.olat.core.gui.dev.controller.SourceViewController;

/**
 * Description:<br>
 * This demo shown you how you can use the bread crumb controller to build
 * simple bread crumb navigation. Bread crumb is good to use when the user can
 * start a workflow that runs within your parent workflow and maybe want to go
 * back.
 * <p>
 * Don't use it to build wizards, see the steps controller for this
 * <P>
 * Initial Date: 08.09.2008 <br>
 * 
 * @author gnaegi
 */
public class GuiDemoBreadCrumbController extends BasicController {
	private BreadCrumbController breadCrumbCtr;
	private VelocityContainer content;

	/**
	 * Constructor for a demo of the bread crumb controller
	 * @param ureq
	 * @param control
	 */
	public GuiDemoBreadCrumbController(UserRequest ureq, WindowControl control) {
		super(ureq, control);
		content = createVelocityContainer("breadcrump");
		// init bread crumb controller and add first element
		breadCrumbCtr = new BreadCrumbController(ureq, control);
		int level = 0;
		CrumbController ctr = new GuiDemoBreadCrumbContentController(ureq, getWindowControl(), level);
		breadCrumbCtr.activateFirstCrumbController(ctr);
		content.put("breadcrump", breadCrumbCtr.getInitialComponent());
		
		//add source view control
    Controller sourceview = new SourceViewController(ureq, control, this.getClass(), content);
    content.put("sourceview", sourceview.getInitialComponent());
		
		putInitialPanel(content);
	}
	
	@Override
	protected void doDispose() {
		if (breadCrumbCtr != null) {
			breadCrumbCtr.dispose();
			breadCrumbCtr = null;
		}
        super.doDispose();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// no events to catch
	}


	/**
	 * Inner class that implements the actual content.
	 * Description:<br>
	 * <P>
	 * Initial Date:  08.09.2008 <br>
	 * @author gnaegi
	 */
	private class GuiDemoBreadCrumbContentController extends CrumbBasicController {
		private int crumbLevel;
		private Link createNewLink, removeCurrentLink;
		private VelocityContainer main = createVelocityContainer("guidemo-breadcrumb");
		
		protected GuiDemoBreadCrumbContentController(UserRequest ureq, WindowControl control, int crumbLevel) {
			super(ureq, control);
			this.crumbLevel = crumbLevel;
			main.contextPut("level", this.crumbLevel);
			createNewLink = LinkFactory.createButton("GuiDemoBreadCrumbController.button.add", main, this);
			if (crumbLevel != 0)	removeCurrentLink = LinkFactory.createButton("GuiDemoBreadCrumbController.button.remove", main, this);
			putInitialPanel(main);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if (source.equals(createNewLink)) {
				CrumbController ctr = new GuiDemoBreadCrumbContentController(ureq, getWindowControl(), this.crumbLevel + 1);
				activateAndListenToChildCrumbController(ctr);
			}
			else if (source.equals(removeCurrentLink)) {
				removeFromBreadCrumbPathAndDispose();
			}
		}

		@Override
		public String getCrumbLinkText() {
			return "crumb " + crumbLevel;
		}

		@Override
		public String getCrumbLinkHooverText() {
			return "click here to to go crumb " + 1;
		}
	}
}