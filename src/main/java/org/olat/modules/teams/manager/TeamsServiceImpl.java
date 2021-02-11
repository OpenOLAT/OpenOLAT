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
package org.olat.modules.teams.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.commons.calendar.CalendarManagedFlag;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;
import org.olat.login.oauth.spi.MicrosoftAzureADFSProvider;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsMeetingDeletionHandler;
import org.olat.modules.teams.TeamsService;
import org.olat.modules.teams.TeamsUser;
import org.olat.modules.teams.model.ConnectionInfos;
import org.olat.modules.teams.model.TeamsError;
import org.olat.modules.teams.model.TeamsErrorCodes;
import org.olat.modules.teams.model.TeamsErrors;
import org.olat.modules.teams.model.TeamsMeetingImpl;
import org.olat.modules.teams.model.TeamsMeetingsSearchParameters;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.extensions.OnlineMeeting;
import com.microsoft.graph.models.extensions.User;
import com.microsoft.graph.models.generated.OnlineMeetingRole;

/**
 * 
 * Initial date: 20 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TeamsServiceImpl implements TeamsService {
	
	private static final Logger log = Tracing.createLoggerFor(TeamsServiceImpl.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private TeamsUserDAO teamsUserDao;
	@Autowired
	private MicrosoftGraphDAO graphDao;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private TeamsMeetingDAO teamsMeetingDao;
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private TeamsAttendeeDAO teamsAttendeeDao;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private TeamsMeetingQueries teamsMeetingQueries;
	@Autowired
	private List<TeamsMeetingDeletionHandler> teamsMeetingDeletionHandlers;

	@Override
	public TeamsMeeting createMeeting(String subject, Date startDate, Date endDate, RepositoryEntry entry, String subIdent,
			BusinessGroup businessGroup, Identity creator) {
		return teamsMeetingDao.createMeeting(subject, startDate, endDate, entry, subIdent, businessGroup, creator);
	}

	@Override
	public TeamsMeeting getMeeting(TeamsMeeting meeting) {
		if(meeting == null || meeting.getKey() == null) return null;
		return teamsMeetingDao.loadByKey(meeting.getKey());
	}

	@Override
	public List<TeamsMeeting> getMeetings(RepositoryEntry entry, String subIdent,
			BusinessGroup businessGroup) {
		return teamsMeetingDao.getMeetings(entry, subIdent, businessGroup);
	}
	
	@Override
	public List<TeamsMeeting> getAllMeetings() {
		return teamsMeetingDao.getAllMeetings();
		
	}

	@Override
	public List<TeamsMeeting> getUpcomingsMeetings(RepositoryEntry entry, String subIdent, int maxResults) {
		return teamsMeetingDao.getUpcomingMeetings(entry, subIdent, maxResults);
	}

	@Override
	public int countMeetings(TeamsMeetingsSearchParameters searchParams) {
		return teamsMeetingQueries.count(searchParams);
	}

	@Override
	public List<TeamsMeeting> searchMeetings(TeamsMeetingsSearchParameters searchParams, int firstResult, int maxResults) {
		return teamsMeetingQueries.search(searchParams, firstResult, maxResults);
	}

	@Override
	public TeamsMeeting updateMeeting(TeamsMeeting meeting) {
		meeting = teamsMeetingDao.updateMeeting(meeting);
		updateCalendarEvent(meeting);
		return meeting;
	}
	
	@Override
	public void deleteMeeting(TeamsMeeting meeting) {
		if(meeting == null || meeting.getKey() == null) return;
		
		TeamsMeeting reloadedMeeting = teamsMeetingDao.loadByKey(meeting.getKey());
		if(reloadedMeeting != null) {
			teamsMeetingDeletionHandlers.forEach(h -> h.onBeforeDelete(reloadedMeeting));
			removeCalendarEvent(reloadedMeeting);
			teamsAttendeeDao.deleteMeetingsAttendees(reloadedMeeting);
			teamsMeetingDao.deleteMeeting(reloadedMeeting);
		}
	}
	
	@Override
	public TeamsMeeting getMeeting(String identifier) {
		if(StringHelper.containsNonWhitespace(identifier)) {
			return teamsMeetingDao.loadByIdentifier(identifier);
		}
		return null;
	}

	@Override
	public boolean isIdentifierInUse(String identifier, TeamsMeeting reference) {
		if(StringHelper.containsNonWhitespace(identifier)) {
			return teamsMeetingDao.isIdentifierInUse(identifier, reference);
		}
		return false;
	}

	@Override
	public boolean isMeetingRunning(TeamsMeeting meeting) {
		return meeting != null && StringHelper.containsNonWhitespace(meeting.getOnlineMeetingJoinUrl());
	}

	@Override
	public TeamsMeeting joinMeeting(TeamsMeeting meeting, Identity identity, boolean presenter, boolean guest, TeamsErrors errors) {
		OnlineMeetingRole role = (presenter && !guest) ? OnlineMeetingRole.PRESENTER : OnlineMeetingRole.ATTENDEE;
		meeting = teamsMeetingDao.loadByKey(meeting.getKey());
		if(meeting == null) {
			errors.append(new TeamsError(TeamsErrorCodes.meetingDeleted));
		} else if(!StringHelper.containsNonWhitespace(meeting.getOnlineMeetingId())) {
			if(presenter || (!guest)) {
				dbInstance.commitAndCloseSession();
				User user = lookupUser(identity, errors);
				meeting = createOnlineMeeting(meeting, user, role, errors);
			} else {
				errors.append(new TeamsError(TeamsErrorCodes.presenterMissing));
			}
		}
		
		if(identity != null && meeting != null && !guest
				&& StringHelper.containsNonWhitespace(meeting.getOnlineMeetingJoinUrl())
				&& !teamsAttendeeDao.hasAttendee(identity, meeting)) {
			teamsAttendeeDao.createAttendee(identity, null, role.name(), new Date(), meeting);
		}
		return meeting;
	}
	
	/**
	 * The create meeting only use the communications API. To set all
	 * settings, an update with the "On behalf" user is needed.<br>
	 * If the create is done with the "On behalf" user, only this user
	 * can make the group rooms and configure the meeting in the Microsoft
	 * Teams application. The process create with /communications and update
	 * with "On behalf" user is a workaround to allow the user to configure
	 * the meeting in Teams App. and OpenOlat to set a maximum of settings.
	 * 
	 * @param meeting The meeting
	 * @param user The user if found
	 * @param role The role, PRESENTER can be elevated to PRODUCER
	 * @param errors The errors object, mandatory
	 * @return The update meeting.
	 */
	private TeamsMeeting createOnlineMeeting(TeamsMeeting meeting, User user, OnlineMeetingRole role, TeamsErrors errors) {
		TeamsMeeting lockedMeeting = null;
		try {
			lockedMeeting = teamsMeetingDao.loadForUpdate(meeting);
			if(lockedMeeting == null) {
				errors.append(new TeamsError(TeamsErrorCodes.meetingDeleted));
			} else if(!StringHelper.containsNonWhitespace(lockedMeeting.getOnlineMeetingId())) {
				OnlineMeeting onlineMeeting = graphDao.createMeeting(lockedMeeting, user, role, errors);
				if(onlineMeeting != null) {
					((TeamsMeetingImpl)lockedMeeting).setOnlineMeetingId(onlineMeeting.id);
					((TeamsMeetingImpl)lockedMeeting).setOnlineMeetingJoinUrl(onlineMeeting.joinUrl);
					lockedMeeting = teamsMeetingDao.updateMeeting(lockedMeeting);
				}
			}
		}catch (ClientException e) {
			errors.append(new TeamsError(TeamsErrorCodes.httpClientError));
			log.error("Cannot create teams meeting", e);
		} catch (Exception e) {
			errors.append(new TeamsError(TeamsErrorCodes.unkown));
			log.error("Cannot create teams meeting", e);
		} finally {
			dbInstance.commit();
		}
		return lockedMeeting;
	}
	
	@Override
	public User lookupUser(Identity identity, TeamsErrors errors) {
		TeamsUser teamsUser = teamsUserDao.getUser(identity);
		if(teamsUser != null) {
			User user = new User();
			user.id = teamsUser.getIdentifier();
			user.displayName = teamsUser.getDisplayName();
			return user;
		}

		
		String email = identity.getUser().getProperty(UserConstants.EMAIL, null);
		String institutionalEmail = identity.getUser().getProperty(UserConstants.INSTITUTIONALEMAIL, null);
		List<User> users = graphDao.searchUsersByMail(email, institutionalEmail, errors);
		if(users.size() > 1) {
			users = reduceToPrefered(email, users);
		}
		if(users.size() == 1) {
			User user = users.get(0);
			teamsUserDao.createUser(identity, user.id, user.displayName);
			dbInstance.commit();
			return user;
		}
		
		List<String> principals = new ArrayList<>();
		if(StringHelper.containsNonWhitespace(email)) {
			principals.add(email);
		}
		if(StringHelper.containsNonWhitespace(institutionalEmail)) {
			principals.add(institutionalEmail);
		}
		Authentication authentication = securityManager.findAuthentication(identity, MicrosoftAzureADFSProvider.PROVIDER);
		if(authentication != null && StringHelper.containsNonWhitespace(authentication.getAuthusername())) {
			principals.add(authentication.getAuthusername());
		}
		User user = graphDao.searchUserByUserPrincipalName(principals, errors);
		if(user == null) {
			log.debug("Cannot find user with email: {} or institutional email: {} (users found {})", email, institutionalEmail, users.size());
		} else {
			teamsUserDao.createUser(identity, user.id, user.displayName);
			dbInstance.commit();
		}
		return user;
	}
	
	private List<User> reduceToPrefered(String email, List<User> users) {
		List<User> preferedUsers = new ArrayList<>();
		// First mail
		for(User user:users) {
			if(user.mail != null && user.mail.equalsIgnoreCase(email)) {
				preferedUsers.add(user);
			}
		}
		
		// Fallback other mails
		if(preferedUsers.isEmpty()) {
			for(User user:users) {
				if(user.otherMails != null) {
					for(String otherMail:user.otherMails) {
						if(otherMail != null && otherMail.equalsIgnoreCase(email)) {
							preferedUsers.add(user);
						}
					}
				}
			}
		}
		
		if(!preferedUsers.isEmpty()) {
			return preferedUsers;
		}
		return users;
	}

	@Override
	public ConnectionInfos checkConnection(TeamsErrors errors) {
		return graphDao.check(errors);
	}

	@Override
	public ConnectionInfos checkConnection(String clientId, String clientSecret, String tenantGuid,
			String producerId, TeamsErrors errors) {
		return graphDao.check(clientId, clientSecret, tenantGuid, producerId, errors);
	}
	
	private void removeCalendarEvent(TeamsMeeting meeting) {
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
	
	private void updateCalendarEvent(TeamsMeeting meeting) {
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
					event.setSubject(meeting.getSubject());
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
			KalendarEvent newEvent = new KalendarEvent(eventId, null, meeting.getSubject(), meeting.getStartDate(), meeting.getEndDate());
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
	
	private String generateEventExternalId(TeamsMeeting meeting) {
		return "ms-teams-meeting-" + meeting.getKey();
	}
	
	private KalendarEventLink generateEventLink(TeamsMeeting meeting) {
		String id = meeting.getKey().toString();
		String displayName = meeting.getSubject();
		if(meeting.getEntry() != null) {
			StringBuilder businessPath = new StringBuilder(128);
			businessPath.append("[RepositoryEntry:").append(meeting.getEntry().getKey()).append("]");
			if(StringHelper.containsNonWhitespace(meeting.getSubIdent())) {
				businessPath.append("[CourseNode:").append(meeting.getSubIdent()).append("]");
			}
			businessPath.append("[Meeting:").append(meeting.getKey()).append("]");
			String url = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath.toString());
			return new KalendarEventLink("teams", id, displayName, url, "o_CourseModule_icon");
		} else if(meeting.getBusinessGroup() != null) {
			StringBuilder businessPath = new StringBuilder(128);
			businessPath.append("[BusinessGroup:").append(meeting.getBusinessGroup().getKey())
				.append("][toolteams:0][Meeting:").append(meeting.getKey()).append("]");
			String url = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath.toString());
			return new KalendarEventLink("teams", id, displayName, url, "o_icon_group");
		}
		return null;
	}
	
	private Kalendar getCalendar(TeamsMeeting meeting) {
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
}
