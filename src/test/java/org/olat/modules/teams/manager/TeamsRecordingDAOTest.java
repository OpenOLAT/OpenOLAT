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

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsRecording;
import org.olat.modules.teams.TeamsRecordingStatusEnum;
import org.olat.modules.teams.model.TeamsRecordingImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.graph.models.CallRecording;

/**
 * 
 * Initial date: 25 août 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class TeamsRecordingDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TeamsMeetingDAO teamsMeetingDao;
	@Autowired
	private TeamsRecordingDAO teamsRecordingDao;
	
	@Test
	public void createRecording() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String name = "Online-Recording - 1";
		String subIdent = UUID.randomUUID().toString();
		
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("teams-rec-1");
		TeamsMeeting meeting = teamsMeetingDao.createMeeting(name, new Date(), new Date(),
				entry, subIdent, null, creator);

		CallRecording cRecording = createCallRecording();
		TeamsRecording recording = teamsRecordingDao.createRecording(meeting, cRecording);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(recording);
		Assert.assertNotNull(recording.getKey());
		Assert.assertNotNull(recording.getCreationDate());
		Assert.assertNotNull(recording.getLastModified());
		Assert.assertNotNull(recording.getStartDate());
		Assert.assertNotNull(recording.getEndDate());
		Assert.assertEquals(cRecording.getId(), recording.getRecordingId());
		Assert.assertEquals(TeamsRecordingStatusEnum.PENDING, recording.getStatus());
		Assert.assertEquals(meeting, recording.getMeeting());
	}
	
	@Test
	public void getRecordingsByMeeting() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String name = "Online-Recording - 2";
		String subIdent = UUID.randomUUID().toString();
		
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("teams-rec-2");
		TeamsMeeting meeting = teamsMeetingDao.createMeeting(name, new Date(), new Date(),
				entry, subIdent, null, creator);

		TeamsRecording recording1 = teamsRecordingDao.createRecording(meeting, createCallRecording());
		TeamsRecording recording2 = teamsRecordingDao.createRecording(meeting, createCallRecording());
		dbInstance.commitAndCloseSession();
		
		List<TeamsRecording> recordings = teamsRecordingDao.getRecordings(meeting);
		Assertions.assertThat(recordings)
			.hasSize(2)
			.containsExactlyInAnyOrder(recording1, recording2);
	}
	
	@Test
	public void getRecordingsToDelete() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String name = "Online-Recording - 2";
		String subIdent = UUID.randomUUID().toString();
		
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("teams-rec-2");
		TeamsMeeting meeting = teamsMeetingDao.createMeeting(name, new Date(), new Date(),
				entry, subIdent, null, creator);

		TeamsRecording recording = teamsRecordingDao.createRecording(meeting, createCallRecording());
		dbInstance.commit();
		
		((TeamsRecordingImpl)recording).setStartDate(DateUtils.addDays(new Date(), -8));
		((TeamsRecordingImpl)recording).setEndDate(DateUtils.addHours(recording.getStartDate(), 1));
		recording = teamsRecordingDao.updateRecording(recording);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(recording);
		
		LocalDateTime referenceDate = LocalDateTime.now().minusDays(5);
		List<Long> recordingsKeys = teamsRecordingDao.getRecordingsToDelete(referenceDate);
		Assertions.assertThat(recordingsKeys)
			.isNotNull()
			.hasSizeGreaterThanOrEqualTo(1)
			.contains(recording.getKey());
	}
	
	private CallRecording createCallRecording() {
		CallRecording rec = new CallRecording();
		rec.setId(UUID.randomUUID().toString());
		rec.setCreatedDateTime(OffsetDateTime.now());
		rec.setEndDateTime(OffsetDateTime.now());
		return rec;
	}
}
