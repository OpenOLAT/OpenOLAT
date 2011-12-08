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

package org.olat.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.commons.coordinate.cluster.ClusterSyncer;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.Syncer;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JMSCodePointServerJunitHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.testutils.codepoints.client.BreakpointStateException;
import org.olat.testutils.codepoints.client.CodepointClient;
import org.olat.testutils.codepoints.client.CodepointClientFactory;
import org.olat.testutils.codepoints.client.CodepointRef;
import org.olat.testutils.codepoints.client.CommunicationException;
import org.olat.testutils.codepoints.client.TemporaryPausedThread;

/**
 * Initial Date:  Mar 26, 2004
 *
 * @author gnaegi
 * 
 * Comment:  
 * 
 */
public class RepositoryManagerTest extends OlatTestCase {
	private static final OLog log = Tracing.createLoggerFor(RepositoryManagerTest.class);
	private static String CODEPOINT_SERVER_ID = "RepositoryManagerTest";
	
	private static final String FG_TYPE = UUID.randomUUID().toString().replace("_", "");
	private static final String CG_TYPE = UUID.randomUUID().toString().replace("-", "");

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before public void setup() {
		try {
			// Setup for code-points
			JMSCodePointServerJunitHelper.startServer(CODEPOINT_SERVER_ID);
		} catch (Exception e) {
			log.error("Error while setting up activeMq or Codepointserver", e);
		}
	}
	
	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@After public void tearDown() {
		try {
			JMSCodePointServerJunitHelper.stopServer();
			DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			log.error("tearDown failed", e);
		}
	}

	/**
	 * Test creation of a repository entry.
	 */
	@Test
	public void testRawRepositoryEntryCreate() {
		try {
			DB db = DBFactory.getInstance();
			OLATResourceManager rm = OLATResourceManager.getInstance();
			// create course and persist as OLATResourceImpl
			OLATResourceable resourceable = new OLATResourceable() {
					public String getResourceableTypeName() {	return "RepoMgrTestCourse";}
					public Long getResourceableId() {return CodeHelper.getForeverUniqueID();}
			};
			OLATResource r =  rm.createOLATResourceInstance(resourceable);
			db.saveObject(r);
	
			// now make a repository entry for this course
			RepositoryEntry d = new RepositoryEntry();
			d.setOlatResource(r);
			d.setResourcename("Lernen mit OLAT");
			d.setInitialAuthor("Florian Gnägi");
			d.setDisplayname("JunitTest_RepositoryEntry");
			db.saveObject(d);
		} catch(Exception ex) {
			fail("No Exception allowed. ex=" + ex.getMessage());
		}
	}
	
