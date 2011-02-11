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

package org.olat.registration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.test.OlatTestCase;


/**
 * Description:
 *
 * @author Sabina Jeger
 */
public class RegistrationManagerTest extends OlatTestCase {
	private static Logger log = Logger.getLogger(RegistrationManagerTest.class.getName());


	/**
	 * Test internal registration.
	 */
	@Test public void testRegister() {
		String emailaddress = "sabina@jeger.net";
		String ipaddress = "130.60.112.10";
		TemporaryKeyImpl result = RegistrationManager.getInstance().register(emailaddress, ipaddress, "register");
		assertTrue(result != null);
		assertEquals(emailaddress,result.getEmailAddress());
		assertEquals(ipaddress,result.getIpAddress());
	}


	/**
	 * Test load of temp key.
	 */
	@Test public void testLoadTemporaryKeyByRegistrationKey() {
		String emailaddress = "christian.guretzki@id.uzh.ch";
		String regkey = "";
		TemporaryKeyImpl result = null;
		String ipaddress = "130.60.112.12";
		RegistrationManager rm = RegistrationManager.getInstance();

		//
		result = rm.loadTemporaryKeyByRegistrationKey(regkey);
		assertTrue("not found, as registration key is empty", result == null);
		
		//now create a temp key
		result = rm.createTemporaryKeyByEmail(emailaddress,ipaddress,rm.REGISTRATION);
		assertTrue("result not null because key generated", result != null);
		//**
		DBFactory.getInstance().closeSession();
		regkey = result.getRegistrationKey();
		//**
		
		//check that loading the key by registration key works
		result = null;
		result = rm.loadTemporaryKeyByRegistrationKey(regkey);
		assertTrue("we should find the key just created", result != null);
	}
	
	/**
	 * Test load of temp key.
	 */
	@Test public void testLoadTemporaryKeyEntry() {
		String emailaddress = "patrickbrunner@uzh.ch";
		TemporaryKeyImpl result = null;
		String ipaddress = "130.60.112.11";
		RegistrationManager rm = RegistrationManager.getInstance();

		//try to load temp key which was not created before
		result = rm.loadTemporaryKeyByEmail(emailaddress);
		assertTrue("result should be null, because not found", result == null);
		
		//now create a temp key
		result = rm.createTemporaryKeyByEmail(emailaddress,ipaddress,rm.REGISTRATION);
		assertTrue("result not null because key generated", result != null);
		//**
		DBFactory.getInstance().closeSession();
		//**
		
		//check that loading the key by e-mail works
		result = null;
		result = rm.loadTemporaryKeyByEmail(emailaddress);
		assertTrue("we shoult find the key just created", result != null);
	}
	
	
	/**
	 * Test load of temp key.
	 */
	@Test public void testCreateTemporaryKeyEntry() {
		String emailaddress = "sabina@jeger.net";
		TemporaryKeyImpl result = null;
		String ipaddress = "130.60.112.10";
		RegistrationManager rm = RegistrationManager.getInstance();

		result = rm.createTemporaryKeyByEmail(emailaddress,ipaddress,rm.REGISTRATION);
		assertTrue(result != null);

		emailaddress = "sabina@jeger.ch";
		result = rm.createTemporaryKeyByEmail(emailaddress,ipaddress,rm.REGISTRATION);

		assertTrue(result != null);
		
		emailaddress = "info@jeger.net";
		result = rm.createTemporaryKeyByEmail(emailaddress,ipaddress,rm.REGISTRATION);

		assertTrue(result != null);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@After public void tearDown() throws Exception {
		DBFactory.getInstance().closeSession();
	}

}
