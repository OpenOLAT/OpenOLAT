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
package org.olat.modules.quality.analysis.manager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.RubricRating;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.quality.analysis.GroupedStatistic;
import org.olat.modules.quality.analysis.GroupedStatisticKeys;
import org.olat.modules.quality.analysis.GroupedStatistics;
import org.olat.modules.quality.analysis.HeatMapStatistic;
import org.olat.modules.quality.analysis.MultiKey;
import org.olat.modules.quality.analysis.MultiTrendSeries;
import org.olat.modules.quality.analysis.MultiTrendSeries.TemporalMinMaxKeys;
import org.olat.modules.quality.analysis.RawGroupedStatistic;
import org.olat.modules.quality.analysis.TemporalGroupBy;
import org.olat.modules.quality.analysis.TemporalKey;
import org.olat.modules.quality.analysis.Trend;
import org.olat.modules.quality.analysis.Trend.DIRECTION;
import org.olat.modules.quality.analysis.model.GroupedStatisticImpl;
import org.olat.modules.quality.analysis.model.HeatMapStatisticImpl;
import org.olat.modules.quality.analysis.model.RawGroupedStatisticImpl;
import org.olat.modules.quality.analysis.model.TrendImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class StatisticsCalculator {

	private static final Logger log = Tracing.createLoggerFor(StatisticsCalculator.class);
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	GroupedStatistics<GroupedStatistic> getGroupedStatistics(GroupedStatistics<RawGroupedStatistic> rawStatistics, Collection<Rubric> rubrics) {
		Map<String, Rubric> identifierToRubric = getIdentifierToRubric(rubrics);
		GroupedStatistics<GroupedStatistic> statistics = new GroupedStatistics<>();
		for (RawGroupedStatistic rawStatistic : rawStatistics.getStatistics()) {
			Rubric rubric = identifierToRubric.get(rawStatistic.getIdentifier());
			if (rubric == null) {
				// fallback for trend (grouped by rubric / grouped by all)
				rubric = getFirstRubric(rubrics);
			}
			if (rubric != null) {
				GroupedStatistic statistic = getGroupedStatistic(rawStatistic, rubric);
				statistics.putStatistic(statistic);
			}
		}
		return statistics;
	}
	
	private Rubric getFirstRubric(Collection<Rubric> rubrics) {
		for (Rubric rubric : rubrics) {
			return rubric;
		}
		return null;
	}
	
	private Map<String, Rubric> getIdentifierToRubric(Collection<Rubric> rubrics) {
		Map<String, Rubric> identifierToRubric = new HashMap<>();
		for (Rubric rubric : rubrics) {
			for (Slider slider : rubric.getSliders()) {
				String identifier = slider.getId();
				identifierToRubric.put(identifier, rubric);
			}
		}
		return identifierToRubric;
	}

	GroupedStatistic getGroupedStatistic(RawGroupedStatistic rawStatistic, Rubric rubric) {
		log.debug("Raw grouped statistic: " + rawStatistic.toString());
		Double rawAvg = rawStatistic.getRawAvg();
		boolean rawAvgMaxGood = !rubric.isStartGoodRating();
		Double scaledAvg = rubric.getScaleType().getStepValue(rubric.getSteps(), rawAvg);
		RubricRating rating = evaluationFormManager.getRubricRating(rubric, scaledAvg);
		int steps = rubric.getSteps();
		GroupedStatistic statistic = new GroupedStatisticImpl(rawStatistic.getIdentifier(), rawStatistic.getMultiKey(),
				rawStatistic.getTemporalKey(), rawStatistic.getCount(), rawAvg, rawAvgMaxGood, scaledAvg, rating, steps);
		log.debug("Grouped statistic:        " + statistic.toString());
		return statistic;
	}

	public HeatMapStatistic calculateRubricsTotal(List<? extends GroupedStatistic> statistics,
			Collection<Rubric> rubrics) {
		Rubric firstRubric = null;
		HeatMapStatistic total;
		long count = 0;
		long sumCount = 0;
		double sumValues = 0;
		for (Rubric rubric: rubrics) {
			if (firstRubric == null) {
				firstRubric = rubric;
			}
			for (Slider slider: rubric.getSliders()) {
				GroupedStatistic statistic = getStatistic(statistics, slider);
				if (statistic != null) {
					Long statisticCount = statistic.getCount();
					if (statisticCount != null) {
						count += statisticCount.longValue();
						long weightedCount = statisticCount.longValue() * slider.getWeight().intValue();
						sumCount += weightedCount;
						sumValues += weightedCount * statistic.getAvg().doubleValue();
					}
				}
			}
		}
		if (count == 0) {
			total = new HeatMapStatisticImpl(null, null, null);
		} else {
			double avg = sumValues / sumCount;
			RubricRating rating = evaluationFormManager.getRubricRating(firstRubric, avg);
			total = new HeatMapStatisticImpl(count, avg, rating);
		}
		return total;
	}
	
	private GroupedStatistic getStatistic(List<? extends GroupedStatistic> statistics, Slider slider) {
		for (GroupedStatistic statistic : statistics) {
			if (statistic != null && slider.getId().equals(statistic.getIdentifier())) {
				return statistic;
			}
		}
		return null;
	}

	HeatMapStatistic calculateSliderTotal(List<? extends HeatMapStatistic> statistics, Rubric rubric) {
		HeatMapStatistic total;
		long count = 0;
		double sumValues = 0;
		for (HeatMapStatistic statistic : statistics) {
			if (statistic != null) {
				Long statisticCount = statistic.getCount();
				if (statisticCount != null) {
					count += statisticCount.longValue();
					sumValues += statisticCount.longValue() * statistic.getAvg().doubleValue();
				}
			}
		}
		
		if (count == 0) {
			total = new HeatMapStatisticImpl(null, null, null);
		} else {
			double avg = sumValues / count;
			RubricRating rating = evaluationFormManager.getRubricRating(rubric, avg);
			total = new HeatMapStatisticImpl(count, avg, rating);
		}
		return total;
	}
	
	List<RawGroupedStatistic> reduceIdentifier(List<RawGroupedStatistic> statisticsList, Set<Rubric> rubrics) {
		Map<String, Integer> sliderToWeight = rubrics.stream()
				.map(Rubric::getSliders)
				.flatMap(s -> s.stream())
				.collect(Collectors.toMap(Slider::getId, Slider::getWeight));
		List<MultiKey> multiKeys = statisticsList.stream()
				.map(RawGroupedStatistic::getMultiKey)
				.distinct()
				.collect(Collectors.toList());
		List<TemporalKey> temporalKeys = statisticsList.stream()
				.map(RawGroupedStatistic::getTemporalKey)
				.distinct()
				.collect(Collectors.toList());
		
		List<RawGroupedStatistic> statistics = new ArrayList<>();
		for (MultiKey multiKey: multiKeys) {
			for (TemporalKey temporalKey: temporalKeys) {
				List<RawGroupedStatistic> keysStatistics = filterByKey(statisticsList, multiKey, temporalKey);
				CountAvg countAvg = getCountAvg(keysStatistics, sliderToWeight);
				RawGroupedStatisticImpl reducedStatistic = new RawGroupedStatisticImpl(null, multiKey, temporalKey,
						countAvg.getCount(), countAvg.getAvg());
				statistics.add(reducedStatistic);
			}
		}
		return statistics;
	}

	private List<RawGroupedStatistic> filterByKey(List<RawGroupedStatistic> statisticsList, MultiKey multiKey,
			TemporalKey temporalKey) {
		return statisticsList.stream()
				.filter(s -> multiKey.equals(s.getMultiKey()) && temporalKey.equals(s.getTemporalKey()))
				.collect(Collectors.toList());
	}

	private CountAvg getCountAvg(List<RawGroupedStatistic> keysStatistics, Map<String, Integer> sliderToWeight) {
		long count = 0;
		int sumCount = 0;
		double sumValues = 0;
		for (RawGroupedStatistic statistic : keysStatistics) {
			long statisticCount = statistic.getCount()!= null? statistic.getCount().longValue(): 0;
			Integer weight = sliderToWeight.get(statistic.getIdentifier());
			count += weight != 0? statisticCount: 0;
			sumCount += statisticCount * weight;
			double statisticAvg = statistic.getRawAvg()!= null? statistic.getRawAvg().doubleValue(): 0;
			sumValues += statisticAvg * statisticCount * weight;
		}
		return count > 0
				? new CountAvg(count, sumValues / sumCount)
				: new CountAvg(null, null);
	}


	MultiTrendSeries<String> getTrendsByIdentifiers(GroupedStatistics<GroupedStatistic> statistics, TemporalGroupBy temporalGroupBy) {
		TemporalMinMaxKeys minMaxKeys = getTemporalMinMax(statistics);
		Set<String> identifiers = statistics.getIdentifiers();
		
		MultiTrendSeries<String> multiTrendSeries = new MultiTrendSeries<>(temporalGroupBy, minMaxKeys);
		for (String identifier: identifiers) {
			GroupedStatistic lastStatistic = null;
			for (TemporalKey temporalKey: multiTrendSeries.getTemporalKeys()) {
				GroupedStatistic currentStatistic = statistics.getStatistic(identifier, temporalKey);
				if (currentStatistic != null) {
					DIRECTION direction = getTrendDirection(lastStatistic, currentStatistic, currentStatistic.isRawAvgMaxGood());
					Double avgDiffAbsolute = getAvgDiffAbsolute(lastStatistic, currentStatistic);
					Double avgDiffRelative = getAvgDiffRelativ(avgDiffAbsolute, currentStatistic.getSteps());
					Trend trend = new TrendImpl(currentStatistic, direction, avgDiffAbsolute, avgDiffRelative);
					multiTrendSeries.put(identifier, temporalKey, trend);
					lastStatistic = currentStatistic;
				}
			}
		}
		return multiTrendSeries;
	}
	
	MultiTrendSeries<MultiKey> getTrendsByMultiKey(GroupedStatistics<GroupedStatistic> statistics, TemporalGroupBy temporalGroupBy) {
		TemporalMinMaxKeys minMaxKeys = getTemporalMinMax(statistics);
		Set<MultiKey> multiKeys = statistics.getMultiKeys();
		
		MultiTrendSeries<MultiKey> multiTrendSeries = new MultiTrendSeries<>(temporalGroupBy, minMaxKeys);
		for (MultiKey multiKey: multiKeys) {
			GroupedStatistic lastStatistic = null;
			for (TemporalKey temporalKey: multiTrendSeries.getTemporalKeys()) {
				GroupedStatistic currentStatistic = statistics.getStatistic(multiKey, temporalKey);
				if (currentStatistic != null) {
					DIRECTION direction = getTrendDirection(lastStatistic, currentStatistic, currentStatistic.isRawAvgMaxGood());
					Double avgDiffAbsolute = getAvgDiffAbsolute(lastStatistic, currentStatistic);
					Double avgDiffRelative = getAvgDiffRelativ(avgDiffAbsolute, currentStatistic.getSteps());
					Trend trend = new TrendImpl(currentStatistic, direction, avgDiffAbsolute, avgDiffRelative);
					multiTrendSeries.put(multiKey, temporalKey, trend);
					lastStatistic = currentStatistic;
				}
			}
		}
		return multiTrendSeries;
	}
	
	private TemporalMinMaxKeys getTemporalMinMax(GroupedStatistics<GroupedStatistic> statistics) {
		if (statistics.getStatistics().isEmpty()) return TemporalMinMaxKeys.nones();
		
		Set<TemporalKey> temporalKeys = new HashSet<>();
		for (GroupedStatisticKeys groupedStatistic : statistics.getStatistics()) {
			temporalKeys.add(groupedStatistic.getTemporalKey());
		}
		List<TemporalKey> sortedTemporalKeys = new ArrayList<>(temporalKeys);
		Collections.sort(sortedTemporalKeys);
		TemporalKey minKey = sortedTemporalKeys.get(0);
		TemporalKey maxKey = sortedTemporalKeys.get(sortedTemporalKeys.size() - 1);
		
		return TemporalMinMaxKeys.of(minKey, maxKey);
	}
	
	Double getAvgDiffAbsolute(GroupedStatistic prev, GroupedStatistic current) {
		if (prev == null || prev.getAvg() == null || current == null || current.getAvg() == null) return null;
		
		BigDecimal diff = BigDecimal.valueOf(current.getAvg()).subtract(BigDecimal.valueOf(prev.getAvg()));
		return Double.valueOf(diff.doubleValue());
	}
	
	Double getAvgDiffRelativ(Double diffAbsulute, int steps) {
		if (diffAbsulute == null) return null;
		
		double all = (double)steps - 1;
		double diff = diffAbsulute.doubleValue() / all;
		return Double.valueOf(diff);
	}

	DIRECTION getTrendDirection(GroupedStatistic prev, GroupedStatistic current, boolean rawAvgMaxGood) {
		// First in a series
		if (prev == null) return DIRECTION.EQUAL;
		
		Double prevAvg = prev.getRawAvg();
		Double currentAvg = current.getRawAvg();
		return getTrendDirection(prevAvg, currentAvg, rawAvgMaxGood);
	}

	DIRECTION getTrendDirection(Double prevAvg, Double currentAvg, boolean rawAvgMaxGood) {
		if (prevAvg == null || currentAvg == null) return null;
		
		double diffRange = 0.05;
		double diff = currentAvg.doubleValue() - prevAvg.doubleValue();
		DIRECTION direction = DIRECTION.EQUAL;
		if (diff > diffRange) {
			direction = rawAvgMaxGood? DIRECTION.UP: DIRECTION.DOWN;
		} else if (diff < -diffRange) {
			direction = rawAvgMaxGood? DIRECTION.DOWN: DIRECTION.UP;
		}
		return direction;
	}
	
	private static class CountAvg {
		
		private final Long count;
		private final Double avg;
		
		private CountAvg(Long count, Double avg) {
			this.count = count;
			this.avg = avg;
		}

		public Long getCount() {
			return count;
		}

		public Double getAvg() {
			return avg;
		}

	}

}
