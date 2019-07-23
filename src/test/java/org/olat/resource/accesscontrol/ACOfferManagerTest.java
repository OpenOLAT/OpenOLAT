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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.OLATResourceable;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.manager.ACMethodDAO;
import org.olat.resource.accesscontrol.manager.ACOfferDAO;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.OfferImpl;
import org.olat.resource.accesscontrol.model.TokenAccessMethod;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 *
 * Description:<br>
 *
 * <P>
 * Initial Date:  18 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ACOfferManagerTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;

	@Autowired
	private ACOfferDAO acOfferManager;

	@Autowired
	private ACService acService;

	@Autowired
	private ACMethodDAO acMethodManager;

	@Test
	public void testManagers() {
		assertNotNull(acOfferManager);
		assertNotNull(acService);
	}

	@Test
	public void testSaveOffer() {
		//create a resource
		OLATResourceable testOreable = new TypedResourceable(UUID.randomUUID().toString().replace("-", ""));
		OLATResource testOres = OLATResourceManager.getInstance().findOrPersistResourceable(testOreable);
		assertNotNull(testOres);

		dbInstance.commitAndCloseSession();

		//create an offer
		Offer offer = acOfferManager.createOffer(testOres, "TestSaveOffer");
		assertNotNull(offer);
		assertEquals(OfferImpl.class, offer.getClass());
		if(offer instanceof OfferImpl) {
			OfferImpl offerImpl = (OfferImpl)offer;
			offerImpl.setToken("token1");
		}
		offer.setValidFrom(new Date());
		offer.setValidTo(new Date());
		//and save the offer
		acOfferManager.saveOffer(offer);

		dbInstance.commitAndCloseSession();

		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		AccessMethod method = methods.get(0);
		OfferAccess access = acMethodManager.createOfferAccess(offer, method);
		acMethodManager.save(access);

		dbInstance.commitAndCloseSession();

		//check if the offer is saved
		List<Offer> offers = acOfferManager.findOfferByResource(testOres, true, null);
		assertNotNull(offers);
		assertEquals(1, offers.size());
		Offer savedOffer = offers.get(0);
		assertNotNull(savedOffer);
		assertEquals(OfferImpl.class, savedOffer.getClass());
		if(savedOffer instanceof OfferImpl) {
			OfferImpl offerImpl = (OfferImpl)savedOffer;
			assertEquals("token1", offerImpl.getToken());
		}
		assertNotNull(offer.getValidFrom());
		assertNotNull(offer.getValidTo());
		assertEquals(testOres.getResourceableId(), savedOffer.getResourceId());
		assertEquals(testOres.getResourceableTypeName(), savedOffer.getResourceTypeName());
		assertEquals("TestSaveOffer", savedOffer.getResourceDisplayName());
	}

	@Test
	public void testDeleteOffer() {
		OLATResourceable testOreable = new TypedResourceable(UUID.randomUUID().toString().replace("-", ""));
		OLATResource testOres = OLATResourceManager.getInstance().findOrPersistResourceable(testOreable);
		assertNotNull(testOres);

		dbInstance.commitAndCloseSession();

		//create an offer
		Offer offer = acOfferManager.createOffer(testOres, "TestDeleteOffer");
		assertNotNull(offer);
		assertEquals(OfferImpl.class, offer.getClass());
		//and save the offer
		acOfferManager.saveOffer(offer);

		dbInstance.commitAndCloseSession();

		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		AccessMethod method = methods.get(0);
		OfferAccess access = acMethodManager.createOfferAccess(offer, method);
		acMethodManager.save(access);

		dbInstance.commitAndCloseSession();

		//retrieve the offer
		List<Offer> offers = acOfferManager.findOfferByResource(testOres, true, null);
		assertNotNull(offers);
		assertEquals(1, offers.size());
		assertEquals(offer, offers.get(0));
		dbInstance.commitAndCloseSession();

		//delete the offer
		acOfferManager.deleteOffer(offer);
		dbInstance.commitAndCloseSession();

		//try to retrieve the offer
		List<Offer> noOffers = acOfferManager.findOfferByResource(testOres, true, null);
		assertNotNull(noOffers);
		assertEquals(0, noOffers.size());
		dbInstance.commitAndCloseSession();

		//retrieve all offers, deleted too
		List<Offer> delOffers = acOfferManager.findOfferByResource(testOres, false, null);
		assertNotNull(delOffers);
		assertEquals(1, delOffers.size());
		assertEquals(offer, delOffers.get(0));
		assertEquals(false, delOffers.get(0).isValid());
		dbInstance.commitAndCloseSession();
	}

	@Test
	public void testDeleteResource() {
		//create a random resource
		OLATResourceable testOreable = new TypedResourceable(UUID.randomUUID().toString().replace("-", ""));
		OLATResource testOres = OLATResourceManager.getInstance().findOrPersistResourceable(testOreable);
		assertNotNull(testOres);

		dbInstance.commitAndCloseSession();

		//create an offer
		Offer offer = acOfferManager.createOffer(testOres, "TestDeleteResource");
		assertNotNull(offer);
		assertEquals(OfferImpl.class, offer.getClass());
		//and save the offer
		acOfferManager.saveOffer(offer);

		dbInstance.commitAndCloseSession();

		//delete the resource
		testOres = dbInstance.getCurrentEntityManager().find(OLATResourceImpl.class, testOres.getKey());
		dbInstance.deleteObject(testOres);

		dbInstance.commitAndCloseSession();

		//load offer by resource -> nothing found
		List<Offer> retrievedOffers = acOfferManager.findOfferByResource(testOres, true, null);
		assertNotNull(retrievedOffers);
		assertEquals(0, retrievedOffers.size());

		//load offer by key -> found and loaded without error
		Offer retrievedOffer = acOfferManager.loadOfferByKey(offer.getKey());
		assertNotNull(retrievedOffer);
		assertNull(retrievedOffer.getResource());
		assertEquals(offer, retrievedOffer);
	}

	@Test
	public void testFilter() {
		//create resources
		OLATResourceable testOreable1 = new TypedResourceable(UUID.randomUUID().toString().replace("-", ""));
		OLATResource testOres1 = OLATResourceManager.getInstance().findOrPersistResourceable(testOreable1);
		assertNotNull(testOres1);

		OLATResourceable testOreable2 = new TypedResourceable(UUID.randomUUID().toString().replace("-", ""));
		OLATResource testOres2 = OLATResourceManager.getInstance().findOrPersistResourceable(testOreable2);
		assertNotNull(testOres2);

		OLATResourceable testOreable3 = new TypedResourceable(UUID.randomUUID().toString().replace("-", ""));
		OLATResource testOres3 = OLATResourceManager.getInstance().findOrPersistResourceable(testOreable3);
		assertNotNull(testOres3);

		dbInstance.commitAndCloseSession();

		//create  offers
		Offer offer1 = acOfferManager.createOffer(testOres1, "TestFilter 1");
		Offer offer2 = acOfferManager.createOffer(testOres2, "TestFilter 2");
		acOfferManager.saveOffer(offer1);
		acOfferManager.saveOffer(offer2);

		dbInstance.commitAndCloseSession();

		//filter by resources
		List<Long> resourceKeys = new ArrayList<>();
		resourceKeys.add(testOres1.getKey());
		resourceKeys.add(testOres2.getKey());
		resourceKeys.add(testOres3.getKey());
		Set<Long> filteredKeys = acOfferManager.filterResourceWithOffer(resourceKeys);
		assertNotNull(filteredKeys);
		assertEquals(2, filteredKeys.size());
		assertTrue(filteredKeys.contains(testOres1.getKey()));
		assertTrue(filteredKeys.contains(testOres2.getKey()));
		assertFalse(filteredKeys.contains(testOres3.getKey()));
	}

	@Test
	public void testFilterWithDelete() {
		//create resources
		OLATResourceable testOreable1 = new TypedResourceable(UUID.randomUUID().toString().replace("-", ""));
		OLATResource testOres1 = OLATResourceManager.getInstance().findOrPersistResourceable(testOreable1);
		assertNotNull(testOres1);

		OLATResourceable testOreable2 = new TypedResourceable(UUID.randomUUID().toString().replace("-", ""));
		OLATResource testOres2 = OLATResourceManager.getInstance().findOrPersistResourceable(testOreable2);
		assertNotNull(testOres2);

		OLATResourceable testOreable3 = new TypedResourceable(UUID.randomUUID().toString().replace("-", ""));
		OLATResource testOres3 = OLATResourceManager.getInstance().findOrPersistResourceable(testOreable3);
		assertNotNull(testOres3);

		dbInstance.commitAndCloseSession();

		//create  offers
		Offer offer1 = acOfferManager.createOffer(testOres1, "TestFilterWithDelete 1");
		Offer offer2 = acOfferManager.createOffer(testOres2, "TestFilterWithDelete 2");
		acOfferManager.saveOffer(offer1);
		acOfferManager.saveOffer(offer2);

		dbInstance.commitAndCloseSession();

		//delete resource of offer 2
		testOres2 = dbInstance.getCurrentEntityManager().find(OLATResourceImpl.class, testOres2.getKey());
		dbInstance.deleteObject(testOres2);

		//filter by resources
		List<Long> resourceKeys = new ArrayList<>();
		resourceKeys.add(testOres1.getKey());
		resourceKeys.add(testOres2.getKey());
		resourceKeys.add(testOres3.getKey());
		Set<Long> filteredKeys = acOfferManager.filterResourceWithOffer(resourceKeys);
		assertNotNull(filteredKeys);
		assertEquals(1, filteredKeys.size());
		assertTrue(filteredKeys.contains(testOres1.getKey()));
		assertFalse(filteredKeys.contains(testOres2.getKey()));
		assertFalse(filteredKeys.contains(testOres3.getKey()));
	}
}
