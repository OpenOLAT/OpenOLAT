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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 août 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class TeamsRecordingStorageTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TeamsMeetingDAO teamsMeetingDao;
	@Autowired
	private TeamsRecordingStorage teamsRecordingStorage;
	
	@Test
	public void createMeeting() throws IOException {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String name = "Online-Meeting - 1";
		String subIdent = UUID.randomUUID().toString();
		
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("teams-1");
		TeamsMeeting meeting = teamsMeetingDao.createMeeting(name, new Date(), new Date(),
				entry, subIdent, null, creator);
		dbInstance.commitAndCloseSession();
		
		VFSLeaf recording = null;
		URL videoUrl = JunitTestHelper.class.getResource("file_resources/big_buck_bunny.mp4");
		try(InputStream stream = videoUrl.openStream()) {
			String filename = "Test-recording-" + CodeHelper.getUniqueID() + ".mp4";
			recording = teamsRecordingStorage.storeRecording(meeting, filename, creator, stream);
		} catch(IOException e) {
			throw e;
		}
		
		Assert.assertNotNull(recording);
		Assert.assertTrue(recording.exists());
		Assert.assertTrue(recording.getSize() > 1000);
	}

}
