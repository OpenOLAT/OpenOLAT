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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 1 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CumulatingDurationEvaluatorTest {
	
	CumulatingDurationEvaluator sut = new CumulatingDurationEvaluator();
	
	@Test
	public void shouldNotDependOnCurrentNode() {
		assertThat(sut.isDependingOnCurrentNode()).isFalse();
	}

	@Test
	public void shouldDependOnChildren() {
		assertThat(sut.isDependingOnChildNodes()).isTrue();
	}
	
	@Test
	public void shouldSumDurationOfMandatoryChildren() {
		List<AssessmentEvaluation> children = new ArrayList<>();
		AssessmentEvaluation child1 = createAssessmentEvaluation(AssessmentObligation.mandatory, 2);
		children.add(child1);
		AssessmentEvaluation child2 = createAssessmentEvaluation(AssessmentObligation.mandatory, 3);
		children.add(child2);
		AssessmentEvaluation child3 = createAssessmentEvaluation(AssessmentObligation.mandatory, null);
		children.add(child3);
		AssessmentEvaluation child4 = createAssessmentEvaluation(AssessmentObligation.optional, 10);
		children.add(child4);
		
		Integer duration = sut.getDuration(children);
		
		assertThat(duration).isEqualTo(5);
	}
	
	private AssessmentEvaluation createAssessmentEvaluation(AssessmentObligation obligation, Integer duration) {
		return new AssessmentEvaluation(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
				null, null, null, null, 0, null, null, null, null, null, null, ObligationOverridable.of(obligation), duration, null, null);
	}

}
