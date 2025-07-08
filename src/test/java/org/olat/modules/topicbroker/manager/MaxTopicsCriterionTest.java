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
import org.olat.modules.topicbroker.model.TBTransientTopic;

/**
 * 
 * Initial date: Jun 25, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class MaxTopicsCriterionTest {

	@Test
	public void shouldGetSatisfaction() {
		MaxTopicsCriterion sut = new MaxTopicsCriterion(40);
		
		List<TBSelection> selections = List.of(
				createSelection(1, true),
				createSelection(1, true),
				createSelection(1, true),
				createSelection(1, true),
				createSelection(1, false),
				createSelection(2, true),
				createSelection(3, false)
				);
		assertThat(sut.getValue(selections)).isEqualTo(0.05);
	}

	@Test
	public void shouldGetSatisfaction_one_topic() {
		MaxTopicsCriterion sut = new MaxTopicsCriterion(100);
		
		List<TBSelection> selections = List.of(
				createSelection(1, true)
				);
		assertThat(sut.getValue(selections)).isEqualTo(0.01);
	}

	@Test
	public void shouldGetSatisfaction_zero_topics() {
		MaxTopicsCriterion sut = new MaxTopicsCriterion(0);
		
		assertThat(sut.getValue(List.of())).isEqualTo(0);
	}
	
	private TBSelection createSelection(long topicKey, boolean enrolled) {
		TBTransientTopic topic = new TBTransientTopic();
		topic.setKey(topicKey);
		
		TBTransientSelection selection = new TBTransientSelection();
		selection.setTopic(topic);
		selection.setEnrolled(enrolled);
		return selection;
	}
	
}
