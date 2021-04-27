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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28.08.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupConcurrentTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	

	
	@Test
	public void concurrentSetLastUsageFor_multipleUser() {
		BusinessGroup group = businessGroupDao.createAndPersist(null, "gdao", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		dbInstance.commit();

		int numOfThreads = 25;
		final CountDownLatch doneSignal = new CountDownLatch(numOfThreads);
		
		SetLastUsageThread[] threads = new SetLastUsageThread[numOfThreads];
		for(int i=numOfThreads; i-->0; ) {
			Identity id = JunitTestHelper.createAndPersistIdentityAsUser("group-concurent-" + i + "-" + UUID.randomUUID().toString());
			businessGroupRelationDao.addRole(id, group, GroupRoles.participant.name());
			threads[i] = new SetLastUsageThread(group.getKey(), id, doneSignal);
		}
		dbInstance.commitAndCloseSession();;
		
		for(int i=numOfThreads; i-->0; ) {
			threads[i].start();
		}

		try {
			boolean interrupt = doneSignal.await(240, TimeUnit.SECONDS);
			assertTrue("Test takes too long (more than 10s)", interrupt);
		} catch (InterruptedException e) {
			fail("" + e.getMessage());
		}
		
		int errorCount = 0;
		for(int i=numOfThreads; i-->0; ) {
			errorCount += threads[i].getErrorCount();
		}
		Assert.assertEquals(0, errorCount);
	}
	
	@Test
	public void concurrentSetLastUsageFor_singleUser() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("group-cc-single-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupDao.createAndPersist(id, "gdao", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		dbInstance.commitAndCloseSession();

		int numOfThreads = 25;
		final CountDownLatch doneSignal = new CountDownLatch(numOfThreads);
		
		SetLastUsageThread[] threads = new SetLastUsageThread[numOfThreads];
		for(int i=numOfThreads; i-->0; ) {
			threads[i] = new SetLastUsageThread(group.getKey(), id, doneSignal);
		}
		
		for(int i=numOfThreads; i-->0; ) {
			threads[i].start();
		}

		try {
			boolean interrupt = doneSignal.await(240, TimeUnit.SECONDS);
			assertTrue("Test takes too long (more than 10s)", interrupt);
		} catch (InterruptedException e) {
			fail("" + e.getMessage());
		}
		
		int errorCount = 0;
		for(int i=numOfThreads; i-->0; ) {
			errorCount += threads[i].getErrorCount();
		}
		Assert.assertEquals(0, errorCount);
	}
	
	
	private class SetLastUsageThread extends Thread {
		
		private AtomicInteger errorCount = new AtomicInteger();
		
		private final Long key;
		private final Identity identity;
		private final CountDownLatch doneSignal;
		
		public SetLastUsageThread(Long key, Identity identity, CountDownLatch doneSignal) {
			this.key = key;
			this.identity = identity;
			this.doneSignal = doneSignal;
		}
		
		public int getErrorCount() {
			return errorCount.get();
		}
	
		@Override
		public void run() {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			BusinessGroupService service = CoreSpringFactory.getImpl(BusinessGroupService.class);
			try {
				BusinessGroup group = service.loadBusinessGroup(key);
				for(int i=50; i-->0; ) {
					group = service.setLastUsageFor(identity, group);
				}
			} catch (Exception e) {
				e.printStackTrace();
				errorCount.incrementAndGet();
			} finally {
				doneSignal.countDown();
				dbInstance.closeSession();
			}
		}
	}
}