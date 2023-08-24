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
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 19 Jul 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentCounter {
	
	public interface AssessmentCounts {
		
		public int getNumAssessable();
		
		public int getNumUserVisible();
		
		public boolean isAllAssessed();
	}
	
	private CourseAssessmentService courseAssessmentService;
	
	public AssessmentCounts getCounts(RepositoryEntryRef courseEntry, CourseNode courseNode, ScoreAccounting scoreAccounting) {
		return getCounts(courseEntry, courseNode, scoreAccounting, getCourseAssessmentService());
	}
	
	AssessmentCounts getCounts(RepositoryEntryRef courseEntry, CourseNode courseNode, ScoreAccounting scoreAccounting, CourseAssessmentService courseAssessmentService) {
		AssessmentCountVisitor visitor = new AssessmentCountVisitor(courseEntry, courseNode, scoreAccounting, courseAssessmentService);
		TreeVisitor treeVisitor = new TreeVisitor(visitor, courseNode, true);
		treeVisitor.visitAll();
		return visitor;
	}
	
	private CourseAssessmentService getCourseAssessmentService() {
		if (courseAssessmentService == null) {
			courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		}
		return courseAssessmentService;
	}
	
	private final static class AssessmentCountVisitor implements AssessmentCounts, Visitor {
		
		private final RepositoryEntryRef courseEntry;
		private final CourseNode root;
		private final ScoreAccounting scoreAccounting;
		private int numAssessable;
		private int numUserVisible;
		
		private final CourseAssessmentService courseAssessmentService;
		
		private AssessmentCountVisitor(RepositoryEntryRef courseEntry, CourseNode root, ScoreAccounting scoreAccounting, CourseAssessmentService courseAssessmentService) {
			this.courseEntry = courseEntry;
			this.root = root;
			this.scoreAccounting = scoreAccounting;
			this.courseAssessmentService = courseAssessmentService;
		}

		@Override
		public int getNumAssessable() {
			return numAssessable;
		}

		@Override
		public int getNumUserVisible() {
			return numUserVisible;
		}

		@Override
		public boolean isAllAssessed() {
			return numAssessable == numUserVisible;
		}

		@Override
		public void visit(INode node) {
			if (node.getIdent().equals(root.getIdent())) return;
			
			if (node instanceof CourseNode courseNode) {
				AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, courseNode);
				// Only score and passed are relevant for for the learning path course passed rules
				// but not user comment and documents
				if ((Mode.setByNode == assessmentConfig.getPassedMode() || Mode.setByNode == assessmentConfig.getScoreMode())
						&& !assessmentConfig.ignoreInCourseAssessment()) {
					AssessmentEvaluation assessmentEvaluation = scoreAccounting.evalCourseNode(courseNode);
					if (isNotExcluded(assessmentEvaluation)) {
						numAssessable++;
						Boolean userVisible = assessmentEvaluation.getUserVisible();
						if (userVisible != null && userVisible.booleanValue()) {
							numUserVisible++;
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
