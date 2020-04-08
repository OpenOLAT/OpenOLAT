/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.bigbluebutton.manager;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.Logger;
import org.olat.commons.calendar.CalendarManagedFlag;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.BigBlueButtonRecording;
import org.olat.modules.bigbluebutton.BigBlueButtonServer;
import org.olat.modules.bigbluebutton.BigBlueButtonTemplatePermissions;
import org.olat.modules.bigbluebutton.GuestPolicyEnum;
import org.olat.modules.bigbluebutton.model.BigBlueButtonError;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrorCodes;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingImpl;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingInfos;
import org.olat.modules.bigbluebutton.model.BigBlueButtonServerInfos;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class BigBlueButtonManagerImpl implements BigBlueButtonManager, InitializingBean {
	
	private static final Logger log = Tracing.createLoggerFor(BigBlueButtonManagerImpl.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BigBlueButtonServerDAO bigBlueButtonServerDao;
	@Autowired
	private BigBlueButtonMeetingDAO bigBlueButtonMeetingDao;
	@Autowired
	private BigBlueButtonMeetingTemplateDAO bigBlueButtonMeetingTemplateDao;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		List<BigBlueButtonMeetingTemplate> templates = bigBlueButtonMeetingTemplateDao.getTemplates();
		
		// Online meeting, all features enable
		defaultTemplate("sys-meetings", "Meeting", 2, 8, 240,
				Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, // recording
				Boolean.FALSE, Boolean.TRUE, 	// webcams moderator only, unmute
				Boolean.FALSE, Boolean.FALSE, 	// cam, mic
				Boolean.FALSE, Boolean.FALSE, 	// chat
				Boolean.FALSE, Boolean.FALSE, 	// notes, layout
				Boolean.TRUE, 					// breakout
				Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, // user list, join-lock, lock configurable
				GuestPolicyEnum.ALWAYS_ACCEPT, true, templates);
		
		// Traditional classroom setting with many participants. Only presenter has video
		defaultTemplate("sys-classes", "Classroom", 10, 30, 240,
				Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, // recording
				Boolean.TRUE,  Boolean.TRUE, 	// webcams moderator only, unmute
				Boolean.TRUE,  Boolean.FALSE, 	// cam, mic
				Boolean.FALSE, Boolean.FALSE, 	// chat
				Boolean.FALSE, Boolean.FALSE, 	// notes, layout
				Boolean.TRUE, 					// breakout
				Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, // user list, join-lock, lock configurable
				GuestPolicyEnum.ALWAYS_ACCEPT, true, templates);
		
		// Mixed setup, some with webcams, some without. 
		defaultTemplate("sys-cafe", "Cafe", 5, 15, 240,
				Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, // recording
				Boolean.FALSE, Boolean.TRUE, 	// webcams moderator only, unmute
				Boolean.TRUE,  Boolean.FALSE, 	// cam, mic
				Boolean.FALSE, Boolean.FALSE, 	// chat
				Boolean.FALSE, Boolean.FALSE, 	// notes, layout
				Boolean.TRUE, 					// breakout
				Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, // user list, join-lock, lock configurable 
				GuestPolicyEnum.ALWAYS_ACCEPT, false, templates);
		
		// Interview situation, face-to-face meeting
		defaultTemplate("sys-interview", "Interview", 5, 2, 240,
				Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, // recording
				Boolean.FALSE, Boolean.TRUE, 	// webcams moderator only, unmute
				Boolean.FALSE, Boolean.FALSE, 	// cam, mic
				Boolean.FALSE, Boolean.FALSE, 	// chat
				Boolean.FALSE, Boolean.FALSE, 	// notes, layout
				Boolean.FALSE, 					// breakout
				Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, // user list, join-lock, lock configurable
				GuestPolicyEnum.ALWAYS_ACCEPT, false, templates);
		
		// Exam monitoring. Participants have video but only presenter can see the video
		defaultTemplate("sys-assessment", "Assessment", 1, 30, 240,
				Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, // recording
				Boolean.TRUE,  Boolean.TRUE, 	// webcams moderator only, unmute
				Boolean.FALSE, Boolean.TRUE, 	// cam, mic
				Boolean.TRUE,  Boolean.FALSE, 	// chat
				Boolean.TRUE,  Boolean.TRUE, 	// notes, layout
				Boolean.FALSE, 					// breakout
				Boolean.TRUE,  Boolean.TRUE, Boolean.FALSE, // user list, join-lock, lock configurable
				GuestPolicyEnum.ALWAYS_ACCEPT, false, templates);

	}
	
	private void defaultTemplate(String externalId, String name,
			Integer maxConcurrentMeetings, Integer maxParticipants, Integer maxDuration,
			Boolean muteOnStart, Boolean autoStartRecording, Boolean allowStartStopRecording,
			Boolean webcamsOnlyForModerator, Boolean allowModsToUnmuteUsers,
			Boolean lockSettingsDisableCam, Boolean lockSettingsDisableMic,
			Boolean lockSettingsDisablePrivateChat, Boolean lockSettingsDisablePublicChat,
			Boolean lockSettingsDisableNote, Boolean lockSettingsLockedLayout,
			Boolean breakoutRoomsEnabled,
			Boolean lockSettingsHideUserList, Boolean lockSettingsLockOnJoin, Boolean lockSettingsLockOnJoinConfigurable,
			GuestPolicyEnum guestPolicy, boolean enabled, List<BigBlueButtonMeetingTemplate> templates) {
		
		BigBlueButtonMeetingTemplate template = templates.stream()
				.filter(tpl -> externalId.equals(tpl.getExternalId()))
				.findFirst().orElse(null);
		if(template != null) {
			return;
		}
		
		template = bigBlueButtonMeetingTemplateDao.createTemplate(name, externalId, true);
		template.setMaxConcurrentMeetings(maxConcurrentMeetings);
		template.setMaxParticipants(maxParticipants);
		template.setMaxDuration(maxDuration);
		template.setRecord(Boolean.TRUE);
		template.setBreakoutRoomsEnabled(Boolean.TRUE);
		template.setPermissions(BigBlueButtonTemplatePermissions.valuesAsList());
		template.setMuteOnStart(muteOnStart);
		template.setAutoStartRecording(autoStartRecording);
		template.setAllowStartStopRecording(allowStartStopRecording);
		template.setWebcamsOnlyForModerator(webcamsOnlyForModerator);
		template.setAllowModsToUnmuteUsers(allowModsToUnmuteUsers);
		template.setBreakoutRoomsEnabled(breakoutRoomsEnabled);
		
		template.setLockSettingsDisableCam(lockSettingsDisableCam);
		template.setLockSettingsDisableMic(lockSettingsDisableMic);
		template.setLockSettingsDisablePrivateChat(lockSettingsDisablePrivateChat);
		template.setLockSettingsDisablePublicChat(lockSettingsDisablePublicChat);
		template.setLockSettingsDisableNote(lockSettingsDisableNote);
		template.setLockSettingsLockedLayout(lockSettingsLockedLayout);
		
		template.setLockSettingsHideUserList(lockSettingsHideUserList);
		template.setLockSettingsLockOnJoin(lockSettingsLockOnJoin);
		template.setLockSettingsLockOnJoinConfigurable(lockSettingsLockOnJoinConfigurable);

		template.setGuestPolicyEnum(guestPolicy);
		template.setEnabled(enabled);
		bigBlueButtonMeetingTemplateDao.updateTemplate(template);
	}

	@Override
	public BigBlueButtonServer createServer(String url, String recordingUrl, String sharedSecret) {
		return bigBlueButtonServerDao.createServer(url, recordingUrl, sharedSecret);
	}

	@Override
	public BigBlueButtonServer updateServer(BigBlueButtonServer server) {
		return bigBlueButtonServerDao.updateServer(server);
	}

	@Override
	public List<BigBlueButtonServer> getServers() {
		return bigBlueButtonServerDao.getServers();
	}

	@Override
	public List<BigBlueButtonServerInfos> getServersInfos() {
		List<BigBlueButtonServer> servers = getServers();
		return getServersInfos(servers);
	}

	@Override
	public List<BigBlueButtonServerInfos> filterServersInfos(List<BigBlueButtonServerInfos> infos) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -5);
		Date from = cal.getTime();
		cal.add(Calendar.DATE, 10);
		Date to = cal.getTime();
		
		List<String> meetingsIds = this.bigBlueButtonMeetingDao.getMeetingsIds(from, to);
		Set<String> instanceMeetingsIds = new HashSet<>(meetingsIds);
		List<BigBlueButtonServerInfos> instanceInfos = new ArrayList<>();
		for(BigBlueButtonServerInfos info:infos) {
			List<BigBlueButtonMeetingInfos> meetings = info.getMeetingsInfos();
			List<BigBlueButtonMeetingInfos> instanceMeetings = meetings.stream()
					.filter(meeting -> instanceMeetingsIds.contains(meeting.getMeetingId()))
					.collect(Collectors.toList());
			double load = this.calculateLoad(info.getServer(), instanceMeetings);
			instanceInfos.add(new BigBlueButtonServerInfos(info.getServer(), instanceMeetings, load));
		}
		return instanceInfos;
	}

	@Override
	public void deleteServer(BigBlueButtonServer server, BigBlueButtonErrors errors) {
		List<BigBlueButtonMeeting> meetings = bigBlueButtonMeetingDao.getMeetings(server);
		for(BigBlueButtonMeeting meeting:meetings) {
			deleteMeeting(meeting, errors);
		}
		bigBlueButtonServerDao.deleteServer(server);
	}

	@Override
	public boolean hasServer(String url) {
		List<BigBlueButtonServer> servers = this.getServers();
		return servers.stream().anyMatch(server -> server.getUrl().startsWith(url) || url.startsWith(server.getUrl()));
	}

	@Override
	public BigBlueButtonMeeting createAndPersistMeeting(String name, RepositoryEntry entry, String subIdent, BusinessGroup businessGroup) {
		return bigBlueButtonMeetingDao.createAndPersistMeeting(name, entry, subIdent, businessGroup);
	}
	
	@Override
	public boolean isSlotAvailable(BigBlueButtonMeeting meeting, BigBlueButtonMeetingTemplate template, Date startDate, long leadTime, Date endDate, long followupTime) {
		if(template == null) return false; // template are mandatory
		if(template.getMaxConcurrentMeetings() == null) {
			return true;
		}
		Date start = bigBlueButtonMeetingDao.calculateStartWithLeadTime(startDate, leadTime);
		Date end = bigBlueButtonMeetingDao.calculateEndWithFollowupTime(endDate, followupTime);
		List<Long> currentMeetings = bigBlueButtonMeetingDao.getConcurrentMeetings(template, start, end);
		if(meeting != null && currentMeetings.contains(meeting.getKey())) {
			return true; // it's my slot
		}
		return currentMeetings.size() < template.getMaxConcurrentMeetings().intValue();
	}

	@Override
	public BigBlueButtonMeeting getMeeting(BigBlueButtonMeeting meeting) {
		return bigBlueButtonMeetingDao.loadByKey(meeting.getKey());
	}

	@Override
	public BigBlueButtonMeeting updateMeeting(BigBlueButtonMeeting meeting) {
		updateCalendarEvent(meeting);
		return bigBlueButtonMeetingDao.updateMeeting(meeting);
	}	

	@Override
	public BigBlueButtonMeetingTemplate createAndPersistTemplate(String name) {
		return bigBlueButtonMeetingTemplateDao.createTemplate(name, null, false);
	}
	
	@Override
	public BigBlueButtonMeetingTemplate updateTemplate(BigBlueButtonMeetingTemplate template) {
		return bigBlueButtonMeetingTemplateDao.updateTemplate(template);
	}

	@Override
	public void deleteTemplate(BigBlueButtonMeetingTemplate template) {
		bigBlueButtonMeetingTemplateDao.deleteTemplate(template);
	}

	@Override
	public boolean isTemplateInUse(BigBlueButtonMeetingTemplate template) {
		return bigBlueButtonMeetingTemplateDao.isTemplateInUse(template);
	}

	@Override
	public List<BigBlueButtonMeetingTemplate> getTemplates() {
		return bigBlueButtonMeetingTemplateDao.getTemplates();
	}

	@Override
	public List<BigBlueButtonMeetingTemplate> getTemplates(List<BigBlueButtonTemplatePermissions> permissions) {
		List<BigBlueButtonMeetingTemplate> templates = getTemplates();
		
		List<BigBlueButtonMeetingTemplate> authorisedTemplates = new ArrayList<>();
		for(BigBlueButtonMeetingTemplate template:templates) {
			if(template.isEnabled() && template.availableTo(permissions))  {
				authorisedTemplates.add(template);
			}
		}
		return authorisedTemplates;
	}

	@Override
	public List<BigBlueButtonMeeting> getAllMeetings() {
		return bigBlueButtonMeetingDao.getAllMeetings();
	}

	@Override
	public List<BigBlueButtonMeeting> getMeetings(RepositoryEntryRef entry, String subIdent, BusinessGroup businessGroup) {
		return bigBlueButtonMeetingDao.getMeetings(entry, subIdent, businessGroup);
	}
	
	@Override
	public List<BigBlueButtonMeeting> getUpcomingsMeetings(RepositoryEntryRef entry, String subIdent, int maxResults) {
		return bigBlueButtonMeetingDao.getUpcomingMeetings(entry, subIdent, maxResults);
	}
	
	@Override
	public boolean deleteMeeting(BigBlueButtonMeeting meeting, BigBlueButtonErrors errors) {
		BigBlueButtonMeeting reloadedMeeting = bigBlueButtonMeetingDao.loadByKey(meeting.getKey());
		if(reloadedMeeting != null) {
			removeCalendarEvent(reloadedMeeting);
			deleteRecordings(meeting, errors);
			bigBlueButtonMeetingDao.deleteMeeting(reloadedMeeting);
		}
		return false;
	}
	
	public BigBlueButtonServer getAvailableServer() {
		List<BigBlueButtonServer> servers = getServers();
		List<BigBlueButtonServer> availableServers = servers.stream()
				.filter(BigBlueButtonServer::isEnabled)
				.collect(Collectors.toList());
		if(availableServers.isEmpty()) {
			return null;
		} else if(availableServers.size() == 1) {
			return availableServers.get(0);
		}
		return getBigBlueButtonServer(servers);
	}
	
	private BigBlueButtonServer getBigBlueButtonServer(List<BigBlueButtonServer> servers) {
		List<BigBlueButtonServerInfos> serversInfos = getServersInfos(servers);
		if(serversInfos.isEmpty()) {
			return null;
		}
		Collections.sort(serversInfos, new ServerLoadComparator());
		return serversInfos.get(0).getServer();
	}
	
	private List<BigBlueButtonServerInfos> getServersInfos(List<BigBlueButtonServer> servers) {
		CountDownLatch serverLatch = new CountDownLatch(servers.size());
		
		List<MeetingInfosThread> threads = new ArrayList<>();
		for(BigBlueButtonServer server:servers) {
			MeetingInfosThread thread = new MeetingInfosThread(server, serverLatch);
			threads.add(thread);
			thread.start();
		}
		
		try {
			serverLatch.await(15, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error("", e);
		}

		return threads.stream()
			.filter(MeetingInfosThread::isExecuted)
			.filter(thread -> !thread.hasErrors())
			.map(thread -> new BigBlueButtonServerInfos(thread.getServer(), thread.getMeetingsInfos(),
					calculateLoad(thread.getServer(), thread.getMeetingsInfos())))
			.collect(Collectors.toList());
	}
	
	private double calculateLoad(BigBlueButtonServer server, List<BigBlueButtonMeetingInfos> meetingsInfos) {
		double load = 0.0d;
		for(BigBlueButtonMeetingInfos meetingInfos:meetingsInfos) {
			load += meetingInfos.getListenerCount() * 1.0d;
			load += meetingInfos.getVideoCount() * 3.0d;
			load += meetingInfos.getVoiceParticipantCount() * 2.0d;
		}
		if(load > 0.0d
				&& server.getCapacityFactory() != null
				&& server.getCapacityFactory().doubleValue() > 1.0) {
			load = load / server.getCapacityFactory().doubleValue();
		}
		return load;
	}
	
	private List<BigBlueButtonMeetingInfos> getMeetingInfos(BigBlueButtonServer server, BigBlueButtonErrors errors) {
		if(!server.isEnabled()) {
			errors.append(new BigBlueButtonError(BigBlueButtonErrorCodes.serverDisabled));
			return new ArrayList<>();
		}
		
		BigBlueButtonUriBuilder uriBuilder = getUriBuilder(server);
		uriBuilder
			.operation("getMeetings");
		
		Document doc = sendRequest(uriBuilder, errors);
		BigBlueButtonUtils.print(doc);
		if(BigBlueButtonUtils.checkSuccess(doc, errors)) {
			return BigBlueButtonUtils.getMeetings(doc);
		}
		return new ArrayList<>();
	}
	
	private void deleteRecordings(BigBlueButtonMeeting meeting, BigBlueButtonErrors errors) {
		StringBuilder sb = new StringBuilder();
		
		List<BigBlueButtonRecording> recordings = getRecordings(meeting, errors);
		if(recordings != null && !recordings.isEmpty()) {
			for(BigBlueButtonRecording recording:recordings) {
				String recordId = recording.getRecordId();
				if(StringHelper.containsNonWhitespace(recordId)) {
					if(sb.length() > 0) sb.append(",");
					sb.append(recordId);
				}
			}
		}
		
		if(sb.length() > 0) {
			deleteRecording(sb.toString(), meeting.getServer(), errors);
		}
	}
	
	private void deleteRecording(String recordId, BigBlueButtonServer server, BigBlueButtonErrors errors) {
		if(!server.isEnabled()) {
			log.error("Try deleting a recording of a disabled server: {}", server.getUrl());
			errors.append(new BigBlueButtonError(BigBlueButtonErrorCodes.serverDisabled));
			return;
		}
		BigBlueButtonUriBuilder uriBuilder = getUriBuilder(server);
		uriBuilder
			.operation("deleteRecordings")
			.parameter("recordID", recordId);
		
		Document doc = sendRequest(uriBuilder, errors);
		BigBlueButtonUtils.print(doc);
		BigBlueButtonUtils.checkSuccess(doc, errors);
	}
	
	private void removeCalendarEvent(BigBlueButtonMeeting meeting) {
		Kalendar calendar = getCalendar(meeting);
		if(calendar == null) return;
		
		String externalId = generateEventExternalId(meeting);
		List<KalendarEvent> events = calendar.getEvents();
		for(KalendarEvent event:events) {
			if(externalId.equals(event.getExternalId())) {
				calendarManager.removeEventFrom(calendar, event);
			}
		}
	}
	
	private void updateCalendarEvent(BigBlueButtonMeeting meeting) {
		Kalendar calendar = getCalendar(meeting);
		if(calendar == null) return;
		
		CalendarManagedFlag[] managedFlags = { CalendarManagedFlag.all };
		
		String externalId = generateEventExternalId(meeting);
		List<KalendarEvent> events = calendar.getEvents();
		for(KalendarEvent event:events) {
			if(externalId.equals(event.getExternalId())) {
				if(meeting.isPermanent()) {
					calendarManager.removeEventFrom(calendar, event);
				} else {
					event.setSubject(meeting.getName());
					event.setDescription(meeting.getDescription());
					event.setBegin(meeting.getStartDate());
					event.setEnd(meeting.getEndDate());
					event.setManagedFlags(managedFlags);
					calendarManager.updateEventFrom(calendar, event);
				}
				return;
			}
		}
		
		if(!meeting.isPermanent()) {
			String eventId = CodeHelper.getGlobalForeverUniqueID();
			KalendarEvent newEvent = new KalendarEvent(eventId, null, meeting.getName(), meeting.getStartDate(), meeting.getEndDate());
			newEvent.setDescription(meeting.getDescription());
			newEvent.setManagedFlags(managedFlags);
			newEvent.setExternalId(externalId);
			calendarManager.addEventTo(calendar, newEvent);
		}
	}
	
	private String generateEventExternalId(BigBlueButtonMeeting meeting) {
		return "bigbluebutton-".concat(meeting.getMeetingId());
	}
	
	private Kalendar getCalendar(BigBlueButtonMeeting meeting) {
		KalendarRenderWrapper wrapper = null;
		if(meeting.getBusinessGroup() != null) {
			wrapper = calendarManager.getGroupCalendar(meeting.getBusinessGroup());
		} else if(meeting.getEntry() != null) {
			RepositoryEntry entry = repositoryEntryDao.loadByKey(meeting.getEntry().getKey());
			ICourse course = CourseFactory.loadCourse(entry);
			wrapper = calendarManager.getCourseCalendar(course);
		}
		return wrapper == null ? null: wrapper.getKalendar();
	}
	
	@Override
	public List<BigBlueButtonTemplatePermissions> calculatePermissions(RepositoryEntry entry, BusinessGroup businessGroup, Identity identity, Roles userRoles) {
		List<BigBlueButtonTemplatePermissions> permissions = new ArrayList<>();
		if(userRoles.isAdministrator() || userRoles.isSystemAdmin()) {
			permissions.add(BigBlueButtonTemplatePermissions.administrator);
		}
		if(userRoles.isAuthor() || userRoles.isLearnResourceManager()) {
			// global authors / LR-managers can use author templates also in groups
			permissions.add(BigBlueButtonTemplatePermissions.author);
		}
		
		if(businessGroup != null) {
			if(businessGroupService.isIdentityInBusinessGroup(identity, businessGroup)) {
				// all group user can choose the group templates (if they are allowed to create group online-meetings)
				permissions.add(BigBlueButtonTemplatePermissions.group);	
				if(businessGroupService.isIdentityInBusinessGroup(identity, businessGroup.getKey(), true, false, null)) {
					permissions.add(BigBlueButtonTemplatePermissions.coach);
				}
			}
		} else if(entry != null) {
			RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(identity, userRoles, entry);
			if(reSecurity.isEntryAdmin()) {
				permissions.add(BigBlueButtonTemplatePermissions.owner);
			}
			if(reSecurity.isCourseCoach()) {
				permissions.add(BigBlueButtonTemplatePermissions.coach);
			}
		}
		
		return permissions;
	}

	@Override
	public boolean isMeetingRunning(BigBlueButtonMeeting meeting) {
		BigBlueButtonServer server = meeting.getServer();
		if(server == null || !server.isEnabled()) {
			return false;
		}
		
		BigBlueButtonUriBuilder uriBuilder = getUriBuilder(server);
		uriBuilder
			.operation("isMeetingRunning")
			.parameter("meetingID", meeting.getMeetingId());
		
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		Document doc = sendRequest(uriBuilder, errors);
		if(doc == null || errors.hasErrors() || !BigBlueButtonUtils.checkSuccess(doc, errors)) {
			return false;
		}
		String running = BigBlueButtonUtils.getFirstElementValue(doc.getDocumentElement(), "running");
		return "true".equals(running);
	}

	@Override
	public String join(BigBlueButtonMeeting meeting, Identity identity, boolean moderator, boolean guest, Boolean isRunning, BigBlueButtonErrors errors) {
		this.getAvailableServer();
		
		String joinUrl = null;
		if(isRunning != null && isRunning.booleanValue() && meeting.getServer() != null) {
			joinUrl = buildJoinUrl(meeting, meeting.getServer(), identity, moderator, guest);
		} else {
			meeting = getMeetingWithServer(meeting);
			if(createBigBlueButtonMeeting(meeting, errors)) {
				joinUrl = buildJoinUrl(meeting, meeting.getServer(), identity, moderator, guest);
			}
		}
		return joinUrl;
	}
	
	private String buildJoinUrl(BigBlueButtonMeeting meeting, BigBlueButtonServer server, Identity identity, boolean moderator, boolean guest) {
		String password = moderator ? meeting.getModeratorPassword() : meeting.getAttendeePassword();
		
		String userId = null;
		if(!guest) {
			userId = WebappHelper.getInstanceId() + "-" + identity.getKey();
		}

		BigBlueButtonUriBuilder uriBuilder = getUriBuilder(server);
		return uriBuilder
			.operation("join")
			.parameter("meetingID", meeting.getMeetingId())
			.parameter("fullName", getFullName(identity))
			.parameter("password", password)
			.optionalParameter("userID", userId)
			.build()
			.toString();
	}
	
	private String getFullName(Identity identity) {
		StringBuilder sb = new StringBuilder(32);
		User user = identity.getUser();
		if(StringHelper.containsNonWhitespace(user.getFirstName())) {
			sb.append(user.getFirstName());
		}
		if(StringHelper.containsNonWhitespace(user.getLastName())) {
			if(sb.length() > 0) sb.append(" ");
			sb.append(user.getLastName());
		}
		return sb.length() == 0 ? "John Smith" : sb.toString();
	}
	
	private String getBusinessPath(BigBlueButtonMeeting meeting) {
		String businessPath;
		if(meeting.getEntry() != null) {
			businessPath = "[RepositoryEntry:" + meeting.getEntry().getKey() + "]";
			if(StringHelper.containsNonWhitespace(meeting.getSubIdent())) {
				businessPath += "[CourseNode:" + meeting.getSubIdent() + "]";
			}
		} else if(meeting.getBusinessGroup() != null) {
			businessPath = "[BusinessGroup:" + meeting.getBusinessGroup().getKey() + "]";
		} else {
			businessPath = "[RepositoryEntry:0]";
		}
		return BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
	}
	
	private BigBlueButtonMeeting getMeetingWithServer(BigBlueButtonMeeting meeting) {
		meeting = bigBlueButtonMeetingDao.loadByKey(meeting.getKey());
		if(meeting.getServer() != null) {
			return meeting;
		}
		
		BigBlueButtonServer availableServer = getAvailableServer();
		if(availableServer == null) {
			return meeting;
		}
		
		BigBlueButtonMeeting lockedMeeting = bigBlueButtonMeetingDao.loadForUpdate(meeting);
		BigBlueButtonServer currentServer = lockedMeeting.getServer();
		if(currentServer == null) {
			((BigBlueButtonMeetingImpl)lockedMeeting).setServer(availableServer);
			meeting = bigBlueButtonMeetingDao.updateMeeting(lockedMeeting);
			meeting.getServer().getUrl();// ensure server is loaded
		}
		dbInstance.commit();
		
		return meeting;
	}

	private boolean createBigBlueButtonMeeting(BigBlueButtonMeeting meeting, BigBlueButtonErrors errors) {
		BigBlueButtonMeetingTemplate template = meeting.getTemplate();
		BigBlueButtonServer server = meeting.getServer();
		if(!server.isEnabled()) {
			errors.append(new BigBlueButtonError(BigBlueButtonErrorCodes.serverDisabled));
			return false;
		}
		
		BigBlueButtonUriBuilder uriBuilder = getUriBuilder(server);
		uriBuilder
			.operation("create")
			.optionalParameter("name", meeting.getName())
			.parameter("meetingID", meeting.getMeetingId())
			.optionalParameter("welcome", meeting.getWelcome())
			.optionalParameter("attendeePW", meeting.getAttendeePassword())
			.optionalParameter("moderatorPW", meeting.getModeratorPassword())
			.optionalParameter("logoutURL", getBusinessPath(meeting));
		if(meeting.getStartWithLeadTime() != null && meeting.getEndWithFollowupTime() != null) {
			long now = new Date().getTime();
			long start = Math.max(now, meeting.getStartWithLeadTime().getTime());
			long end = meeting.getEndWithFollowupTime().getTime();
			long duration = 1 + (Math.abs(end - start) / (60l * 1000l));// + 1 to compensate rounding error
			uriBuilder.optionalParameter("duration", Long.toString(duration));
		}

		if(template != null) {
			uriBuilder
				.optionalParameter("maxParticipants", template.getMaxParticipants().intValue() + 1)
				.optionalParameter("record", template.getRecord())
				.optionalParameter("breakoutRoomsEnabled", template.getBreakoutRoomsEnabled())
				
				// video options
				.optionalParameter("muteOnStart", template.getMuteOnStart())
				.optionalParameter("autoStartRecording", template.getAutoStartRecording())
				.optionalParameter("allowStartStopRecording", template.getAllowStartStopRecording())
				.optionalParameter("webcamsOnlyForModerator", template.getWebcamsOnlyForModerator())
				.optionalParameter("allowModsToUnmuteUsers", template.getAllowModsToUnmuteUsers())
				// lock settings
				.optionalParameter("lockSettingsDisableCam", template.getLockSettingsDisableCam())
				.optionalParameter("lockSettingsDisableMic", template.getLockSettingsDisableMic())
				.optionalParameter("lockSettingsDisablePrivateChat", template.getLockSettingsDisablePrivateChat())
				.optionalParameter("lockSettingsDisablePublicChat", template.getLockSettingsDisablePublicChat())
				.optionalParameter("lockSettingsDisableNote", template.getLockSettingsDisableNote())
				.optionalParameter("lockSettingsLockedLayout", template.getLockSettingsLockedLayout())
				// lock settings undocumented
				.optionalParameter("lockSettingsHideUserList", template.getLockSettingsHideUserList())
				.optionalParameter("lockSettingsLockOnJoin", template.getLockSettingsLockOnJoin())
				.optionalParameter("lockSettingsLockOnJoinConfigurable", template.getLockSettingsLockOnJoinConfigurable())
				// guest policy
				.optionalParameter("guestPolicy", GuestPolicyEnum.ALWAYS_ACCEPT.name());
		}
		
		Document doc = sendRequest(uriBuilder, errors);
		return BigBlueButtonUtils.checkSuccess(doc, errors);
	}

	@Override
	public List<BigBlueButtonRecording> getRecordings(BigBlueButtonMeeting meeting, BigBlueButtonErrors errors) {
		if(meeting.getServer() == null || !meeting.getServer().isEnabled()) {
			return new ArrayList<>();
		}
		
		BigBlueButtonUriBuilder uriBuilder = getUriBuilder(meeting.getServer());
		uriBuilder
			.operation("getRecordings")
			.parameter("meetingID", meeting.getMeetingId());
		
		Document doc = sendRequest(uriBuilder, errors);
		if(BigBlueButtonUtils.checkSuccess(doc, errors)) {
			return BigBlueButtonUtils.getRecordings(doc);
		}
		return Collections.emptyList();
	}
	
	@Override
	public boolean checkConnection(String url, String sharedSecret, BigBlueButtonErrors errors) {
		BigBlueButtonUriBuilder uriBuilder = BigBlueButtonUriBuilder.fromUri(URI.create(url), sharedSecret);
		uriBuilder.operation("getMeetings");
		Document doc = sendRequest(uriBuilder, errors);
		if(doc != null) {
			return BigBlueButtonUtils.checkSuccess(doc, errors);
		}
		return false;
	}
	
	private BigBlueButtonUriBuilder getUriBuilder(BigBlueButtonServer server) {
		URI uri = URI.create(server.getUrl());
		return BigBlueButtonUriBuilder.fromUri(uri, server.getSharedSecret());	
	}
	
	protected Document sendRequest(BigBlueButtonUriBuilder builder, BigBlueButtonErrors errors) {
		URI uri = builder.build();
		HttpGet get = new HttpGet(uri);
		try(CloseableHttpClient httpClient = HttpClientBuilder.create()
				.disableAutomaticRetries().build();
				CloseableHttpResponse response = httpClient.execute(get)) {
			int statusCode = response.getStatusLine().getStatusCode();
			log.debug("Status code of: {} {}", uri, statusCode);
			return BigBlueButtonUtils.getDocumentFromEntity(response.getEntity());
		} catch(Exception e) {
			errors.append(new BigBlueButtonError(BigBlueButtonErrorCodes.unkown));
			log.error("Cannot send: {}", uri, e);
			return null;
		}
	}
	
	private static class ServerLoadComparator implements Comparator<BigBlueButtonServerInfos> {

		@Override
		public int compare(BigBlueButtonServerInfos o1, BigBlueButtonServerInfos o2) {
			double l1 = o1.getLoad();
			double l2 = o2.getLoad();
			return Double.compare(l1, l2);
		}
	}
	
	private class MeetingInfosThread extends Thread {
		
		private boolean executed = false;
		private final CountDownLatch latch;
		private final BigBlueButtonServer server;
		private List<BigBlueButtonMeetingInfos> infos;
		private final BigBlueButtonErrors errors = new BigBlueButtonErrors();
		
		public MeetingInfosThread(BigBlueButtonServer server, CountDownLatch latch) {
			super("BBB-Meetings-Infos");
			this.latch = latch;
			this.server = server;
			setDaemon(true);
		}
		
		public boolean isExecuted() {
			return executed;
		}
		
		public boolean hasErrors() {
			return errors.hasErrors();
		}
		
		public List<BigBlueButtonMeetingInfos> getMeetingsInfos() {
			return infos;
		}
		
		public BigBlueButtonServer getServer() {
			return server;
		}

		@Override
		public void run() {
			try {
				infos = getMeetingInfos(server, errors);
				executed = true;
			} catch(Exception e) {
				log.error("", e);
			} finally {
				latch.countDown();
			}
		}
	}
}
