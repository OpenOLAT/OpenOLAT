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
* <p>
*/ 

package org.olat.resource.lock.pessimistic;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;


/**
 * 
 */
public class PLockTest extends OlatTestCase {

	private static final int MAX_COUNT = 5; //5; //30;
	private static final int MAX_USERS_MORE = 20; //20; //100;
	
	private static Logger log = Logger.getLogger(PLockTest.class.getName());
	
	
	@Test public void testReentrantLock() {
		//Ignore Test if DB is HSQLDB. HSQLDB does not support LockMode.UPGRADE, which is a prerequisite for testing PessimisticLockManager 
		assumeTrue(!getHsqlDbConfigured());
		
		long start = System.currentTimeMillis();
		String asset = "p1";
		// make sure the lock is created first
		PLock pc = PessimisticLockManager.getInstance().findOrPersistPLock(asset);
		assertNotNull(pc);
		DBFactory.getInstance().closeSession();
		
		// test double acquisition within same transaction
		PLock pc1 = PessimisticLockManager.getInstance().findOrPersistPLock(asset);
		assertNotNull(pc1);
		PLock pc2 = PessimisticLockManager.getInstance().findOrPersistPLock(asset);
		assertNotNull(pc2);
		DBFactory.getInstance().closeSession();
		
		// and without explicit transaction boundary.
		PLock p1 = PessimisticLockManager.getInstance().findOrPersistPLock(asset);
		assertNotNull(p1);
		PLock p2 = PessimisticLockManager.getInstance().findOrPersistPLock(asset);
		assertNotNull(p2);
		long stop = System.currentTimeMillis();
		long diff = stop - start;
		assertTrue("5 select's took longer than 10 seconds -> deadlock / lock timeout ? dur in ms was:"+diff, diff < 10000);
	}

	/**
	 * T1		T2
	 *
	 */
	@Test public void testReentrantLock2Threads() {
		//Ignore Test if DB is HSQLDB. HSQLDB does not support LockMode.UPGRADE, which is a prerequisite for testing PessimisticLockManager 
		assumeTrue(!getHsqlDbConfigured());

		
		
		
		long start = System.currentTimeMillis();
		final String asset = "p1-2";
		final int thread1Running = 0;
		final int thread2Running = 0;
		
		// make sure the lock is created first
		PLock pc = PessimisticLockManager.getInstance().findOrPersistPLock(asset);
		assertNotNull(pc);
		DBFactory.getInstance().closeSession();

		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
		final List<Boolean> statusList = Collections.synchronizedList(new ArrayList<Boolean>(1));

		// thread 1
		new Thread(new Runnable() {
			public void run() {
				try {
					PLock pc1 = PessimisticLockManager.getInstance().findOrPersistPLock(asset);
					assertNotNull(pc1);
					System.out.println("Thread-1: got PLock pc1=" + pc1);
					System.out.println("Thread-1: sleep 10sec");
					sleep(10000);
					PLock pc2 = PessimisticLockManager.getInstance().findOrPersistPLock(asset);
					assertNotNull(pc2);
					System.out.println("Thread-1: got PLock pc2=" + pc2);
					System.out.println("Thread-1: finished");
					statusList.add(Boolean.TRUE);
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					try {
						DBFactory.getInstance().closeSession();
					} catch (Exception e) {
						// ignore
					};
				}	
			}}).start();
		
		// thread 2
		new Thread(new Runnable() {
			public void run() {
				try {
					System.out.println("Thread-2: sleep 5sec");
					sleep(5000);
					System.out.println("Thread-2: try to get PLock...");
					PLock p1 = PessimisticLockManager.getInstance().findOrPersistPLock(asset);
					assertNotNull(p1);
					System.out.println("Thread-2: got PLock p1=" + p1);
					System.out.println("Thread-2: sleep 10sec");
					sleep(10000);
					PLock p2 = PessimisticLockManager.getInstance().findOrPersistPLock(asset);
					assertNotNull(p2);
					System.out.println("Thread-1: got PLock p2=" + p2);
					System.out.println("Thread-1: finished");
					statusList.add(Boolean.TRUE);
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					try {
						DBFactory.getInstance().closeSession();
					} catch (Exception e) {
						// ignore
					};
				}	
			}}).start();
		
		// sleep until t1 and t2 should have terminated/excepted
		int loopCount = 0;
		while ( (statusList.size()<2) && (exceptionHolder.size()<1) && (loopCount<60)) {
			sleep(1000);
			loopCount++;
		}
		assertTrue("Threads did not finish in 60sec", loopCount<60);
		// if not -> they are in deadlock and the db did not detect it
		for (Exception exception : exceptionHolder) {
			System.out.println("exception: "+exception.getMessage());
			exception.printStackTrace();
		}
		assertTrue("exception in test => see sysout", exceptionHolder.size() == 0);				
	}

