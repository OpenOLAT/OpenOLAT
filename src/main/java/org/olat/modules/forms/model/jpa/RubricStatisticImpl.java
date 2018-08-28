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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.forms.RubricStatistic;
import org.olat.modules.forms.SliderStatistic;
import org.olat.modules.forms.manager.EvaluationFormReportDAO;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Slider;

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
	
	private final EvaluationFormReportDAO reportDao;

	
	public RubricStatisticImpl(Rubric rubric, List<? extends EvaluationFormSessionRef> sessions) {
		this.rubric = rubric;
		reportDao = CoreSpringFactory.getImpl(EvaluationFormReportDAO.class);
		
		List<String> responseIdentifiers = rubric.getSliders().stream().map(Slider::getId).collect(Collectors.toList());
		countedResponses = reportDao.getCountByIdentifiersAndNumerical(responseIdentifiers , sessions);
		countedNoResponses = rubric.isNoResponseEnabled()
				? reportDao.getCountNoResponsesByIdentifiers(responseIdentifiers, sessions)
				: Collections.emptyList();
		calculateStatistics();
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
			Double average = getAverage(stepCounts);
			Double variance = getVariance(stepCounts);
			Double stdDev = getStdDev(stepCounts);
			SliderStatisticImpl sliderStatistic = new SliderStatisticImpl(numOfNoRespones, numOfResponses, median, average, variance, stdDev, stepCounts);
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
		String subidentifier = BigDecimal.valueOf((double)step).toPlainString();
		for (CalculatedLong calculatedLong: calculatedLongs) {
			if (calculatedLong.getIdentifier().equals(identifier) && calculatedLong.getSubIdentifier().equals(subidentifier)) {
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
				double stepValue = rubric.getScaleType().getStepValue(rubric.getSteps(), step);
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
	
	private Double getAverage(List<Long> stepCounts) {
		int sumValues = 0;
		int sumResponses = 0;
		for (int step = 1; step <= rubric.getSteps(); step++) {
			Long count = stepCounts.get(step - 1);
			if (count != null) {
				double stepValue = rubric.getScaleType().getStepValue(rubric.getSteps(), step);
				sumValues += count * stepValue;
				sumResponses += count;
			}
		}
		return sumResponses > 0? (double)sumValues / sumResponses: null;
	}
	
	private Double getVariance(List<Long> stepCounts) {
		Double mean = getAverage(stepCounts);
		if (mean == null) return null;
		
		List<Double> scaledValues = getScaledValues(stepCounts);
		if (scaledValues.size() < 2) return null;
		
		double temp = 0;
		for(double a: scaledValues)
			temp += (a-mean)*(a-mean);
		return temp/(scaledValues.size() - 1);
	}

	private Double getStdDev(List<Long> stepCounts) {
		Double variance = getVariance(stepCounts);
		if (variance == null) return null;
		
		return Math.sqrt(getVariance(stepCounts));
	}
	
	private List<Double> getScaledValues(List<Long> stepCounts) {
		List<Double> scaledValues = new ArrayList<>();
		for (int step = 1; step <= rubric.getSteps(); step++) {
			Long count = stepCounts.get(step - 1);
			if (count != null) {
				double stepValue = rubric.getScaleType().getStepValue(rubric.getSteps(), step);
				for (int i = 0; i < count; i++) {
					double scaledValue = stepValue * step;
					scaledValues.add(Double.valueOf(scaledValue));
				}
			}
		}
		return scaledValues;
	}

	private void calculateTotalStatistics() {
		Long numberOfNoResponses = countedNoResponses.stream().mapToLong(CalculatedLong::getValue).sum();
		List<Long> totalStepCounts = getTotalStepCounts();
		Long numOfResponses = getNumOfResponses(totalStepCounts);
		Double median = getMedian(totalStepCounts);
		Double average = getAverage(totalStepCounts);
		Double variance = getVariance(totalStepCounts);
		Double stdDev = getStdDev(totalStepCounts);
		totalStatistic = new SliderStatisticImpl(numberOfNoResponses, numOfResponses, median, average, variance, stdDev, totalStepCounts);
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

}
