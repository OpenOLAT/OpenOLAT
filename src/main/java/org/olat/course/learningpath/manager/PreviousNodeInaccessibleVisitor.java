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

import java.util.HashMap;
import java.util.Map;

import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.Visitor;
import org.olat.course.learningpath.evaluation.LinearAccessEvaluator;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

public class PreviousNodeInaccessibleVisitor implements Visitor {
	
	private final ScoreAccounting scoreAccounting;
	private final Map<String, String> inaccessibleNodeIdentToFirstInaccessibleNodeIdent = new HashMap<>();
	private String firstInaccesssibleNodeIdent;

	public PreviousNodeInaccessibleVisitor(ScoreAccounting scoreAccounting) {
		this.scoreAccounting = scoreAccounting;
	}
	
	public String getInaccessibleNodeIdent(CourseNode courseNode) {
		return inaccessibleNodeIdentToFirstInaccessibleNodeIdent.get(courseNode.getIdent());
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
					firstInaccesssibleNodeIdent = null;
				} else {
					if (firstInaccesssibleNodeIdent == null) {
						firstInaccesssibleNodeIdent = courseNode.getIdent();
					} else {
						inaccessibleNodeIdentToFirstInaccessibleNodeIdent.put(courseNode.getIdent(), firstInaccesssibleNodeIdent);
					}
				}
			}
		}
	}
}	