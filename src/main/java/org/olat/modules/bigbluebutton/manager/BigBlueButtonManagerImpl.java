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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.Logger;
import org.olat.commons.calendar.CalendarManagedFlag;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.commons.modules.bc.comparators.LastModificationComparator;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.taskexecutor.Task;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSLeafButSystemFilter;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.DeletableGroupData;
import org.olat.modules.bigbluebutton.BigBlueButtonAttendee;
import org.olat.modules.bigbluebutton.BigBlueButtonAttendeeRoles;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingDeletionHandler;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingLayoutEnum;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.BigBlueButtonRecording;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingReference;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingsHandler;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingsPublishedRoles;
import org.olat.modules.bigbluebutton.BigBlueButtonServer;
import org.olat.modules.bigbluebutton.BigBlueButtonTemplatePermissions;
import org.olat.modules.bigbluebutton.GuestPolicyEnum;
import org.olat.modules.bigbluebutton.JoinPolicyEnum;
import org.olat.modules.bigbluebutton.model.BigBlueButtonError;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrorCodes;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingAdminInfos;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingImpl;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingInfos;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingsSearchParameters;
import org.olat.modules.bigbluebutton.model.BigBlueButtonRecordingWithReference;
import org.olat.modules.bigbluebutton.model.BigBlueButtonServerInfos;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryDataDeletable;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.user.UserDataDeletable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;
import org.w3c.dom.Document;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class BigBlueButtonManagerImpl implements BigBlueButtonManager,
	DeletableGroupData, RepositoryEntryDataDeletable, UserDataDeletable, InitializingBean {
	
	private static final Logger log = Tracing.createLoggerFor(BigBlueButtonManagerImpl.class);
	private static final String TASK_MEETING_RESNAME = BigBlueButtonMeeting.class.getSimpleName();

	@Autowired
	private DB dbInstance;
	@Autowired
	private MapperService mapperService;
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private TaskExecutorManager taskManager;
	@Autowired
	private HttpClientService httpClientService;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private BigBlueButtonServerDAO bigBlueButtonServerDao;
	@Autowired
	private BigBlueButtonMeetingDAO bigBlueButtonMeetingDao;
	@Autowired
	private BigBlueButtonAttendeeDAO bigBlueButtonAttendeeDao;
	@Autowired
	private BigBlueButtonMeetingQueries bigBlueButtonMeetingQueries;
	@Autowired
	private BigBlueButtonSlidesStorage bigBlueButtonSlidesStorage;
	@Autowired
	private BigBlueButtonMeetingTemplateDAO bigBlueButtonMeetingTemplateDao;
	@Autowired
	private BigBlueButtonRecordingReferenceDAO bigBlueButtonRecordingReferenceDao;
	@Autowired @Qualifier("native")
	private BigBlueButtonRecordingsHandler defaultRecordingsHandler;
	@Autowired
	private List<BigBlueButtonRecordingsHandler> recordingsHandlers;
	@Autowired
	private List<BigBlueButtonMeetingDeletionHandler> bigBlueButtonMeetingDeletionHandlers;
	
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
				JoinPolicyEnum.disabled, true, templates);
		
		// Traditional classroom setting with many participants. Only presenter has video
		defaultTemplate("sys-classes", "Classroom", 10, 30, 240,
				Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, // recording
				Boolean.TRUE,  Boolean.TRUE, 	// webcams moderator only, unmute
				Boolean.TRUE,  Boolean.FALSE, 	// cam, mic
				Boolean.FALSE, Boolean.FALSE, 	// chat
				Boolean.FALSE, Boolean.FALSE, 	// notes, layout
				Boolean.TRUE, 					// breakout
				Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, // user list, join-lock, lock configurable
				JoinPolicyEnum.disabled, true, templates);
		
		// Mixed setup, some with webcams, some without. 
		defaultTemplate("sys-cafe", "Cafe", 5, 15, 240,
				Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, // recording
				Boolean.FALSE, Boolean.TRUE, 	// webcams moderator only, unmute
				Boolean.TRUE,  Boolean.FALSE, 	// cam, mic
				Boolean.FALSE, Boolean.FALSE, 	// chat
				Boolean.FALSE, Boolean.FALSE, 	// notes, layout
				Boolean.TRUE, 					// breakout
				Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, // user list, join-lock, lock configurable 
				JoinPolicyEnum.disabled, false, templates);
		
		// Interview situation, face-to-face meeting
		defaultTemplate("sys-interview", "Interview", 5, 2, 240,
				Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, // recording
				Boolean.FALSE, Boolean.TRUE, 	// webcams moderator only, unmute
				Boolean.FALSE, Boolean.FALSE, 	// cam, mic
				Boolean.FALSE, Boolean.FALSE, 	// chat
				Boolean.FALSE, Boolean.FALSE, 	// notes, layout
				Boolean.FALSE, 					// breakout
				Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, // user list, join-lock, lock configurable
				JoinPolicyEnum.disabled, false, templates);
		
		// Exam monitoring. Participants have video but only presenter can see the video
		defaultTemplate("sys-assessment", "Assessment", 1, 30, 240,
				Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, // recording
				Boolean.TRUE,  Boolean.TRUE, 	// webcams moderator only, unmute
				Boolean.FALSE, Boolean.TRUE, 	// cam, mic
				Boolean.TRUE,  Boolean.FALSE, 	// chat
				Boolean.TRUE,  Boolean.TRUE, 	// notes, layout
				Boolean.FALSE, 					// breakout
				Boolean.TRUE,  Boolean.TRUE, Boolean.FALSE, // user list, join-lock, lock configurable
				JoinPolicyEnum.disabled, false, templates);

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
			JoinPolicyEnum joinPolicy, boolean enabled, List<BigBlueButtonMeetingTemplate> templates) {
		
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

		template.setJoinPolicyEnum(joinPolicy);
		template.setGuestPolicyEnum(GuestPolicyEnum.ALWAYS_ACCEPT);
		template.setEnabled(enabled);
		bigBlueButtonMeetingTemplateDao.updateTemplate(template);
	}
	
	@Override
	public List<BigBlueButtonRecordingsHandler> getRecordingsHandlers() {
		return recordingsHandlers;
	}

	@Override
	public BigBlueButtonRecordingsHandler getRecordingsHandler() {
		String selectedHandler = bigBlueButtonModule.getRecordingHandlerId();
		for(BigBlueButtonRecordingsHandler recordingHandler:recordingsHandlers) {
			if(selectedHandler.equals(recordingHandler.getId())) {
				return recordingHandler;
			}
		}
		return defaultRecordingsHandler;
	}

	@Override
	public boolean deleteGroupDataFor(BusinessGroup group) {
		List<BigBlueButtonMeeting> meetings = bigBlueButtonMeetingDao.getMeetings(null, null, group, false);
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		for(BigBlueButtonMeeting meeting:meetings) {
			deleteMeeting(meeting, errors);
		}
		return !errors.hasErrors();
	}
	
	@Override
	public boolean deleteRepositoryEntryData(RepositoryEntry re) {
		List<BigBlueButtonMeeting> meetings = bigBlueButtonMeetingDao.getAllResourceMeetings(re, null);
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		for(BigBlueButtonMeeting meeting:meetings) {
			deleteMeeting(meeting, errors);
		}
		return !errors.hasErrors();
	}
	
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		bigBlueButtonAttendeeDao.deleteAttendee(identity);
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
	public BigBlueButtonServer getServer(Long key) {
		return bigBlueButtonServerDao.getServer(key);
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
			instanceInfos.add(new BigBlueButtonServerInfos(info.getServer(), info.isAvailable(), instanceMeetings, load));
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
	public BigBlueButtonMeeting createAndPersistMeeting(String name, RepositoryEntry entry, String subIdent,
			BusinessGroup businessGroup, Identity creator) {
		return bigBlueButtonMeetingDao.createAndPersistMeeting(name, entry, subIdent, businessGroup, creator);
	}
	
	@Override
	public BigBlueButtonMeeting persistMeeting(BigBlueButtonMeeting meeting) {
		return bigBlueButtonMeetingDao.persistMeeting(meeting);
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
		if(meeting == null) return null;
		return bigBlueButtonMeetingDao.loadByKey(meeting.getKey());
	}

	@Override
	public BigBlueButtonMeeting getMeeting(String identifier) {
		if(StringHelper.containsNonWhitespace(identifier)) {
			return bigBlueButtonMeetingDao.loadByIdentifier(identifier);
		}
		return null;
	}

	@Override
	public VFSContainer getSlidesContainer(BigBlueButtonMeeting meeting) {
		VFSContainer container;
		if(StringHelper.containsNonWhitespace(meeting.getDirectory())) {
			container = bigBlueButtonSlidesStorage.getStorage(meeting);
		} else if(meeting.getKey() != null) {
			container = bigBlueButtonSlidesStorage.createStorage(meeting);
		} else {
			container = null;
		}
		return container;
	}

	@Override
	public boolean preloadSlides(Long meetingKey) {
		BigBlueButtonMeeting meeting = bigBlueButtonMeetingDao.loadByKey(meetingKey);
		if(meeting == null) {
			return false;
		}

		boolean loaded = false;
		Date now = new Date();
		Date start = meeting.getStartDate();
		Date startWithLeadingTime = meeting.getStartWithLeadTime();
		if((startWithLeadingTime == null && meeting.isPermanent())
				|| (startWithLeadingTime != null && startWithLeadingTime.compareTo(now) <= 0 && start.compareTo(now) >= 0)) {
			List<VFSLeaf> slides = getSlides(meeting);
			if(!slides.isEmpty()) {
				BigBlueButtonErrors errors = new BigBlueButtonErrors();
				meeting = getMeetingWithServer(meeting);
				createBigBlueButtonMeeting(meeting, errors);
				loaded = !errors.hasErrors();
				log.info(Tracing.M_AUDIT, "Slides preloaded: {}", meeting);
			}
		}
		return loaded;
	}

	@Override
	public boolean isIdentifierInUse(String identifier, BigBlueButtonMeeting reference) {
		if(StringHelper.containsNonWhitespace(identifier)) {
			return bigBlueButtonMeetingDao.isIdentifierInUse(identifier, reference);
		}
		return false;
	}

	@Override
	public BigBlueButtonMeeting updateMeeting(BigBlueButtonMeeting meeting) {
		meeting = bigBlueButtonMeetingDao.updateMeeting(meeting);
		updateCalendarEvent(meeting);
		if(StringHelper.containsNonWhitespace(meeting.getDirectory())) {
			OLATResource resource = resourceManager.findResourceable(meeting.getKey(), TASK_MEETING_RESNAME);
			if(resource == null) {
				OLATResourceable res = OresHelper.createOLATResourceableInstance(TASK_MEETING_RESNAME, meeting.getKey());
				resource = resourceManager.createAndPersistOLATResourceInstance(res);
				SlidesPreloaderTask loader = new SlidesPreloaderTask(meeting.getKey());
				taskManager.execute(loader, null, resource, null, meeting.getStartWithLeadTime());
			} else {
				List<Task> currentTasks = taskManager.getTasks(resource);
				if(!currentTasks.isEmpty()) {
					SlidesPreloaderTask loader = new SlidesPreloaderTask(meeting.getKey());
					taskManager.updateAndReturn(currentTasks.get(0), loader, null, meeting.getStartWithLeadTime());
				}
			}
		}
		return meeting;
	}
	
	@Override
	public BigBlueButtonMeeting copyMeeting(String name, BigBlueButtonMeeting meeting, Identity creator) {
		// move the dates in the future
		Date start = meeting.getStartDate();
		Date end = meeting.getEndDate();
		if(start != null && end != null) {
			Date startWith = meeting.getStartWithLeadTime();
			Date now = new Date();
			if(startWith.before(now)) {
				start = DateUtils.copyTime(now, meeting.getStartDate());
				Date nextStartWith = bigBlueButtonMeetingDao.calculateStartWithLeadTime(start, meeting.getLeadTime() + 5);
				if(nextStartWith.before(now)) {
					start = DateUtils.addDays(start, 1);
				}
				
				long diff = meeting.getEndDate().getTime() - meeting.getStartDate().getTime();
				Calendar cal = Calendar.getInstance();
				cal.setTime(start);
				cal.add(Calendar.MILLISECOND, (int)diff);
				end = cal.getTime();
			}
		}
		
		// copy the meeting with new dates
		return bigBlueButtonMeetingDao.copyMeeting(name, meeting, start, end, creator);
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
	public List<BigBlueButtonMeeting> getMeetings(RepositoryEntryRef entry, String subIdent, BusinessGroup businessGroup, boolean guestOnly) {
		return bigBlueButtonMeetingDao.getMeetings(entry, subIdent, businessGroup, guestOnly);
	}
	
	@Override
	public List<BigBlueButtonMeeting> getUpcomingsMeetings(RepositoryEntryRef entry, String subIdent, int maxResults) {
		return bigBlueButtonMeetingDao.getUpcomingMeetings(entry, subIdent, maxResults);
	}
	
	@Override
	public int countMeetings(BigBlueButtonMeetingsSearchParameters params) {
		return bigBlueButtonMeetingQueries.count(params);
	}

	@Override
	public List<BigBlueButtonMeetingAdminInfos> searchMeetings(BigBlueButtonMeetingsSearchParameters params,
			int firstResult, int maxResults) {
		return bigBlueButtonMeetingQueries.search(params, firstResult, maxResults);
	}

	@Override
	public boolean deleteMeeting(BigBlueButtonMeeting meeting, BigBlueButtonErrors errors) {
		BigBlueButtonMeeting reloadedMeeting = bigBlueButtonMeetingDao.loadByKey(meeting.getKey());
		if(reloadedMeeting != null) {
			bigBlueButtonMeetingDeletionHandlers.forEach(h -> h.onBeforeDelete(reloadedMeeting));
			removeCalendarEvent(reloadedMeeting);
			deleteRecordings(reloadedMeeting, errors);
			deleteSlides(reloadedMeeting);
			// slides VFS operations can close the session -> reload
			BigBlueButtonMeeting finalReloadedMeeting = bigBlueButtonMeetingDao.loadByKey(reloadedMeeting.getKey());
			if(finalReloadedMeeting != null) {
				bigBlueButtonAttendeeDao.deleteAttendee(finalReloadedMeeting);
				bigBlueButtonMeetingDao.deleteMeeting(finalReloadedMeeting);
			}
		}
		return false;
	}
	
	@Override
	public void deleteSlides(BigBlueButtonMeeting meeting) {
		if(StringHelper.containsNonWhitespace(meeting.getDirectory())) {
			VFSContainer slidesContainer = bigBlueButtonSlidesStorage.getStorage(meeting);
			if(slidesContainer != null && slidesContainer.exists()) {
				slidesContainer.deleteSilently();
			}
			
			if(meeting.getKey() != null) {
				OLATResource resource = resourceManager.findResourceable(meeting.getKey(), TASK_MEETING_RESNAME);
				if(resource != null) {
					taskManager.delete(resource);
					resourceManager.deleteOLATResource(resource);
				}
			}
		}
	}
	
	/**
	 * Returns an enabled, not selection manual only server based on the
	 * load of the pool of available servers.
	 * 
	 * @return A BigBlueButton server or null
	 */
	private BigBlueButtonServer getLoadBalancedServer() {
		List<BigBlueButtonServer> servers = getServers();
		List<BigBlueButtonServer> availableServers = servers.stream()
				.filter(BigBlueButtonServer::isEnabled)
				.filter(s -> !s.isManualOnly())
				.collect(Collectors.toList());
		if(availableServers.isEmpty()) {
			return null;
		}
		if(availableServers.size() == 1) {
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
		
		long load = Math.round(serversInfos.get(0).getLoad());
		List<BigBlueButtonServerInfos> sameLoadsServer = new ArrayList<>();
		for(BigBlueButtonServerInfos serverInfos : serversInfos) {
			if(Math.round(serverInfos.getLoad()) == load) {
				sameLoadsServer.add(serverInfos);
			}
		}
		
		if(sameLoadsServer.isEmpty()) {
			return serversInfos.get(0).getServer();
		} else if(sameLoadsServer.size() == 1) {
			return sameLoadsServer.get(0).getServer();
		}
		
		Collections.shuffle(sameLoadsServer);
		return sameLoadsServer.get(0).getServer();
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
			if(!serverLatch.await(15, TimeUnit.SECONDS)) {
				log.warn("Request to get infos from BigBlueButton server take more than 15 seconds.");
			}
		} catch (InterruptedException e) {
			log.error("", e);
		}

		return threads.stream()
			.filter(MeetingInfosThread::isExecuted)
			.filter(thread -> !thread.hasErrors())
			.map(thread -> new BigBlueButtonServerInfos(thread.getServer(), thread.isExecuted(), thread.getMeetingsInfos(),
					calculateLoad(thread.getServer(), thread.getMeetingsInfos())))
			.collect(Collectors.toList());
	}
	
	private double calculateLoad(BigBlueButtonServer server, List<BigBlueButtonMeetingInfos> meetingsInfos) {
		double load = 0.0d;
		for(BigBlueButtonMeetingInfos meetingInfos:meetingsInfos) {
			load += calculateMeetingLoad(meetingInfos);
		}
		if(load > 0.0d
				&& server.getCapacityFactory() != null
				&& server.getCapacityFactory().doubleValue() > 1.0) {
			load = load / server.getCapacityFactory().doubleValue();
		}
		return load;
	}
	
	/**
	 * 
	 * @param meetingInfos The meeting to calculate the load
	 * @return The load
	 */
	private double calculateMeetingLoad(BigBlueButtonMeetingInfos meetingInfos) {
		double load = 0.0d;
		Date now = new Date();
		
		Date startTime = meetingInfos.getStartTime();
		// We assume that after 10 minutes that all users are in the room,
		// before we take the max. users to better evaluate the load in the future 
		if(!meetingInfos.isBreakout() && (startTime != null && (now.getTime() - startTime.getTime()) < 10 * 60 * 1000)) {
			long maxUsers = meetingInfos.getMaxUsers();
			if(maxUsers == 0) {
				maxUsers = 1;// mathematical paranoia
			}
			load += maxUsers * 1.0d;
			// count at least 3
			long videoCount = Math.max(meetingInfos.getVideoCount(), 3);
			load += videoCount * 3.0d;
			long voiceCount = Math.max(meetingInfos.getVoiceParticipantCount(), (maxUsers / 4));
			load += voiceCount * 2.0d;
		} else {
			load += meetingInfos.getListenerCount() * 1.0d;
			load += meetingInfos.getVideoCount() * 3.0d;
			load += meetingInfos.getVoiceParticipantCount() * 2.0d;
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
		List<BigBlueButtonRecordingWithReference> recordingsAndRefs = getRecordingAndReferences(meeting, errors);
		if(recordingsAndRefs != null && !recordingsAndRefs.isEmpty()) {
			BigBlueButtonRecordingsHandler recordingsHanlder = getRecordingsHandler();
			boolean defaultPermanent = recordingsHanlder.allowPermanentRecordings()
					&& bigBlueButtonModule.isRecordingsPermanent();
			
			List<BigBlueButtonRecording> recordings = recordingsAndRefs.stream()
					.filter(r -> (r.getReference().getPermanent() == null && !defaultPermanent)
							|| (r.getReference().getPermanent() != null && !r.getReference().getPermanent().booleanValue()))
					.map(BigBlueButtonRecordingWithReference::getRecording)
					.collect(Collectors.toList());
			getRecordingsHandler().deleteRecordings(recordings, meeting, errors);
		}
		bigBlueButtonRecordingReferenceDao.deleteRecordingReferences(meeting);
	}
	
	@Override
	public void deleteRecording(BigBlueButtonRecording recording, BigBlueButtonMeeting meeting, BigBlueButtonErrors errors) {
		getRecordingsHandler().deleteRecordings(Collections.singletonList(recording), meeting, errors);
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
					if(event.getKalendarEventLinks() == null || event.getKalendarEventLinks().isEmpty()) {
						KalendarEventLink eventLink = generateEventLink(meeting);
						if(eventLink != null) {
							List<KalendarEventLink> kalendarEventLinks = new ArrayList<>();
							kalendarEventLinks.add(eventLink);
							event.setKalendarEventLinks(kalendarEventLinks);
						}
					}
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
			KalendarEventLink eventLink = generateEventLink(meeting);
			if(eventLink != null) {
				List<KalendarEventLink> kalendarEventLinks = new ArrayList<>();
				kalendarEventLinks.add(eventLink);
				newEvent.setKalendarEventLinks(kalendarEventLinks);
			}
			calendarManager.addEventTo(calendar, newEvent);
		}
	}
	
	private String generateEventExternalId(BigBlueButtonMeeting meeting) {
		return "bigbluebutton-".concat(meeting.getMeetingId());
	}
	
	private KalendarEventLink generateEventLink(BigBlueButtonMeeting meeting) {
		String id = meeting.getKey().toString();
		String displayName = meeting.getName();
		if(meeting.getEntry() != null) {
			StringBuilder businessPath = new StringBuilder(128);
			businessPath.append("[RepositoryEntry:").append(meeting.getEntry().getKey()).append("]");
			if(StringHelper.containsNonWhitespace(meeting.getSubIdent())) {
				businessPath.append("[CourseNode:").append(meeting.getSubIdent()).append("]");
			}
			businessPath.append("[Meeting:").append(meeting.getKey()).append("]");
			String url = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath.toString());
			return new KalendarEventLink("bigbluebutton", id, displayName, url, "o_CourseModule_icon");
		} else if(meeting.getBusinessGroup() != null) {
			StringBuilder businessPath = new StringBuilder(128);
			businessPath.append("[BusinessGroup:").append(meeting.getBusinessGroup().getKey())
				.append("][toolbigbluebutton:0][Meeting:").append(meeting.getKey()).append("]");
			String url = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath.toString());
			return new KalendarEventLink("bigbluebutton", id, displayName, url, "o_icon_group");
		}
		return null;
	}
	
	private Kalendar getCalendar(BigBlueButtonMeeting meeting) {
		KalendarRenderWrapper wrapper = null;
		if(meeting.getBusinessGroup() != null) {
			wrapper = calendarManager.getGroupCalendar(meeting.getBusinessGroup());
		} else if(meeting.getEntry() != null) {
			try {
				RepositoryEntry entry = repositoryEntryDao.loadByKey(meeting.getEntry().getKey());
				ICourse course = CourseFactory.loadCourse(entry);
				wrapper = calendarManager.getCourseCalendar(course);
			} catch (Exception e) {
				log.error("", e);
				return null;
			}
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
	public void syncReferences(Date endFrom, Date endTo, boolean syncPermanent) {
		Set<BigBlueButtonMeeting> meetings = new HashSet<>();
		if (endFrom != null && endTo != null) {
			List<BigBlueButtonMeeting> finiteMeetings = bigBlueButtonMeetingDao.loadMeetingsByEnd(endFrom, endTo);
			meetings.addAll(finiteMeetings);
		}
		if (syncPermanent) {
			List<BigBlueButtonMeeting> permanantMeetings = bigBlueButtonMeetingDao.loadPermanentMeetings();
			meetings.addAll(permanantMeetings);
		}
		
		BigBlueButtonErrors error = new BigBlueButtonErrors();
		for (BigBlueButtonMeeting meeting : meetings) {
			getRecordingAndReferences(meeting, error);
			if (log.isDebugEnabled()) {
				log.debug(error.hasErrors()? error.getErrorMessages(): "Reference sync successfull");
			}
		}
	}

	@Override
	public String join(BigBlueButtonMeeting meeting, Identity identity, String pseudo, String avatarUrl,
			BigBlueButtonAttendeeRoles role, Boolean isRunning, BigBlueButtonErrors errors) {
		String joinUrl = null;
		boolean moderator = role == BigBlueButtonAttendeeRoles.moderator;
		boolean guest = (role == BigBlueButtonAttendeeRoles.guest || role == BigBlueButtonAttendeeRoles.external);
		
		if(isRunning != null && isRunning.booleanValue() && meeting.getServer() != null) {
			joinUrl = buildJoinUrl(meeting, meeting.getServer(), identity, pseudo, avatarUrl, moderator, guest);
		} else {
			meeting = getMeetingWithServer(meeting);
			if(createBigBlueButtonMeeting(meeting, errors)) {
				joinUrl = buildJoinUrl(meeting, meeting.getServer(), identity, pseudo, avatarUrl, moderator, guest);
			}
		}
		if(StringHelper.containsNonWhitespace(joinUrl)) {
			if((role == BigBlueButtonAttendeeRoles.moderator || role == BigBlueButtonAttendeeRoles.viewer)
					&& !bigBlueButtonAttendeeDao.hasAttendee(identity, meeting)) {
				bigBlueButtonAttendeeDao.createAttendee(identity, null, role, new Date(), meeting);
			} else if((role == BigBlueButtonAttendeeRoles.guest || role == BigBlueButtonAttendeeRoles.external)
					&& !bigBlueButtonAttendeeDao.hasAttendee(pseudo, meeting)) {
				bigBlueButtonAttendeeDao.createAttendee(null, pseudo, role, new Date(), meeting);
			}
		}
		return joinUrl;
	}
	
	private String buildJoinUrl(BigBlueButtonMeeting meeting, BigBlueButtonServer server, Identity identity,
			String pseudo, String avatarUrl, boolean moderator, boolean guest) {
		String password = moderator ? meeting.getModeratorPassword() : meeting.getAttendeePassword();
		
		String userId = null;
		if(!guest && identity != null) {
			userId = WebappHelper.getInstanceId() + "-" + identity.getName();
		}

		BigBlueButtonUriBuilder uriBuilder = getUriBuilder(server);
		uriBuilder
			.operation("join")
			.parameter("meetingID", meeting.getMeetingId())
			.parameter("fullName", getFullName(identity, pseudo))
			.parameter("password", password)
			.optionalParameter("userID", userId)
			.optionalParameter("avatarURL", avatarUrl);
		
		boolean guestFlag;
		if(moderator) {
			guestFlag = false;
		} else if(JoinPolicyEnum.guestsApproval.equals(meeting.getJoinPolicyEnum()) && guest) {
			guestFlag = true;
		} else if(JoinPolicyEnum.allUsersApproval.equals(meeting.getJoinPolicyEnum())) {
			guestFlag = true;
		} else {
			guestFlag = false;
		}
		uriBuilder.parameter("guest", Boolean.toString(guestFlag));
		
		if(BigBlueButtonMeetingLayoutEnum.webcam.equals(meeting.getMeetingLayout())) {
			uriBuilder
				.optionalParameter("userdata-bbb_auto_swap_layout", "true")
				.optionalParameter("userdata-bbb_auto_share_webcam", "true")
				.optionalParameter("userdata-bbb_show_participants_on_login", "false");
		}
		return uriBuilder
			.build()
			.toString();
	}
	
	private String getFullName(Identity identity, String pseudo) {
		StringBuilder sb = new StringBuilder(32);
		if(StringHelper.containsNonWhitespace(pseudo)) {
			sb.append(pseudo).append(" (guest)");
		} else {
			User user = identity.getUser();
			if(StringHelper.containsNonWhitespace(user.getFirstName())) {
				sb.append(user.getFirstName());
			}
			if(StringHelper.containsNonWhitespace(user.getLastName())) {
				if(sb.length() > 0) sb.append(" ");
				sb.append(user.getLastName());
			}
		}
		return sb.length() == 0 ? "John Smith" : sb.toString();
	}
	
	private String getBusinessPath(BigBlueButtonMeeting meeting) {
		String businessPath;
		if(meeting.getEntry() != null) {
			businessPath = "[RepositoryEntry:" + meeting.getEntry().getKey() + "]";
			String subIdent = meeting.getSubIdent();
			if(StringHelper.containsNonWhitespace(subIdent)) {
				int firstIndex = subIdent.indexOf('-');
				if(firstIndex >= 0) {
					subIdent = subIdent.substring(0, firstIndex);
				}
				if(StringHelper.isLong(subIdent)) {
					businessPath += "[CourseNode:" + subIdent + "]";
				}
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
		
		BigBlueButtonServer availableServer = getLoadBalancedServer();
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
		if(server == null || !server.isEnabled()) {
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
		
		GuestPolicyEnum guestPolicy;
		if(JoinPolicyEnum.disabled.equals(meeting.getJoinPolicyEnum())) {
			guestPolicy = GuestPolicyEnum.ALWAYS_ACCEPT;
		} else if(JoinPolicyEnum.guestsApproval.equals(meeting.getJoinPolicyEnum())	
				|| JoinPolicyEnum.allUsersApproval.equals(meeting.getJoinPolicyEnum())) {
			guestPolicy = GuestPolicyEnum.ASK_MODERATOR;
		} else {
			guestPolicy = GuestPolicyEnum.ALWAYS_ACCEPT;
		}
		uriBuilder
			.optionalParameter("guestPolicy", guestPolicy.name());

		if(template != null) {
			boolean record = template.getRecord() != null && template.getRecord().booleanValue();
			if(record && meeting.getRecord() != null) {
				record = meeting.getRecord().booleanValue();
			}

			uriBuilder
				.optionalParameter("maxParticipants", template.getMaxParticipants().intValue() + 1)
				.optionalParameter("record", record)
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
				.optionalParameter("lockSettingsLockOnJoinConfigurable", template.getLockSettingsLockOnJoinConfigurable());
		}
		
		// metadata
		getRecordingsHandler().appendMetadata(uriBuilder, meeting);
		
		// slides
		if(StringHelper.containsNonWhitespace(meeting.getDirectory())) {
			VFSContainer slidesContainer = bigBlueButtonSlidesStorage.getStorage(meeting);
			List<VFSLeaf> slides = getSlides(meeting);
			if(!slides.isEmpty()) {
				MapperKey mapperKey = mapperService.register(null,  meeting.getMeetingId(), new SlidesContainerMapper(slidesContainer), 360);
				String url = Settings.createServerURI() + mapperKey.getUrl() + "/slides/";
				Collections.sort(slides, new LastModificationComparator());
				String slidesXml = slidesDocument(url, slides);
				uriBuilder.xmlPayload(slidesXml);
			}
		}

		Document doc = sendRequest(uriBuilder, errors);
		return BigBlueButtonUtils.checkSuccess(doc, errors);
	}
	
	private String slidesDocument(String url, List<VFSLeaf> slides) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("<?xml version='1.0' encoding='UTF-8'?>")
		  .append("<modules><module name='presentation'>");
		
		for(VFSLeaf slide:slides) {
			String encodedFilename = UriUtils.encodePath(slide.getName(), StandardCharsets.UTF_8);
			sb.append("<document url='").append(url).append(encodedFilename).append("' filename='").append(slide.getName()).append("' />");
		}
		sb.append("</module></modules>");
		return sb.toString();
	}
	
	private List<VFSLeaf> getSlides(BigBlueButtonMeeting meeting) {
		List<VFSLeaf> slides = new ArrayList<>();
		
		if(StringHelper.containsNonWhitespace(meeting.getDirectory())) {
			VFSContainer slidesContainer = bigBlueButtonSlidesStorage.getStorage(meeting);
			if(slidesContainer != null && slidesContainer.exists()) {
				List<VFSItem> items = slidesContainer.getItems(new VFSLeafButSystemFilter());
				for(VFSItem item:items) {
					if(item instanceof VFSLeaf) {
						slides.add((VFSLeaf)item);
					}
				}
			}
		}
		
		return slides;
	}
	
	@Override
	public BigBlueButtonAttendee getAttendee(Identity identity, BigBlueButtonMeeting meeting) {
		return bigBlueButtonAttendeeDao.getAttendee(identity, meeting);
	}
	
	@Override
	public String getRecordingUrl(UserSession usess, BigBlueButtonRecording recording) {
		return getRecordingsHandler().getRecordingURL(usess, recording);
	}

	@Override
	public List<BigBlueButtonRecordingWithReference> getRecordingAndReferences(BigBlueButtonMeeting meeting, BigBlueButtonErrors errors) {
		List<BigBlueButtonRecording> recordings = getRecordingsHandler().getRecordings(meeting, errors);
		List<BigBlueButtonRecordingReference> references = bigBlueButtonRecordingReferenceDao.getRecordingReferences(meeting);
		Map<String,BigBlueButtonRecordingReference> recordIdToReferences = references.stream()
				.collect(Collectors.toMap(BigBlueButtonRecordingReference::getRecordingId, u -> u, (u, v) -> u));

		List<BigBlueButtonRecordingWithReference> withReferences = new ArrayList<>(recordings.size());
		for(BigBlueButtonRecording recording:recordings) {
			BigBlueButtonRecordingReference reference = recordIdToReferences.get(recording.getRecordId());
			if(reference == null) {
				reference = syncReference(recording, meeting);
				if(reference == null) {
					continue;
				}
				recordIdToReferences.put(recording.getRecordId(), reference);
			}
			withReferences.add(new BigBlueButtonRecordingWithReference(recording, reference));
		}
		return withReferences;
	}
	
	private BigBlueButtonRecordingReference syncReference(BigBlueButtonRecording recording, BigBlueButtonMeeting meeting) {
		try {
			BigBlueButtonRecordingsPublishedRoles[] roles = meeting.getRecordingsPublishingEnum(); 
			if(roles == null || roles.length == 0 || roles[0] == null) {
				roles = new BigBlueButtonRecordingsPublishedRoles[] { BigBlueButtonRecordingsPublishedRoles.none };
			}
			BigBlueButtonRecordingReference reference = bigBlueButtonRecordingReferenceDao
					.createReference(recording, meeting, roles);
			dbInstance.commit();
			return reference;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	@Override
	public List<BigBlueButtonRecordingReference> getRecordingReferences(Collection<BigBlueButtonMeeting> meetings) {
		return bigBlueButtonRecordingReferenceDao.getRecordingReferences(meetings);
	}
	
	@Override
	public BigBlueButtonRecordingReference getRecordingReference(BigBlueButtonRecordingReference reference) {
		if(reference == null || reference.getKey() == null) {
			return null;
		}
		return bigBlueButtonRecordingReferenceDao.loadRecordingReferenceByKey(reference.getKey());
	}

	@Override
	public BigBlueButtonRecordingReference updateRecordingReference(BigBlueButtonRecordingReference reference) {
		return bigBlueButtonRecordingReferenceDao.updateRecordingReference(reference);
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

	@Override
	public BigBlueButtonUriBuilder getUriBuilder(BigBlueButtonServer server) {
		URI uri = URI.create(server.getUrl());
		return BigBlueButtonUriBuilder.fromUri(uri, server.getSharedSecret());	
	}
	
	@Override
	public Document sendRequest(BigBlueButtonUriBuilder builder, BigBlueButtonErrors errors) {
		dbInstance.commit();
		if(StringHelper.containsNonWhitespace(builder.getXmlPayload())) {
			return sendPostRequest(builder, errors);
		}
		return sendGetRequest(builder, errors);
	}

	private Document sendPostRequest(BigBlueButtonUriBuilder builder, BigBlueButtonErrors errors) {
		URI uri = builder.build();
		HttpPost post = new HttpPost(uri);
		String payload = builder.getXmlPayload();
		post.addHeader("Content-Language", "en-US");
		ContentType cType = ContentType.create("text/xml", StandardCharsets.UTF_8);
		HttpEntity myEntity = new StringEntity(payload, cType);
		post.setEntity(myEntity);
		
		try(CloseableHttpClient httpClient = httpClientService.createHttpClientBuilder()
				.disableAutomaticRetries()
				.build();
				CloseableHttpResponse response = httpClient.execute(post)) {
			int statusCode = response.getStatusLine().getStatusCode();
			log.debug("Status code of: {} {}", uri, statusCode);
			return BigBlueButtonUtils.getDocumentFromEntity(response.getEntity());
		} catch(Exception e) {
			errors.append(new BigBlueButtonError(BigBlueButtonErrorCodes.unkown));
			log.error("Cannot send: {}", uri, e);
			return null;
		}
	}
	
	private Document sendGetRequest(BigBlueButtonUriBuilder builder, BigBlueButtonErrors errors) {
		URI uri = builder.build();
		HttpGet get = new HttpGet(uri);
		try(CloseableHttpClient httpClient = httpClientService.createHttpClientBuilder()
				.disableAutomaticRetries()
				.build();
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
