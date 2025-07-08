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
 * Initial date: Jun 25, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class MaxEnrollmentsCriterionTest {
	
	@Test
	public void shouldGetValue() {
		MaxEnrollmentsCriterion sut = new MaxEnrollmentsCriterion(40);
		
		List<TBSelection> selections = List.of(
				createSelection(true),
				createSelection(true),
				createSelection(true),
				createSelection(true),
				createSelection(false),
				createSelection(true),
				createSelection(false)
				);
		assertThat(sut.getValue(selections)).isEqualTo(0.125);
	}

	@Test
	public void shouldGetSatisfaction_zero_topics() {
		MaxEnrollmentsCriterion sut = new MaxEnrollmentsCriterion(0);
		
		assertThat(sut.getValue(List.of())).isEqualTo(0);
	}
	
	private TBSelection createSelection(boolean enrolled) {
		TBTransientSelection selection = new TBTransientSelection();
		selection.setEnrolled(enrolled);
		return selection;
	}
}
