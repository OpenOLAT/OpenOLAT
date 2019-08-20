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

package org.olat.course.nodes;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeController;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;


/**
 * Initial Date:  Jun 18, 2004
 * @author gnaegi
 *
 * Comment: 
 * All course nodes that are of an assessement type must implement this 
 * interface so that the assessment results can be managed by the assessment
 * tool.
 */
public interface AssessableCourseNode extends CourseNode {

	/**
	 * this method implementation must not cache any results!
	 * 
	 * The user has no scoring results jet (e.g. made no test yet), then the
	 * ScoreEvaluation.NA has to be returned!
	 * @param userCourseEnv
	 * @return null, if this node cannot deliver any useful scoring info (this is not the case for a test never tried or manual scoring: those have default values 0.0f / false for score/passed; currently only the STNode returns null if there are no scoring rules defined.)
	 */
	public AssessmentEvaluation getUserScoreEvaluation(UserCourseEnvironment userCourseEnv);
	
	/**
	 * @param userCourseEnvironment
	 * @return the details view for this node and this user. will be displayed in 
	 * the user list. if hasDetails= false this returns null
	 */	
	public String getDetailsListView(UserCourseEnvironment userCourseEnvironment);
	/**
	 * @return the details list view header key that is used to label the table row
	 */	
	public String getDetailsListViewHeaderKey();
	
	/**
	 * Returns the controller with the list of assessed identities for
	 * a specific course node.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param stackPanel
	 * @param courseEntry
	 * @param group
	 * @param coachCourseEnv
	 * @param toolContainer
	 * @param assessmentCallback
	 * @return
	 */
	public AssessmentCourseNodeController getIdentityListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, BusinessGroup group, UserCourseEnvironment coachCourseEnv,
			AssessmentToolContainer toolContainer, AssessmentToolSecurityCallback assessmentCallback);
	

	

}
