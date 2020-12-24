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

import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.Blocker;
import org.olat.course.run.scoring.FullyAssessedEvaluator;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 18 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class STFullyAssessedEvaluator implements FullyAssessedEvaluator {

	@Override
	public Boolean getFullyAssessed(AssessmentEvaluation currentEvaluation, List<AssessmentEvaluation> children,
			Blocker blocker) {
		boolean hasMandatory = false;
		for (AssessmentEvaluation evaluation : children) {
			if (isMandatory(evaluation)) {
				hasMandatory = true;
				if (isNotFullyAssessedYet(evaluation)) {
					blocker.block();
					return Boolean.FALSE;
				}
			}
		}
		// If the participant has fully assessed all mandatory elements,
		// he should be able to continue from that element. In that case
		// he has fully assessed the nodes earlier. So don't stop him afterwards!
		if (hasMandatory) {
			return Boolean.TRUE;
		}
		return Boolean.valueOf(!blocker.isBlocked());
	}

	private boolean isNotFullyAssessedYet(AssessmentEvaluation evaluation) {
		return evaluation.getFullyAssessed() == null || !evaluation.getFullyAssessed();
	}

	private boolean isMandatory(AssessmentEvaluation evaluation) {
		return evaluation.getObligation() != null && AssessmentObligation.mandatory == evaluation.getObligation().getCurrent();
	}

}
