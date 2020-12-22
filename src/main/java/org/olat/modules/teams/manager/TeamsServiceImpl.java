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
import org.olat.modules.teams.TeamsModule;
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

	private static final Iterable<TeamsMeetingDeletionHandler> teamdMeetingDeletionHandlers = null;

	@Autowired
	private DB dbInstance;
	@Autowired
	private TeamsModule teamsModule;
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
			
			String onlineMeetingId = reloadedMeeting.getOnlineMeetingId();
			if(StringHelper.containsNonWhitespace(onlineMeetingId)
					&& StringHelper.containsNonWhitespace(teamsModule.getOnBehalfUserId())) {
				try {
					graphDao.delete(onlineMeetingId);
				} catch (Exception e) {
					log.error("Cannot delete meeting with id: {}", onlineMeetingId, e);
				}
			}
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
				User user = lookupUser(identity);
				meeting = createOnlineMeeting(meeting, user, role, errors);
			} else {
				errors.append(new TeamsError(TeamsErrorCodes.presenterMissing));
			}
		} else if(identity != null && !guest && StringHelper.containsNonWhitespace(teamsModule.getOnBehalfUserId())) {
			dbInstance.commitAndCloseSession();
			User user = lookupUser(identity);
			updateOnlineMeeting(meeting, user, role);
		}
		
		if(identity != null && meeting != null && !guest
				&& StringHelper.containsNonWhitespace(meeting.getOnlineMeetingJoinUrl())
				&& !teamsAttendeeDao.hasAttendee(identity, meeting)) {
			teamsAttendeeDao.createAttendee(identity, null, role.name(), new Date(), meeting);
		}
		return meeting;
	}
	
	private TeamsMeeting createOnlineMeeting(TeamsMeeting meeting, User user, OnlineMeetingRole role, TeamsErrors errors) {
		TeamsMeeting lockedMeeting = null;
		try {
			lockedMeeting = teamsMeetingDao.loadForUpdate(meeting);
			if(lockedMeeting == null) {
				errors.append(new TeamsError(TeamsErrorCodes.meetingDeleted));
			} else if(StringHelper.containsNonWhitespace(lockedMeeting.getOnlineMeetingId())) {
				updateOnlineMeeting(lockedMeeting, user, role);
			} else {
				OnlineMeeting onlineMeeting = graphDao.createMeeting(lockedMeeting, user, role, errors);
				if(onlineMeeting != null) {
					((TeamsMeetingImpl)lockedMeeting).setOnlineMeetingId(onlineMeeting.id);
					((TeamsMeetingImpl)lockedMeeting).setOnlineMeetingJoinUrl(onlineMeeting.joinUrl);
					lockedMeeting = teamsMeetingDao.updateMeeting(lockedMeeting);
				}
			}
		} catch (Exception e) {
			errors.append(new TeamsError(TeamsErrorCodes.unkown));
			log.error("Cannot create teams meeting", e);
		} finally {
			dbInstance.commit();
		}
		return lockedMeeting;
	}
	
	private void updateOnlineMeeting(TeamsMeeting meeting, User graphUser, OnlineMeetingRole role) {
		if(graphUser != null && StringHelper.containsNonWhitespace(teamsModule.getOnBehalfUserId())) {
			graphDao.updateOnlineMeeting(meeting, graphUser, role);
		}
	}
	
	@Override
	public User lookupUser(Identity identity) {
		TeamsUser teamsUser = teamsUserDao.getUser(identity);
		if(teamsUser != null) {
			User user = new User();
			user.id = teamsUser.getIdentifier();
			user.displayName = teamsUser.getDisplayName();
			return user;
		}

		
		String email = identity.getUser().getProperty(UserConstants.EMAIL, null);
		String institutionalEmail = identity.getUser().getProperty(UserConstants.INSTITUTIONALEMAIL, null);
		List<User> users = graphDao.searchUsersByMail(email, institutionalEmail);
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
		User user = graphDao.searchUserByUserPrincipalName(principals);
		if(user == null) {
			log.debug("Cannot find user with email: {} or institutional email: {} (users found {})", email, institutionalEmail, users.size());
		} else {
			teamsUserDao.createUser(identity, user.id, user.displayName);
			dbInstance.commit();
		}
		return null;
	}

	@Override
	public ConnectionInfos checkConnection(TeamsErrors errors) {
		return graphDao.check(errors);
	}

	@Override
	public ConnectionInfos checkConnection(String clientId, String clientSecret, String tenantGuid,
			String applicationId, String producerId, String onBehalfId, TeamsErrors errors) {
		return graphDao.check(clientId, clientSecret, tenantGuid, applicationId, producerId, onBehalfId, errors);
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
