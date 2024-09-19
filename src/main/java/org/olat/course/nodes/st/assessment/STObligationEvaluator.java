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

import org.olat.course.learningpath.evaluation.ExceptionalObligationEvaluator;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 23 Oct 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class STObligationEvaluator extends AbstractConfigObligationEvaluator {
	
	@Override
	public AssessmentObligation getMostImportantExceptionalObligation(Set<AssessmentObligation> assessmentObligations,
			AssessmentObligation defaultObligation) {
		return AssessmentObligation.evaluated == defaultObligation? AssessmentObligation.excluded: AssessmentObligation.evaluated;
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
	
}
