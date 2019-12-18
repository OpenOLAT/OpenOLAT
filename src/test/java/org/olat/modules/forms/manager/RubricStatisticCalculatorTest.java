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
import org.olat.modules.forms.SliderStatistic;
import org.olat.modules.forms.StepCounts;
import org.olat.modules.forms.manager.RubricStatisticCalculator.SumMean;
import org.olat.modules.forms.model.SlidersStepCountsImpl;
import org.olat.modules.forms.model.StepCountsBuilder;
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
	public void shouldGetSliderCountNoResponses() {
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
	public void shouldGetSliderCountNoResponsesDefault() {
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
		
		StepCounts stepCounts = sut.getStepCounts(slider, 3, countedResponses).build();

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(stepCounts.getStepCount(1)).isEqualTo(countStep1);
		softly.assertThat(stepCounts.getStepCount(2)).isEqualTo(0);
		softly.assertThat(stepCounts.getStepCount(3)).isEqualTo(countStep3);
		softly.assertAll();
	}
	
	@Test
	public void shouldCalculateSliderCount() {
		StepCountsBuilder stepCountsBuilder = StepCountsBuilder.builder(6);
		stepCountsBuilder.withCount(1, Long.valueOf(2));
		stepCountsBuilder.withCount(2, Long.valueOf(15));
		stepCountsBuilder.withCount(3, Long.valueOf(3));
		stepCountsBuilder.withCount(4, Long.valueOf(0));
		stepCountsBuilder.withCount(5, Long.valueOf(4));
		stepCountsBuilder.withCount(6, Long.valueOf(1));
		
		Long count = sut.getCount(stepCountsBuilder.build());
		
		assertThat(count).isEqualTo(25);
	}

	@Test
	public void shouldCalculateSliderSumMean() {
		StepCountsBuilder stepCountsBuilder = StepCountsBuilder.builder(6);
		stepCountsBuilder.withCount(1, Long.valueOf(2));
		stepCountsBuilder.withCount(2, Long.valueOf(15));
		stepCountsBuilder.withCount(3, Long.valueOf(3));
		stepCountsBuilder.withCount(4, Long.valueOf(0));
		stepCountsBuilder.withCount(5, Long.valueOf(4));
		stepCountsBuilder.withCount(6, Long.valueOf(1));
		
		SumMean sumMean = sut.getSumMean(stepCountsBuilder.build(), ScaleType.oneToMax);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sumMean.getSum()).isEqualTo(67);
		softly.assertThat(sumMean.getMean()).isEqualTo(2.68);
		softly.assertAll();
	}
	
	@Test
	public void shouldCalculateSliderSumMeanScaled() {
		StepCountsBuilder stepCountsBuilder = StepCountsBuilder.builder(6);
		stepCountsBuilder.withCount(1, Long.valueOf(2));
		stepCountsBuilder.withCount(2, Long.valueOf(15));
		stepCountsBuilder.withCount(3, Long.valueOf(3));
		stepCountsBuilder.withCount(4, Long.valueOf(0));
		stepCountsBuilder.withCount(5, Long.valueOf(4));
		stepCountsBuilder.withCount(6, Long.valueOf(1));
		
		SumMean sumMean = sut.getSumMean(stepCountsBuilder.build(), ScaleType.zeroToMax);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sumMean.getSum()).isEqualTo(42);
		softly.assertThat(sumMean.getMean()).isEqualTo(1.68);
		softly.assertAll();
	}
	
	@Test
	public void shouldCalculateSliderMedian() {
		StepCountsBuilder stepCountsBuilder = StepCountsBuilder.builder(3);
		stepCountsBuilder.withCount(1, Long.valueOf(3));
		stepCountsBuilder.withCount(2, Long.valueOf(6));
		stepCountsBuilder.withCount(3, Long.valueOf(1));
		
		Double median = sut.getMedian(stepCountsBuilder.build(), ScaleType.oneToMax);
		
		assertThat(median).isEqualTo(2);
	}
	
	@Test
	public void shouldCalculateSliderMedianBetween() {
		StepCountsBuilder stepCountsBuilder = StepCountsBuilder.builder(3);
		stepCountsBuilder.withCount(1, Long.valueOf(1));
		stepCountsBuilder.withCount(2, Long.valueOf(1));
		stepCountsBuilder.withCount(3, Long.valueOf(0));
		
		Double median = sut.getMedian(stepCountsBuilder.build(), ScaleType.oneToMax);
		
		assertThat(median).isEqualTo(1.5);
	}
	
	@Test
	public void shouldCalculateSliderMedianNoValues() {
		StepCountsBuilder stepCountsBuilder = StepCountsBuilder.builder(3);
		stepCountsBuilder.withCount(1, Long.valueOf(0));
		stepCountsBuilder.withCount(2, Long.valueOf(0));
		stepCountsBuilder.withCount(3, Long.valueOf(0));
		
		Double median = sut.getMedian(stepCountsBuilder.build(), ScaleType.oneToMax);
		
		assertThat(median).isNull();
	}

	@Test
	public void shouldCalculateSliderVariance() {
		ScaleType scaleType = ScaleType.oneToMax;
		StepCountsBuilder stepCountsBuilder = StepCountsBuilder.builder(4);
		stepCountsBuilder.withCount(1, Long.valueOf(2));
		stepCountsBuilder.withCount(2, Long.valueOf(2));
		stepCountsBuilder.withCount(3, Long.valueOf(0));
		stepCountsBuilder.withCount(4, Long.valueOf(1));
		SumMean sumMean = sut.getSumMean(stepCountsBuilder.build(), scaleType);
		
		Double variance = sut.getVariance(stepCountsBuilder.build(), scaleType, sumMean.getMean());
		
		assertThat(variance).isEqualTo(1.5);
	}

	@Test
	public void shouldCalculateSliderVarianceOfTwoResponses() {
		ScaleType scaleType = ScaleType.oneToMax;
		StepCountsBuilder stepCountsBuilder = StepCountsBuilder.builder(4);
		stepCountsBuilder.withCount(1, Long.valueOf(1));
		stepCountsBuilder.withCount(2, Long.valueOf(1));
		stepCountsBuilder.withCount(3, Long.valueOf(0));
		stepCountsBuilder.withCount(4, Long.valueOf(0));
		SumMean sumMean = sut.getSumMean(stepCountsBuilder.build(), scaleType);
		
		Double variance = sut.getVariance(stepCountsBuilder.build(), scaleType, sumMean.getMean());
		
		assertThat(variance).isEqualTo(0.5);
	}
	@Test
	public void shouldCalculateSliderVarianceOfOneResponse() {
		ScaleType scaleType = ScaleType.oneToMax;
		StepCountsBuilder stepCountsBuilder = StepCountsBuilder.builder(4);
		stepCountsBuilder.withCount(1, Long.valueOf(1));
		stepCountsBuilder.withCount(2, Long.valueOf(0));
		stepCountsBuilder.withCount(3, Long.valueOf(0));
		stepCountsBuilder.withCount(4, Long.valueOf(0));
		SumMean sumMean = sut.getSumMean(stepCountsBuilder.build(), scaleType);
		
		Double variance = sut.getVariance(stepCountsBuilder.build(), scaleType, sumMean.getMean());
		
		assertThat(variance).isNull();
	}

	@Test
	public void shouldCalculateSliderVarianceScaled() {
		ScaleType scaleType = ScaleType.zeroBallanced;
		StepCountsBuilder stepCountsBuilder = StepCountsBuilder.builder(4);
		stepCountsBuilder.withCount(1, Long.valueOf(2));
		stepCountsBuilder.withCount(2, Long.valueOf(2));
		stepCountsBuilder.withCount(3, Long.valueOf(0));
		stepCountsBuilder.withCount(4, Long.valueOf(1));
		SumMean sumMean = sut.getSumMean(stepCountsBuilder.build(), scaleType);
		
		Double variance = sut.getVariance(stepCountsBuilder.build(), scaleType, sumMean.getMean());
		
		assertThat(variance).isEqualTo(1.5);
	}

	@Test
	public void shouldGetTotalStepCount() {
		List<Slider> sliders = new ArrayList<>();
		SlidersStepCountsImpl stepCountsImpl = new SlidersStepCountsImpl();
		Slider slider1 = new Slider();
		slider1.setId(random());
		sliders.add(slider1);
		StepCountsBuilder stepCountsBuilder1 = StepCountsBuilder.builder(3);
		stepCountsBuilder1.withCount(1, Long.valueOf(3));
		stepCountsBuilder1.withCount(2, Long.valueOf(3));
		stepCountsBuilder1.withCount(3, Long.valueOf(3));
		stepCountsImpl.put(slider1, stepCountsBuilder1.build());
		Slider slider2 = new Slider();
		slider2.setId(random());
		sliders.add(slider2);
		StepCountsBuilder stepCountsBuilder2 = StepCountsBuilder.builder(3);
		stepCountsBuilder2.withCount(1, Long.valueOf(3));
		stepCountsBuilder2.withCount(2, Long.valueOf(3));
		stepCountsBuilder2.withCount(3, Long.valueOf(3));
		stepCountsImpl.put(slider2, stepCountsBuilder2.build());
		Slider slider3 = new Slider();
		slider3.setId(random());
		slider3.setWeight(3);
		sliders.add(slider3);
		StepCountsBuilder stepCountsBuilder3 = StepCountsBuilder.builder(3);
		stepCountsBuilder3.withCount(1, Long.valueOf(1));
		stepCountsBuilder3.withCount(2, Long.valueOf(2));
		stepCountsBuilder3.withCount(3, Long.valueOf(3));
		stepCountsImpl.put(slider3, stepCountsBuilder3.build());
		Slider slider4 = new Slider();
		slider4.setId(random());
		slider4.setWeight(0);
		sliders.add(slider4);
		StepCountsBuilder stepCountsBuilder4 = StepCountsBuilder.builder(4);
		stepCountsBuilder4.withCount(1, Long.valueOf(1));
		stepCountsBuilder4.withCount(2, Long.valueOf(2));
		stepCountsBuilder4.withCount(3, Long.valueOf(4));
		stepCountsImpl.put(slider4, stepCountsBuilder4.build());
		Rubric rubric = new Rubric();
		rubric.setSliders(sliders);
		rubric.setSteps(3);
		
		SoftAssertions softly = new SoftAssertions();
		StepCounts totalStepCountsUnweighted = sut.getTotalStepCounts(rubric, stepCountsImpl, false).build();
		softly.assertThat(totalStepCountsUnweighted.getStepCount(1)).isEqualTo(7);
		softly.assertThat(totalStepCountsUnweighted.getStepCount(2)).isEqualTo(8);
		softly.assertThat(totalStepCountsUnweighted.getStepCount(3)).isEqualTo(9);
		StepCounts totalStepCountsWeighted = sut.getTotalStepCounts(rubric, stepCountsImpl, true).build();
		softly.assertThat(totalStepCountsWeighted.getStepCount(1)).isEqualTo(9);
		softly.assertThat(totalStepCountsWeighted.getStepCount(2)).isEqualTo(12);
		softly.assertThat(totalStepCountsWeighted.getStepCount(3)).isEqualTo(15);
		softly.assertAll();
	}

	@Test
	public void shouldCalculateRubricTotal() {
		List<Slider> sliders = new ArrayList<>();
		SlidersStepCountsImpl slidersStepCountsImpl = new SlidersStepCountsImpl();
		Slider slider1 = new Slider();
		slider1.setId(random());
		sliders.add(slider1);
		StepCountsBuilder stepCountsBuilder1 = StepCountsBuilder.builder(3);
		stepCountsBuilder1.withCount(1, Long.valueOf(1));
		stepCountsBuilder1.withCount(2, Long.valueOf(1));
		stepCountsBuilder1.withCount(3, Long.valueOf(1));
		stepCountsBuilder1.withCountNoResponses(Long.valueOf(1));
		slidersStepCountsImpl.put(slider1, stepCountsBuilder1.build());
		Slider slider2 = new Slider();
		slider2.setId(random());
		sliders.add(slider2);
		StepCountsBuilder stepCountsBuilder2 = StepCountsBuilder.builder(3);
		stepCountsBuilder2.withCount(1, Long.valueOf(2));
		stepCountsBuilder2.withCount(2, Long.valueOf(2));
		stepCountsBuilder2.withCount(3, Long.valueOf(2));
		stepCountsBuilder2.withCountNoResponses(Long.valueOf(2));
		slidersStepCountsImpl.put(slider2, stepCountsBuilder2.build());
		Slider slider3 = new Slider();
		slider3.setId(random());
		slider3.setWeight(3);
		sliders.add(slider3);
		StepCountsBuilder stepCountsBuilder3 = StepCountsBuilder.builder(3);
		stepCountsBuilder3.withCount(1, Long.valueOf(3));
		stepCountsBuilder3.withCount(2, Long.valueOf(4));
		stepCountsBuilder3.withCount(3, Long.valueOf(5));
		stepCountsBuilder3.withCountNoResponses(Long.valueOf(3));
		slidersStepCountsImpl.put(slider3, stepCountsBuilder3.build());
		Slider slider4 = new Slider();
		slider4.setId(random());
		slider4.setWeight(0);
		sliders.add(slider4);
		StepCountsBuilder stepCountsBuilder4 = StepCountsBuilder.builder(4);
		stepCountsBuilder4.withCount(1, Long.valueOf(4));
		stepCountsBuilder4.withCount(2, Long.valueOf(4));
		stepCountsBuilder4.withCount(4, Long.valueOf(5));
		stepCountsBuilder4.withCountNoResponses(Long.valueOf(4));
		slidersStepCountsImpl.put(slider4, stepCountsBuilder4.build());
		Rubric rubric = new Rubric();
		rubric.setSliders(sliders);
		rubric.setSteps(3);
		
		SliderStatistic totalStatistic = sut.calculateTotalStatistic(rubric, slidersStepCountsImpl);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(totalStatistic.getNumberOfNoResponses()).as("Number of no responses").isEqualTo(6);
		softly.assertThat(totalStatistic.getNumberOfResponses()).as("Number of responses").isEqualTo(21);
		softly.assertThat(totalStatistic.getSum()).as("Sum").isEqualTo(96);
		softly.assertThat(totalStatistic.getAvg()).as("Avg").isEqualTo(2.13, offset(0.01));
		softly.assertThat(totalStatistic.getMedian()).as("Median").isEqualTo(2);
		softly.assertThat(totalStatistic.getVariance()).as("Variance").isEqualTo(0.663, offset(0.001));
		softly.assertThat(totalStatistic.getStdDev()).as("StdDev").isEqualTo(0.814, offset(0.001));
		softly.assertAll();
	}

}
