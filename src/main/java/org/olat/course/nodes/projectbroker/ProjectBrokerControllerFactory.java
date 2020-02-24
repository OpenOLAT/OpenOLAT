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

package org.olat.course.nodes.projectbroker;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ProjectBrokerCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 *
 * @author guretzki
 * 
 */

public class ProjectBrokerControllerFactory {

	public static ProjectBrokerCourseEditorController createCourseEditController(UserRequest ureq, WindowControl wControl, ICourse course, UserCourseEnvironment euce, ProjectBrokerCourseNode projectBrokerCourseNode) {
		return new ProjectBrokerCourseEditorController(ureq, wControl, course, projectBrokerCourseNode);
	}

	public static Controller createRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNode courseNode) {
		return new ProjectListController(ureq, wControl, userCourseEnv, courseNode, false);
	}

	public static Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNode courseNode) {
		return new ProjectListController(ureq, wControl, userCourseEnv, courseNode, true);
	}

	public static Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			CourseNode courseNode) {
		return new ProjectBrokerPeekViewRunController(ureq, wControl, userCourseEnv, courseNode);
	}
	
}
