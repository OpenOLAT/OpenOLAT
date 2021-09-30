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

import static org.olat.core.util.DateUtils.toDate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.LastModificationsEvaluator.LastModifications;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 18 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class STLastModificationsEvaluatorTest {
	
	private STLastModificationsEvaluator sut = new STLastModificationsEvaluator();
	
	@Test
	public void shouldGetLaterFromChildren() {
		Date lastUserModified = toDate(LocalDateTime.of(2012, 8, 19, 12, 0, 0));
		Date lastCoachModified = toDate(LocalDateTime.of(2012, 1, 9, 0, 1, 1));
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(lastUserModified, lastCoachModified, AssessmentObligation.mandatory);
		Date lastUserModifiedChild1 = toDate(LocalDateTime.of(2016, 8, 19, 12, 0, 0));
		Date lastCoachModifiedChild1 = lastCoachModified;
		AssessmentEvaluation evaluationChild1 = createAssessmentEvaluation(lastUserModifiedChild1, lastCoachModifiedChild1, AssessmentObligation.mandatory);
		Date lastUserModifiedChild2 = lastUserModified;
		Date lastCoachModifiedChild2 = toDate(LocalDateTime.of(2016, 8, 19, 12, 0, 0));
		AssessmentEvaluation evaluationChild2 = createAssessmentEvaluation(lastUserModifiedChild2, lastCoachModifiedChild2, AssessmentObligation.mandatory);
		List<AssessmentEvaluation> children = Arrays.asList(evaluationChild1, evaluationChild2);
		
		LastModifications lastModifications = sut.getLastModifications(currentEvaluation, children);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(lastModifications.getLastUserModified()).isEqualTo(lastUserModifiedChild1);
		softly.assertThat(lastModifications.getLastCoachModified()).isEqualTo(lastCoachModifiedChild2);
		softly.assertAll();
	}
	
	@Test
	public void shouldNotChangeDatedIfItHasNoChildren() {
		Date lastUserModified = toDate(LocalDateTime.of(2016, 8, 19, 12, 0, 0));
		Date lastCoachModified = toDate(LocalDateTime.of(2012, 1, 9, 0, 1, 1));
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(lastUserModified, lastCoachModified, AssessmentObligation.mandatory);
		
		LastModifications lastModifications = sut.getLastModifications(currentEvaluation, Collections.emptyList());
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(lastModifications.getLastUserModified()).isEqualTo(lastUserModified);
		softly.assertThat(lastModifications.getLastCoachModified()).isEqualTo(lastCoachModified);
		softly.assertAll();
	}
	
	@Test
	public void shouldIgnoreInvisibleChildren() {
		Date lastUserModified = toDate(LocalDateTime.of(2012, 8, 19, 12, 0, 0));
		Date lastCoachModified = toDate(LocalDateTime.of(2012, 1, 9, 0, 1, 1));
		AssessmentEvaluation currentEvaluation = createAssessmentEvaluation(lastUserModified, lastCoachModified, AssessmentObligation.mandatory);
		Date lastUserModifiedChild1 = toDate(LocalDateTime.of(2016, 8, 19, 12, 0, 0));
		Date lastCoachModifiedChild1 = toDate(LocalDateTime.of(2016, 8, 19, 12, 0, 0));
		AssessmentEvaluation evaluationChild1 = createAssessmentEvaluation(lastUserModifiedChild1, lastCoachModifiedChild1, AssessmentObligation.mandatory);
		Date lastUserModifiedChild2 = toDate(LocalDateTime.of(2017, 8, 19, 12, 0, 0));
		Date lastCoachModifiedChild2 = toDate(LocalDateTime.of(2017, 8, 19, 12, 0, 0));
		AssessmentEvaluation evaluationChild2 = createAssessmentEvaluation(lastUserModifiedChild2, lastCoachModifiedChild2, AssessmentObligation.excluded);
		List<AssessmentEvaluation> children = Arrays.asList(evaluationChild1, evaluationChild2);
		
		LastModifications lastModifications = sut.getLastModifications(currentEvaluation, children);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(lastModifications.getLastUserModified()).isEqualTo(lastUserModifiedChild1);
		softly.assertThat(lastModifications.getLastCoachModified()).isEqualTo(lastCoachModifiedChild1);
		softly.assertAll();
	}


	private AssessmentEvaluation createAssessmentEvaluation(Date lastUserModified, Date lastCoachModified, AssessmentObligation obligation) {
		return new AssessmentEvaluation(null, null, null, null, null, null, null, null, null, null, null, null, null,
				null, null, null, 0, null, lastUserModified, lastCoachModified, null, null, null, Overridable.of(obligation), null, null, null);
	}

}
