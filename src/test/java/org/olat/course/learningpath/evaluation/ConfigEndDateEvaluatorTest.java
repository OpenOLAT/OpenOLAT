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
import static org.olat.modules.assessment.model.AssessmentObligation.mandatory;
import static org.olat.modules.assessment.model.AssessmentObligation.optional;

import java.time.LocalDate;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;
import org.olat.core.util.DateUtils;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.SPCourseNode;
import org.olat.course.nodes.st.assessment.SequentialBlocker;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.Blocker;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 26 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ConfigEndDateEvaluatorTest {
	
	@Test
	public void shouldGetConfigDate() {
		Date configDate = new GregorianCalendar(2017, 2, 3, 1, 2 ,3).getTime();
		CourseNode courseNode = new SPCourseNode();
		AssessmentEvaluation currentEvaluation = createEvaluation(mandatory);

		ConfigEndDateEvaluator sut = new ConfigEndDateEvaluator(cn -> configDate);
		Overridable<Date> endDate = sut.getEndDate(currentEvaluation, courseNode, new SequentialBlocker(AssessmentObligation.mandatory));
		
		assertThat(endDate.getCurrent()).isEqualTo(configDate);
	}
	
	@Test
	public void shouldGetOverridenDate() {
		Date configDate = new GregorianCalendar(2017, 2, 3, 1, 2 ,3).getTime();
		Date overriddenDate = new GregorianCalendar(2017, 2, 3, 1, 2 ,3).getTime();
		CourseNode courseNode = new SPCourseNode();
		AssessmentEvaluation currentEvaluation = createEvaluation(mandatory);
		currentEvaluation.getEndDate().setCurrent(configDate);
		currentEvaluation.getEndDate().override(overriddenDate, null, null);

		ConfigEndDateEvaluator sut = new ConfigEndDateEvaluator(cn -> configDate);
		Overridable<Date> endDate = sut.getEndDate(currentEvaluation, courseNode, new SequentialBlocker(AssessmentObligation.mandatory));
		
		assertThat(endDate.getCurrent()).isEqualTo(overriddenDate);
		assertThat(endDate.getOriginal()).isEqualTo(configDate);
	}
	
	@Test
	public void shouldGetNoDateIfOptional() {
		Date configDate = new GregorianCalendar(2017, 2, 3, 1, 2 ,3).getTime();
		Date overriddenDate = new GregorianCalendar(2017, 2, 3, 1, 2 ,3).getTime();
		CourseNode courseNode = new SPCourseNode();
		AssessmentEvaluation currentEvaluation = createEvaluation(optional);
		currentEvaluation.getEndDate().setCurrent(configDate);
		currentEvaluation.getEndDate().override(overriddenDate, null, null);
		
		ConfigEndDateEvaluator sut = new ConfigEndDateEvaluator(cn -> configDate);
		Overridable<Date> endDate = sut.getEndDate(currentEvaluation, courseNode, new SequentialBlocker(AssessmentObligation.mandatory));
		
		assertThat(endDate.getCurrent()).isNull();
		assertThat(endDate.getOriginal()).isNull();
	}

	@Test
	public void shouldNotBlockIfNotFullyAssessedAndConfigDateNotOver() {
		Blocker blocker = new SequentialBlocker(AssessmentObligation.mandatory);
		Date configDateNotOver = DateUtils.toDate(LocalDate.now().plusDays(3));
		
		ConfigEndDateEvaluator sut = new ConfigEndDateEvaluator();
		sut.evaluateBlocker(Boolean.FALSE, configDateNotOver, blocker);
		
		assertThat(blocker.isBlocked()).isFalse();
	}

	@Test
	public void shouldNotBlockIfFullyAssessedAndConfigDateNotOver() {
		Blocker blocker = new SequentialBlocker(AssessmentObligation.mandatory);
		Date configDateNotOver = DateUtils.toDate(LocalDate.now().plusDays(3));
		
		ConfigEndDateEvaluator sut = new ConfigEndDateEvaluator();
		sut.evaluateBlocker(Boolean.TRUE, configDateNotOver, blocker);
		
		assertThat(blocker.isBlocked()).isFalse();
	}

	@Test
	public void shouldNotBlockIfUnknownFullyAssessedAndConfigDateNotOver() {
		Blocker blocker = new SequentialBlocker(AssessmentObligation.mandatory);
		Date configDateNotOver = DateUtils.toDate(LocalDate.now().plusDays(3));
		
		ConfigEndDateEvaluator sut = new ConfigEndDateEvaluator();
		sut.evaluateBlocker(null, configDateNotOver, blocker);
		
		assertThat(blocker.isBlocked()).isFalse();
	}

	@Test
	public void shouldBlockIfNotFullyAssessedAndConfigDateOver() {
		Blocker blocker = new SequentialBlocker(AssessmentObligation.mandatory);
		Date configDateOver = DateUtils.toDate(LocalDate.now().minusDays(3));
		
		ConfigEndDateEvaluator sut = new ConfigEndDateEvaluator();
		sut.evaluateBlocker(Boolean.FALSE, configDateOver, blocker);
		
		assertThat(blocker.isBlocked()).isTrue();
	}

	@Test
	public void shouldNotBlockIfFullyAssessedAndConfigDateOver() {
		Blocker blocker = new SequentialBlocker(AssessmentObligation.mandatory);
		Date configDateOver = DateUtils.toDate(LocalDate.now().minusDays(3));
		
		ConfigEndDateEvaluator sut = new ConfigEndDateEvaluator();
		sut.evaluateBlocker(Boolean.TRUE, configDateOver, blocker);
		
		assertThat(blocker.isBlocked()).isFalse();
	}

	@Test
	public void shouldBlockIfUnknownFullyAssessedAndConfigDateOver() {
		Blocker blocker = new SequentialBlocker(AssessmentObligation.mandatory);
		Date configDateOver = DateUtils.toDate(LocalDate.now().minusDays(3));
		
		ConfigEndDateEvaluator sut = new ConfigEndDateEvaluator();
		sut.evaluateBlocker(null, configDateOver, blocker);
		
		assertThat(blocker.isBlocked()).isTrue();
	}
	
	private AssessmentEvaluation createEvaluation(AssessmentObligation obligation) {
		return new AssessmentEvaluation(null, null, null, null, null, null, null, null, null, null, null, null, null,
				null, null, null, 0, null, null, null, null, null, Overridable.empty(), ObligationOverridable.of(obligation), null,
				null, null);
	}


}