	@Test public void testLockWaitTimout() {
		//Ignore Test if DB is HSQLDB. HSQLDB does not support LockMode.UPGRADE, which is a prerequisite for testing PessimisticLockManager 
		assumeTrue(!getHsqlDbConfigured());
		assumeTrue(!isPostgresqlConfigured());//no lock timeout on postgres
		
		System.out.println("testing if holding a lock timeouts");
		// make sure all three row entries for the locks are created, otherwise the system-wide locking 
		// applied on lock-row-creation cannot support row-level-locking by definition. 

		PLock pc3 = PessimisticLockManager.getInstance().findOrPersistPLock("blibli");
		assertNotNull(pc3);
		DBFactory.getInstance().closeSession();
		
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
		
		// t1
		new Thread(new Runnable() {
			public void run() {
				try {
					sleep(2500);
					PLock p3 = PessimisticLockManager.getInstance().findOrPersistPLock("blibli");
					assertNotNull(p3);					
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					try {
						DBFactory.getInstance().closeSession();
					} catch (Exception e) {
						// ignore
					};
				}	
			}}).start();
		
		// t2
		new Thread(new Runnable() {
			public void run() {
				try {
					PLock p2 = PessimisticLockManager.getInstance().findOrPersistPLock("blibli");
					assertNotNull(p2);
					sleep(60000);
					// holding the lock for more than the transaction timeout (normally 30secs, configured where? hib) should cause a lock timeout
					// if the db is configured so.
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					try {
						DBFactory.getInstance().closeSession();
					} catch (Exception e) {
						// ignore
					};
				}					
			}}).start();
		
		// sleep until t1 and t2 should have terminated/excepted
		System.out.println("Sleep 55s");
		sleep(55000);
		
		// if not -> they are in deadlock and the db did not detect it
		for (Exception exception : exceptionHolder) {
			System.out.println("exception: "+exception.getMessage());
			exception.printStackTrace();
		}
		assertTrue("expected a lock wait timeout exceeded exception", exceptionHolder.size() > 0);				
	}
	
	@Test public void testSingleRowLockingSupported() {
		//Ignore Test if DB is HSQLDB. HSQLDB does not support LockMode.UPGRADE, which is a prerequisite for testing PessimisticLockManager 
		assumeTrue(!getHsqlDbConfigured());

		
		
		System.out.println("testing if one lock only locks the given row and not the complete table (test whether the database supports rowlocking)");
		// make sure both row entries for the locks are created, otherwise the system-wide locking 
		// applied on lock-row-creation cannot support row-level-locking by definition. 
		PLock pc1 = PessimisticLockManager.getInstance().findOrPersistPLock("blabla");
		assertNotNull(pc1);
		PLock pc2 = PessimisticLockManager.getInstance().findOrPersistPLock("blublu");
		assertNotNull(pc2);
		DBFactory.getInstance().closeSession();
		
		final List<Long> holder = new ArrayList<Long>(1);
		// first thread acquires the lock and waits and continues holding the lock for some time.
		PLock p1 = PessimisticLockManager.getInstance().findOrPersistPLock("blabla");
		assertNotNull(p1);
		
		new Thread(new Runnable() {
			public void run() {
				PLock p2 = PessimisticLockManager.getInstance().findOrPersistPLock("blublu");
				assertNotNull(p2);
				long p2Acquired = System.currentTimeMillis();
				holder.add(new Long(p2Acquired));
				DBFactory.getInstance().closeSession();
				
			}}).start();
		
		sleep(5000);
		long p1AboutToRelease= System.currentTimeMillis();
		DBFactory.getInstance().closeSession();
		
		// if row locking is not supported, then the timestamp when p2 has been acquired will be shortly -after- p1 has been released
		boolean singleRowLockingOk = holder.size() >0 &&  holder.get(0).longValue() < p1AboutToRelease;
		assertTrue("the database does not seem to support row locking when executing 'select for update', critical for performance!, ", singleRowLockingOk);
	}
	
