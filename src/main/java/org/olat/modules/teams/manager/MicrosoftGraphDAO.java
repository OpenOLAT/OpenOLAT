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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsModule;
import org.olat.modules.teams.model.TeamsError;
import org.olat.modules.teams.model.TeamsErrorCodes;
import org.olat.modules.teams.model.TeamsErrors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.Identity;
import com.microsoft.graph.models.extensions.IdentitySet;
import com.microsoft.graph.models.extensions.ItemBody;
import com.microsoft.graph.models.extensions.MeetingParticipantInfo;
import com.microsoft.graph.models.extensions.MeetingParticipants;
import com.microsoft.graph.models.extensions.OnlineMeeting;
import com.microsoft.graph.models.extensions.User;
import com.microsoft.graph.models.generated.AccessLevel;
import com.microsoft.graph.models.generated.BodyType;
import com.microsoft.graph.models.generated.OnlineMeetingPresenters;
import com.microsoft.graph.models.generated.OnlineMeetingRole;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
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
	@Autowired
	private MicrosoftGraphAccessTokenManagerImpl accessTokenManager;
	
	public IGraphServiceClient client() {
		@SuppressWarnings("deprecation")
		IAuthenticationProvider authProvider = new AuthenticationTokenProvider(accessTokenManager);
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
	
	public OnlineMeeting createMeeting(TeamsMeeting meeting, User organizer, TeamsErrors errors) {
		MeetingParticipants participants = new MeetingParticipants();
		if(organizer != null) {
			IdentitySet identitySet = createIdentitySetById(organizer);
			participants.organizer = createParticipantInfo(identitySet, OnlineMeetingRole.PRESENTER);
		} else if(StringHelper.containsNonWhitespace(teamsModule.getProducerId())) {
			IdentitySet identitySet = createIdentitySetById(teamsModule.getProducerId());
			participants.organizer = createParticipantInfo(identitySet, OnlineMeetingRole.PRODUCER);
		} else {
			errors.append(new TeamsError(TeamsErrorCodes.organizerMissing));
			return null;
		}
		
		OnlineMeeting onlineMeeting = new OnlineMeeting();
		onlineMeeting.startDateTime = toCalendar(meeting.getStartDate());
		onlineMeeting.endDateTime = toCalendar(meeting.getEndDate());
		onlineMeeting.subject = meeting.getSubject();
		onlineMeeting.participants = participants;
		onlineMeeting.allowedPresenters = toOnlineMeetingPresenters(meeting.getAllowedPresenters());
		onlineMeeting.accessLevel = toAccessLevel(meeting.getAccessLevel());
		onlineMeeting.entryExitAnnouncement = Boolean.valueOf(meeting.isEntryExitAnnouncement());
		
		String joinInformations = meeting.getJoinInformation();
		if(StringHelper.containsNonWhitespace(joinInformations)) {
			ItemBody body = new ItemBody();
			if(StringHelper.isHtml(joinInformations)) {
				body.contentType = BodyType.HTML;
			} else {
				body.contentType = BodyType.TEXT;
			}
			body.content = joinInformations;
			onlineMeeting.joinInformation = body;
		}
		
		onlineMeeting = client()
				.communications()
				.onlineMeetings()
				.buildRequest()
				.post(onlineMeeting);
		
		log.info(Tracing.M_AUDIT, "Oneline-Meeting created with id: {}", onlineMeeting.id);
		return onlineMeeting;
	}

	/**
	 * @param email The E-mail (mandatory)
	 * @param institutionalEmail The institutional E-mail (optional)
	 * @return The first users found
	 */
	public List<User> searchUsersByMail(String email, String institutionalEmail) {
		StringBuilder sb = new StringBuilder();
		sb.append("mail eq '").append(email).append("'");
		if(StringHelper.containsNonWhitespace(institutionalEmail)) {
			sb.append(" or mail eq '").append(institutionalEmail).append("'");
		}

		IUserCollectionPage user = client()
				.users()
				.buildRequest()
				.filter(sb.toString())
				.select("displayName,id,mail")
				.get();
		return user.getCurrentPage();
	}

	public OnlineMeeting updateOnlineMeeting(TeamsMeeting meeting) {
		String id =  meeting.getOnlineMeetingId();
		
		OnlineMeeting onlineMeeting = new OnlineMeeting();
		onlineMeeting.startDateTime = toCalendar(meeting.getStartDate());
		onlineMeeting.endDateTime = toCalendar(meeting.getEndDate());
		onlineMeeting.subject = meeting.getSubject();

		OnlineMeeting updatedOnlineMeeting = client()
				.communications()
				.onlineMeetings(id)
				.buildRequest()
				.put(onlineMeeting);
		
		log.info(Tracing.M_AUDIT, "Oneline-Meeting updated with id: {}", id);
		return updatedOnlineMeeting;
	}
	
	public OnlineMeeting searchOnlineMeeting(String externalId) {
		return client()
				.users(teamsModule.getOnBehalfUserId())
				.onlineMeetings(externalId)
				.buildRequest()
				.get();
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
	
	private IdentitySet createIdentitySetById(User user) {
		IdentitySet identitySet = new IdentitySet();
		Identity userIdentity = new Identity();
		userIdentity.id = user.id;
		identitySet.user = userIdentity;

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
