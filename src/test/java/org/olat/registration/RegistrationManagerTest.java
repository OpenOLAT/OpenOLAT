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

package org.olat.registration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Description:
 *
 * @author Sabina Jeger
 */
public class RegistrationManagerTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private RegistrationManager registrationManager;
	@Autowired
	private RegistrationModule registrationModule;
	
	@Test
	public void testManagers() {
		assertNotNull(registrationModule);
		assertNotNull(registrationManager);
	}

	/**
	 * Test internal registration.
	 */
	@Test
	public void testRegister() {
		String emailaddress = "sabina@jeger.net";
		String ipaddress = "130.60.112.10";
		TemporaryKeyImpl result = registrationManager.register(emailaddress, ipaddress, "register");
		assertTrue(result != null);
		assertEquals(emailaddress,result.getEmailAddress());
		assertEquals(ipaddress,result.getIpAddress());
	}


	/**
	 * Test load of temp key.
	 */
	@Test
	public void testLoadTemporaryKeyByRegistrationKey() {
		String emailaddress = "christian.guretzki@id.uzh.ch";
		String regkey = "";
		TemporaryKeyImpl result = null;
		String ipaddress = "130.60.112.12";

		//
		result = registrationManager.loadTemporaryKeyByRegistrationKey(regkey);
		assertTrue("not found, as registration key is empty", result == null);
		
		//now create a temp key
		result = registrationManager.createTemporaryKeyByEmail(emailaddress,ipaddress, RegistrationManager.REGISTRATION);
		assertTrue("result not null because key generated", result != null);
		//**
		dbInstance.closeSession();
		regkey = result.getRegistrationKey();
		//**
		
		//check that loading the key by registration key works
		result = null;
		result = registrationManager.loadTemporaryKeyByRegistrationKey(regkey);
		assertTrue("we should find the key just created", result != null);
	}
	
	/**
	 * Test load of temp key.
	 */
	@Test
	public void testLoadTemporaryKeyEntry() {
		String emailaddress = UUID.randomUUID().toString().replace("-", "") + "@frentix.com";
		TemporaryKeyImpl result = null;
		String ipaddress = "130.60.112.11";

		//try to load temp key which was not created before
		result = registrationManager.loadTemporaryKeyByEmail(emailaddress);
		assertTrue("result should be null, because not found", result == null);
		
		//now create a temp key
		result = registrationManager.createTemporaryKeyByEmail(emailaddress,ipaddress, RegistrationManager.REGISTRATION);
		assertTrue("result not null because key generated", result != null);
		//**
		dbInstance.closeSession();
		//**
		
		//check that loading the key by e-mail works
		result = null;
		result = registrationManager.loadTemporaryKeyByEmail(emailaddress);
		assertTrue("we shoult find the key just created", result != null);
	}
	
	
	/**
	 * Test load of temp key.
	 */
	@Test public void testCreateTemporaryKeyEntry() {
		String emailaddress = "sabina@jeger.net";
		TemporaryKeyImpl result = null;
		String ipaddress = "130.60.112.10";

		result = registrationManager.createTemporaryKeyByEmail(emailaddress,ipaddress, RegistrationManager.REGISTRATION);
		assertTrue(result != null);

		emailaddress = "sabina@jeger.ch";
		result = registrationManager.createTemporaryKeyByEmail(emailaddress,ipaddress, RegistrationManager.REGISTRATION);

		assertTrue(result != null);
		
		emailaddress = "info@jeger.net";
		result = registrationManager.createTemporaryKeyByEmail(emailaddress,ipaddress, RegistrationManager.REGISTRATION);

		assertNotNull(result);
	}
	
	@Test
	public void validateAgainstWhiteList() {
		//set domains
		registrationModule.setDomainListRaw("frentix.com cyberiacafe.ch,openolat.org\nfrentix.de");
		sleep(2000);//event based, asynchronous
		String domains = registrationModule.getDomainListRaw();
		assertEquals("frentix.com,cyberiacafe.ch,openolat.org,frentix.de", domains);
		
		//check equals matching
		assertTrue(registrationManager.validateEmailUsername("aoi@cyberiacafe.ch"));
		assertFalse(registrationManager.validateEmailUsername("aoi@cyberia.ch"));
		assertFalse(registrationManager.validateEmailUsername("aoi@blog.cyberiacafe.ch"));
	}
	
	@Test
	public void validateAgainstWhiteListWithWildCard() {
		//set domains
		registrationModule.setDomainListRaw("frentix.com *cyberiacafe.ch,openolat.org\nfrentix.de");
		sleep(2000);//event based, asynchronous
		String domains = registrationModule.getDomainListRaw();
		assertEquals("frentix.com,*cyberiacafe.ch,openolat.org,frentix.de", domains);
		
		//check equals matching
		assertTrue(registrationManager.validateEmailUsername("aoi@cyberiacafe.ch"));
		assertFalse(registrationManager.validateEmailUsername("aoi@cyberia.ch"));
		assertTrue(registrationManager.validateEmailUsername("aoi@blog.cyberiacafe.ch"));
	}
}
