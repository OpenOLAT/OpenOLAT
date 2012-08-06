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

package org.olat.course.groupsandrights.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.group.GroupLoggingAction;
import org.olat.group.ui.management.BGManagementController;
import org.olat.resource.OLATResource;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description:<BR/> This controller searches for available group contexts for
 * this course. Currently only one context per grouptype per course is
 * supported. <P/>
 * 
 * Initial Date: Aug 25, 2004
 * @author gnaegi
 */
public class CourseGroupManagementMainController extends MainLayoutBasicController {

	private final BGManagementController groupManageCtr;

	
	/**
	 * Constructor for the course group management main controller
	 * 
	 * @param ureq
	 * @param wControl
	 * @param course
	 * @param groupType
	 */
	public CourseGroupManagementMainController(UserRequest ureq, WindowControl wControl, OLATResourceable ores) {
		super(ureq, wControl);
		
		getUserActivityLogger().setStickyActionType(ActionType.admin);
		
		// set user activity logger for this controller
		ICourse course = CourseFactory.loadCourse(ores);
		addLoggingResourceable(LoggingResourceable.wrap(course));

		CourseGroupManager groupManager = course.getCourseEnvironment().getCourseGroupManager();
		OLATResource courseResource = groupManager.getCourseResource();

		groupManageCtr = new BGManagementController(ureq, getWindowControl(), courseResource, course.getCourseTitle(), false);
		listenTo(groupManageCtr);
		putInitialPanel(groupManageCtr.getInitialComponent());
		
		// logging
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUPMANAGEMENT_START, getClass());
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
	// empty
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == groupManageCtr) {
			if (event == Event.DONE_EVENT) {
				// Send done event to parent controller
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUPMANAGEMENT_CLOSE, getClass());
	}
}