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
package org.olat.course.learningpath.evaluation;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.course.learningpath.ui.LearningPathTreeNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 2 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LinearAccessEvaluator implements AccessEvaluator {

	private static final Logger log = Tracing.createLoggerFor(LinearAccessEvaluator.class);
	
	private static final AssessmentEntryStatus[] accessibleStati = {
			AssessmentEntryStatus.done,
			AssessmentEntryStatus.inProgress,
			AssessmentEntryStatus.inReview,
			AssessmentEntryStatus.notStarted
	};
	
	@Override
	public boolean isAccessible(LearningPathTreeNode currentNode, UserCourseEnvironment userCourseEnv) {
		if (userCourseEnv.isAdmin() || userCourseEnv.isCoach()) return true;
		
		AssessmentEntryStatus status = currentNode.getAssessmentStatus();
		boolean hasAccess = hasAccess(status);
		log.debug("Access for couse node {} ({}): status '{}' => access '{}'",
				currentNode.getIdent(),
				currentNode.getCourseNode().getType(),
				status,
				hasAccess);
		return hasAccess;
	}

	public static boolean hasAccess(AssessmentEntryStatus status) {
		for (AssessmentEntryStatus accessibleStatus : accessibleStati) {
			if (accessibleStatus.equals(status)) {
				return true;
			}
		}
		return false;
	}

}
