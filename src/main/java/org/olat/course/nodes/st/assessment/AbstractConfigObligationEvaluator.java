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

import java.util.List;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.evaluation.ExceptionalObligationEvaluator;
import org.olat.course.learningpath.obligation.ExceptionalObligation;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ObligationEvaluator;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 25 Oct 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractConfigObligationEvaluator implements ObligationEvaluator {

	private LearningPathService learningPathService;

	@Override
	public ObligationOverridable getObligation(AssessmentEvaluation currentEvaluation, CourseNode courseNode,
			AssessmentObligation parentObligation, ExceptionalObligationEvaluator exceptionalObligationEvaluator) {
		ObligationOverridable obligation = currentEvaluation.getObligation().clone();
		
		AssessmentObligation inherited = AssessmentObligation.excluded == parentObligation? parentObligation: null;
		obligation.setInherited(inherited);
		
		AssessmentObligation configObligation = getConfigObligation(courseNode, exceptionalObligationEvaluator);
		obligation.setConfigCurrent(configObligation);
		
		AssessmentObligation currentObligation = getCurrentObligation(obligation);
		obligation.setCurrent(currentObligation);
		
		return obligation;
	}

	private AssessmentObligation getConfigObligation(CourseNode courseNode, ExceptionalObligationEvaluator exceptionalObligationEvaluator) {
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

	@Override
	public ObligationOverridable getObligation(AssessmentEvaluation currentEvaluation, CourseNode courseNode,
			ExceptionalObligationEvaluator exceptionalObligationEvaluator, List<AssessmentEvaluation> children) {
		ObligationOverridable obligation = currentEvaluation.getObligation().clone();
	
		AssessmentObligation evaluatedObligation = AssessmentObligation.evaluated == obligation.getConfigCurrent()
				? getChildrenObligation(children)
				: null;
		obligation.setEvaluated(evaluatedObligation);
		
		AssessmentObligation currentObligation = getCurrentObligation(obligation);
		obligation.setCurrent(currentObligation);
		
		return obligation;
	}

	private AssessmentObligation getChildrenObligation(List<AssessmentEvaluation> children) {
		for (AssessmentEvaluation child : children) {
			AssessmentObligation childObligation = child.getObligation().getConfigCurrent();
			if (AssessmentObligation.evaluated == childObligation) {
				childObligation = child.getObligation().getEvaluated();
			}
			
			if (AssessmentObligation.mandatory == childObligation) {
				return AssessmentObligation.mandatory;
			}
		}
		return AssessmentObligation.optional;
	}

	protected AssessmentObligation getCurrentObligation(ObligationOverridable obligation) {
		if (obligation.getInherited() != null) {
			return obligation.getInherited();
		}
		
		if (AssessmentObligation.evaluated == obligation.getConfigCurrent()) {
			return obligation.getEvaluated();
		}
		
		return obligation.getConfigCurrent();
	}

	protected LearningPathService getLearningPathService() {
		if (learningPathService == null) {
			learningPathService = CoreSpringFactory.getImpl(LearningPathService.class);
		}
		return learningPathService;
	}

	/**
	 * For Testing only!
	 *
	 * @param learningPathService
	 */
	protected void setLearningPathService(LearningPathService learningPathService) {
		this.learningPathService = learningPathService;
	}

	@Override
	public abstract AssessmentObligation getMostImportantExceptionalObligation(Set<AssessmentObligation> assessmentObligations,
			AssessmentObligation defaultObligation);

}