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

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBSelectionSearchParams;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TBTopicDate;
import org.olat.modules.topicbroker.TBTopicDateSearchParams;
import org.olat.modules.topicbroker.TBTopicDateValues;
import org.olat.modules.topicbroker.TBTopicExecutionType;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 20 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBCalendarSyncherTest extends OlatTestCase {
	
	private static final int TIMEOUT = 10000;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TopicBrokerService topicBrokerService;
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryManager repositoryManager;
	
	@Test
	public void shouldCreateNoEvent_anytime() {
		TBBroker broker = createBroker();
		Identity participant = createParticipant(broker);
		TBTopic topic = createPeriodTopic(broker, null, null);
		
		topicBrokerService.select(participant, participant, topic, null);
		
		assertThat(getEvents(participant)).isEmpty();
	}
	
	@Test
	public void shouldCreateRangeEvent() {
		TBBroker broker = createBroker();
		Identity participant = createParticipant(broker);
		TBTopic topic = createPeriodTopic(broker, new Date(), DateUtils.addDays(new Date(), 2));
		
		topicBrokerService.select(participant, participant, topic, null);
		
		List<KalendarEvent> events = getEvents(participant);
		assertThat(events).hasSize(1);
		KalendarEvent event = events.get(0);
		assertThat(event.getSubject()).isEqualTo("BLOCKER: " + topic.getTitle());
		assertThat(event.isAllDayEvent()).isTrue();
		assertThat(event.isManaged()).isTrue();
		assertThat(event.getExternalId()).isEqualTo("tb-" + broker.getKey() + "-" + topic.getKey() + "-range");
		assertThat(event.getDescription()).contains(topic.getIdentifier());
		assertThat(event.getKalendarEventLinks()).hasSize(1);
		assertThat(event.getKalendarEventLinks().get(0).getURI()).contains(broker.getSubIdent());
	}
	
	@Test
	public void shouldCreateOneDayEvent() {
		TBBroker broker = createBroker();
		Identity participant = createParticipant(broker);
		Date date = new Date();
		TBTopic topic = createPeriodTopic(broker, date, date);
		
		topicBrokerService.select(participant, participant, topic, null);
		
		List<KalendarEvent> events = getEvents(participant);
		assertThat(events).hasSize(1);
		assertThat(events.get(0).isAllDayEvent()).isTrue();
	}
	
	@Test
	public void shouldCreateDateEvents_dates() {
		TBBroker broker = createBroker();
		Identity participant = createParticipant(broker);
		TBTopic topic = createDatesTopic(broker, TBTopicExecutionType.dates, null, null);
		
		topicBrokerService.select(participant, participant, topic, null);
		
		List<KalendarEvent> events = getEvents(participant);
		assertThat(events).hasSize(2);
		assertThat(events).extracting(KalendarEvent::isAllDayEvent).containsOnly(false);
		assertThat(events).extracting(KalendarEvent::getLocation).containsExactlyInAnyOrder("Room 1", "Room 2");
	}
	
	@Test
	public void shouldCreateDateEvents_rangeWithDates() {
		TBBroker broker = createBroker();
		Identity participant = createParticipant(broker);
		TBTopic topic = createDatesTopic(broker, TBTopicExecutionType.period, new Date(), DateUtils.addDays(new Date(), 10));
		
		topicBrokerService.select(participant, participant, topic, null);
		
		List<KalendarEvent> events = getEvents(participant);
		assertThat(events).hasSize(2);
		assertThat(events).extracting(KalendarEvent::getExternalId).doesNotContain("tb-" + broker.getKey() + "-" + topic.getKey() + "-range");
	}
	
	@Test
	public void shouldRemoveEvents_unselect() {
		TBBroker broker = createBroker();
		Identity participant = createParticipant(broker);
		TBTopic topic = createDatesTopic(broker, TBTopicExecutionType.dates, null, null);
		topicBrokerService.select(participant, participant, topic, null);
		assertThat(getEvents(participant)).hasSize(2);
		
		topicBrokerService.unselect(participant, participant, topic);
		
		assertThat(getEvents(participant)).isEmpty();
	}
	
	@Test
	public void shouldNotCreateBlocker_beyondMaxSelections() {
		TBBroker broker = createBroker();
		Identity participant = createParticipant(broker);
		for (int i = 0; i < broker.getMaxSelections().intValue(); i++) {
			TBTopic topic = createPeriodTopic(broker, new Date(), new Date());
			topicBrokerService.select(participant, participant, topic, null);
		}
		TBTopic topicBeyond = createPeriodTopic(broker, new Date(), new Date());
		
		topicBrokerService.select(participant, participant, topicBeyond, null);
		
		assertThat(getEvents(participant))
				.hasSize(broker.getMaxSelections().intValue())
				.extracting(KalendarEvent::getSubject)
				.doesNotContain("BLOCKER: " + topicBeyond.getTitle());
	}
	
	@Test
	public void shouldMoveBlocker_acrossMaxSelections() {
		TBBroker broker = createBroker();
		Identity participant = createParticipant(broker);
		TBTopic topicLast = null;
		for (int i = 0; i < broker.getMaxSelections().intValue(); i++) {
			topicLast = createPeriodTopic(broker, new Date(), new Date());
			topicBrokerService.select(participant, participant, topicLast, null);
		}
		TBTopic topicBeyond = createPeriodTopic(broker, new Date(), new Date());
		topicBrokerService.select(participant, participant, topicBeyond, null);
		
		topicBrokerService.moveSelection(participant, participant, topicBeyond, true);
		
		assertThat(getEvents(participant))
				.extracting(KalendarEvent::getSubject)
				.contains("BLOCKER: " + topicBeyond.getTitle())
				.doesNotContain("BLOCKER: " + topicLast.getTitle());
	}
	
	@Test
	public void shouldShowAssigned_enrollBeforeDone() {
		TBBroker broker = createBroker();
		Identity participant = createParticipant(broker);
		TBTopic topic = createPeriodTopic(broker, new Date(), new Date());
		topicBrokerService.select(participant, participant, topic, null);
		
		topicBrokerService.enroll(participant, participant, topic, false);
		
		assertThat(getEvents(participant)).extracting(KalendarEvent::getSubject).containsExactly(topic.getTitle());
	}
	
	@Test
	public void shouldKeepAssignedRemoveBlocker_enrollmentDone() {
		TBBroker broker = createBroker();
		Identity participant = createParticipant(broker);
		TBTopic topicEnrolled = createPeriodTopic(broker, new Date(), new Date());
		TBTopic topicNotEnrolled = createPeriodTopic(broker, new Date(), new Date());
		topicBrokerService.select(participant, participant, topicEnrolled, null);
		topicBrokerService.select(participant, participant, topicNotEnrolled, null);
		assertThat(getEvents(participant)).hasSize(2);
		String eventId = getEvents(participant).stream()
				.filter(event -> event.getSubject().contains(topicEnrolled.getTitle()))
				.findFirst().get().getID();
		
		topicBrokerService.enroll(null, participant, topicEnrolled, true);
		topicBrokerService.updateEnrollmentProcessDone(null, broker, false);
		dbInstance.commitAndCloseSession();
		
		waitForCondition(() -> getEvents(participant).size() == 1, TIMEOUT);
		List<KalendarEvent> events = getEvents(participant);
		assertThat(events).hasSize(1);
		assertThat(events.get(0).getSubject()).isEqualTo(topicEnrolled.getTitle());
		assertThat(events.get(0).getID()).isEqualTo(eventId);
	}
	
	@Test
	public void shouldRestoreBlocker_reset() {
		TBBroker broker = createBroker();
		Identity participant = createParticipant(broker);
		TBTopic topic = createPeriodTopic(broker, new Date(), new Date());
		topicBrokerService.select(participant, participant, topic, null);
		topicBrokerService.updateEnrollmentProcessDone(null, broker, false);
		dbInstance.commitAndCloseSession();
		waitForCondition(() -> getEvents(participant).isEmpty(), TIMEOUT);
		assertThat(getEvents(participant)).isEmpty();
		
		topicBrokerService.resetEnrollmentProcessStatus(null, broker);
		dbInstance.commitAndCloseSession();
		
		waitForCondition(() -> getEvents(participant).size() == 1, TIMEOUT);
		assertThat(getEvents(participant)).extracting(KalendarEvent::getSubject).containsExactly("BLOCKER: " + topic.getTitle());
	}
	
	@Test
	public void shouldUpdateEvents_topicDates() {
		TBBroker broker = createBroker();
		Identity participant = createParticipant(broker);
		TBTopic topic = createDatesTopic(broker, TBTopicExecutionType.dates, null, null);
		topicBrokerService.select(participant, participant, topic, null);
		assertThat(getEvents(participant)).hasSize(2);
		
		TBTopicDateSearchParams searchParams = new TBTopicDateSearchParams();
		searchParams.setTopic(topic);
		TBTopicDate date = topicBrokerService.getTopicDates(searchParams).get(0);
		topicBrokerService.updateTopicDates(participant, topic,
				List.of(new TBTopicDateValues(date.getKey(), date.getStartDate(), date.getEndDate(), "Room 9", null)));
		dbInstance.commitAndCloseSession();
		
		waitForCondition(() -> getEvents(participant).size() == 1, TIMEOUT);
		List<KalendarEvent> events = getEvents(participant);
		assertThat(events).hasSize(1);
		assertThat(events.get(0).getLocation()).isEqualTo("Room 9");
	}
	
	@Test
	public void shouldUpdateEvents_topicTitle() {
		TBBroker broker = createBroker();
		Identity participant = createParticipant(broker);
		TBTopic topic = createPeriodTopic(broker, new Date(), new Date());
		topicBrokerService.select(participant, participant, topic, null);
		
		String title = random();
		topicBrokerService.updateTopic(participant, topic, topic.getIdentifier(), title, null,
				TBTopicExecutionType.period, topic.getBeginDate(), topic.getEndDate(), 0, 6, null);
		dbInstance.commitAndCloseSession();
		
		waitForCondition(() -> getEvents(participant).get(0).getSubject().contains(title), TIMEOUT);
		assertThat(getEvents(participant)).extracting(KalendarEvent::getSubject).containsExactly("BLOCKER: " + title);
	}
	
	@Test
	public void shouldRemoveEvents_deleteTopic() {
		TBBroker broker = createBroker();
		Identity participant = createParticipant(broker);
		TBTopic topic = createPeriodTopic(broker, new Date(), new Date());
		topicBrokerService.select(participant, participant, topic, null);
		assertThat(getEvents(participant)).hasSize(1);
		
		topicBrokerService.deleteTopicSoftly(participant, topic);
		dbInstance.commitAndCloseSession();
		
		waitForCondition(() -> getEvents(participant).isEmpty(), TIMEOUT);
		assertThat(getEvents(participant)).isEmpty();
	}
	
	@Test
	public void shouldRemoveEvents_deleteBroker() {
		TBBroker broker = createBroker();
		Identity participant = createParticipant(broker);
		TBTopic topic = createPeriodTopic(broker, new Date(), new Date());
		topicBrokerService.select(participant, participant, topic, null);
		assertThat(getEvents(participant)).hasSize(1);
		
		topicBrokerService.deleteBroker(broker.getRepositoryEntry(), broker.getSubIdent());
		dbInstance.commitAndCloseSession();
		
		waitForCondition(() -> getEvents(participant).isEmpty(), TIMEOUT);
		assertThat(getEvents(participant)).isEmpty();
	}
	
	@Test
	public void shouldRemoveAndRestoreEvents_courseTrash() {
		TBBroker broker = createBroker();
		Identity participant = createParticipant(broker);
		TBTopic topic = createPeriodTopic(broker, new Date(), new Date());
		topicBrokerService.select(participant, participant, topic, null);
		assertThat(getEvents(participant)).hasSize(1);
		
		RepositoryEntry entry = repositoryManager.setStatus(broker.getRepositoryEntry(), RepositoryEntryStatusEnum.trash);
		dbInstance.commitAndCloseSession();
		topicBrokerService.syncCalendars(entry, null);
		
		waitForCondition(() -> getEvents(participant).isEmpty(), TIMEOUT);
		assertThat(getEvents(participant)).isEmpty();
		
		entry = repositoryManager.setStatus(entry, RepositoryEntryStatusEnum.published);
		dbInstance.commitAndCloseSession();
		topicBrokerService.syncCalendars(entry, null);
		
		waitForCondition(() -> getEvents(participant).size() == 1, TIMEOUT);
		assertThat(getEvents(participant)).hasSize(1);
	}
	
	@Test
	public void shouldRemoveEvents_membershipRemoved() {
		TBBroker broker = createBroker();
		Identity participant = createParticipant(broker);
		TBTopic topic = createPeriodTopic(broker, new Date(), new Date());
		topicBrokerService.select(participant, participant, topic, null);
		assertThat(getEvents(participant)).hasSize(1);
		
		repositoryService.removeRole(participant, broker.getRepositoryEntry(), GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		topicBrokerService.syncCalendars(broker.getRepositoryEntry(), participant);
		
		waitForCondition(() -> getEvents(participant).isEmpty(), TIMEOUT);
		assertThat(getEvents(participant)).isEmpty();
		TBSelectionSearchParams searchParams = new TBSelectionSearchParams();
		searchParams.setBroker(broker);
		searchParams.setIdentity(participant);
		assertThat(topicBrokerService.getSelections(searchParams)).hasSize(1);
		
		repositoryService.addRole(participant, broker.getRepositoryEntry(), GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		topicBrokerService.syncCalendars(broker.getRepositoryEntry(), participant);
		
		waitForCondition(() -> getEvents(participant).size() == 1, TIMEOUT);
		assertThat(getEvents(participant)).hasSize(1);
	}
	
	@Test
	public void shouldKeepForeignEvents() {
		TBBroker broker = createBroker();
		Identity participant = createParticipant(broker);
		Kalendar cal = calendarManager.getCalendar(CalendarManager.TYPE_USER, participant.getName());
		KalendarEvent foreignEvent = new KalendarEvent(UUID.randomUUID().toString(), null, random(), ZonedDateTime.now(), ZonedDateTime.now().plusHours(1));
		calendarManager.addEventTo(cal, foreignEvent);
		TBTopic topic = createPeriodTopic(broker, new Date(), new Date());
		
		topicBrokerService.select(participant, participant, topic, null);
		assertThat(getEvents(participant)).hasSize(1);
		topicBrokerService.unselect(participant, participant, topic);
		
		assertThat(calendarManager.getCalendar(CalendarManager.TYPE_USER, participant.getName()).getEvents())
				.extracting(KalendarEvent::getID)
				.containsExactly(foreignEvent.getID());
	}
	
	private List<KalendarEvent> getEvents(Identity identity) {
		return calendarManager.getCalendar(CalendarManager.TYPE_USER, identity.getName()).getEvents().stream()
				.filter(event -> TBCalendarSyncher.EXTERNAL_SOURCE.equals(event.getExternalSource()))
				.toList();
	}
	
	private TBBroker createBroker() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		TBBroker broker = topicBrokerService.createBroker(JunitTestHelper.getDefaultAuthor(), entry, String.valueOf(System.nanoTime()));
		dbInstance.commitAndCloseSession();
		return broker;
	}
	
	private Identity createParticipant(TBBroker broker) {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		repositoryService.addRole(participant, broker.getRepositoryEntry(), GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		return participant;
	}
	
	private TBTopic createPeriodTopic(TBBroker broker, Date begin, Date end) {
		Identity doer = JunitTestHelper.getDefaultAuthor();
		TBTopic topic = topicBrokerService.createTopic(doer, broker);
		topic = topicBrokerService.updateTopic(doer, topic, random(), random(), null, TBTopicExecutionType.period,
				begin, end, 0, 6, null);
		dbInstance.commitAndCloseSession();
		return topic;
	}
	
	private TBTopic createDatesTopic(TBBroker broker, TBTopicExecutionType executionType, Date begin, Date end) {
		Identity doer = JunitTestHelper.getDefaultAuthor();
		TBTopic topic = topicBrokerService.createTopic(doer, broker);
		topic = topicBrokerService.updateTopic(doer, topic, random(), random(), null, executionType, begin, end, 0, 6, null);
		Date start1 = DateUtils.addDays(new Date(), 1);
		Date start2 = DateUtils.addDays(new Date(), 2);
		topicBrokerService.updateTopicDates(doer, topic, List.of(
				new TBTopicDateValues(null, start1, DateUtils.addHours(start1, 2), "Room 1", random()),
				new TBTopicDateValues(null, start2, DateUtils.addHours(start2, 2), "Room 2", null)));
		dbInstance.commitAndCloseSession();
		return topic;
	}
	
}
