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

package org.olat.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JMSCodePointServerJunitHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  Mar 26, 2004
 *
 * @author gnaegi
 * 
 * Comment:  
 * 
 */
public class RepositoryManagerConcurrentTest extends OlatTestCase {
	private static final OLog log = Tracing.createLoggerFor(RepositoryManagerConcurrentTest.class);
	private static String CODEPOINT_SERVER_ID = "RepositoryManagerTest";
	
	private static final String FG_TYPE = UUID.randomUUID().toString().replace("_", "");
	private static final String CG_TYPE = UUID.randomUUID().toString().replace("-", "");
	
	@Autowired
	private RepositoryService repositoryService;
	
	@Before
	public void setup() {
		try {
			// Setup for code-points
			JMSCodePointServerJunitHelper.startServer(CODEPOINT_SERVER_ID);
		} catch (Exception e) {
			log.error("Error while setting up activeMq or Codepointserver", e);
		}
	}

	@After public void tearDown() {
		try {
			JMSCodePointServerJunitHelper.stopServer();
		} catch (Exception e) {
			log.error("tearDown failed", e);
		}
	}

	@Test
	public void testQueryReferencableResourcesLimitType() {
		DB db = DBFactory.getInstance();
		RepositoryManager rm = RepositoryManager.getInstance();
		
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsAuthor("id1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsAuthor("id2");

		// generate 5000 repo entries
		int numbRes = 5000;
		long startCreate = System.currentTimeMillis();
		for (int i = 1; i < numbRes; i++) {
			// create course and persist as OLATResourceImpl
			Identity owner = (i % 2 > 0) ? id1 : id2;
			RepositoryEntry re = createRepositoryEntryFG(owner, i);				
			if ((i % 2 > 0)) {
				re.setCanReference(true);
			} else {
				re.setCanReference(false);
			}
			// save the repository entry
			rm.saveRepositoryEntry(re);
			
			// Create course admin policy for owner group of repository entry
			// -> All owners of repository entries are course admins
			//securityManager.createAndPersistPolicy(re.getOwnerGroup(), Constants.PERMISSION_ADMIN, re.getOlatResource());	
			
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
		List<RepositoryEntry> results = rm.queryReferencableResourcesLimitType(id1, new Roles(false, false, false, true, false, false, false), typelist, null, null, null);
		long endSearchReferencable = System.currentTimeMillis();
		log.debug("found " + results.size() + " repo entries " + (endSearchReferencable - startSearchReferencable) + "ms");

		// only half of the items should be found
		assertEquals((int) (numbRes / 2), results.size());
		
		// inserting must take longer than searching, otherwhise most certainly we have a problem somewhere in the query
		assertTrue((endCreate - startCreate) > (endSearchReferencable - startSearchReferencable));
	}


	private RepositoryEntry createRepositoryEntryFG(Identity owner, final int i) {
		DB db = DBFactory.getInstance();
		
		OLATResourceable resourceable = new OLATResourceable() {
			public String getResourceableTypeName() {	return FG_TYPE;}
			public Long getResourceableId() {return new Long(i); }
		};
		OLATResource r =  OLATResourceManager.getInstance().createOLATResourceInstance(resourceable);
		db.saveObject(r);
		
		// now make a repository entry for this course
		final RepositoryEntry re = repositoryService.create(owner, "Lernen mit OLAT " + i,
				"JunitTest_RepositoryEntry_" + i, "yo man description bla bla + i", r);
		re.setAccess(RepositoryEntry.ACC_OWNERS_AUTHORS);
		return re;
	}
	
	/**
	 * 
	 */
	@Test
	public void testIncrementLaunchCounter() {
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
		}
		RepositoryEntry repositoryEntry2 = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
		assertEquals("Wrong value of incrementLaunch counter",mainLoop * loop,repositoryEntry2.getLaunchCounter());
		System.out.println("testIncrementLaunchCounter finished");
	}

	@Test
	public void testIncrementDownloadCounter() {
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
		}
		RepositoryEntry repositoryEntry2 = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
		assertEquals("Wrong value of incrementLaunch counter",mainLoop * loop,repositoryEntry2.getDownloadCounter());
		System.out.println("testIncrementDownloadCounter finished");
	}
	
