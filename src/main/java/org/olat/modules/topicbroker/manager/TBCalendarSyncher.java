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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.commons.calendar.CalendarManagedFlag;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.commons.services.taskexecutor.TaskRunnable;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.course.nodes.TopicBrokerCourseNode;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBBrokerRef;
import org.olat.modules.topicbroker.TBBrokerSearchParams;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBSelectionSearchParams;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TBTopicDate;
import org.olat.modules.topicbroker.TBTopicDateSearchParams;
import org.olat.modules.topicbroker.TBTopicRef;
import org.olat.modules.topicbroker.ui.TBUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 20 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Component
public class TBCalendarSyncher {
	
	private static final Logger log = Tracing.createLoggerFor(TBCalendarSyncher.class);
	
	static final String EXTERNAL_SOURCE = "topicbroker";
	private static final CalendarManagedFlag[] CAL_MANAGED_FLAGS = new CalendarManagedFlag[] { CalendarManagedFlag.all };
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TBBrokerDAO brokerDao;
	@Autowired
	private TBSelectionDAO selectionDao;
	@Autowired
	private TBTopicDateDAO topicDateDao;
	@Autowired
	private CalendarModule calendarModule;
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private TaskExecutorManager taskExecutorManager;
	@Autowired
	private BaseSecurity securityManager;
	
	void syncCalendar(TBBrokerRef broker, Identity identity) {
		if (!isEnabled()) {
			return;
		}
		
		try {
			TBBroker reloadedBroker = loadBroker(broker);
			if (reloadedBroker == null) {
				return;
			}
			
			RepositoryEntry entry = repositoryService.loadByKey(reloadedBroker.getRepositoryEntry().getKey());
			List<TBSelection> selections = List.of();
			if (isActive(entry) && repositoryService.hasRoleExpanded(identity, entry, GroupRoles.participant.name())) {
				TBSelectionSearchParams searchParams = createSelectionSearchParams(reloadedBroker);
				searchParams.setIdentity(identity);
				selections = selectionDao.loadSelections(searchParams);
			}
			sync(reloadedBroker, entry, identity, selections, loadTopicKeyToDates(reloadedBroker));
		} catch (Exception e) {
			log.error("Sync calendar of topic broker {} failed for identity {}.", broker.getKey(), identity.getKey(), e);
		}
	}
	
	void syncCalendarsAsync(TBBrokerRef broker) {
		syncCalendarsAsync(broker, (TBTopicRef)null);
	}
	
	void syncCalendarsAsync(TBTopic topic) {
		syncCalendarsAsync(topic.getBroker(), topic);
	}
	
	private void syncCalendarsAsync(TBBrokerRef broker, TBTopicRef topic) {
		if (!isEnabled()) {
			return;
		}
		
		Long brokerKey = broker.getKey();
		TBBrokerRef brokerRef = () -> brokerKey;
		Long topicKey = topic != null? topic.getKey(): null;
		TBTopicRef topicRef = topicKey != null? () -> topicKey: null;
		dbInstance.commit();
		taskExecutorManager.execute(new SequentialTask(() -> syncCalendars(brokerRef, topicRef)));
	}
	
	void syncCalendars(TBBrokerRef broker, TBTopicRef topic) {
		TBSelectionSearchParams searchParams = createSelectionSearchParams(broker);
		searchParams.setTopic(topic);
		List<Identity> identities = selectionDao.loadSelections(searchParams).stream()
				.map(selection -> selection.getParticipant().getIdentity())
				.distinct()
				.toList();
		syncCalendars(broker, identities);
	}
	
	void syncCalendarsAsync(TBBrokerRef broker, Collection<? extends IdentityRef> identities) {
		if (!isEnabled() || identities.isEmpty()) {
			return;
		}
		
		Long brokerKey = broker.getKey();
		TBBrokerRef brokerRef = () -> brokerKey;
		List<IdentityRef> identityRefs = identities.stream()
				.map(identity -> (IdentityRef)new IdentityRefImpl(identity.getKey()))
				.toList();
		dbInstance.commit();
		taskExecutorManager.execute(new SequentialTask(() -> syncCalendars(brokerRef, securityManager.loadIdentityByRefs(identityRefs))));
	}
	
