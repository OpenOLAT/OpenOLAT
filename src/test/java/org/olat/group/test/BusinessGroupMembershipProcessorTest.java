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
package org.olat.group.test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.MailPackage;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupMembershipProcessor;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupMembershipProcessorTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private NotificationsManager notificationManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private BusinessGroupMembershipProcessor businessGroupMembershipProcessor;
	
	@Test
	public void testUnlinkMemberOfBusinessGroup() {
		//create a group with members
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("mbr-proc-1");
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("mbr-proc-2");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("mbr-proc-3");
		BusinessGroup businessGroup = businessGroupDao.createAndPersist(coach, "mbr-proc-1", "mbr-proc-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRole(id1, businessGroup, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2, businessGroup, GroupRoles.participant.name());
		
		//create a publisher
		SubscriptionContext context = new SubscriptionContext(businessGroup, "");
		PublisherData publisherData = new PublisherData("testGroupPublishers", "e.g. something", null);
		Publisher publisher = notificationManager.getOrCreatePublisher(context, publisherData);
		Assert.assertNotNull(publisher);
		dbInstance.commitAndCloseSession();
		
		//subscribe
		notificationManager.subscribe(coach, context, publisherData);
		notificationManager.subscribe(id1, context, publisherData);
		notificationManager.subscribe(id2, context, publisherData);
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(notificationManager.isSubscribed(coach, context));
		Assert.assertTrue(notificationManager.isSubscribed(id1, context));
		Assert.assertTrue(notificationManager.isSubscribed(id2, context));
		
		//remove id1 and check subscription
		MailPackage mailing = new MailPackage(false);
		List<Identity> identitiesToRemove = Collections.singletonList(id1);
		businessGroupService.removeParticipants(coach, identitiesToRemove, businessGroup, mailing);
		
		//wait for the remove of subscription
		waitForCondition(new CheckUnsubscription(id1, context, dbInstance, notificationManager), 5000);
		
		//check that subscription of id1 was deleted but not the ones of id2 and coach
		boolean subscribedId1 = notificationManager.isSubscribed(id1, context);
		Assert.assertFalse(subscribedId1);
		boolean subscribedId2 = notificationManager.isSubscribed(id2, context);
		Assert.assertTrue(subscribedId2);
		boolean subscribedCoach = notificationManager.isSubscribed(coach, context);
		Assert.assertTrue(subscribedCoach);
	}
	
	@Test
	public void testUnlinkMemberOfBusinessGroup_with2Roles() {
		//create a group with members
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("mbr-proc-4");
		BusinessGroup businessGroup = businessGroupDao.createAndPersist(member, "mbr-proc-2", "mbr-proc-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRole(member, businessGroup, GroupRoles.participant.name());
		
		//create a publisher
		SubscriptionContext context = new SubscriptionContext(businessGroup, "");
		PublisherData publisherData = new PublisherData("testGroupPublishers", "e.g. something", null);
		notificationManager.getOrCreatePublisher(context, publisherData);
		notificationManager.subscribe(member, context, publisherData);
		dbInstance.commitAndCloseSession();
		
		//remove id1 as participant and check subscription
		businessGroupRelationDao.removeRole(member, businessGroup, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//manually trigger the event
		businessGroupMembershipProcessor.event(new BusinessGroupModifiedEvent(BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT, businessGroup, member, null));
		dbInstance.commitAndCloseSession();
	
		//check that subscription of member was not deleted because it's still coach
		boolean subscribed = notificationManager.isSubscribed(member, context);
		Assert.assertTrue(subscribed);
	}
	
	@Test
	public void testUnlinkRepositoryEntry() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		//create a group with members
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("mbr-proc-1");
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("mbr-proc-2");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("mbr-proc-3");
		BusinessGroup businessGroup = businessGroupDao.createAndPersist(coach, "mbr-proc-1", "mbr-proc-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(businessGroup, re);
		businessGroupRelationDao.addRole(id1, businessGroup, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2, businessGroup, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(coach, re, GroupRoles.owner.name());
		
		//create a publisher
		SubscriptionContext context = new SubscriptionContext(re.getOlatResource(), "");
		PublisherData publisherData = new PublisherData("testGroupPublishers", "e.g. something", null);
		Publisher publisher = notificationManager.getOrCreatePublisher(context, publisherData);
		Assert.assertNotNull(publisher);
		dbInstance.commitAndCloseSession();
		
		//subscribe
		notificationManager.subscribe(coach, context, publisherData);
		notificationManager.subscribe(id1, context, publisherData);
		notificationManager.subscribe(id2, context, publisherData);
		dbInstance.commitAndCloseSession();
		
		//remove link between group and repository entry
		businessGroupService.removeResourceFrom(Collections.singletonList(businessGroup), re);
		dbInstance.commitAndCloseSession();
		
		//wait for the remove of subscription
		waitForCondition(new CheckUnsubscription(id1, context, dbInstance, notificationManager), 5000);
		waitForCondition(new CheckUnsubscription(id2, context, dbInstance, notificationManager), 5000);
		
		//check that subscription of id1 was deleted but not the ones of id2 and coach
		boolean subscribedId1 = notificationManager.isSubscribed(id1, context);
		Assert.assertFalse(subscribedId1);
		boolean subscribedId2 = notificationManager.isSubscribed(id2, context);
		Assert.assertFalse(subscribedId2);
		boolean subscribedCoach = notificationManager.isSubscribed(coach, context);
		Assert.assertTrue(subscribedCoach);
	}

	private static class CheckUnsubscription implements Callable<Boolean> {
		
		private final DB db;
		private final NotificationsManager notificationMgr;
		
		private final Identity identity;
		private final SubscriptionContext context;
		
		public CheckUnsubscription(Identity identity, SubscriptionContext context, DB db, NotificationsManager notificationMgr) {
			this.identity = identity;
			this.context = context;
			this.db = db;
			this.notificationMgr = notificationMgr;
		}

		@Override
		public Boolean call() throws Exception {
			boolean subscribed = notificationMgr.isSubscribed(identity, context);
			db.commitAndCloseSession();
			return !subscribed;
		}
		
	}
}
