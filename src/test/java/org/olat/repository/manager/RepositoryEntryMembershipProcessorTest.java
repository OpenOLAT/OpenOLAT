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
package org.olat.repository.manager;

import java.util.ArrayList;
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
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryMembershipProcessorTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private NotificationsManager notificationManager;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
	@Test
	public void testRemoveParticipant() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		//create a group with members
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("remp-proc-1");
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("remp-proc-2");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mbr-proc-3");

		repositoryEntryRelationDao.addRole(owner, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(member, re, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(member, re, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(participant, re, GroupRoles.participant.name());
		
		//create a publisher
		SubscriptionContext context = new SubscriptionContext(re.getOlatResource(), "");
		PublisherData publisherData = new PublisherData("testGroupPublishers", "e.g. something", null);
		Publisher publisher = notificationManager.getOrCreatePublisher(context, publisherData);
		Assert.assertNotNull(publisher);
		dbInstance.commitAndCloseSession();
		
		//subscribe
		notificationManager.subscribe(owner, context, publisherData);
		notificationManager.subscribe(member, context, publisherData);
		notificationManager.subscribe(participant, context, publisherData);
		dbInstance.commitAndCloseSession();
		
		//remove member and participant as participant of the repo entry
		List<Identity> removeIdentities = new ArrayList<>(2);
		removeIdentities.add(member);
		removeIdentities.add(participant);
		MailPackage mailing = new MailPackage(false);
		repositoryManager.removeParticipants(owner, removeIdentities, re, mailing, false);

		//wait for the remove of subscription
		waitForCondition(new CheckUnsubscription(participant, context, dbInstance, notificationManager), 5000);
		sleep(1000);
		
		//check that subscription of id1 was deleted but not the ones of id2 and coach
		boolean subscribedPart = notificationManager.isSubscribed(participant, context);
		Assert.assertFalse(subscribedPart);
		boolean subscribedMember = notificationManager.isSubscribed(member, context);
		Assert.assertTrue(subscribedMember);
		boolean subscribedOwner = notificationManager.isSubscribed(owner, context);
		Assert.assertTrue(subscribedOwner);
	}
	
	@Test
	public void testRemoveCoach_withBusinessGroups() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		//create a group with members
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("remp-proc-1");
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("remp-proc-2");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("mbr-proc-3");

		repositoryEntryRelationDao.addRole(owner, re, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(member, re, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(coach, re, GroupRoles.coach.name());
		
		BusinessGroup businessGroup = businessGroupDao.createAndPersist(coach, "mbr-proc-1", "mbr-proc-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(businessGroup, re);
		
		//create a publisher
		SubscriptionContext context = new SubscriptionContext(re.getOlatResource(), "");
		PublisherData publisherData = new PublisherData("testGroupPublishers", "e.g. something", null);
		Publisher publisher = notificationManager.getOrCreatePublisher(context, publisherData);
		Assert.assertNotNull(publisher);
		dbInstance.commitAndCloseSession();
		
		//subscribe
		notificationManager.subscribe(owner, context, publisherData);
		notificationManager.subscribe(member, context, publisherData);
		notificationManager.subscribe(coach, context, publisherData);
		dbInstance.commitAndCloseSession();
		
		//remove member and coach as coach of the repo entry
		List<Identity> removeIdentities = new ArrayList<>(2);
		removeIdentities.add(member);
		removeIdentities.add(coach);
		repositoryManager.removeTutors(owner, removeIdentities, re, new MailPackage(false));

		//wait for the remove of subscription
		waitForCondition(new CheckUnsubscription(member, context, dbInstance, notificationManager), 5000);
		sleep(1000);
		
		//check that subscription of id1 was deleted but not the ones of id2 and coach
		boolean subscribedMember = notificationManager.isSubscribed(member, context);
		Assert.assertFalse(subscribedMember);
		boolean subscribedCoach = notificationManager.isSubscribed(coach, context);
		Assert.assertTrue(subscribedCoach);
		boolean subscribedOwner = notificationManager.isSubscribed(owner, context);
		Assert.assertTrue(subscribedOwner);
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
