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

package org.olat.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.OLATResourceable;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A <b>OLATResourceManagerTest </b> is used for SecurityResourceManager
 * testing.
 * 
 * @author Andreas Ch. Kapp
 *  
 */
public class OLATResourceManagerTest extends OlatTestCase {
	private static final Logger log = Tracing.createLoggerFor(OLATResourceManagerTest.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private OLATResourceManager rm;

	/**
	 * Test creation/insert/update and deletion of a resource
	 */
	@Test
	public void testCreateInsertUpdateDeleteResource() {
		//create
		String resName = UUID.randomUUID().toString();
		TestResourceable resource = new TestResourceable(8213649l, resName);
		OLATResource res = rm.createOLATResourceInstance( resource);
		Assert.assertNotNull(res);
		rm.saveOLATResource(res);
		
		Assert.assertEquals(Long.valueOf(8213649l), res.getResourceableId());
		Assert.assertEquals(resName, res.getResourceableTypeName());
		Assert.assertNotNull(res.getCreationDate());
		Assert.assertNotNull(res.getKey());
		dbInstance.commit();
	}

	/**
	 * Test find/persist of a resource
	 */
	@Test
	public void testFindOrPersistResourceable() {
		String resName = UUID.randomUUID().toString();
		TestResourceable resource = new TestResourceable(8213650l, resName);
		
		//create by finding
		OLATResource ores1 = rm.findOrPersistResourceable(resource);
		Assert.assertNotNull(ores1);
		//only find
		OLATResource ores2 = rm.findOrPersistResourceable(resource);
		Assert.assertNotNull(ores2);
		Assert.assertEquals(ores1, ores2);
	}

	/**
	 * Test type-only resource
	 */
	@Test
	public void testInsertTypeOnly() {
		TestResourceable resource = new TestResourceable(null, "typeonly");
		OLATResource ores1 = rm.findOrPersistResourceable(resource);
		Assert.assertNotNull(ores1);
		Assert.assertNull(ores1.getResourceableId());
	}

	/**
	 * Test deletion of a resource
	 */
	@Test
	public void testDeleteResourceable() {
		String resName = UUID.randomUUID().toString();
		TestResourceable resource = new TestResourceable(8213651l, resName);
		
		//delete on not persisted resourceable
		rm.deleteOLATResourceable(resource);
		//delete persisted resourceable
		OLATResource ores = rm.findOrPersistResourceable(resource);
		Assert.assertNotNull(ores);
		rm.deleteOLATResourceable(resource);
		dbInstance.commit();
		
		OLATResource deletedRes = rm.findResourceable(8213651l, resName);
		Assert.assertNull(deletedRes);
	}
	
	@Test
	public void findResourceById() {
		String resName = UUID.randomUUID().toString();
		TestResourceable resource = new TestResourceable(8213652l, resName);
		OLATResource ores = rm.findOrPersistResourceable(resource);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(ores);
		Assert.assertNotNull(ores.getKey());
		
		//find by id
		OLATResource reloadedOres = rm.findResourceById(ores.getKey());
		Assert.assertNotNull(reloadedOres);
		Assert.assertEquals(ores, reloadedOres);
	}
	
	@Test
	public void findResourceByTypes() {
		String resName1 = UUID.randomUUID().toString();
		TestResourceable resource1 = new TestResourceable(8213653l, resName1);
		OLATResource ores1 = rm.findOrPersistResourceable(resource1);
		String resName2 = UUID.randomUUID().toString();
		TestResourceable resource2 = new TestResourceable(8213654l, resName2);
		OLATResource ores2 = rm.findOrPersistResourceable(resource2);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(ores1);
		Assert.assertNotNull(ores2);

		//find by types
		List<String> types = new ArrayList<>(2);
		types.add(resName1);
		types.add(resName2);
		
		List<OLATResource> reloadedOres = rm.findResourceByTypes(types);
		Assert.assertNotNull(reloadedOres);
		Assert.assertEquals(2, reloadedOres.size());
		Assert.assertTrue(reloadedOres.contains(ores1));
		Assert.assertTrue(reloadedOres.contains(ores2));
	}
	
	@Test
	public void findResourceable() {
		String resName = UUID.randomUUID().toString();
		TestResourceable resource = new TestResourceable(8213655l, resName);
		OLATResource ores = rm.findOrPersistResourceable(resource);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(ores);
		Assert.assertNotNull(ores.getKey());
		
		//find by id
		OLATResource reloadedOres = rm.findResourceable(8213655l, resName);
		Assert.assertNotNull(reloadedOres);
		Assert.assertEquals(ores, reloadedOres);
	}

	/**
	 * Test find/persist of a resource
	 */
	@Test
	public void testConcurrentFindOrPersistResourceable() {
		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
		final List<OLATResource> statusList = Collections.synchronizedList(new ArrayList<OLATResource>(1));
		final String resourceName = UUID.randomUUID().toString();
		final CountDownLatch doneSignal = new CountDownLatch(2);
		
		Thread thread1 = new Thread(new Runnable() {
			public void run() {
				try {
					sleep(10);
					OLATResource resource = OLATResourceManager.getInstance().findOrPersistResourceable(new TestResourceable(123123999l, resourceName));
					assertNotNull(resource);
					statusList.add(resource);
					log.info("testConcurrentFindOrPersistResourceable thread1 finished");
				} catch (Exception ex) {
					exceptionHolder.add(ex);// no exception should happen
				} finally {
					DBFactory.getInstance().commitAndCloseSession();
					doneSignal.countDown();
				}
			}});
		
		Thread thread2 = new Thread(new Runnable() {
			public void run() {
				try {
					sleep(10);
					OLATResource resource = OLATResourceManager.getInstance().findOrPersistResourceable(new TestResourceable(123123999l, resourceName));
					assertNotNull(resource);
					statusList.add(resource);
					log.info("testConcurrentFindOrPersistResourceable thread2 finished");
				} catch (Exception ex) {
					exceptionHolder.add(ex);// no exception should happen
				} finally {
					DBFactory.getInstance().commitAndCloseSession();
					doneSignal.countDown();
				}
			}});
		
		thread1.start();
		thread2.start();

		try {
			boolean interrupt = doneSignal.await(10, TimeUnit.SECONDS);
			assertTrue("Test takes too long (more than 10s)", interrupt);
		} catch (InterruptedException e) {
			fail("" + e.getMessage());
		}
	
		// if not -> they are in deadlock and the db did not detect it
		for (Exception exception : exceptionHolder) {
			log.error("exception: ", exception);
		}
		if (exceptionHolder.size() > 0) {
			assertTrue("It throws an exception in test => see sysout exception[0]=" + exceptionHolder.get(0).getMessage(), exceptionHolder.size() == 0);	
		}
		assertEquals("Missing created OresResource in statusList",2, statusList.size());
		assertEquals("Created OresResource has not same key",statusList.get(0).getKey(), statusList.get(1).getKey());
		log.info("testConcurrentFindOrPersistResourceable finish successful");
	}

	/**
	 * Test resource for null values
	 */
	@Test
	public void testNULLVALUE() {
		NullTester ntester = new NullTester();
		// Uncomment for testing:
		OLATResource or = null;
		try {
			or = rm.createOLATResourceInstance(ntester);
		} catch (RuntimeException re) {
			assertNull(or);
		}
	}
	
	/**
	 * Resource with null value
	 */
	private static class NullTester implements OLATResourceable {

		@Override
		public Long getResourceableId() {
			return Long.valueOf(0);
		}

		@Override
		public String getResourceableTypeName() {
			return this.getClass().getName();
		}
	}
	
	///////////////////////////////
	// Inner class TestResourceable
	///////////////////////////////
	private static class TestResourceable implements OLATResourceable {
		private final Long resId;
		private final String resName;
		
		public TestResourceable(Long resId, String resourceName) {
			this.resId = resId;
			this.resName = resourceName;
		}

		@Override
		public Long getResourceableId() {
			return resId;
		}

		@Override
		public String getResourceableTypeName() {
			return resName;
		}
	}
}