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

import java.util.Set;

import org.olat.course.nodes.st.assessment.AbstractConfigObligationEvaluator;
import org.olat.course.run.scoring.ObligationEvaluator;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 1 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ConfigObligationEvaluator extends AbstractConfigObligationEvaluator implements ObligationEvaluator {
	
	@Override
	public AssessmentObligation getMostImportantExceptionalObligation(Set<AssessmentObligation> assessmentObligations,
			AssessmentObligation defaultObligation) {
		if (!assessmentObligations.isEmpty()) {
			if (AssessmentObligation.mandatory == defaultObligation) {
				defaultObligation = assessmentObligations.contains(AssessmentObligation.optional)
						? AssessmentObligation.optional
						: AssessmentObligation.excluded;
			} else if (AssessmentObligation.optional == defaultObligation) {
				defaultObligation = assessmentObligations.contains(AssessmentObligation.mandatory)
						? AssessmentObligation.mandatory
						: AssessmentObligation.excluded;
			} else if (AssessmentObligation.excluded == defaultObligation) {
				defaultObligation = assessmentObligations.contains(AssessmentObligation.mandatory)
						? AssessmentObligation.mandatory
						: AssessmentObligation.optional;
			}
		}
		return defaultObligation;
	}

}
