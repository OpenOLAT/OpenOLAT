/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.manager;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.mail.MailPackage;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumAccountingSearchParams;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.BillingAddress;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.FreeAccessMethod;
import org.olat.resource.accesscontrol.model.OrderAdditionalInfos;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumAccountingDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ACService acService;
	@Autowired
	private CurriculumService curriculumService;
	
	@Autowired
	private CurriculumAccountingDAO curriculumAccountingDao;
	
	@Test
	public void bookingOrdersByCurriculumOwner() {
		// Create a curriculum with an element and an invoice offer
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("account-1");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("account-2");
		
		// Make curriculum
		Organisation organisation = JunitTestHelper.getDefaultOrganisation();
		Curriculum curriculum = curriculumService.createCurriculum("CUR-ACCOUNTING-1", "Curriculum accounting 1", "Curriculum", false, organisation);
		curriculumService.addMember(curriculum, owner, CurriculumRoles.curriculumowner);
		
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for reservation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		
		AccessResult accessResult = createOrder(id, element.getResource());
		Assert.assertTrue(accessResult.isAccessible());
		
		List<UserPropertyHandler> handlers = new ArrayList<>();
		CurriculumAccountingSearchParams searchParams = new CurriculumAccountingSearchParams();
		searchParams.setIdentity(owner);
		List<BookingOrder> bookingOrders = curriculumAccountingDao.bookingOrders(searchParams, handlers);
		Assertions.assertThat(bookingOrders)
			.hasSize(1)
			.map(BookingOrder::getOrder)
			.containsExactly(accessResult.getOrder());
	}
	
	@Test
	public void bookingOrdersByCurriculumNotOwner() {
		// Create a curriculum with an element and an invoice offer
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("account-6");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("account-7");
		
		// Make curriculum
		Organisation organisation = JunitTestHelper.getDefaultOrganisation();
		Curriculum curriculum = curriculumService.createCurriculum("CUR-ACCOUNTING-1", "Curriculum accounting 1", "Curriculum", false, organisation);
		curriculumService.addMember(curriculum, owner, CurriculumRoles.curriculumowner);
		
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for reservation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		
		AccessResult accessResult = createOrder(id, element.getResource());
		Assert.assertTrue(accessResult.isAccessible());
		
		// Buyer has not access to the report.
		List<UserPropertyHandler> handlers = new ArrayList<>();
		CurriculumAccountingSearchParams searchParams = new CurriculumAccountingSearchParams();
		searchParams.setIdentity(id);
		List<BookingOrder> bookingOrders = curriculumAccountingDao.bookingOrders(searchParams, handlers);
		Assertions.assertThat(bookingOrders)
			.isEmpty();
	}
	
	@Test
	public void bookingOrdersByCurriculum() {
		// Create a curriculum with an element and an invoice offer
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("account-3");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("account-4");
		
		// Make curriculum
		Organisation organisation = JunitTestHelper.getDefaultOrganisation();
		Curriculum curriculum = curriculumService.createCurriculum("CUR-ACCOUNTING-2", "Curriculum accounting 2", "Curriculum", false, organisation);
		curriculumService.addMember(curriculum, coach, CurriculumRoles.curriculumowner);
		
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for reservation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		
		AccessResult accessResult = createOrder(id, element.getResource());
		Assert.assertTrue(accessResult.isAccessible());
		
		List<UserPropertyHandler> handlers = new ArrayList<>();
		CurriculumAccountingSearchParams searchParams = new CurriculumAccountingSearchParams();
		searchParams.setCurriculum(curriculum);
		List<BookingOrder> bookingOrders = curriculumAccountingDao.bookingOrders(searchParams, handlers);
		Assertions.assertThat(bookingOrders)
			.hasSize(1)
			.map(BookingOrder::getOrder)
			.containsExactly(accessResult.getOrder());
	}
	
	@Test
	public void bookingOrdersByCurriculumElement() {
		// Create a curriculum with an element and an invoice offer
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("account-5");
		
		// Make curriculum
		Organisation organisation = JunitTestHelper.getDefaultOrganisation();
		Curriculum curriculum = curriculumService.createCurriculum("CUR-ACCOUNTING-3", "Curriculum accounting 3", "Curriculum", false, organisation);
		
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for reservation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		
		AccessResult accessResult = createOrder(id, element.getResource());
		Assert.assertTrue(accessResult.isAccessible());
		
		List<UserPropertyHandler> handlers = new ArrayList<>();
		CurriculumAccountingSearchParams searchParams = new CurriculumAccountingSearchParams();
		searchParams.setCurriculumElement(element);
		List<BookingOrder> bookingOrders = curriculumAccountingDao.bookingOrders(searchParams, handlers);
		Assertions.assertThat(bookingOrders)
			.hasSize(1)
			.map(BookingOrder::getOrder)
			.containsExactly(accessResult.getOrder());
	}
	
	private AccessResult createOrder(Identity delivery, OLATResource resource) {
		// Make an offer
		Offer offer = acService.createOffer(resource, "Access curriculum element");
		offer = acService.save(offer);
		List<AccessMethod> methods = acService.getAvailableMethodsByType(FreeAccessMethod.class);
		OfferAccess offerAccess = acService.createOfferAccess(offer, methods.get(0));
		offerAccess = acService.saveOfferAccess(offerAccess);
		BillingAddress billingAddress = acService.createBillingAddress(null, delivery);
		dbInstance.commitAndCloseSession();
		
		// Book the curriculum
		MailPackage mailing = new MailPackage(false);
		OrderAdditionalInfos infos = new OrderAdditionalInfos(null, null, billingAddress, false);
		AccessResult result = acService.accessResource(delivery, offerAccess, OrderStatus.PAYED, infos, mailing, delivery, null);
		dbInstance.commitAndCloseSession();
		return result;
	}

}