	void syncCalendars(TBBrokerRef broker, List<Identity> identities) {
		if (identities.isEmpty()) {
			return;
		}
		
		TBBroker reloadedBroker = loadBroker(broker);
		if (reloadedBroker == null) {
			return;
		}
		
		RepositoryEntry entry = repositoryService.loadByKey(reloadedBroker.getRepositoryEntry().getKey());
		Set<Long> participantKeys = isActive(entry)
				? new HashSet<>(repositoryService.getMemberKeys(entry, RepositoryEntryRelationType.all, GroupRoles.participant.name()))
				: Set.of();
		
		TBSelectionSearchParams searchParams = createSelectionSearchParams(reloadedBroker);
		searchParams.setIdentities(identities);
		Map<Long, List<TBSelection>> identityKeyToSelections = selectionDao.loadSelections(searchParams).stream()
				.collect(Collectors.groupingBy(selection -> selection.getParticipant().getIdentity().getKey()));
		
		Map<Long, List<TBTopicDate>> topicKeyToDates = loadTopicKeyToDates(reloadedBroker);
		for (Identity identity : identities) {
			List<TBSelection> selections = participantKeys.contains(identity.getKey())
					? identityKeyToSelections.getOrDefault(identity.getKey(), List.of())
					: List.of();
			sync(reloadedBroker, entry, identity, selections, topicKeyToDates);
		}
	}
	
	void unsyncCalendarsAsync(TBBrokerRef broker) {
		TBSelectionSearchParams searchParams = new TBSelectionSearchParams();
		searchParams.setBroker(broker);
		searchParams.setFetchParticipant(true);
		searchParams.setFetchIdentity(true);
		List<String> identityNames = selectionDao.loadSelections(searchParams).stream()
				.map(selection -> selection.getParticipant().getIdentity().getName())
				.distinct()
				.toList();
		if (identityNames.isEmpty()) {
			return;
		}
		
		String externalIdPrefix = getExternalIdPrefix(broker);
		taskExecutorManager.execute(new SequentialTask(() -> identityNames.forEach(identityName -> removeEvents(identityName, externalIdPrefix))));
	}
	
	private void removeEvents(String identityName, String externalIdPrefix) {
		try {
			Kalendar cal = calendarManager.getCalendar(CalendarManager.TYPE_USER, identityName);
			List<KalendarEvent> events = cal.getEvents().stream()
					.filter(event -> isBrokerEvent(event, externalIdPrefix))
					.toList();
			if (!events.isEmpty()) {
				calendarManager.removeEventsFrom(cal, events);
			}
		} catch (Exception e) {
			log.error("Remove topic broker calendar events {} of {} failed.", externalIdPrefix, identityName, e);
		}
	}
	
	private void sync(TBBroker broker, RepositoryEntry entry, Identity identity, List<TBSelection> selections,
			Map<Long, List<TBTopicDate>> topicKeyToDates) {
		try {
			Kalendar cal = calendarManager.getCalendar(CalendarManager.TYPE_USER, identity.getName());
			Map<String, KalendarEvent> externalIdToEvent = createEvents(broker, entry, identity, selections, topicKeyToDates);
			
			String externalIdPrefix = getExternalIdPrefix(broker);
			List<KalendarEvent> modifiedEvents = new ArrayList<>();
			List<KalendarEvent> removedEvents = new ArrayList<>();
			for (KalendarEvent event : cal.getEvents()) {
				if (isBrokerEvent(event, externalIdPrefix)) {
					KalendarEvent targetEvent = externalIdToEvent.remove(event.getExternalId());
					if (targetEvent == null) {
						removedEvents.add(event);
					} else if (updateEvent(event, targetEvent)) {
						modifiedEvents.add(event);
					}
				}
			}
			
			List<KalendarEvent> newEvents = new ArrayList<>(externalIdToEvent.values());
			if (!newEvents.isEmpty() || !modifiedEvents.isEmpty()) {
				calendarManager.updateCalendar(cal, newEvents, modifiedEvents);
			}
			if (!removedEvents.isEmpty()) {
				calendarManager.removeEventsFrom(cal, removedEvents);
			}
		} catch (Exception e) {
			log.error("Sync calendar of topic broker {} failed for identity {}.", broker.getKey(), identity.getKey(), e);
		}
	}
	
