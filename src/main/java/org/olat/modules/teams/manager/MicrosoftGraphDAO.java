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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsModule;
import org.olat.modules.teams.model.ConnectionInfos;
import org.olat.modules.teams.model.TeamsError;
import org.olat.modules.teams.model.TeamsErrorCodes;
import org.olat.modules.teams.model.TeamsErrors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.extensions.Application;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.Identity;
import com.microsoft.graph.models.extensions.IdentitySet;
import com.microsoft.graph.models.extensions.ItemBody;
import com.microsoft.graph.models.extensions.LobbyBypassSettings;
import com.microsoft.graph.models.extensions.MeetingParticipantInfo;
import com.microsoft.graph.models.extensions.MeetingParticipants;
import com.microsoft.graph.models.extensions.OnlineMeeting;
import com.microsoft.graph.models.extensions.Organization;
import com.microsoft.graph.models.extensions.User;
import com.microsoft.graph.models.generated.AccessLevel;
import com.microsoft.graph.models.generated.BodyType;
import com.microsoft.graph.models.generated.LobbyBypassScope;
import com.microsoft.graph.models.generated.OnlineMeetingPresenters;
import com.microsoft.graph.models.generated.OnlineMeetingRole;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.microsoft.graph.requests.extensions.IApplicationCollectionPage;
import com.microsoft.graph.requests.extensions.IUserCollectionPage;

