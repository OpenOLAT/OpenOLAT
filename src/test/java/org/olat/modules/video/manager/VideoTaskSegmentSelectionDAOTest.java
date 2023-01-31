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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.video.VideoTaskSegmentSelection;
import org.olat.modules.video.VideoTaskSession;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskSegmentSelectionDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VideoTaskSessionDAO taskSessionDao;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private VideoTaskSegmentSelectionDAO taskSegmentSelectionDao;
	
	/**
	 * Both the reference entries are the video repository entry
	 */
	@Test
	public void createTaskSessionSelection() {
		// Prepare a test and a user
		RepositoryEntry videoEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("vsession-1");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, videoEntry, "-", null, videoEntry);

		VideoTaskSession taskSession = taskSessionDao.createAndPersistTaskSession(videoEntry, videoEntry,
				"-", assessmentEntry, assessedIdentity, null, 1, true);
		VideoTaskSegmentSelection selection = taskSegmentSelectionDao.createSegmentSelection(taskSession,
				"blue-segment", "purple-category", Boolean.TRUE, 12000, "00:00:12");
		dbInstance.commit();
		
		Assert.assertNotNull(selection);
		Assert.assertNotNull(selection.getKey());
		Assert.assertNotNull(selection.getCreationDate());
		Assert.assertNotNull(selection.getLastModified());
		Assert.assertEquals(Long.valueOf(12000l), selection.getTime());
		Assert.assertEquals(Boolean.TRUE, selection.getCorrect());
		Assert.assertEquals("00:00:12", selection.getRawTime());
		Assert.assertEquals("blue-segment", selection.getSegmentId());
		Assert.assertEquals("purple-category", selection.getCategoryId());
		Assert.assertEquals(taskSession, selection.getTaskSession());
	}
	
	@Test
	public void getSegmentSelection() {
		// Prepare a test and a user
		RepositoryEntry videoEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("vsession-1");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, videoEntry, "-", null, videoEntry);

		VideoTaskSession taskSession = taskSessionDao.createAndPersistTaskSession(videoEntry, videoEntry,
				"-", assessmentEntry, assessedIdentity, null, 1, true);
		VideoTaskSegmentSelection selection = taskSegmentSelectionDao.createSegmentSelection(taskSession,
				"segment-1", "green", Boolean.FALSE, 12000, "00:00:12");
		dbInstance.commitAndCloseSession();
		
		List<VideoTaskSegmentSelection> selections = taskSegmentSelectionDao.getSegmentSelection(List.of(taskSession));
		assertThat(selections)
			.hasSize(1)
			.containsExactly(selection);
	}

}
