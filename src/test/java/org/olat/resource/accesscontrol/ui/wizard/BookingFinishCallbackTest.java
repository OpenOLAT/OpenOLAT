/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.resource.accesscontrol.ui.wizard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.DefaultStepsRunContext;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.gui.util.WindowControlMocker;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.manager.ACMethodDAO;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.FreeAccessMethod;
import org.olat.resource.accesscontrol.model.TransientBillingAddress;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 4 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class BookingFinishCallbackTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ACService acService;
	@Autowired
	private ACMethodDAO acMethodManager;

	private OfferAccess createFreeOfferAccess(Identity bookedIdentity) {
		RepositoryEntry repositoryEntry = JunitTestHelper.createRandomRepositoryEntry(bookedIdentity);
		OLATResource resource = repositoryEntry.getOlatResource();
		Offer offer = acService.createOffer(resource, random());
		offer = acService.save(offer);
		List<AccessMethod> freeMethods = acMethodManager.getAvailableMethodsByType(FreeAccessMethod.class);
		OfferAccess offerAccess = acService.createOfferAccess(offer, freeMethods.get(0));
		return acService.saveOfferAccess(offerAccess);
	}

	@Test
	public void shouldCreateOrder() {
		Identity bookedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser(UUID.randomUUID().toString());
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(UUID.randomUUID().toString());
		OfferAccess offerAccess = createFreeOfferAccess(bookedIdentity);
		dbInstance.commitAndCloseSession();

		BookingContext bookingContext = new BookingContext(offerAccess, bookedIdentity, doer, OrderStatus.PAYED, List.of());
		BookingFinishCallback finishCallback = new BookingFinishCallback(bookingContext);
		UserRequest ureq = new SyntheticUserRequest(doer, Locale.ENGLISH);
		WindowControl wControl = new WindowControlMocker();

		finishCallback.execute(ureq, wControl, new DefaultStepsRunContext());
		dbInstance.commitAndCloseSession();

		assertThat(bookingContext.isAccessible()).isTrue();
		Order order = bookingContext.getOrder();
		assertThat(order).isNotNull();
	}

	@Test
	public void shouldApplyNewBillingAddressAndOrderDetails() {
		Identity bookedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser(UUID.randomUUID().toString());
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(UUID.randomUUID().toString());
		OfferAccess offerAccess = createFreeOfferAccess(bookedIdentity);
		dbInstance.commitAndCloseSession();

		BookingContext bookingContext = new BookingContext(offerAccess, bookedIdentity, doer, OrderStatus.PAYED, List.of());
		TransientBillingAddress billingAddress = new TransientBillingAddress();
		billingAddress.setNameLine1("Jane Doe");
		billingAddress.setAddressLine1("Main street 1");
		billingAddress.setZip("1234");
		billingAddress.setCity("Anywhere");
		billingAddress.setCountry("CH");
		bookingContext.setBillingAddress(billingAddress);
		bookingContext.setPurchaseOrderNumber("PO-123");
		bookingContext.setComment("Test comment");

		BookingFinishCallback finishCallback = new BookingFinishCallback(bookingContext);
		UserRequest ureq = new SyntheticUserRequest(doer, Locale.ENGLISH);
		WindowControl wControl = new WindowControlMocker();

		finishCallback.execute(ureq, wControl, new DefaultStepsRunContext());
		dbInstance.commitAndCloseSession();

		Order order = bookingContext.getOrder();
		assertThat(order).isNotNull();
		assertThat(order.getPurchaseOrderNumber()).isEqualTo("PO-123");
		assertThat(order.getComment()).isEqualTo("Test comment");
		assertThat(order.getBillingAddress()).isNotNull();
		assertThat(order.getBillingAddress().getKey()).isNotNull();
		assertThat(order.getBillingAddress().getNameLine1()).isEqualTo("Jane Doe");
		assertThat(order.getBillingAddress().getCity()).isEqualTo("Anywhere");
	}

}
