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

import java.util.function.Predicate;

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
 * Initial date: 13 Mar 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PassCounter {
	
	public static final Predicate<AssessmentEvaluation> MANDTATORY = evaluation -> {
		ObligationOverridable obligation = evaluation.getObligation();
		return obligation.getCurrent() == null || obligation.getCurrent() == AssessmentObligation.mandatory;
	};
	public static final Predicate<AssessmentEvaluation> NOT_EXCLUDED = evaluation -> {
		ObligationOverridable obligation = evaluation.getObligation();
		return obligation.getCurrent() == null || obligation.getCurrent() != AssessmentObligation.excluded;
	};
	
	public interface Counts {
		
		public int getPassable();
		
		public int getPassed();
		
		public int getFailed();
		
		public boolean isAllAssessed();
	}
	
	private CourseAssessmentService courseAssessmentService;
	
	public Counts getCounts(RepositoryEntryRef courseEntry, CourseNode courseNode, ScoreAccounting scoreAccounting, Predicate<AssessmentEvaluation> filter) {
		return getCounts(courseEntry, courseNode, scoreAccounting, filter, getCourseAssessmentService());
	}
	
	Counts getCounts(RepositoryEntryRef courseEntry, CourseNode courseNode, ScoreAccounting scoreAccounting,
			Predicate<AssessmentEvaluation> filter, CourseAssessmentService courseAssessmentService) {
		PassedCountVisitor visitor = new PassedCountVisitor(courseEntry, courseNode, scoreAccounting, filter, courseAssessmentService);
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
	
	private final static class PassedCountVisitor implements Counts, Visitor {
		
		private final RepositoryEntryRef courseEntry;
		private final CourseNode root;
		private final ScoreAccounting scoreAccounting;
		private final Predicate<AssessmentEvaluation> filter;
		private int passable;
		private int passed;
		private int failed;
		
		private final CourseAssessmentService courseAssessmentService;
		
		private PassedCountVisitor(RepositoryEntryRef courseEntry, CourseNode root, ScoreAccounting scoreAccounting,
				Predicate<AssessmentEvaluation> filter, CourseAssessmentService courseAssessmentService) {
			this.courseEntry = courseEntry;
			this.root = root;
			this.scoreAccounting = scoreAccounting;
			this.filter = filter;
			this.courseAssessmentService = courseAssessmentService;
		}

		@Override
		public int getPassable() {
			return passable;
		}

		@Override
		public int getPassed() {
			return passed;
		}

		@Override
		public int getFailed() {
			return failed;
		}

		@Override
		public boolean isAllAssessed() {
			return passable == passed + failed;
		}

		@Override
		public void visit(INode node) {
			if (node.getIdent().equals(root.getIdent())) return;
			
			if (node instanceof CourseNode) {
				CourseNode courseNode = (CourseNode)node;
				AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, courseNode);
				if (Mode.setByNode == assessmentConfig.getPassedMode() && !assessmentConfig.ignoreInCourseAssessment()) {
					AssessmentEvaluation assessmentEvaluation = scoreAccounting.evalCourseNode(courseNode);
					if (filter.test(assessmentEvaluation)) {
						passable++;
						Boolean userVisible = assessmentEvaluation.getUserVisible();
						if (userVisible != null && userVisible.booleanValue()) {
							Boolean nodePassed = assessmentEvaluation.getPassed();
							if (nodePassed != null) {
								if (nodePassed) {
									passed++;
								} else {
									failed++;
								}
							}
						}
					}
				}
			}
		}

	}

}
