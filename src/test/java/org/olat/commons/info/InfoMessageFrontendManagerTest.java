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
package org.olat.commons.info;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.model.DBMailLight;
import org.olat.core.util.mail.model.DBMailRecipient;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: 20.03.2017
 * 
 * @author fkiefer, fabian.kiefer@frentix.com, www.frentix.com
 */
public class InfoMessageFrontendManagerTest extends OlatTestCase {

	@Autowired
	private NotificationsManager notificationManager;
	@Autowired
	private InfoMessageFrontendManager infoManager;
	@Autowired
	private BusinessGroupService groupService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private DB dbInstance;

	@Test
	public void createSaveLoadAndCountInfoMessage() {
		// same methods as already tested @InfoManagerTest
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("info-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("info-2");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("info-3");
		final String resName = UUID.randomUUID().toString();
		Random random = new Random();
		final InfoOLATResourceable ores = new InfoOLATResourceable(random.nextLong(), resName);
		// create, save
		InfoMessage msg1 = infoManager.createInfoMessage(ores, InfoMessageFrontendManager.businessGroupResSubPath, null, id2);
		msg1.setTitle("title-1");
		msg1.setMessage("message-1");
		Assert.assertNotNull(msg1);
		msg1 = infoManager.saveInfoMessageAndNotify(msg1);
		// create, save
		InfoMessage msg2 = infoManager.createInfoMessage(ores, InfoMessageFrontendManager.businessGroupResSubPath, null, id3);
		msg2.setTitle("title-2");
		msg2.setMessage("message-2");
		Assert.assertNotNull(msg2);
		msg2 = infoManager.saveInfoMessageAndNotify(msg2);
		// create, not save
		InfoMessage msg3 = infoManager.createInfoMessage(ores, InfoMessageFrontendManager.businessGroupResSubPath, null, id1);
		msg3.setTitle("title-3");
		msg3.setMessage("message-3");
		Assert.assertNotNull(msg3);
		msg3 = infoManager.saveInfoMessageAndNotify(msg3);

		dbInstance.commitAndCloseSession();

		// load by key
		InfoMessage loadedMsg1 = infoManager.loadInfoMessage(msg1.getKey());
		Assert.assertNotNull(loadedMsg1);
		InfoMessage loadedMsg2 = infoManager.loadInfoMessage(msg2.getKey());
		Assert.assertNotNull(loadedMsg2);
		InfoMessage loadedMsg3 = infoManager.loadInfoMessage(msg3.getKey());
		Assert.assertNotNull(loadedMsg3);

		// load by resource
		List<InfoMessage> loadedMessages = infoManager.loadInfoMessageByResource(ores,
				InfoMessageFrontendManager.businessGroupResSubPath, null, null, null, 0, 0);
		Assert.assertNotNull(loadedMessages);
		Assert.assertEquals(3, loadedMessages.size());
		Assert.assertTrue(loadedMessages.contains(msg1));
		Assert.assertTrue(loadedMessages.contains(msg2));
		Assert.assertTrue(loadedMessages.contains(msg3));

		// count info messages
		int count = infoManager.countInfoMessageByResource(ores, null, null, null, null);
		Assert.assertEquals(3, count);
	}

	@Test
	public void deleteInfoMessage() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("info-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("info-2");
		final String resName = UUID.randomUUID().toString();
		final InfoOLATResourceable ores = new InfoOLATResourceable(5l, resName);
		// create, save
		InfoMessage msg1 = infoManager.createInfoMessage(ores, InfoMessageFrontendManager.businessGroupResSubPath, null, id2);
		msg1.setTitle("title-1");
		msg1.setMessage("message-1");
		Assert.assertNotNull(msg1);
		msg1 = infoManager.saveInfoMessageAndNotify(msg1);
		// create, save
		InfoMessage msg2 = infoManager.createInfoMessage(ores, InfoMessageFrontendManager.businessGroupResSubPath, null, id1);
		msg2.setTitle("title-2");
		msg2.setMessage("message-2");
		Assert.assertNotNull(msg2);
		msg2 = infoManager.saveInfoMessageAndNotify(msg2);

		dbInstance.commitAndCloseSession();

		infoManager.deleteInfoMessage(msg1);
		dbInstance.commitAndCloseSession();

		InfoMessage loadedMsg1 = infoManager.loadInfoMessage(msg1.getKey());
		Assert.assertNull(loadedMsg1);
		InfoMessage loadedMsg2 = infoManager.loadInfoMessage(msg2.getKey());
		Assert.assertNotNull(loadedMsg2);
	}

	@Test
	public void removeInfoMessagesAndSubscriptionContext() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("info-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("info-2");
		RepositoryEntry resource = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup businessGroup = groupService.createBusinessGroup(null, "gdao1", "gdao1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, resource);
		final OLATResourceable ores = new OLATResourceable() {
			@Override
			public String getResourceableTypeName() {
				return businessGroup.getResourceableTypeName();
			}

			@Override
			public Long getResourceableId() {
				return businessGroup.getResourceableId();
			}
		};		
		// create, save
		InfoMessage msg1 = infoManager.createInfoMessage(ores, InfoMessageFrontendManager.businessGroupResSubPath, null, id2);
		msg1.setTitle("title-1");
		msg1.setMessage("message-1");
		Assert.assertNotNull(msg1);
		msg1 = infoManager.saveInfoMessageAndNotify(msg1);
		// create, save
		InfoMessage msg2 = infoManager.createInfoMessage(ores, InfoMessageFrontendManager.businessGroupResSubPath, null, id1);
		msg2.setTitle("title-2");
		msg2.setMessage("message-2");
		Assert.assertNotNull(msg2);
		msg2 = infoManager.saveInfoMessageAndNotify(msg2);
		// create, save
		InfoMessage msg3 = infoManager.createInfoMessage(ores, InfoMessageFrontendManager.businessGroupResSubPath, null, id1);
		msg3.setTitle("title-3");
		msg3.setMessage("message-3");
		Assert.assertNotNull(msg3);
		msg3 = infoManager.saveInfoMessageAndNotify(msg3);

		dbInstance.commitAndCloseSession();
		
		SubscriptionContext sc = new SubscriptionContext(businessGroup.getResourceableTypeName(),
				businessGroup.getResourceableId(), InfoMessageFrontendManager.businessGroupResSubPath);
		PublisherData pd = new PublisherData("InfoMessage", "e.g. infoMessage=anyMessage", null);
		// subscribe
		notificationManager.subscribe(id1, sc, pd);
		notificationManager.subscribe(id2, sc, pd);
		dbInstance.closeSession();
		
		// check if publisher was created
		Publisher p = notificationManager.getPublisher(sc);
		assertNotNull(p);
		
		// check before message deletion
		List<InfoMessage> loadedMessages1 = infoManager.loadInfoMessageByResource(ores,
				InfoMessageFrontendManager.businessGroupResSubPath, null, null, null, 0, 0);
		Assert.assertEquals(3, loadedMessages1.size());
		Assert.assertTrue(loadedMessages1.contains(msg1));
		Assert.assertTrue(loadedMessages1.contains(msg2));
		Assert.assertTrue(loadedMessages1.contains(msg3));
		// delete
		infoManager.removeInfoMessagesAndSubscriptionContext(businessGroup);
		dbInstance.commitAndCloseSession();
		// check if messages are deleted
		List<InfoMessage> loadedMessages2 = infoManager.loadInfoMessageByResource(ores,
				InfoMessageFrontendManager.businessGroupResSubPath, null, null, null, 0, 0);
		Assert.assertEquals(0, loadedMessages2.size());
		Assert.assertFalse(loadedMessages2.contains(msg1));
		Assert.assertFalse(loadedMessages2.contains(msg2));
		Assert.assertFalse(loadedMessages2.contains(msg3));
		// check if pubisher is deleted
		Publisher p2 = notificationManager.getPublisher(sc);
		assertNull("publisher marked deleted should not be found", p2);

	}

	@Test
	public void getInfoSubscribers() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("info-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("info-2");
		RepositoryEntry resource = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup businessGroup = groupService.createBusinessGroup(null, "gdao", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, resource);
		final OLATResourceable ores = new OLATResourceable() {
			@Override
			public String getResourceableTypeName() {
				return businessGroup.getResourceableTypeName();
			}

			@Override
			public Long getResourceableId() {
				return businessGroup.getResourceableId();
			}
		};
		// create publisher data
		String identifier = InfoMessageFrontendManager.businessGroupResSubPath;
		SubscriptionContext context = new SubscriptionContext(businessGroup.getResourceableTypeName(),
				businessGroup.getResourceableId(), identifier);
		PublisherData publisherData = new PublisherData("testGetSubscriberIdentities", "e.g. data=infomessage", null);
		dbInstance.commitAndCloseSession();

		// add subscribers
		notificationManager.subscribe(id1, context, publisherData);
		notificationManager.subscribe(id2, context, publisherData);
		dbInstance.commitAndCloseSession();

		// get identities
		List<Identity> identities = infoManager.getInfoSubscribers(ores, identifier);
		Assert.assertNotNull(identities);
		Assert.assertEquals(2, identities.size());
		Assert.assertTrue(identities.contains(id1));
		Assert.assertTrue(identities.contains(id2));
	}

	/**
	 * A scheduled info message on a course, addressed to "All course participants" only
	 * (the course-only "participant-course" option, no groups selected), must be sent to the course's
	 * own participants only - not to identities who are participants only via a business group linked
	 * to the course. The scheduled-message cron used to match "participant-course" with a substring
	 * check against "participant", wrongly treating it as the combined (course + groups) option.
	 */
	@Test
	public void sendScheduledCourseMessage_shouldNotSendToLinkedGroupParticipants() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("oo10022-author");
		Identity courseParticipant = JunitTestHelper.createAndPersistIdentityAsRndUser("oo10022-course-part");
		Identity groupParticipant = JunitTestHelper.createAndPersistIdentityAsRndUser("oo10022-group-part");

		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(author);
		dbInstance.commitAndCloseSession();

		// direct course participant
		repositoryService.addRole(courseParticipant, courseEntry, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		// business group linked to the course, with a participant who is not a course member
		BusinessGroup group = groupService.createBusinessGroup(null, "OO-10022 group", "OO-10022 group desc",
				BusinessGroup.BUSINESS_TYPE, null, null, false, false, courseEntry);
		dbInstance.commitAndCloseSession();
		groupService.addParticipants(author, Roles.administratorRoles(), Collections.singletonList(groupParticipant), group, null);
		dbInstance.commitAndCloseSession();

		ICourse course = CourseFactory.loadCourse(courseEntry);
		String title = "oo10022-" + UUID.randomUUID();
		InfoMessage msg = infoManager.createInfoMessage(course, InfoMessageFrontendManager.businessGroupResSubPath, null, author);
		msg.setTitle(title);
		msg.setMessage("Scheduled message for the course participants only.");
		// "participant-course" (course-only) as opposed to the combined "participant" key which also
		// includes group/curriculum participants (see SendMembersMailOption.getOptionKey())
		msg.setSendMailTo(GroupRoles.participant.name() + "-course");
		msg.setPublishDate(new Date(System.currentTimeMillis() - 60000L));
		msg = infoManager.saveInfoMessageAndNotify(msg);
		dbInstance.commitAndCloseSession();

		// trigger the scheduled-message cron (SendInfoMessageJob)
		infoManager.sendScheduledInfoMessages();
		dbInstance.commitAndCloseSession();

		InfoMessage reloaded = infoManager.loadInfoMessage(msg.getKey());
		Assert.assertNotNull(reloaded);
		Assert.assertTrue(reloaded.isPublished());

		List<DBMailLight> outbox = mailManager.getOutbox(author, 0, -1, true);
		DBMailLight mail = outbox.stream()
				.filter(m -> title.equals(m.getSubject()))
				.findFirst()
				.orElse(null);
		Assert.assertNotNull("Expected an e-mail to have been sent for the scheduled info message", mail);

		List<Identity> recipients = mail.getRecipients().stream()
				.map(DBMailRecipient::getRecipient)
				.toList();
		Assert.assertTrue("The direct course participant must receive the e-mail", recipients.contains(courseParticipant));
		Assert.assertFalse("A participant of a group linked to the course must not receive the e-mail",
				recipients.contains(groupParticipant));
	}

	private class InfoOLATResourceable implements OLATResourceable {
		private final Long resId;
		private final String resName;

		public InfoOLATResourceable(Long resId, String resName) {
			this.resId = resId;
			this.resName = resName;
		}

		@Override
		public String getResourceableTypeName() {
			return resName;
		}

		@Override
		public Long getResourceableId() {
			return resId;
		}
	}
}
