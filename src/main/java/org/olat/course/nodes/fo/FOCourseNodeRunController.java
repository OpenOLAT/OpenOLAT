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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.course.nodes.FOCourseNode;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumCallback;
import org.olat.modules.fo.ui.ForumController;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Initial Date: Apr 22, 2004
 * 
 * @author gnaegi
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class FOCourseNodeRunController extends BasicController implements Activateable2 {

	private final ForumController forumCtrl;

	/**
	 * Constructor for a forum course building block runtime controller
	 * 
	 * @param ureq The user request
	 * @param userCourseEnv
	 * @param wContr The current window controller
	 * @param forum The forum to be displayed
	 * @param foCallback The forum security callback
	 * @param foCourseNode The current course node
	 * @param userCourseEnv 
	 */
	public FOCourseNodeRunController(UserRequest ureq, WindowControl wControl, Forum forum,
			ForumCallback foCallback, FOCourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);

		// set logger on this run controller
		addLoggingResourceable(LoggingResourceable.wrap(courseNode));
		
		forumCtrl = new ForumController(ureq, getWindowControl(), forum, foCallback, true);
		listenTo(forumCtrl);
		Controller titledCtrl = TitledWrapperHelper.getWrapper(ureq, getWindowControl(), forumCtrl, userCourseEnv, courseNode, "o_fo_icon");
		listenTo(titledCtrl);
		putInitialPanel(titledCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing to do
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		forumCtrl.activate(ureq, entries, state);
	}
}