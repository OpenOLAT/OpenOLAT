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
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.login.oauth.spi.MicrosoftAzureADFSProvider;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsModule;
import org.olat.modules.teams.TeamsService;
import org.olat.modules.teams.model.ConnectionInfos;
import org.olat.modules.teams.model.TeamsErrors;
import org.olat.modules.teams.model.TeamsMeetingImpl;
import org.olat.modules.teams.model.TeamsMeetingsSearchParameters;
import org.olat.repository.RepositoryEntry;
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

	@Autowired
	private DB dbInstance;
	@Autowired
	private TeamsModule teamsModule;
	@Autowired
	private MicrosoftGraphDAO graphDao;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private TeamsMeetingDAO teamsMeetingDao;
	@Autowired
	private TeamsMeetingQueries teamsMeetingQueries;
	
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

		return meeting;
	}
	
	@Override
	public void deleteMeeting(TeamsMeeting meeting) {
		if(meeting == null || meeting.getKey() == null) return;
		
		TeamsMeeting reloadedMeeting = teamsMeetingDao.loadByKey(meeting.getKey());
		if(reloadedMeeting != null) {
			teamsMeetingDao.deleteMeeting(reloadedMeeting);
		}
	}

	@Override
	public boolean isMeetingRunning(TeamsMeeting meeting) {
		return meeting != null && StringHelper.containsNonWhitespace(meeting.getOnlineMeetingJoinUrl());
	}

	@Override
	public TeamsMeeting joinMeeting(TeamsMeeting meeting, Identity identity, boolean presenter, TeamsErrors errors) {
		meeting = teamsMeetingDao.loadByKey(meeting.getKey());
		if(meeting != null && !StringHelper.containsNonWhitespace(meeting.getOnlineMeetingId())) {
			dbInstance.commitAndCloseSession();
			User gPresenter = lookupUser(identity);
			OnlineMeeting onlineMeeting = graphDao.createMeeting(meeting, gPresenter, errors);
			if(onlineMeeting != null) {
				((TeamsMeetingImpl)meeting).setOnlineMeetingId(onlineMeeting.id);
				((TeamsMeetingImpl)meeting).setOnlineMeetingJoinUrl(onlineMeeting.joinUrl);
				meeting = teamsMeetingDao.updateMeeting(meeting);
			}
		} else if(StringHelper.containsNonWhitespace(teamsModule.getOnBehalfUserId())) {
			dbInstance.commitAndCloseSession();
			User graphUser = lookupUser(identity);
			if(graphUser != null) {
				OnlineMeetingRole role = presenter ? OnlineMeetingRole.PRESENTER : OnlineMeetingRole.ATTENDEE;
				graphDao.updateOnlineMeeting(meeting, graphUser, role);
			}
		}
		return meeting;
	}
	
	@Override
	public User lookupUser(Identity identity) {
		String email = identity.getUser().getProperty(UserConstants.EMAIL, null);
		String institutionalEmail = identity.getUser().getProperty(UserConstants.INSTITUTIONALEMAIL, null);
		List<User> users = graphDao.searchUsersByMail(email, institutionalEmail);
		if(users.size() == 1) {
			return users.get(0);
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
		}
		return null;
	}

	@Override
	public ConnectionInfos checkConnection() {
		return graphDao.check();
	}

	@Override
	public ConnectionInfos checkConnection(String clientId, String clientSecret, String tenantGuid,
			String applicationId, String producerId, String onBehalfId) {
		return graphDao.check(clientId, clientSecret, tenantGuid, applicationId, producerId, onBehalfId);
	}
}
