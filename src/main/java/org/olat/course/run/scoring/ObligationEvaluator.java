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

import java.util.List;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.evaluation.ExceptionalObligationEvaluator;
import org.olat.course.learningpath.obligation.ExceptionalObligation;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 1 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface ObligationEvaluator {
	
	public ObligationResult getObligation(AssessmentEvaluation currentEvaluation, CourseNode courseNode,
			AssessmentObligation parentObligation, ConfigObligationEvaluator configObligationEvaluator,
			ExceptionalObligationEvaluator exceptionalObligationEvaluator);

	public ObligationOverridable getObligation(AssessmentEvaluation currentEvaluation,
			CourseNode courseNode, ExceptionalObligationEvaluator exceptionalObligationEvaluator,
			List<AssessmentEvaluation> children);
	
	public AssessmentObligation getMostImportantExceptionalObligation(Set<AssessmentObligation> assessmentObligations,
			AssessmentObligation defaultObligation);
	
	public final static class ObligationResult {
		
		private final ObligationOverridable obligationOverridable;
		private final ConfigObligationEvaluator configObligationEvaluator;
		
		public ObligationResult(ObligationOverridable obligationOverridable, ConfigObligationEvaluator configObligationEvaluator) {
			this.obligationOverridable = obligationOverridable;
			this.configObligationEvaluator = configObligationEvaluator;
		}
		
		public ObligationOverridable getObligationOverridable() {
			return obligationOverridable;
		}

		public ConfigObligationEvaluator getConfigObligationResolver() {
			return configObligationEvaluator;
		}
		
	}
	
	public interface ConfigObligationEvaluator {
		
		AssessmentObligation getConfigObligation(CourseNode courseNode, ExceptionalObligationEvaluator exceptionalObligationEvaluator);
		
	}
	
	public static final DefaultConfigObligationEvaluator DEFAULT_CONFIG_OBLIGATION_EVALUATOR = new DefaultConfigObligationEvaluator();
	
	public static final class DefaultConfigObligationEvaluator implements ConfigObligationEvaluator {
		
		private LearningPathService learningPathService;
		
		@Override
		public AssessmentObligation getConfigObligation(CourseNode courseNode, ExceptionalObligationEvaluator exceptionalObligationEvaluator) {
			LearningPathConfigs configs = getLearningPathService().getConfigs(courseNode);
			// Initialize the obligation by the configured standard obligation
			AssessmentObligation nodeObligation = configs.getObligation();
			
			List<ExceptionalObligation> exceptionalObligations = configs.getExceptionalObligations();
			if (exceptionalObligations != null && !exceptionalObligations.isEmpty()) {
				Set<AssessmentObligation> filtered = exceptionalObligationEvaluator
						.filterAssessmentObligation(exceptionalObligations, nodeObligation);
				if (!filtered.isEmpty()) {
					nodeObligation = filtered.stream().findFirst().get();
				}
			}
			return nodeObligation;
		}
		
		public LearningPathService getLearningPathService() {
			if (learningPathService == null) {
				learningPathService = CoreSpringFactory.getImpl(LearningPathService.class);
			}
			return learningPathService;
		}
		
		/*
		 * For testing purposes only
		 */
		public void setLearningPathService(LearningPathService learningPathService) {
			this.learningPathService = learningPathService;
		}
	}

}
