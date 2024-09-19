/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.cns.assessment;

import java.util.Set;

import org.olat.course.learningpath.evaluation.ExceptionalObligationEvaluator;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.st.assessment.AbstractConfigObligationEvaluator;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 13 Sep 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CNSObligationEvaluator extends AbstractConfigObligationEvaluator {
	
	private static final CNSConfigObligationEvaluator CONFIG_OBLIGATION_EVALUATOR = new CNSConfigObligationEvaluator();

	@Override
	public AssessmentObligation getMostImportantExceptionalObligation(Set<AssessmentObligation> assessmentObligations,
			AssessmentObligation defaultObligation) {
		return AssessmentObligation.excluded;
	}
	
	@Override
	protected ConfigObligationEvaluator getConfigObligationEvaluator() {
		return CONFIG_OBLIGATION_EVALUATOR;
	}
	
	static final class CNSConfigObligationEvaluator implements ConfigObligationEvaluator {
		
		@Override
		public AssessmentObligation getConfigObligation(CourseNode courseNode,
				ExceptionalObligationEvaluator exceptionalObligationEvaluator) {
			return AssessmentObligation.excluded;
		}
		
	}

}