	private Map<String, KalendarEvent> createEvents(TBBroker broker, RepositoryEntry entry, Identity identity,
			List<TBSelection> selections, Map<Long, List<TBTopicDate>> topicKeyToDates) {
		Map<String, KalendarEvent> externalIdToEvent = new HashMap<>();
		if (selections.isEmpty()) {
			return externalIdToEvent;
		}
		
		Translator translator = createTranslator(identity);
		KalendarEventLink link = createLink(broker, entry);
		for (TBSelection selection : selections) {
			TBTopic topic = selection.getTopic();
			boolean blocker = !selection.isEnrolled()
					&& broker.getEnrollmentDoneDate() == null
					&& (broker.getMaxSelections() == null || selection.getSortOrder() <= broker.getMaxSelections().intValue());
			if (topic.getDeletedDate() == null && (selection.isEnrolled() || blocker)) {
				String subject = blocker
						? translator.translate("calendar.event.blocker", topic.getTitle())
						: topic.getTitle();
				List<TBTopicDate> dates = topicKeyToDates.getOrDefault(topic.getKey(), List.of());
				if (!dates.isEmpty()) {
					for (TBTopicDate date : dates) {
						ZonedDateTime begin = DateUtils.toZonedDateTime(date.getStartDate(), calendarModule.getDefaultZoneId());
						ZonedDateTime end = DateUtils.toZonedDateTime(date.getEndDate(), calendarModule.getDefaultZoneId());
						String description = createDescription(translator, topic, date.getDescription(), blocker);
						KalendarEvent event = createEvent(broker, topic, date.getKey().toString(), subject, begin, end,
								false, date.getLocation(), description, link);
						externalIdToEvent.put(event.getExternalId(), event);
					}
				} else if (topic.getBeginDate() != null && topic.getEndDate() != null) {
					ZonedDateTime begin = DateUtils.setZonedTime(DateUtils.toZonedDateTime(topic.getBeginDate(), calendarModule.getDefaultZoneId()), 0, 0, 0);
					ZonedDateTime end = DateUtils.setZonedTime(DateUtils.toZonedDateTime(topic.getEndDate(), calendarModule.getDefaultZoneId()), 23, 59, 59);
					String description = createDescription(translator, topic, null, blocker);
					KalendarEvent event = createEvent(broker, topic, "range", subject, begin, end, true, null,
							description, link);
					externalIdToEvent.put(event.getExternalId(), event);
				}
			}
		}
		return externalIdToEvent;
	}
	
	private KalendarEvent createEvent(TBBroker broker, TBTopic topic, String externalIdSuffix, String subject,
			ZonedDateTime begin, ZonedDateTime end, boolean allDay, String location, String description,
			KalendarEventLink link) {
		KalendarEvent event = new KalendarEvent(UUID.randomUUID().toString(), null, subject, begin, end);
		event.setAllDayEvent(allDay);
		event.setLocation(location);
		event.setDescription(description);
		event.setKalendarEventLinks(new ArrayList<>(List.of(link)));
		event.setManagedFlags(CAL_MANAGED_FLAGS);
		event.setExternalSource(EXTERNAL_SOURCE);
		event.setExternalId(getExternalIdPrefix(broker) + topic.getKey() + "-" + externalIdSuffix);
		return event;
	}
	
