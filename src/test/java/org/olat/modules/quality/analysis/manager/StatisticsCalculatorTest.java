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
		GroupedStatistic statistic = new GroupedStatistic(null, null, null, null, null, 9.0);
		Rubric rubric = new Rubric();
		rubric.setScaleType(ScaleType.oneToMax);
		rubric.setSteps(10);
		
		GroupedStatistic scaledStatistic = sut.getScaledStatistic(statistic, rubric);
		
		assertThat(scaledStatistic.getAvg()).isEqualByComparingTo(9.0);
	}

	@Test
	public void shouldScaleMaxToOne() {
		GroupedStatistic statistic = new GroupedStatistic(null, null, null, null, null, 9.0);
		Rubric rubric = new Rubric();
		rubric.setScaleType(ScaleType.maxToOne);
		rubric.setSteps(10);
		
		GroupedStatistic scaledStatistic = sut.getScaledStatistic(statistic, rubric);
		
		assertThat(scaledStatistic.getAvg()).isEqualByComparingTo(2.0);
	}

	@Test
	public void shouldScaleZeroBalanced() {
		GroupedStatistic statistic = new GroupedStatistic(null, null, null, null, null, 9.0);
		Rubric rubric = new Rubric();
		rubric.setScaleType(ScaleType.zeroBallanced);
		rubric.setSteps(10);
		
		GroupedStatistic scaledStatistic = sut.getScaledStatistic(statistic, rubric);
		
		assertThat(scaledStatistic.getAvg()).isEqualByComparingTo(3.5);
	}

}
