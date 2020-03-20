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
import java.util.List;

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
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.GuestPolicyEnum;
import org.olat.modules.bigbluebutton.model.BigBlueButtonError;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrorCodes;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryDAO;
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
	private CalendarManager calendarManager;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private BigBlueButtonMeetingDAO bigBlueButtonMeetingDao;
	@Autowired
	private BigBlueButtonMeetingTemplateDAO bigBlueButtonMeetingTemplateDao;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		List<BigBlueButtonMeetingTemplate> templates = bigBlueButtonMeetingTemplateDao.getTemplates();
		
		// Web conferen
		defaultTemplate("web-conference", "Web conference", 100,
				Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, // recording
				Boolean.TRUE, Boolean.TRUE, // webcams, unmute
				Boolean.TRUE, Boolean.TRUE, // cam, mic
				Boolean.FALSE, Boolean.TRUE, // chat
				Boolean.FALSE, Boolean.FALSE, // node, layout
				GuestPolicyEnum.ALWAYS_ACCEPT, templates);
		
		defaultTemplate("web-classe", "Classes / Klasse", 25,
				Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, // recording
				Boolean.FALSE, Boolean.TRUE, // webcams, unmute
				Boolean.FALSE, Boolean.FALSE, // cam, mic
				Boolean.FALSE, Boolean.FALSE, // chat
				Boolean.FALSE, Boolean.FALSE, // node, layout
				GuestPolicyEnum.ALWAYS_DENY, templates);
		
		defaultTemplate("web-one-to-one", "One to one", 2,
				Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, // recording
				Boolean.FALSE, Boolean.TRUE, // webcams, unmute
				Boolean.FALSE, Boolean.FALSE, // cam, mic
				Boolean.TRUE, Boolean.FALSE, // chat
				Boolean.FALSE, Boolean.FALSE, // node, layout
				GuestPolicyEnum.ALWAYS_DENY, templates);
	}
	
	private void defaultTemplate(String externalId, String name, Integer maxParticipants,
			Boolean muteOnStart, Boolean autoStartRecording, Boolean allowStartStopRecording,
			Boolean webcamsOnlyForModerator, Boolean allowModsToUnmuteUsers,
			Boolean lockSettingsDisableCam, Boolean lockSettingsDisableMic,
			Boolean lockSettingsDisablePrivateChat, Boolean lockSettingsDisablePublicChat,
			Boolean lockSettingsDisableNote, Boolean lockSettingsLockedLayout,
			GuestPolicyEnum guestPolicy, List<BigBlueButtonMeetingTemplate> templates) {
		
		BigBlueButtonMeetingTemplate template = templates.stream()
				.filter(tpl -> externalId.equals(tpl.getExternalId()))
				.findFirst().orElse(null);
		if(template == null) {
			template = bigBlueButtonMeetingTemplateDao.createTemplate(name, externalId, true);
		}
		template.setMaxParticipants(maxParticipants);
		template.setMuteOnStart(muteOnStart);
		template.setAutoStartRecording(autoStartRecording);
		template.setAllowStartStopRecording(allowStartStopRecording);
		template.setWebcamsOnlyForModerator(webcamsOnlyForModerator);
		template.setAllowModsToUnmuteUsers(allowModsToUnmuteUsers);
		template.setLockSettingsDisableCam(lockSettingsDisableCam);
		template.setLockSettingsDisableMic(lockSettingsDisableMic);
		template.setLockSettingsDisablePrivateChat(lockSettingsDisablePrivateChat);
		template.setLockSettingsDisablePublicChat(lockSettingsDisablePublicChat);
		template.setLockSettingsDisableNote(lockSettingsDisableNote);
		template.setLockSettingsLockedLayout(lockSettingsLockedLayout);
		template.setGuestPolicyEnum(guestPolicy);
		bigBlueButtonMeetingTemplateDao.updateTemplate(template);
	}

	@Override
	public BigBlueButtonMeeting createAndPersistMeeting(String name, RepositoryEntry entry, String subIdent, BusinessGroup businessGroup) {
		return bigBlueButtonMeetingDao.createAndPersistMeeting(name, entry, subIdent, businessGroup);
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
	public List<BigBlueButtonMeeting> getAllMeetings() {
		return bigBlueButtonMeetingDao.getAllMeetings();
	}

	@Override
	public List<BigBlueButtonMeeting> getMeetings(RepositoryEntry entry, String subIdent, BusinessGroup businessGroup) {
		return bigBlueButtonMeetingDao.getMeetings(entry, subIdent, businessGroup);
	}
	
	@Override
	public boolean deleteMeeting(BigBlueButtonMeeting meeting, BigBlueButtonErrors errors) {
		BigBlueButtonMeeting reloadedMeeting = bigBlueButtonMeetingDao.loadByKey(meeting.getKey());
		removeCalendarEvent(reloadedMeeting);
		bigBlueButtonMeetingDao.deleteMeeting(reloadedMeeting);
		return false;
	}
	
	private void removeCalendarEvent(BigBlueButtonMeeting meeting) {
		Kalendar calendar = getCalendar(meeting);
		if(calendar == null) return;
		
		String externalId = generateEventExternalId(meeting);
		List<KalendarEvent> events = calendar.getEvents();
		for(KalendarEvent event:events) {
			if(meeting.getMeetingId().equals(externalId)) {
				calendarManager.removeEventFrom(calendar, event);
			}
		}
	}
	
	private void updateCalendarEvent(BigBlueButtonMeeting meeting) {
		Kalendar calendar = getCalendar(meeting);
		if(calendar == null) return;
		
		String externalId = generateEventExternalId(meeting);
		List<KalendarEvent> events = calendar.getEvents();
		for(KalendarEvent event:events) {
			if(event.getExternalId().equals(externalId)) {
				if(meeting.isPermanent()) {
					calendarManager.removeEventFrom(calendar, event);
				} else {
					event.setBegin(meeting.getStartDate());
					event.setEnd(meeting.getEndDate());
					calendarManager.updateEventFrom(calendar, event);
				}
				return;
			}
		}
		
		if(!meeting.isPermanent()) {
			String eventId = CodeHelper.getGlobalForeverUniqueID();
			KalendarEvent newEvent = new KalendarEvent(eventId, null, meeting.getName(), meeting.getStartDate(), meeting.getEndDate());
			newEvent.setDescription(meeting.getDescription());
			CalendarManagedFlag[] managedFlags = {
					CalendarManagedFlag.all
			};
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
	public boolean isMeetingRunning(BigBlueButtonMeeting meeting) {
		BigBlueButtonUriBuilder uriBuilder = getUriBuilder();
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
	public String join(BigBlueButtonMeeting meeting, Identity identity, boolean moderator, boolean guest, BigBlueButtonErrors errors) {
		String joinUrl = null;
		if(createBigBlueButtonMeeting(meeting, errors)) {
			joinUrl = buildJoinUrl(meeting, identity, moderator, guest);
		}
		return joinUrl;
	}
	
	private String buildJoinUrl(BigBlueButtonMeeting meeting, Identity identity, boolean moderator, boolean guest) {
		String password = moderator ? meeting.getModeratorPassword() : meeting.getAttendeePassword();
		
		String userId = null;
		if(!guest) {
			userId = WebappHelper.getInstanceId() + "-" + identity.getKey();
		}

		BigBlueButtonUriBuilder uriBuilder = getUriBuilder();
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
				businessPath = "[CourseNode:" + meeting.getSubIdent() + "]";
			}
		} else if(meeting.getBusinessGroup() != null) {

			businessPath = "[BusinessGroup:" + meeting.getBusinessGroup().getKey() + "]";
		} else {
			businessPath = "[RepositoryEntry:0]";
		}
		return BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
	}

	private boolean createBigBlueButtonMeeting(BigBlueButtonMeeting meeting, BigBlueButtonErrors errors) {
		BigBlueButtonMeetingTemplate template = meeting.getTemplate();
		
		BigBlueButtonUriBuilder uriBuilder = getUriBuilder();
		uriBuilder
			.operation("create")
			.optionalParameter("name", meeting.getName())
			.parameter("meetingID", meeting.getMeetingId())
			.optionalParameter("welcome", meeting.getWelcome())
			.optionalParameter("attendeePW", meeting.getAttendeePassword())
			.optionalParameter("moderatorPW", meeting.getModeratorPassword())
			.optionalParameter("logoutURL", getBusinessPath(meeting));
		if(meeting.getStartWithLeadTime() != null && meeting.getEndWithFollowupTime() != null) {
			long start = meeting.getStartWithLeadTime().getTime();
			long end = meeting.getEndWithFollowupTime().getTime();
			long duration = (end - start) / (60l * 1000l);
			uriBuilder.optionalParameter("duration", Long.toString(duration + 1));// + 1 for rounding error
		}

		if(template != null) {
			uriBuilder
				.optionalParameter("maxParticipants", template.getMaxParticipants())
				.optionalParameter("record", (String)null)
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
				// guest policy
				.optionalParameter("guestPolicy", template.getGuestPolicyEnum().name());
		}
		
		Document doc = sendRequest(uriBuilder, errors);
		return BigBlueButtonUtils.checkSuccess(doc, errors);
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
	
	private BigBlueButtonUriBuilder getUriBuilder() {
		return BigBlueButtonUriBuilder.fromUri(bigBlueButtonModule.getBigBlueButtonURI(), bigBlueButtonModule.getSharedSecret());	
	}
	
	protected Document sendRequest(BigBlueButtonUriBuilder builder, BigBlueButtonErrors errors) {
		URI uri = builder.build();
		HttpGet get = new HttpGet(uri);
		try(CloseableHttpClient httpClient = HttpClientBuilder.create().build();
				CloseableHttpResponse response = httpClient.execute(get)) {
			int statusCode = response.getStatusLine().getStatusCode();
			log.debug("Status code of: {} {}", uri, statusCode);
			return BigBlueButtonUtils.getDocumentFromEntity(response.getEntity());
		} catch(Exception e) {
			errors.append(new BigBlueButtonError(BigBlueButtonErrorCodes.unkown));
			log.error("", e);
			return null;
		}
	}
}