	/**
	 */
	@Test public void testQueryReferencableResourcesLimitType() {
		DB db = DBFactory.getInstance();
		RepositoryManager rm = RepositoryManager.getInstance();
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsAuthor("id1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsAuthor("id2");

		// generate 5000 repo entries
		int numbRes = 5000;
		long startCreate = System.currentTimeMillis();
		for (int i = 1; i < numbRes; i++) {
			// create course and persist as OLATResourceImpl
			RepositoryEntry re = createRepositoryEntryFG(i);				
			if ((i % 2 > 0)) {
				re.setCanReference(true);
			} else {
				re.setCanReference(false);
			}
			// create security group
			SecurityGroup ownerGroup = securityManager.createAndPersistSecurityGroup();
			// member of this group may modify member's membership
			securityManager.createAndPersistPolicy(ownerGroup, Constants.PERMISSION_ACCESS, ownerGroup);
			// members of this group are always authors also
			securityManager.createAndPersistPolicy(ownerGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR);
			if ((i % 2 > 0)) {
				securityManager.addIdentityToSecurityGroup(id1, ownerGroup);
			} else {
				securityManager.addIdentityToSecurityGroup(id2, ownerGroup);				
			}
			re.setOwnerGroup(ownerGroup);
			// save the repository entry
			rm.saveRepositoryEntry(re);
			// Create course admin policy for owner group of repository entry
			// -> All owners of repository entries are course admins
			securityManager.createAndPersistPolicy(re.getOwnerGroup(), Constants.PERMISSION_ADMIN, re.getOlatResource());	
			
			// flush database and hibernate session cache after 10 records to improve performance
			// without this optimization, the first entries will be fast but then the adding new 
			// entries will slow down due to the fact that hibernate needs to adjust the size of
			// the session cache permanently. flushing or transactions won't help since the problem
			// is in the session cache. 
			if (i%10 == 0) {
				db.closeSession();
				db = DBFactory.getInstance();
			}
		}
		long endCreate = System.currentTimeMillis();
		log.debug("created " + numbRes + " repo entries in " + (endCreate - startCreate) + "ms");
		
		List<String> typelist = Collections.singletonList(FG_TYPE);
		// finally the search query
		long startSearchReferencable = System.currentTimeMillis();
		List results = rm.queryReferencableResourcesLimitType(id1, new Roles(false, false, false, true, false, false, false), typelist, null, null, null);
		long endSearchReferencable = System.currentTimeMillis();
		log.debug("found " + results.size() + " repo entries " + (endSearchReferencable - startSearchReferencable) + "ms");

		// only half of the items should be found
		assertEquals((int) (numbRes / 2), results.size());
		
		// inserting must take longer than searching, otherwhise most certainly we have a problem somewhere in the query
		assertTrue((endCreate - startCreate) > (endSearchReferencable - startSearchReferencable));
		
	}

	private RepositoryEntry createRepositoryEntryFG(final int i) {
		DB db = DBFactory.getInstance();
		
		OLATResourceable resourceable = new OLATResourceable() {
			public String getResourceableTypeName() {	return FG_TYPE;}
			public Long getResourceableId() {return new Long(i); }
		};
		OLATResource r =  OLATResourceManager.getInstance().createOLATResourceInstance(resourceable);
		db.saveObject(r);
		
		// now make a repository entry for this course
		final RepositoryEntry re = RepositoryManager.getInstance().createRepositoryEntryInstance("Florian Gnägi", "Lernen mit OLAT " + i, "yo man description bla bla + i");
		re.setDisplayname("JunitTest_RepositoryEntry_" + i);		
		re.setOlatResource(r);
		re.setAccess(RepositoryEntry.ACC_OWNERS_AUTHORS);
		return re;
	}
	
	@Test public void testCountByTypeLimitAccess() {
		RepositoryManager rm = RepositoryManager.getInstance();
		int count = rm.countByTypeLimitAccess("unkown", RepositoryEntry.ACC_OWNERS_AUTHORS);
    assertEquals("Unkown type must return 0 elements", 0,count);
    int countValueBefore = rm.countByTypeLimitAccess(FG_TYPE, RepositoryEntry.ACC_OWNERS_AUTHORS);
    // add 1 entry
    RepositoryEntry re = createRepositoryEntryFG(999999);
		// create security group
		SecurityGroup ownerGroup = BaseSecurityManager.getInstance().createAndPersistSecurityGroup();
		re.setOwnerGroup(ownerGroup);
    rm.saveRepositoryEntry(re);
    count = rm.countByTypeLimitAccess(FG_TYPE, RepositoryEntry.ACC_OWNERS_AUTHORS);
    // check count must be one more element
    assertEquals("Add one course repository-entry, but countByTypeLimitAccess does NOT return one more element", countValueBefore + 1,count);
	}
	
	/**
	 * 
	 */
	@Test public void testIncrementLaunchCounter() {
		Syncer syncer = CoordinatorManager.getInstance().getCoordinator().getSyncer();
		assertTrue("syncer is not of type 'ClusterSyncer'", syncer instanceof ClusterSyncer);
		RepositoryEntry repositoryEntry = createRepositoryCG("T1_perf2");		
		final Long keyRepo = repositoryEntry.getKey();
		final OLATResourceable resourceable = repositoryEntry.getOlatResource();
		assertNotNull(resourceable);
		DBFactory.getInstance().closeSession();
		assertEquals("Launch counter was not 0", 0, repositoryEntry.getLaunchCounter() );
		final int mainLoop = 10;
		final int loop = 50;  // 10 * 50 = 500
		for (int m = 0; m < mainLoop; m++) {
			long startTime = System.currentTimeMillis();
			for (int i = 0; i < loop; i++) {
				// 1. load RepositoryEntry
				RepositoryEntry repositoryEntryT1 = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
				RepositoryManager.getInstance().incrementLaunchCounter(repositoryEntryT1);
				DBFactory.getInstance().closeSession();
			}
			long endTime = System.currentTimeMillis();
			System.out.println("testIncrementLaunchCounter time=" + (endTime - startTime) + " for " + loop + " incrementLaunchCounter calls");
			sleep(2000);
		}
		sleep(20000);
		RepositoryEntry repositoryEntry2 = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
		assertEquals("Wrong value of incrementLaunch counter",mainLoop * loop,repositoryEntry2.getLaunchCounter());
		System.out.println("testIncrementLaunchCounter finished");
	}
	
	/**
	 * 
	 */
	@Test public void testIncrementDownloadCounter() {
		Syncer syncer = CoordinatorManager.getInstance().getCoordinator().getSyncer();
		assertTrue("syncer is not of type 'ClusterSyncer'", syncer instanceof ClusterSyncer);
		RepositoryEntry repositoryEntry = createRepositoryCG("T1_perf2");		
		final Long keyRepo = repositoryEntry.getKey();
		final OLATResourceable resourceable = repositoryEntry.getOlatResource();
		assertNotNull(resourceable);
		DBFactory.getInstance().closeSession();
		assertEquals("Download counter was not 0", 0, repositoryEntry.getDownloadCounter() );
		final int mainLoop = 10;
		final int loop = 50;  // 10 * 50 = 500
		for (int m = 0; m < mainLoop; m++) {
			long startTime = System.currentTimeMillis();
			for (int i = 0; i < loop; i++) {
				// 1. load RepositoryEntry
				RepositoryEntry repositoryEntryT1 = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
				RepositoryManager.getInstance().incrementDownloadCounter(repositoryEntryT1);
				DBFactory.getInstance().closeSession();
			}
			long endTime = System.currentTimeMillis();
			System.out.println("testIncrementDownloadCounter time=" + (endTime - startTime) + " for " + loop + " incrementDownloadCounter calls");
			sleep(2000);
		}
		sleep(20000);
		RepositoryEntry repositoryEntry2 = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
		assertEquals("Wrong value of incrementLaunch counter",mainLoop * loop,repositoryEntry2.getDownloadCounter());
		System.out.println("testIncrementDownloadCounter finished");
	}


	/**
	 * Test synchronization between same RepositoryEntry and setLastUsageNowFor, incrementLaunchCounter and incrementDownloadCounter.
	 * This test starts 4 threads : 
	 *   2 to call setLastUsageNowFor, 
	 *   1 to call incrementLaunchCounter 
	 *   1 to call incrementDownloadCounter 
	 * Breakpoint is set for 'setLastUsageNowFor', all other calls must wait.
	 */
	@Test public void testSetLastUsageNowFor() {
		Date lastSetLastUsageDate = null;
		Syncer syncer = CoordinatorManager.getInstance().getCoordinator().getSyncer();
		assertTrue("syncer is not of type 'ClusterSyncer'", syncer instanceof ClusterSyncer);
		final int loop = 500;
		RepositoryEntry repositoryEntry = createRepositoryCG("T1_perf2");		
		final Long keyRepo = repositoryEntry.getKey();
		final OLATResourceable resourceable = repositoryEntry.getOlatResource();
		assertNotNull(resourceable);
		DBFactory.getInstance().closeSession();
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < loop; i++) {
			// 1. load RepositoryEntry
			RepositoryEntry repositoryEntryT1 = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
			RepositoryManager.setLastUsageNowFor(repositoryEntryT1);
			lastSetLastUsageDate = Calendar.getInstance().getTime();
			DBFactory.getInstance().closeSession();
		}
		long endTime = System.currentTimeMillis();
		sleep(20000);
		RepositoryEntry repositoryEntry2 = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
		assertTrue("Wrong date-value of lastUsage, could not be before ",repositoryEntry2.getLastUsage().after(lastSetLastUsageDate) );
		System.out.println("testSetLastUsageNowFor time=" + (endTime - startTime) + " for " + loop + " testSetLastUsageNowFor calls");
		System.out.println("testSetLastUsageNowFor finished");
	}
	
