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

import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.Role;

/**
 * 
 * Description:<br>
 * Provides a course node with the possibility to retrive its last ScoreEvaluation.
 * 
 * <P>
 * Initial Date:  11.04.2008 <br>
 * @author Lavinia Dumitrescu
 */
public interface SelfAssessableCourseNode {

	/**
	 * Provides the AssessmentEvaluation for this course node.
	 * Returns null if no scoring stored yet (that is no selftest finished yet).
	 * @param userCourseEnv
	 * @return Returns the AssessmentEvaluation.
	 */
	public AssessmentEvaluation getAssessmentEvaluation(UserCourseEnvironment userCourseEnv);
	
	/**
	 * Increments the users attempts for this node and this user + 1. 
	 * @param userCourseEnvironment
	 */
	public void incrementUserAttempts(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment, Role by);
	
}
