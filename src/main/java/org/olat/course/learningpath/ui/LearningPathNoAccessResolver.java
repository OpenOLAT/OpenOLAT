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
package org.olat.course.learningpath.ui;

import java.util.Date;

import org.olat.core.util.tree.TreeVisitor;
import org.olat.course.learningpath.manager.PreviousNodeInaccessibleVisitor;
import org.olat.course.nodeaccess.NoAccessResolver;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Initial date: 12 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathNoAccessResolver implements NoAccessResolver {

	private final UserCourseEnvironment userCourseEnv;
	private PreviousNodeInaccessibleVisitor previousNodeInaccessibleVisitor;

	public LearningPathNoAccessResolver(UserCourseEnvironment userCourseEnv) {
		this.userCourseEnv = userCourseEnv;
	}

	@Override
	public NoAccess getNoAccessMessage(CourseNode courseNode) {
		String inaccessibleNodeIdent = getPreviousNodeInaccessibleVisitor().getInaccessibleNodeIdent(courseNode);
		if (inaccessibleNodeIdent != null) {
			return NoAccessResolver.previousNotDone(inaccessibleNodeIdent);
		}
		
		AssessmentEvaluation assessmentEvaluation = userCourseEnv.getScoreAccounting().evalCourseNode(courseNode);
		if (assessmentEvaluation != null &&assessmentEvaluation.getStartDate() != null && assessmentEvaluation.getStartDate().after(new Date())) {
			return NoAccessResolver.startDateInFuture(assessmentEvaluation.getStartDate());
		}
		
		return NoAccessResolver.unknown();
	}
	
	private PreviousNodeInaccessibleVisitor getPreviousNodeInaccessibleVisitor() {
		if (previousNodeInaccessibleVisitor == null) {
			previousNodeInaccessibleVisitor = new PreviousNodeInaccessibleVisitor(userCourseEnv.getScoreAccounting());
			TreeVisitor tv = new TreeVisitor(previousNodeInaccessibleVisitor, userCourseEnv.getCourseEnvironment().getRunStructure().getRootNode(), false);
			tv.visitAll();
		}
		return previousNodeInaccessibleVisitor;
	}

}
