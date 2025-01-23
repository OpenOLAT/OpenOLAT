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

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupMembershipHistory;
import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.manager.GroupMembershipHistoryDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumMailing;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.manager.ACMethodDAO;
import org.olat.resource.accesscontrol.manager.ACOfferDAO;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.FreeAccessMethod;
import org.olat.resource.accesscontrol.model.TokenAccessMethod;
import org.olat.resource.accesscontrol.provider.invoice.model.InvoiceAccessMethod;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalAccessMethod;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.dumbster.smtp.SmtpMessage;

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
	private CurriculumService curriculumService;
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
	@Autowired
	private GroupMembershipHistoryDAO groupMembershipHistoryDao;

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
		AccessResult result = acService.accessResource(id, offerAccess, null, id);
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
		AccessResult result = acService.accessResource(id3, offerAccess, null, id3);
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
		AccessResult result = acService.accessResource(id, offerAccess, null, id);
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
		AccessResult result = acService.accessResource(id3, offerAccess, null, id3);
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
		boolean reserved = acService.reserveAccessToResource(id1, offerAccess.getOffer(), offerAccess.getMethod());
		Assert.assertTrue(reserved);
		dbInstance.commitAndCloseSession();

		//admin fill the group
		businessGroupRelationDao.addRole(id2, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id3, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//id1 finish the process
		AccessResult result = acService.accessResource(id1, offerAccess, null, id1);
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
		boolean reserved = acService.reserveAccessToResource(id1, offerAccess.getOffer(), offerAccess.getMethod());
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
		boolean reserved = acService.reserveAccessToResource(id, offerAccess.getOffer(), offerAccess.getMethod());
		Assert.assertTrue(reserved);
	}
	
	@Test
	public void testPaiedReservationAccessToCurriculumElement() {
		//enable paypal
		boolean enabled = acModule.isPaypalEnabled();
		if(!enabled) {
			acModule.setPaypalEnabled(true);
		}

		//create a group with a free offer
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pay-21");
		
		Curriculum curriculum = curriculumService.createCurriculum("CUR-AC-1", "Curriculum AC 1", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for reservation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		
		Offer offer = acService.createOffer(element.getResource(), "Paypal curriculum element");
		offer = acService.save(offer);
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(PaypalAccessMethod.class);
		Assert.assertFalse(methods.isEmpty());
		OfferAccess offerAccess = acService.createOfferAccess(offer, methods.get(0));
		Assert.assertNotNull(offerAccess);
		offerAccess = acService.saveOfferAccess(offerAccess);
		dbInstance.commitAndCloseSession();
		
		//id try to reserve a place before the payment process, no problem, no limit
		boolean reserved = acService.reserveAccessToResource(id, offerAccess.getOffer(), offerAccess.getMethod());
		Assert.assertTrue(reserved);
		
		List<ResourceReservation> reservations = acService.getReservations(List.of(element.getResource()));
		Assertions.assertThat(reservations)
			.hasSize(1);
	}
	
	@Test
	public void testOrderAndCancelAccessToCurriculumElement() {
		//enable paypal
		boolean enabled = acModule.isPaypalEnabled();
		if(!enabled) {
			acModule.setPaypalEnabled(true);
		}

		//create curriculum with a free offer
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pay-22");
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser("doer-22");
		
		Curriculum curriculum = curriculumService.createCurriculum("CUR-AC-2", "Curriculum AC 2", "Curriculum", false, null);
		CurriculumElement implementationElement = curriculumService.createCurriculumElement("Implementation-for-order", "Implementation for order",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-order", "Element for order",
				CurriculumElementStatus.active, null, null, implementationElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		
		Offer offer = acService.createOffer(implementationElement.getResource(), "Invoice curriculum element");
		offer = acService.save(offer);
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(InvoiceAccessMethod.class);
		Assert.assertFalse(methods.isEmpty());
		OfferAccess offerAccess = acService.createOfferAccess(offer, methods.get(0));
		Assert.assertNotNull(offerAccess);
		offerAccess = acService.saveOfferAccess(offerAccess);
		dbInstance.commitAndCloseSession();
		
		// Book the curriculum
		AccessResult result = acService.accessResource(id, offerAccess, OrderStatus.PREPAYMENT, null, doer);
		Assert.assertTrue(result.isAccessible());
		dbInstance.commitAndCloseSession();
		
		List<CurriculumElementMembership> memberships = curriculumService.getCurriculumElementMemberships(curriculum, id);
		Assertions.assertThat(memberships)
			.hasSize(2)
			.map(CurriculumElementMembership::getCurriculumElementKey)
			.containsExactlyInAnyOrder(implementationElement.getKey(), element.getKey());
		
		List<GroupMembershipHistory> activeHistory = groupMembershipHistoryDao.loadMembershipHistory(element.getGroup(), id);
		Assertions.assertThat(activeHistory)
			.filteredOn(point -> GroupMembershipStatus.active == point.getStatus())
			.hasSize(1);
		
		dbInstance.commitAndCloseSession();
		
		// Cancel the order
		Order order = result.getOrder();
		MailTemplate mail = CurriculumMailing.getStatusCancelledMailTemplate(curriculum, implementationElement, doer);
		MailPackage mailing = new MailPackage(mail, null);
		acService.cancelOrder(order, doer, "Cancelled", mailing);
		
		dbInstance.commitAndCloseSession();
		
		// Check all memberships are cancelled
		List<CurriculumElementMembership> cancelledMemberships = curriculumService.getCurriculumElementMemberships(curriculum, id);
		Assert.assertTrue(cancelledMemberships.isEmpty());
		
		List<GroupMembershipHistory> canceledHistory = groupMembershipHistoryDao.loadMembershipHistory(element.getGroup(), id);
		Assertions.assertThat(canceledHistory)
			.hasSize(2)
			.filteredOn(point -> GroupMembershipStatus.cancel == point.getStatus())
			.hasSize(1);
		
		// Check email send
		List<SmtpMessage> mails = getSmtpServer().getReceivedEmails();
		Assertions.assertThat(mails)
			.hasSize(1);
	}

	@Test
	public void makeAccessible() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("acc");
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
		AccessResult acResult = acService.isAccessible(re, id, null, false, null, true);
		Assert.assertNotNull(acResult);
		Assert.assertTrue(acResult.isAccessible());
		dbInstance.commit();
		CodeHelper.printMilliSecondTime(start, "One click");
	}
	
	@Test
	public void testStandardMethods() {
		Identity ident = JunitTestHelper.createAndPersistIdentityAsRndUser("ac-method-mgr");
		
		Roles roles = Roles.authorRoles();
		List<AccessMethod> methods = acService.getAvailableMethods(createRandomResource(), ident, roles);
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