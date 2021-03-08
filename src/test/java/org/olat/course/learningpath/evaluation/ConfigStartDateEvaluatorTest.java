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

import java.time.LocalDate;
import java.util.Date;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.util.DateUtils;
import org.olat.course.nodes.st.assessment.SequentialBlocker;
import org.olat.course.run.scoring.Blocker;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 5 Nov 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ConfigStartDateEvaluatorTest {
	
	private ConfigStartDateEvaluator sut = new ConfigStartDateEvaluator();
	
	@Test
	public void shouldBlockIfStartIsInFuture() {
		SoftAssertions softly = new SoftAssertions();
		Date inThreeDays = DateUtils.toDate(LocalDate.now().plusDays(3));
		
		Blocker blocker = new SequentialBlocker();
		Date startDate = sut.evaluateDate(inThreeDays, AssessmentObligation.mandatory, blocker);
		
		softly.assertThat(startDate).as("mandatory: start").isEqualTo(inThreeDays);
		softly.assertThat(blocker.getStartDate()).as("mandatory: start (blocker)").isEqualTo(inThreeDays);
		softly.assertThat(blocker.isBlocked()).as("mandatory: blocked").isTrue();
		blocker.nextCourseNode();
		softly.assertThat(blocker.isBlocked()).as("mandatory: block without pass through").isTrue();
		
		blocker = new SequentialBlocker();
		startDate = sut.evaluateDate(inThreeDays, AssessmentObligation.optional, blocker);
		
		softly.assertThat(startDate).as("optional: start").isEqualTo(inThreeDays);
		// If optional, the blocker has no date because block without pass through
		softly.assertThat(blocker.getStartDate()).as("optional: start (blocker)").isNull();
		softly.assertThat(blocker.isBlocked()).as("optional: blocked").isTrue();
		blocker.nextCourseNode();
		softly.assertThat(blocker.isBlocked()).as("optional: block without pass through").isFalse();
		
		softly.assertAll();
	}
	
	@Test
	public void shouldNotBlockIfStartIsInPast() {
		SoftAssertions softly = new SoftAssertions();
		Date threeDaysBefore = DateUtils.toDate(LocalDate.now().minusDays(3));
		
		Blocker blocker = new SequentialBlocker();
		Date startDate = sut.evaluateDate(threeDaysBefore, AssessmentObligation.mandatory, blocker);
		
		softly.assertThat(startDate).as("mandatory: start").isNull();
		softly.assertThat(blocker.getStartDate()).as("mandatory: start (blocker)").isNull();
		softly.assertThat(blocker.isBlocked()).as("mandatory: blocked").isFalse();
		
		blocker = new SequentialBlocker();
		startDate = sut.evaluateDate(threeDaysBefore, AssessmentObligation.optional, blocker);
		
		softly.assertThat(startDate).as("optional: start").isNull();
		softly.assertThat(blocker.getStartDate()).as("optional: start (blocker)").isNull();
		softly.assertThat(blocker.isBlocked()).as("optional: blocked").isFalse();
		softly.assertAll();
	}
	
	@Test
	public void shouldNotBlockIfHasNoStart() {
		SoftAssertions softly = new SoftAssertions();
		
		Blocker blocker = new SequentialBlocker();
		Date startDate = sut.evaluateDate(null, AssessmentObligation.mandatory, blocker);
		
		softly.assertThat(startDate).as("mandatory: start").isNull();
		softly.assertThat(blocker.getStartDate()).as("mandatory: start (blocker)").isNull();
		softly.assertThat(blocker.isBlocked()).as("mandatory: blocked").isFalse();
		
		blocker = new SequentialBlocker();
		startDate = sut.evaluateDate(null, AssessmentObligation.optional, blocker);
		
		softly.assertThat(startDate).as("optional: start").isNull();
		softly.assertThat(blocker.getStartDate()).as("optional: start (blocker)").isNull();
		softly.assertThat(blocker.isBlocked()).as("optional: blocked").isFalse();
		softly.assertAll();
	}

	@Test
	public void shouldReturnBlockerStartIfBlockerIsLater() {
		SoftAssertions softly = new SoftAssertions();
		Date inFourDays = DateUtils.toDate(LocalDate.now().plusDays(4));
		Date inThreeDays = DateUtils.toDate(LocalDate.now().plusDays(3));

		Blocker blocker = new SequentialBlocker();
		blocker.block(inFourDays);
		Date startDate = sut.evaluateDate(inThreeDays, AssessmentObligation.mandatory, blocker);
		
		softly.assertThat(startDate).as("mandatory: start").isEqualTo(inFourDays);
		softly.assertThat(blocker.getStartDate()).as("mandatory: start (blocker)").isEqualTo(inFourDays);
		softly.assertThat(blocker.isBlocked()).as("mandatory: blocked").isTrue();
		
		blocker = new SequentialBlocker();
		blocker.block(inFourDays);
		startDate = sut.evaluateDate(inThreeDays, AssessmentObligation.optional, blocker);
		
		softly.assertThat(startDate).as("optional: start").isEqualTo(inFourDays);
		softly.assertThat(blocker.getStartDate()).as("optional: start (blocker)").isEqualTo(inFourDays);
		softly.assertThat(blocker.isBlocked()).as("optional: blocked").isTrue();
		softly.assertAll();
	}

	@Test
	public void shouldReturnNodeStartIfNodeIsLater() {
		SoftAssertions softly = new SoftAssertions();
		Date inTwoDays = DateUtils.toDate(LocalDate.now().plusDays(2));
		Date inThreeDays = DateUtils.toDate(LocalDate.now().plusDays(3));

		Blocker blocker = new SequentialBlocker();
		blocker.block(inTwoDays);
		Date startDate = sut.evaluateDate(inThreeDays, AssessmentObligation.mandatory, blocker);
		
		softly.assertThat(startDate).as("mandatory: start").isEqualTo(inThreeDays);
		softly.assertThat(blocker.getStartDate()).as("mandatory: start (blocker)").isEqualTo(inThreeDays);
		softly.assertThat(blocker.isBlocked()).as("mandatory: blocked").isTrue();
		
		blocker = new SequentialBlocker();
		blocker.block(inTwoDays);
		startDate = sut.evaluateDate(inThreeDays, AssessmentObligation.optional, blocker);
		
		softly.assertThat(startDate).as("optional: start").isEqualTo(inThreeDays);
		// Blocker date if optional
		softly.assertThat(blocker.getStartDate()).as("optional: start (blocker)").isEqualTo(inTwoDays);
		softly.assertThat(blocker.isBlocked()).as("optional: blocked").isTrue();
		softly.assertAll();
	}

	@Test
	public void shouldReturnBlockerStartIfNodeIsNotBlocking() {
		SoftAssertions softly = new SoftAssertions();
		Date inFourDays = DateUtils.toDate(LocalDate.now().plusDays(4));
		
		Blocker blocker = new SequentialBlocker();
		blocker.block(inFourDays);
		Date startDate = sut.evaluateDate(null, AssessmentObligation.mandatory, blocker);
		
		softly.assertThat(startDate).as("mandatory: start").isEqualTo(inFourDays);
		softly.assertThat(blocker.getStartDate()).as("mandatory: start (blocker)").isEqualTo(inFourDays);
		softly.assertThat(blocker.isBlocked()).as("mandatory: blocked").isTrue();
		
		blocker = new SequentialBlocker();
		blocker.block(inFourDays);
		startDate = sut.evaluateDate(null, AssessmentObligation.optional, blocker);
		
		softly.assertThat(startDate).as("optional: start").isEqualTo(inFourDays);
		softly.assertThat(blocker.getStartDate()).as("optional: start (blocker)").isEqualTo(inFourDays);
		softly.assertThat(blocker.isBlocked()).as("optional: blocked").isTrue();
		softly.assertAll();
	}

}
