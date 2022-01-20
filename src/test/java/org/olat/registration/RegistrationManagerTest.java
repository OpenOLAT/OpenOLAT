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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.util.CodeHelper;
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
	 * Test load of temp key.
	 */
	@Test
	public void testLoadTemporaryKeyByRegistrationKey() {
		String emailaddress = UUID.randomUUID() + "@openolat.com";
		String regkey = "";
		String ipaddress = "130.60.112.12";

		TemporaryKey result1 = registrationManager.loadTemporaryKeyByRegistrationKey(regkey);
		Assert.assertNull("not found, as registration key is empty", result1);
		
		//now create a temp key
		TemporaryKey result2 = registrationManager.loadOrCreateTemporaryKeyByEmail(emailaddress, ipaddress, RegistrationManager.REGISTRATION, null);
		Assert.assertNotNull("result not null because key generated", result2);
		dbInstance.commitAndCloseSession();

		//check that loading the key by registration key works
		TemporaryKey reloadedResult = registrationManager.loadTemporaryKeyByRegistrationKey(result2.getRegistrationKey());
		Assert.assertNotNull("we should find the key just created", reloadedResult);
		Assert.assertEquals(result2, reloadedResult);
	}
	
	/**
	 * Test load of temp key.
	 */
	@Test
	public void testLoadTemporaryKeyByAction() {
		String emailaddress = UUID.randomUUID() + "@openolat.com";
		String regAction =  UUID.randomUUID().toString();
		String ipaddress = "130.60.112.12";

		//now create a temporary key
		TemporaryKey result = registrationManager.loadOrCreateTemporaryKeyByEmail(emailaddress, ipaddress, regAction, null);
		Assert.assertNotNull("result not null because key generated", result);
		dbInstance.commitAndCloseSession();

		//check that loading the key by registration key works
		List<TemporaryKey> reloadedResults = registrationManager.loadTemporaryKeyByAction(regAction);
		Assert.assertNotNull(reloadedResults);
		Assert.assertEquals(1, reloadedResults.size());
		Assert.assertEquals(result, reloadedResults.get(0));
	}
	
	/**
	 * Test load of temp key.
	 */
	@Test
	public void testLoadTemporaryKeyEntry() {
		Long identityKey = (new Random()).nextLong();
		String emailaddress = UUID.randomUUID() + "@frentix.com";
		String ipaddress = "130.60.112.11";

		//try to load temp key which was not created before
		TemporaryKey result = registrationManager.loadTemporaryKeyByEmail(emailaddress);
		Assert.assertNull("result should be null, because not found", result);
		
		//now create a temp key
		TemporaryKey realResult = registrationManager.loadOrCreateTemporaryKeyByEmail(emailaddress,ipaddress, RegistrationManager.REGISTRATION, null);
		Assert.assertNotNull("result not null because key generated", realResult);
		dbInstance.commitAndCloseSession();
		
		//check that loading the key by e-mail works
		TemporaryKey reloadResult = registrationManager.loadTemporaryKeyByEmail(emailaddress);
		Assert.assertNotNull("we shoult find the key just created", reloadResult);
		Assert.assertEquals(realResult, reloadResult);
		
		//must be case insensitive
		TemporaryKey lowerResult = registrationManager.loadTemporaryKeyByEmail(emailaddress.toLowerCase());
		Assert.assertNotNull("we shoult find the key just created", lowerResult);
		Assert.assertEquals(realResult, lowerResult);
		TemporaryKey upperResult = registrationManager.loadTemporaryKeyByEmail(emailaddress.toUpperCase());
		Assert.assertNotNull("we shoult find the key just created", upperResult);
		Assert.assertEquals(realResult, upperResult);
		
		//example with identity
		TemporaryKey keyOfIdentity = registrationManager.createAndDeleteOldTemporaryKey(identityKey, emailaddress, ipaddress, RegistrationManager.REGISTRATION, null);
		registrationManager.createAndDeleteOldTemporaryKey(identityKey, emailaddress, ipaddress, RegistrationManager.PW_CHANGE, null);
		dbInstance.commitAndCloseSession();
		List<TemporaryKey> keys = registrationManager.loadTemporaryKeyByIdentity(identityKey, RegistrationManager.REGISTRATION);
		Assert.assertEquals(1, keys.size());
		Assert.assertEquals(keyOfIdentity, keys.get(0));
	}
	
	/**
	 * Test load of temp key.
	 */
	@Test public void testCreateTemporaryKeyEntry() {
		String emailaddress = UUID.randomUUID() + "@openolat.com";
		String ipaddress = "130.60.112.10";

		TemporaryKey result = registrationManager.loadOrCreateTemporaryKeyByEmail(emailaddress, ipaddress, RegistrationManager.REGISTRATION, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getKey());
		Assert.assertNotNull(result.getCreationDate());
		Assert.assertNotNull(result.getValidUntil());
		Assert.assertEquals(ipaddress, result.getIpAddress());
		Assert.assertEquals(emailaddress, result.getEmailAddress());
	}
	
	@Test
	public void testCreateAndDeleteOldTemporaryKey() {
		Long identityKey = (new Random()).nextLong();
		String emailaddress = UUID.randomUUID() + "@openolat.com";
		String ipaddress = "130.60.112.10";

		TemporaryKey firstKey = registrationManager.createAndDeleteOldTemporaryKey(identityKey, emailaddress, ipaddress, RegistrationManager.PW_CHANGE, null);
		String firstResistrationKey = firstKey.getRegistrationKey();
		System.out.println(firstResistrationKey);

		TemporaryKey secondKey = registrationManager.createAndDeleteOldTemporaryKey(identityKey, emailaddress, ipaddress, RegistrationManager.PW_CHANGE, null);
		System.out.println(secondKey.getRegistrationKey());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(secondKey);
		Assert.assertNotNull(secondKey.getKey());
		Assert.assertNotNull(secondKey.getCreationDate());
		Assert.assertNotNull(secondKey.getValidUntil());
		Assert.assertEquals(ipaddress, secondKey.getIpAddress());
		Assert.assertEquals(emailaddress, secondKey.getEmailAddress());
		Assert.assertEquals(identityKey, secondKey.getIdentityKey());
		
		TemporaryKey reloadedFirstKey = registrationManager.loadTemporaryKeyByRegistrationKey(firstResistrationKey);
		Assert.assertNull(reloadedFirstKey);
	}
	
	@Test
	public void deleteInvalidTemporaryKeys() {
		String ipaddress = "130.60.112.10";
		TemporaryKey temporaryKeyPast1 = registrationManager.createAndDeleteOldTemporaryKey(CodeHelper.getForeverUniqueID(),
				UUID.randomUUID() + "@openolat.com", ipaddress, RegistrationManager.PW_CHANGE, -1000);
		TemporaryKey temporaryKeyPast2 = registrationManager.createAndDeleteOldTemporaryKey(CodeHelper.getForeverUniqueID(),
				UUID.randomUUID() + "@openolat.com", ipaddress, RegistrationManager.PW_CHANGE, -1000);
		TemporaryKey temporaryKeyFuture = registrationManager.createAndDeleteOldTemporaryKey(CodeHelper.getForeverUniqueID(),
				UUID.randomUUID() + "@openolat.com", ipaddress, RegistrationManager.PW_CHANGE, 1000);
		dbInstance.commitAndCloseSession();
		
		int deletedRow = registrationManager.deleteInvalidTemporaryKeys();
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(deletedRow >= 2);
		
		List<TemporaryKey> allTemporaryKeys = registrationManager.loadAll();
		Assert.assertTrue(!allTemporaryKeys.contains(temporaryKeyPast1));
		Assert.assertTrue(!allTemporaryKeys.contains(temporaryKeyPast2));
		Assert.assertTrue(allTemporaryKeys.contains(temporaryKeyFuture));
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
	
	@Test
	public void readWriteTemporaryMap() {
		Map<String, String> mailMap = new HashMap<>();
		mailMap.put("currentEMail", "current");
		mailMap.put("changedEMail", "changed");
		String xml = registrationManager.temporaryValueToString(mailMap);
		Map<String, String> xmlMap = registrationManager.readTemporaryValue(xml);
		Assert.assertEquals("current", xmlMap.get("currentEMail"));
		Assert.assertEquals("changed", xmlMap.get("changedEMail"));
	}
}
