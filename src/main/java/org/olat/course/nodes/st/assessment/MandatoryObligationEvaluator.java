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

import org.olat.core.id.Identity;
import org.olat.course.Structure;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ObligationContext;
import org.olat.course.run.scoring.ObligationEvaluator;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 23 Oct 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MandatoryObligationEvaluator implements ObligationEvaluator {

	@Override
	public Overridable<AssessmentObligation> getObligation(AssessmentEvaluation currentEvaluation,
			CourseNode courseNode, Identity identity, Structure runStructure,
			ScoreAccounting scoreAccounting, ObligationContext obligationContext) {
		return currentEvaluation.getObligation();
	}

	@Override
	public Overridable<AssessmentObligation> getObligation(AssessmentEvaluation currentEvaluation,
			List<AssessmentEvaluation> children) {
		for (AssessmentEvaluation child : children) {
			if (AssessmentObligation.mandatory == child.getObligation().getCurrent()) {
				return Overridable.of(AssessmentObligation.mandatory);
			}
		}
		return Overridable.of(AssessmentObligation.optional);
	}

}