/**
 * 
 * Initial date: 23 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MicrosoftGraphDAO {
	
	private static final Logger log = Tracing.createLoggerFor(MicrosoftGraphDAO.class);
	
	private static final String SERVICE_ROOT = "https://graph.microsoft.com/beta";

	@Autowired
	private TeamsModule teamsModule;
	
	private AuthenticationTokenProvider tokenProvider;
	
	private synchronized AuthenticationTokenProvider getTokenProvider() {
		if(tokenProvider == null
				|| (teamsModule.getApiKey() != null && !teamsModule.getApiKey().equals(tokenProvider.getClientId()))
				|| (teamsModule.getApiSecret() != null && !teamsModule.getApiSecret().equals(tokenProvider.getClientSecret()))
				|| (teamsModule.getTenantGuid() != null && !teamsModule.getTenantGuid().equals(tokenProvider.getTenantGuid()))) {
			String clientId = teamsModule.getApiKey();
			String clientSecret = teamsModule.getApiSecret();
			String tenantGuid = teamsModule.getTenantGuid();
			MicrosoftGraphAccessTokenManager tokenManager = new MicrosoftGraphAccessTokenManager(clientId, clientSecret, tenantGuid);
			tokenProvider = new AuthenticationTokenProvider(tokenManager);
		}
		return tokenProvider;
	}
	
	public IGraphServiceClient client() {
		AuthenticationTokenProvider authProvider = getTokenProvider();
		IGraphServiceClient graphClient = GraphServiceClient
				.builder()
				.authenticationProvider(authProvider)
				.buildClient();
		graphClient.setServiceRoot(SERVICE_ROOT);
		return graphClient;
	}
	
	public IGraphServiceClient client(AuthenticationTokenProvider authProvider) {
		IGraphServiceClient graphClient = GraphServiceClient
				.builder()
				.authenticationProvider(authProvider)
				.buildClient();
		graphClient.setServiceRoot(SERVICE_ROOT);
		return graphClient;
	}
	
	public User getUser(String id) {
		return client().users()
				.byId(id)
				.buildRequest()
				.get();
	}
	
	/**
	 * 
	 * @param mail
	 * @param issuer
	 * @return
	 */
	public List<User> searchUsersByAssignedId(String mail, String issuer) {
		StringBuilder sb = new StringBuilder();
		sb.append("identities/any(c:c/issuerAssignedId eq '").append(mail).append("'")
		  .append(" and c/issuer eq '").append(issuer).append("')");
		
		IUserCollectionPage user = client().users()
				.buildRequest()
				.filter(sb.toString())
				.select("displayName,id")
				.get();
		
		return user.getCurrentPage();
	}
	
	public static boolean canAttendeeOpenMeeting(TeamsMeeting meeting) {
		return (meeting.getAllowedPresentersEnum() == OnlineMeetingPresenters.EVERYONE
				|| meeting.getAllowedPresentersEnum() == OnlineMeetingPresenters.ORGANIZATION)
			&& (meeting.getLobbyBypassScopeEnum() == LobbyBypassScope.EVERYONE
					|| meeting.getLobbyBypassScopeEnum() == LobbyBypassScope.ORGANIZATION
					|| meeting.getLobbyBypassScopeEnum() == LobbyBypassScope.ORGANIZATION_AND_FEDERATED);
		
	}
	
	public OnlineMeeting createMeeting(TeamsMeeting meeting, User user, OnlineMeetingRole role, TeamsErrors errors) {
		MeetingParticipants participants = new MeetingParticipants();
		if(user != null) {
			if(role == OnlineMeetingRole.PRESENTER) {
				IdentitySet identitySet = createIdentitySetById(user);
				participants.organizer = createParticipantInfo(identitySet, OnlineMeetingRole.PRODUCER);
				IdentitySet identityAsPresenterSet = createIdentitySetById(user);
				participants.attendees = new ArrayList<>();
				MeetingParticipantInfo  infos = createParticipantInfo(identityAsPresenterSet, OnlineMeetingRole.PRESENTER);
				participants.attendees.add(infos);
			} else if(StringHelper.containsNonWhitespace(teamsModule.getProducerId()) && canAttendeeOpenMeeting(meeting)) {
				// Attendee can create an online meeting only if they have a chance to enter it
				IdentitySet identitySet = createIdentitySetById(teamsModule.getProducerId());
				participants.organizer = createParticipantInfo(identitySet, OnlineMeetingRole.PRODUCER);
				IdentitySet identityAsPresenterSet = createIdentitySetById(user);
				participants.attendees = new ArrayList<>();
				MeetingParticipantInfo  infos = createParticipantInfo(identityAsPresenterSet, OnlineMeetingRole.ATTENDEE);
				participants.attendees.add(infos);
			} else {
				errors.append(new TeamsError(TeamsErrorCodes.organizerMissing));
				return null;
			}	
		} else if(StringHelper.containsNonWhitespace(teamsModule.getProducerId())) {
			if(canAttendeeOpenMeeting(meeting)) {
				IdentitySet identitySet = createIdentitySetById(teamsModule.getProducerId());
				participants.organizer = createParticipantInfo(identitySet, OnlineMeetingRole.PRODUCER);
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
			onlineMeeting.startDateTime = toCalendar(meeting.getStartDate());
			onlineMeeting.endDateTime = toCalendar(meeting.getEndDate());
		}
		onlineMeeting.subject = meeting.getSubject();
		onlineMeeting.participants = participants;
		onlineMeeting.allowedPresenters = toOnlineMeetingPresenters(meeting.getAllowedPresenters());
		onlineMeeting.accessLevel = toAccessLevel(meeting.getAccessLevel());
		onlineMeeting.entryExitAnnouncement = Boolean.valueOf(meeting.isEntryExitAnnouncement());
		
		LobbyBypassSettings lobbyBypassSettings = new LobbyBypassSettings();
		lobbyBypassSettings.isDialInBypassEnabled = Boolean.FALSE;
		lobbyBypassSettings.scope = toLobbyBypassScope(meeting.getLobbyBypassScope());
		onlineMeeting.lobbyBypassSettings = lobbyBypassSettings;

		String joinInformations = meeting.getJoinInformation();
		if(StringHelper.containsNonWhitespace(joinInformations)) {
			ItemBody body = new ItemBody();
			if(StringHelper.isHtml(joinInformations)) {
				body.contentType = BodyType.HTML;
			} else {
				body.contentType = BodyType.TEXT;
			}
			body.content = "<html><body>" + joinInformations + "</body></html>";
			onlineMeeting.joinInformation = body;
		}
		
		if(StringHelper.containsNonWhitespace(teamsModule.getOnBehalfUserId())) {
			onlineMeeting = client()
					.users(teamsModule.getOnBehalfUserId())
					.onlineMeetings()
					.buildRequest()
					.post(onlineMeeting);
		} else {
			onlineMeeting = client()
					.communications()
					.onlineMeetings()
					.buildRequest()
					.post(onlineMeeting);
		}
		
		log.info(Tracing.M_AUDIT, "Online-Meeting created with id: {}", onlineMeeting.id);
		return onlineMeeting;
	}
	
	/**
	 * This method is only supported if the application is allowed to
	 * operate on behalf of a real user.
	 * 
	 * @param meeting The meeting
	 * @param user The user
	 * @param role The role of the user
	 * @return The updated meeting
	 */
	public OnlineMeeting updateOnlineMeeting(TeamsMeeting meeting, User user, OnlineMeetingRole role) {
		String id =  meeting.getOnlineMeetingId();
		
		OnlineMeeting onlineMeeting = new OnlineMeeting();
		if(meeting.getStartDate() != null && meeting.getEndDate() != null) {
			onlineMeeting.startDateTime = toCalendar(meeting.getStartDate());
			onlineMeeting.endDateTime = toCalendar(meeting.getEndDate());
		}
		onlineMeeting.subject = meeting.getSubject();
		
		if(user != null) {
			MeetingParticipants participants = new MeetingParticipants();
			IdentitySet identitySet = createIdentitySetById(user);
			participants.attendees = new ArrayList<>();
			participants.attendees.add(createParticipantInfo(identitySet, role));
			onlineMeeting.participants = participants;
		}
		
		// access, body informations cannot be updatet
		onlineMeeting.allowedPresenters = toOnlineMeetingPresenters(meeting.getAllowedPresenters());
		
		LobbyBypassSettings lobbyBypassSettings = new LobbyBypassSettings();
		lobbyBypassSettings.isDialInBypassEnabled = Boolean.FALSE;
		lobbyBypassSettings.scope = toLobbyBypassScope(meeting.getLobbyBypassScope());
		onlineMeeting.lobbyBypassSettings = lobbyBypassSettings;

		OnlineMeeting updatedOnlineMeeting = client()
				.users(teamsModule.getOnBehalfUserId())
				.onlineMeetings(id)
				.buildRequest()
				.patch(onlineMeeting);
		
		log.info(Tracing.M_AUDIT, "Online-Meeting updated with id: {}", id);
		return updatedOnlineMeeting;
	}
	
	public OnlineMeeting searchOnlineMeeting(String externalId) {
		return client()
				.users(teamsModule.getOnBehalfUserId())
				.onlineMeetings(externalId)
				.buildRequest()
				.get();
	}
	
	public void delete(String meetingId) {
		client()
				.users(teamsModule.getOnBehalfUserId())
				.onlineMeetings(meetingId)
				.buildRequest()
				.delete();
		
	}

	/**
	 * $filter=otherMails/any(x:x eq ‘xxx@abc.com’)
	 * 
	 * @param email The E-mail (mandatory)
	 * @param institutionalEmail The institutional E-mail (optional)
	 * @return The first users found
	 */
	public List<User> searchUsersByMail(String email, String institutionalEmail) {
		StringBuilder sb = new StringBuilder();
		sb.append("mail eq '").append(email).append("'")
		  .append(" or otherMails/any(x:x eq '").append(email).append("')");
		if(StringHelper.containsNonWhitespace(institutionalEmail)) {
			sb.append(" or mail eq '").append(institutionalEmail).append("'")
			  .append(" or otherMails/any(x:x eq '").append(institutionalEmail).append("')");
		}

		IUserCollectionPage user = client()
				.users()
				.buildRequest()
				.filter(sb.toString())
				.select("displayName,id,mail,otherMails")
				.get();
		return user.getCurrentPage();
	}
	
	/**
	 * 
	 * @param mail
	 * @param issuer
	 * @return
	 */
	public User searchUserByUserPrincipalName(List<String> principals) {
		if(principals == null || principals.isEmpty()) return null;
		
		StringBuilder sb = new StringBuilder();
		for(String principal:principals) {
			if(sb.length() > 0) {
				sb.append(" or ");
			}
			sb.append("userPrincipalName eq '").append(principal).append("'");
		}

		IUserCollectionPage user = client().users()
				.buildRequest()
				.filter(sb.toString())
				.select("displayName,id,mail,otherMails")
				.top(1)
				.get();
		
		List<User> users = user.getCurrentPage();
		return users.isEmpty() ? null : users.get(0);
	}
	
	public User searchUserById(String id, IGraphServiceClient client) {
		try {
			return client
					.users(id)
					.buildRequest()
					.select("displayName,id,mail,otherMails")
					.get();
		} catch (ClientException e) {
			log.error("Cannot find user with id: {}", id, e);
			return null;
		}
	}
	
	public List<User> getAllUsers() {
		IUserCollectionPage user = client()
				.users()
				.buildRequest()
				.select("displayName,id,mail,otherMails")
				.get();
		return user.getCurrentPage();
	}
	
	public Organization getOrganisation(String id, IGraphServiceClient client) {
		try {
			return client
				.organization(id)
				.buildRequest()
				.select("id,displayName")
				.get();
		} catch (ClientException e) {
			log.error("", e);
			return null;
		}
	}
	
	public Application getApplication(String id, IGraphServiceClient client) {
		try {
			IApplicationCollectionPage appsPage = client
				.applications()
				.buildRequest()
				.filter("appId eq '" + id + "'")
				.top(1)
				.get();
			
			List<Application> apps = appsPage.getCurrentPage();
			return apps == null || apps.isEmpty() ? null : apps.get(0);
		} catch (ClientException e) {
			log.error("", e);
			return null;
		}
	}
	
	public ConnectionInfos check(String clientId, String clientSecret, String tenantGuid,
			String applicationId, String producerId, String onBehalfId) {
		
		try {
			MicrosoftGraphAccessTokenManager accessTokenManager = new MicrosoftGraphAccessTokenManager(clientId, clientSecret, tenantGuid);
			AuthenticationTokenProvider authProvider = new AuthenticationTokenProvider(accessTokenManager);
			IGraphServiceClient client = client(authProvider);
			
			Organization org = getOrganisation(tenantGuid, client);
			String organisation = org == null ? null : org.displayName;
			
			String onBehalfDisplayName = null;
			if(StringHelper.containsNonWhitespace(onBehalfId)) {
				User onbehalfUser = searchUserById(onBehalfId, client);
				onBehalfDisplayName = onbehalfUser == null ? null : onbehalfUser.displayName;
			}
			
			String producerDisplayName = null;
			if(StringHelper.containsNonWhitespace(producerId)) {
				User producer = searchUserById(producerId, client);
				producerDisplayName = producer == null ? null : producer.displayName;
			}
			
			String application = null;
			if(StringHelper.containsNonWhitespace(applicationId)) {
				Application app = getApplication(applicationId, client);
				application = app == null ? null : app.displayName;
			}

			return new ConnectionInfos(organisation, onBehalfDisplayName, producerDisplayName, application);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	public final ConnectionInfos check() {
		try {
			IGraphServiceClient client = client();
			String tenantId = teamsModule.getTenantGuid();
			Organization org = getOrganisation(tenantId, client);
			String organisation = org == null ? null : org.displayName;
			
			User onbehalfUser = null;
			String onBehalfDisplayName = null;
			if(StringHelper.containsNonWhitespace(teamsModule.getOnBehalfUserId())) {
				onbehalfUser = searchUserById(teamsModule.getOnBehalfUserId(), client);
				onBehalfDisplayName = onbehalfUser == null ? null : onbehalfUser.displayName;
			}

			String producerDisplayName = null;
			if(StringHelper.containsNonWhitespace(teamsModule.getProducerId())) {
				if(onbehalfUser != null && onbehalfUser.id.equals(teamsModule.getProducerId())) {
					producerDisplayName = onbehalfUser.displayName;
				} else {
					User producer = searchUserById(teamsModule.getProducerId(), client);
					producerDisplayName = producer == null ? null : producer.displayName;
				}
			}

			String application = null;
			if(StringHelper.containsNonWhitespace(teamsModule.getApplicationId())) {
				Application app = getApplication(teamsModule.getApplicationId(), client);
				application = app == null ? null : app.displayName;
			}

			return new ConnectionInfos(organisation, onBehalfDisplayName, producerDisplayName, application);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

	public static final LobbyBypassScope toLobbyBypassScope(String string) {
		LobbyBypassScope val = LobbyBypassScope.ORGANIZATION;
		if(StringHelper.containsNonWhitespace(string)) {
			try {
				val = LobbyBypassScope.valueOf(string);
			} catch (Exception e) {
				log.error("Cannot parse lobby bypass scope: {}", string, e);
			}
		}
		return val;
	}
	
	public static final AccessLevel toAccessLevel(String string) {
		AccessLevel val = AccessLevel.EVERYONE;
		if(StringHelper.containsNonWhitespace(string)) {
			try {
				val = AccessLevel.valueOf(string);
			} catch (Exception e) {
				log.error("Cannot parse access level: {}", string, e);
			}
		}
		return val;
	}
	
	public static final OnlineMeetingPresenters toOnlineMeetingPresenters(String string) {
		OnlineMeetingPresenters val = OnlineMeetingPresenters.EVERYONE;
		if(StringHelper.containsNonWhitespace(string)) {
			try {
				val = OnlineMeetingPresenters.valueOf(string);
			} catch (Exception e) {
				log.error("Cannot parse online meeting presenters: {}", string, e);
			}
		}
		return val;
	}
	
	private IdentitySet createIdentitySetById(User user) {
		return createIdentitySetById(user.id);
	}
	
	private IdentitySet createIdentitySetById(String id) {
		IdentitySet identitySet = new IdentitySet();
		Identity user = new Identity();
		user.id = id;
		identitySet.user = user;

		if(StringHelper.containsNonWhitespace(teamsModule.getApplicationId())) {
			Identity app = new Identity();
			app.id = teamsModule.getApplicationId();
			identitySet.application = app;
		}
		return identitySet;
	}
	
	private MeetingParticipantInfo createParticipantInfo(IdentitySet identity, OnlineMeetingRole role) {
		MeetingParticipantInfo participantInfo = new MeetingParticipantInfo();
		participantInfo.identity = identity;
		participantInfo.role = role;
		return participantInfo;
	}
	
	private static final Calendar toCalendar(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}
}
