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

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.RubricRating;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.quality.analysis.GroupedStatistic;
import org.olat.modules.quality.analysis.GroupedStatisticKeys;
import org.olat.modules.quality.analysis.GroupedStatistics;
import org.olat.modules.quality.analysis.MultiKey;
import org.olat.modules.quality.analysis.MultiTrendSeries;
import org.olat.modules.quality.analysis.RawGroupedStatistic;
import org.olat.modules.quality.analysis.TemporalGroupBy;
import org.olat.modules.quality.analysis.TemporalKey;
import org.olat.modules.quality.analysis.Trend;
import org.olat.modules.quality.analysis.Trend.DIRECTION;
import org.olat.modules.quality.analysis.model.GroupedStatisticImpl;
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

	private static final OLog log = Tracing.createLoggerFor(StatisticsCalculator.class);
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	GroupedStatistics<GroupedStatistic> getGroupedStatistics(GroupedStatistics<RawGroupedStatistic> rawStatistics, Collection<Rubric> rubrics) {
		Map<String, Rubric> identifierToRubric = getIdentifierToRubric(rubrics);
		GroupedStatistics<GroupedStatistic> statistics = new GroupedStatistics<>();
		for (RawGroupedStatistic rawStatistic : rawStatistics.getStatistics()) {
			Rubric rubric = identifierToRubric.get(rawStatistic.getIdentifier());
			if (rubric != null) {
				GroupedStatistic statistic = getGroupedStatistic(rawStatistic, rubric);
				statistics.putStatistic(statistic);
			}
		}
		return statistics;
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
		Double scaledAvg = getScaledAvg(rubric, rawAvg);
		RubricRating rating = evaluationFormManager.getRubricRating(rubric, scaledAvg);
		int steps = rubric.getSteps();
		GroupedStatistic statistic = new GroupedStatisticImpl(rawStatistic.getIdentifier(), rawStatistic.getMultiKey(),
				rawStatistic.getTemporalKey(), rawStatistic.getCount(), rawAvg, rawAvgMaxGood, scaledAvg, rating, steps);
		log.debug("Grouped statistic:        " + statistic.toString());
		return statistic;
	}

	private Double getScaledAvg(Rubric rubric, Double rawAvg) {
		Double scaledAvg = rawAvg;
		switch (rubric.getScaleType()) {
		case maxToOne: {
			scaledAvg = rubric.getSteps() + 1 - rawAvg;
			break;
		}
		case zeroBallanced: {
			double offset = (rubric.getSteps() - 1) / 2.0;
			scaledAvg = rawAvg - 1 - offset;
			break;
		}
		default:
			break;
		}
		return scaledAvg;
	}

	MultiTrendSeries<String> getTrendsByIdentifiers(GroupedStatistics<GroupedStatistic> statistics, TemporalGroupBy temporalGroupBy) {
		Set<TemporalKey> temporalKeys = new HashSet<>();
		for (GroupedStatisticKeys groupedStatistic : statistics.getStatistics()) {
			temporalKeys.add(groupedStatistic.getTemporalKey());
		}
		List<TemporalKey> sortedTemporalKeys = new ArrayList<>(temporalKeys);
		Collections.sort(sortedTemporalKeys);
		TemporalKey minKey = sortedTemporalKeys.get(0);
		TemporalKey maxKey = sortedTemporalKeys.get(sortedTemporalKeys.size() - 1);
		
		Set<String> identifiers = statistics.getIdentifiers();
		
		MultiTrendSeries<String> multiTrendSeries = new MultiTrendSeries<>(temporalGroupBy, minKey, maxKey);
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
		Set<TemporalKey> temporalKeys = new HashSet<>();
		for (GroupedStatisticKeys groupedStatistic : statistics.getStatistics()) {
			temporalKeys.add(groupedStatistic.getTemporalKey());
		}
		List<TemporalKey> sortedTemporalKeys = new ArrayList<>(temporalKeys);
		Collections.sort(sortedTemporalKeys);
		TemporalKey minKey = sortedTemporalKeys.get(0);
		TemporalKey maxKey = sortedTemporalKeys.get(sortedTemporalKeys.size() - 1);
		
		Set<MultiKey> multiKeys = statistics.getMultiKeys();
		
		MultiTrendSeries<MultiKey> multiTrendSeries = new MultiTrendSeries<>(temporalGroupBy, minKey, maxKey);
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

}
