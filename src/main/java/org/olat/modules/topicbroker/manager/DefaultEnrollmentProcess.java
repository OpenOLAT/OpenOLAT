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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.modules.topicbroker.TBAuditLog;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBEnrollmentProcess;
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.modules.topicbroker.model.TBParticipantImpl;
import org.olat.modules.topicbroker.model.TBProcessInfos;
import org.olat.modules.topicbroker.model.TBProcessSelections;
import org.olat.modules.topicbroker.model.TBTopicImpl;
import org.olat.modules.topicbroker.model.TBTransientSelection;
import org.olat.modules.topicbroker.model.TBTransientTopic;
import org.olat.modules.topicbroker.ui.TBUIFactory;

/**
 * 
 * Initial date: 18 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class DefaultEnrollmentProcess implements TBEnrollmentProcess {

	private static final Logger log = Tracing.createLoggerFor(DefaultEnrollmentProcess.class);
	
	private final TBBroker broker;
	private final List<TBParticipant> participants;
	private final int maxSelections;
	private final BigDecimal baseBudget;
	private final List<TBTopic> shuffledTopics;
	private final Map<Long, Identity> participantKeyToIdentity;
	private final List<TBSelection> previewSelections;

	private final List<EvaluationActivity> activities = new ArrayList<>();
	private final Set<Long> topicKeysMinNotReached = new HashSet<>();
	private final List<ParticipantKeyTopicKey> withdrawSelections = new ArrayList<>();

	// To get the order of the enrolled selections
	private List<ParticipantKeyTopicKey> enrolledSelections;
	private List<MatchingTopic> topics;
	private Set<Long> participantKeysFullyEnrolled;
	private Map<Long, MatchingParticipant> participantKeyToMatchingParticipant;
	private Map<TopicKeyPriority, List<MatchingParticipant>> topicKeyPriorityToMatchingIdentities;

	public DefaultEnrollmentProcess(TBBroker broker, List<TBTopic> topics, List<TBSelection> selections) {
		this.broker = broker;
		this.participants = selections.stream().map(TBSelection::getParticipant).distinct().toList();
		addActivity(TBAuditLog.Action.processStart, TBProcessInfos.ofProcessStart(participants.size(), topics.size(), selections.size()));
		
		maxSelections = broker.getMaxSelections();
		
		baseBudget = BigDecimal.valueOf(maxSelections)
				.multiply(BigDecimal.valueOf(maxSelections+1))
				.divide(BigDecimal.valueOf(2));
		
		participantKeyToIdentity = participants.stream()
				.collect(Collectors.toMap(TBParticipant::getKey, TBParticipant::getIdentity));
		
		this.shuffledTopics = new ArrayList<>(topics);
		Collections.shuffle(this.shuffledTopics);
		addActivity(TBAuditLog.Action.topicSchuffle, TBProcessInfos.ofTopics(shuffledTopics.size()));
		
		previewSelections = new ArrayList<>();
		for (TBSelection selection : selections) {
			TBTransientSelection copy = TBTransientSelection.copyValuesOf(selection);
			copy.setTopic(TBTransientTopic.copyKeyAndTitleOf(selection.getTopic()));
			copy.setParticipant(selection.getParticipant());
			// Check (again) if it's not a surplus selection
			if (selection.isEnrolled() || selection.getSortOrder() <= maxSelections) {
				previewSelections.add(copy);
			}
		}
		
		resetAndEvaluate();
		applyEnrollments();
		
		addActivity(TBAuditLog.Action.processEnd, TBProcessInfos.ofProcessEnd(enrolledSelections.size(), withdrawSelections.size()));
	}

	private void resetAndEvaluate() {
		reset();
		addActivity(TBAuditLog.Action.evaluationStart, TBProcessInfos.ofTopics(topics.size()));
		
		for (int priority = 1; priority <= maxSelections; priority++) {
			evaluatePriority(priority);
		}

		addActivity(TBAuditLog.Action.evaluationEnd);
		
		EvaluationStats stats = new EvaluationStats();
		if (stats.isAnyTopicsMinNotReached()) {
			removeMostUnpopularTopic();
			resetAndEvaluate();
		}
	}

	private void evaluatePriority(int priority) {
		addActivity(TBAuditLog.Action.evaluationLevelStart, TBProcessInfos.ofPriority(priority));
		if (topics.isEmpty()) {
			addActivity(TBAuditLog.Action.evaluationLevelEndTopicsEmpty, TBProcessInfos.ofPriority(priority));
			return;
		}
		
		BigDecimal priorityCost = getPriorityCost(priority);
		for (int topicIndex = 0; topicIndex < topics.size(); topicIndex++) {
			MatchingTopic topic = topics.get(topicIndex);
			if (!topic.isMaxReached()) {
				TBProcessInfos infos = new TBProcessInfos();
				infos.setNumEnrollments(Integer.valueOf(topic.getNumEnrollments()));
				infos.setNumEnrollmentsLeft(Integer.valueOf(topic.getLeftEnrollments()));
				List<MatchingParticipant> participants = topicKeyPriorityToMatchingIdentities.getOrDefault(new TopicKeyPriority(topic.getKey(), priority), List.of());
				infos.setNumSelections(Integer.valueOf(participants.size()));
				participants = new ArrayList<>(participants);
				participants.removeIf(participant -> participantKeysFullyEnrolled.contains(participant.getKey()));
				infos.setNumParticipants(Integer.valueOf( participants.size()));
				infos.setPriority(Integer.valueOf(priority));
				addActivity(TBAuditLog.Action.evaluationTopicStart, null, topic.getKey(), infos, null);
				
				if (!participants.isEmpty()) {
					participants = sortAndShuffle(topic, participants);
					
					int leftEnrollments = topic.getLeftEnrollments();
					if (leftEnrollments > participants.size()) {
						leftEnrollments = participants.size();
					}
					for (int i = 0; i<leftEnrollments; i++) {
						MatchingParticipant participant = participants.get(i);
						enroll(participant,  topic, priority, priorityCost, TBAuditLog.Action.participantEnroll);
					}
				}
				
				infos = new TBProcessInfos();
				infos.setNumEnrollments(Integer.valueOf(topic.getNumEnrollments()));
				infos.setNumEnrollmentsLeft(Integer.valueOf(topic.getLeftEnrollments()));
				infos.setPriority(Integer.valueOf(priority));
				addActivity(TBAuditLog.Action.evaluationTopicEnd, null, topic.getKey(), null, infos);
			}
		}
		
		EvaluationStats stats = new EvaluationStats();
		TBProcessInfos infos = TBProcessInfos.ofStats(stats.numTopicsMaxNotReached, stats.numTopicsMinNotReached, stats.numParticipantsMaxNotReached);
		infos.setPriority(Integer.valueOf(priority));
		addActivity(TBAuditLog.Action.evaluationLevelEnd, infos);
	}

	private void removeMostUnpopularTopic() {
		for (MatchingTopic topic : topics) {
			if (topic.getNumEnrollments() == 0) {
				topicKeysMinNotReached.add(topic.getKey());
				addActivity(TBAuditLog.Action.topicExcludeByUnpopularity, null, topic.getKey(),  TBProcessInfos
						.ofUnpupularity(topic.getNumEnrollments(), topic.getMinParticipants(), topic.getLeftEnrollments()), null);
			}
		}
		
		List<MatchingTopic> topicsByNumEnrollments = topics.stream()
				.filter(topic -> topic.getNumEnrollments() > 0 && topic.getNumEnrollments() < topic.getMinParticipants())
				.sorted((t1,t2) -> Integer.compare(t1.getNumEnrollments(), t2.getNumEnrollments()))
				.toList();
		if (!topicsByNumEnrollments.isEmpty()) {
			// Remove not every time the same topic.
			List<MatchingTopic> topicsWithSmallestNumEnrollments = getTopicsWithSmallestNumEnrollments(topicsByNumEnrollments);
			Collections.shuffle(topicsWithSmallestNumEnrollments);
			
			MatchingTopic topic = topicsWithSmallestNumEnrollments.get(0);
			topicKeysMinNotReached.add(topic.getKey());
			addActivity(TBAuditLog.Action.topicExcludeByUnpopularity, null, topic.getKey(), TBProcessInfos
					.ofUnpupularity(topic.getNumEnrollments(), topic.getMinParticipants(), topic.getLeftEnrollments()), null);
		}
	}

	/**
	 * @param topicsByNumEnrollments notEmpty and ordered by number of enrollments.
	 */
	private List<MatchingTopic> getTopicsWithSmallestNumEnrollments(List<MatchingTopic> topicsByNumEnrollments) {
		List<MatchingTopic> topicsWithSmallestNumEnrollments = new ArrayList<>(1);
		int currentNumEnrollments = topicsByNumEnrollments.get(0).getNumEnrollments();
		for (MatchingTopic matchingTopic : topicsByNumEnrollments) {
			if (matchingTopic.getNumEnrollments() <= currentNumEnrollments) {
				topicsWithSmallestNumEnrollments.add(matchingTopic);
			} else {
				return topicsWithSmallestNumEnrollments;
			}
		}
		return topicsWithSmallestNumEnrollments;
	}

	private List<MatchingParticipant> sortAndShuffle(MatchingTopic topic, List<MatchingParticipant> participants) {
		TreeMap<BigDecimal, List<MatchingParticipant>> budgetToMatchingParticipants = new TreeMap<>(Collections.reverseOrder());
		
		for (MatchingParticipant participant : participants) {
			budgetToMatchingParticipants.computeIfAbsent(participant.getBudget(), key -> new ArrayList<>()).add(participant);
		}
		
		List<MatchingParticipant> shuffledParticipants = new ArrayList<>(participants.size());
		for (List<MatchingParticipant> participantsPerBudget : budgetToMatchingParticipants.values()) {
			Collections.shuffle(participantsPerBudget);
			participantsPerBudget.forEach(participant -> shuffledParticipants.add(participant));
		}
		
		addActivity(TBAuditLog.Action.participantsOrdered, null, topic.getKey(), null, TBProcessInfos.ofParticipants(shuffledParticipants.size()));
		return shuffledParticipants;
	}

	private void enroll(MatchingParticipant participant, MatchingTopic topic, int priority, BigDecimal cost, TBAuditLog.Action action) {
		if (TBAuditLog.Action.participantEnroll == action) {
			enrolledSelections.add(new ParticipantKeyTopicKey(participant.getKey(), topic.getKey()));
		}
		TBProcessInfos infos = new TBProcessInfos();
		
		infos.setBudgetBefore(Float.valueOf(participant.getBudget().floatValue()));
		infos.setCost(Float.valueOf(cost.floatValue()));
		participant.setNumEnrollments(participant.getNumEnrollments()+1);
		participant.setBudget(participant.getBudget().subtract(cost));
		topic.setNumEnrollments(topic.getNumEnrollments()+1);
		
		infos.setBudgetAfter(Float.valueOf(participant.getBudget().floatValue()));
		infos.setNumEnrollments(Integer.valueOf(participant.getNumEnrollments()));
		infos.setNumEnrollmentsRequired(Integer.valueOf(participant.getRequiredEnrollments()));
		infos.setPriority(Integer.valueOf(priority));
		addActivity(TBAuditLog.Action.participantEnroll, participant.getKey(), topic.getKey(), null, infos);

		if (participant.isMaxEnrollmentsReached()) {
			participantKeysFullyEnrolled.add(participant.getKey());
		}
	}

	private void reset() {
		participantKeysFullyEnrolled = new HashSet<>();
		enrolledSelections = new ArrayList<>();
		
		topics = shuffledTopics.stream()
				.filter(topic -> !topicKeysMinNotReached.contains(topic.getKey()))
				.map(MatchingTopic::new)
				.collect(Collectors.toList());
		Map<Long, MatchingTopic> topicKeyToTopic = topics.stream().collect(Collectors.toMap(MatchingTopic::getKey, Function.identity()));
		
		participantKeyToMatchingParticipant = new HashMap<>(participants.size());
		for (TBParticipant participant : participants) {
			int maxEnrollments = TBUIFactory.getRequiredEnrollments(broker, participant);
			MatchingParticipant matchingParticipant = new MatchingParticipant(participant.getKey(),
					maxEnrollments, participant.getBoost());
			matchingParticipant.setBudget(baseBudget.add(matchingParticipant.getBoost()));
			participantKeyToMatchingParticipant.put(participant.getKey(), matchingParticipant);
		}
		
		
		BigDecimal priorityCost = getPriorityCost(0);
		
		List<TBSelection> selectionsWithoutExcludedTopics = new ArrayList<>();
		Map<Long, List<TBSelection>> participantKeyToSelectionsWithoutExcludedTopics = new HashMap<>();
		for (TBSelection selection : previewSelections) {
			if (selection.isEnrolled()) {
				// Already enrolled by the coach before the evaluation started
				if (topicKeysMinNotReached.contains(selection.getTopic().getKey())) {
					ParticipantKeyTopicKey participantKeyTopicKey = new ParticipantKeyTopicKey(selection.getParticipant().getKey(), selection.getTopic().getKey());
					if (!withdrawSelections.contains(participantKeyTopicKey)) {
						withdrawSelections.add(participantKeyTopicKey);
						addActivity(TBAuditLog.Action.participantWithdraw, participantKeyTopicKey.participantKey(), participantKeyTopicKey.topicKey(), null, null);
					}
				} else {
					MatchingParticipant participant = participantKeyToMatchingParticipant.get(selection.getParticipant().getKey());
					enroll(participant, topicKeyToTopic.get(selection.getTopic().getKey()), 0, priorityCost, TBAuditLog.Action.participantPreEnrolled);
				}
			} else if (!topicKeysMinNotReached.contains(selection.getTopic().getKey())) {
				TBTransientSelection selectionCopy = TBTransientSelection.copyValuesOf(selection);
				selectionCopy.setTopic(TBTransientTopic.copyKeyAndTitleOf(selection.getTopic()));
				selectionCopy.setParticipant(selection.getParticipant());
				selectionsWithoutExcludedTopics.add(selectionCopy);
				participantKeyToSelectionsWithoutExcludedTopics
					.computeIfAbsent(selection.getParticipant().getKey(), key -> new ArrayList<>())
					.add(selectionCopy);
			}
		}
		
		// Update sortOrder if topic minimum participants not reached
		for (List<TBSelection> selectionOfParticipant : participantKeyToSelectionsWithoutExcludedTopics.values()) {
			TBProcessSelections before = new TBProcessSelections();
			TBProcessSelections after = new TBProcessSelections();
			boolean sortOrderChanged = false;
			
			selectionOfParticipant.sort((s1, s2) -> Integer.compare(s1.getSortOrder(), s2.getSortOrder()));
			for (int i = 0; i < selectionOfParticipant.size(); i++) {
				TBSelection selection = selectionOfParticipant.get(i);
				before.addSelection(selection.getTopic().getKey(), selection.getTopic().getTitle(), selection.getSortOrder());
				
				int newSortOrder = i + 1;
				if (selection.getSortOrder() != newSortOrder) {
					((TBTransientSelection)selection).setSortOrder(newSortOrder);
					sortOrderChanged = true;
				}
				after.addSelection(selection.getTopic().getKey(), selection.getTopic().getTitle(), selection.getSortOrder());
			}
			
			if (sortOrderChanged) {
				addActivity(TBAuditLog.Action.participantExcludeTopics, selectionOfParticipant.get(0).getParticipant().getKey(), null, before, after);
			}
		}
		
		topicKeyPriorityToMatchingIdentities = new HashMap<>();
		for (TBSelection selection : selectionsWithoutExcludedTopics) {
			MatchingParticipant matchingParticipant = participantKeyToMatchingParticipant.get(selection.getParticipant().getKey());
			TopicKeyPriority topicKeyPriority = new TopicKeyPriority(selection.getTopic().getKey(), selection.getSortOrder());
			topicKeyPriorityToMatchingIdentities
				.computeIfAbsent(topicKeyPriority, key -> new ArrayList<>())
				.add(matchingParticipant);
		}
	}

	private BigDecimal getPriorityCost(int costPriority) {
		return BigDecimal.valueOf(maxSelections + 1 - costPriority);
	}

	private void applyEnrollments() {
		// Make a set to avoid loops
		Set<ParticipantKeyTopicKey> enrollmentKeys = new HashSet<>(enrolledSelections);
		Set<ParticipantKeyTopicKey> withdrawKeys = new HashSet<>(withdrawSelections);
		for (TBSelection selection : previewSelections) {
			ParticipantKeyTopicKey participantKeyTopicKey = new ParticipantKeyTopicKey(selection.getParticipant().getKey(), selection.getTopic().getKey());
			if (enrollmentKeys.contains(participantKeyTopicKey)) {
				((TBTransientSelection)selection).setEnrolled(true);
			} else if (withdrawKeys.contains(participantKeyTopicKey)) {
				((TBTransientSelection)selection).setEnrolled(false);
			}
		}
	}

	@Override
	public List<TBSelection> getPreviewSelections() {
		return previewSelections;
	}

	@Override
	public void persist(Identity doer) {
		DB dbInstance = CoreSpringFactory.getImpl(DB.class);
		TopicBrokerService topicBrokerService = CoreSpringFactory.getImpl(TopicBrokerService.class);
		
		int counter = 0;
		for (EvaluationActivity activity : activities) {
			String before = activity.before() != null? TopicBrokerXStream.toXml(activity.before()): null;
			String after = activity.after() != null? TopicBrokerXStream.toXml(activity.after()): null;
			TBParticipant participant = activity.participantKey() != null
					? dbInstance.getCurrentEntityManager().getReference(TBParticipantImpl.class , activity.participantKey())
					: null;
			TBTopic topic  = activity.topicKey() != null
					? dbInstance.getCurrentEntityManager().getReference(TBTopicImpl.class , activity.topicKey())
					: null;
			topicBrokerService.log(activity.action(), before, after, doer, broker, participant, topic, null);
			counter = countAndCommit(dbInstance, counter);
		}
		
		for (ParticipantKeyTopicKey participantKeyTopicKey : withdrawSelections) {
			topicBrokerService.withdraw(doer,
					participantKeyToIdentity.get(participantKeyTopicKey.participantKey()),
					() -> participantKeyTopicKey.topicKey(),
					true);
			counter = countAndCommit(dbInstance, counter);
		}
		
		for (ParticipantKeyTopicKey participantKeyTopicKey : enrolledSelections) {
			topicBrokerService.enroll(doer,
					participantKeyToIdentity.get(participantKeyTopicKey.participantKey()),
					() -> participantKeyTopicKey.topicKey(),
					true);
			counter = countAndCommit(dbInstance, counter);
		}
		dbInstance.commitAndCloseSession();
	}
	
	private int countAndCommit(DB dbInstance, int counter) {
		if (counter % 25 == 0) {
			dbInstance.commitAndCloseSession();
		}
		return counter++;
	}
	
	private final static class MatchingParticipant {
		
		private final Long key;
		private final int requiredEnrollments;
		private final BigDecimal boost;
		private int numEnrollments = 0;
		private BigDecimal budget;
		
		public MatchingParticipant(Long key, int requiredEnrollments, Integer boost) {
			this.key = key;
			this.requiredEnrollments = requiredEnrollments;
			this.boost = boost != null? BigDecimal.valueOf(boost.intValue()): BigDecimal.ZERO;
		}
		
		public Long getKey() {
			return key;
		}

		public int getRequiredEnrollments() {
			return requiredEnrollments;
		}

		public boolean isMaxEnrollmentsReached() {
			return numEnrollments >= requiredEnrollments;
		}
		
		public BigDecimal getBoost() {
			return boost;
		}

		public int getNumEnrollments() {
			return numEnrollments;
		}

		public void setNumEnrollments(int numEnrollments) {
			this.numEnrollments = numEnrollments;
		}

		public BigDecimal getBudget() {
			return budget;
		}

		public void setBudget(BigDecimal budget) {
			this.budget = budget;
		}
		
	}
	
	private final static class MatchingTopic {
		
		private final Long key;
		private final int minParticipants;
		private final int  maxParticipants;
		private int numEnrollments;
		
		public MatchingTopic(TBTopic topic) {
			key = topic.getKey();
			minParticipants = topic.getMinParticipants() != null? topic.getMinParticipants().intValue(): 0;
			maxParticipants = topic.getMaxParticipants() != null? topic.getMaxParticipants().intValue(): 0;
		}
		
		public Long getKey() {
			return key;
		}
		
		public int getMinParticipants() {
			return minParticipants;
		}

		public int getNumEnrollments() {
			return numEnrollments;
		}
		
		public void setNumEnrollments(int numEnrollments) {
			this.numEnrollments = numEnrollments;
		}
		
		public int getLeftEnrollments() {
			return maxParticipants - numEnrollments;
		}
		
		public boolean isMinReached() {
			return numEnrollments >= minParticipants;
		}
		
		public boolean isMaxReached() {
			return numEnrollments >= maxParticipants;
		}
		
	}
	
	private class EvaluationStats {
		
		private final long numTopicsMaxNotReached;
		private final long numTopicsMinNotReached;
		private final long numParticipantsMaxNotReached;
		
		public EvaluationStats() {
			numTopicsMaxNotReached = topics.stream().filter(topic -> !topic.isMaxReached()).count();
			numTopicsMinNotReached = topics.stream().filter(topic -> !topic.isMinReached()).count();
			numParticipantsMaxNotReached = participantKeyToMatchingParticipant.values().stream().filter(participant -> !participant.isMaxEnrollmentsReached()).count();
		}
		
		public boolean isAnyTopicsMinNotReached() {
			return numTopicsMinNotReached > 0;
		}
		
	}

	private void addActivity(TBAuditLog.Action action) {
		addActivity(action, null, null, null, null);
	}
	
	private void addActivity(TBAuditLog.Action action, TBProcessInfos before) {
		addActivity(action, null, null, before, null);
	}
	
	private void addActivity(TBAuditLog.Action action, Long participantKey, Long topicKey, Object before, Object after) {
		EvaluationActivity activity = new EvaluationActivity(action, participantKey, topicKey, before, after);
		activities.add(activity);
		log.debug(activity);
	}
	
	private static record TopicKeyPriority(Long topicKey, int priority) {}
	private static record ParticipantKeyTopicKey(Long participantKey, Long topicKey) {}
	private static record EvaluationActivity(TBAuditLog.Action action, Long participantKey, Long topicKey, Object before, Object after) {}

}