	@Test public void testNestedLockingSupported() {
		//Ignore Test if DB is HSQLDB. HSQLDB does not support LockMode.UPGRADE, which is a prerequisite for testing PessimisticLockManager 
		assumeTrue(!getHsqlDbConfigured());

		
		
		System.out.println("testing if nested locking is supported");
		// make sure all three row entries for the locks are created, otherwise the system-wide locking 
		// applied on lock-row-creation cannot support row-level-locking by definition. 

		PLock pc1 = PessimisticLockManager.getInstance().findOrPersistPLock("blabla");
		assertNotNull(pc1);
		PLock pc2 = PessimisticLockManager.getInstance().findOrPersistPLock("blublu");
		assertNotNull(pc2);
		PLock pc3 = PessimisticLockManager.getInstance().findOrPersistPLock("blibli");
		assertNotNull(pc3);
		DBFactory.getInstance().closeSession();
		
		final List<Long> holder = new ArrayList<Long>(1);
		// first thread acquires the two locks and waits and continues holding the lock for some time.
		PLock p1 = PessimisticLockManager.getInstance().findOrPersistPLock("blabla");
		assertNotNull(p1);
		PLock p3 = PessimisticLockManager.getInstance().findOrPersistPLock("blibli");
		assertNotNull(p3);
		
		new Thread(new Runnable() {
			public void run() {
				PLock p2 = PessimisticLockManager.getInstance().findOrPersistPLock("blibli");
				assertNotNull(p2);
				long p2Acquired = System.currentTimeMillis();
				holder.add(new Long(p2Acquired));
				DBFactory.getInstance().closeSession();
				
			}}).start();
		sleep(5000);
		boolean acOk = holder.size() == 0;
		DBFactory.getInstance().closeSession();
		sleep(5000);
		boolean acNowOk = holder.size() == 1;
		
		// if row locking is not supported, then the timestamp when p2 has been acquired will be shortly -after- p1 has been released
		assertTrue("since holding the blabla lock, no other may acquire it", acOk);
		assertTrue("after having released the blabla lock, a next waiting thread must have acquired it after some time", acNowOk);
	}
	
