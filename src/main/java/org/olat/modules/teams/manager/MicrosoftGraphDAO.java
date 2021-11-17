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

import com.microsoft.aad.msal4j.MsalException;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.AccessLevel;
import com.microsoft.graph.models.Application;
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
import com.microsoft.graph.requests.ApplicationCollectionPage;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.UserCollectionPage;

import okhttp3.Request;

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
	
	public static final List<OnlineMeetingPresenters> ALLOWED_PRESENTERS_FOR_ATTENDEE = List
			.of(OnlineMeetingPresenters.EVERYONE, OnlineMeetingPresenters.ORGANIZATION);
	public static final List<LobbyBypassScope> ALLOWED_LOBBY_BYPASS_FOR_ATTENDEE = List
			.of(LobbyBypassScope.EVERYONE, LobbyBypassScope.ORGANIZATION, LobbyBypassScope.ORGANIZATION_AND_FEDERATED);
	

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
	
	public GraphServiceClient<Request> client() {
		AuthenticationTokenProvider authProvider = getTokenProvider();
		GraphServiceClient<Request> graphClient = GraphServiceClient
				.builder()
				.authenticationProvider(authProvider)
				.buildClient();
		graphClient.setServiceRoot(SERVICE_ROOT);
		return graphClient;
	}
	
	public GraphServiceClient<Request> client(AuthenticationTokenProvider authProvider) {
		GraphServiceClient<Request> graphClient = GraphServiceClient
				.builder()
				.authenticationProvider(authProvider)
				.buildClient();
		graphClient.setServiceRoot(SERVICE_ROOT);
		return graphClient;
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
	public OnlineMeeting createMeeting(TeamsMeeting meeting, User user, OnlineMeetingRole role, TeamsErrors errors)
	throws ClientException {
		MeetingParticipants participants = new MeetingParticipants();
		participants.attendees = new ArrayList<>();
		if(user != null) {
			if(role == OnlineMeetingRole.PRESENTER) {
				// Add all possible roles
				participants.organizer = createParticipantInfo(user, OnlineMeetingRole.PRESENTER);
				participants.attendees.add(createParticipantInfo(user, OnlineMeetingRole.PRESENTER));
				log.info("Create Teams Meeting on MS for {}, for role {} and MS user as organizer and presenter {} {}", meeting.getKey(), role, user.id, user.displayName);
			} else if(StringHelper.containsNonWhitespace(teamsModule.getProducerId()) && canAttendeeOpenMeeting(meeting)) {
				// Attendee can create an online meeting only if they have a chance to enter it
				participants.organizer = createParticipantInfo(teamsModule.getProducerId(), OnlineMeetingRole.PRESENTER);
				MeetingParticipantInfo  infos = createParticipantInfo(user, OnlineMeetingRole.ATTENDEE);
				participants.attendees.add(infos);
				log.info("Create Teams Meeting on MS for {}, for role {} and MS user as attendee {} {}", meeting.getKey(), role, user.id, user.displayName);
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

		LobbyBypassSettings lobbyBypassSettings = new LobbyBypassSettings();
		lobbyBypassSettings.isDialInBypassEnabled = Boolean.TRUE;
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

		onlineMeeting = client()
				.communications()
				.onlineMeetings()
				.buildRequest()
				.post(onlineMeeting);
		log.info(Tracing.M_AUDIT, "Online-Meeting created (/communications) with id: {}", onlineMeeting.id);

		return onlineMeeting;
	}

	/**
	 * $filter=otherMails/any(x:x eq ‘xxx@abc.com’)
	 * 
	 * @param email The E-mail (mandatory)
	 * @param institutionalEmail The institutional E-mail (optional)
	 * @return The first users found
	 */
	public List<User> searchUsersByMail(String email, String institutionalEmail, TeamsErrors errors) {
		StringBuilder sb = new StringBuilder();
		sb.append("mail eq '").append(email).append("'")
		  .append(" or otherMails/any(x:x eq '").append(email).append("')");
		if(StringHelper.containsNonWhitespace(institutionalEmail)) {
			sb.append(" or mail eq '").append(institutionalEmail).append("'")
			  .append(" or otherMails/any(x:x eq '").append(institutionalEmail).append("')");
		}

		try {
			UserCollectionPage user = client()
					.users()
					.buildRequest()
					.filter(sb.toString())
					.select("displayName,id,mail,otherMails")
					.get();
			return user.getCurrentPage();
		} catch (ClientException | NullPointerException | IllegalArgumentException e) {
			log.error("Cannot find user with email: {} {}", email, institutionalEmail, e);
			errors.append(new TeamsError(TeamsErrorCodes.httpClientError));
			return new ArrayList<>();
		}
	}
	
	/**
	 * 
	 * @param mail
	 * @param issuer
	 * @return
	 */
	public User searchUserByUserPrincipalName(List<String> principals, TeamsErrors errors) {
		if(principals == null || principals.isEmpty()) return null;
		
		StringBuilder sb = new StringBuilder();
		for(String principal:principals) {
			if(sb.length() > 0) {
				sb.append(" or ");
			}
			sb.append("userPrincipalName eq '").append(principal).append("'");
		}

		try {
			UserCollectionPage user = client().users()
					.buildRequest()
					.filter(sb.toString())
					.select("displayName,id,mail,otherMails")
					.top(1)
					.get();
			
			List<User> users = user.getCurrentPage();
			return users.isEmpty() ? null : users.get(0);
		} catch (ClientException | NullPointerException | IllegalArgumentException e) {
			errors.append(new TeamsError(TeamsErrorCodes.httpClientError));
			log.error("Cannot find user with principal names", e);
			return null;
		}
	}
	
	public User searchUserById(String id, GraphServiceClient<Request> client, TeamsErrors errors) {
		try {
			return client
					.users(id)
					.buildRequest()
					.select("displayName,id,mail,otherMails")
					.get();
		} catch (ClientException | NullPointerException | IllegalArgumentException e) {
			errors.append(new TeamsError(e.getMessage(), ""));
			log.error("Cannot find user with id: {}", id, e);
			return null;
		}
	}
	
	public List<User> getAllUsers() {
		UserCollectionPage user = client()
				.users()
				.buildRequest()
				.select("displayName,id,mail,otherMails")
				.get();
		return user.getCurrentPage();
	}
	
	public Organization getOrganisation(String id, GraphServiceClient<Request> client) {
		return client
			.organization(id)
			.buildRequest()
			.select("id,displayName")
			.get();
	}
	
	public Application getApplication(String id, GraphServiceClient<Request> client, TeamsErrors errors) {
		try {
			ApplicationCollectionPage appsPage = client
				.applications()
				.buildRequest()
				.filter("appId eq '" + id + "'")
				.top(1)
				.get();
			
			List<Application> apps = appsPage.getCurrentPage();
			return apps == null || apps.isEmpty() ? null : apps.get(0);
		} catch (ClientException | NullPointerException | IllegalArgumentException e) {
			errors.append(new TeamsError(e.getMessage(), ""));
			log.error("", e);
			return null;
		}
	}
	
	public ConnectionInfos check(String clientId, String clientSecret, String tenantGuid,
			String producerId, TeamsErrors errors) {
		
		try {
			MicrosoftGraphAccessTokenManager accessTokenManager = new MicrosoftGraphAccessTokenManager(clientId, clientSecret, tenantGuid);
			AuthenticationTokenProvider authProvider = new AuthenticationTokenProvider(accessTokenManager);
			GraphServiceClient<Request> client = client(authProvider);
			
			Organization org = getOrganisation(tenantGuid, client);
			String organisation = org == null ? null : org.displayName;

			String producerDisplayName = null;
			if(StringHelper.containsNonWhitespace(producerId)) {
				User producer = searchUserById(producerId, client, errors);
				producerDisplayName = producer == null ? null : producer.displayName;
			}

			return new ConnectionInfos(organisation, producerDisplayName);
		} catch (ClientException e) {
			errors.append(extractMsalFrom(e));
			log.error("", e);
			return null;
		} catch (NullPointerException | IllegalArgumentException e) {
			errors.append(new TeamsError(TeamsErrorCodes.httpClientError));
			log.error("", e);
			return null;
		} catch (Exception e) {
			errors.append(new TeamsError(e.getMessage()));
			log.error("", e);
			return null;
		}
	}
	
	/**
	 * Not sure if we can use it everywhere, the message can perhaps hold
	 * some sensitive informations about the configuration.
	 * 
	 * @param ex The exception
	 * @return
	 */
	private TeamsError extractMsalFrom(ClientException ex) {
		int count = 0;// prevent some infinite loop
		Throwable e = ex;
		String message = ex.getMessage();
		
		do {
			if(e instanceof MsalException) {
				message = e.getMessage();
				if(message != null && message.indexOf('\n') >= 0) {
					message = message.substring(0, message.indexOf('\n'));
				}
				break;
			}
			e = e.getCause();
			count++;
		} while (e != null && count < 10);
		
		return new TeamsError(message);
	}
	
	public final ConnectionInfos check(TeamsErrors errors) {
		try {
			GraphServiceClient<Request> client = client();
			String tenantId = teamsModule.getTenantGuid();
			Organization org = getOrganisation(tenantId, client);
			String organisation = org == null ? null : org.displayName;
		
			String producerDisplayName = null;
			if(StringHelper.containsNonWhitespace(teamsModule.getProducerId())) {
				User producer = searchUserById(teamsModule.getProducerId(), client, errors);
				producerDisplayName = producer == null ? null : producer.displayName;
			}

			return new ConnectionInfos(organisation, producerDisplayName);
		} catch (ClientException | NullPointerException | IllegalArgumentException e) {
			errors.append(new TeamsError(TeamsErrorCodes.httpClientError));
			log.error("", e);
			return null;
		} catch (Exception e) {
			errors.append(new TeamsError(e.getMessage(), ""));
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
		return identitySet;
	}
	
	private MeetingParticipantInfo createParticipantInfo(IdentitySet identity, OnlineMeetingRole role) {
		MeetingParticipantInfo participantInfo = new MeetingParticipantInfo();
		participantInfo.identity = identity;
		participantInfo.role = role;
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
