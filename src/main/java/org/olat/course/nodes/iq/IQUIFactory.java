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
package org.olat.course.nodes.iq;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQSURVCourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * Description:<br>
 * TODO: patrickb Class Description for IQUIFactory
 * 
 * <P>
 * Initial Date: 18.06.2010 <br>
 * 
 * @author patrickb
 */
public class IQUIFactory {

	/**
	 * [SPRING] builds the SpecificControllerCreator as argument in the
	 * constructor. This "extension point" was created during ONYX integration
	 * review. It delegates creation to the factory instead of using new
	 * XXXController(..) creation of different IQxyzRun / IQxyzEdit /
	 * IQxyzPreviewControllers within the IQxyzCourseNodes.
	 */
	private static IQControllerCreator iqControllerCreator = new IQControllerCreatorOlat();

	public IQUIFactory() {
		//
	}

	public static TabbableController createIQTestEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course,
			IQTESTCourseNode courseNode, CourseGroupManager groupMgr, UserCourseEnvironment euce) {
		return IQUIFactory.iqControllerCreator.createIQTestEditController(ureq, wControl, stackPanel, course, courseNode, groupMgr, euce);
	}

	public static TabbableController createIQSelftestEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course,
			IQSELFCourseNode courseNode, CourseGroupManager groupMgr, UserCourseEnvironment euce) {
		return IQUIFactory.iqControllerCreator.createIQSelftestEditController(ureq, wControl, stackPanel, course, courseNode, groupMgr, euce);
	}

	public static TabbableController createIQSurveyEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course,
			IQSURVCourseNode courseNode, CourseGroupManager groupMgr, UserCourseEnvironment euce) {
		return IQUIFactory.iqControllerCreator.createIQSurveyEditController(ureq, wControl, stackPanel,  course, courseNode, groupMgr, euce);
	}

	public static Controller createIQTestRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			IQTESTCourseNode courseNode) {
		return IQUIFactory.iqControllerCreator.createIQTestRunController(ureq, wControl, userCourseEnv, courseNode);
	}

	public static Controller createIQTestPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			IQTESTCourseNode courseNode) {
		return IQUIFactory.iqControllerCreator.createIQTestPreviewController(ureq, wControl, userCourseEnv, courseNode);
	}

	public static Controller createIQSelftestRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			IQSELFCourseNode courseNode) {
		return IQUIFactory.iqControllerCreator.createIQSelftestRunController(ureq, wControl, userCourseEnv, courseNode);
	}

	public static Controller createIQSurveyRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			IQSURVCourseNode courseNode) {
		return IQUIFactory.iqControllerCreator.createIQSurveyRunController(ureq, wControl, userCourseEnv, courseNode);
	}
}
