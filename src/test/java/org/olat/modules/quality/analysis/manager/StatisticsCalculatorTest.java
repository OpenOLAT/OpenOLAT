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
import static org.assertj.core.api.Assertions.offset;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.olat.test.JunitTestHelper.random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.RubricRating;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.ScaleType;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.quality.analysis.GroupedStatistic;
import org.olat.modules.quality.analysis.GroupedStatistics;
import org.olat.modules.quality.analysis.HeatMapStatistic;
import org.olat.modules.quality.analysis.MultiKey;
import org.olat.modules.quality.analysis.MultiTrendSeries;
import org.olat.modules.quality.analysis.RawGroupedStatistic;
import org.olat.modules.quality.analysis.TemporalGroupBy;
import org.olat.modules.quality.analysis.TemporalKey;
import org.olat.modules.quality.analysis.Trend;
import org.olat.modules.quality.analysis.Trend.DIRECTION;
import org.olat.modules.quality.analysis.TrendSeries;
import org.olat.modules.quality.analysis.model.GroupedStatisticImpl;
import org.olat.modules.quality.analysis.model.HeatMapStatisticImpl;
import org.olat.modules.quality.analysis.model.RawGroupedStatisticImpl;

/**
 * 
 * Initial date: 14.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class StatisticsCalculatorTest {
	
	@Mock
	private EvaluationFormManager evaluationFormManagerMock;
	
	@InjectMocks
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
		DIRECTION direction = sut.getTrendDirection(1.0, 2.0, true);
		
		assertThat(direction).isEqualTo(DIRECTION.UP);
	}
	
	@Test
	public void shouldCalculateTrendDirectionDown() {
		DIRECTION direction = sut.getTrendDirection(2.0, 1.0, true);
		
		assertThat(direction).isEqualTo(DIRECTION.DOWN);
	}
	
	@Test
	public void shouldCalculateTrendDirectionEqual() {
		DIRECTION direction = sut.getTrendDirection(1.0, 1.01, true);
		
		assertThat(direction).isEqualTo(DIRECTION.EQUAL);
	}
	
	@Test
	public void shouldCalculateTrendDirectionDownWithRawMaxBad() {
		DIRECTION direction = sut.getTrendDirection(1.0, 2.0, false);
		
		assertThat(direction).isEqualTo(DIRECTION.DOWN);
	}
	
	@Test
	public void shouldCalculateTrendDirectionUpWithRawMaxBad() {
		DIRECTION direction = sut.getTrendDirection(2.0, 1.0, false);
		
		assertThat(direction).isEqualTo(DIRECTION.UP);
	}
	
	@Test
	public void shouldCalculateTrendDirectionEqualWithRawMaxBad() {
		DIRECTION direction = sut.getTrendDirection(1.0, 1.01, false);
		
		assertThat(direction).isEqualTo(DIRECTION.EQUAL);
	}
	
	@Test
	public void shouldCalculateDiffAbsoluteWithGoodEnd() {
		boolean rawMaxGood = true;
		double avgPrev = 1.0;
		double avgCurrent = 1.1;
		double expected = 0.1;
		GroupedStatistic prev = new GroupedStatisticImpl(null, MultiKey.none(), null, 1l, null, rawMaxGood, avgPrev, null, 5);
		GroupedStatistic current = new GroupedStatisticImpl(null, MultiKey.none(), null, 1l, null, rawMaxGood, avgCurrent, null, 5);

		Double diff = sut.getAvgDiffAbsolute(prev, current);
		
		assertThat(diff).isEqualTo(expected);
	}
	
	@Test
	public void shouldCalculateDiffAbsoluteWithBadEnd() {
		boolean rawMaxGood = false;
		double avgPrev = 1.0;
		double avgCurrent = 1.1;
		double expected = 0.1;
		GroupedStatistic prev = new GroupedStatisticImpl(null, MultiKey.none(), null, 1l, null, rawMaxGood, avgPrev, null, 5);
		GroupedStatistic current = new GroupedStatisticImpl(null, MultiKey.none(), null, 1l, null, rawMaxGood, avgCurrent, null, 5);

		Double diff = sut.getAvgDiffAbsolute(prev, current);
		
		assertThat(diff).isEqualTo(expected);
	}
	
	@Test
	public void shouldCalculateDiffRelativeWithGoodEnd() {
		int steps = 6;
		boolean rawMaxGood = true;
		double avgPrev = 1.0;
		double avgCurrent = 2.0;
		double expected = 0.2;
		GroupedStatistic prev = new GroupedStatisticImpl(null, MultiKey.none(), null, 1l, null, rawMaxGood, avgPrev, null, steps);
		GroupedStatistic current = new GroupedStatisticImpl(null, MultiKey.none(), null, 1l, null, rawMaxGood, avgCurrent, null, steps);

		Double diffAbs = sut.getAvgDiffAbsolute(prev, current);
		Double diff = sut.getAvgDiffRelativ(diffAbs, steps);
		
		assertThat(diff).isEqualTo(expected);
	}
	
	@Test
	public void shouldCalculateDiffRelativeWithBadEnd() {
		int steps = 6;
		boolean rawMaxGood = false;
		double avgPrev = 1.0;
		double avgCurrent = 2.0;
		double expected = 0.2;
		GroupedStatistic prev = new GroupedStatisticImpl(null, MultiKey.none(), null, 1l, null, rawMaxGood, avgPrev, null, steps);
		GroupedStatistic current = new GroupedStatisticImpl(null, MultiKey.none(), null, 1l, null, rawMaxGood, avgCurrent, null, steps);

		Double diffAbs = sut.getAvgDiffAbsolute(prev, current);
		Double diff = sut.getAvgDiffRelativ(diffAbs, steps);
		
		assertThat(diff).isEqualTo(expected);
	}
	
	@Test
	public void shouldCalculateTrendsByIdentifiers() {
		String identifier1 = "i1";
		TemporalKey tk2000 = TemporalKey.of(2000);
		TemporalKey tk2005 = TemporalKey.of(2005);
		GroupedStatistics<GroupedStatistic> statistics = new GroupedStatistics<>();
		GroupedStatistic gs0 = new GroupedStatisticImpl(identifier1, MultiKey.none(), tk2000, 1l, 1.0, true, null, null, 5);
		statistics.putStatistic(gs0);
		GroupedStatistic gs1 = new GroupedStatisticImpl(identifier1, MultiKey.none(), TemporalKey.of(2001), 1l, 1.0, true, null, null, 5);
		statistics.putStatistic(gs1);
		GroupedStatistic gs2 = new GroupedStatisticImpl(identifier1, MultiKey.none(), TemporalKey.of(2002), 1l, 2.0, true, null, null, 5);
		statistics.putStatistic(gs2);
		GroupedStatistic gs3 = new GroupedStatisticImpl(identifier1, MultiKey.none(), TemporalKey.of(2003), 1l, 2.01, true, null, null, 5);
		statistics.putStatistic(gs3);
		GroupedStatistic gs4 = new GroupedStatisticImpl(identifier1, MultiKey.none(), TemporalKey.of(2004), 1l, 1.0, true, null, null, 5);
		statistics.putStatistic(gs4);
		GroupedStatistic gs5 = new GroupedStatisticImpl(identifier1, MultiKey.none(), tk2005, 1l, 1.0, true, null, null, 5);
		statistics.putStatistic(gs5);
		
		MultiTrendSeries<String> multiTrendSeries = sut.getTrendsByIdentifiers(statistics, TemporalGroupBy.DATA_COLLECTION_DEADLINE_YEAR);
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
	public void shouldClaculateTrendByIdentifiersGaps() {
		String identifier1 = "i1";
		GroupedStatistics<GroupedStatistic> statistics = new GroupedStatistics<>();
		GroupedStatistic gs0 = new GroupedStatisticImpl(identifier1, MultiKey.none(), TemporalKey.of(2000), 1l, 1.0, true, null, null, 5);
		statistics.putStatistic(gs0);
		GroupedStatistic gs3 = new GroupedStatisticImpl(identifier1, MultiKey.none(), TemporalKey.of(2003), 1l, 2.01, true, null, null, 5);
		statistics.putStatistic(gs3);
		
		MultiTrendSeries<String> multiTrendSeries = sut.getTrendsByIdentifiers(statistics, TemporalGroupBy.DATA_COLLECTION_DEADLINE_YEAR);
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
	
	@Test
	public void shouldCalculateTrendsByMultipleKeys() {
		MultiKey multiKey = MultiKey.of("i1", "i1");
		TemporalKey tk2000 = TemporalKey.of(2000);
		TemporalKey tk2005 = TemporalKey.of(2005);
		GroupedStatistics<GroupedStatistic> statistics = new GroupedStatistics<>();
		GroupedStatistic gs0 = new GroupedStatisticImpl(null, multiKey, tk2000, 1l, 1.0, true, null, null, 5);
		statistics.putStatistic(gs0);
		GroupedStatistic gs1 = new GroupedStatisticImpl(null, multiKey, TemporalKey.of(2001), 1l, 1.0, true, null, null, 5);
		statistics.putStatistic(gs1);
		GroupedStatistic gs2 = new GroupedStatisticImpl(null, multiKey, TemporalKey.of(2002), 1l, 2.0, true, null, null, 5);
		statistics.putStatistic(gs2);
		GroupedStatistic gs3 = new GroupedStatisticImpl(null, multiKey, TemporalKey.of(2003), 1l, 2.01, true, null, null, 5);
		statistics.putStatistic(gs3);
		GroupedStatistic gs4 = new GroupedStatisticImpl(null, multiKey, TemporalKey.of(2004), 1l, 1.0, true, null, null, 5);
		statistics.putStatistic(gs4);
		GroupedStatistic gs5 = new GroupedStatisticImpl(null, multiKey, tk2005, 1l, 1.0, true, null, null, 5);
		statistics.putStatistic(gs5);
		
		MultiTrendSeries<MultiKey> multiTrendSeries = sut.getTrendsByMultiKey(statistics, TemporalGroupBy.DATA_COLLECTION_DEADLINE_YEAR);
		TrendSeries series = multiTrendSeries.getSeries(multiKey);
		
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
	public void shouldClaculateTrendByMultiKeyGaps() {
		MultiKey multiKey = MultiKey.of("i1", "i1");
		GroupedStatistics<GroupedStatistic> statistics = new GroupedStatistics<>();
		GroupedStatistic gs0 = new GroupedStatisticImpl(null, multiKey, TemporalKey.of(2000), 1l, 1.0, true, null, null, 5);
		statistics.putStatistic(gs0);
		GroupedStatistic gs3 = new GroupedStatisticImpl(null, multiKey, TemporalKey.of(2003), 1l, 2.01, true, null, null, 5);
		statistics.putStatistic(gs3);
		
		MultiTrendSeries<MultiKey> multiTrendSeries = sut.getTrendsByMultiKey(statistics, TemporalGroupBy.DATA_COLLECTION_DEADLINE_YEAR);
		TrendSeries series = multiTrendSeries.getSeries(multiKey);
		
		Trend trend0 = series.getTrend(0);
		assertThat(trend0.getDirection()).isEqualTo(DIRECTION.EQUAL);
		Trend trend1 = series.getTrend(1);
		assertThat(trend1).isNull();
		Trend trend2 = series.getTrend(2);
		assertThat(trend2).isNull();
		Trend trend3 = series.getTrend(3);
		assertThat(trend3.getDirection()).isEqualTo(DIRECTION.UP);
	}
	
	@Test
	public void shouldReduceIdentifier() {
		// Rubric 1
		List<Slider> sliders1 = new ArrayList<>();
		Slider slider11 = new Slider();
		slider11.setId(random());
		slider11.setWeight(1);
		sliders1.add(slider11);
		Slider slider12 = new Slider();
		slider12.setId(random());
		slider12.setWeight(2);
		sliders1.add(slider12);
		Rubric rubric1 = new Rubric();
		rubric1.setId(random());
		rubric1.setSliders(sliders1);
		// Rubric2
		List<Slider> sliders2 = new ArrayList<>();
		Slider slider21 = new Slider();
		slider21.setId(random());
		slider21.setWeight(3);
		sliders2.add(slider21);
		Slider slider22 = new Slider();
		slider22.setId(random());
		slider22.setWeight(0);
		sliders2.add(slider22);
		Rubric rubric2 = new Rubric();
		rubric2.setId(random());
		rubric2.setSliders(sliders2);
		Set<Rubric> rubrics = new HashSet<>();
		rubrics.add(rubric1);
		rubrics.add(rubric2);
		// Raw statistics
		MultiKey multiKey1 = MultiKey.of("1");
		MultiKey multiKey2 = MultiKey.of("2");
		TemporalKey temporalKey1 = TemporalKey.of(1);
		TemporalKey temporalKey2 = TemporalKey.of(2);
		List<RawGroupedStatistic> statisticsList = new ArrayList<>();
		statisticsList.add(new RawGroupedStatisticImpl(slider11.getId(), multiKey1, temporalKey1, 1l, 1.0));
		statisticsList.add(new RawGroupedStatisticImpl(slider12.getId(), multiKey1, temporalKey1, 1l, 1.0));
		statisticsList.add(new RawGroupedStatisticImpl(slider21.getId(), multiKey1, temporalKey1, 1l, 1.0));
		statisticsList.add(new RawGroupedStatisticImpl(slider22.getId(), multiKey1, temporalKey1, 1l, 1.0));
		statisticsList.add(new RawGroupedStatisticImpl(slider11.getId(), multiKey1, temporalKey2, 2l, 1.0));
		statisticsList.add(new RawGroupedStatisticImpl(slider12.getId(), multiKey1, temporalKey2, 2l, 2.0));
		statisticsList.add(new RawGroupedStatisticImpl(slider21.getId(), multiKey1, temporalKey2, 2l, 3.0));
		statisticsList.add(new RawGroupedStatisticImpl(slider22.getId(), multiKey1, temporalKey2, 2l, 3.0));
		statisticsList.add(new RawGroupedStatisticImpl(slider12.getId(), multiKey2, temporalKey1, 100l, 20.2));
		statisticsList.add(new RawGroupedStatisticImpl(slider11.getId(), multiKey2, temporalKey2, 3l, 3.0));
		statisticsList.add(new RawGroupedStatisticImpl(slider12.getId(), multiKey2, temporalKey2, 2l, 2.0));
		statisticsList.add(new RawGroupedStatisticImpl(slider21.getId(), multiKey2, temporalKey2, 1l, 1.0));
		statisticsList.add(new RawGroupedStatisticImpl(slider22.getId(), multiKey2, temporalKey2, 2l, 2.0));
		
		List<RawGroupedStatistic> reducedStatistics = sut.reduceIdentifier(statisticsList, rubrics);
		
		SoftAssertions softly = new SoftAssertions();
		assertReducedIdentifier(softly, reducedStatistics, multiKey1, temporalKey1, 3l, 1);
		assertReducedIdentifier(softly, reducedStatistics, multiKey1, temporalKey2, 6l, 2.33);
		assertReducedIdentifier(softly, reducedStatistics, multiKey2, temporalKey1, 100l, 20.2);
		assertReducedIdentifier(softly, reducedStatistics, multiKey2, temporalKey2, 6l, 2);
		softly.assertAll();
	}

	private void assertReducedIdentifier(SoftAssertions softly, List<RawGroupedStatistic> statistics,
			MultiKey multiKey, TemporalKey temporalKey, long expectedCount, double expectedAvg) {
		for (RawGroupedStatistic statistic : statistics) {
			if (multiKey.equals(statistic.getMultiKey()) && temporalKey.equals(statistic.getTemporalKey())) {
				softly.assertThat(statistic.getCount()).as("Count of %s, %s is wrong", multiKey, temporalKey).isEqualTo(expectedCount);
				softly.assertThat(statistic.getRawAvg()).as("Average of %s, %s is wrong", multiKey, temporalKey).isEqualTo(expectedAvg, offset(0.01));
				return;
			}
		}
		softly.fail("No statistic for %s, %s", multiKey, temporalKey);
	}
	
	@Test
	public void shouldCalculateRubricsTotal() {
		when(evaluationFormManagerMock.getRubricRating(any(), any())).thenReturn(RubricRating.NEUTRAL);
		
		List<Slider> sliders1 = new ArrayList<>();
		Slider slider11 = new Slider();
		String slider11Id = random();
		slider11.setId(slider11Id);
		sliders1.add(slider11);
		Slider slider12 = new Slider();
		String slider12Id = random();
		slider12.setId(slider12Id);
		sliders1.add(slider12);
		Rubric rubric1 = new Rubric();
		rubric1.setSliders(sliders1);
		Rubric rubric2 = new Rubric();
		List<Slider> sliders2 = new ArrayList<>();
		Slider slider21 = new Slider();
		String slider21Id = random();
		slider21.setId(slider21Id);
		slider21.setWeight(2);
		sliders2.add(slider21);
		rubric2.setSliders(sliders2);
		List<Rubric> rubrics = Arrays.asList(rubric1, rubric2);
		
		List<GroupedStatistic> statistics = new ArrayList<>();
		statistics.add(new GroupedStatisticImpl(slider11Id, null, null, 1l, null, true, 1.0, null, 0));
		statistics.add(new GroupedStatisticImpl(slider12Id, null, null, 1l, null, true, 2.0, null, 0));
		statistics.add(new GroupedStatisticImpl(slider21Id, null, null, 2l, null, true, 3.0, null, 0));
		
		HeatMapStatistic total = sut.calculateRubricsTotal(statistics , rubrics);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(total.getCount()).isEqualTo(4);
		softly.assertThat(total.getAvg()).isEqualTo(2.5, offset(0.001));
		softly.assertThat(total.getRating()).isEqualTo(RubricRating.NEUTRAL);
		softly.assertAll();
	}


	@Test
	public void shouldCalculateSliderTotal() {
		when(evaluationFormManagerMock.getRubricRating(any(), any())).thenReturn(RubricRating.NEUTRAL);
		
		Rubric rubric = new Rubric();
		List<HeatMapStatistic> statistic = new ArrayList<>();
		statistic.add(new HeatMapStatisticImpl(2l, 3.0, null));
		statistic.add(new HeatMapStatisticImpl(1l, 1.5, null));
		statistic.add(new HeatMapStatisticImpl(3l, 5.0, null));
		statistic.add(new HeatMapStatisticImpl(null, null, null));
		
		HeatMapStatistic total = sut.calculateSliderTotal(statistic, rubric);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(total.getCount()).isEqualTo(6);
		softly.assertThat(total.getAvg()).isEqualTo(3.75, offset(0.001));
		softly.assertThat(total.getRating()).isEqualTo(RubricRating.NEUTRAL);
		softly.assertAll();
	}

}