	@Test public void testConcurrentIncrementLaunchCounter() {
		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
		final List<Boolean> statusList = Collections.synchronizedList(new ArrayList<Boolean>(1));

		Syncer syncer = CoordinatorManager.getInstance().getCoordinator().getSyncer();
		assertTrue("syncer is not of type 'ClusterSyncer'", syncer instanceof ClusterSyncer);
		final int loop = 100;
		final int numberOfThreads = 3;
		RepositoryEntry repositoryEntry = createRepositoryCG("T1_perf2");		
		final Long keyRepo = repositoryEntry.getKey();
		final OLATResourceable resourceable = repositoryEntry.getOlatResource();
		assertNotNull(resourceable);
		DBFactory.getInstance().closeSession();
		assertEquals("Launch counter was not 0", 0, repositoryEntry.getLaunchCounter() );
		long startTime = System.currentTimeMillis();
		// start thread 1 : incrementLaunchCounter / setAccess
		new Thread(new Runnable() {
			public void run() {
				try {
					for (int i = 1; i <= loop; i++) {
						// 1. load RepositoryEntry
						RepositoryEntry repositoryEntryT1 = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
						RepositoryManager.getInstance().incrementLaunchCounter(repositoryEntryT1);
						if (i % 20 == 0 ) {
							int ACCESS_VALUE = 4;
							System.out.println("RepositoryManagerTest: call setAccess i=" + i);
							//fxdiff VCRP-1,2: access control of resources
							RepositoryManager.getInstance().setAccess(repositoryEntryT1, ACCESS_VALUE, false);
							DBFactory.getInstance().closeSession();
							RepositoryEntry repositoryEntryT1Reloaded = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
							assertEquals("Wrong access value",ACCESS_VALUE,repositoryEntryT1Reloaded.getAccess());
						} else if (i % 10 == 0 ) {
							int ACCESS_VALUE = 1;
							System.out.println("RepositoryManagerTest: call setAccess i=" + i);
							//fxdiff VCRP-1,2: access control of resources
							RepositoryManager.getInstance().setAccess(repositoryEntryT1, ACCESS_VALUE, false);
							DBFactory.getInstance().closeSession();
							RepositoryEntry repositoryEntryT1Reloaded = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
							assertEquals("Wrong access value",ACCESS_VALUE,repositoryEntryT1Reloaded.getAccess());
						}
						DBFactory.getInstance().closeSession();
					}
					System.out.println("Thread-1: finished");
					statusList.add(Boolean.TRUE);
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					try {
						DBFactory.getInstance().closeSession();
					} catch (Exception e) {
						// ignore
					}
				}	
			}}).start();
		// start thread 2 : incrementLaunchCounter / setDescriptionAndName
		new Thread(new Runnable() {
			public void run() {
				try {
					for (int i = 1; i <= loop; i++) {
						// 1. load RepositoryEntry
						RepositoryEntry repositoryEntryT1 = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
						RepositoryManager.getInstance().incrementLaunchCounter(repositoryEntryT1);
						if (i % 25 == 0 ) {
							String displayName = "DisplayName" + i;
							String description = "Description" + i;
							System.out.println("RepositoryManagerTest: call setDescriptionAndName");
							RepositoryManager.getInstance().setDescriptionAndName(repositoryEntryT1, displayName,description);
							DBFactory.getInstance().closeSession();
							RepositoryEntry repositoryEntryT1Reloaded = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
							assertEquals("Wrong displayName value",displayName,repositoryEntryT1Reloaded.getDisplayname());
							assertEquals("Wrong description value",description,repositoryEntryT1Reloaded.getDescription());
						}
						DBFactory.getInstance().closeSession();
					}
					System.out.println("Thread-1: finished");
					statusList.add(Boolean.TRUE);
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					try {
						DBFactory.getInstance().closeSession();
					} catch (Exception e) {
						// ignore
					}
				}	
			}}).start();
		// start thread 3
		new Thread(new Runnable() {
			public void run() {
				try {
					for (int i = 1; i <= loop; i++) {
						// 1. load RepositoryEntry
						RepositoryEntry repositoryEntryT1 = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
						RepositoryManager.getInstance().incrementLaunchCounter(repositoryEntryT1);
						if (i % 30 == 0 ) {
							System.out.println("RepositoryManagerTest: call setProperties i=" + i);
							RepositoryManager.getInstance().setProperties(repositoryEntryT1, true, false, true, false);	
							DBFactory.getInstance().closeSession();
							RepositoryEntry repositoryEntryT1Reloaded = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
							assertEquals("Wrong canCopy value",true,repositoryEntryT1Reloaded.getCanCopy());
							assertEquals("Wrong getCanReference value",false,repositoryEntryT1Reloaded.getCanReference());
							assertEquals("Wrong getCanLaunch value",true,repositoryEntryT1Reloaded.getCanLaunch());
							assertEquals("Wrong getCanDownload value",false,repositoryEntryT1Reloaded.getCanDownload());
						} else 	if (i % 15 == 0 ) {
							System.out.println("RepositoryManagerTest: call setProperties i=" + i);
							RepositoryManager.getInstance().setProperties(repositoryEntryT1, false, true, false, true);	
							DBFactory.getInstance().closeSession();
							RepositoryEntry repositoryEntryT1Reloaded = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
							assertEquals("Wrong canCopy value",false,repositoryEntryT1Reloaded.getCanCopy());
							assertEquals("Wrong getCanReference value",true,repositoryEntryT1Reloaded.getCanReference());
							assertEquals("Wrong getCanLaunch value",false,repositoryEntryT1Reloaded.getCanLaunch() );
							assertEquals("Wrong getCanDownload value",true,repositoryEntryT1Reloaded.getCanDownload());
						}
						DBFactory.getInstance().closeSession();
					}
					System.out.println("Thread-1: finished");
					statusList.add(Boolean.TRUE);
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					try {
						DBFactory.getInstance().closeSession();
					} catch (Exception e) {
						// ignore
					}
				}	
			}}).start();
		
		long endTime = System.currentTimeMillis();
		sleep(20000);
		RepositoryEntry repositoryEntry2 = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
		assertEquals("Worng value of incrementLaunch counter",loop * numberOfThreads,repositoryEntry2.getLaunchCounter());
		System.out.println("testConcurrentIncrementLaunchCounter time=" + (endTime - startTime) + " for " + loop + " incrementLaunchCounter calls");
		System.out.println("testConcurrentIncrementLaunchCounter finished");
	}

