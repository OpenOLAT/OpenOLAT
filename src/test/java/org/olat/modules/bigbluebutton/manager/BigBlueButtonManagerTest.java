/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.bigbluebutton.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.prefs.PreferencesFactory;
import org.olat.core.util.prefs.gui.GuiPreferenceService;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.BigBlueButtonRecording;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingReference;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingsHandler;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.modules.bigbluebutton.model.BigBlueButtonRecordingImpl;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonMeetingController;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 janv. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class BigBlueButtonManagerTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private GuiPreferenceService guiPreferenceService;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	@Autowired
	private BigBlueButtonMeetingDAO bigBlueButtonMeetingDao;
	@Autowired
	private BigBlueButtonUnitTestRecordingsHandler unitTestRecordingsHandler;
	@Autowired
	private BigBlueButtonRecordingReferenceDAO bigBlueButtonRecordingReferenceDao;
	
	@Test
	public void checkUnitTestsRecordingsHandler() {
		String currentHandlerId = bigBlueButtonModule.getRecordingHandlerId();
		bigBlueButtonModule.setRecordingHandlerId("unittests");
		
		BigBlueButtonRecordingsHandler recordingsHandler = bigBlueButtonManager.getRecordingsHandler();
		Assert.assertEquals(unitTestRecordingsHandler, recordingsHandler);
		List<BigBlueButtonRecordingsHandler> recordingHandlers = bigBlueButtonManager.getRecordingsHandlers();
		Assert.assertEquals(3, recordingHandlers.size());

		bigBlueButtonModule.setRecordingHandlerId(currentHandlerId);
	}
	
	@Test
	public void deleteMeetingsPermanentRecording() {
		String currentHandlerId = bigBlueButtonModule.getRecordingHandlerId();
		bigBlueButtonModule.setRecordingsPermanent(true);
		bigBlueButtonModule.setRecordingHandlerId("unittests");
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bbb-recording-1");
		BusinessGroup group = businessGroupDao.createAndPersist(null, "BBB Recording 1", "Delete recording or not", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		BigBlueButtonMeeting meeting = bigBlueButtonMeetingDao.createAndPersistMeeting("Recording saved - 1", null, null, group, id);
		dbInstance.commit();
		
		// default
		BigBlueButtonRecording recordingDef = BigBlueButtonRecordingImpl.valueOf(UUID.randomUUID().toString(), "Recorded", UUID.randomUUID().toString(),
				new Date(), new Date(), "http://button.openolat.com/1", "presentation");
		bigBlueButtonRecordingReferenceDao.createReference(recordingDef, meeting, null);
		
		// flagged explicitly as not permanent
		BigBlueButtonRecording recordingNot = BigBlueButtonRecordingImpl.valueOf(UUID.randomUUID().toString(), "Recorded", UUID.randomUUID().toString(),
				new Date(), new Date(), "http://button.openolat.com/2", "presentation");
		BigBlueButtonRecordingReference referenceNot = bigBlueButtonRecordingReferenceDao.createReference(recordingNot, meeting, null);
		referenceNot.setPermanent(Boolean.FALSE);
		referenceNot = bigBlueButtonRecordingReferenceDao.updateRecordingReference(referenceNot);

		// flagged explicitly as permanent
		BigBlueButtonRecording recordingPermanent = BigBlueButtonRecordingImpl.valueOf(UUID.randomUUID().toString(), "Recorded", UUID.randomUUID().toString(),
				new Date(), new Date(), "http://button.openolat.com/3", "presentation");
		BigBlueButtonRecordingReference referencePermanent = bigBlueButtonRecordingReferenceDao.createReference(recordingPermanent, meeting, null);
		referencePermanent.setPermanent(Boolean.TRUE);
		referencePermanent = bigBlueButtonRecordingReferenceDao.updateRecordingReference(referencePermanent);

		unitTestRecordingsHandler.setRecordingsList(List.of(recordingDef, recordingNot, recordingPermanent));
		dbInstance.commitAndCloseSession();
		
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		bigBlueButtonManager.deleteMeeting(meeting, errors);
		dbInstance.commitAndCloseSession();
		
		List<BigBlueButtonRecording> recordings = unitTestRecordingsHandler.getRecordingsList();
		assertThat(recordings)
			.isNotNull()
			.contains(recordingDef, recordingPermanent)
			.doesNotContain(recordingNot);

		List<BigBlueButtonRecordingReference> deletedReferences = bigBlueButtonRecordingReferenceDao.getRecordingReferences(meeting);
		Assert.assertTrue(deletedReferences.isEmpty());

		bigBlueButtonModule.setRecordingHandlerId(currentHandlerId);
	}
	
	@Test
	public void deleteMeetingsNotPermanentRecording() {
		String currentHandlerId = bigBlueButtonModule.getRecordingHandlerId();
		bigBlueButtonModule.setRecordingsPermanent(false);
		bigBlueButtonModule.setRecordingHandlerId("unittests");
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bbb-recording-2");
		BusinessGroup group = businessGroupDao.createAndPersist(null, "BBB Recording 2", "Not hold recordings", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		BigBlueButtonMeeting meeting = bigBlueButtonMeetingDao.createAndPersistMeeting("Record all - 2", null, null, group, id);
		dbInstance.commit();
		
		// default
		BigBlueButtonRecording recordingDef = BigBlueButtonRecordingImpl.valueOf(UUID.randomUUID().toString(), "Recorded", UUID.randomUUID().toString(),
				new Date(), new Date(), "http://button.openolat.com/1", "presentation");
		bigBlueButtonRecordingReferenceDao.createReference(recordingDef, meeting, null);
		
		// flagged explicitly as not permanent
		BigBlueButtonRecording recordingNot = BigBlueButtonRecordingImpl.valueOf(UUID.randomUUID().toString(), "Recorded", UUID.randomUUID().toString(),
				new Date(), new Date(), "http://button.openolat.com/2", "presentation");
		BigBlueButtonRecordingReference referenceNot = bigBlueButtonRecordingReferenceDao.createReference(recordingNot, meeting, null);
		referenceNot.setPermanent(Boolean.FALSE);
		referenceNot = bigBlueButtonRecordingReferenceDao.updateRecordingReference(referenceNot);

		// flagged explicitly as permanent
		BigBlueButtonRecording recordingPermanent = BigBlueButtonRecordingImpl.valueOf(UUID.randomUUID().toString(), "Recorded", UUID.randomUUID().toString(),
				new Date(), new Date(), "http://button.openolat.com/3", "presentation");
		BigBlueButtonRecordingReference referencePermanent = bigBlueButtonRecordingReferenceDao.createReference(recordingPermanent, meeting, null);
		referencePermanent.setPermanent(Boolean.TRUE);
		referencePermanent = bigBlueButtonRecordingReferenceDao.updateRecordingReference(referencePermanent);

		unitTestRecordingsHandler.setRecordingsList(List.of(recordingDef, recordingNot, recordingPermanent));
		dbInstance.commitAndCloseSession();
		
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		bigBlueButtonManager.deleteMeeting(meeting, errors);
		dbInstance.commitAndCloseSession();
		
		List<BigBlueButtonRecording> recordings = unitTestRecordingsHandler.getRecordingsList();
		assertThat(recordings)
			.isNotNull()
			.contains(recordingPermanent)
			.doesNotContain(recordingDef, recordingNot);
		
		List<BigBlueButtonRecordingReference> deletedReferences = bigBlueButtonRecordingReferenceDao.getRecordingReferences(meeting);
		Assert.assertTrue(deletedReferences.isEmpty());

		bigBlueButtonModule.setRecordingHandlerId(currentHandlerId);
	}

	@Test
	public void testSetUserConformanceDecisionById() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bbb-user-" + UUID.randomUUID());
		String meetingId = "test-meeting-123";
		boolean isConform = true;
		Preferences guiPrefs = PreferencesFactory.getInstance().getPreferencesFor(id, false);

		bigBlueButtonManager.setUserConformanceDecisionById(meetingId, guiPrefs, isConform);

		boolean storedValue = (boolean) guiPrefs.get(BigBlueButtonMeetingController.class, meetingId);
		Assert.assertEquals(isConform, storedValue);
	}

	@Test
	public void testGetUserConformanceDecisionById() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bbb-user-" + UUID.randomUUID());
		String meetingId = "test-meeting-123";
		boolean expectedDecision = true;
		Preferences guiPrefs = PreferencesFactory.getInstance().getPreferencesFor(id, false);
		guiPrefs.putAndSave(BigBlueButtonMeetingController.class, meetingId, expectedDecision);

		boolean actualDecision = bigBlueButtonManager.getUserConformanceDecisionById(meetingId, guiPrefs);

		Assert.assertEquals(expectedDecision, actualDecision);
	}

	@Test
	public void testDeleteMeetingRemovesGuiPrefs() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("bbb-user-" + UUID.randomUUID());
		Preferences guiPrefs = PreferencesFactory.getInstance().getPreferencesFor(id, false);

		BusinessGroup group = businessGroupDao.createAndPersist(id, "BBB Recording" + UUID.randomUUID(), "Delete recording or not", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		BigBlueButtonMeeting meeting = bigBlueButtonMeetingDao.createAndPersistMeeting("Recording saved - 1", null, null, group, id);
		dbInstance.commit();
		String meetingId = meeting.getMeetingId();

		guiPrefs.putAndSave(BigBlueButtonMeetingController.class, meetingId, true);

		bigBlueButtonManager.deleteMeeting(meeting, new BigBlueButtonErrors());

		guiPreferenceService.deleteGuiPrefsByUniqueProperties(null, BigBlueButtonMeetingController.class.getName(), meetingId);
		Object storedValue = guiPrefs.get(BigBlueButtonMeetingController.class, meetingId);
		Assert.assertNull(storedValue);
	}

}
