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

import java.util.ArrayList;
import java.util.List;

import org.olat.modules.forms.RubricRating;
import org.olat.modules.forms.RubricStatistic;
import org.olat.modules.forms.SliderStatistic;
import org.olat.modules.forms.SlidersStatistic;
import org.olat.modules.forms.SlidersStepCounts;
import org.olat.modules.forms.StepCounts;
import org.olat.modules.forms.model.RubricStatisticImpl;
import org.olat.modules.forms.model.SliderStatisticImpl;
import org.olat.modules.forms.model.SlidersStatisticImpl;
import org.olat.modules.forms.model.StepCountsBuilder;
import org.olat.modules.forms.model.jpa.CalculatedLong;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.ScaleType;
import org.olat.modules.forms.model.xml.Slider;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 25 Jun 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class RubricStatisticCalculator {
	
	SlidersStatistic calculateSlidersStatistic(Rubric rubric, SlidersStepCounts slidersStepCounts) {
		SlidersStatisticImpl slidersStatistic = new SlidersStatisticImpl();
		for (Slider slider: rubric.getSliders()) {
			StepCounts stepCounts = slidersStepCounts.getStepCounts(slider);
			SliderStatistic sliderStatistic;
			if (stepCounts != null) {
				Long count = getCount(stepCounts);
				SumMean sumMean = getSumMean(stepCounts, rubric.getScaleType());
				Double median = getMedian(stepCounts, rubric.getScaleType());
				Double variance = getVariance(stepCounts, rubric.getScaleType(), sumMean.getMean());
				Double stdDev = getStdDev(variance);
				RubricRating rating = RubricRatingEvaluator.rate(rubric, sumMean.getMean());
				sliderStatistic = new SliderStatisticImpl(count, sumMean.getSum(), median,
						sumMean.getMean(), variance, stdDev, stepCounts, rating);
			} else {
				sliderStatistic = new SliderStatisticImpl(null, null, null, null, null, null, null, null);
			}
			slidersStatistic.put(slider, sliderStatistic);
		}
		return slidersStatistic;
	}
	
	RubricStatistic calculateRubricStatistics(Rubric rubric, SlidersStatistic slidersStatistic) {
		SliderStatistic totalStatistic = calculateTotalStatistic(rubric, slidersStatistic);
		return new RubricStatisticImpl(rubric, slidersStatistic, totalStatistic);
	}

	SliderStatistic calculateTotalStatistic(Rubric rubric, SlidersStepCounts slidersStepCounts) {
		StepCountsBuilder stepCountsUnweightedBuilder = getTotalStepCounts(rubric, slidersStepCounts, false);
		Long numOfNoRespones = getCountNoResponses(rubric, slidersStepCounts);
		stepCountsUnweightedBuilder.withCountNoResponses(numOfNoRespones);
		StepCounts stepCountsUnweighted = stepCountsUnweightedBuilder.build();
		Long count = getCount(stepCountsUnweighted);
		
		StepCounts stepCountsWeighted = getTotalStepCounts(rubric, slidersStepCounts, true).build();
		SumMean sumMean = getSumMean(stepCountsWeighted, rubric.getScaleType());
		Double median = getMedian(stepCountsWeighted, rubric.getScaleType());
		Double variance = getVariance(stepCountsWeighted, rubric.getScaleType(), sumMean.getMean());
		Double stdDev = getStdDev(variance);
		RubricRating rating = RubricRatingEvaluator.rate(rubric, sumMean.getMean());
		SliderStatistic totalStatistic = new SliderStatisticImpl(count, sumMean.getSum(), median, sumMean.getMean(),
				variance, stdDev, stepCountsUnweighted, rating);
		return totalStatistic;
	}

	StepCountsBuilder getTotalStepCounts(Rubric rubric, SlidersStepCounts sliderStepCounts, boolean weighted) {
		StepCountsBuilder stepCountsBuilder = StepCountsBuilder.builder(rubric.getSteps());
		for (int step = 1; step <= rubric.getSteps(); step++) {
			long stepCount = 0;
			for (Slider slider: rubric.getSliders()) {
				Long sliderStepCount = sliderStepCounts.getStepCounts(slider).getStepCount(step);
				// Sliders with weight 0 are ignored even if it is not weighted
				long weightedCount = weighted || slider.getWeight().intValue() == 0
						? sliderStepCount.longValue() * slider.getWeight().intValue()
						: sliderStepCount.longValue();
				stepCount += weightedCount;
			}
			stepCountsBuilder.withCount(step, stepCount);
		}
		return stepCountsBuilder;
	}

	Long getCountNoResponses(Slider slider, List<CalculatedLong> countedNoResponses) {
		for (CalculatedLong calculatedLong: countedNoResponses) {
			if (calculatedLong.getIdentifier().equals(slider.getId())) {
				return calculatedLong.getValue();
			}
		}
		return 0l;
	}
	
	private Long getCountNoResponses(Rubric rubric, SlidersStepCounts slidersStepCounts) {
		long noResponses = 0;
		for (Slider slider: rubric.getSliders()) {
			Long sliderNoResponses = slidersStepCounts.getStepCounts(slider).getNumberOfNoResponses();
			if (sliderNoResponses != null && slider.getWeight().intValue() > 0) {
				noResponses += sliderNoResponses.longValue();
			}
		}
		return Long.valueOf(noResponses);
	}

	StepCountsBuilder getStepCounts(Slider slider, int numberOfSteps, List<CalculatedLong> countedResponses) {
		StepCountsBuilder stepCountsBuilder = StepCountsBuilder.builder(numberOfSteps);
		for (int step = 1; step <= numberOfSteps; step++) {
			Long value = getValue(countedResponses, slider.getId(), step);
			stepCountsBuilder.withCount(step, value);
		}
		return stepCountsBuilder;
	}
	
	private Long getValue(List<CalculatedLong> calculatedLongs, String identifier, int step) {
		for (CalculatedLong calculatedLong: calculatedLongs) {
			int calculatedStep = Double.valueOf(calculatedLong.getSubIdentifier()).intValue();
			if (calculatedLong.getIdentifier().equals(identifier) && calculatedStep == step) {
				return calculatedLong.getValue();
			}
		}
		return Long.valueOf(0);
	}
	
	Long getCount(StepCounts stepCounts) {
		long count = 0;
		for (int step = 1; step <= stepCounts.getNumberOfSteps(); step++) {
			long stepCount = stepCounts.getStepCount(step);
			count += stepCount;
		}
		return count > 0? Long.valueOf(count): null;
	}
	
	SumMean getSumMean(StepCounts stepCounts, ScaleType scaleType) {
		int count = 0;
		double sumValues = 0;
		for (int step = 1; step <= stepCounts.getNumberOfSteps(); step++) {
			long stepCount = stepCounts.getStepCount(step);
			count += stepCount;
			double stepValue = scaleType.getStepValue(stepCounts.getNumberOfSteps(), step);
			sumValues += stepCount * stepValue;
		}
		return count > 0
				? new SumMean(sumValues, sumValues / count)
				: new SumMean(null, null);
	}
	
	Double getMedian(StepCounts stepCounts, ScaleType scaleType) {
		List<Double> values = new ArrayList<>();
		for (int step = 1; step <= stepCounts.getNumberOfSteps(); step++) {
			long stepCount = stepCounts.getStepCount(step);
			double stepValue = scaleType.getStepValue(stepCounts.getNumberOfSteps(), step);
			for (int i = 0; i < stepCount; i++) {
				values.add(stepValue);
			}
		}
		if (values.isEmpty()) return null;
		
		double median;
		if (values.size() % 2 == 0)
			median = (values.get(values.size() / 2) + values.get(values.size() / 2 - 1)) / 2;
		else
			median = values.get(values.size() / 2);
		return median;
	}
	
	Double getVariance(StepCounts stepCounts, ScaleType scaleType, Double mean) {
		if (mean == null) return null;
		
		double varianceDividend = 0;
		int size = 0;
		for (int step = 1; step <= stepCounts.getNumberOfSteps(); step++) {
			double value = scaleType.getStepValue(stepCounts.getNumberOfSteps(), step);
			long stepCount = stepCounts.getStepCount(step);
			for (int i = 0; i < stepCount; i++) {
				varianceDividend += (value-mean)*(value-mean);
				size++;
			}
		}
		return size > 1? varianceDividend/(size - 1): null;
	}

	Double getStdDev(Double variance) {
		if (variance == null) return null;
		
		return Math.sqrt(variance);
	}
	
	static final class SumMean {
		
		private final Double sum;
		private final Double mean;
		
		private SumMean(Double sum, Double mean) {
			this.sum = sum;
			this.mean = mean;
		}

		Double getSum() {
			return sum;
		}

		Double getMean() {
			return mean;
		}
		
	}
}
