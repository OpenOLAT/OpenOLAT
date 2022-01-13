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
import org.olat.course.learningpath.ui.LearningPathTreeNode;
import org.olat.course.nodeaccess.NoAccessResolver;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 12 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathNoAccessResolver implements NoAccessResolver {

	private final INode rootNode;
	private LearningPathNoAccessVisitor learningPathNoAccessVisitor;

	public LearningPathNoAccessResolver(INode rootNode) {
		this.rootNode = rootNode;
	}

	@Override
	public NoAccess getNoAccessMessage(CourseNode courseNode) {
		NoAccess noAccess = getPreviousNodeInaccessibleVisitor().getNoAccess(courseNode.getIdent());
		
		return noAccess != null? noAccess: NoAccessResolver.unknown();
	}
	
	private LearningPathNoAccessVisitor getPreviousNodeInaccessibleVisitor() {
		if (learningPathNoAccessVisitor == null) {
			learningPathNoAccessVisitor = new LearningPathNoAccessVisitor();
			TreeVisitor tv = new TreeVisitor(learningPathNoAccessVisitor, rootNode, false);
			tv.visitAll();
		}
		return learningPathNoAccessVisitor;
	}
	
	private class LearningPathNoAccessVisitor implements Visitor {
		
		private final Map<String, NoAccess> inaccessibleNodeIdentToNoAccess = new HashMap<>();
		private String linkNodeIdent;
		private String toDoNodeIdent;
		
		public NoAccess getNoAccess(String courseNodeIdent) {
			return inaccessibleNodeIdentToNoAccess.get(courseNodeIdent);
		}

		@Override
		public void visit(INode node) {
			if (node instanceof LearningPathTreeNode) {
				LearningPathTreeNode lpTreeNode = (LearningPathTreeNode)node;
				AssessmentEvaluation assessmentEvaluation = lpTreeNode.getAssessmentEvaluation();
				if (assessmentEvaluation != null) {
					boolean hasAccess = LinearAccessEvaluator.hasAccess(assessmentEvaluation.getAssessmentStatus());
					if (hasAccess && !lpTreeNode.getSequenceConfig().isInSequence()) {
						linkNodeIdent = null;
						toDoNodeIdent = null;
					} else if (!isFullyAssessed(assessmentEvaluation)) {
						if (hasAccess) {
							if (toDoNodeIdent == null && isMandatory(assessmentEvaluation) && !(lpTreeNode.getCourseNode() instanceof STCourseNode)) {
								toDoNodeIdent = lpTreeNode.getIdent();
							}
						} else {
							if (isStartInFuture(assessmentEvaluation)) {
								if (linkNodeIdent == null) {
									linkNodeIdent = lpTreeNode.getIdent();
								}
								inaccessibleNodeIdentToNoAccess.put(lpTreeNode.getIdent(), NoAccessResolver.startDateInFuture(assessmentEvaluation.getStartDate()));
							} else {
								if (linkNodeIdent == null) {
									linkNodeIdent = toDoNodeIdent;
								}
								inaccessibleNodeIdentToNoAccess.put(lpTreeNode.getIdent(), NoAccessResolver.previousNotDone(linkNodeIdent));
							}
						}
					}
				}
			}
		}

		private boolean isStartInFuture(AssessmentEvaluation assessmentEvaluation) {
			return assessmentEvaluation.getStartDate() != null && assessmentEvaluation.getStartDate().after(new Date());
		}
		
		private boolean isFullyAssessed(AssessmentEvaluation assessmentEvaluation) {
			return assessmentEvaluation.getFullyAssessed() != null && assessmentEvaluation.getFullyAssessed().booleanValue();
		}

		private boolean isMandatory(AssessmentEvaluation assessmentEvaluation) {
			return assessmentEvaluation.getObligation() == null || AssessmentObligation.mandatory == assessmentEvaluation.getObligation().getCurrent();
		}	
	}

}
