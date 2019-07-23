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

package org.olat.resource.lock.pessimistic;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.manager.SecurityGroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.lock.pessimistic.PLock;
import org.olat.core.commons.services.lock.pessimistic.PessimisticLockManager;
import org.olat.core.id.Identity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 */
public class PLockTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(PLockTest.class);

	
	@Autowired
	private DB dbInstance;
	@Autowired
	private SecurityGroupDAO securityGroupDao;
	@Autowired
	private PessimisticLockManager pessimisticLockManager;

	
	@Test public void testReentrantLock() {
		long start = System.currentTimeMillis();
		String asset = "p1";
		// make sure the lock is created first
		PLock pc = pessimisticLockManager.findOrPersistPLock(asset);
		assertNotNull(pc);
		dbInstance.closeSession();
		
		// test double acquisition within same transaction
		PLock pc1 = pessimisticLockManager.findOrPersistPLock(asset);
		assertNotNull(pc1);
		PLock pc2 = pessimisticLockManager.findOrPersistPLock(asset);
		assertNotNull(pc2);
		dbInstance.closeSession();
		
		// and without explicit transaction boundary.
		PLock p1 = pessimisticLockManager.findOrPersistPLock(asset);
		assertNotNull(p1);
		PLock p2 = pessimisticLockManager.findOrPersistPLock(asset);
		assertNotNull(p2);
		long stop = System.currentTimeMillis();
		long diff = stop - start;
		assertTrue("5 select's took longer than 10 seconds -> deadlock / lock timeout ? dur in ms was:"+diff, diff < 10000);
	}

	/**
	 * T1		T2
	 *
	 */
	@Test
	public void testReentrantLock2Threads() {
		final String asset = "p1-2";
		
		// make sure the lock is created first
		PLock pc = pessimisticLockManager.findOrPersistPLock(asset);
		assertNotNull(pc);
		dbInstance.closeSession();

		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
		final CountDownLatch finishCount = new CountDownLatch(2);

		// thread 1
		new Thread(new Runnable() {
			public void run() {
				try {
					PLock pc1 = pessimisticLockManager.findOrPersistPLock(asset);
					assertNotNull(pc1);
					log.info("Thread-1: got PLock pc1=" + pc1);
					log.info("Thread-1: sleep 1sec");
					sleep(1000);
					PLock pc2 = pessimisticLockManager.findOrPersistPLock(asset);
					assertNotNull(pc2);
					log.info("Thread-1: got PLock pc2=" + pc2);
					log.info("Thread-1: finished");
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					finishCount.countDown();
					try {
						dbInstance.commitAndCloseSession();
					} catch (Exception e) {
						// ignore
					}
				}	
			}}).start();
		
		// thread 2
		new Thread(new Runnable() {
			public void run() {
				try {
					log.info("Thread-2: sleep 0.5sec");
					sleep(500);
					log.info("Thread-2: try to get PLock...");
					PLock p1 = pessimisticLockManager.findOrPersistPLock(asset);
					assertNotNull(p1);
					log.info("Thread-2: got PLock p1=" + p1);
					log.info("Thread-2: sleep 1sec");
					sleep(1000);
					PLock p2 = pessimisticLockManager.findOrPersistPLock(asset);
					assertNotNull(p2);
					log.info("Thread-1: got PLock p2=" + p2);
					log.info("Thread-1: finished");
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					finishCount.countDown();
					try {
						dbInstance.commitAndCloseSession();
					} catch (Exception e) {
						// ignore
					}
				}	
			}}).start();
		
		// sleep until t1 and t2 should have terminated/excepted
		try {
			finishCount.await(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Assert.fail("Test takes too long (more than 60s)");
		}
		
		// if not -> they are in deadlock and the db did not detect it
		for (Exception exception : exceptionHolder) {
			log.info("exception: "+exception.getMessage());
			exception.printStackTrace();
		}
		assertTrue("exception in test => see sysout", exceptionHolder.size() == 0);				
	}

	@Test public void testLockWaitTimout() {
		//Ignore Test if DB is PostgreSQL. PostgreSQL has not lock timeout
		assumeTrue(!isPostgresqlConfigured() && !isOracleConfigured());
		
		final String asset = "testLockWaitTimout";
		
		log.info("testing if holding a lock timeouts");
		// make sure all three row entries for the locks are created, otherwise the system-wide locking 
		// applied on lock-row-creation cannot support row-level-locking by definition. 

		PLock pc3 = pessimisticLockManager.findOrPersistPLock("blibli");
		assertNotNull(pc3);
		dbInstance.closeSession();
		
		/**
		 *    t1   t2
		 *    ..  bli
		 *    ..   ..
		 *    ..   ..
		 *    ..   ..
		 *    bli  ..
		 *         ..
		 *         ..
		 *         .... hold for longer than 30 secs
		 *    
		 */
		
	
		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
		final CountDownLatch finishCount = new CountDownLatch(2);
		
		// t1
		new Thread(new Runnable() {
			public void run() {
				try {
					sleep(500);
					PLock p3 = pessimisticLockManager.findOrPersistPLock(asset);
					assertNotNull(p3);					
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					finishCount.countDown();
					try {
						dbInstance.closeSession();
					} catch (Exception e) {
						// ignore
					}
				}	
			}}).start();
		
		// t2
		new Thread(new Runnable() {
			public void run() {
				try {
					PLock p2 = pessimisticLockManager.findOrPersistPLock(asset);
					assertNotNull(p2);
					sleep(55000);
					// holding the lock for more than the transaction timeout
					// (normally 30secs, configured where? hib) should cause a lock timeout
					// if the db is configured so (innodb_lock_wait_timeout).
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					finishCount.countDown();
					try {
						dbInstance.closeSession();
					} catch (Exception e) {
						// ignore
					}
				}					
			}}).start();
		
		// sleep until t1 and t2 should have terminated/excepted
		try {
			log.info("Sleep 55s");
			finishCount.await(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Assert.fail("");
		}
		
		Assert.assertEquals("expected a lock wait timeout exceeded exception", 1, exceptionHolder.size());				
	}
	
	@Test
	public void testSingleRowLockingSupported() {
		log.info("testing if one lock only locks the given row and not the complete table (test whether the database supports rowlocking)");
		// make sure both row entries for the locks are created, otherwise the system-wide locking 
		// applied on lock-row-creation cannot support row-level-locking by definition. 
		PLock pc1 = pessimisticLockManager.findOrPersistPLock("blabla");
		Assert.assertNotNull(pc1);
		PLock pc2 = pessimisticLockManager.findOrPersistPLock("blublu");
		Assert.assertNotNull(pc2);
		dbInstance.closeSession();
		
		final List<Long> holder = new ArrayList<>(1);
		// first thread acquires the lock and waits and continues holding the lock for some time.
		PLock p1 = pessimisticLockManager.findOrPersistPLock("blabla");
		Assert.assertNotNull(p1);
		
		new Thread(new Runnable() {
			public void run() {
				PLock p2 = pessimisticLockManager.findOrPersistPLock("blublu");
				assertNotNull(p2);
				long p2Acquired = System.nanoTime();
				holder.add(new Long(p2Acquired));
				dbInstance.closeSession();
				
			}}).start();
		
		sleep(500);
		long p1AboutToRelease = System.nanoTime();
		dbInstance.closeSession();
		
		// if row locking is not supported, then the timestamp when p2 has been acquired will be shortly -after- p1 has been released
		boolean singleRowLockingOk = holder.size() > 0 &&  holder.get(0).longValue() < p1AboutToRelease;
		assertTrue("the database does not seem to support row locking when executing 'select for update', critical for performance!, ", singleRowLockingOk);
	}
	
	@Test
	public void testNestedLockingSupported() {
		log.info("testing if nested locking is supported");
		// make sure all three row entries for the locks are created, otherwise the system-wide locking 
		// applied on lock-row-creation cannot support row-level-locking by definition. 

		PLock pc1 = pessimisticLockManager.findOrPersistPLock("blabla");
		assertNotNull(pc1);
		PLock pc2 = pessimisticLockManager.findOrPersistPLock("blublu");
		assertNotNull(pc2);
		PLock pc3 = pessimisticLockManager.findOrPersistPLock("blibli");
		assertNotNull(pc3);
		dbInstance.closeSession();
		
		final List<Long> holder = new ArrayList<>(1);
		// first thread acquires the two locks and waits and continues holding the lock for some time.
		PLock p1 = pessimisticLockManager.findOrPersistPLock("blabla");
		assertNotNull(p1);
		PLock p3 = pessimisticLockManager.findOrPersistPLock("blibli");
		assertNotNull(p3);
		
		new Thread(new Runnable() {
			public void run() {
				PLock p2 = pessimisticLockManager.findOrPersistPLock("blibli");
				assertNotNull(p2);
				long p2Acquired = System.nanoTime();
				holder.add(new Long(p2Acquired));
				dbInstance.closeSession();
				
			}}).start();
		sleep(500);
		boolean acOk = holder.size() == 0;
		//the commit will drop the lock on blibli d
		dbInstance.closeSession();
		sleep(500);
		boolean acNowOk = holder.size() == 1;
		
		// if row locking is not supported, then the timestamp when p2 has been acquired will be shortly -after- p1 has been released
		assertTrue("since holding the blabla lock, no other may acquire it", acOk);
		assertTrue("after having released the blabla lock, a next waiting thread must have acquired it after some time", acNowOk);
	}
	
	@Test
	public void testDeadLockTimeout() {
		log.info("testing if deadlock detection and handling is supported");
		// make sure all three row entries for the locks are created, otherwise the system-wide locking 
		// applied on lock-row-creation cannot support row-level-locking by definition. 

		PLock pc1 = pessimisticLockManager.findOrPersistPLock("blabla");
		assertNotNull(pc1);
		PLock pc2 = pessimisticLockManager.findOrPersistPLock("blublu");
		assertNotNull(pc2);
		PLock pc3 = pessimisticLockManager.findOrPersistPLock("blibli");
		assertNotNull(pc3);
		dbInstance.closeSession();
		
		/**
		 *    t1   t2
		 *    bla  bli
		 *    ..   ..
		 *    ..   ..
		 *    ..   ..
		 *    bli  ..
		 *         ..
		 *         ..
		 *         bla
		 *         -> deadlock! t2 waits on bla (already acquired by t1, but t1 waits on bli, already acquired by t2)
		 *    
		 */
		
	
		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
		final CountDownLatch finishCount = new CountDownLatch(2);
		// t1
		new Thread(new Runnable() {
			public void run() {
				try {
					PLock p1 = pessimisticLockManager.findOrPersistPLock("blabla");
					assertNotNull(p1);
					sleep(250);
					// now try to acquire blibli but that fails, since blibli is already locked by thread 2.
					// but thread 2 cannot continue either, since it is waiting for lock blabla, which is already hold by thread 1
					// -> deadlock
					PLock p3 = pessimisticLockManager.findOrPersistPLock("blibli");
					assertNotNull(p3);					
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					try {
						dbInstance.closeSession();
					} catch (Exception e) {
						// ignore
					}
					finishCount.countDown();
				}	
			}}).start();
		
		// t2
		new Thread(new Runnable() {
			public void run() {
				try {
					PLock p2 = pessimisticLockManager.findOrPersistPLock("blibli");
					assertNotNull(p2);
					sleep(500);
					PLock p3 = pessimisticLockManager.findOrPersistPLock("blabla");
					assertNotNull(p3);
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					try {
						dbInstance.closeSession();
					} catch (Exception e) {
						// ignore
					}
					finishCount.countDown();
				}					
			}}).start();
		
		// sleep until t1 and t2 should have terminated/excepted
		try {
			finishCount.await(8, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Assert.fail("Takes too long (more than 8sec)");
		}
		
		// if not -> they are in deadlock and the db did not detect it
		for (Exception exception : exceptionHolder) {
			log.error("exception: ", exception);
		}
		assertTrue("expected a deadlock exception, but got none", exceptionHolder.size() > 0);				
	}
	
	
	@Test public void testPerf() {
		log.info("testing what the throughput is for the pessimistic locking");
		// test what the throughput is for the pessimistic locking.
		// take 500 threads (created and started with no delay (as fast as the vm can) trying to acquire a plock on 20 different olatresourceables.
		// measure how long that takes. and warn if it exceeds an upper boundary.
		// the server is assumed to have around 2GHz cpu and 2GBytes RAM.
		// the first thread to acquire a new olatres will first lock on the global lock and then create the new entry to lock upon.
		// we therefore also measure how long it takes again when all locks have already been inserted into the plock table.
		// results: on a std. laptop with postgres 8, 500 threads with 20 resourceables take about 3000ms (thread creation inclusive)
		// -> about 
		
		// 1. prepare collection
		int numthreads = 500;
		int numores = 1;
		
		// 2. create 500 threads and start them
		long start = System.currentTimeMillis();
		final CountDownLatch doneSignal = new CountDownLatch(numthreads);
		for (int i = 0; i < numthreads; i++) {
			final String asset = "assetaboutaslongasores"+(i % numores);
			Runnable r = new Runnable() {
				public void run() {
					try {
						pessimisticLockManager.findOrPersistPLock(asset);
						doneSignal.countDown();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						dbInstance.closeSession();
					}
				}
			};
			new Thread(r).start();
		}	

		// 4. wait till all are finished or it takes too long
		try {
			doneSignal.await(60, TimeUnit.SECONDS);
			log.info("perf for Plocktest:testPerf(): "+(System.currentTimeMillis()-start));
		} catch (InterruptedException e) {
			fail("Test takes too long (more than 60s)");
		}
		
		// repeat the same again - this time it should/could be faster
		// 2. create 500 threads and start them
		long start2 = System.currentTimeMillis();
		final CountDownLatch doneSignal2 = new CountDownLatch(numthreads);
		for (int i2 = 0; i2 < numthreads; i2++) {
			final String asset = "assetaboutaslongasores"+(i2 % numores);
			Runnable r = new Runnable() {
				public void run() {
					try {
						pessimisticLockManager.findOrPersistPLock(asset);
						doneSignal2.countDown();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						dbInstance.commitAndCloseSession();
					}
				}
			};
			new Thread(r).start();
		}	

		// 4. wait till all are finished or it takes too long
		
		try {
			boolean interrupt = doneSignal.await(60, TimeUnit.SECONDS);
			log.info("perf (again) for Plocktest:testPerf(): "+(System.currentTimeMillis()-start2));
			assertTrue("Test takes too long (more than 60s)", interrupt);
		} catch (InterruptedException e) {
			fail("" + e.getMessage());
		}
	}
	
	@Test
	public void testSync() {
		log.info("testing enrollment");
		
		int count = 5;
		int maxUsers = isOracleConfigured() ? 5 : 20;// I give less connections
		
		//	 ------------------ now check with lock -------------------
		// create a group
		//	 create users
		final List<Identity> identities = new ArrayList<>();
		for (int i = 0; i < count + maxUsers; i++) {
			Identity id = JunitTestHelper.createAndPersistIdentityAsUser("u-" + i + "-" + UUID.randomUUID().toString());
			identities.add(id);
			log.info("testSync: Identity=" + id.getName() + " created");
		}
		dbInstance.closeSession();

		final SecurityGroup group2 = securityGroupDao.createAndPersistSecurityGroup();
		// make sure the lock has been written to the disk (tests for createOrFind see other methods)
		dbInstance.closeSession();
		
		//prepare threads
		int numOfThreads = count + maxUsers;
		final CountDownLatch finishCount = new CountDownLatch(numOfThreads);

		// try to enrol all in the same group
		for (int i = 0; i < numOfThreads; i++) {
			final int j = i;
			new Thread(new Runnable(){
				public void run() {
					try {
						log.info("testSync: thread started j=" + j);
						Identity id = identities.get(j);
						//
						PLock p2 = pessimisticLockManager.findOrPersistPLock("befinsert");
						assertNotNull(p2);
						doNoLockingEnrol(id, group2, count);
						dbInstance.commit();
					} catch (Exception e) {
						log.error("", e);
					} finally {
						finishCount.countDown();
						dbInstance.closeSession();
					}
				}}).start();
		}
		
		try {
			finishCount.await(120, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error("", e);
		}

		// now count 
		dbInstance.closeSession();
		int cnt2 = securityGroupDao.countIdentitiesOfSecurityGroup(group2);
		assertTrue("cnt should be smaller or eq than allowed since synced with select for update. cnt:"+cnt2+", max "+count, cnt2 <= count);
		assertTrue("cnt should be eq to allowed since synced with select for update. cnt:"+cnt2+", max "+count, cnt2 == count);
		log.info("cnt lock "+cnt2);
	}
	
	
	private void doNoLockingEnrol(Identity i, SecurityGroup group, int count) {
		// check that below max
		try {
			StringBuilder sb = new StringBuilder(128);
			int cnt = securityGroupDao.countIdentitiesOfSecurityGroup(group);
			sb.append("enrol:cnt:"+cnt);
			if (cnt < count) {
				// now sleep a while to allow others to think also that there is still space left in the group
				sleep(100);
				// now add the user to the security group
				sb.append(" adding " + i.getName() + ": current.. " + cnt + ", max = " + count);
				securityGroupDao.addIdentityToSecurityGroup(i, group);
			}
			log.info(sb.toString());
		} catch (Exception e) {
			log.error("", e);
		}
	}
}