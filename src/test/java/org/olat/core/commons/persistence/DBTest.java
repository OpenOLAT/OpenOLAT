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

package org.olat.core.commons.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Test;
import org.olat.core.logging.DBRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.test.OlatTestCase;


/**
 * A <b>DBTest</b> is used to test the persistence package.
 * 
 * @author Andreas Ch. Kapp
 *
 */
public class DBTest extends OlatTestCase {

		
	/**
	 * testCloseOfUninitializedSession
	 */
	@Test
	public void testCloseOfUninitializedSession() {
		// first get a initialized db
		DB db = DBImpl.getInstance(false);
		//close it
		db.closeSession();
		//then get a uninitialized db
		db = DBImpl.getInstance(false);
		// and close it.
		db.closeSession();
		
	}
	
	/**
	 * testErrorHandling
	 */
	@Test
	public void testErrorHandling() {
		TestTable entry = new TestTable();
		entry.setField1("foo");
		entry.setField2(1234354566776L);
		DBImpl db = DBImpl.getInstance();
		try {		
			db.saveObject(entry);
			fail("Should generate an error");
		} catch (DBRuntimeException dre) {
			assertTrue(db.isError());
			assertNotNull(db.getError());
		}

		db.closeSession();
		// in a transaction
		db = DBImpl.getInstance();
		TestTable entryTwo = new TestTable();
		entryTwo.setField1("bar");
		entryTwo.setField2(2221234354566776L);
		try {
			db.saveObject(entryTwo);
			db.closeSession();
			fail("Should generate an error");
		} catch (DBRuntimeException dre) {
			assertTrue(db.isError());
			assertNotNull(db.getError());
		}
	}
	@Test
	public void testRollback() {
		DB db = DBFactory.getInstance();
		String propertyKey = "testRollback-1";
		String testValue = "testRollback-1";
		try {
			PropertyManager pm = PropertyManager.getInstance();
			Property p1 = pm.createPropertyInstance(null, null, null, null, propertyKey, null, null, testValue, null);
			pm.saveProperty(p1);
			String propertyKey2 = "testRollback-2";
			String testValue2 = "testRollback-2";
			// name is null => generated DB error => rollback
			Property p2 = pm.createPropertyInstance(null, null, null, null, null, null, null, testValue2, null);
			pm.saveProperty(p2);
			fail("Should generate error for rollback.");
		} catch (Exception ex) {
			db.closeSession();
		}
		// check if p1 is rollbacked
		db = DBFactory.getInstance();
		PropertyManager pm = PropertyManager.getInstance();
		Property p =pm.findProperty(null, null, null, null, propertyKey);
		assertNull("Property.save is NOT rollbacked", p);
	}
	@Test
	public void testMixedNonTransactional_Transactional() {
		DB db = DBFactory.getInstance();
		String propertyKey1 = "testMixed-1";
		String testValue1 = "testMixed-1";
		String propertyKey2 = "testMixed-2";
		String testValue2 = "testMixed-2";
		String propertyKey3 = "testMixed-3";
		String testValue3 = "testMixed-3";
		try {
			// outside of transaction
			PropertyManager pm = PropertyManager.getInstance();
			Property p1 = pm.createPropertyInstance(null, null, null, null, propertyKey1, null, null, testValue1, null);
			pm.saveProperty(p1);
			// inside of transaction
			Property p2 = pm.createPropertyInstance(null, null, null, null, propertyKey2, null, null, testValue2, null);
			pm.saveProperty(p2);
			// name is null => generated DB error => rollback
			Property p3 = pm.createPropertyInstance(null, null, null, null, null, null, null, testValue3, null);
			pm.saveProperty(p3);
			fail("Should generate error for rollback.");
			db.closeSession();
		} catch (Exception ex) {
			db.closeSession();
		}
		// check if p1&p2 is rollbacked
		PropertyManager pm = PropertyManager.getInstance();
		Property p_1 =pm.findProperty(null, null, null, null, propertyKey1);
		assertNull("Property1 is NOT rollbacked", p_1);
		Property p_2 =pm.findProperty(null, null, null, null, propertyKey2);
		assertNull("Property2 is NOT rollbacked", p_2);
	}
	@Test
	public void testRollbackNonTransactional() {
		DB db = DBFactory.getInstance();
		String propertyKey1 = "testNonTransactional-1";
		String testValue1 = "testNonTransactional-1";
		String propertyKey2 = "testNonTransactional-2";
		String testValue2 = "testNonTransactional-2";
		String propertyKey3 = "testNonTransactional-3";
		String testValue3 = "testNonTransactional-3";
		try {
			PropertyManager pm = PropertyManager.getInstance();
			Property p1 = pm.createPropertyInstance(null, null, null, null, propertyKey1, null, null, testValue1, null);
			pm.saveProperty(p1);
			Property p2 = pm.createPropertyInstance(null, null, null, null, propertyKey2, null, null, testValue2, null);
			pm.saveProperty(p2);
			// name is null => generated DB error => rollback ?
			Property p3 = pm.createPropertyInstance(null, null, null, null, null, null, null, testValue3, null);
			pm.saveProperty(p3);
			fail("Should generate error for rollback.");
			db.closeSession();
		} catch (Exception ex) {
			db.closeSession();
		}
		// check if p1 & p2 is NOT rollbacked
		PropertyManager pm = PropertyManager.getInstance();
		Property p_1 =pm.findProperty(null, null, null, null, propertyKey1);
		assertNull("Property1 is NOT rollbacked", p_1);
		Property p_2 =pm.findProperty(null, null, null, null, propertyKey2);
		assertNull("Property2 is NOT rollbacked", p_2);
	}

	
	/**
	 * Test concurrent updating. DbWorker threads updates concurrent db.
	 */
	@Test
	public void testConcurrentUpdate() {
		int maxWorkers = 5;
		int loops = 100;
		Tracing.logInfo("start testConcurrentUpdate maxWorkers=" + maxWorkers + "  loops=" + loops, this.getClass());
		DbWorker[] dbWorkers = new DbWorker[maxWorkers];
		for (int i=0; i<maxWorkers; i++) {
			dbWorkers[i] = new DbWorker(i,loops);
		}
		boolean allDbWorkerFinished = false;
		while (!allDbWorkerFinished) {
			allDbWorkerFinished = true;
			for (int i=0; i<maxWorkers; i++) {
				if (!dbWorkers[i].isfinished() ) {
					allDbWorkerFinished = false;
				}
			}
			try {
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
				Tracing.logWarn("testConcurrentUpdate InterruptedException=" + e, this.getClass());
			}
		}
		for (int i=0; i<maxWorkers; i++) {
			assertEquals(0,dbWorkers[i].getErrorCounter());
		}
		Tracing.logInfo("finished testConcurrentUpdate " , this.getClass());
	}
	@Test
	public void testDbPerf() {
		int loops = 1000;
		long timeWithoutTransction = 0;
		Tracing.logInfo("start testDbPerf with loops=" + loops, this.getClass());
		try {
			long startTime = System.currentTimeMillis();
			for (int loopCounter=0; loopCounter<loops; loopCounter++) {
				String propertyKey = "testDbPerfKey-" + loopCounter;
				DB db = DBFactory.getInstance();
				PropertyManager pm = PropertyManager.getInstance();
				String testValue = "testDbPerfValue-" + loopCounter;
				Property p = pm.createPropertyInstance(null, null, null, null, propertyKey, null, null, testValue, null);
				pm.saveProperty(p);
				// forget session cache etc.
				db.closeSession();
				pm.deleteProperty(p);
			}
			long endTime = System.currentTimeMillis();
			timeWithoutTransction = endTime - startTime;
			Tracing.logInfo("testDbPerf without transaction takes :" + timeWithoutTransction + "ms", this.getClass());
		} catch (Exception ex) {
			fail("Exception in testDbPerf without transaction ex="+ ex);
		}
		
		try {
			long startTime = System.currentTimeMillis();
			for (int loopCounter=0; loopCounter<loops; loopCounter++) {	
				String propertyKey = "testDbPerfKey-" + loopCounter;
				DB db = DBFactory.getInstance();
				PropertyManager pm = PropertyManager.getInstance();
				String testValue = "testDbPerfValue-" + loopCounter;
				Property p = pm.createPropertyInstance(null, null, null, null, propertyKey, null, null, testValue, null);
				pm.saveProperty(p);
				// forget session cache etc.
				db.closeSession();
				db = DBFactory.getInstance();
				pm.deleteProperty(p);
			}
			long endTime = System.currentTimeMillis();
			long timeWithTransction = endTime - startTime;
			Tracing.logInfo("testDbPerf with transaction takes :" + timeWithTransction + "ms", this.getClass());
			Tracing.logInfo("testDbPerf diff between transaction and without transaction :" + (timeWithTransction - timeWithoutTransction) + "ms", this.getClass());
		} catch (Exception ex) {
			fail("Exception in testDbPerf with transaction ex="+ ex);
		}
	}
	@Test
	public void testDBUTF8capable() {
		//FIXME:fj: move this test to the db module
		
		DB db = DBFactory.getInstance();
		PropertyManager pm = PropertyManager.getInstance();
		String unicodetest = "a-greek a\u03E2a\u03EAa\u03E8 arab \u0630a\u0631 chinese:\u3150a\u3151a\u3152a\u3153a\u3173a\u3110-z";
		Property p = pm.createPropertyInstance(null, null, null, null, "superbluberkey", null, null, unicodetest, null);
		pm.saveProperty(p);
		// forget session cache etc.
		db.closeSession();
		
		Property p2 = pm.findProperty(null, null, null, null, "superbluberkey");
		String lStr = p2.getStringValue();
		assertEquals(unicodetest, lStr);
		
	}
	@Test
	public void testFindObject() {
		// 1. create a property to have an object  
		Property p = PropertyManager.getInstance().createPropertyInstance(null, null, null, null, "testFindObject", null, null, "testFindObject_Value", null);
		PropertyManager.getInstance().saveProperty(p);
		long propertyKey = p.getKey();
		// forget session cache etc.
		DBFactory.getInstance().closeSession();
		// 2. try to find object
		Object testObject = DBFactory.getInstance().findObject(Property.class, propertyKey);
		assertNotNull(testObject);
		// 3. Delete object
		PropertyManager.getInstance().deleteProperty( (Property)testObject );
		DBFactory.getInstance().closeSession();
		// 4. try again to find object, now no-one should be found, must return null
		testObject = DBFactory.getInstance().findObject(Property.class, propertyKey);
		assertNull(testObject);
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@After
	public void tearDown() throws Exception {
		DBFactory.getInstance().closeSession();
	}


}

class DbWorker implements Runnable {

