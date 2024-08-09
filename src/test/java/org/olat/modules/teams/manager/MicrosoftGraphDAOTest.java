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

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.olat.basesecurity.model.OAuth2TokensImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.login.oauth.OAuthLoginModule;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.model.TeamsErrors;
import org.olat.modules.teams.model.TeamsMeetingImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.graph.models.LobbyBypassScope;
import com.microsoft.graph.models.OnlineMeeting;
import com.microsoft.graph.models.OnlineMeetingPresenters;
import com.microsoft.graph.models.OnlineMeetingRole;
import com.microsoft.graph.models.User;

/**
 * 
 * Initial date: 16 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MicrosoftGraphDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private TeamsMeetingDAO teamsMeetingDao;
	@Autowired
	private OAuthLoginModule oauthLoginModule;
	@Autowired
	private MicrosoftGraphDAO microsoftGraphDao;
	
	@Test
	public void canAttendeeOpenMeeting() {
		TeamsMeetingImpl test = new TeamsMeetingImpl();
		// everyone
		test.setAllowedPresenters(OnlineMeetingPresenters.Everyone.name());
		test.setLobbyBypassScope(LobbyBypassScope.Everyone.name());
		Assert.assertTrue(MicrosoftGraphDAO.canAttendeeOpenMeeting(test));
		
		test.setAllowedPresenters(OnlineMeetingPresenters.Organization.name());
		test.setLobbyBypassScope(LobbyBypassScope.Organizer.name());
		Assert.assertFalse(MicrosoftGraphDAO.canAttendeeOpenMeeting(test));
	}
	
	@Test
	public void canAttendeeOpenMeetingCompatibilityMode() {
		TeamsMeetingImpl test = new TeamsMeetingImpl();
		// everyone
		test.setAllowedPresenters("EVERYONE");
		test.setLobbyBypassScope("EVERYONE");
		Assert.assertTrue(MicrosoftGraphDAO.canAttendeeOpenMeeting(test));
		
		test.setAllowedPresenters("ORGANIZATION");
		test.setLobbyBypassScope("ORGANIZER");
		Assert.assertFalse(MicrosoftGraphDAO.canAttendeeOpenMeeting(test));
	}
	
	@Test
	public void canAttendeeOpenMeetingCheckNulls() {
		Assert.assertFalse(MicrosoftGraphDAO.canAttendeeOpenMeeting(null));
		
		TeamsMeetingImpl test = new TeamsMeetingImpl();
		Assert.assertFalse(MicrosoftGraphDAO.canAttendeeOpenMeeting(test));
		test.setAllowedPresenters(OnlineMeetingPresenters.Everyone.name());
		Assert.assertFalse(MicrosoftGraphDAO.canAttendeeOpenMeeting(test));
		test.setLobbyBypassScope(LobbyBypassScope.Everyone.name());
		Assert.assertTrue(MicrosoftGraphDAO.canAttendeeOpenMeeting(test));
	}
	
	@Test
	public void createUpdateDeleteOnBehalf() throws Exception {
		String refreshToken = System.getProperty("test.env.azure.adfs.refresh.token");
		Assume.assumeTrue(StringHelper.containsNonWhitespace(oauthLoginModule.getAzureAdfsApiKey()));
		Assume.assumeTrue(StringHelper.containsNonWhitespace(refreshToken));
		
		// create an OpenOlat meeting
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String name = "Real-Online-Meeting - 1";
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("teams-1");
		OAuth2TokensImpl oauth2Tokens = new OAuth2TokensImpl();
		oauth2Tokens.setRefreshToken(refreshToken);
		
		Date start = DateUtils.addHours(new Date(), 3);
		Date end = DateUtils.addMinutes(start, 15);
		TeamsMeeting meeting = teamsMeetingDao.createMeeting(name, start, end, entry, null, null, creator);
		meeting.setAccessLevel("EVERYONE");
		meeting.setAllowedPresenters(OnlineMeetingPresenters.Everyone.name());
		meeting.setLobbyBypassScope(LobbyBypassScope.Everyone.name());
		meeting = teamsMeetingDao.updateMeeting(meeting);
		dbInstance.commitAndCloseSession();
		
		// create the online meeting
		User user = microsoftGraphDao.getMe(oauth2Tokens);				
		TeamsErrors errors = new TeamsErrors();
		OnlineMeeting onlineMeeting = microsoftGraphDao.createMeeting(meeting, user, OnlineMeetingRole.Presenter, oauth2Tokens, errors);
		Assert.assertNotNull(onlineMeeting);
		Assert.assertNotNull(onlineMeeting.getId());
		Assert.assertNotNull(onlineMeeting.getJoinWebUrl());
		Assert.assertEquals(name, onlineMeeting.getSubject());
		
		// update the meeting
		String updateName = "Update-Online-Meeting - 1";
		meeting.setSubject(updateName);
		((TeamsMeetingImpl)meeting).setOnlineMeetingId(onlineMeeting.getId());
		((TeamsMeetingImpl)meeting).setOnlineMeetingJoinUrl(onlineMeeting.getJoinWebUrl());
		meeting = teamsMeetingDao.updateMeeting(meeting);
		dbInstance.commitAndCloseSession();
	}

}
