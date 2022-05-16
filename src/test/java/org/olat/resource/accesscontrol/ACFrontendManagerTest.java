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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.olat.test.JunitTestHelper.createRandomResource;
import static org.olat.test.JunitTestHelper.random;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.CodeHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.manager.ACMethodDAO;
import org.olat.resource.accesscontrol.manager.ACOfferDAO;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.FreeAccessMethod;
import org.olat.resource.accesscontrol.model.TokenAccessMethod;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalAccessMethod;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Description:<br>
 * Test the frontend manager
 *
 * <P>
 * Initial Date:  18 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ACFrontendManagerTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ACOfferDAO acOfferManager;
	@Autowired
	private ACService acService;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private ACMethodDAO acMethodManager;
	@Autowired
	private AccessControlModule acModule;

	@Test
	public void testManagers() {
		assertNotNull(acOfferManager);
		assertNotNull(acService);
		assertNotNull(dbInstance);
		assertNotNull(resourceManager);
		assertNotNull(repositoryManager);
		assertNotNull(securityManager);
	}

	@Test
	public void testRepoWorkflow() {
		//create a repository entry
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();

		//create and save an offer
		Offer offer = acService.createOffer(re.getOlatResource(), "TestRepoWorkflow");
		assertNotNull(offer);
		offer = acService.save(offer);
		dbInstance.commitAndCloseSession();

		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		AccessMethod method = methods.get(0);
		OfferAccess access = acMethodManager.createOfferAccess(offer, method);
		acMethodManager.save(access);

		dbInstance.commitAndCloseSession();

		//retrieve the offer
		List<Offer> offers = acService.findOfferByResource(re.getOlatResource(), true, null, null);
		assertEquals(1, offers.size());
		Offer savedOffer = offers.get(0);
		assertNotNull(savedOffer);
		assertNotNull(savedOffer.getResource());
		assertTrue(re.getOlatResource().equalsByPersistableKey(savedOffer.getResource()));
	}

	/**
	 * Test free access to a group without waiting list
	 */
	@Test
	public void testFreeAccesToBusinessGroup() {
		//create a group with a free offer
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("agp-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "Free group", "Really free", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, null);
		Offer offer = acService.createOffer(group.getResource(), "FreeGroup");
		offer = acService.save(offer);
		List<AccessMethod> freeMethods = acMethodManager.getAvailableMethodsByType(FreeAccessMethod.class);
		OfferAccess offerAccess = acService.createOfferAccess(offer, freeMethods.get(0));
		Assert.assertNotNull(offerAccess);
		offerAccess = acService.saveOfferAccess(offerAccess);
		dbInstance.commitAndCloseSession();

		//access it
		AccessResult result = acService.accessResource(id, offerAccess, null);
		Assert.assertNotNull(result);
		Assert.assertTrue(result.isAccessible());
		dbInstance.commitAndCloseSession();

		//is id a participant?
		boolean participant = businessGroupRelationDao.hasRole(id, group, GroupRoles.participant.name());
		Assert.assertTrue(participant);
	}

	/**
	 * Test free access to a group without waiting list and which is full
	 */
	@Test
	public void testFreeAccesToBusinessGroup_full() {
		//create a group with a free offer, fill 2 places on 2
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("agp-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("agp-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("agp-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "Free group", "But you must wait", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(2), false, false, null);
		businessGroupRelationDao.addRole(id1, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2, group, GroupRoles.participant.name());

		Offer offer = acService.createOffer(group.getResource(), "Free group (waiting)");
		offer = acService.save(offer);
		List<AccessMethod> freeMethods = acMethodManager.getAvailableMethodsByType(FreeAccessMethod.class);
		OfferAccess offerAccess = acService.createOfferAccess(offer, freeMethods.get(0));
		Assert.assertNotNull(offerAccess);
		offerAccess = acService.saveOfferAccess(offerAccess);
		dbInstance.commitAndCloseSession();

		//access it
		AccessResult result = acService.accessResource(id3, offerAccess, null);
		Assert.assertNotNull(result);
		Assert.assertFalse(result.isAccessible());
		dbInstance.commitAndCloseSession();

		//is id a waiting?
		boolean participant = businessGroupRelationDao.hasRole(id3, group, GroupRoles.participant.name());
		Assert.assertFalse(participant);
		boolean waiting = businessGroupRelationDao.hasRole(id3, group, GroupRoles.waiting.name());
		Assert.assertFalse(waiting);
	}

	/**
	 * Test free access to a group with waiting list enough place
	 */
	@Test
	public void testFreeAccesToBusinessGroupWithWaitingList_enoughPlace() {
		//create a group with a free offer
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("agp-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "Free group", "But you must wait", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(10), true, false, null);
		Offer offer = acService.createOffer(group.getResource(), "Free group (waiting)");
		offer = acService.save(offer);
		List<AccessMethod> freeMethods = acMethodManager.getAvailableMethodsByType(FreeAccessMethod.class);
		OfferAccess offerAccess = acService.createOfferAccess(offer, freeMethods.get(0));
		Assert.assertNotNull(offerAccess);
		offerAccess = acService.saveOfferAccess(offerAccess);
		dbInstance.commitAndCloseSession();

		//access it
		AccessResult result = acService.accessResource(id, offerAccess, null);
		Assert.assertNotNull(result);
		Assert.assertTrue(result.isAccessible());
		dbInstance.commitAndCloseSession();

		//is id a waiting?
		boolean participant = businessGroupRelationDao.hasRole(id, group, GroupRoles.participant.name());
		Assert.assertTrue(participant);
		boolean waiting = businessGroupRelationDao.hasRole(id, group, GroupRoles.waiting.name());
		Assert.assertFalse(waiting);
	}

	/**
	 * Test free access to a group with waiting list enough place
	 */
	@Test
	public void testFreeAccesToBusinessGroupWithWaitingList_full() {
		//create a group with a free offer, fill 2 places on 2
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("agp-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("agp-2");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("agp-3");
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "Free group", "But you must wait", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(2), true, false, null);
		businessGroupRelationDao.addRole(id1, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2, group, GroupRoles.participant.name());

		Offer offer = acService.createOffer(group.getResource(), "Free group (waiting)");
		offer = acService.save(offer);
		List<AccessMethod> freeMethods = acMethodManager.getAvailableMethodsByType(FreeAccessMethod.class);
		OfferAccess offerAccess = acService.createOfferAccess(offer, freeMethods.get(0));
		Assert.assertNotNull(offerAccess);
		offerAccess = acService.saveOfferAccess(offerAccess);
		dbInstance.commitAndCloseSession();

		//access it
		AccessResult result = acService.accessResource(id3, offerAccess, null);
		Assert.assertNotNull(result);
		Assert.assertTrue(result.isAccessible());
		dbInstance.commitAndCloseSession();

		//is id a waiting?
		boolean participant = businessGroupRelationDao.hasRole(id3, group, GroupRoles.participant.name());
		Assert.assertFalse(participant);
		boolean waiting = businessGroupRelationDao.hasRole(id3, group, GroupRoles.waiting.name());
		Assert.assertTrue(waiting);
	}


	/**
	 * Test paypal scenario where a user begin the process to pay an access
	 * to a group while an administrator is filling the group,
	 */
	@Test
	public void testPaiedAccesToBusinessGroupWithWaitingList_enoughPlaceButAdmin() {
		//enable paypal
		boolean enabled = acModule.isPaypalEnabled();
		if(!enabled) {
			acModule.setPaypalEnabled(true);
		}

		//create a group with a free offer
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("pay-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("agp-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("agp-" + UUID.randomUUID().toString());

		BusinessGroup group = businessGroupService.createBusinessGroup(null, "Free group", "But you must wait", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(2), true, false, null);
		Offer offer = acService.createOffer(group.getResource(), "Free group (waiting)");
		offer = acService.save(offer);
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(PaypalAccessMethod.class);
		Assert.assertFalse(methods.isEmpty());
		OfferAccess offerAccess = acService.createOfferAccess(offer, methods.get(0));
		Assert.assertNotNull(offerAccess);
		offerAccess = acService.saveOfferAccess(offerAccess);
		dbInstance.commitAndCloseSession();

		//id1 start payment process
		boolean reserved = acService.reserveAccessToResource(id1, offerAccess);
		Assert.assertTrue(reserved);
		dbInstance.commitAndCloseSession();

		//admin fill the group
		businessGroupRelationDao.addRole(id2, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id3, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//id1 finish the process
		AccessResult result = acService.accessResource(id1, offerAccess, null);
		Assert.assertNotNull(result);
		Assert.assertTrue(result.isAccessible());
		dbInstance.commitAndCloseSession();

		//is id a waiting?
		boolean participant = businessGroupRelationDao.hasRole(id1, group, GroupRoles.participant.name());
		Assert.assertTrue(participant);
		boolean waiting = businessGroupRelationDao.hasRole(id1, group, GroupRoles.waiting.name());
		Assert.assertFalse(waiting);

		if(!enabled) {
			acModule.setPaypalEnabled(false);
		}
	}

	@Test
	public void testPaiedAccesToBusinessGroup_full() {
		//enable paypal
		boolean enabled = acModule.isPaypalEnabled();
		if(!enabled) {
			acModule.setPaypalEnabled(true);
		}

		//create a group with a free offer
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("pay-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("agp-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("agp-" + UUID.randomUUID().toString());

		BusinessGroup group = businessGroupService.createBusinessGroup(null, "Free group", "But you must wait", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), Integer.valueOf(2), false, false, null);
		Offer offer = acService.createOffer(group.getResource(), "Free group (waiting)");
		offer = acService.save(offer);
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(PaypalAccessMethod.class);
		Assert.assertFalse(methods.isEmpty());
		OfferAccess offerAccess = acService.createOfferAccess(offer, methods.get(0));
		Assert.assertNotNull(offerAccess);
		offerAccess = acService.saveOfferAccess(offerAccess);
		dbInstance.commitAndCloseSession();

		//admin fill the group
		businessGroupRelationDao.addRole(id2, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id3, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//id1 try to reserve a place before the payment process
		boolean reserved = acService.reserveAccessToResource(id1, offerAccess);
		Assert.assertFalse(reserved);

		if(!enabled) {
			acModule.setPaypalEnabled(false);
		}
	}
	
	/**
	 * Check a special case which produced NPE
	 */
	@Test
	public void testPaiedReservationAccessToBusinessGroupNoLimit() {
		//enable paypal
		boolean enabled = acModule.isPaypalEnabled();
		if(!enabled) {
			acModule.setPaypalEnabled(true);
		}

		//create a group with a free offer
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pay-21");

		BusinessGroup group = businessGroupService.createBusinessGroup(null, "Paypal group", "Asap", BusinessGroup.BUSINESS_TYPE,
				Integer.valueOf(0), null, true, false, null);
		Offer offer = acService.createOffer(group.getResource(), "Paypal group (no limit)");
		offer = acService.save(offer);
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(PaypalAccessMethod.class);
		Assert.assertFalse(methods.isEmpty());
		OfferAccess offerAccess = acService.createOfferAccess(offer, methods.get(0));
		Assert.assertNotNull(offerAccess);
		offerAccess = acService.saveOfferAccess(offerAccess);
		dbInstance.commitAndCloseSession();
		
		//id try to reserve a place before the payment process, no problem, no limit
		boolean reserved = acService.reserveAccessToResource(id, offerAccess);
		Assert.assertTrue(reserved);
	}

	@Test
	public void makeAccessible() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("acc-" + UUID.randomUUID());
		Organisation organisation = organisationService.createOrganisation(random(), null, random(), null, null);
		organisationService.addMember(organisation, id, OrganisationRoles.user);
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(FreeAccessMethod.class);
		AccessMethod method = methods.get(0);

		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();

		//create an offer to buy
		OLATResource randomOres = re.getOlatResource();
		Offer offer = acService.createOffer(randomOres, "Test auto access");
		offer.setAutoBooking(true);
		OfferAccess link = acService.createOfferAccess(offer, method);
		offer = acService.save(offer);
		link = acService.saveOfferAccess(link);
		acService.updateOfferOrganisations(offer, List.of(organisation));
		dbInstance.commit();

		long start = System.nanoTime();
		AccessResult acResult = acService.isAccessible(re, id, null, false, true);
		Assert.assertNotNull(acResult);
		Assert.assertTrue(acResult.isAccessible());
		dbInstance.commit();
		CodeHelper.printMilliSecondTime(start, "One click");
	}
	
	@Test
	public void testStandardMethods() {
		Identity ident = JunitTestHelper.createAndPersistIdentityAsRndUser("ac-method-mgr");
		
		Roles roles = Roles.authorRoles();
		List<AccessMethod> methods = acService.getAvailableMethods(ident, roles);
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
	public void shouldUpdateOrganisations() {
		Offer offer = acService.createOffer(createRandomResource(), random());
		offer = acService.save(offer);
		dbInstance.commitAndCloseSession();
		
		// No organisations
		acService.updateOfferOrganisations(offer, null);
		dbInstance.commitAndCloseSession();
		
		assertThat(acService.getOfferOrganisations(offer)).isEmpty();
		
		// Add two organisations
		Organisation organisation1 = organisationService.createOrganisation(random(), null, random(), null, null);
		Organisation organisation2 = organisationService.createOrganisation(random(), null, random(), null, null);
		acService.updateOfferOrganisations(offer, List.of(organisation1, organisation2));
		dbInstance.commitAndCloseSession();
		
		assertThat(acService.getOfferOrganisations(offer)).containsExactlyInAnyOrder(organisation1, organisation2);
		
		// Remove one organisation, add two new organisations
		Organisation organisation3 = organisationService.createOrganisation(random(), null, random(), null, null);
		Organisation organisation4 = organisationService.createOrganisation(random(), null, random(), null, null);
		acService.updateOfferOrganisations(offer, List.of(organisation2, organisation3, organisation4));
		dbInstance.commitAndCloseSession();
		
		assertThat(acService.getOfferOrganisations(offer)).containsExactlyInAnyOrder(organisation2, organisation3, organisation4);
		
		// Delete all organisations
		acService.updateOfferOrganisations(offer, null);
		dbInstance.commitAndCloseSession();
		
		assertThat(acService.getOfferOrganisations(offer)).isEmpty();
	}
	
}