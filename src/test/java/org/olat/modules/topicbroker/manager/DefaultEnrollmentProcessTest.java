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
import org.olat.admin.user.imp.TransientIdentity;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.model.TBTransientBroker;
import org.olat.modules.topicbroker.model.TBTransientParticipant;
import org.olat.modules.topicbroker.model.TBTransientSelection;
import org.olat.modules.topicbroker.model.TBTransientTopic;

/**
 * 
 * Initial date: 18 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class DefaultEnrollmentProcessTest {

	@Test
	public void shouldEvaluate_allParticipantsIn1Topic() {
		TBBroker broker = createBroker(3, 1);
		TBTransientParticipant participant1 = createParticipant(1, null);
		TBTransientParticipant participant2 = createParticipant(2, null);
		TBTransientParticipant participant3 = createParticipant(3, null);
		
		TBTransientTopic topic = createTopic(1, 2, 6);
		List<TBTopic> topics = List.of(topic);
		
		TBTransientSelection selection1 = createSelection(participant1, topic, 1, false);
		TBTransientSelection selection2 = createSelection(participant2, topic, 1, false);
		TBTransientSelection selection3 = createSelection(participant3, topic, 2, false);
		List<TBSelection> selections = List.of(selection1, selection2, selection3);
		
		DefaultEnrollmentProcess sut = new DefaultEnrollmentProcess(broker, topics, selections);
		List<TBSelection> previewSelections = sut.getPreviewSelections();
		
		assertThat(previewSelections).hasSize(3);
		previewSelections.forEach(previewSlection -> assertThat(previewSlection.isEnrolled()).isTrue());
	}

	@Test
	public void shouldEvaluate_ensureMinParticipantsPerTopic() {
		TBBroker broker = createBroker(3, 1);
		TBTransientParticipant participant1 = createParticipant(1, null);
		TBTransientParticipant participant2 = createParticipant(2, null);
		TBTransientParticipant participant3 = createParticipant(3, null);
		
		// Needs at least 4 participants
		TBTransientTopic topic = createTopic(1, 4, 6);
		List<TBTopic> topics = List.of(topic);
		
		TBTransientSelection selection1 = createSelection(participant1, topic, 1, false);
		TBTransientSelection selection2 = createSelection(participant2, topic, 1, false);
		TBTransientSelection selection3 = createSelection(participant3, topic, 2, false);
		List<TBSelection> selections = List.of(selection1, selection2, selection3);
		
		DefaultEnrollmentProcess sut = new DefaultEnrollmentProcess(broker, topics, selections);
		List<TBSelection> previewSelections = sut.getPreviewSelections();
		
		assertThat(previewSelections.stream().filter(TBSelection::isEnrolled).count()).isEqualTo(0);
	}

	@Test
	public void shouldEvaluate_ensureMaxParticipantsPerTopic() {
		TBBroker broker = createBroker(3, 1);
		TBTransientParticipant participant1 = createParticipant(1, null);
		TBTransientParticipant participant2 = createParticipant(2, null);
		TBTransientParticipant participant3 = createParticipant(3, null);
		
		// Limit to 2 participants
		TBTransientTopic topic = createTopic(1, 2, 2);
		List<TBTopic> topics = List.of(topic);
		
		TBTransientSelection selection1 = createSelection(participant1, topic, 1, false);
		TBTransientSelection selection2 = createSelection(participant2, topic, 1, false);
		TBTransientSelection selection3 = createSelection(participant3, topic, 2, false);
		List<TBSelection> selections = List.of(selection1, selection2, selection3);
		
		DefaultEnrollmentProcess sut = new DefaultEnrollmentProcess(broker, topics, selections);
		List<TBSelection> previewSelections = sut.getPreviewSelections();
		
		assertThat(previewSelections.stream().filter(TBSelection::isEnrolled).count()).isEqualTo(2);
	}

	@Test
	public void shouldEvaluate_ensureBoost() {
		TBBroker broker = createBroker(3, 1);
		TBTransientParticipant participant1 = createParticipant(1, 1);
		TBTransientParticipant participant2 = createParticipant(2, 2);
		TBTransientParticipant participant3 = createParticipant(3, 3);
		
		TBTransientTopic topic = createTopic(1, 0, 2);
		List<TBTopic> topics = List.of(topic);
		
		TBTransientSelection selection1 = createSelection(participant1, topic, 1, false);
		TBTransientSelection selection2 = createSelection(participant2, topic, 1, false);
		TBTransientSelection selection3 = createSelection(participant3, topic, 1, false);
		List<TBSelection> selections = List.of(selection1, selection2, selection3);
		
		DefaultEnrollmentProcess sut = new DefaultEnrollmentProcess(broker, topics, selections);
		List<TBSelection> previewSelections = sut.getPreviewSelections();
		
		List<Long> enrolledParticipantKeys = previewSelections.stream()
				.filter(TBSelection::isEnrolled)
				.map(selection -> selection.getParticipant().getKey())
				.toList();
		assertThat(enrolledParticipantKeys).containsExactlyInAnyOrder(participant2.getKey(), participant3.getKey());
	}
	
	@Test
	public void shouldEvaluate_ensureBoostIfFirstSelectionUnpopular() {
		TBBroker broker = createBroker(3, 1);
		TBTransientParticipant participant1 = createParticipant(1, 1);
		TBTransientParticipant participant2 = createParticipant(2, 2);
		TBTransientParticipant participant3 = createParticipant(3, 3);
		
		TBTransientTopic topic = createTopic(1, 0, 2);
		TBTransientTopic topicUnpopuar = createTopic(2, 2, 4);
		List<TBTopic> topics = List.of(topic, topicUnpopuar);
		
		TBTransientSelection selection1 = createSelection(participant1, topic, 1, false);
		TBTransientSelection selection2 = createSelection(participant2, topic, 1, false);
		TBTransientSelection selection31 = createSelection(participant3, topicUnpopuar, 1, false);
		TBTransientSelection selection32 = createSelection(participant3, topic, 2, false);
		List<TBSelection> selections = List.of(selection1, selection2, selection31, selection32);
		
		DefaultEnrollmentProcess sut = new DefaultEnrollmentProcess(broker, topics, selections);
		List<TBSelection> previewSelections = sut.getPreviewSelections();
		
		List<Long> enrolledParticipantKeys = previewSelections.stream()
				.filter(TBSelection::isEnrolled)
				.map(selection -> selection.getParticipant().getKey())
				.toList();
		assertThat(enrolledParticipantKeys).containsExactlyInAnyOrder(participant2.getKey(), participant3.getKey());
	}
	
	@Test
	public void shouldEvaluate_ignoreTooLowPriorities() {
		TBBroker broker = createBroker(3, 1);
		TBTransientParticipant participant1 = createParticipant(1, null);
		TBTransientParticipant participant2 = createParticipant(2, null);
		TBTransientParticipant participant3 = createParticipant(3, null);
		
		TBTransientTopic topic = createTopic(1, 2, 6);
		List<TBTopic> topics = List.of(topic);
		
		// Too low priority
		TBTransientSelection selection1 = createSelection(participant1, topic, 4, false);
		TBTransientSelection selection2 = createSelection(participant2, topic, 1, false);
		TBTransientSelection selection3 = createSelection(participant3, topic, 1, false);
		List<TBSelection> selections = List.of(selection1, selection2, selection3);
		
		DefaultEnrollmentProcess sut = new DefaultEnrollmentProcess(broker, topics, selections);
		List<TBSelection> previewSelections = sut.getPreviewSelections();
		
		List<Long> enrolledParticipantKeys = previewSelections.stream()
				.filter(TBSelection::isEnrolled)
				.map(selection -> selection.getParticipant().getKey())
				.toList();
		assertThat(enrolledParticipantKeys).containsExactlyInAnyOrder(participant2.getKey(), participant3.getKey());
	}
	
	@Test
	public void shouldEvaluate_enrollLowerPrioIfMinParticipantsOfTopicNotReached() {
		TBBroker broker = createBroker(3, 1);
		TBTransientParticipant participant1 = createParticipant(1, null);
		TBTransientParticipant participant2 = createParticipant(2, null);
		TBTransientParticipant participant3 = createParticipant(3, null);
		
		TBTransientTopic topic1 = createTopic(1, 2, 6);
		Long topic1Key = topic1.getKey();
		TBTransientTopic topic2 = createTopic(2, 2, 6);
		List<TBTopic> topics = List.of(topic1, topic2);
		
		// Too low priority
		TBTransientSelection selection1 = createSelection(participant1, topic1, 1, false);
		TBTransientSelection selection2 = createSelection(participant2, topic1, 1, false);
		// Has topic 1 with lower priority the topic 2
		TBTransientSelection selection31 = createSelection(participant3, topic1, 2, false);
		TBTransientSelection selection32 = createSelection(participant3, topic2, 1, false);
		List<TBSelection> selections = List.of(selection1, selection2, selection31, selection32);
		
		DefaultEnrollmentProcess sut = new DefaultEnrollmentProcess(broker, topics, selections);
		List<TBSelection> previewSelections = sut.getPreviewSelections();
		
		List<Long> enrolledParticipantKeys = previewSelections.stream()
				.filter(TBSelection::isEnrolled)
				.filter(selection -> selection.getTopic().getKey().equals(topic1Key))
				.map(selection -> selection.getParticipant().getKey())
				.toList();
		assertThat(enrolledParticipantKeys).containsExactlyInAnyOrder(participant1.getKey(), participant2.getKey(), participant3.getKey());
	}
	
	@Test
	public void shouldEvaluate_ensurePreEnrollmentHasHigherPriorityThanBoost() {
		TBBroker broker = createBroker(3, 1);
		TBTransientParticipant participant1 = createParticipant(1, 100);
		TBTransientParticipant participant2 = createParticipant(2, 100);
		TBTransientParticipant participant3 = createParticipant(3, null);
		
		TBTransientTopic topic = createTopic(1, 1, 1);
		List<TBTopic> topics = List.of(topic);
		
		TBTransientSelection selection1 = createSelection(participant1, topic, 1, false);
		TBTransientSelection selection2 = createSelection(participant2, topic, 1, false);
		TBTransientSelection selection3 = createSelection(participant3, topic, 2, true);
		List<TBSelection> selections = List.of(selection1, selection2, selection3);
		
		DefaultEnrollmentProcess sut = new DefaultEnrollmentProcess(broker, topics, selections);
		List<TBSelection> previewSelections = sut.getPreviewSelections();
		
		List<Long> enrolledParticipantKeys = previewSelections.stream()
				.filter(TBSelection::isEnrolled)
				.map(selection -> selection.getParticipant().getKey())
				.toList();
		assertThat(enrolledParticipantKeys).containsExactlyInAnyOrder(participant3.getKey());
	}
	
	@Test
	public void shouldEvaluate_ensurePreEnrollmentHasHigherPriorityThanPriority() {
		TBBroker broker = createBroker(3, 1);
		TBTransientParticipant participant1 = createParticipant(1, null);
		TBTransientParticipant participant2 = createParticipant(2, null);
		TBTransientParticipant participant3 = createParticipant(3, null);
		
		TBTransientTopic topic1 = createTopic(1, 1, 6);
		Long topic1Key = topic1.getKey();
		TBTransientTopic topic2 = createTopic(2, 1, 6);
		Long topic2Key = topic2.getKey();
		List<TBTopic> topics = List.of(topic1, topic2);
		
		// Too low priority
		TBTransientSelection selection1 = createSelection(participant1, topic1, 1, false);
		TBTransientSelection selection2 = createSelection(participant2, topic1, 1, false);
		TBTransientSelection selection31 = createSelection(participant3, topic1, 1, false);
		// Enrollment has only priority 2
		TBTransientSelection selection32 = createSelection(participant3, topic2, 2, true);
		List<TBSelection> selections = List.of(selection1, selection2, selection31, selection32);
		
		DefaultEnrollmentProcess sut = new DefaultEnrollmentProcess(broker, topics, selections);
		List<TBSelection> previewSelections = sut.getPreviewSelections();
		
		List<Long> enrolledParticipantKeys1 = previewSelections.stream()
				.filter(TBSelection::isEnrolled)
				.filter(selection -> selection.getTopic().getKey().equals(topic1Key))
				.map(selection -> selection.getParticipant().getKey())
				.toList();
		assertThat(enrolledParticipantKeys1).containsExactlyInAnyOrder(participant1.getKey(), participant2.getKey());
		List<Long> enrolledParticipantKeys2 = previewSelections.stream()
				.filter(TBSelection::isEnrolled)
				.filter(selection -> selection.getTopic().getKey().equals(topic2Key))
				.map(selection -> selection.getParticipant().getKey())
				.toList();
		assertThat(enrolledParticipantKeys2).containsExactlyInAnyOrder(participant3.getKey());
	}
	
	@Test
	public void shouldEvaluate_minParticipantsOfTwoTopicsNotReached() {
		TBBroker broker = createBroker(3, 1);
		TBTransientParticipant participant1 = createParticipant(1, null);
		TBTransientParticipant participant2 = createParticipant(2, null);
		TBTransientParticipant participant3 = createParticipant(3, null);
		
		TBTransientTopic topic1 = createTopic(1, 3, 6);
		Long topic1Key = topic1.getKey();
		TBTransientTopic topic2 = createTopic(2, 3, 6);
		List<TBTopic> topics = List.of(topic1, topic2);
		
		TBTransientSelection selection11 = createSelection(participant1, topic1, 1, false);
		TBTransientSelection selection12 = createSelection(participant1, topic2, 2, false);
		TBTransientSelection selection21 = createSelection(participant2, topic1, 1, false);
		TBTransientSelection selection22 = createSelection(participant2, topic2, 2, false);
		// Priorities inverted
		TBTransientSelection selection31 = createSelection(participant3, topic1, 2, false);
		TBTransientSelection selection32 = createSelection(participant3, topic2, 1, false);
		List<TBSelection> selections = List.of(selection11, selection12, selection21, selection22, selection31, selection32);
		
		DefaultEnrollmentProcess sut = new DefaultEnrollmentProcess(broker, topics, selections);
		List<TBSelection> previewSelections = sut.getPreviewSelections();
		
		List<Long> enrolledParticipantKeys = previewSelections.stream()
				.filter(TBSelection::isEnrolled)
				.filter(selection -> selection.getTopic().getKey().equals(topic1Key))
				.map(selection -> selection.getParticipant().getKey())
				.toList();
		assertThat(enrolledParticipantKeys).containsExactlyInAnyOrder(participant1.getKey(), participant2.getKey(), participant3.getKey());
	}


	private TBBroker createBroker(int maxSelections, int requiredEnrollments) {
		TBTransientBroker broker = new TBTransientBroker();
		broker.setMaxSelections(maxSelections);
		broker.setRequiredEnrollments(requiredEnrollments);
		return broker;
	}

	private TBTransientParticipant createParticipant(int key, Integer boost) {
		TBTransientParticipant participant = new TBTransientParticipant();
		participant.setKey(Long.valueOf(key));
		participant.setBoost(boost);
		participant.setIdentity(new TransientIdentity());
		return participant;
	}

	private TBTransientTopic createTopic(int key, int minParticipants, int maxParticipants) {
		TBTransientTopic topic1 = new TBTransientTopic();
		topic1.setKey(Long.valueOf(key));
		topic1.setMinParticipants(Integer.valueOf(minParticipants));
		topic1.setMaxParticipants(Integer.valueOf(maxParticipants));
		return topic1;
	}
	
	private TBTransientSelection createSelection(TBParticipant participant, TBTopic topic, int priority, boolean enrolled) {
		TBTransientSelection selection = new TBTransientSelection();
		selection.setParticipant(participant);
		selection.setTopic(topic);
		selection.setSortOrder(priority);
		selection.setEnrolled(enrolled);
		return selection;
	}

}
