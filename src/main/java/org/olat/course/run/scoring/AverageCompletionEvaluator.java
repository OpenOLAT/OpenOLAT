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
package org.olat.course.run.scoring;

import java.util.function.Function;

import org.olat.core.CoreSpringFactory;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.nodes.CollectingVisitor;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 23 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AverageCompletionEvaluator implements CompletionEvaluator {
	
	public final static Function<AssessmentEvaluation, Integer> UNWEIGHTED = (ae) -> 1;
	public final static Function<AssessmentEvaluation, Integer> DURATION_WEIGHTED = 
			(ae) -> ae.getDuration() != null? ae.getDuration(): Integer.valueOf(1);
			
	private static final StatusCompletionEvaluator statusCompletionEvaluator = new StatusCompletionEvaluator();
	
	private final CourseAssessmentService courseAssessmentService;
	private final Function<AssessmentEvaluation, Integer> weightFunction;
	
	public AverageCompletionEvaluator(Function<AssessmentEvaluation, Integer> weightFunction) {
		this(CoreSpringFactory.getImpl(CourseAssessmentService.class), weightFunction);
	}
	
	public AverageCompletionEvaluator(CourseAssessmentService courseAssessmentService,
			Function<AssessmentEvaluation, Integer> weightFunction) {
		this.courseAssessmentService = courseAssessmentService;
		this.weightFunction = weightFunction;
	}
	
	@Override
	public Double getCompletion(AssessmentEvaluation currentEvaluation, CourseNode courseNode,
			ScoreAccounting scoreAccounting, RepositoryEntryRef courseEntry) {
		
		// get all children
		CollectingVisitor visitor = CollectingVisitor.testing(cn -> !cn.getIdent().equals(courseNode.getIdent()));
		TreeVisitor tv = new TreeVisitor(visitor, courseNode, true);
		tv.visitAll();
		
		int count = 0;
		double completion = 0.0;
		boolean allOptional = true;
		for (CourseNode child: visitor.getCourseNodes()) {
			AssessmentEvaluation assessmentEvaluation = scoreAccounting.evalCourseNode(child);
			if (isMandatory(assessmentEvaluation)) {
				allOptional = false;
				AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, child);
				int nodeCount = 0;
				double nodeCompletion = 0.0;
				if (Mode.evaluated.equals(assessmentConfig.getCompletionMode())) {
					continue; // do not count twice
				} else if (isFullyAssessed(assessmentEvaluation)) {
					nodeCount = 1;
					nodeCompletion = 1.0;
				} else if (Mode.setByNode.equals(assessmentConfig.getCompletionMode())) {
					nodeCount = 1;
					nodeCompletion = assessmentEvaluation.getCompletion() != null
							? assessmentEvaluation.getCompletion().doubleValue()
							: 0.0;
				} else if (Mode.none.equals(assessmentConfig.getCompletionMode())) {
					nodeCount = 1;
					completion += statusCompletionEvaluator.getCompletion(assessmentEvaluation);
				}
				int weight = weightFunction.apply(assessmentEvaluation).intValue();
				count += weight * nodeCount;
				completion += weight * nodeCompletion;
			}
		}
		
		if (allOptional) {
			return 1.0;
		}
		return count > 0? completion / count: null;
	}

	private boolean isFullyAssessed(AssessmentEvaluation assessmentEvaluation) {
		return Boolean.TRUE.equals(assessmentEvaluation.getFullyAssessed());
	}

	private boolean isMandatory(AssessmentEvaluation evaluation) {
		return evaluation != null
				&& evaluation.getObligation() != null
				&& AssessmentObligation.mandatory == evaluation.getObligation().getCurrent();
	}

}
