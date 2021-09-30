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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.olat.admin.user.imp.TransientIdentity;
import org.olat.core.id.Identity;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.obligation.ExceptionalObligation;
import org.olat.course.learningpath.obligation.ExceptionalObligationHandler;
import org.olat.course.learningpath.obligation.OrganisationExceptionalObligation;
import org.olat.course.learningpath.obligation.TestingObligationContext;
import org.olat.group.BusinessGroupRef;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 2 Sep 2021<br>
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
	
	@Test
	public void filterShouldIgnoreRegularObligations() {
		ConfigObligationEvaluator sut = new ConfigObligationEvaluator();
		LearningPathService learningPathService = mock(LearningPathService.class);
		ExceptionalObligationHandler trueHandler = mock(ExceptionalObligationHandler.class);
		when(trueHandler.matchesIdentity(any(), any(), any(), any(), any())).thenReturn(Boolean.TRUE);
		when(learningPathService.getExceptionalObligationHandler("trueType")).thenReturn(trueHandler);
		sut.setLearningPathService(learningPathService);
		
		Identity identity = new TransientIdentity();
		TestingObligationContext obligationContext = new TestingObligationContext();
		BusinessGroupRef businessGroupRef = () -> 12L;
		obligationContext.addBusinessGroupRef(businessGroupRef);
		
		List<ExceptionalObligation> exceptionalObligations = new ArrayList<>();
		OrganisationExceptionalObligation hit = new OrganisationExceptionalObligation();
		hit.setType("trueType");
		hit.setObligation(AssessmentObligation.mandatory);
		exceptionalObligations.add(hit);
		OrganisationExceptionalObligation miss = new OrganisationExceptionalObligation();
		miss.setType("trueType");
		miss.setObligation(AssessmentObligation.excluded);
		exceptionalObligations.add(miss);
		
		Set<AssessmentObligation> filtered = sut.filterAssessmentObligation(identity, null, null,
				exceptionalObligations, AssessmentObligation.mandatory, obligationContext);
		
		assertThat(filtered)
				.contains(AssessmentObligation.excluded)
				.doesNotContain(AssessmentObligation.mandatory);
	}

	@Test
	public void filterShouldTestByHandler() {
		ConfigObligationEvaluator sut = new ConfigObligationEvaluator();
		LearningPathService learningPathService = mock(LearningPathService.class);
		ExceptionalObligationHandler trueHandler = mock(ExceptionalObligationHandler.class);
		ExceptionalObligationHandler falseHandler = mock(ExceptionalObligationHandler.class);
		when(trueHandler.matchesIdentity(any(), any(), any(), any(), any())).thenReturn(Boolean.TRUE);
		when(falseHandler.matchesIdentity(any(), any(), any(), any(), any())).thenReturn(Boolean.FALSE);
		when(learningPathService.getExceptionalObligationHandler("trueType")).thenReturn(trueHandler);
		when(learningPathService.getExceptionalObligationHandler("falseType")).thenReturn(falseHandler);
		sut.setLearningPathService(learningPathService);
		
		List<ExceptionalObligation> exceptionalObligations = new ArrayList<>();
		OrganisationExceptionalObligation hit = new OrganisationExceptionalObligation();
		hit.setType("trueType");
		hit.setObligation(AssessmentObligation.excluded);
		exceptionalObligations.add(hit);
		OrganisationExceptionalObligation miss = new OrganisationExceptionalObligation();
		miss.setType("falseType");
		miss.setObligation(AssessmentObligation.optional);
		exceptionalObligations.add(miss);
		
		Set<AssessmentObligation> filtered = sut.filterAssessmentObligation(null, null, null, exceptionalObligations,
				AssessmentObligation.mandatory, null);
		
		assertThat(filtered)
				.contains(AssessmentObligation.excluded)
				.doesNotContain(AssessmentObligation.optional);
	}
	
	@Test
	public void filterShouldIgnoreIfNoHandler() {
		ConfigObligationEvaluator sut = new ConfigObligationEvaluator();
		LearningPathService learningPathService = mock(LearningPathService.class);
		ExceptionalObligationHandler trueHandler = mock(ExceptionalObligationHandler.class);
		when(trueHandler.matchesIdentity(any(), any(), any(), any(), any())).thenReturn(Boolean.TRUE);
		when(learningPathService.getExceptionalObligationHandler("trueType")).thenReturn(trueHandler);
		sut.setLearningPathService(learningPathService);
		
		Identity identity = new TransientIdentity();
		TestingObligationContext obligationContext = new TestingObligationContext();
		BusinessGroupRef businessGroupRef = () -> 12L;
		obligationContext.addBusinessGroupRef(businessGroupRef);
		
		List<ExceptionalObligation> exceptionalObligations = new ArrayList<>();
		OrganisationExceptionalObligation hit = new OrganisationExceptionalObligation();
		hit.setType("trueType");
		hit.setObligation(AssessmentObligation.excluded);
		exceptionalObligations.add(hit);
		OrganisationExceptionalObligation miss = new OrganisationExceptionalObligation();
		miss.setType("someType");
		miss.setObligation(AssessmentObligation.optional);
		exceptionalObligations.add(miss);
		
		Set<AssessmentObligation> filtered = sut.filterAssessmentObligation(identity, null, null,
				exceptionalObligations, AssessmentObligation.mandatory, obligationContext);
		
		assertThat(filtered)
				.contains(AssessmentObligation.excluded)
				.doesNotContain(AssessmentObligation.optional);
	}

}
