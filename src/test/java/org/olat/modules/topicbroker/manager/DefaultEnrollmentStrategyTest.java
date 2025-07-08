/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.topicbroker.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.junit.Test;
import org.olat.modules.topicbroker.TBEnrollmentStrategyCriterion;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.model.TBEnrollmentStrategyConfigImpl;

/**
 * 
 * Initial date: Jun 25, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class DefaultEnrollmentStrategyTest {
	
	@Test
	public void shouldReplaceCriterion() {
		DefaultEnrollmentStrategy evaluator = createStrategy();
		evaluator.addCriterion(new ValueCriterion("type", 0.6), 1);
		assertThat(evaluator.getValue(List.of())).isEqualTo(0.6);
		
		evaluator.addCriterion(new ValueCriterion("type", 0.2), 1);
		assertThat(evaluator.getValue(List.of())).isEqualTo(0.2);
		
		evaluator.addCriterion(new ValueCriterion("type2", 0.6), 1);
		assertThat(evaluator.getValue(List.of())).isEqualTo(0.4);
	}

	@Test
	public void shouldCalculateOverallValue_no_criterion() {
		DefaultEnrollmentStrategy evaluator = createStrategy();
		assertThat(evaluator.getValue(List.of())).isEqualTo(0);
	}
	
	@Test
	public void shouldCalculateOverallValue_one_criterion() {
		DefaultEnrollmentStrategy evaluator = createStrategy();
		evaluator.addCriterion(new ValueCriterion(random(), 0.6), 1);
		
		assertThat(evaluator.getValue(List.of())).isEqualTo(0.6);
	}
	
	@Test
	public void shouldCalculateOverallValue_three_criterion() {
		DefaultEnrollmentStrategy evaluator = createStrategy();
		evaluator.addCriterion(new ValueCriterion(random(), 0.6), 1);
		evaluator.addCriterion(new ValueCriterion(random(), 0.8), 1);
		evaluator.addCriterion(new ValueCriterion(random(), 0.1), 1);
		
		assertThat(evaluator.getValue(List.of())).isEqualTo(0.5);
	}
	
	@Test
	public void shouldCalculateOverallValue_weighted_1() {
		DefaultEnrollmentStrategy evaluator = createStrategy();
		evaluator.addCriterion(new ValueCriterion(random(), 1), 0);
		evaluator.addCriterion(new ValueCriterion(random(), 0), 0);
		evaluator.addCriterion(new ValueCriterion(random(), 0.5), 25);
		
		assertThat(evaluator.getValue(List.of())).isEqualTo(0.5);
	}
	
	@Test
	public void shouldCalculateOverallValue_weighted_2() {
		DefaultEnrollmentStrategy evaluator = createStrategy();
		evaluator.addCriterion(new ValueCriterion(random(), 1), 0);
		evaluator.addCriterion(new ValueCriterion(random(), 0), 100);
		evaluator.addCriterion(new ValueCriterion(random(), 0.5), 25);
		
		assertThat(evaluator.getValue(List.of())).isEqualTo(0.1);
	}
	
	@Test
	public void shouldCalculateOverallValue_weighted_3() {
		DefaultEnrollmentStrategy evaluator = createStrategy();
		evaluator.addCriterion(new ValueCriterion(random(), 1), 1);
		evaluator.addCriterion(new ValueCriterion(random(), 0), 10);
		evaluator.addCriterion(new ValueCriterion(random(), 0.5), 3);
		evaluator.addCriterion(new ValueCriterion(random(), 0.1), 6);
		evaluator.addCriterion(new ValueCriterion(random(), 0.9), 0);
		
		assertThat(evaluator.getValue(List.of())).isEqualTo(0.155);
	}
	
	@Test
	public void shouldCalculateOverallValue_weight_zero() {
		DefaultEnrollmentStrategy evaluator = createStrategy();
		evaluator.addCriterion(new ValueCriterion(random(), 0.6), 0);
		
		assertThat(evaluator.getValue(List.of())).isEqualTo(0);
	}

	private DefaultEnrollmentStrategy createStrategy() {
		return new DefaultEnrollmentStrategy(new TBEnrollmentStrategyConfigImpl());
	}
	
	private final static class ValueCriterion implements TBEnrollmentStrategyCriterion {
		
		private final String type;
		private final double value;

		public ValueCriterion(String type, double value) {
			this.type = type;
			this.value = value;
		}

		@Override
		public String getType() {
			return type;
		}

		@Override
		public double getValue(List<TBSelection> selections) {
			return value;
		}
		
	}

}
