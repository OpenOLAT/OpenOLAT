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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.Test;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 7 Oct 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ConfigObligationEvaluatorTest {

	@Test
	public void shouldGetMostImportantExceptionalObligation() {
		ConfigObligationEvaluator sut = new ConfigObligationEvaluator();
		
		assertThat(sut.getMostImportantExceptionalObligation(Set.of(AssessmentObligation.optional, AssessmentObligation.excluded), AssessmentObligation.mandatory)).isEqualTo(AssessmentObligation.optional);
		assertThat(sut.getMostImportantExceptionalObligation(Set.of(AssessmentObligation.optional), AssessmentObligation.mandatory)).isEqualTo(AssessmentObligation.optional);
		assertThat(sut.getMostImportantExceptionalObligation(Set.of(AssessmentObligation.excluded), AssessmentObligation.mandatory)).isEqualTo(AssessmentObligation.excluded);
		assertThat(sut.getMostImportantExceptionalObligation(Set.of(AssessmentObligation.mandatory, AssessmentObligation.excluded), AssessmentObligation.optional)).isEqualTo(AssessmentObligation.mandatory);
		assertThat(sut.getMostImportantExceptionalObligation(Set.of(AssessmentObligation.mandatory), AssessmentObligation.optional)).isEqualTo(AssessmentObligation.mandatory);
		assertThat(sut.getMostImportantExceptionalObligation(Set.of(AssessmentObligation.excluded), AssessmentObligation.optional)).isEqualTo(AssessmentObligation.excluded);
		assertThat(sut.getMostImportantExceptionalObligation(Set.of(AssessmentObligation.optional, AssessmentObligation.mandatory), AssessmentObligation.excluded)).isEqualTo(AssessmentObligation.mandatory);
		assertThat(sut.getMostImportantExceptionalObligation(Set.of(AssessmentObligation.mandatory), AssessmentObligation.excluded)).isEqualTo(AssessmentObligation.mandatory);
		assertThat(sut.getMostImportantExceptionalObligation(Set.of(AssessmentObligation.optional), AssessmentObligation.excluded)).isEqualTo(AssessmentObligation.optional);
		assertThat(sut.getMostImportantExceptionalObligation(Set.of(), AssessmentObligation.optional)).isEqualTo(AssessmentObligation.optional);
	}

}
