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
package org.olat.modules.forms.model.jpa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.modules.forms.RubricRating;
import org.olat.modules.forms.RubricStatistic;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SliderStatistic;
import org.olat.modules.forms.manager.EvaluationFormReportDAO;
import org.olat.modules.forms.manager.RubricRatingEvaluator;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Slider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RubricStatisticImpl implements RubricStatistic {
	
	private final Rubric rubric;
	private final List<CalculatedLong> countedResponses;
	private final List<CalculatedLong> countedNoResponses;
	private Map<Slider, SliderStatisticImpl> sliderToStatistic;
	private SliderStatistic totalStatistic;
	
	@Autowired
	private EvaluationFormReportDAO reportDao;
	
	public RubricStatisticImpl(Rubric rubric, SessionFilter filter) {
		this.rubric = rubric;
		CoreSpringFactory.autowireObject(this);
		
		List<String> responseIdentifiers = rubric.getSliders().stream().map(Slider::getId).collect(Collectors.toList());
		countedResponses = reportDao.getCountByIdentifiersAndNumerical(responseIdentifiers , filter);
		countedNoResponses = rubric.isNoResponseEnabled()
				? reportDao.getCountNoResponsesByIdentifiers(responseIdentifiers, filter)
				: Collections.emptyList();
		calculateStatistics();
	}
	
	@Override
	public Rubric getRubric() {
		return rubric;
	}
	
	@Override
	public SliderStatistic getSliderStatistic(Slider slider) {
		return sliderToStatistic.get(slider);
	}
	
	@Override
	public SliderStatistic getTotalStatistic() {
		return totalStatistic;
	}
	
	private void calculateStatistics() {
		calculateSliderStatistics();
		calculateTotalStatistics();
	}

	private void calculateSliderStatistics() {
		sliderToStatistic = new HashMap<>();
		for (Slider slider: rubric.getSliders()) {
			Long numOfNoRespones = getNumOfNoResponses(slider);
			List<Long> stepCounts = getStepCounts(slider);
			Long numOfResponses = getNumOfResponses(stepCounts);
			Double median = getMedian(stepCounts);
			SumAverage sumAverage = getSumAverage(stepCounts);
			Double variance = getVariance(stepCounts);
			Double stdDev = getStdDev(stepCounts);
			RubricRating rating = RubricRatingEvaluator.rate(rubric, sumAverage.getAverage());
			SliderStatisticImpl sliderStatistic = new SliderStatisticImpl(numOfNoRespones, numOfResponses,
					sumAverage.getSum(), median, sumAverage.getAverage(), variance, stdDev, stepCounts, rating);
			sliderToStatistic.put(slider, sliderStatistic);
		}
	}
	
	private Long getNumOfNoResponses(Slider slider) {
		for (CalculatedLong calculatedLong: countedNoResponses) {
			if (calculatedLong.getIdentifier().equals(slider.getId())) {
				return calculatedLong.getValue();
			}
		}
		return 0l;
	}

	private List<Long> getStepCounts(Slider slider) {
		List<Long> stepCounts = new ArrayList<>();
		for (int step = 1; step <= rubric.getSteps(); step++) {
			Long value = getValue(countedResponses, slider.getId(), step);
			stepCounts.add(value);
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
		return 0l;
	}
	
	private Long getNumOfResponses(List<Long> stepCounts) {
		return stepCounts.stream().filter(count -> count != null).mapToLong(Long::longValue).sum();
	}
	
	private Double getMedian(List<Long> stepCounts) {
		Long numOfResponses = getNumOfResponses(stepCounts);
		if (numOfResponses == null || numOfResponses == 0) return null;
		
		int valueLength = numOfResponses.intValue();
		double[] values = new double[valueLength];
		int counter = 0;
		
		for (int step = 1; step <= rubric.getSteps(); step++) {
			Long count = stepCounts.get(step - 1);
			if (count != null) {
				double stepValue = rubric.getScaleType().getStepValue(rubric.getSteps(), step, rubric.getWeight());
				for (int i = 0; i < count; i++) {
					values[counter++] = stepValue;
				}
			}
		}
		double median;
		if (values.length % 2 == 0)
			median = (values[values.length/2] + values[values.length/2 - 1]) / 2;
		else
			median = values[values.length/2];
		return median;
	}
	
	private SumAverage getSumAverage(List<Long> stepCounts) {
		double sumValues = 0;
		int sumResponses = 0;
		for (int step = 1; step <= rubric.getSteps(); step++) {
			Long count = stepCounts.get(step - 1);
			if (count != null) {
				double stepValue = rubric.getScaleType().getStepValue(rubric.getSteps(), step, rubric.getWeight());
				sumValues += count * stepValue;
				sumResponses += count;
			}
		}
		return sumResponses > 0
				? new SumAverage(sumValues, sumValues / sumResponses)
				: new SumAverage(null, null);
	}
	
	private Double getVariance(List<Long> stepCounts) {
		SumAverage sumAverage = getSumAverage(stepCounts);
		Double mean = sumAverage.getAverage();
		if (mean == null) return null;
		
		double temp = 0;
		int size = 0;
		for (int step = 1; step <= rubric.getSteps(); step++) {
			double value = rubric.getScaleType().getStepValue(rubric.getSteps(), step, rubric.getWeight());
			int count = stepCounts.get(step - 1).intValue();
			for (int i = 0; i < count; i++) {
				temp += (value-mean)*(value-mean);
				size++;
			}
		}
		return size > 2? temp/(size - 1): null;
	}

	private Double getStdDev(List<Long> stepCounts) {
		Double variance = getVariance(stepCounts);
		if (variance == null) return null;
		
		return Math.sqrt(getVariance(stepCounts));
	}
	
	private void calculateTotalStatistics() {
		Long numberOfNoResponses = countedNoResponses.stream().mapToLong(CalculatedLong::getValue).sum();
		List<Long> totalStepCounts = getTotalStepCounts();
		Long numOfResponses = getNumOfResponses(totalStepCounts);
		Double median = getMedian(totalStepCounts);
		SumAverage sumAverage = getSumAverage(totalStepCounts);
		Double variance = getVariance(totalStepCounts);
		Double stdDev = getStdDev(totalStepCounts);
		RubricRating rating = RubricRatingEvaluator.rate(rubric, sumAverage.getAverage());
		totalStatistic = new SliderStatisticImpl(numberOfNoResponses, numOfResponses, sumAverage.getSum(), median,
				sumAverage.getAverage(), variance, stdDev, totalStepCounts, rating);
	}

	private List<Long> getTotalStepCounts() {
		List<Long> totalStepCounts = new ArrayList<>();
		List<Slider> sliders = rubric.getSliders();
		for (int step = 0; step < rubric.getSteps(); step++) {
			long totalStepCount = 0;
			for (Slider slider: sliders) {
				Long sliderCount = sliderToStatistic.get(slider).getStepCounts().get(step);
				totalStepCount += sliderCount;
			}
			totalStepCounts.add(Long.valueOf(totalStepCount));
		}
		return totalStepCounts;
	}
	
	private static final class SumAverage {
		
		private final Double sum;
		private final Double average;
		
		private SumAverage(Double sum, Double average) {
			this.sum = sum;
			this.average = average;
		}

		private Double getSum() {
			return sum;
		}

		private Double getAverage() {
			return average;
		}
		
		
	}

}
