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

package org.olat.course.nodes.tu;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.nodes.TUCourseNode;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.tu.IframeTunnelController;
import org.olat.modules.tu.TunnelController;

/**
*  Description:<br>
*  is the controller for displaying contents using olat tunnel
*
* @author Felix Jost
* @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
*/
public class TURunController extends BasicController {

	private Controller startPage;
	private Link showButton;
	private TUCourseNode courseNode;
	private Panel main;	
	private ModuleConfiguration config;
	private CourseEnvironment courseEnv;	

	/**
 	 * Constructor for tunneling run controller
	 * @param wControl
	 * @param config The module configuration
	 * @param ureq The user request
	 * @param tuCourseNode The current course node
	 * @param cenv the course environment
	 */
	public TURunController(WindowControl wControl, ModuleConfiguration config, UserRequest ureq, TUCourseNode tuCourseNode, CourseEnvironment cenv) { 
		super(ureq, wControl);
		this.courseNode = tuCourseNode;
		this.config = config;
		this.courseEnv = cenv;
		
		main = new Panel("turunmain");
		if (config.getBooleanSafe(TUConfigForm.CONFIG_EXTERN, false)) {
			doStartPage(ureq);
		} else {
			doLaunch(ureq);
		}		
		putInitialPanel(main);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == showButton) {
			doLaunch(ureq);				
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == startPage) {
			if (event == Event.DONE_EVENT) {
				doLaunch(ureq);
			}
		}
	}
	
	private void doStartPage(UserRequest ureq) {		
		Controller startPageInner = new TUStartController(ureq, getWindowControl(), config);
		startPage = TitledWrapperHelper.getWrapper(ureq, getWindowControl(), startPageInner, courseNode, "o_tu_icon");
		listenTo(startPage);
		main.setContent(startPage.getInitialComponent());
	}

	private void doLaunch(UserRequest ureq) {
		boolean iniframe = config.getBooleanSafe(TUConfigForm.CONFIG_IFRAME);
		Controller controller;
		if (iniframe) {  
			// Do not dispose this controller if the course is closed...
			controller = new IframeTunnelController(ureq, getWindowControl(), config);
		} else {
			controller = new TunnelController(ureq, getWindowControl(), config);
		}
		listenTo(controller);
		
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, getWindowControl(), controller, courseNode, "o_tu_icon");
		main.setContent(ctrl.getInitialComponent());
	}
	
	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
    //child controller registered with listenTo gets disposed in BasicController
	}


}
