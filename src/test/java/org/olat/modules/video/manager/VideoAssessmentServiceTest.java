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
package org.olat.modules.video.manager;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.video.VideoAssessmentService;
import org.olat.modules.video.VideoTaskSession;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoAssessmentServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private VideoAssessmentService videoAssessmentService;
	
	@Test
	public void hasService() {
		Assert.assertNotNull(videoAssessmentService);
	}
	
	/**
	 * Both the reference entries are the video repository entry
	 */
	@Test
	public void createTaskSession() {
		// prepare a test and a user
		RepositoryEntry videoEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("vsession-1");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, videoEntry, "-", null, videoEntry);
		dbInstance.commit();
		
		VideoTaskSession taskSession = videoAssessmentService.createTaskSession(assessedIdentity, null, assessmentEntry, videoEntry, null, videoEntry, true);
		Assert.assertNotNull(taskSession);
		dbInstance.commit();
		
		Assert.assertNotNull(taskSession.getCreationDate());
		Assert.assertNotNull(taskSession.getLastModified());
		Assert.assertEquals(1l, taskSession.getAttempt());
		Assert.assertEquals(assessedIdentity, taskSession.getIdentity());
		Assert.assertEquals(assessmentEntry, taskSession.getAssessmentEntry());
		Assert.assertEquals(videoEntry, taskSession.getVideoEntry());
		Assert.assertEquals(videoEntry, taskSession.getRepositoryEntry());
	}
	
	@Test
	public void deleteTaskSession() {
		// prepare a test and a user
		RepositoryEntry videoEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("vsession-2");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, videoEntry, "-", null, videoEntry);
		dbInstance.commit();
		
		String subIdent = "sub-ident-" + assessedIdentity.getKey();
		VideoTaskSession taskSession = videoAssessmentService.createTaskSession(assessedIdentity, null, assessmentEntry, videoEntry, subIdent, videoEntry, true);
		videoAssessmentService.createTaskSegmentSelection(taskSession, "seg-1", "cat-1", Boolean.TRUE, 10000, "00:00:10");
		videoAssessmentService.createTaskSegmentSelection(taskSession, "seg-2", "cat-1", Boolean.TRUE, 10000, "00:00:10");
		videoAssessmentService.createTaskSegmentSelection(taskSession, "seg-3", "cat-1", Boolean.TRUE, 10000, "00:00:10");
		Assert.assertNotNull(taskSession);
		dbInstance.commitAndCloseSession();
		
		long deletedRows = videoAssessmentService.deleteTaskSessions(videoEntry, subIdent);;
		Assert.assertEquals(4l, deletedRows);
	}

}