	/**
	 * Compare async increment-call with sync 'setDscription' call.
	 */
	@Test public void testIncrementLaunchCounterSetDescription() {
		System.out.println("testIncrementLaunchCounterSetDescription: START...");
		RepositoryEntry repositoryEntry = createRepositoryCG("testIncrementLaunchCounterSetDescription");
		DBFactory.getInstance().closeSession();
		final Long keyRepo = repositoryEntry.getKey();
		RepositoryEntry repositoryEntryT1 = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
		System.out.println("RepositoryManagerTest: call incrementLaunchCounter");
		long t1 = System.nanoTime();
		RepositoryManager.getInstance().incrementLaunchCounter(repositoryEntryT1);
		long t2 = System.nanoTime();
		System.out.println("RepositoryManagerTest: call incrementLaunchCounter DONE");
		String displayName = "DisplayName_testIncrementLaunchCounterSetDescription";
		String description = "Description_testIncrementLaunchCounterSetDescription";
		System.out.println("RepositoryManagerTest: call setDescriptionAndName");
		long t3 = System.nanoTime();
		RepositoryManager.getInstance().setDescriptionAndName(repositoryEntryT1, displayName,description);
		long t4 = System.nanoTime();
		System.out.println("RepositoryManagerTest: call setDescriptionAndName DONE");
		System.out.println("RepositoryManagerTest: increments take=" + (t2 - t1) + " setDescription take=" + (t4 -t3) );
		DBFactory.getInstance().closeSession();
		RepositoryEntry repositoryEntryT1Reloaded = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
		assertEquals("Wrong displayName value",displayName,repositoryEntryT1Reloaded.getDisplayname());
		assertEquals("Wrong description value",description,repositoryEntryT1Reloaded.getDescription());
		System.out.println("testIncrementLaunchCounterSetDescription: FINISHED");
	}
	
