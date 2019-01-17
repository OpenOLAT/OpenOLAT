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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.ScaleType;
import org.olat.modules.quality.analysis.GroupedStatistic;
import org.olat.modules.quality.analysis.GroupedStatistics;
import org.olat.modules.quality.analysis.MultiKey;
import org.olat.modules.quality.analysis.MultiTrendSeries;
import org.olat.modules.quality.analysis.RawGroupedStatistic;
import org.olat.modules.quality.analysis.TemporalGroupBy;
import org.olat.modules.quality.analysis.TemporalKey;
import org.olat.modules.quality.analysis.Trend;
import org.olat.modules.quality.analysis.Trend.DIRECTION;
import org.olat.modules.quality.analysis.TrendSeries;
import org.olat.modules.quality.analysis.model.GroupedStatisticImpl;
import org.olat.modules.quality.analysis.model.RawGroupedStatisticImpl;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class StatisticsCalculatorTest extends OlatTestCase {
	
	@Autowired
	private StatisticsCalculator sut;
	
	@Test
	public void shouldScaleOneToMax() {
		RawGroupedStatistic statistic = new RawGroupedStatisticImpl(null, null, null, null, null, null, 9.0);
		Rubric rubric = new Rubric();
		rubric.setScaleType(ScaleType.oneToMax);
		rubric.setSteps(10);
		
		GroupedStatistic scaledStatistic = sut.getGroupedStatistic(statistic, rubric);
		
		assertThat(scaledStatistic.getAvg()).isEqualByComparingTo(9.0);
	}

	@Test
	public void shouldScaleMaxToOne() {
		RawGroupedStatistic statistic = new RawGroupedStatisticImpl(null, null, null, null, null, null, 9.0);
		Rubric rubric = new Rubric();
		rubric.setScaleType(ScaleType.maxToOne);
		rubric.setSteps(10);
		
		GroupedStatistic scaledStatistic = sut.getGroupedStatistic(statistic, rubric);
		
		assertThat(scaledStatistic.getAvg()).isEqualByComparingTo(2.0);
	}

	@Test
	public void shouldScaleZeroBalanced() {
		RawGroupedStatistic statistic = new RawGroupedStatisticImpl(null, null, null, null, null, null, 9.0);
		Rubric rubric = new Rubric();
		rubric.setScaleType(ScaleType.zeroBallanced);
		rubric.setSteps(10);
		
		GroupedStatistic scaledStatistic = sut.getGroupedStatistic(statistic, rubric);
		
		assertThat(scaledStatistic.getAvg()).isEqualByComparingTo(3.5);
	}
	
	@Test
	public void shouldCalculateTrendDirectionUp() {
		DIRECTION direction = sut.getTrendDirection(1.0, 2.0);
		
		assertThat(direction).isEqualTo(DIRECTION.UP);
	}
	
	@Test
	public void shouldCalculateTrendDirectionDown() {
		DIRECTION direction = sut.getTrendDirection(2.0, 1.0);
		
		assertThat(direction).isEqualTo(DIRECTION.DOWN);
	}
	
	@Test
	public void shouldCalculateTrendDirectionEqual() {
		DIRECTION direction = sut.getTrendDirection(1.0, 1.01);
		
		assertThat(direction).isEqualTo(DIRECTION.EQUAL);
	}
	
	@Test
	public void shouldCalculateTrends() {
		String identifier1 = "i1";
		TemporalKey tk2000 = TemporalKey.of(2000);
		TemporalKey tk2005 = TemporalKey.of(2005);
		GroupedStatistics<GroupedStatistic> statistics = new GroupedStatistics<>();
		GroupedStatistic gs0 = new GroupedStatisticImpl(identifier1, MultiKey.none(), tk2000, 1l, 1.0, null, null);
		statistics.putStatistic(gs0);
		GroupedStatistic gs1 = new GroupedStatisticImpl(identifier1, MultiKey.none(), TemporalKey.of(2001), 1l, 1.0, null, null);
		statistics.putStatistic(gs1);
		GroupedStatistic gs2 = new GroupedStatisticImpl(identifier1, MultiKey.none(), TemporalKey.of(2002), 1l, 2.0, null, null);
		statistics.putStatistic(gs2);
		GroupedStatistic gs3 = new GroupedStatisticImpl(identifier1, MultiKey.none(), TemporalKey.of(2003), 1l, 2.01, null, null);
		statistics.putStatistic(gs3);
		GroupedStatistic gs4 = new GroupedStatisticImpl(identifier1, MultiKey.none(), TemporalKey.of(2004), 1l, 1.0, null, null);
		statistics.putStatistic(gs4);
		GroupedStatistic gs5 = new GroupedStatisticImpl(identifier1, MultiKey.none(), tk2005, 1l, 1.0, null, null);
		statistics.putStatistic(gs5);
		
		MultiTrendSeries<String> multiTrendSeries = sut.getTrends(statistics, TemporalGroupBy.DATA_COLLECTION_DEADLINE_YEAR);
		TrendSeries series = multiTrendSeries.getSeries(identifier1);
		
		Trend trend0 = series.getTrend(0);
		assertThat(trend0.getDirection()).isEqualTo(DIRECTION.EQUAL);
		Trend trend1 = series.getTrend(1);
		assertThat(trend1.getDirection()).isEqualTo(DIRECTION.EQUAL);
		Trend trend2 = series.getTrend(2);
		assertThat(trend2.getDirection()).isEqualTo(DIRECTION.UP);
		Trend trend3 = series.getTrend(3);
		assertThat(trend3.getDirection()).isEqualTo(DIRECTION.EQUAL);
		Trend trend4 = series.getTrend(4);
		assertThat(trend4.getDirection()).isEqualTo(DIRECTION.DOWN);
		Trend trend5 = series.getTrend(5);
		assertThat(trend5.getDirection()).isEqualTo(DIRECTION.EQUAL);
	}
	
	@Test
	public void shouldClaculateTrendGaps() {
		String identifier1 = "i1";
		GroupedStatistics<GroupedStatistic> statistics = new GroupedStatistics<>();
		GroupedStatistic gs0 = new GroupedStatisticImpl(identifier1, MultiKey.none(), TemporalKey.of(2000), 1l, 1.0, null, null);
		statistics.putStatistic(gs0);
		GroupedStatistic gs3 = new GroupedStatisticImpl(identifier1, MultiKey.none(), TemporalKey.of(2003), 1l, 2.01, null, null);
		statistics.putStatistic(gs3);
		
		MultiTrendSeries<String> multiTrendSeries = sut.getTrends(statistics, TemporalGroupBy.DATA_COLLECTION_DEADLINE_YEAR);
		TrendSeries series = multiTrendSeries.getSeries(identifier1);
		
		Trend trend0 = series.getTrend(0);
		assertThat(trend0.getDirection()).isEqualTo(DIRECTION.EQUAL);
		Trend trend1 = series.getTrend(1);
		assertThat(trend1).isNull();
		Trend trend2 = series.getTrend(2);
		assertThat(trend2).isNull();
		Trend trend3 = series.getTrend(3);
		assertThat(trend3.getDirection()).isEqualTo(DIRECTION.UP);
	}

}
