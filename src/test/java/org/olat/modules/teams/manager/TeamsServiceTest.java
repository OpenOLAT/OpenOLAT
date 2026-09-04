/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.teams.manager;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsRecording;
import org.olat.modules.teams.TeamsService;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.graph.models.CallRecording;

/**
 * 
 * Initial date: 27 août 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class TeamsServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TeamsService teamsService;
	@Autowired
	private TeamsMeetingDAO teamsMeetingDao;
	@Autowired
	private TeamsRecordingDAO teamsRecordingDao;
	@Autowired
	private OrganisationService organisationService;
	
	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-teams-test", "Org-teams-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void deleteMeetingWithRecording() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("teams-1", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		String name = "Online-Recording - 2";
		String subIdent = UUID.randomUUID().toString();
		
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("teams-rec-2");
		TeamsMeeting meeting = teamsMeetingDao.createMeeting(name, new Date(), new Date(),
				entry, subIdent, null, creator);

		TeamsRecording recording1 = teamsRecordingDao.createRecording(meeting, createCallRecording());
		TeamsRecording recording2 = teamsRecordingDao.createRecording(meeting, createCallRecording());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(recording2);
		
		teamsService.deleteMeeting(meeting);
		dbInstance.commitAndCloseSession();
		
		TeamsMeeting deletedMeeting = teamsMeetingDao.loadByKey(meeting.getKey());
		Assert.assertNull(deletedMeeting);
		TeamsRecording deletedRecording1 = teamsRecordingDao.loadRecordingByKey(recording1.getKey());
		Assert.assertNull(deletedRecording1);
	}

	private CallRecording createCallRecording() {
		CallRecording rec = new CallRecording();
		rec.setId(UUID.randomUUID().toString());
		rec.setCreatedDateTime(OffsetDateTime.now());
		rec.setEndDateTime(OffsetDateTime.now());
		return rec;
	}
}
