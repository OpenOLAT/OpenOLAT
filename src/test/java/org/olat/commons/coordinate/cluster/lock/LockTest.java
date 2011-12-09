/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.commons.coordinate.cluster.lock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.commons.coordinate.cluster.ClusterCoordinator;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.TestTable;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.DBRuntimeException;
import org.olat.core.util.SignOnOffEvent;
import org.olat.core.util.coordinate.LockEntry;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.coordinate.Locker;
import org.olat.core.util.resource.OresHelper;
import org.olat.test.OlatTestCase;


/**
 * 
 */
public class LockTest extends OlatTestCase {

	private static final int MAX_COUNT = 30; //at least 2
	private static final int MAX_USERS_MORE = 100; //20; //100;
	
	private static Logger log = Logger.getLogger(LockTest.class.getName());

	@Test
	public void testCreateDeleteAcquire() {
		// some setup
		final List<Identity> identities = new ArrayList<Identity>();
		BaseSecurity baseSecurityManager = applicationContext.getBean(BaseSecurity.class);
		for (int i = 0; i < MAX_COUNT + MAX_USERS_MORE; i++) {
			Identity i1 = baseSecurityManager.createAndPersistIdentity("u"+i, null, null, null, null);
			identities.add(i1);
		}
		DBFactory.getInstance().closeSession();

		ClusterLockManager cm = ClusterLockManager.getInstance();
		Identity ident = identities.get(0);
		Identity ident2 = identities.get(1);
		OLATResourceable ores = OresHelper.createOLATResourceableInstanceWithoutCheck(LockTest.class.getName(), new Long(123456789)); 
		
		// ------------------ test the clusterlockmanager ----------------------
		// create a lock
		String asset = OresHelper.createStringRepresenting(ores, "locktest");
		LockImpl li = cm.createLockImpl(asset, ident);
		cm.saveLock(li);
		DBFactory.getInstance().closeSession();
		
		// find it
		LockImpl l2 = cm.findLock(asset);
		assertNotNull(l2);
		assertEquals(li.getKey(), l2.getKey());
		
		// delete it 
		cm.deleteLock(l2);
		DBFactory.getInstance().closeSession();
		
		// may not find it again
		LockImpl l3 = cm.findLock(asset);
		assertNull(l3);
		
		
		// ------------------ test the clusterlocker ----------------------	
		//access the cluster locker explicitely
		Locker cl = applicationContext.getBean(ClusterCoordinator.class).getLocker();
		
		// acquire
		LockResult res1 = cl.acquireLock(ores, ident, "abc");
		assertTrue(res1.isSuccess());
		DBFactory.getInstance().closeSession();
		
		// reacquire same identity (get from db)
		LockResult res11 = cl.acquireLock(ores, ident, "abc");
		long lock1Ac = res11.getLockAquiredTime();
		assertTrue(res11.isSuccess());
		DBFactory.getInstance().closeSession();

		// acquire by another identity must fail
		LockResult res2 = cl.acquireLock(ores, ident2, "abc");
		assertFalse(res2.isSuccess());
		DBFactory.getInstance().closeSession();

		// reacquire same identity
		LockResult res3 = cl.acquireLock(ores, ident, "abc");
		assertTrue(res3.isSuccess());
		DBFactory.getInstance().closeSession();

		// test the admin
		List<LockEntry> entries = cl.adminOnlyGetLockEntries();
		assertEquals(1, entries.size());
		LockEntry le = entries.get(0);
		// must be original owner
		assertEquals(le.getOwner().getName(), ident.getName());

		// release lock
		cl.releaseLock(res3);
		DBFactory.getInstance().closeSession();
		// test the admin
		entries = cl.adminOnlyGetLockEntries();
		assertEquals(0,entries.size());

		// make sure it is not locked anymore
		boolean lo = cl.isLocked(ores, "abc");
		assertFalse(lo);
		
		
		
		
		
		/*LockResult res3 = cl.releaseLock(lockResult)acquireLock(ores, ident, "abc");
		assertTrue(res3.isSuccess());
		DBFactory.getInstance().closeSession();
		*/
		
		//final SecurityGroup group2 = ManagerFactory.getManager().createAndPersistSecurityGroup();
		// make sure the lock has been written to the disk (tests for createOrFind see other methods)

		//PLock p1 = PessimisticLockManager.getInstance().findOrPersistPLock("befinsert");
		//assertNotNull(p1);

		
		// try to enrol all in the same group
		/*for (int i = 0; i < MAX_COUNT + MAX_USERS_MORE; i++) {
			final int j = i;
			new Thread(new Runnable(){
				public void run() {
					try {
						System.out.println("thread started");
						Identity id = identities.get(j);
						//
						DBFactory.getInstance().beginSingleTransaction();
						PLock p2 = ClusterLockManager.getInstance().findOrPersistPLock("befinsert");
						assertNotNull(p2);
						doNoLockingEnrol(id, group2);
						DBFactory.getInstance().commit();
						DBFactory.getInstance().closeSession();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}}).start();
		}		
		
		sleep(20000);
		// now count 
		DBFactory.getInstance().closeSession();
		int cnt2 = ManagerFactory.getManager().countIdentitiesOfSecurityGroup(group2);
		assertTrue("cnt should be smaller or eq than allowed since synced with select for update. cnt:"+cnt2+", max "+MAX_COUNT, cnt2 <= MAX_COUNT);
		assertTrue("cnt should be eq to allowed since synced with select for update. cnt:"+cnt2+", max "+MAX_COUNT, cnt2 == MAX_COUNT);
		System.out.println("cnt lock "+cnt2);
		*/
	}

	@Test
	public void testSaveEvent() {
		BaseSecurity baseSecurityManager = applicationContext.getBean(BaseSecurity.class);
    Identity identity = baseSecurityManager.createAndPersistIdentity("testSaveEvent", null, null, null, null);
    DBFactory.getInstance().closeSession();
    System.out.println("Created identity=" + identity);
		//
		TestTable entry = new TestTable();
		entry.setField1("bar");
		entry.setField2(2221234354566776L);
		try {
	    DBFactory.getInstance().saveObject(entry);
	    DBFactory.getInstance().commit();
	    fail("Should generate an error");
		} catch (DBRuntimeException dre) {
			System.out.println("DB connection is in error-state");
		}
		// DB transaction must be in error state for this test
		try {
			
			ClusterLocker locker = (ClusterLocker) applicationContext.getBean(ClusterCoordinator.class).getLocker();
			System.out.println("ClusterLocker created");
	    Event event = new SignOnOffEvent(identity, false);
	    System.out.println("START locker.event(event)");
	    locker.event(event);
			System.out.println("DONE locker.event(event)");
		} catch(Exception ex) {
			System.err.println(ex);
			fail("BLOCKER : ClusterLocker.event is not error-safe, db exception could happen and de-register event-listener");
		}
	}


	private void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@After public void tearDown() throws Exception {
		try {
			DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			log.error("tearDown failed: ", e);
		}
	}
}