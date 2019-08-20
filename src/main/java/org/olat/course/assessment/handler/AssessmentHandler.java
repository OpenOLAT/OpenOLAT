/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.handler;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 20 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface AssessmentHandler {
	
	public String acceptCourseNodeType();
	
	public AssessmentConfig getAssessmentConfig(CourseNode courseNode);
	
	/**
	 * Returns a controller to edit the node specific details. Check
	 * AssessmentConfig.hasEditableDetails() before invoking this method.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param courseNode
	 * @param userCourseEnvironment
	 * @return a controller or null
	 */
	public Controller getDetailsEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			CourseNode courseNode, UserCourseEnvironment coachCourseEnv, UserCourseEnvironment assessedUserCourseEnvironment);
	
	/**
	 * 
	 * @return whether this node has a custom controller for the identity list.
	 */
	public boolean hasCustomIdentityList();
	
	/**
	 * Returns the controller with the list of assessed identities for a specific
	 * course node. Check AssessmentHandler.hasCustomIdentityList() before invoking
	 * this method.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param stackPanel
	 * @param courseNode
	 * @param courseEntry
	 * @param group
	 * @param coachCourseEnv
	 * @param toolContainer
	 * @param assessmentCallback
	 * @return
	 */
	public AssessmentCourseNodeController getIdentityListController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel stackPanel, CourseNode courseNode, RepositoryEntry courseEntry, BusinessGroup group,
			UserCourseEnvironment coachCourseEnv, AssessmentToolContainer toolContainer,
			AssessmentToolSecurityCallback assessmentCallback);
}
