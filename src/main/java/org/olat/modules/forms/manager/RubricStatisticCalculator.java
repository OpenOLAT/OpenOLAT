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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.modules.forms.RubricRating;
import org.olat.modules.forms.RubricStatistic;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SliderStatistic;
import org.olat.modules.forms.SlidersStatistic;
import org.olat.modules.forms.StepCounts;
import org.olat.modules.forms.model.RubricStatisticImpl;
import org.olat.modules.forms.model.SliderStatisticImpl;
import org.olat.modules.forms.model.SlidersStatisticImpl;
import org.olat.modules.forms.model.StepCountsImpl;
import org.olat.modules.forms.model.jpa.CalculatedLong;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.ScaleType;
import org.olat.modules.forms.model.xml.Slider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 25 Jun 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class RubricStatisticCalculator {
	
	@Autowired
	private EvaluationFormReportDAO reportDao;
	
	SlidersStatistic calculateSlidersStatistic(Rubric rubric, SessionFilter filter) {
		List<String> responseIdentifiers = rubric.getSliders().stream().map(Slider::getId).collect(Collectors.toList());
		List<CalculatedLong> countedResponses = reportDao.getCountByIdentifiersAndNumerical(responseIdentifiers , filter);
		List<CalculatedLong> countedNoResponses = rubric.isNoResponseEnabled()
				? reportDao.getCountNoResponsesByIdentifiers(responseIdentifiers, filter)
				: Collections.emptyList();
		
		SlidersStatisticImpl slidersStatistic = new SlidersStatisticImpl();
		for (Slider slider: rubric.getSliders()) {
			Long numOfNoRespones = getCountNoResponses(slider, countedNoResponses);
			StepCounts stepCounts = getStepCounts(slider, rubric.getSteps(), countedResponses);
			Long count = getCount(stepCounts);
			SumMean sumMean = getSumMean(stepCounts, rubric.getScaleType());
			Double median = getMedian(stepCounts, rubric.getScaleType());
			Double variance = getVariance(stepCounts, rubric.getScaleType(), sumMean.getMean());
			Double stdDev = getStdDev(variance);
			RubricRating rating = RubricRatingEvaluator.rate(rubric, sumMean.getMean());
			SliderStatistic sliderStatistic = new SliderStatisticImpl(numOfNoRespones,
					count, sumMean.getSum(), median, sumMean.getMean(),
					variance, stdDev, stepCounts, rating);
			slidersStatistic.put(slider, sliderStatistic);
		}
		return slidersStatistic;
	}
	
	RubricStatistic calculateRubricStatistics(Rubric rubric, SlidersStatistic slidersStatistic) {
		StepCounts stepCountsUnweighted = getTotalStepCounts(rubric, slidersStatistic, false);
		StepCounts stepCountsWeighted = getTotalStepCounts(rubric, slidersStatistic, true);
		
		Long numOfNoRespones = getCountNoResponses(rubric, slidersStatistic);
		Long count = getCount(stepCountsUnweighted);
		SumMean sumMean = getSumMean(stepCountsWeighted, rubric.getScaleType());
		Double median = getMedian(stepCountsWeighted, rubric.getScaleType());
		Double variance = getVariance(stepCountsWeighted, rubric.getScaleType(), sumMean.getMean());
		Double stdDev = getStdDev(variance);
		RubricRating rating = RubricRatingEvaluator.rate(rubric, sumMean.getMean());
		SliderStatistic totalStatistic = new SliderStatisticImpl(numOfNoRespones,
				count, sumMean.getSum(), median, sumMean.getMean(),
				variance, stdDev, stepCountsUnweighted, rating);
		return new RubricStatisticImpl(rubric, slidersStatistic, totalStatistic);
	}

	StepCounts getTotalStepCounts(Rubric rubric, SlidersStatistic sliderStatistics, boolean weighted) {
		StepCounts stepCounts = new StepCountsImpl(rubric.getSteps());
		for (int step = 1; step <= rubric.getSteps(); step++) {
			long stepCount = 0;
			for (Slider slider: rubric.getSliders()) {
				Long sliderStepCount = sliderStatistics.getSliderStatistic(slider).getStepCounts().getCount(step);
				long weightedCount = weighted
						? sliderStepCount.longValue() * slider.getWeight().intValue()
						: sliderStepCount.longValue();
				stepCount += weightedCount;
			}
			stepCounts.setCount(step, stepCount);
		}
		return stepCounts;
	}

	Long getCountNoResponses(Slider slider, List<CalculatedLong> countedNoResponses) {
		for (CalculatedLong calculatedLong: countedNoResponses) {
			if (calculatedLong.getIdentifier().equals(slider.getId())) {
				return calculatedLong.getValue();
			}
		}
		return 0l;
	}
	
	private Long getCountNoResponses(Rubric rubric, SlidersStatistic sliderStatistics) {
		long noResponses = 0;
		for (Slider slider: rubric.getSliders()) {
			Long sliderNoResponses = sliderStatistics.getSliderStatistic(slider).getNumberOfNoResponses();
			if (sliderNoResponses != null ) {
				noResponses += sliderNoResponses.longValue();
			}
		}
		return Long.valueOf(noResponses);
	}

	StepCounts getStepCounts(Slider slider, int numberOfSteps, List<CalculatedLong> countedResponses) {
		StepCounts stepCounts = new StepCountsImpl(numberOfSteps);
		for (int step = 1; step <= numberOfSteps; step++) {
			Long value = getValue(countedResponses, slider.getId(), step);
			stepCounts.setCount(step, value);
		}
		return stepCounts;
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
			long stepCount = stepCounts.getCount(step);
			count += stepCount;
		}
		return count > 0? Long.valueOf(count): null;
	}
	
	SumMean getSumMean(StepCounts stepCounts, ScaleType scaleType) {
		int count = 0;
		double sumValues = 0;
		for (int step = 1; step <= stepCounts.getNumberOfSteps(); step++) {
			long stepCount = stepCounts.getCount(step);
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
			long stepCount = stepCounts.getCount(step);
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
			long stepCount = stepCounts.getCount(step);
			for (int i = 0; i < stepCount; i++) {
				varianceDividend += (value-mean)*(value-mean);
				size++;
			}
		}
		return size > 2? varianceDividend/(size - 1): null;
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
