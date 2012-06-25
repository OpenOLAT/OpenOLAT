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

package org.olat.group;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Christian Guretzki
 */
public class BGAreaManagerTest extends OlatTestCase {

	private static OLog log = Tracing.createLoggerFor(BGAreaManagerTest.class);

	private OLATResource c1, c2;
	
	@Autowired
	private BGAreaManager areaManager;


	@Before
	public void setUp() {
		try {
			OLATResourceable ores1 = OresHelper.createOLATResourceableInstance(UUID.randomUUID().toString(), 0l);
			c1 = OLATResourceManager.getInstance().createOLATResourceInstance(ores1);
			OLATResourceable ores2 = OresHelper.createOLATResourceableInstance(UUID.randomUUID().toString(), 0l);
			c2 = OLATResourceManager.getInstance().createOLATResourceInstance(ores2);
			Assert.assertNotNull(c2);

			DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			log.error("Exception in setUp(): " + e);
			e.printStackTrace();
		}
	}

	@After
	public void tearDown() throws Exception {
		try {
			DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			log.error("Exception in tearDown(): " + e);
			e.printStackTrace();
			throw e;
		}
	}

	
	/** 
	 * Do in different threads ant check that no exception happens :
	 * 1. create BG-Area
	 * 5. delete
	 */
	@Test
	public void testSynchronisationCreateBGArea() {

		final int maxLoop = 400; // => 400 x 100ms => 40sec => finished in 50sec
		final String areaName = "BGArea_1";

		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
		final CountDownLatch finfishCount = new CountDownLatch(3);
		
		BGArea bgArea = areaManager.findBGArea(areaName, c1);
		assertNull(bgArea);
		
		startThreadCreateDeleteBGArea(areaName, maxLoop, exceptionHolder, 100, 20, finfishCount);
		startThreadCreateDeleteBGArea(areaName, maxLoop, exceptionHolder, 30, 40, finfishCount);
		startThreadCreateDeleteBGArea(areaName, maxLoop, exceptionHolder, 15, 20, finfishCount);
		
		// sleep until t1 and t2 should have terminated/excepted
		try {
			finfishCount.await(120, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// if not -> they are in deadlock and the db did not detect it
		for (Exception exception : exceptionHolder) {
			System.err.println("exception: "+exception.getMessage());
			exception.printStackTrace();
		}
		assertTrue("Exceptions #" + exceptionHolder.size(), exceptionHolder.size() == 0);				
		assertEquals("Not all threads has finished", 0, finfishCount.getCount());
	}


	/**
	 * thread 1 : try to create - sleep - delete sleep

	 * @param areaName
	 * @param maxLoop
	 * @param exceptionHolder
	 * @param sleepAfterCreate
	 * @param sleepAfterDelete
	 */
	private void startThreadCreateDeleteBGArea(final String areaName, final int maxLoop, final List<Exception> exceptionHolder, 
			final int sleepAfterCreate, final int sleepAfterDelete, final CountDownLatch finishedCount) {
		new Thread(new Runnable() {
			public void run() {
				try {
				
				for (int i=0; i<maxLoop; i++) {
					try {
						BGArea bgArea = areaManager.createAndPersistBGAreaIfNotExists(areaName, "description:" + areaName, c1);
						if (bgArea != null) {
							DBFactory.getInstance().closeSession();
							// created a new bg area
							sleep(sleepAfterCreate);
							areaManager.deleteBGArea(bgArea);
						}
					} catch (Exception e) {
						exceptionHolder.add(e);
					} finally {
						try {
							DBFactory.getInstance().closeSession();
						} catch (Exception e) {
							// ignore
						};
					}
					sleep(sleepAfterDelete);
				}
				} catch(Exception e) {
					exceptionHolder.add(e);
				} finally {
					finishedCount.countDown();
				}
			}
		}).start();
	}


	/** 
	 * Do in different threads ant check that no exception happens :
	 * 1. create BG-Area
	 * 5. delete
	 */
	@Test
	public void testSynchronisationUpdateBGArea() {

		final int maxLoop = 400; // => 400 x 100ms => 40sec => finished in 50sec
		final String areaName = "BGArea_2";

		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
		final CountDownLatch finfishCount = new CountDownLatch(3);
		
		
		BGArea bgArea = areaManager.findBGArea(areaName, c1);
		assertNull(bgArea);
		bgArea = areaManager.createAndPersistBGAreaIfNotExists(areaName, "description:" + areaName, c1);
		assertNotNull(bgArea);
		
		startThreadUpdateBGArea(areaName, maxLoop, exceptionHolder, 20, finfishCount);
		startThreadUpdateBGArea(areaName, maxLoop, exceptionHolder, 40, finfishCount);
		startThreadUpdateBGArea(areaName, maxLoop, exceptionHolder, 15, finfishCount);
		
		// sleep until t1 and t2 should have terminated/excepted
		try {
			finfishCount.await(120, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			exceptionHolder.add(e);
		}

		// if not -> they are in deadlock and the db did not detect it
		for (Exception exception : exceptionHolder) {
			System.err.println("exception: "+exception.getMessage());
			exception.printStackTrace();
		}
		assertTrue("Exceptions #" + exceptionHolder.size(), exceptionHolder.size() == 0);				
		assertEquals("Not all threads has finished", 0, finfishCount.getCount());
	}
	
	private void startThreadUpdateBGArea(final String areaName, final int maxLoop, final List<Exception> exceptionHolder, 
			final int sleepTime, final CountDownLatch finishedCount) {
		// thread 2 : update,copy 
		new Thread(new Runnable() {
			public void run() {
				try {
					for (int i=0; i<maxLoop; i++) {
						try {
							BGArea bgArea = areaManager.findBGArea(areaName, c1);
							DBFactory.getInstance().closeSession();// Detached the bg-area object with closing session 
							if (bgArea != null) {
								bgArea.setDescription("description:" + areaName + i);
								areaManager.updateBGArea(bgArea);
							}
						} catch (Exception e) {
							exceptionHolder.add(e);
						} finally {
							try {
								DBFactory.getInstance().closeSession();
							} catch (Exception e) {
								// ignore
							};
						}
						sleep(sleepTime);
					}
				} catch(Exception e) {
					exceptionHolder.add(e);
				} finally {
					finishedCount.countDown();
				}
			}}).start();
	}

	
	/**
	 * 
	 * @param millis the duration in milliseconds to sleep
	 */
	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}