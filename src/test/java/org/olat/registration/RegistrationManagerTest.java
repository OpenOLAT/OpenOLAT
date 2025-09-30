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

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.test.JunitTestHelper;
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
	@Autowired
	private OrganisationModule organisationModule;
	
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
	
	@Test
	public void loadTemporaryKeyEntryAndAction() {
		String emailAddress = UUID.randomUUID() + "@frentix.com";
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("load-tmp-1");

		TemporaryKey tempKey = registrationManager.createAndDeleteOldTemporaryKey(id.getKey(), emailAddress, "192.168.1.100", RegistrationManager.REGISTRATION, null);
		dbInstance.commitAndCloseSession();
		
		TemporaryKey result = registrationManager.loadTemporaryKeyByEmail(emailAddress, RegistrationManager.REGISTRATION);
		Assert.assertNotNull(result);
		Assert.assertEquals(tempKey, result);
	}
	
	@Test
	public void loadTemporaryKeyEntryWithoutAction() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("load-tmp-2");
		String email = id.getUser().getEmail();
		TemporaryKey temporaryKey = registrationManager.createAndDeleteOldTemporaryKey(id.getKey(), email, "192.168.1.100", RegistrationManager.PW_CHANGE, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(temporaryKey);
		
		TemporaryKey resultWithoutAction = registrationManager.loadTemporaryKeyByEmail(email, null);
		Assert.assertNull(resultWithoutAction);
	}
	
	@Test
	public void loadTemporaryKeyEntryWithoutEmail() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("load-tmp-3");
		String email = id.getUser().getEmail();
		TemporaryKey temporaryKey = registrationManager.createAndDeleteOldTemporaryKey(id.getKey(), email, "192.168.1.100", RegistrationManager.PW_CHANGE, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(temporaryKey);
		
		TemporaryKey resultWithoutEmail = registrationManager.loadTemporaryKeyByEmail(null, RegistrationManager.PW_CHANGE);
		Assert.assertNull(resultWithoutEmail);
	}
	
	@Test
	public void loadTemporaryKeyEntryWithNulls() {
		TemporaryKey resultWithout = registrationManager.loadTemporaryKeyByEmail(null, null);
		Assert.assertNull(resultWithout);
	}

	@Test
	public void hasTemporaryKeyEntryAndAction() {
		String emailAddress = UUID.randomUUID() + "@frentix.com";
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("load-tmp-1");

		TemporaryKey tempKey = registrationManager.createAndDeleteOldTemporaryKey(id.getKey(), emailAddress, "192.168.1.100", RegistrationManager.PW_CHANGE, null);
		dbInstance.commitAndCloseSession();
		
		TemporaryKey result = registrationManager.loadTemporaryKeyByEmail(emailAddress, RegistrationManager.PW_CHANGE);
		Assert.assertNotNull(result);
		Assert.assertEquals(tempKey, result);
	}
	
	/**
	 * Test load of temporary key.
	 */
	@Test
	public void createTemporaryKeyEntry() {
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
	public void testUpdateTemporaryRegistrationKey() {
		// Create a temporary key entry
		String emailAddress = UUID.randomUUID() + "@openolat.com";
		String ipAddress = "192.168.1.1";
		TemporaryKey originalKey = registrationManager.loadOrCreateTemporaryKeyByEmail(
				emailAddress, ipAddress, RegistrationManager.REGISTRATION, 30
		);
		dbInstance.commitAndCloseSession();
		sleep(100);

		// Validate initial state
		Assert.assertNotNull(originalKey);
		Assert.assertNotNull(originalKey.getRegistrationKey());
		Assert.assertNotNull(originalKey.getValidUntil());
		String originalRegistrationKey = originalKey.getRegistrationKey();
		Date originalValidUntil = originalKey.getValidUntil();

		// Update temporary key
		TemporaryKey updatedKey = registrationManager.updateTemporaryRegistrationKey(emailAddress);
		dbInstance.commitAndCloseSession();

		// Validate the update
		Assert.assertNotNull(updatedKey);
		Assert.assertEquals(originalKey.getKey(), updatedKey.getKey()); // Same record updated
		Assert.assertEquals(emailAddress, updatedKey.getEmailAddress()); // Email should remain unchanged
		Assert.assertNotEquals(originalRegistrationKey, updatedKey.getRegistrationKey()); // Registration key should be updated
		Assert.assertNotEquals(originalValidUntil, updatedKey.getValidUntil()); // Valid until date should be updated
		Assert.assertTrue(updatedKey.getValidUntil().after(originalValidUntil)); // Ensure validUntil is extended
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
		organisationModule.setEnabled(false);

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
		organisationModule.setEnabled(false);
		
		//check equals matching
		assertTrue(registrationManager.validateEmailUsername("aoi@cyberiacafe.ch"));
		assertFalse(registrationManager.validateEmailUsername("aoi@cyberia.ch"));
		assertTrue(registrationManager.validateEmailUsername("aoi@blog.cyberiacafe.ch"));
	}
}