	private Thread workerThread = null;
	private int numberOfLoops;
	private String workerId;
	private int errorCounter = 0;
	private boolean isfinished = false;

	public DbWorker(int id, int numberOfLoops) {
		this.numberOfLoops = numberOfLoops;
		this.workerId = Integer.toString(id);
		if ( (workerThread == null) || !workerThread.isAlive()) {
			Tracing.logInfo("start DbWorker thread id=" + id, this.getClass());
			workerThread = new Thread(this, "TestWorkerThread-" + id);
			workerThread.setPriority(Thread.MAX_PRIORITY);
			workerThread.setDaemon(true);
			workerThread.start();
		}
	}

	public void run() {
		int loopCounter = 0;
		try {
			while (loopCounter++ < numberOfLoops ) {
				String propertyKey = "DbWorkerKey-" + workerId + "-" + loopCounter;
				DB db = DBFactory.getInstance();
				PropertyManager pm = PropertyManager.getInstance();
				String testValue = "DbWorkerValue-" + workerId + "-" + loopCounter;
				Property p = pm.createPropertyInstance(null, null, null, null, propertyKey, null, null, testValue, null);
				pm.saveProperty(p);
				// forget session cache etc.
				db.closeSession();
				
				db = DBFactory.getInstance();
				Property p2 = pm.findProperty(null, null, null, null, propertyKey);
				String lStr = p2.getStringValue();
				if (!testValue.equals(lStr)) {
					Tracing.logInfo("Property ERROR testValue=" + testValue + ": lStr=" + lStr, this.getClass());
					errorCounter++;
				}
				db.closeSession();
				
				Thread.currentThread().sleep(5);
			}
		} catch (Exception ex) {
			Tracing.logInfo("ERROR workerId=" + workerId + ": Exception=" + ex, this.getClass());
			errorCounter++;
		}
		isfinished  = true;
	}
	
	protected int getErrorCounter() {
		return errorCounter;
	}
	
	protected boolean isfinished() {
		return isfinished;
	}
}