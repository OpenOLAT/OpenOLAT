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
package org.olat.modules.bigbluebutton.manager;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonRecording;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingReference;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingsPublishedRoles;
import org.olat.modules.bigbluebutton.model.BigBlueButtonRecordingImpl;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 août 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonRecordingReferenceDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private BigBlueButtonMeetingDAO bigBlueButtonMeetingDao;
	@Autowired
	private BigBlueButtonRecordingReferenceDAO bigBlueButtonRecordingReferenceDao;
	
	@Test
	public void createReference() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bbb-record-1");
		BusinessGroup group = businessGroupDao.createAndPersist(null, "BBB Recording 1", "bbb-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		BigBlueButtonMeeting meeting = bigBlueButtonMeetingDao.createAndPersistMeeting("Record all - 1", null, null, group, id);
		dbInstance.commit();
		
		BigBlueButtonRecording recording = BigBlueButtonRecordingImpl.valueOf(UUID.randomUUID().toString(), "Recorded", UUID.randomUUID().toString(),
				new Date(), new Date(), "http://button.openolat.com", "presentation");
		BigBlueButtonRecordingReference reference = bigBlueButtonRecordingReferenceDao.createReference(recording, meeting, null);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(reference);
		Assert.assertNotNull(reference.getCreationDate());
		Assert.assertNotNull(reference.getLastModified());
		Assert.assertNotNull(reference.getStartDate());
		Assert.assertNotNull(reference.getEndDate());
		Assert.assertEquals(meeting, reference.getMeeting());
		Assert.assertEquals("presentation", reference.getType());
		Assert.assertEquals("http://button.openolat.com", reference.getUrl());
	}
	
	@Test
	public void getRecordingReferences() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bbb-record-2");
		BusinessGroup group = businessGroupDao.createAndPersist(null, "BBB Recording 2", "bbb-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		BigBlueButtonMeeting meeting = bigBlueButtonMeetingDao.createAndPersistMeeting("Recording - 2", null, null, group, id);

		BigBlueButtonRecording recording = BigBlueButtonRecordingImpl.valueOf(UUID.randomUUID().toString(), "Recorded always", UUID.randomUUID().toString(),
				new Date(), new Date(), "http://button.openolat.com/2", "presentation");
		BigBlueButtonRecordingReference reference = bigBlueButtonRecordingReferenceDao.createReference(recording, meeting, null);
		dbInstance.commitAndCloseSession();
		
		List<BigBlueButtonRecordingReference> references = bigBlueButtonRecordingReferenceDao.getRecordingReferences(meeting);
		Assert.assertNotNull(references);
		Assert.assertEquals(1, references.size());
		Assert.assertEquals(reference, references.get(0));
		
		BigBlueButtonRecordingReference reloadReference = references.get(0);
		Assert.assertNotNull(reloadReference);
		Assert.assertNotNull(reloadReference.getCreationDate());
		Assert.assertNotNull(reloadReference.getLastModified());
		Assert.assertNotNull(reloadReference.getStartDate());
		Assert.assertNotNull(reloadReference.getEndDate());
		Assert.assertEquals(meeting, reloadReference.getMeeting());
		Assert.assertEquals("presentation", reloadReference.getType());
		Assert.assertEquals("http://button.openolat.com/2", reloadReference.getUrl());
	}
	
	@Test
	public void getRecordingReferencesOfMeetings() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bbb-record-3");
		BusinessGroup group = businessGroupDao.createAndPersist(null, "BBB Recording 3", "bbb-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		BigBlueButtonMeeting meeting1 = bigBlueButtonMeetingDao.createAndPersistMeeting("Recording - 3.1", null, null, group, id);
		BigBlueButtonMeeting meeting2 = bigBlueButtonMeetingDao.createAndPersistMeeting("Recording - 3.2", null, null, group, id);
		BigBlueButtonMeeting meeting3 = bigBlueButtonMeetingDao.createAndPersistMeeting("Recording - 3.3", null, null, group, id);

		BigBlueButtonRecording recording11 = BigBlueButtonRecordingImpl.valueOf(UUID.randomUUID().toString(), "Recorded always", UUID.randomUUID().toString(),
				new Date(), new Date(), "http://button.openolat.com/2", "presentation");
		BigBlueButtonRecordingReference reference11 = bigBlueButtonRecordingReferenceDao.createReference(recording11, meeting1, null);
		BigBlueButtonRecording recording12 = BigBlueButtonRecordingImpl.valueOf(UUID.randomUUID().toString(), "Recorded always", UUID.randomUUID().toString(),
				new Date(), new Date(), "http://button.openolat.com/2", "presentation");
		BigBlueButtonRecordingReference reference12 = bigBlueButtonRecordingReferenceDao.createReference(recording12, meeting1, null);
		BigBlueButtonRecording recording21 = BigBlueButtonRecordingImpl.valueOf(UUID.randomUUID().toString(), "Recorded always", UUID.randomUUID().toString(),
				new Date(), new Date(), "http://button.openolat.com/2", "presentation");
		BigBlueButtonRecordingReference reference21 = bigBlueButtonRecordingReferenceDao.createReference(recording21, meeting2, null);
		BigBlueButtonRecording recording31 = BigBlueButtonRecordingImpl.valueOf(UUID.randomUUID().toString(), "Recorded always", UUID.randomUUID().toString(),
				new Date(), new Date(), "http://button.openolat.com/2", "presentation");
		BigBlueButtonRecordingReference reference31 = bigBlueButtonRecordingReferenceDao.createReference(recording31, meeting3, null);
		dbInstance.commitAndCloseSession();
		
		List<BigBlueButtonRecordingReference> recordingReferences = bigBlueButtonRecordingReferenceDao.getRecordingReferences(Arrays.asList(meeting1, meeting2));
		Assert.assertTrue(recordingReferences.contains(reference11));
		Assert.assertTrue(recordingReferences.contains(reference12));
		Assert.assertTrue(recordingReferences.contains(reference21));
		Assert.assertFalse(recordingReferences.contains(reference31));
	}
	
	@Test
	public void loadRecordingReferenceByKey() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bbb-record-4");
		BusinessGroup group = businessGroupDao.createAndPersist(null, "BBB Recording 4", "A description", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, true, true, false);
		BigBlueButtonMeeting meeting = bigBlueButtonMeetingDao.createAndPersistMeeting("Recording - 4", null, null, group, id);

		BigBlueButtonRecording recording = BigBlueButtonRecordingImpl.valueOf(UUID.randomUUID().toString(), "Recorded always", UUID.randomUUID().toString(),
				new Date(), new Date(), "http://button.openolat.com/2", "presentation");
		BigBlueButtonRecordingReference reference = bigBlueButtonRecordingReferenceDao.createReference(recording, meeting,
				new BigBlueButtonRecordingsPublishedRoles[] { BigBlueButtonRecordingsPublishedRoles.all });
		dbInstance.commitAndCloseSession();
		
		BigBlueButtonRecordingReference reloadReference = bigBlueButtonRecordingReferenceDao.loadRecordingReferenceByKey(reference.getKey());
		Assert.assertEquals(reference, reloadReference);
	}
	
	@Test
	public void deleteRecordingReferences() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bbb-record-5");
		BusinessGroup group = businessGroupDao.createAndPersist(null, "BBB Recording 5", "Several recordings", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		BigBlueButtonMeeting meeting1 = bigBlueButtonMeetingDao.createAndPersistMeeting("Recording - 5.1", null, null, group, id);
		BigBlueButtonMeeting meeting2 = bigBlueButtonMeetingDao.createAndPersistMeeting("Recording - 5.2", null, null, group, id);
		
		BigBlueButtonRecording recording11 = BigBlueButtonRecordingImpl.valueOf(UUID.randomUUID().toString(), "Recorded A", UUID.randomUUID().toString(),
				new Date(), new Date(), "http://button.openolat.com/5", "presentation");
		BigBlueButtonRecordingReference reference11 = bigBlueButtonRecordingReferenceDao.createReference(recording11, meeting1, null);
		BigBlueButtonRecording recording12 = BigBlueButtonRecordingImpl.valueOf(UUID.randomUUID().toString(), "Recorded B", UUID.randomUUID().toString(),
				new Date(), new Date(), "http://button.openolat.com/6", "presentation");
		BigBlueButtonRecordingReference reference12 = bigBlueButtonRecordingReferenceDao.createReference(recording12, meeting1, null);
		BigBlueButtonRecording recording21 = BigBlueButtonRecordingImpl.valueOf(UUID.randomUUID().toString(), "Recorded C", UUID.randomUUID().toString(),
				new Date(), new Date(), "http://button.openolat.com/7", "presentation");
		BigBlueButtonRecordingReference reference21 = bigBlueButtonRecordingReferenceDao.createReference(recording21, meeting2, null);
		dbInstance.commitAndCloseSession();
		
		bigBlueButtonRecordingReferenceDao.deleteRecordingReferences(meeting1);
		dbInstance.commitAndCloseSession();
		
		List<BigBlueButtonRecordingReference> recordingReferences = bigBlueButtonRecordingReferenceDao.getRecordingReferences(Arrays.asList(meeting1, meeting2));
		Assert.assertFalse(recordingReferences.contains(reference11));
		Assert.assertFalse(recordingReferences.contains(reference12));
		Assert.assertTrue(recordingReferences.contains(reference21));
	}
}
