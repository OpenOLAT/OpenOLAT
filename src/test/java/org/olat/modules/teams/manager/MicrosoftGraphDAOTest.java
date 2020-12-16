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

import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsModule;
import org.olat.modules.teams.model.ConnectionInfos;
import org.olat.modules.teams.model.TeamsErrors;
import org.olat.modules.teams.model.TeamsMeetingImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.graph.models.extensions.OnlineMeeting;
import com.microsoft.graph.models.extensions.User;
import com.microsoft.graph.models.generated.AccessLevel;
import com.microsoft.graph.models.generated.LobbyBypassScope;
import com.microsoft.graph.models.generated.OnlineMeetingPresenters;
import com.microsoft.graph.models.generated.OnlineMeetingRole;

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
	private TeamsModule teamsModule;
	@Autowired
	private TeamsMeetingDAO teamsMeetingDao;
	@Autowired
	private MicrosoftGraphDAO microsoftGraphDao;
	
	@Test
	public void aTeamsModule() {
		Assume.assumeTrue(StringHelper.containsNonWhitespace(teamsModule.getApiKey()));
		
		Assert.assertNotNull(teamsModule.getApiKey());
		Assert.assertNotNull(teamsModule.getApiSecret());
		Assert.assertNotNull(teamsModule.getTenantGuid());
	}
	
	@Test
	public void login() {
		Assume.assumeTrue(StringHelper.containsNonWhitespace(teamsModule.getApiKey()));
		
		ConnectionInfos infos = microsoftGraphDao.check();
		Assert.assertNotNull(infos);
		Assert.assertNotNull(infos.getOrganisation());
		
		if(StringHelper.containsNonWhitespace(teamsModule.getProducerId())) {
			Assert.assertNotNull(infos.getProducerDisplayName());
		}
		if(StringHelper.containsNonWhitespace(teamsModule.getOnBehalfUserId())) {
			Assert.assertNotNull(infos.getOnBehalfDisplayName());
		}
	}
	
	@Test
	public void createUpdateDeleteOnBehalf() {
		Assume.assumeTrue(StringHelper.containsNonWhitespace(teamsModule.getApiKey()));
		Assume.assumeTrue(StringHelper.containsNonWhitespace(teamsModule.getOnBehalfUserId()));
		
		// create an OpenOlat meeting
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String name = "Real-Online-Meeting - 1";
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("teams-1");
		
		Date start = DateUtils.addMinutes(new Date(), 5);
		Date end = DateUtils.addMinutes(start, 5);
		TeamsMeeting meeting = teamsMeetingDao.createMeeting(name, start, end, entry, null, null, creator);
		meeting.setAccessLevel(AccessLevel.SAME_ENTERPRISE_AND_FEDERATED.name());
		meeting.setAllowedPresenters(OnlineMeetingPresenters.ROLE_IS_PRESENTER.name());
		meeting.setLobbyBypassScope(LobbyBypassScope.ORGANIZER.name());
		meeting = teamsMeetingDao.updateMeeting(meeting);
		dbInstance.commitAndCloseSession();
		
		// create the online meeting
		String userId = teamsModule.getOnBehalfUserId();
		User user = new User();
		user.id = userId;	
		TeamsErrors errors = new TeamsErrors();
		OnlineMeeting onlineMeeting = microsoftGraphDao.createMeeting(meeting, user, OnlineMeetingRole.PRESENTER, errors);
		Assert.assertNotNull(onlineMeeting);
		Assert.assertNotNull(onlineMeeting.id);
		Assert.assertNotNull(onlineMeeting.joinUrl);
		Assert.assertEquals(name, onlineMeeting.subject);
		Assert.assertEquals(OnlineMeetingPresenters.ROLE_IS_PRESENTER, onlineMeeting.allowedPresenters);
		Assert.assertEquals(LobbyBypassScope.ORGANIZER, onlineMeeting.lobbyBypassSettings.scope);
		Assert.assertEquals(Boolean.FALSE, onlineMeeting.lobbyBypassSettings.isDialInBypassEnabled);
		
		// update the meeting
		String updateName = "Update-Online-Meeting - 1";
		meeting.setSubject(updateName);
		((TeamsMeetingImpl)meeting).setOnlineMeetingId(onlineMeeting.id);
		((TeamsMeetingImpl)meeting).setOnlineMeetingJoinUrl(onlineMeeting.joinUrl);
		meeting = teamsMeetingDao.updateMeeting(meeting);
		dbInstance.commitAndCloseSession();

		// update the online meeting
		OnlineMeeting updatedOnlineMeeting =  microsoftGraphDao.updateOnlineMeeting(meeting, user, OnlineMeetingRole.PRESENTER);
		Assert.assertNotNull(updatedOnlineMeeting);
		Assert.assertNotNull(updatedOnlineMeeting.id);
		Assert.assertNotNull(updatedOnlineMeeting.joinUrl);
		Assert.assertEquals(updateName, updatedOnlineMeeting.subject);
		
		// delete the online meeting
		microsoftGraphDao.delete(updatedOnlineMeeting.id);
	}

}
