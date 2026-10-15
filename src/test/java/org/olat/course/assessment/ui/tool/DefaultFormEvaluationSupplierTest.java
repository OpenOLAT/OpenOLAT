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
package org.olat.course.assessment.ui.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.olat.course.assessment.handler.AssessmentConfig.FormEvaluationScoreMode;
import org.olat.course.assessment.ui.tool.AssessmentParticipantViewController.DefaultFormEvaluationSupplier;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.ScaleType;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 15 Oct 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class DefaultFormEvaluationSupplierTest extends OlatTestCase {

	@Autowired
	private EvaluationFormManager evaluationFormManager;

	@Test
	public void shouldGetMaxScoreOneToMax() {
		Rubric rubric = createRubric(ScaleType.oneToMax, 5, 1);

		Double maxScore = DefaultFormEvaluationSupplier.getMaxScore(rubric, FormEvaluationScoreMode.sum, evaluationFormManager);

		assertThat(maxScore).isEqualTo(5.0);
	}

	@Test
	public void shouldGetMaxScoreZeroToMax() {
		Rubric rubric = createRubric(ScaleType.zeroToMax, 5, 1);

		Double maxScore = DefaultFormEvaluationSupplier.getMaxScore(rubric, FormEvaluationScoreMode.sum, evaluationFormManager);

		assertThat(maxScore).isEqualTo(4.0);
	}

	@Test
	public void shouldGetMaxScoreMaxToOne() {
		Rubric rubric = createRubric(ScaleType.maxToOne, 5, 1);

		Double maxScore = DefaultFormEvaluationSupplier.getMaxScore(rubric, FormEvaluationScoreMode.sum, evaluationFormManager);

		assertThat(maxScore).isEqualTo(5.0);
	}

	@Test
	public void shouldGetMaxScoreMaxToZero() {
		Rubric rubric = createRubric(ScaleType.maxToZero, 5, 1);

		Double maxScore = DefaultFormEvaluationSupplier.getMaxScore(rubric, FormEvaluationScoreMode.sum, evaluationFormManager);

		assertThat(maxScore).isEqualTo(4.0);
	}

	@Test
	public void shouldGetMaxScoreBalanced() {
		Rubric rubric = createRubric(ScaleType.zeroBallanced, 5, 1);

		Double maxScore = DefaultFormEvaluationSupplier.getMaxScore(rubric, FormEvaluationScoreMode.sum, evaluationFormManager);

		assertThat(maxScore).isEqualTo(2.0);
	}

	@Test
	public void shouldGetMaxScoreManySteps() {
		Rubric rubric = createRubric(ScaleType.maxToZero, 100, 1);

		Double maxScore = DefaultFormEvaluationSupplier.getMaxScore(rubric, FormEvaluationScoreMode.sum, evaluationFormManager);

		assertThat(maxScore).isEqualTo(99.0);
	}

	@Test
	public void shouldGetMaxScoreSumOfSliders() {
		Rubric rubric = createRubric(ScaleType.maxToZero, 5, 3);

		Double maxScore = DefaultFormEvaluationSupplier.getMaxScore(rubric, FormEvaluationScoreMode.sum, evaluationFormManager);

		assertThat(maxScore).isEqualTo(12.0);
	}

	@Test
	public void shouldGetMaxScoreAvg() {
		Rubric rubric = createRubric(ScaleType.maxToZero, 5, 3);

		Double maxScore = DefaultFormEvaluationSupplier.getMaxScore(rubric, FormEvaluationScoreMode.avg, evaluationFormManager);

		assertThat(maxScore).isEqualTo(4.0);
	}

	private Rubric createRubric(ScaleType scaleType, int steps, int numberOfSliders) {
		Rubric rubric = new Rubric();
		rubric.setScaleType(scaleType);
		rubric.setSteps(steps);
		List<Slider> sliders = new ArrayList<>();
		for (int i = 0; i < numberOfSliders; i++) {
			Slider slider = new Slider();
			slider.setId(random());
			sliders.add(slider);
		}
		rubric.setSliders(sliders);
		return rubric;
	}

}
