/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */

package org.olat.resource.accesscontrol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.OLATResourceable;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.manager.ACMethodDAO;
import org.olat.resource.accesscontrol.manager.ACOfferDAO;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.FreeAccessMethod;
import org.olat.resource.accesscontrol.model.TokenAccessMethod;
import org.olat.shibboleth.manager.ShibbolethAutoAccessMethod;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Description:<br>
 * Test the payment manager
 *
 * <P>
 * Initial Date:  18 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ACMethodManagerTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;

	@Autowired
	private ACOfferDAO acOfferManager;

	@Autowired
	private ACService acService;

	@Autowired
	private ACMethodDAO acMethodManager;

	@Autowired
	private OLATResourceManager resourceManager;

	@Before
	public void setUp() {
		acMethodManager.enableMethod(ShibbolethAutoAccessMethod.class, true);
	}

	@Test
	public void testManagers() {
		assertNotNull(acOfferManager);
		assertNotNull(acService);
		assertNotNull(dbInstance);
		assertNotNull(acMethodManager);
	}

	@Test
	public void testTokenMethod() {
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		assertNotNull(methods);
		assertEquals(1, methods.size());
	}

	@Test
	public void testFreeMethod() {
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(FreeAccessMethod.class);
		assertNotNull(methods);
		assertEquals(1, methods.size());
	}

	@Test
	public void testAutoShibMethod() {
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(ShibbolethAutoAccessMethod.class);
		assertNotNull(methods);
		assertEquals(1, methods.size());
	}

	@Test
	public void testStandardMethods() {
		List<AccessMethod> methods = acMethodManager.getAvailableMethods();
		assertNotNull(methods);
		assertTrue(methods.size() >= 2);

		Set<String> duplicateTypes = new HashSet<>();

		boolean foundFree = false;
		boolean foundToken = false;
		for(AccessMethod method:methods) {
			Assert.assertFalse(duplicateTypes.contains(method.getType()));
			if(method instanceof FreeAccessMethod) {
				foundFree = true;
			} else if(method instanceof TokenAccessMethod) {
				foundToken = true;
			}
			assertTrue(method.isEnabled());
			assertTrue(method.isValid());
			duplicateTypes.add(method.getType());
		}
		assertTrue(foundFree);
		assertTrue(foundToken);
	}

	@Test
	public void testIsValidMethodAvailable() {
		//create a resource and an offer
		OLATResource randomOres = createResource();
		Offer offer = acService.createOffer(randomOres, "TestIsValidMethodAvailable");
		offer = acService.save(offer);
		dbInstance.commitAndCloseSession();

		//create a link offer to gui method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		AccessMethod method = methods.get(0);
		OfferAccess access = acMethodManager.createOfferAccess(offer, method);
		acMethodManager.save(access);

		//create a link offer to gui method
		List<AccessMethod> methodsNonGUI = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		AccessMethod methodNonGUI = methodsNonGUI.get(0);
		OfferAccess accessNonGUI = acMethodManager.createOfferAccess(offer, methodNonGUI);
		acMethodManager.save(accessNonGUI);

		dbInstance.commitAndCloseSession();

		boolean isAvailable = acMethodManager.isValidMethodAvailable(randomOres, null);

		assertTrue(isAvailable);
	}

	@Test
	public void testIsValidMethodAvailable_nonGui() {
		//create a resource and an offer
		OLATResource randomOres = createResource();
		Offer offer = acService.createOffer(randomOres, "TestIsValidMethodAvailableNonGui");
		offer = acService.save(offer);
		dbInstance.commitAndCloseSession();

		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(ShibbolethAutoAccessMethod.class);
		assertNotNull(methods);
		assertEquals(1, methods.size());

		AccessMethod method = methods.get(0);
		OfferAccess access = acMethodManager.createOfferAccess(offer, method);
		acMethodManager.save(access);

		dbInstance.commitAndCloseSession();

		boolean isAvailable = acMethodManager.isValidMethodAvailable(randomOres, null);

		assertFalse(isAvailable);
	}

	@Test
	public void testOfferAccess() {
		//create a resource and an offer
		OLATResource randomOres = createResource();
		Offer offer = acService.createOffer(randomOres, "TestOfferAccess");
		offer = acService.save(offer);
		dbInstance.commitAndCloseSession();

		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		assertNotNull(methods);
		assertEquals(1, methods.size());

		AccessMethod method = methods.get(0);
		OfferAccess access = acMethodManager.createOfferAccess(offer, method);
		acMethodManager.save(access);

		dbInstance.commitAndCloseSession();

		//retrieve the link
		List<OfferAccess> retrievedOfferAccess = acMethodManager.getOfferAccess(offer, true);
		assertNotNull(retrievedOfferAccess);
		assertEquals(1, retrievedOfferAccess.size());
		OfferAccess retrievedAccess = retrievedOfferAccess.get(0);
		assertNotNull(retrievedAccess);
		assertNotNull(retrievedAccess.getMethod());
		assertEquals(method, retrievedAccess.getMethod());
		assertNotNull(retrievedAccess.getOffer());
		Assert.assertEquals(offer, retrievedAccess.getOffer());
	}

	@Test
	public void testSeveralOfferAccess() {
		//create some resources and offers
		OLATResource randomOres1 = createResource();
		Offer offer1 = acService.createOffer(randomOres1, "TestSeveralOfferAccess 1");
		offer1 = acService.save(offer1);

		OLATResource randomOres2 = createResource();
		Offer offer2 = acService.createOffer(randomOres2, "TestSeveralOfferAccess 2");
		offer2 = acService.save(offer2);

		OLATResource randomOres3 = createResource();
		Offer offer3 = acService.createOffer(randomOres3, "TestSeveralOfferAccess 3");
		offer3 = acService.save(offer3);

		dbInstance.commitAndCloseSession();

		//create a link offer to method
		List<AccessMethod> tokenMethods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		assertNotNull(tokenMethods);
		assertEquals(1, tokenMethods.size());
		AccessMethod tokenMethod = tokenMethods.get(0);

		List<AccessMethod> freeMethods = acMethodManager.getAvailableMethodsByType(FreeAccessMethod.class);
		assertNotNull(freeMethods);
		assertEquals(1, freeMethods.size());
		AccessMethod freeMethod = freeMethods.get(0);

		OfferAccess access1 = acMethodManager.createOfferAccess(offer1, tokenMethod);
		acMethodManager.save(access1);

		OfferAccess access2 = acMethodManager.createOfferAccess(offer2, tokenMethod);
		acMethodManager.save(access2);

		OfferAccess access3_1 = acMethodManager.createOfferAccess(offer3, tokenMethod);
		acMethodManager.save(access3_1);

		OfferAccess access3_2 = acMethodManager.createOfferAccess(offer3, freeMethod);
		acMethodManager.save(access3_2);

		dbInstance.commitAndCloseSession();

		//retrieve the link to offer 1
		List<OfferAccess> retrievedOfferAccess = acMethodManager.getOfferAccess(offer1, true);
		assertNotNull(retrievedOfferAccess);
		assertEquals(1, retrievedOfferAccess.size());
		OfferAccess retrievedAccess = retrievedOfferAccess.get(0);
		assertNotNull(retrievedAccess);
		assertNotNull(retrievedAccess.getMethod());
		assertEquals(tokenMethod, retrievedAccess.getMethod());
		assertNotNull(retrievedAccess.getOffer());
		assertEquals(offer1, retrievedAccess.getOffer());
		dbInstance.commitAndCloseSession();


		{//retrieve the links to offer 3
			List<OfferAccess> retrievedOfferAccess3 = acMethodManager.getOfferAccess(offer3, true);
			assertNotNull(retrievedOfferAccess3);
			assertEquals(2, retrievedOfferAccess3.size());
			//3_1
			OfferAccess retrievedAccess3_1 = retrievedOfferAccess3.get(0);
			assertNotNull(retrievedAccess3_1);
			assertNotNull(retrievedAccess3_1.getMethod());
			if(access3_1.equals(retrievedAccess3_1)) {
				assertEquals(tokenMethod, retrievedAccess3_1.getMethod());
			} else {
				assertEquals(freeMethod, retrievedAccess3_1.getMethod());
			}
			assertNotNull(retrievedAccess3_1.getOffer());
			assertEquals(offer3, retrievedAccess3_1.getOffer());
			//3_2
			OfferAccess retrievedAccess3_2 = retrievedOfferAccess3.get(1);
			assertNotNull(retrievedAccess3_2);
			assertNotNull(retrievedAccess3_2.getMethod());
			if(access3_2.equals(retrievedAccess3_2)) {
				assertEquals(freeMethod, retrievedAccess3_2.getMethod());
			} else {
				assertEquals(tokenMethod, retrievedAccess3_2.getMethod());
			}
			assertNotNull(retrievedAccess3_2.getOffer());
			assertEquals(offer3, retrievedAccess3_2.getOffer());

			dbInstance.commitAndCloseSession();
		}

		{//retrieve the links by resource
			List<Offer> offers = new ArrayList<>();
			offers.add(offer1);
			offers.add(offer2);
			offers.add(offer3);

			List<OfferAccess> retrievedAllOfferAccess = acMethodManager.getOfferAccess(offers, true);
			assertNotNull(retrievedAllOfferAccess);
			assertEquals(4, retrievedAllOfferAccess.size());

			dbInstance.commitAndCloseSession();
		}
	}

	@Test
	public void testDeleteOfferAccess() {
		//create some resources and offers
		OLATResource randomOres1 = createResource();
		Offer offer = acService.createOffer(randomOres1, "TestDeleteOfferAccess");
		offer = acService.save(offer);
		dbInstance.commitAndCloseSession();

		//create two link offer to method
		List<AccessMethod> tokenMethods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		assertNotNull(tokenMethods);
		assertEquals(1, tokenMethods.size());
		AccessMethod tokenMethod = tokenMethods.get(0);

		List<AccessMethod> freeMethods = acMethodManager.getAvailableMethodsByType(FreeAccessMethod.class);
		assertNotNull(freeMethods);
		assertEquals(1, freeMethods.size());
		AccessMethod freeMethod = freeMethods.get(0);


		OfferAccess access3_1 = acMethodManager.createOfferAccess(offer, tokenMethod);
		acMethodManager.save(access3_1);

		OfferAccess access3_2 = acMethodManager.createOfferAccess(offer, freeMethod);
		acMethodManager.save(access3_2);

		dbInstance.commitAndCloseSession();

		//delete one of them
		acMethodManager.delete(access3_2);
		dbInstance.commitAndCloseSession();

		//retrieve
		List<OfferAccess> retrievedOfferAccess = acMethodManager.getOfferAccess(offer, true);
		assertNotNull(retrievedOfferAccess);
		assertEquals(1, retrievedOfferAccess.size());
		assertEquals(access3_1, retrievedOfferAccess.get(0));
	}

	private OLATResource createResource() {
		//create a repository entry
		OLATResourceable resourceable = new TypedResourceable(UUID.randomUUID().toString().replace("-", ""));
		OLATResource r =  resourceManager.createOLATResourceInstance(resourceable);
		dbInstance.saveObject(r);
		return r;
	}
}
