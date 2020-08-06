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

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.GuestPolicyEnum;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingTemplateImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonMeetingTemplateDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BigBlueButtonMeetingDAO bigBlueButtonMeetingDao;
	@Autowired
	private BigBlueButtonMeetingTemplateDAO bigBlueButtonMeetingTemplateDao;
	
	@Test
	public void createTemplate() {
		String externalId = UUID.randomUUID().toString();
		BigBlueButtonMeetingTemplate template = bigBlueButtonMeetingTemplateDao.createTemplate("A new template", externalId, false);
		dbInstance.commit();
		
		Assert.assertNotNull(template.getKey());
		Assert.assertNotNull(template.getCreationDate());
		Assert.assertNotNull(template.getLastModified());
		Assert.assertEquals(externalId, ((BigBlueButtonMeetingTemplateImpl)template).getExternalId());
	}
	
	@Test
	public void createUpdateTemplate() {
		String externalId = UUID.randomUUID().toString();
		BigBlueButtonMeetingTemplate template = bigBlueButtonMeetingTemplateDao.createTemplate("A new template to update", externalId, false);
		dbInstance.commit();
		
		template.setMaxParticipants(99);
		template.setMuteOnStart(Boolean.TRUE);
		template.setAutoStartRecording(Boolean.FALSE);
		template.setAllowStartStopRecording(Boolean.TRUE);
		template.setWebcamsOnlyForModerator(Boolean.FALSE);
		template.setAllowModsToUnmuteUsers(Boolean.TRUE);
		template.setLockSettingsDisableCam(Boolean.FALSE);
		template.setLockSettingsDisableMic(Boolean.TRUE);
		template.setLockSettingsDisablePrivateChat(Boolean.FALSE);
		template.setLockSettingsDisablePublicChat(Boolean.TRUE);
		template.setLockSettingsDisableNote(Boolean.FALSE);
		template.setLockSettingsLockedLayout(Boolean.TRUE);
		template.setGuestPolicyEnum(GuestPolicyEnum.ASK_MODERATOR);
		
		BigBlueButtonMeetingTemplate updatedTemplate = bigBlueButtonMeetingTemplateDao.updateTemplate(template);
		dbInstance.commitAndCloseSession();
		
		BigBlueButtonMeetingTemplate reloadedTemplate = bigBlueButtonMeetingTemplateDao.getTemplate(updatedTemplate);
		Assert.assertNotNull(reloadedTemplate);
		Assert.assertEquals(Integer.valueOf(99), reloadedTemplate.getMaxParticipants());
		Assert.assertEquals(Boolean.TRUE, reloadedTemplate.getMuteOnStart());
		Assert.assertEquals(Boolean.FALSE, reloadedTemplate.getAutoStartRecording());
		Assert.assertEquals(Boolean.TRUE, reloadedTemplate.getAllowStartStopRecording());
		Assert.assertEquals(Boolean.FALSE, reloadedTemplate.getWebcamsOnlyForModerator());
		Assert.assertEquals(Boolean.TRUE, reloadedTemplate.getAllowModsToUnmuteUsers());
		Assert.assertEquals(Boolean.FALSE, reloadedTemplate.getLockSettingsDisableCam());
		Assert.assertEquals(Boolean.TRUE, reloadedTemplate.getLockSettingsDisableMic());
		Assert.assertEquals(Boolean.FALSE, reloadedTemplate.getLockSettingsDisablePrivateChat());
		Assert.assertEquals(Boolean.TRUE, reloadedTemplate.getLockSettingsDisablePublicChat());
		Assert.assertEquals(Boolean.FALSE, reloadedTemplate.getLockSettingsDisableNote());
		Assert.assertEquals(Boolean.TRUE, reloadedTemplate.getLockSettingsLockedLayout());
		Assert.assertEquals(GuestPolicyEnum.ASK_MODERATOR, reloadedTemplate.getGuestPolicyEnum());
	}
	
	@Test
	public void getTemplates() {
		String externalId = UUID.randomUUID().toString();
		BigBlueButtonMeetingTemplate template = bigBlueButtonMeetingTemplateDao.createTemplate("A new template", externalId, false);
		dbInstance.commit();
		
		List<BigBlueButtonMeetingTemplate> templates = bigBlueButtonMeetingTemplateDao.getTemplates();
		Assert.assertNotNull(templates);
		Assert.assertTrue(templates.contains(template));
	}
	
	@Test
	public void isTemplateInNotUse() {
		String externalId = UUID.randomUUID().toString();
		BigBlueButtonMeetingTemplate template = bigBlueButtonMeetingTemplateDao.createTemplate("A new template", externalId, false);
		dbInstance.commit();
		
		boolean inUse = bigBlueButtonMeetingTemplateDao.isTemplateInUse(template);
		Assert.assertFalse(inUse);
	}
	
	@Test
	public void isTemplateInUse() {
		// make a template and use it in a meeting
		BigBlueButtonMeetingTemplate template = bigBlueButtonMeetingTemplateDao.createTemplate("A new template", UUID.randomUUID().toString(), false);
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		BigBlueButtonMeeting meeting = bigBlueButtonMeetingDao.createAndPersistMeeting("BigBlueButton templated - 10", entry, UUID.randomUUID().toString(), null, null);
		meeting.setTemplate(template);
		bigBlueButtonMeetingDao.updateMeeting(meeting);
		dbInstance.commitAndCloseSession();
		
		boolean inUse = bigBlueButtonMeetingTemplateDao.isTemplateInUse(template);
		Assert.assertTrue(inUse);
	}

}
