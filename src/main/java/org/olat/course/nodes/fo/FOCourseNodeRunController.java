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

package org.olat.course.nodes.fo;

import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.docking.DockController;
import org.olat.core.gui.control.generic.docking.DockLayoutControllerCreatorCallback;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.title.TitledWrapperController;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.course.CourseFactory;
import org.olat.course.nodes.FOCourseNode;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumCallback;
import org.olat.modules.fo.ForumUIFactory;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Initial Date: Apr 22, 2004
 * 
 * @author gnaegi
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class FOCourseNodeRunController extends BasicController implements Activateable2 {

	private DockController dockC;
	private FOCourseNode courseNode;
	private Panel main;
	private CourseEnvironment courseEnv;
	private Forum forum;
	private ForumCallback foCallback;
	private Link showButton;

	/**
	 * Constructor for a forum course building block runtime controller
	 * 
	 * @param ureq The user request
	 * @param userCourseEnv
	 * @param wContr The current window controller
	 * @param forum The forum to be displayed
	 * @param foCallback The forum security callback
	 * @param foCourseNode The current course node
	 */
	public FOCourseNodeRunController(UserRequest ureq, UserCourseEnvironment userCourseEnv, WindowControl wControl, Forum forum,
			ForumCallback foCallback, FOCourseNode foCourseNode) {
		super(ureq, wControl);
		this.courseNode = foCourseNode;
		this.courseEnv = userCourseEnv.getCourseEnvironment();
		this.forum = forum;
		this.foCallback = foCallback;
		// set logger on this run controller
		addLoggingResourceable(LoggingResourceable.wrap(foCourseNode));

		main = new Panel("forunmain");
		doLaunch(ureq);
		putInitialPanel(main);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == showButton) {
			doLaunch(ureq);
		}
	}

	private void doLaunch(UserRequest ureq) {
		dockC = new DockController(ureq, getWindowControl(), false, new ControllerCreator(){
			public Controller createController(UserRequest lureq, WindowControl lwControl) {
				Controller foCtr = ForumUIFactory.getStandardForumController(lureq, lwControl, forum, foCallback);
				listenTo(foCtr);
				Controller titledCtrl = TitledWrapperHelper.getWrapper(lureq, lwControl, foCtr, courseNode, "o_fo_icon");
				return titledCtrl;
			}}, 
			new DockLayoutControllerCreatorCallback() {
				public ControllerCreator createLayoutControllerCreator(UserRequest ureq, final ControllerCreator contentControllerCreator) {
					return BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, new ControllerCreator() {
						@SuppressWarnings("synthetic-access")
						public Controller createController(UserRequest lureq, WindowControl lwControl) {
							// Wrap in column layout, popup window needs a layout controller
							Controller ctr = contentControllerCreator.createController(lureq, lwControl);
							LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, null, null, ctr.getInitialComponent(),
									null);
							layoutCtr.setCustomCSS(CourseFactory.getCustomCourseCss(lureq.getUserSession(), courseEnv));
							layoutCtr.addDisposableChildController(ctr);
							return layoutCtr;
						}
					});
				}
			});
		listenTo(dockC);
		main.setContent(dockC.getInitialComponent());
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	//fxdiff BAKS-7 Resume function
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(dockC != null && dockC.getController() instanceof TitledWrapperController) {
			TitledWrapperController wrapper2 = (TitledWrapperController)dockC.getController();
			if(wrapper2.getContentController() instanceof Activateable2) {
				((Activateable2)wrapper2.getContentController()).activate(ureq, entries, state);
			}
		}
	}
}