	@Test
	public void testConcurrentIncrementLaunchCounter() {
		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
		final List<Boolean> statusList = Collections.synchronizedList(new ArrayList<Boolean>(1));

		final int loop = 100;
		final int numberOfThreads = 3;
		final RepositoryEntry repositoryEntry = createRepositoryCG("T1_perf2");		
		final Long keyRepo = repositoryEntry.getKey();
		assertNotNull(repositoryEntry.getOlatResource());
		DBFactory.getInstance().commitAndCloseSession();
		assertEquals("Launch counter was not 0", 0, repositoryEntry.getLaunchCounter() );
		long startTime = System.currentTimeMillis();

		final CountDownLatch doneSignal = new CountDownLatch(3);
		
		// start thread 1 : incrementLaunchCounter / setAccess
		Thread thread1 = new Thread(){
			public void run() {
				try {
					sleep(10);
					for (int i = 1; i <= loop; i++) {
						// 1. load RepositoryEntry
						RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
						re = RepositoryManager.getInstance().incrementLaunchCounter(re);
						if (i % 20 == 0 ) {
							int ACCESS_VALUE = 4;
							//fxdiff VCRP-1,2: access control of resources
							re = RepositoryManager.getInstance().setAccess(re, ACCESS_VALUE, false);
							assertEquals("Wrong access value", ACCESS_VALUE, re.getAccess());
						} else if (i % 10 == 0 ) {
							int ACCESS_VALUE = 1;
							//fxdiff VCRP-1,2: access control of resources
							re = RepositoryManager.getInstance().setAccess(re, ACCESS_VALUE, false);
							assertEquals("Wrong access value",ACCESS_VALUE,re.getAccess());
						}
						DBFactory.getInstance().commitAndCloseSession();
					}
					statusList.add(Boolean.TRUE);
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					try {
						DBFactory.getInstance().closeSession();
					} catch (Exception e) {
						// ignore
					}
					doneSignal.countDown();
				}	
			}
		};
		
		// start thread 2 : incrementLaunchCounter / setDescriptionAndName
		Thread thread2 = new Thread() {
			public void run() {
				try {
					sleep(10);
					for (int i = 1; i <= loop; i++) {
						// 1. load RepositoryEntry
						RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
						re = RepositoryManager.getInstance().incrementLaunchCounter(re);
						if (i % 25 == 0 ) {
							String displayName = "DisplayName" + i;
							String description = "Description" + i;
							re = RepositoryManager.getInstance().setDescriptionAndName(re, displayName,description);
							assertEquals("Wrong displayName value", displayName, re.getDisplayname());
							assertEquals("Wrong description value", description, re.getDescription());
						}
						DBFactory.getInstance().commitAndCloseSession();
					}
					statusList.add(Boolean.TRUE);
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					try {
						DBFactory.getInstance().closeSession();
					} catch (Exception e) {
						// ignore
					}
					doneSignal.countDown();
				}	
			}
		};
		
		// start thread 3
		Thread thread3 = new Thread() {
			public void run() {
				try {
					sleep(10);
					for (int i = 1; i <= loop; i++) {
						// 1. load RepositoryEntry
						RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
						re = RepositoryManager.getInstance().incrementLaunchCounter(re);
						if (i % 30 == 0 ) {
							re = RepositoryManager.getInstance().setProperties(re, true, false, true, false);	
							assertEquals("Wrong canCopy value", true, re.getCanCopy());
							assertEquals("Wrong getCanReference value",false, re.getCanReference());
							assertEquals("Wrong getCanLaunch value", true, re.getCanLaunch());
							assertEquals("Wrong getCanDownload value", false, re.getCanDownload());
						} else 	if (i % 15 == 0 ) {
							re = RepositoryManager.getInstance().setProperties(re, false, true, false, true);	
							assertEquals("Wrong canCopy value", false, re.getCanCopy());
							assertEquals("Wrong getCanReference value", true, re.getCanReference());
							assertEquals("Wrong getCanLaunch value", false, re.getCanLaunch() );
							assertEquals("Wrong getCanDownload value", true, re.getCanDownload());
						}
						DBFactory.getInstance().commitAndCloseSession();
					}
					statusList.add(Boolean.TRUE);
				} catch (Exception e) {
					exceptionHolder.add(e);
				} finally {
					try {
						DBFactory.getInstance().closeSession();
					} catch (Exception e) {
						// ignore
					}
					doneSignal.countDown();
				}	
			}
		};
		
		//go! go! go!
		thread1.start();
		thread2.start();
		thread3.start();

		try {
			boolean interrupt = doneSignal.await(30, TimeUnit.SECONDS);
			assertTrue("Test takes too long (more than 10s)", interrupt);
		} catch (InterruptedException e) {
			fail("" + e.getMessage());
		}

		for(Exception e:exceptionHolder) {
			e.printStackTrace();
		}
		assertEquals("Exceptions", 0, exceptionHolder.size());

		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
		assertEquals("Wrong value of incrementLaunch counter", loop * numberOfThreads, re.getLaunchCounter());
		assertEquals("DisplayName" + loop, re.getDisplayname());//check if the displayname is correct
		assertEquals("Description" + loop, re.getDescription());
		System.out.println("testConcurrentIncrementLaunchCounter time=" + (System.currentTimeMillis() - startTime) + " for " + loop + " incrementLaunchCounter calls");
		System.out.println("testConcurrentIncrementLaunchCounter finished");
	}

