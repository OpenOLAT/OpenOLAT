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
import static org.assertj.core.api.Assertions.offset;

import java.util.List;

import org.junit.Test;
import org.olat.modules.topicbroker.TBEnrollmentFunction;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.model.TBTransientSelection;

/**
 * 
 * Initial date: Jul 7, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class MaxPrioritiesCriterionTest {
	
	@Test
	public void shouldGetValue_constant() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10, TBEnrollmentFunction.constant, null, null);
		
		List<TBSelection> selections = List.of(
				createSelection(true, 1),
				createSelection(true, 1),
				createSelection(true, 10),
				createSelection(false, 1)
			);
		
		assertThat(sut.getValue(selections)).isEqualTo(1);
	}
	
	@Test
	public void shouldGetValue_constant_after() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10, TBEnrollmentFunction.constant, 3, TBEnrollmentFunction.constant);
		
		List<TBSelection> selections = List.of(
				createSelection(true, 1),
				createSelection(true, 1),
				createSelection(true, 10),
				createSelection(false, 1)
			);
		
		assertThat(sut.getValue(selections)).isEqualTo(1);
	}
	
	@Test
	public void shouldGetValue_linear_constant() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10, TBEnrollmentFunction.linear, 6, TBEnrollmentFunction.constant);
		
		List<TBSelection> selections = List.of(
				createSelection(true, 9)
			);
		
		assertThat(sut.getValue(selections)).isEqualTo(0.444, offset(0.001));
	}
	
	@Test
	public void shouldGetValue_linear() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10, TBEnrollmentFunction.linear, null, null);
		
		List<TBSelection> selections = List.of(
				createSelection(true, 1),
				createSelection(true, 1),
				createSelection(true, 1),
				createSelection(true, 1),
				createSelection(true, 1),
				createSelection(true, 1),
				createSelection(true, 1),
				createSelection(true, 10),
				createSelection(false, 1)
			);
		
		assertThat(sut.getValue(selections)).isEqualTo(0.875);
	}
	
	@Test
	public void shouldGetValue_linear_max() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10, TBEnrollmentFunction.linear, null, null);
		
		List<TBSelection> selections = List.of(
				createSelection(true, 1),
				createSelection(true, 1),
				createSelection(true, 1),
				createSelection(false, 1)
			);
		
		assertThat(sut.getValue(selections)).isEqualTo(1);
	}
	
	@Test
	public void shouldGetValue_linear_min() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10, TBEnrollmentFunction.linear, null, null);
		
		List<TBSelection> selections = List.of(
				createSelection(true, 10),
				createSelection(true, 10),
				createSelection(true, 10)
			);
		
		assertThat(sut.getValue(selections)).isEqualTo(0.0);
	}
	
	@Test
	public void shouldGetValue_linear_zero() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10, TBEnrollmentFunction.linear, null, null);
		
		assertThat(sut.getValue(List.of())).isEqualTo(0);
	}
	
	@Test
	public void shouldGetValue_linear_10() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10, TBEnrollmentFunction.linear, null, null);
		
		List<TBSelection> selections = List.of(
				createSelection(true, 10)
			);
		
		assertThat(sut.getValue(selections)).isEqualTo(0);
	}
	
	@Test
	public void shouldGetValue_linear_9() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10, TBEnrollmentFunction.linear, null, null);
		
		List<TBSelection> selections = List.of(
				createSelection(true, 9),
				createSelection(true, 9)
			);
		
		assertThat(sut.getValue(selections)).isEqualTo(0.111, offset(0.001));
	}
	
	@Test
	public void shouldGetValue_linear_3() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10, TBEnrollmentFunction.linear, null, null);
		
		List<TBSelection> selections = List.of(
				createSelection(true, 3)
			);
		
		assertThat(sut.getValue(selections)).isEqualTo(0.777, offset(0.001));
	}
	
	@Test
	public void shouldGetValue_linear_2() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10, TBEnrollmentFunction.linear, null, null);
		
		List<TBSelection> selections = List.of(
				createSelection(true, 2)
			);
		
		assertThat(sut.getValue(selections)).isEqualTo(0.888, offset(0.001));
	}
	
	@Test
	public void shouldGetValue_linear_1() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10, TBEnrollmentFunction.linear, null, null);
		
		List<TBSelection> selections = List.of(
				createSelection(true, 1)
			);
		
		assertThat(sut.getValue(selections)).isEqualTo(1);
	}
	
	@Test
	public void shouldGetValue_logarythmic_1() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10, TBEnrollmentFunction.logarithmic, null, null);
		
		List<TBSelection> selections = List.of(
				createSelection(true, 1)
			);
		
		assertThat(sut.getValue(selections)).isEqualTo(1);
	}
	
	@Test
	public void shouldGetValue_logarythmic_2() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10, TBEnrollmentFunction.logarithmic, null, null);
		
		List<TBSelection> selections = List.of(
				createSelection(true, 2)
			);
		
		assertThat(sut.getValue(selections)).isEqualTo(0.698, offset(0.001));
	}
	
	@Test
	public void shouldGetValue_logarythmic_3() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10, TBEnrollmentFunction.logarithmic, null, null);
		
		List<TBSelection> selections = List.of(
				createSelection(true, 3)
			);
		
		assertThat(sut.getValue(selections)).isEqualTo(0.522, offset(0.001));
	}
	
	@Test
	public void shouldGetValue_logarythmic_4() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10, TBEnrollmentFunction.logarithmic, null, null);
		
		List<TBSelection> selections = List.of(
				createSelection(true, 4)
			);
		
		assertThat(sut.getValue(selections)).isEqualTo(0.397, offset(0.001));
	}
	
	@Test
	public void shouldGetValue_logarythmic_5() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10, TBEnrollmentFunction.logarithmic, null, null);
		
		List<TBSelection> selections = List.of(
				createSelection(true, 5)
			);
		
		assertThat(sut.getValue(selections)).isEqualTo(0.301, offset(0.001));
	}
	
	@Test
	public void shouldGetValue_logarythmic_6() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10, TBEnrollmentFunction.logarithmic, null, null);
		
		List<TBSelection> selections = List.of(
				createSelection(true, 6)
			);
		
		assertThat(sut.getValue(selections)).isEqualTo(0.221, offset(0.001));
	}
	
	@Test
	public void shouldGetValue_logarythmic_7() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10, TBEnrollmentFunction.logarithmic, null, null);
		
		List<TBSelection> selections = List.of(
				createSelection(true, 7)
			);
		
		assertThat(sut.getValue(selections)).isEqualTo(0.154, offset(0.001));
	}
	
	@Test
	public void shouldGetValue_logarythmic_8() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10, TBEnrollmentFunction.logarithmic, null, null);
		
		List<TBSelection> selections = List.of(
				createSelection(true, 8)
			);
		
		assertThat(sut.getValue(selections)).isEqualTo(0.096, offset(0.001));
	}
	
	@Test
	public void shouldGetValue_logarythmic_9() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10, TBEnrollmentFunction.logarithmic, null, null);
		
		List<TBSelection> selections = List.of(
				createSelection(true, 9)
			);
		
		assertThat(sut.getValue(selections)).isEqualTo(0.045, offset(0.001));
	}
	
	@Test
	public void shouldGetValue_logarythmic_10() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10, TBEnrollmentFunction.logarithmic, null, null);
		
		List<TBSelection> selections = List.of(
				createSelection(true, 10)
			);
		
		assertThat(sut.getValue(selections)).isEqualTo(0);
	}

	private TBSelection createSelection(boolean enrolled, int sortOrder) {
		TBTransientSelection selection = new TBTransientSelection();
		selection.setEnrolled(enrolled);
		selection.setSortOrder(sortOrder);
		return selection;
	}

}
