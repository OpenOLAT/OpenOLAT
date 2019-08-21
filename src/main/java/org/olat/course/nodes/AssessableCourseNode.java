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
import org.olat.modules.assessment.AssessmentEntry;


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
	
	public AssessmentEvaluation getUserScoreEvaluation(AssessmentEntry entry); 

	/**
	 * this method implementation must not cache any results!
	 * 
	 * The user has no scoring results jet (e.g. made no test yet), then the
	 * ScoreEvaluation.NA has to be returned!
	 * @param userCourseEnv
	 * @return null, if this node cannot deliver any useful scoring info (this is not the case for a test never tried or manual scoring: those have default values 0.0f / false for score/passed; currently only the STNode returns null if there are no scoring rules defined.)
	 */
	public AssessmentEvaluation getUserScoreEvaluation(UserCourseEnvironment userCourseEnv);

}
