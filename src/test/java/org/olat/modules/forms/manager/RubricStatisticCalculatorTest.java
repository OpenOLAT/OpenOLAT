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
package org.olat.modules.forms.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.olat.test.JunitTestHelper.random;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.modules.forms.RubricStatistic;
import org.olat.modules.forms.SliderStatistic;
import org.olat.modules.forms.StepCounts;
import org.olat.modules.forms.manager.RubricStatisticCalculator.SumMean;
import org.olat.modules.forms.model.SliderStatisticImpl;
import org.olat.modules.forms.model.SlidersStatisticImpl;
import org.olat.modules.forms.model.StepCountsImpl;
import org.olat.modules.forms.model.jpa.CalculatedLong;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.ScaleType;
import org.olat.modules.forms.model.xml.Slider;

/**
 * 
 * Initial date: 25 Jun 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RubricStatisticCalculatorTest {
	
	private RubricStatisticCalculator sut = new RubricStatisticCalculator();
	
	@Test
	public void shouldCountNoResponses() {
		long expectedCounts = 10;
		Slider slider = new Slider();
		String sliderId = random();
		slider.setId(sliderId);
		List<CalculatedLong> countedNoResponses = new ArrayList<>();
		countedNoResponses.add(new CalculatedLong(random(), 3));
		countedNoResponses.add(new CalculatedLong(sliderId, expectedCounts));
		countedNoResponses.add(new CalculatedLong(random(), 5));
		
		Long countNoResponses = sut.getCountNoResponses(slider, countedNoResponses);

		assertThat(countNoResponses).isEqualTo(expectedCounts);
	}
	
	@Test
	public void shouldCountNoResponsesDefault() {
		Slider slider = new Slider();
		String sliderId = random();
		slider.setId(sliderId);
		List<CalculatedLong> countedNoResponses = new ArrayList<>();
		countedNoResponses.add(new CalculatedLong(random(), 3));
		
		Long countNoResponses = sut.getCountNoResponses(slider, countedNoResponses);

		assertThat(countNoResponses).isEqualTo(0);
	}
	
	@Test
	public void shouldGetSliderStepCounts() {
		Long countStep1 = Long.valueOf(9);
		Long countStep3 = Long.valueOf(8);
		Slider slider = new Slider();
		String sliderId = random();
		slider.setId(sliderId);
		List<CalculatedLong> countedResponses = new ArrayList<>();
		countedResponses.add(new CalculatedLong(sliderId, BigDecimal.valueOf(1), countStep1));
		countedResponses.add(new CalculatedLong(random(), BigDecimal.valueOf(2), -1));
		countedResponses.add(new CalculatedLong(sliderId, BigDecimal.valueOf(3), countStep3));
		
		StepCounts stepCounts = sut.getStepCounts(slider, 3, countedResponses);

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(stepCounts.getCount(1)).isEqualTo(countStep1);
		softly.assertThat(stepCounts.getCount(2)).isEqualTo(0);
		softly.assertThat(stepCounts.getCount(3)).isEqualTo(countStep3);
		softly.assertAll();
	}
	
	@Test
	public void shouldCalculateSliderCount() {
		StepCounts stepCounts = new StepCountsImpl(6);
		stepCounts.setCount(1, Long.valueOf(2));
		stepCounts.setCount(2, Long.valueOf(15));
		stepCounts.setCount(3, Long.valueOf(3));
		stepCounts.setCount(4, Long.valueOf(0));
		stepCounts.setCount(5, Long.valueOf(4));
		stepCounts.setCount(6, Long.valueOf(1));
		
		Long count = sut.getCount(stepCounts);
		
		assertThat(count).isEqualTo(25);
	}

	@Test
	public void shouldCalculateSliderSumMean() {
		StepCounts stepCounts = new StepCountsImpl(6);
		stepCounts.setCount(1, Long.valueOf(2));
		stepCounts.setCount(2, Long.valueOf(15));
		stepCounts.setCount(3, Long.valueOf(3));
		stepCounts.setCount(4, Long.valueOf(0));
		stepCounts.setCount(5, Long.valueOf(4));
		stepCounts.setCount(6, Long.valueOf(1));
		
		SumMean sumMean = sut.getSumMean(stepCounts, ScaleType.oneToMax);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sumMean.getSum()).isEqualTo(67);
		softly.assertThat(sumMean.getMean()).isEqualTo(2.68);
		softly.assertAll();
	}
	
	@Test
	public void shouldCalculateSliderSumMeanScaled() {
		StepCounts stepCounts = new StepCountsImpl(6);
		stepCounts.setCount(1, Long.valueOf(2));
		stepCounts.setCount(2, Long.valueOf(15));
		stepCounts.setCount(3, Long.valueOf(3));
		stepCounts.setCount(4, Long.valueOf(0));
		stepCounts.setCount(5, Long.valueOf(4));
		stepCounts.setCount(6, Long.valueOf(1));
		
		SumMean sumMean = sut.getSumMean(stepCounts, ScaleType.zeroToMax);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sumMean.getSum()).isEqualTo(42);
		softly.assertThat(sumMean.getMean()).isEqualTo(1.68);
		softly.assertAll();
	}
	
	@Test
	public void shouldCalculateMean() {
		StepCounts stepCounts = new StepCountsImpl(3);
		stepCounts.setCount(1, Long.valueOf(3));
		stepCounts.setCount(2, Long.valueOf(6));
		stepCounts.setCount(3, Long.valueOf(1));
		
		Double median = sut.getMedian(stepCounts, ScaleType.oneToMax);
		
		assertThat(median).isEqualTo(2);
	}
	
	@Test
	public void shouldCalculateMeanBetween() {
		StepCounts stepCounts = new StepCountsImpl(3);
		stepCounts.setCount(1, Long.valueOf(1));
		stepCounts.setCount(2, Long.valueOf(1));
		stepCounts.setCount(3, Long.valueOf(0));
		
		Double median = sut.getMedian(stepCounts, ScaleType.oneToMax);
		
		assertThat(median).isEqualTo(1.5);
	}
	
	@Test
	public void shouldCalculateMeanNoValues() {
		StepCounts stepCounts = new StepCountsImpl(3);
		stepCounts.setCount(1, Long.valueOf(0));
		stepCounts.setCount(2, Long.valueOf(0));
		stepCounts.setCount(3, Long.valueOf(0));
		
		Double median = sut.getMedian(stepCounts, ScaleType.oneToMax);
		
		assertThat(median).isNull();
	}

	@Test
	public void shouldCalculateVariance() {
		ScaleType scaleType = ScaleType.oneToMax;
		StepCounts stepCounts = new StepCountsImpl(4);
		stepCounts.setCount(1, Long.valueOf(2));
		stepCounts.setCount(2, Long.valueOf(2));
		stepCounts.setCount(3, Long.valueOf(0));
		stepCounts.setCount(4, Long.valueOf(1));
		SumMean sumMean = sut.getSumMean(stepCounts, scaleType);
		
		Double variance = sut.getVariance(stepCounts, scaleType, sumMean.getMean());
		
		assertThat(variance).isEqualTo(1.5);
	}

	@Test
	public void shouldCalculateVarianceScaled() {
		ScaleType scaleType = ScaleType.zeroBallanced;
		StepCounts stepCounts = new StepCountsImpl(4);
		stepCounts.setCount(1, Long.valueOf(2));
		stepCounts.setCount(2, Long.valueOf(2));
		stepCounts.setCount(3, Long.valueOf(0));
		stepCounts.setCount(4, Long.valueOf(1));
		SumMean sumMean = sut.getSumMean(stepCounts, scaleType);
		
		Double variance = sut.getVariance(stepCounts, scaleType, sumMean.getMean());
		
		assertThat(variance).isEqualTo(1.5);
	}

	@Test
	public void shouldGetTotalStepCount() {
		List<Slider> sliders = new ArrayList<>();
		SlidersStatisticImpl slidersStatisticImpl = new SlidersStatisticImpl();
		Slider slider1 = new Slider();
		slider1.setId(random());
		sliders.add(slider1);
		StepCounts stepCount1 = new StepCountsImpl(3);
		stepCount1.setCount(1, Long.valueOf(3));
		stepCount1.setCount(2, Long.valueOf(3));
		stepCount1.setCount(3, Long.valueOf(3));
		slidersStatisticImpl.put(slider1, new SliderStatisticImpl(null, null, null, null, null, null, null, stepCount1 , null));
		Slider slider2 = new Slider();
		slider2.setId(random());
		sliders.add(slider2);
		StepCounts stepCount2 = new StepCountsImpl(3);
		stepCount2.setCount(1, Long.valueOf(3));
		stepCount2.setCount(2, Long.valueOf(3));
		stepCount2.setCount(3, Long.valueOf(3));
		slidersStatisticImpl.put(slider2, new SliderStatisticImpl(null, null, null, null, null, null, null, stepCount2 , null));
		Slider slider3 = new Slider();
		slider3.setId(random());
		slider3.setWeight(3);
		sliders.add(slider3);
		StepCounts stepCount3 = new StepCountsImpl(3);
		stepCount3.setCount(1, Long.valueOf(1));
		stepCount3.setCount(2, Long.valueOf(2));
		stepCount3.setCount(3, Long.valueOf(3));
		slidersStatisticImpl.put(slider3, new SliderStatisticImpl(null, null, null, null, null, null, null, stepCount3 , null));
		Rubric rubric = new Rubric();
		rubric.setSliders(sliders);
		rubric.setSteps(3);
		
		SoftAssertions softly = new SoftAssertions();
		StepCounts totalStepCountsUnweighted = sut.getTotalStepCounts(rubric, slidersStatisticImpl, false);
		softly.assertThat(totalStepCountsUnweighted.getCount(1)).isEqualTo(7);
		softly.assertThat(totalStepCountsUnweighted.getCount(2)).isEqualTo(8);
		softly.assertThat(totalStepCountsUnweighted.getCount(3)).isEqualTo(9);
		StepCounts totalStepCountsWeighted = sut.getTotalStepCounts(rubric, slidersStatisticImpl, true);
		softly.assertThat(totalStepCountsWeighted.getCount(1)).isEqualTo(9);
		softly.assertThat(totalStepCountsWeighted.getCount(2)).isEqualTo(12);
		softly.assertThat(totalStepCountsWeighted.getCount(3)).isEqualTo(15);
		softly.assertAll();
	}

	@Test
	public void shouldCalculateRubricTotal() {
		List<Slider> sliders = new ArrayList<>();
		SlidersStatisticImpl slidersStatisticImpl = new SlidersStatisticImpl();
		Slider slider1 = new Slider();
		slider1.setId(random());
		sliders.add(slider1);
		StepCounts stepCount1 = new StepCountsImpl(3);
		stepCount1.setCount(1, Long.valueOf(1));
		stepCount1.setCount(2, Long.valueOf(1));
		stepCount1.setCount(3, Long.valueOf(1));
		slidersStatisticImpl.put(slider1, new SliderStatisticImpl(Long.valueOf(1), null, null, null, null, null, null, stepCount1 , null));
		Slider slider2 = new Slider();
		slider2.setId(random());
		sliders.add(slider2);
		StepCounts stepCount2 = new StepCountsImpl(3);
		stepCount2.setCount(1, Long.valueOf(2));
		stepCount2.setCount(2, Long.valueOf(2));
		stepCount2.setCount(3, Long.valueOf(2));
		slidersStatisticImpl.put(slider2, new SliderStatisticImpl(Long.valueOf(2), null, null, null, null, null, null, stepCount2 , null));
		Slider slider3 = new Slider();
		slider3.setId(random());
		slider3.setWeight(3);
		sliders.add(slider3);
		StepCounts stepCount3 = new StepCountsImpl(3);
		stepCount3.setCount(1, Long.valueOf(3));
		stepCount3.setCount(2, Long.valueOf(4));
		stepCount3.setCount(3, Long.valueOf(5));
		slidersStatisticImpl.put(slider3, new SliderStatisticImpl(Long.valueOf(3), null, null, null, null, null, null, stepCount3 , null));
		Rubric rubric = new Rubric();
		rubric.setSliders(sliders);
		rubric.setSteps(3);
		
		RubricStatistic rubricStatistic = sut.calculateRubricStatistics(rubric, slidersStatisticImpl);
		SliderStatistic totalStatistic = rubricStatistic.getTotalStatistic();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(totalStatistic.getNumberOfNoResponses()).isEqualTo(6);
		softly.assertThat(totalStatistic.getNumberOfResponses()).isEqualTo(21);
		softly.assertThat(totalStatistic.getSum()).isEqualTo(96);
		softly.assertThat(totalStatistic.getAvg()).isEqualTo(2.13, offset(0.01));
		softly.assertThat(totalStatistic.getMedian()).isEqualTo(2);
		softly.assertThat(totalStatistic.getVariance()).isEqualTo(0.663, offset(0.001));
		softly.assertThat(totalStatistic.getStdDev()).isEqualTo(0.814, offset(0.001));
		softly.assertAll();
	}

}
