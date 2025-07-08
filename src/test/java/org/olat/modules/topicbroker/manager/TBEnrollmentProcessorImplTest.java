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

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.junit.Test;
import org.olat.admin.user.imp.TransientIdentity;
import org.olat.core.logging.Tracing;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBEnrollmentStrategy;
import org.olat.modules.topicbroker.TBEnrollmentStrategyContext;
import org.olat.modules.topicbroker.TBEnrollmentStrategyFactory;
import org.olat.modules.topicbroker.TBEnrollmentStrategyType;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.model.TBTransientBroker;
import org.olat.modules.topicbroker.model.TBTransientParticipant;
import org.olat.modules.topicbroker.model.TBTransientSelection;
import org.olat.modules.topicbroker.model.TBTransientTopic;

/**
 * 
 * This test is not intended as a regular test but only as an ad-hoc test during development.
 * 
 * Initial date: Jun 25, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBEnrollmentProcessorImplTest {

	@Test
	public void shouldBeFast() {
		// Good basic example
//		shouldBeFast(10, 10, 10, 1);
		
		shouldBeFast(10, 200, 10, 2);
//		shouldBeFast(100, 10000, 10, 2); // 22 millis / run on my mac
	}
	
	public void shouldBeFast(int numTopicsTotal, int numParticipantsTotal, int numSelectionsPerParticipant, int requiredEnrollments) {
		TBBroker broker = createBroker(requiredEnrollments, numSelectionsPerParticipant);
		List<TBTopic> topics = createTopics(broker, numTopicsTotal);
		List<TBSelection> selections = createSelections(topics, numParticipantsTotal);
		
		TBEnrollmentStrategyContext context = TBEnrollmentStrategyFactory.createContext(broker, topics, selections);
		TBEnrollmentStrategy strategy =TBEnrollmentStrategyFactory.createStrategy(
				TBEnrollmentStrategyFactory.createConfig(TBEnrollmentStrategyType.custom), context);
		
		List<TBEnrollmentStrategy> debugStrategies = List.of(
				TBEnrollmentStrategyFactory.createStrategy(TBEnrollmentStrategyFactory.createMaxEnrollmentsConfig(), context),
				TBEnrollmentStrategyFactory.createStrategy(TBEnrollmentStrategyFactory.createMaxPrioritiesConfig(), context),
				TBEnrollmentStrategyFactory.createStrategy(TBEnrollmentStrategyFactory.createMaxTopicsConfig(), context)
			);

		// Get duration from log
		Tracing.setLevelForLogger(Level.DEBUG, Tracing.createLoggerFor(TBEnrollmentProcessorImpl.class).getName());
		
		new TBEnrollmentProcessorImpl(5000, broker, topics, selections, strategy, debugStrategies).getBest();
		
		Tracing.setLevelForLogger(Level.INFO, Tracing.createLoggerFor(TBEnrollmentProcessorImpl.class).getName());
	}

	private TBBroker createBroker(int requiredEnrollments, int maxSelections) {
		TBTransientBroker broker = new TBTransientBroker();
		broker.setMaxSelections(maxSelections);
		broker.setRequiredEnrollments(requiredEnrollments);
		return broker;
	}

	private List<TBTopic> createTopics(TBBroker broker, int numTopicsTotal) {
		List<TBTopic> topics = new ArrayList<>(numTopicsTotal);
		for (int i = 0; i < numTopicsTotal; i++) {
			TBTransientTopic topic = new TBTransientTopic();
			topic.setKey(Long.valueOf(i));
			topic.setBroker(broker);
			topic.setMinParticipants(3);
			topic.setMaxParticipants(3);
			topics.add(topic);
		}
		return topics;
	}

	private List<TBSelection> createSelections(List<TBTopic> topics, int numParticipantsTotal) {
		Integer maxSelections = topics.get(0).getBroker().getMaxSelections();
		long counter = 0;
		SecureRandom random = new SecureRandom();
		
		List<TBSelection> selections = new ArrayList<>();
		for (int i = 0; i < numParticipantsTotal; i++) {
			Set<Long> selectedTopicKeys = new HashSet<>();
			TransientIdentity identity = new TransientIdentity();
			identity.setKey(Long.valueOf(i));
			TBTransientParticipant participant = new TBTransientParticipant();
			participant.setKey(Long.valueOf(i));
			participant.setIdentity(identity);
			
			for (int j = 1; j <= maxSelections; j++) {
				TBTransientSelection selection = new TBTransientSelection();
				selection.setKey(counter++);
				selection.setParticipant(participant);
				TBTopic topic = topics.get(random.nextInt(topics.size()));
				if (!selectedTopicKeys.contains(topic.getKey())) {
					selection.setTopic(topic);
					selection.setSortOrder(j);
					selections.add(selection);
					selectedTopicKeys.add(topic.getKey());
				}
			}
		}
		
		return selections;
	}

}
