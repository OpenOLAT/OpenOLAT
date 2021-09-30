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
package org.olat.course.learningpath.evaluation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.course.Structure;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.obligation.ExceptionalObligation;
import org.olat.course.learningpath.obligation.ExceptionalObligationHandler;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ObligationContext;
import org.olat.course.run.scoring.ObligationEvaluator;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 1 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ConfigObligationEvaluator implements ObligationEvaluator {
	
	private LearningPathService learningPathService;

	@Override
	public Overridable<AssessmentObligation> getObligation(AssessmentEvaluation currentEvaluation,
			CourseNode courseNode, Identity identity, Structure runStructure,
			ScoreAccounting scoreAccounting, ObligationContext obligationContext) {
		LearningPathConfigs configs = getLearningPathService().getConfigs(courseNode);
		// Initialize the obligation by the configured standard obligation
		AssessmentObligation evaluatedObligation = configs.getObligation();
		
		// Check if the user is affected by a exceptional obligation
		List<ExceptionalObligation> exceptionalObligations = configs.getExceptionalObligations();
		if (exceptionalObligations != null && !exceptionalObligations.isEmpty()) {
			Set<AssessmentObligation> filtered = filterAssessmentObligation(identity, runStructure, scoreAccounting,
					exceptionalObligations, evaluatedObligation, obligationContext);
			evaluatedObligation = getMostImportantExceptionalObligation(filtered, evaluatedObligation);
		}
		
		Overridable<AssessmentObligation> obligation = currentEvaluation.getObligation().clone();
		obligation.setCurrent(evaluatedObligation);
		return obligation;
	}

	public AssessmentObligation getMostImportantExceptionalObligation(Set<AssessmentObligation> assessmentObligations, AssessmentObligation obligation) {
		if (!assessmentObligations.isEmpty()) {
			if (AssessmentObligation.mandatory == obligation) {
				obligation = assessmentObligations.contains(AssessmentObligation.optional)
						? AssessmentObligation.optional
						: AssessmentObligation.excluded;
			} else if (AssessmentObligation.optional == obligation) {
				obligation = assessmentObligations.contains(AssessmentObligation.mandatory)
						? AssessmentObligation.mandatory
						: AssessmentObligation.excluded;
			} else if (AssessmentObligation.excluded == obligation) {
				obligation = assessmentObligations.contains(AssessmentObligation.mandatory)
						? AssessmentObligation.mandatory
						: AssessmentObligation.optional;
			}
		}
		return obligation;
	}

	Set<AssessmentObligation> filterAssessmentObligation(Identity identity,  Structure runStructure,
			ScoreAccounting scoreAccounting, List<ExceptionalObligation> exceptionalObligations,
			AssessmentObligation defaultObligation, ObligationContext obligationContext) {
		return filter(identity, runStructure, scoreAccounting, exceptionalObligations, defaultObligation, obligationContext)
				.map(ExceptionalObligation::getObligation)
				.collect(Collectors.toSet());
	}
	
	public List<ExceptionalObligation> filterExceptionalObligations(Identity identity,  Structure runStructure,
			ScoreAccounting scoreAccounting, List<ExceptionalObligation> exceptionalObligations,
			AssessmentObligation defaultObligation, ObligationContext obligationContext) {
		return filter(identity, runStructure, scoreAccounting, exceptionalObligations, defaultObligation, obligationContext)
				.collect(Collectors.toList());
	}

	private Stream<ExceptionalObligation> filter(Identity identity, Structure runStructure,
			ScoreAccounting scoreAccounting, List<ExceptionalObligation> exceptionalObligations,
			AssessmentObligation defaultObligation, ObligationContext obligationContext) {
		return exceptionalObligations.stream()
				.filter(eo -> defaultObligation != eo.getObligation()) // not exceptional if the same obligation
				.filter(eo -> matchesIdentity(identity, runStructure, scoreAccounting, eo, obligationContext));
	}

	private boolean matchesIdentity(Identity identity, Structure runStructure,
			ScoreAccounting scoreAccounting, ExceptionalObligation exceptionalObligation,
			ObligationContext obligationContext) {
		ExceptionalObligationHandler exceptionalObligationHandler = getLearningPathService().getExceptionalObligationHandler(exceptionalObligation.getType());
		if (exceptionalObligationHandler != null) {
			return exceptionalObligationHandler.matchesIdentity(exceptionalObligation, identity, obligationContext, runStructure, scoreAccounting);
		}
		return false;
	}
	
	@Override
	public Overridable<AssessmentObligation> getObligation(AssessmentEvaluation currentEvaluation,
			List<AssessmentEvaluation> children) {
		return currentEvaluation.getObligation();
	}
	
	private LearningPathService getLearningPathService() {
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
	void setLearningPathService(LearningPathService learningPathService) {
		this.learningPathService = learningPathService;
	}

}