	/**
	 * Modify a repository-entry from three thread to check if the exception does not pop up.  
	 * Thread 1 : call incrementDownloadCounter after 100ms
	 * Thread 2 : call incrementDownloadCounter after 300ms
	 * Thread 3 : update access-value on repository-entry directly after 200ms
	 * Codepoint-breakpoint at IncrementDownloadCounterBackgroundTask in executeTask before update
	 */
	@Ignore //the test works random
	@Test public void testConcurrentIncrementLaunchCounterWithCodePoints() {
		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));

		RepositoryEntry repositoryEntry = createRepositoryCG("IncCodePoint");		
		final Long keyRepo = repositoryEntry.getKey();
		final OLATResourceable resourceable = repositoryEntry.getOlatResource();
		assertNotNull(resourceable);
		final int access = 4;
		DBFactory.getInstance().closeSession();
		assertEquals("Launch counter was not 0", 0, repositoryEntry.getLaunchCounter() );

		// enable breakpoint
		CodepointClient codepointClient = null;
		CodepointRef codepointRef = null;
		try {
			codepointClient = CodepointClientFactory.createCodepointClient("vm://localhost?broker.persistent=false", CODEPOINT_SERVER_ID);
			codepointRef = codepointClient.getCodepoint("org.olat.repository.async.IncrementDownloadCounterBackgroundTask.executeTask-before-update");
			codepointRef.enableBreakpoint();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not initialzed CodepointClient");
		}
		

		final CountDownLatch doneSignal = new CountDownLatch(3);
		
		// thread 1
		Thread thread1 = new Thread() {
			public void run() {
				try {
					Thread.sleep(100);
					RepositoryEntry repositoryEntryT1 = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
					RepositoryManager.getInstance().incrementDownloadCounter(repositoryEntryT1);
					System.out.println("testConcurrentIncrementLaunchCounterWithCodePoints: Thread1 incremented download-counter");
				} catch (Exception ex) {
					exceptionHolder.add(ex);// no exception should happen
				} finally {
					doneSignal.countDown();
					DBFactory.getInstance().commitAndCloseSession();
				}
			}};
		
		// thread 2
		Thread thread2 = new Thread() {
			public void run() {
				try {
					Thread.sleep(300);
					RepositoryEntry repositoryEntryT2 = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
					RepositoryManager.getInstance().incrementDownloadCounter(repositoryEntryT2);
					System.out.println("testConcurrentIncrementLaunchCounterWithCodePoints: Thread2 incremented download-counter");
				} catch (Exception ex) {
					exceptionHolder.add(ex);// no exception should happen
				} finally {
					doneSignal.countDown();
					DBFactory.getInstance().commitAndCloseSession();
				}
			}};

		// thread 3
		Thread thread3 = new Thread() {
			public void run() {
				try {
					Thread.sleep(200);
					RepositoryEntry repositoryEntryT3 = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
					// change repository directly and not via RepositoryManager.setAccess(...) for testing
					repositoryEntryT3.setAccess(access);
					RepositoryManager.getInstance().updateRepositoryEntry(repositoryEntryT3);
					DBFactory.getInstance().closeSession();
					System.out.println("testConcurrentIncrementLaunchCounterWithCodePoints: Thread3 setAccess DONE");
				} catch (Exception ex) {
					exceptionHolder.add(ex);// no exception should happen
				} finally {
					doneSignal.countDown();
					DBFactory.getInstance().commitAndCloseSession();
				}
			}};
			
		thread1.start();
		thread2.start();
		thread3.start();

		try {
			boolean interrupt = doneSignal.await(10, TimeUnit.SECONDS);
			assertTrue("Test takes too long (more than 10s)", interrupt);
		} catch (InterruptedException e) {
			fail("" + e.getMessage());
		}

		try {
			// to see all registered code-points: comment-in next 2 lines
			// List<CodepointRef> codepointList = codepointClient.listAllCodepoints();
			// System.out.println("codepointList=" + codepointList);
			System.out.println("testConcurrentIncrementLaunchCounterWithCodePoints start waiting for breakpoint reached");
			TemporaryPausedThread[] threads = codepointRef.waitForBreakpointReached(1000);
			assertTrue("Did not reach breakpoint", threads.length > 0);
			System.out.println("threads[0].getCodepointRef()=" + threads[0].getCodepointRef());
			codepointRef.disableBreakpoint(true);
			System.out.println("testConcurrentIncrementLaunchCounterWithCodePoints breakpoint reached => continue");
		} catch (BreakpointStateException e) {
			e.printStackTrace();
			fail("Codepoints: BreakpointStateException=" + e.getMessage());
		} catch (CommunicationException e) {
			e.printStackTrace();
			fail("Codepoints: CommunicationException=" + e.getMessage());
		}
		sleep(100);
		RepositoryEntry repositoryEntry2 = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
		assertEquals("Wrong value of incrementLaunch counter",2,repositoryEntry2.getDownloadCounter());
		assertEquals("Wrong access value",access,repositoryEntry2.getAccess());

		codepointClient.close();
		System.out.println("testConcurrentIncrementLaunchCounterWithCodePoints finish successful");		
	}

	private RepositoryEntry createRepositoryCG(String name) {
		OLATResourceManager rm = OLATResourceManager.getInstance();
		// create course and persist as OLATResourceImpl
		OLATResource r =  rm.createOLATResourceInstance(CG_TYPE);
		DBFactory.getInstance().saveObject(r);

		// now make a repository entry for this course
		RepositoryEntry d = new RepositoryEntry();
		d.setOlatResource(r);
		d.setResourcename(name);
		d.setInitialAuthor("Christian Guretzki");
		d.setDisplayname("JunitTest_RepositoryEntry");
		DBFactory.getInstance().saveObject(d);
		return d;
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

}
