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
package org.olat.course.nodes.st.assessment;

import org.olat.core.CoreSpringFactory;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.scoring.ScoreEvaluator;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 9 Mar 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class CumulatingScoreEvaluator implements ScoreEvaluator {
	
	public interface Score {
		
		public Float getSum();
		
		public Float getAverage();
	}
	
	private final boolean average;
	
	private CourseAssessmentService courseAssessmentService;
	
	CumulatingScoreEvaluator(boolean average) {
		this.average = average;
	}
	
	@Override
	public Float getScore(AssessmentEvaluation currentEvaluation, CourseNode courseNode,
			ScoreAccounting scoreAccounting, RepositoryEntryRef courseEntry, ConditionInterpreter conditionInterpreter) {
		Score score = getScore(courseNode, scoreAccounting, courseEntry, courseAssessmentService());
		return average? score.getAverage(): score.getSum();
	}
	
	Score getScore(CourseNode courseNode, ScoreAccounting scoreAccounting, RepositoryEntryRef courseEntry, CourseAssessmentService courseAssessmentService) {
		ScoreVisitor visitor = new ScoreVisitor(courseNode, scoreAccounting, courseEntry, courseAssessmentService);
		TreeVisitor treeVisitor = new TreeVisitor(visitor, courseNode, true);
		treeVisitor.visitAll();
		return visitor;
	}
	
	private CourseAssessmentService courseAssessmentService() {
		if (courseAssessmentService== null) {
			courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		}
		return courseAssessmentService;
	}
	
	private final static class ScoreVisitor implements Score, Visitor {
		
		private final CourseNode root;
		private final ScoreAccounting scoreAccounting;
		private final RepositoryEntryRef courseEntry;
		private float sum;
		private int count;
		
		private final CourseAssessmentService courseAssessmentService;
		
		private ScoreVisitor(CourseNode root, ScoreAccounting scoreAccounting, RepositoryEntryRef courseEntry, CourseAssessmentService courseAssessmentService) {
			this.root = root;
			this.scoreAccounting = scoreAccounting;
			this.courseEntry = courseEntry;
			this.courseAssessmentService = courseAssessmentService;
		}
		
		@Override
		public Float getSum() {
			return count > 0? Float.valueOf(sum): null;
		}

		@Override
		public Float getAverage() {
			return count > 0? Float.valueOf(sum / count): null;
		}

		@Override
		public void visit(INode node) {
			if (node.getIdent().equals(root.getIdent())) return;
			
			if (node instanceof CourseNode) {
				CourseNode courseNode = (CourseNode)node;
				AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, courseNode);
				if (Mode.setByNode == assessmentConfig.getScoreMode() && !assessmentConfig.ignoreInCourseAssessment()) {
					AssessmentEvaluation assessmentEvaluation = scoreAccounting.evalCourseNode(courseNode);
					Boolean userVisible = assessmentEvaluation.getUserVisible();
					if (userVisible != null && userVisible.booleanValue() && isNotExcluded(assessmentEvaluation)) {
						Float score = assessmentEvaluation.getScore();
						if (score != null) {
							sum += score.floatValue();
							count++;
						}
					}
				}
			}
		}

		private boolean isNotExcluded(AssessmentEvaluation assessmentEvaluation) {
			ObligationOverridable obligation = assessmentEvaluation.getObligation();
			return obligation.getCurrent() == null || obligation.getCurrent() != AssessmentObligation.excluded;
		}
	}
	
}
