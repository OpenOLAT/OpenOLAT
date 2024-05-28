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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.OAuth2Tokens;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsModule;
import org.olat.modules.teams.model.TeamsError;
import org.olat.modules.teams.model.TeamsErrorCodes;
import org.olat.modules.teams.model.TeamsErrors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.azure.core.credential.TokenCredential;
import com.microsoft.graph.models.Application;
import com.microsoft.graph.models.ApplicationCollectionResponse;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.Identity;
import com.microsoft.graph.models.IdentitySet;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.LobbyBypassScope;
import com.microsoft.graph.models.LobbyBypassSettings;
import com.microsoft.graph.models.MeetingParticipantInfo;
import com.microsoft.graph.models.MeetingParticipants;
import com.microsoft.graph.models.OnlineMeeting;
import com.microsoft.graph.models.OnlineMeetingPresenters;
import com.microsoft.graph.models.OnlineMeetingRole;
import com.microsoft.graph.models.Organization;
import com.microsoft.graph.models.User;
import com.microsoft.graph.models.UserCollectionResponse;
import com.microsoft.graph.serviceclient.GraphServiceClient;

/**
 * 
 * Initial date: 23 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MicrosoftGraphDAO {
	
	private static final Logger log = Tracing.createLoggerFor(MicrosoftGraphDAO.class);
	
	private static final String[] USER_ATTRS = new String[] { "displayName", "id", "mail", "otherMails" };
	private static final String[] ORGANISATION_ATTRS = new String[] { "id", "displayName" };
	
	public static final List<OnlineMeetingPresenters> ALLOWED_PRESENTERS_FOR_ATTENDEE = List
			.of(OnlineMeetingPresenters.Everyone, OnlineMeetingPresenters.Organization);
	public static final List<LobbyBypassScope> ALLOWED_LOBBY_BYPASS_FOR_ATTENDEE = List
			.of(LobbyBypassScope.Everyone, LobbyBypassScope.Organization, LobbyBypassScope.OrganizationAndFederated);
	

	@Autowired
	private TeamsModule teamsModule;
	
	public synchronized TokenCredential getTokenProvider(OAuth2Tokens tokens) {
		return new OAuth2TokenCredential(tokens);
	}
	
	public GraphServiceClient client(OAuth2Tokens tokens) {
		TokenCredential authProvider = getTokenProvider(tokens);
		return client(authProvider);
	}
	
	public GraphServiceClient client(TokenCredential authProvider) {
		return new GraphServiceClient(authProvider);
	}
	
	public static boolean canAttendeeOpenMeeting(TeamsMeeting meeting) {
		if(meeting == null || meeting.getAllowedPresentersEnum() == null || meeting.getLobbyBypassScopeEnum() == null) {
			return false;
		}
		return ALLOWED_PRESENTERS_FOR_ATTENDEE.contains(meeting.getAllowedPresentersEnum())
			&& ALLOWED_LOBBY_BYPASS_FOR_ATTENDEE.contains(meeting.getLobbyBypassScopeEnum());
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
	 * @param role The role (PRESENTER can be promoted to PRODUCER)
	 * @param errors Mandatory errors object
	 * @return An online meeting if successful
	 */
	public OnlineMeeting createMeeting(TeamsMeeting meeting, User user, OnlineMeetingRole role, OAuth2Tokens oauth2Tokens, TeamsErrors errors) {
		MeetingParticipants participants = new MeetingParticipants();
		participants.setAttendees(new ArrayList<>());
		if(user != null) {
			if(role == OnlineMeetingRole.Presenter) {
				// Add all possible roles
				participants.setOrganizer(createParticipantInfo(user, OnlineMeetingRole.Presenter));
				participants.getAttendees().add(createParticipantInfo(user, OnlineMeetingRole.Presenter));
				log.info("Create Teams Meeting on MS for {}, for role {} and MS user as organizer and presenter {} {}", meeting.getKey(), role, user.getId(), user.getDisplayName());
			} else if(StringHelper.containsNonWhitespace(teamsModule.getProducerId()) && canAttendeeOpenMeeting(meeting)) {
				// Attendee can create an online meeting only if they have a chance to enter it
				participants.setOrganizer(createParticipantInfo(teamsModule.getProducerId(), OnlineMeetingRole.Presenter));
				MeetingParticipantInfo  infos = createParticipantInfo(user, OnlineMeetingRole.Attendee);
				participants.getAttendees().add(infos);
				log.info("Create Teams Meeting on MS for {}, for role {} and MS user as attendee {} {}", meeting.getKey(), role, user.getId(), user.getDisplayName());
			} else {
				errors.append(new TeamsError(TeamsErrorCodes.organizerMissing));
				return null;
			}	
		} else {
			errors.append(new TeamsError(TeamsErrorCodes.organizerMissing));
			return null;
		}
		
		OnlineMeeting onlineMeeting = new OnlineMeeting();
		if(meeting.getStartDate() != null && meeting.getEndDate() != null) {
			onlineMeeting.setStartDateTime(toCalendar(meeting.getStartDate()));
			onlineMeeting.setEndDateTime(toCalendar(meeting.getEndDate()));
		}
		onlineMeeting.setSubject(meeting.getSubject());
		onlineMeeting.setParticipants(participants);
		onlineMeeting.setAllowedPresenters(meeting.getAllowedPresentersEnum());

		LobbyBypassSettings lobbyBypassSettings = new LobbyBypassSettings();
		lobbyBypassSettings.setIsDialInBypassEnabled(Boolean.TRUE);
		lobbyBypassSettings.setScope(meeting.getLobbyBypassScopeEnum());
		onlineMeeting.setLobbyBypassSettings(lobbyBypassSettings);
	
		String joinInformations = meeting.getJoinInformation();
		if(StringHelper.containsNonWhitespace(joinInformations)) {
			ItemBody body = new ItemBody();
			if(StringHelper.isHtml(joinInformations)) {
				body.setContentType(BodyType.Html);
			} else {
				body.setContentType(BodyType.Text);
			}
			body.setContent("<html><body>" + joinInformations + "</body></html>");
			onlineMeeting.setJoinInformation(body);
		}

		onlineMeeting = client(oauth2Tokens)
				.me()
				.onlineMeetings()
				.post(onlineMeeting);
		log.info(Tracing.M_AUDIT, "Online-Meeting created (/communications) with id: {}", onlineMeeting.getId());

		return onlineMeeting;
	}

	/**
	 * $filter=otherMails/any(x:x eq ‘xxx@abc.com’)
	 * 
	 * @param email The E-mail (mandatory)
	 * @param institutionalEmail The institutional E-mail (optional)
	 * @return The first users found
	 */
	public List<User> searchUsersByMail(String email, String institutionalEmail, OAuth2Tokens oauth2Tokens, TeamsErrors errors) {
		StringBuilder sb = new StringBuilder();
		sb.append("mail eq '").append(email).append("'")
		  .append(" or otherMails/any(x:x eq '").append(email).append("')");
		if(StringHelper.containsNonWhitespace(institutionalEmail)) {
			sb.append(" or mail eq '").append(institutionalEmail).append("'")
			  .append(" or otherMails/any(x:x eq '").append(institutionalEmail).append("')");
		}

		try {
			UserCollectionResponse user = client(oauth2Tokens)
					.users()
					.get(requestConfiguration -> {
						requestConfiguration.queryParameters.select = USER_ATTRS;
						requestConfiguration.queryParameters.filter = sb.toString();
					});
			return user.getValue();
		} catch (NullPointerException | IllegalArgumentException e) {
			log.error("Cannot find user with email: {} {}", email, institutionalEmail, e);
			errors.append(new TeamsError(TeamsErrorCodes.httpClientError));
			return new ArrayList<>();
		}
	}
	
	public Organization getOrganisation(String id, GraphServiceClient client) {
		return client
			.organization()
			.byOrganizationId(id)
			.get(requestConfiguration ->
		    	requestConfiguration.queryParameters.select = ORGANISATION_ATTRS);
	}
	
	public Application getApplication(String id, GraphServiceClient client, TeamsErrors errors) {
		try {
			ApplicationCollectionResponse appsPage = client
				.applications()
				.get(requestConfiguration -> {
					requestConfiguration.queryParameters.top = 1;
					requestConfiguration.queryParameters.filter = "appId eq '" + id + "'";
				});
			
			List<Application> apps = appsPage.getValue();
			return apps == null || apps.isEmpty() ? null : apps.get(0);
		} catch (NullPointerException | IllegalArgumentException e) {
			errors.append(new TeamsError(e.getMessage(), ""));
			log.error("", e);
			return null;
		}
	}
	
	public User getMe(OAuth2Tokens oauth2Tokens) {
		try {
			User me = oauth2Tokens.getUser(User.class);
			if(me == null) {
				me = client(oauth2Tokens)
					.me()
					.get();
			}
			if(me != null) {
				log.info("Me: {} ({})", me.getGivenName(), me.getId());
			}
			return me;
		} catch (Exception e) {
			log.error("", e);
		}
		return null;
	}

	public static final LobbyBypassScope toLobbyBypassScope(String string) {
		LobbyBypassScope val = LobbyBypassScope.Organization;
		if(StringHelper.containsNonWhitespace(string)) {
			for(LobbyBypassScope scope:LobbyBypassScope.values()) {
				if(scope.name().equalsIgnoreCase(string)) {
					return scope;
				}
			}
		}
		return val;
	}
	
	public static final OnlineMeetingPresenters toOnlineMeetingPresenters(String string) {
		OnlineMeetingPresenters val = OnlineMeetingPresenters.Everyone;
		if(StringHelper.containsNonWhitespace(string)) {
			for(OnlineMeetingPresenters presenter:OnlineMeetingPresenters.values()) {
				if(presenter.name().equalsIgnoreCase(string)) {
					return presenter;
				}
			}
		}
		return val;
	}
	
	private IdentitySet createIdentitySetById(User user) {
		return createIdentitySetById(user.getId());
	}
	
	private IdentitySet createIdentitySetById(String id) {
		IdentitySet identitySet = new IdentitySet();
		Identity user = new Identity();
		user.setId(id);
		identitySet.setUser(user);
		return identitySet;
	}
	
	private MeetingParticipantInfo createParticipantInfo(IdentitySet identity, OnlineMeetingRole role) {
		MeetingParticipantInfo participantInfo = new MeetingParticipantInfo();
		participantInfo.setIdentity(identity);
		participantInfo.setRole(role);
		return participantInfo;
	}
	
	private MeetingParticipantInfo createParticipantInfo(User user, OnlineMeetingRole role) {
		return createParticipantInfo(createIdentitySetById(user), role) ;
	}
	
	private MeetingParticipantInfo createParticipantInfo(String id, OnlineMeetingRole role) {
		return createParticipantInfo(createIdentitySetById(id), role) ;
	}
	
	private static final OffsetDateTime toCalendar(Date date) {
		return date.toInstant()
				  .atOffset(ZoneOffset.UTC);
	}
}
