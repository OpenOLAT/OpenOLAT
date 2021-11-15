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
package org.olat.course.learningpath.manager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.course.learningpath.evaluation.LinearAccessEvaluator;
import org.olat.course.nodeaccess.NoAccessResolver;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 12 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathNoAccessResolver implements NoAccessResolver {

	private final UserCourseEnvironment userCourseEnv;
	private LearningPathNoAccessVisitor learningPathNoAccessVisitor;

	public LearningPathNoAccessResolver(UserCourseEnvironment userCourseEnv) {
		this.userCourseEnv = userCourseEnv;
	}

	@Override
	public NoAccess getNoAccessMessage(CourseNode courseNode) {
		NoAccess noAccess = getPreviousNodeInaccessibleVisitor().getNoAccess(courseNode.getIdent());
		
		return noAccess != null? noAccess: NoAccessResolver.unknown();
	}
	
	private LearningPathNoAccessVisitor getPreviousNodeInaccessibleVisitor() {
		if (learningPathNoAccessVisitor == null) {
			learningPathNoAccessVisitor = new LearningPathNoAccessVisitor(userCourseEnv.getScoreAccounting());
			TreeVisitor tv = new TreeVisitor(learningPathNoAccessVisitor, userCourseEnv.getCourseEnvironment().getRunStructure().getRootNode(), false);
			tv.visitAll();
		}
		return learningPathNoAccessVisitor;
	}
	
	private class LearningPathNoAccessVisitor implements Visitor {
		
		private final ScoreAccounting scoreAccounting;
		private final Map<String, NoAccess> inaccessibleNodeIdentToNoAccess = new HashMap<>();
		private String linkNodeIdent;
		private String lastAccessibleNodeIdent;

		public LearningPathNoAccessVisitor(ScoreAccounting scoreAccounting) {
			this.scoreAccounting = scoreAccounting;
		}
		
		public NoAccess getNoAccess(String courseNodeIdent) {
			return inaccessibleNodeIdentToNoAccess.get(courseNodeIdent);
		}

		@Override
		public void visit(INode node) {
			if (node instanceof CourseNode) {
				CourseNode courseNode = (CourseNode)node;
				AssessmentEvaluation assessmentEvaluation = scoreAccounting.evalCourseNode(courseNode);
				if (assessmentEvaluation != null) {
					AssessmentEntryStatus assessmentStatus = assessmentEvaluation.getAssessmentStatus();
					boolean hasAccess = LinearAccessEvaluator.hasAccess(assessmentStatus);
					if (hasAccess) {
						linkNodeIdent = null;
						lastAccessibleNodeIdent = courseNode.getIdent();
					} else {
						if (linkNodeIdent == null && isStartInFuture(assessmentEvaluation)) {
							linkNodeIdent = courseNode.getIdent();
							inaccessibleNodeIdentToNoAccess.put(courseNode.getIdent(), NoAccessResolver.startDateInFuture(assessmentEvaluation.getStartDate()));
						} else {
							if (linkNodeIdent == null) {
								linkNodeIdent = lastAccessibleNodeIdent;
							}
							inaccessibleNodeIdentToNoAccess.put(courseNode.getIdent(), NoAccessResolver.previousNotDone(linkNodeIdent));
						}
					}
				}
			}
		}

		private boolean isStartInFuture(AssessmentEvaluation assessmentEvaluation) {
			return assessmentEvaluation.getStartDate() != null && assessmentEvaluation.getStartDate().after(new Date());
		}
	}	

}
