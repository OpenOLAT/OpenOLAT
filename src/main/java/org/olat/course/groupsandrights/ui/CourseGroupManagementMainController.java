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

import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.group.GroupLoggingAction;
import org.olat.group.ui.BGControllerFactory;
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
	private static final String CMD_CLOSE = "cmd.close";

	private Panel content;

	private LayoutMain3ColsController columnLayoutCtr;
	private ToolController toolC;
	private TableController contextListCtr;

	private BGManagementController groupManageCtr;
	private OLATResourceable ores;
	
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
		
		this.ores = ores;
		// set user activity logger for this controller
		ICourse course = CourseFactory.loadCourse(ores);
		addLoggingResourceable(LoggingResourceable.wrap(course));

		CourseGroupManager groupManager = course.getCourseEnvironment().getCourseGroupManager();
		OLATResource courseResource = groupManager.getCourseResource();

		// init content panel. current panel content will be set later in init process, use null for now
		content = putInitialPanel(null);

		removeAsListenerAndDispose(groupManageCtr);
		groupManageCtr = BGControllerFactory.getInstance().createManagementController(ureq, getWindowControl(), courseResource, false);
		listenTo(groupManageCtr);
		content.setContent(groupManageCtr.getInitialComponent());
		
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
			} else if (event == Event.BACK_EVENT) {
				// show context list again
				// reinitialize context list since it could be dirty
				List groupContexts;
				ICourse course = CourseFactory.loadCourse(ores);
				CourseGroupManager groupManager = course.getCourseEnvironment().getCourseGroupManager();
				//TODO gm
				/*
				if (BusinessGroup.TYPE_LEARNINGROUP.equals(groupType)) {
					groupContexts = groupManager.getLearningGroupContexts();
				} else {
					groupContexts = groupManager.getRightGroupContexts();
				}
				*/
				//doInitContextList(ureq, groupContexts);
				content.setContent(columnLayoutCtr.getInitialComponent());
			}
		} else if (source == contextListCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				int rowid = te.getRowId();
				/*BGContext context = contextTableModel.getGroupContextAt(rowid);
				if (actionid.equals(CMD_CONTEXT_RUN)) {
					doInitGroupmanagement(ureq, context, true);
				}*/
			}
		} else if (source == toolC) {
			if (event.getCommand().equals(CMD_CLOSE)) {
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