	@Test public void testDeadLockTimeout() {
		//Ignore Test if DB is HSQLDB. HSQLDB does not support LockMode.UPGRADE, which is a prerequisite for testing PessimisticLockManager 
		assumeTrue(!getHsqlDbConfigured());

		
		
		System.out.println("testing if deadlock detection and handling is supported");
		// make sure all three row entries for the locks are created, otherwise the system-wide locking 
		// applied on lock-row-creation cannot support row-level-locking by definition. 

		PLock pc1 = PessimisticLockManager.getInstance().findOrPersistPLock("blabla");
		assertNotNull(pc1);
		PLock pc2 = PessimisticLockManager.getInstance().findOrPersistPLock("blublu");
		assertNotNull(pc2);
		PLock pc3 = PessimisticLockManager.getInstance().findOrPersistPLock("blibli");
		assertNotNull(pc3);
		DBFactory.getInstance().closeSession();
		
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
		
		// t1
		new Thread(new Runnable() {
			public void run() {
				try {
					PLock p1 = PessimisticLockManager.getInstance().findOrPersistPLock("blabla");
					assertNotNull(p1);
					sleep(2500);
					// now try to acquire blibli but that fails, since blibli is already locked by thread 2.
					// but thread 2 cannot continue either, since it is waiting for lock blabla, which is already hold by thread 1
					// -> deadlock
					PLock p3 = PessimisticLockManager.getInstance().findOrPersistPLock("blibli");
					assertNotNull(p3);					
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					try {
						DBFactory.getInstance().closeSession();
					} catch (Exception e) {
						// ignore
					};
				}	
			}}).start();
		
		// t2
		new Thread(new Runnable() {
			public void run() {
				try {
					PLock p2 = PessimisticLockManager.getInstance().findOrPersistPLock("blibli");
					assertNotNull(p2);
					sleep(5000);
					PLock p3 = PessimisticLockManager.getInstance().findOrPersistPLock("blabla");
					assertNotNull(p3);
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					try {
						DBFactory.getInstance().closeSession();
					} catch (Exception e) {
						// ignore
					};
				}					
			}}).start();
		
		// sleep until t1 and t2 should have terminated/excepted
		sleep(8000);
		// if not -> they are in deadlock and the db did not detect it
		for (Exception exception : exceptionHolder) {
			System.out.println("exception: "+exception.getMessage());
			exception.printStackTrace();
		}
		assertTrue("expected a deadlock exception, but got none", exceptionHolder.size() > 0);				
	}
	
	
	@Test public void testPerf() {
		//Ignore Test if DB is HSQLDB. HSQLDB does not support LockMode.UPGRADE, which is a prerequisite for testing PessimisticLockManager 
		assumeTrue(!getHsqlDbConfigured());

		
		
		System.out.println("testing what the throughput is for the pessimistic locking");
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
		int maxwait = 12; // seconds to wait for completion (upper performance boundary)
		class Collector {
			private int threadsDone = 0;
			synchronized void incThreadDone() {
				threadsDone++;
			}
			
			synchronized int getThreadsDoneCnt() {
				return threadsDone;
			}
		};
		
		// 2. create 500 threads and start them
		long start = System.currentTimeMillis();
		final Collector c = new Collector();
		for (int i = 0; i < numthreads; i++) {
			final String asset = "assetaboutaslongasores"+(i % numores);
			Runnable r = new Runnable() {
				public void run() {
					PessimisticLockManager.getInstance().findOrPersistPLock(asset);
					c.incThreadDone();
					DBFactory.getInstance().closeSession();
				}
			};
			new Thread(r).start();
		}	
		int i;
		// 4. wait till all are finished or it takes too long
		for (i = 0; i < maxwait; i++) {
			int donecnt = c.getThreadsDoneCnt();
			if (donecnt < numthreads) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					//
				}
			} else { // done
				break;
			}
		}
		long stop = System.currentTimeMillis();
		System.out.println("perf for Plocktest:testPerf(): "+(stop-start));
		assertTrue("performance is not within boundary:"+maxwait, i<maxwait);
		
		// repeat the same again - this time it should/could be faster
		// 2. create 500 threads and start them
		long start2 = System.currentTimeMillis();
		final Collector c2 = new Collector();
		for (int i2 = 0; i2 < numthreads; i2++) {
			final String asset = "assetaboutaslongasores"+(i2 % numores);
			Runnable r = new Runnable() {
				public void run() {
					PessimisticLockManager.getInstance().findOrPersistPLock(asset);
					c2.incThreadDone();
					DBFactory.getInstance().closeSession();
				}
			};
			new Thread(r).start();
		}	

		// 4. wait till all are finished or it takes too long
		for (i = 0; i < maxwait; i++) {
			int donecnt = c2.getThreadsDoneCnt();
			if (donecnt < numthreads) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					//
				}
			} else { // done
				break;
			}
		}
		long stop2 = System.currentTimeMillis();
		System.out.println("perf (again) for Plocktest:testPerf(): "+(stop2-start2));
		assertTrue("performance is not within boundary:"+maxwait, i<maxwait);
	}
	
	@Test public void testSync() {
		//Ignore Test if DB is HSQLDB. HSQLDB does not support LockMode.UPGRADE, which is a prerequisite for testing PessimisticLockManager 
		assumeTrue(!getHsqlDbConfigured());

		
		
		
		System.out.println("testing enrollment");
    //	 ------------------ now check with lock -------------------
		// create a group
		//	 create users
		final List<Identity> identities = new ArrayList<Identity>();
		for (int i = 0; i < MAX_COUNT + MAX_USERS_MORE; i++) {
			Identity i1 = JunitTestHelper.createAndPersistIdentityAsUser("u"+i);
			identities.add(i1);
			System.out.println("testSync: Identity=" + "u"+i + "created");
			DBFactory.getInstance().closeSession();
		}

		
		final SecurityGroup group2 = BaseSecurityManager.getInstance().createAndPersistSecurityGroup();
		// make sure the lock has been written to the disk (tests for createOrFind see other methods)
		DBFactory.getInstance().closeSession();
		//PLock p1 = PessimisticLockManager.getInstance().findOrPersistPLock("befinsert");
		//assertNotNull(p1);

		
		// try to enrol all in the same group
		for (int i = 0; i < MAX_COUNT + MAX_USERS_MORE; i++) {
			final int j = i;
			new Thread(new Runnable(){
				public void run() {
					try {
						System.out.println("testSync: thread started j=" + j);
						Identity id = identities.get(j);
						//
						PLock p2 = PessimisticLockManager.getInstance().findOrPersistPLock("befinsert");
						assertNotNull(p2);
						doNoLockingEnrol(id, group2);
						DBFactory.getInstance().closeSession();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}}).start();
		}		
		
		sleep(20000);
		// now count 
		DBFactory.getInstance().closeSession();
		int cnt2 = BaseSecurityManager.getInstance().countIdentitiesOfSecurityGroup(group2);
		assertTrue("cnt should be smaller or eq than allowed since synced with select for update. cnt:"+cnt2+", max "+MAX_COUNT, cnt2 <= MAX_COUNT);
		assertTrue("cnt should be eq to allowed since synced with select for update. cnt:"+cnt2+", max "+MAX_COUNT, cnt2 == MAX_COUNT);
		System.out.println("cnt lock "+cnt2);
	}
	
	
	void doNoLockingEnrol(Identity i, SecurityGroup group) {
		// check that below max
		try {
			StringBuilder sb = new StringBuilder();
			int cnt = BaseSecurityManager.getInstance().countIdentitiesOfSecurityGroup(group);
			sb.append("enrol:cnt:"+cnt);
			if (cnt < MAX_COUNT) {
				// now sleep a while to allow others to think also that there is still space left in the group
				sleep(100);
				// now add the user to the security group
				sb.append(" adding "+i.getName()+": current.. "+cnt+", max = "+MAX_COUNT);
				BaseSecurityManager.getInstance().addIdentityToSecurityGroup(i, group);
			}
			System.out.println(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param milis the duration in miliseconds to sleep
	 */
	private void sleep(int milis) {
		try {
			Thread.sleep(milis);
		} catch (InterruptedException e) {
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