	private boolean updateEvent(KalendarEvent event, KalendarEvent targetEvent) {
		KalendarEventLink link = event.getKalendarEventLinks().isEmpty()? null: event.getKalendarEventLinks().get(0);
		KalendarEventLink targetLink = targetEvent.getKalendarEventLinks().get(0);
		boolean changed = !Objects.equals(event.getSubject(), targetEvent.getSubject())
				|| !isSameInstant(event.getBegin(), targetEvent.getBegin())
				|| !isSameInstant(event.getEnd(), targetEvent.getEnd())
				|| event.isAllDayEvent() != targetEvent.isAllDayEvent()
				|| !Objects.equals(event.getLocation(), targetEvent.getLocation())
				|| !Objects.equals(event.getDescription(), targetEvent.getDescription())
				|| link == null
				|| !Objects.equals(link.getURI(), targetLink.getURI())
				|| !Objects.equals(link.getDisplayName(), targetLink.getDisplayName());
		if (changed) {
			event.setSubject(targetEvent.getSubject());
			event.setBegin(targetEvent.getBegin());
			event.setEnd(targetEvent.getEnd());
			event.setAllDayEvent(targetEvent.isAllDayEvent());
			event.setLocation(targetEvent.getLocation());
			event.setDescription(targetEvent.getDescription());
			event.setKalendarEventLinks(targetEvent.getKalendarEventLinks());
			event.setManagedFlags(CAL_MANAGED_FLAGS);
			event.setExternalSource(EXTERNAL_SOURCE);
		}
		return changed;
	}
	
	private boolean isSameInstant(ZonedDateTime date1, ZonedDateTime date2) {
		if (date1 == null || date2 == null) {
			return date1 == date2;
		}
		return date1.toInstant().equals(date2.toInstant());
	}
	
	private String createDescription(Translator translator, TBTopic topic, String dateDescription, boolean blocker) {
		StringBuilder sb = new StringBuilder();
		if (blocker) {
			sb.append(translator.translate("calendar.event.blocker.desc")).append("\n");
		}
		sb.append(translator.translate("calendar.event.identifier", topic.getIdentifier()));
		if (StringHelper.containsNonWhitespace(dateDescription)) {
			sb.append("\n").append(dateDescription);
		}
		return sb.toString();
	}
	
	private KalendarEventLink createLink(TBBroker broker, RepositoryEntry entry) {
		String businessPath = "[RepositoryEntry:" + entry.getKey() + "][CourseNode:" + broker.getSubIdent() + "]";
		String url = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
		return new KalendarEventLink(EXTERNAL_SOURCE, broker.getKey().toString(), entry.getDisplayname(), url,
				TopicBrokerCourseNode.ICON_CSS);
	}
	
	private Translator createTranslator(Identity identity) {
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(identity.getUser().getPreferences().getLanguage());
		return Util.createPackageTranslator(TBUIFactory.class, locale);
	}
	
	private TBSelectionSearchParams createSelectionSearchParams(TBBrokerRef broker) {
		TBSelectionSearchParams searchParams = new TBSelectionSearchParams();
		searchParams.setBroker(broker);
		searchParams.setFetchParticipant(true);
		searchParams.setFetchIdentity(true);
		searchParams.setFetchTopic(true);
		return searchParams;
	}
	
	private Map<Long, List<TBTopicDate>> loadTopicKeyToDates(TBBrokerRef broker) {
		TBTopicDateSearchParams searchParams = new TBTopicDateSearchParams();
		searchParams.setBroker(broker);
		return topicDateDao.loadDates(searchParams).stream()
				.collect(Collectors.groupingBy(date -> date.getTopic().getKey()));
	}
	
	private TBBroker loadBroker(TBBrokerRef broker) {
		TBBrokerSearchParams searchParams = new TBBrokerSearchParams();
		searchParams.setBroker(broker);
		List<TBBroker> brokers = brokerDao.loadBrokers(searchParams);
		return !brokers.isEmpty()? brokers.get(0): null;
	}
	
	private boolean isActive(RepositoryEntry entry) {
		return entry != null
				&& entry.getEntryStatus() != RepositoryEntryStatusEnum.trash
				&& entry.getEntryStatus() != RepositoryEntryStatusEnum.deleted;
	}
	
	private boolean isBrokerEvent(KalendarEvent event, String externalIdPrefix) {
		return event.getExternalId() != null && event.getExternalId().startsWith(externalIdPrefix);
	}
	
	private String getExternalIdPrefix(TBBrokerRef broker) {
		return "tb-" + broker.getKey() + "-";
	}
	
	private boolean isEnabled() {
		return calendarModule.isEnabled() && calendarModule.isEnablePersonalCalendar();
	}
	
	private record SequentialTask(Runnable task) implements TaskRunnable {
		
		@Override
		public void run() {
			task.run();
		}
		
		@Override
		public Queue getExecutorsQueue() {
			return Queue.sequential;
		}
		
	}
	
}