	/**
	 * Compare async increment-call with sync 'setDscription' call.
	 */
	@Test
	public void testIncrementLaunchCounterSetDescription() {
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
	@Test
	public void testConcurrentIncrementLaunchCounter_v2() {
		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));

		RepositoryEntry repositoryEntry = createRepositoryCG("IncCodePoint");		
		final Long keyRepo = repositoryEntry.getKey();
		final OLATResourceable resourceable = repositoryEntry.getOlatResource();
		assertNotNull(resourceable);
		final int access = 4;
		DBFactory.getInstance().closeSession();
		assertEquals("Launch counter was not 0", 0, repositoryEntry.getLaunchCounter() );

		final CountDownLatch doneSignal = new CountDownLatch(3);
		
		// thread 1
		Thread thread1 = new Thread() {
			public void run() {
				try {
					Thread.sleep(100);
					RepositoryEntry repositoryEntryT1 = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
					repositoryEntryT1 = RepositoryManager.getInstance().incrementDownloadCounter(repositoryEntryT1);
					System.out.println("testConcurrentIncrementLaunchCounterWithCodePoints: Thread1 incremented download-counter");
				} catch (Exception ex) {
					exceptionHolder.add(ex);// no exception should happen
				} finally {
					DBFactory.getInstance().commitAndCloseSession();
					doneSignal.countDown();
				}
			}};
		
		// thread 2
		Thread thread2 = new Thread() {
			public void run() {
				try {
					Thread.sleep(300);
					RepositoryEntry repositoryEntryT2 = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
					repositoryEntryT2 = RepositoryManager.getInstance().incrementDownloadCounter(repositoryEntryT2);
					System.out.println("testConcurrentIncrementLaunchCounterWithCodePoints: Thread2 incremented download-counter");
				} catch (Exception ex) {
					exceptionHolder.add(ex);// no exception should happen
				} finally {
					DBFactory.getInstance().commitAndCloseSession();
					doneSignal.countDown();
				}
			}};

		// thread 3
		Thread thread3 = new Thread() {
			public void run() {
				try {
					Thread.sleep(200);
					RepositoryEntry repositoryEntryT3 = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
					repositoryEntryT3 = RepositoryManager.getInstance().setAccess(repositoryEntryT3, access, false);
					DBFactory.getInstance().closeSession();
					System.out.println("testConcurrentIncrementLaunchCounterWithCodePoints: Thread3 setAccess DONE");
				} catch (Exception ex) {
					exceptionHolder.add(ex);// no exception should happen
				} finally {
					DBFactory.getInstance().commitAndCloseSession();
					doneSignal.countDown();
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

		RepositoryEntry repositoryEntry2 = RepositoryManager.getInstance().lookupRepositoryEntry(keyRepo);
		assertEquals("Wrong value of incrementLaunch counter",2,repositoryEntry2.getDownloadCounter());
		assertEquals("Wrong access value",access,repositoryEntry2.getAccess());
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
}
