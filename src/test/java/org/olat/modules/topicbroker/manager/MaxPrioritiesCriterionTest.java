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

import java.util.List;

import org.junit.Test;
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
	public void shouldGetValue_linear() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10);
		
		List<TBSelection> selections = List.of(
				createSelection(true, 1),
				createSelection(true, 4),
				createSelection(true, 10),
				createSelection(false, 1)
			);
		
		assertThat(sut.getValue(selections)).isEqualTo(0.6);
	}
	
	@Test
	public void shouldGetValue_linear_max() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10);
		
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
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10);
		
		List<TBSelection> selections = List.of(
				createSelection(true, 10),
				createSelection(true, 10),
				createSelection(true, 10)
			);
		
		assertThat(sut.getValue(selections)).isEqualTo(0.1);
	}
	
	@Test
	public void shouldGetValue_linear_zero() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10);
		
		assertThat(sut.getValue(List.of())).isEqualTo(0);
	}
	
	@Test
	public void shouldGetValue_linear_2() {
		MaxPrioritiesCriterion sut = new MaxPrioritiesCriterion(10);
		
		List<TBSelection> selections = List.of(
				createSelection(true, 2)
			);
		
		assertThat(sut.getValue(selections)).isEqualTo(0.9);
	}

	private TBSelection createSelection(boolean enrolled, int sortOrder) {
		TBTransientSelection selection = new TBTransientSelection();
		selection.setEnrolled(enrolled);
		selection.setSortOrder(sortOrder);
		return selection;
	}

}
