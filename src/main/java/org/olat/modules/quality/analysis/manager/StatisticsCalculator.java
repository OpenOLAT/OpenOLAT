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

import java.util.Collection;
import java.util.Map;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.quality.analysis.GroupedStatistic;
import org.olat.modules.quality.analysis.GroupedStatistics;
import org.olat.modules.quality.analysis.MultiKey;
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

	GroupedStatistics getScaledStatistics(GroupedStatistics statistics, Collection<Rubric> rubrics) {
		GroupedStatistics scaledStatistics = new GroupedStatistics();
		for (Rubric rubric : rubrics) {
			for (Slider slider : rubric.getSliders()) {
				String identifier = slider.getId();
				Map<MultiKey, GroupedStatistic> sliderStatistics = statistics.getStatistics(identifier);
				if (sliderStatistics != null) {
					for (GroupedStatistic statistic : sliderStatistics.values()) {
						GroupedStatistic scaledStatistic = getScaledStatistic(statistic, rubric);
						scaledStatistics.putStatistic(scaledStatistic);
					}
				}
			}
		}
		return scaledStatistics;
	}

	GroupedStatistic getScaledStatistic(GroupedStatistic statistic, Rubric rubric) {
		log.debug("Unscaled statistic: " + statistic.toString());
		Double scaledAvg = statistic.getAvg();
		switch (rubric.getScaleType()) {
		case maxToOne: {
			scaledAvg = rubric.getSteps() + 1 - statistic.getAvg();
			break;
		}
		case zeroBallanced: {
			double offset = (rubric.getSteps() - 1) / 2.0;
			scaledAvg = statistic.getAvg() - 1 - offset;
			break;
		}
		default:
			break;
		}
		GroupedStatistic scaledStatistic = new GroupedStatistic(statistic.getIdentifier(), statistic.getMultiKey(),
				statistic.getCount(), scaledAvg);
		log.debug("Scaled statistic:   " + scaledStatistic.toString());
		return scaledStatistic;
	}
}
