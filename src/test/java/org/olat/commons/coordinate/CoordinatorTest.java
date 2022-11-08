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

package org.olat.commons.coordinate;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


public class CoordinatorTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(CoordinatorTest.class);

	
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;
	

	/**
	 * Test with 2 threads T1 & T2.
	 * T1		      T2
	 * doInSync T1-1  sleep 5sec
	 * sleep 10sec    ...
	 * ...            ...
	 * ...            doInSync T2-1
	 * ...            sleep 10sec
	 * ...            ...
	 * doInSync T1-2  ...
	 * finished       ...
	 *                doInSync T2-2
	 *                finished
	 */
	@Test
	public void testDoInSyncWithSyncerExecutor() {

		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
		final List<Boolean> statusList = Collections.synchronizedList(new ArrayList<Boolean>(1));
	
		final CountDownLatch finishCount = new CountDownLatch(2);
		final OLATResourceable ores = OresHelper.createOLATResourceableInstance("testDoInSync", Long.valueOf("123"));
		
		// thread 1
		new Thread(new Runnable() {
			public void run() {
				try {
					// do something in sync
					CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerExecutor(){
						public void execute() {
							log.info("Thread-1: execute doInSync 1");
						}
					});//end syncerCallback
					
					// sleep
					sleep(1000);
					
					// do again do something in sync
					CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerExecutor(){
						public void execute() {
							log.info("Thread-1: execute doInSync 2");
						}
					});//end syncerCallback
					log.info("Thread-1: finished");
					statusList.add(Boolean.TRUE);
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					try {
						DBFactory.getInstance().closeSession();
					} catch (Exception e) {
						// ignore
					}
					finishCount.countDown();
				}	
			}}).start();
		
		// thread 2
		new Thread(new Runnable() {
			public void run() {
				try {
					// sleep
					sleep(500);

					// do something in sync
					CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerExecutor(){
						public void execute() {
							log.info("Thread-2: execute doInSync 1");
						}
					});//end syncerCallback
					
					// sleep
					sleep(1000);
					
					// do again do something in sync
					CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerExecutor(){
						public void execute() {
							log.info("Thread-2: execute doInSync 2");
						}
					});//end syncerCallback
					log.info("Thread-2: finished");
					statusList.add(Boolean.TRUE);
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					try {
						DBFactory.getInstance().closeSession();
					} catch (Exception e) {
						// ignore
					}
					finishCount.countDown();
				}	
			}}).start();
		
		// sleep until t1 and t2 should have terminated/excepted
		try {
			finishCount.await(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Assert.fail("Threads did not finish in 10sec");
		}

		// if not -> they are in deadlock and the db did not detect it
		for (Exception exception : exceptionHolder) {
			log.error("exception: ", exception);
		}
		Assert.assertEquals("It throws an exception in test", 0, exceptionHolder.size());	
	}

	/**
	 * Test with 2 threads T1 & T2.
	 * T1		      T2
	 * doInSync T1-1  sleep 5sec
	 * sleep 10sec    ...
	 * ...            ...
	 * ...            doInSync T2-1
	 * ...            sleep 10sec
	 * ...            ...
	 * doInSync T1-2  ...
	 * finished       ...
	 *                doInSync T2-2
	 *                finished
	 */
	@Test
	public void testDoInSyncWithSyncerCallback() {

		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
		final List<Boolean> statusList = Collections.synchronizedList(new ArrayList<Boolean>(1));
	
		final OLATResourceable ores = OresHelper.createOLATResourceableInstance("testDoInSync", Long.valueOf("123"));
		final CountDownLatch finishCount = new CountDownLatch(2);
		
		// thread 1
		new Thread(new Runnable() {
			public void run() {
				try {
					// do something in sync
					CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<Boolean>(){
						public Boolean execute() {
							log.info("Thread-1: execute doInSync 1");
							return Boolean.TRUE;
						}
					});//end syncerCallback
					
					// sleep
					sleep(1000);
					
					// do again do something in sync
					CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<Boolean>(){
						public Boolean execute() {
							log.info("Thread-1: execute doInSync 2");
							return Boolean.TRUE;
						}
					});//end syncerCallback
					log.info("Thread-1: finished");
					statusList.add(Boolean.TRUE);
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					try {
						DBFactory.getInstance().closeSession();
					} catch (Exception e) {
						// ignore
					}
					finishCount.countDown();
				}	
			}}).start();
		
		// thread 2
		new Thread(new Runnable() {
			public void run() {
				try {
					// sleep
					sleep(500);

					// do something in sync
					CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<Boolean>(){
						public Boolean execute() {
							log.info("Thread-2: execute doInSync 1");
							return Boolean.TRUE;
						}
					});//end syncerCallback
					
					// sleep
					sleep(1000);
					
					// do again do something in sync
					CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<Boolean>(){
						public Boolean execute() {
							log.info("Thread-2: execute doInSync 2");
							return Boolean.TRUE;
						}
					});//end syncerCallback
					log.info("Thread-2: finished");
					statusList.add(Boolean.TRUE);
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					try {
						DBFactory.getInstance().closeSession();
					} catch (Exception e) {
						// ignore
					}
					finishCount.countDown();
				}	
			}}).start();
		
		// sleep until t1 and t2 should have terminated/excepted
		try {
			finishCount.await(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Assert.fail("Test takes too long (more than 10s)");
		}

		// if not -> they are in deadlock and the db did not detect it
		for (Exception exception : exceptionHolder) {
			log.error("exception: ", exception);
		}

		Assert.assertEquals("It throws an exception in test", 0, exceptionHolder.size());	
	}

	@Test(expected = AssertException.class) 
	public void testNestedAssertExceptionInDoInSync() {
		final OLATResourceable ores = OresHelper.createOLATResourceableInstance("testNestedAssertExceptionInDoInSync", Long.valueOf("123"));
		
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<Boolean>(){
			public Boolean execute() {
				log.info("testNestedAssertExceptionInDoInSync: execute doInSync 1");
				
				// Do agin in sync => nested => no allowed!
				CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<Boolean>(){
					public Boolean execute() {
						log.info("testNestedAssertExceptionInDoInSync: execute doInSync 2");
						fail("No NestedAssertException thrown");
						return Boolean.TRUE;
					}
				});//end syncerCallback

				return Boolean.TRUE;
			}
		});//end syncerCallback
	}
	
	@Test
	public void testSyncerAssertAlreadyDoInSyncFor() {
		final OLATResourceable ores = OresHelper.createOLATResourceableInstance("testSyncerAssertAlreadyDoInSyncFor", Long.valueOf("123"));
		
		// 1. check assertAlreadyDoInSyncFor WITHOUT sync-block => AssertException must be thrown
		try {
			CoordinatorManager.getInstance().getCoordinator().getSyncer().assertAlreadyDoInSyncFor(ores);
			fail("Did not throw AssertException");
		} catch (AssertException ex) {
			log.info("testSyncerAssertAlreadyDoInSyncFor: This exception is ok, exception=" + ex.getMessage());
		}

		// 2.check assertAlreadyDoInSyncFor WITH sync-block => No AssertException should occour
		try {
			log.info("testSyncerAssertAlreadyDoInSyncFor: before doInSync");
			CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<Boolean>(){
				public Boolean execute() {
					log.info("testSyncerAssertAlreadyDoInSyncFor: execute before assertAlreadyDoInSyncFor");
					CoordinatorManager.getInstance().getCoordinator().getSyncer().assertAlreadyDoInSyncFor(ores);
					log.info("testSyncerAssertAlreadyDoInSyncFor: execute done");
					return Boolean.TRUE;
				}
			});//end syncerCallback
		}catch(AssertException aex) {
			fail("testSyncerAssertAlreadyDoInSyncFor: got a AssertException=" + aex);
		}
	
	}

	@Test
	public void testDoInSyncPerformance() {
		final OLATResourceable ores = OresHelper.createOLATResourceableInstance(UUID.randomUUID().toString(), Long.valueOf("123989456"));
		OLATResource r =  CoreSpringFactory.getImpl(OLATResourceManager.class).findOrPersistResourceable(ores);
		int maxLoop = 500;

		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		final RepositoryEntry re = repositoryService.create(null, "test", "perfTest", "testPerf", "perfTest description",
				r, RepositoryEntryStatusEnum.trash, defOrganisation);
		// create security group
		repositoryService.update(re);
		DBFactory.getInstance().commitAndCloseSession();
		
		// 1. Do job without doInSync
		log.info("testDoInSyncPerformance: start test with doInSync");
		long startTimeWithoutSync = System.currentTimeMillis();
		for (int i = 0; i<maxLoop ; i++) {
			doTestPerformanceJob(re);
			DBFactory.getInstance().closeSession();
		}
		long endTimeWithoutSync = System.currentTimeMillis();

		// 2. Do job with doInSync
		log.info("testDoInSyncPerformance: start test with doInSync");
		long startTimeDoInSync = System.currentTimeMillis();
		for (int i = 0; i<maxLoop ; i++) {
			CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerExecutor(){
				@Override
				public void execute() {
					doTestPerformanceJob(re);
				}

			});//end syncerCallback
			DBFactory.getInstance().closeSession();
		}
		long endTimeDoInSync = System.currentTimeMillis();
		
		// Compare time
		long timeWithoutSync = endTimeWithoutSync - startTimeWithoutSync;
		float perJobWithoutSync = (float)timeWithoutSync / maxLoop;
		log.info("testDoInSyncPerformance timeWithoutSync=" + timeWithoutSync + " ms for loop with " + maxLoop + " iterations");
		log.info("testDoInSyncPerformance perJobWithoutSync=" + perJobWithoutSync + " ms");

		long timeWithDoInSync = endTimeDoInSync - startTimeDoInSync;
		float perJobWithDoInSync = (float)timeWithDoInSync / maxLoop;
		log.info("testDoInSyncPerformance timeWithDoInSync=" + timeWithDoInSync + " ms for loop with " + maxLoop + " iterations");
		log.info("testDoInSyncPerformance perJobWithDoInSync=" + perJobWithDoInSync + " ms");
		
		long timeDiffLoop = timeWithDoInSync - timeWithoutSync;
		float timeDiffPerCall = perJobWithDoInSync - perJobWithoutSync;
		log.info("testDoInSyncPerformance diffLoop=" + timeDiffLoop + " ms for loop with " + maxLoop + " iterations");
		log.info("testDoInSyncPerformance diffPerCall=" + timeDiffPerCall + " ms");
	}

	private Boolean doTestPerformanceJob(RepositoryEntry re) {
		repositoryService.incrementLaunchCounter(re);
		return Boolean.TRUE;
	}
}