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
package org.olat.resource.accesscontrol.ui.wizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.CoreSpringFactory;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.BillingAddress;
import org.olat.resource.accesscontrol.Order;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The only place in the booking wizard that writes data: creates the order,
 * then one form survey participation and session per attached form, and
 * saves the answers collected in the wizard's form steps into them.
 *
 * Initial date: 2 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class BookingFinishCallback implements StepRunnerCallback {

	private final BookingContext bookingContext;

	@Autowired
	private ACService acService;
	@Autowired
	private BookingWizardHelper bookingWizardHelper;

	public BookingFinishCallback(BookingContext bookingContext) {
		CoreSpringFactory.autowireObject(this);
		this.bookingContext = bookingContext;
	}

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		AccessResult result = acService.accessResource(bookingContext.getBookedIdentity(), bookingContext.getOfferAccess(),
				bookingContext.getOrderStatus(), bookingContext.getArgument(), bookingContext.getDoer());
		bookingContext.setAccessible(result.isAccessible());
		if (!result.isAccessible()) {
			return StepsMainRunController.DONE_MODIFIED;
		}

		Order order = result.getOrder();
		bookingContext.setOrder(order);
		if (bookingContext.getBillingAddress() != null) {
			order = applyBillingDetails(order);
			bookingContext.setOrder(order);
		}
		bookingWizardHelper.saveFormResponses(ureq, runContext, bookingContext, order, bookingContext.getBookedIdentity());

		return StepsMainRunController.DONE_MODIFIED;
	}

	private Order applyBillingDetails(Order order) {
		BillingAddress billingAddress = bookingContext.getBillingAddress();
		if (billingAddress.getKey() == null) {
			BillingAddress newAddress = billingAddress;
			billingAddress = acService.createBillingAddress(newAddress.getOrganisation(), newAddress.getIdentity());
			billingAddress.setIdentifier(newAddress.getIdentifier());
			billingAddress.setNameLine1(newAddress.getNameLine1());
			billingAddress.setNameLine2(newAddress.getNameLine2());
			billingAddress.setAddressLine1(newAddress.getAddressLine1());
			billingAddress.setAddressLine2(newAddress.getAddressLine2());
			billingAddress.setAddressLine3(newAddress.getAddressLine3());
			billingAddress.setAddressLine4(newAddress.getAddressLine4());
			billingAddress.setPoBox(newAddress.getPoBox());
			billingAddress.setRegion(newAddress.getRegion());
			billingAddress.setZip(newAddress.getZip());
			billingAddress.setCity(newAddress.getCity());
			billingAddress.setCountry(newAddress.getCountry());
			billingAddress = acService.updateBillingAddress(billingAddress);
		}

		order = acService.addBillingAddress(order, billingAddress);
		if (StringHelper.containsNonWhitespace(bookingContext.getPurchaseOrderNumber())) {
			order.setPurchaseOrderNumber(bookingContext.getPurchaseOrderNumber());
		}
		if (StringHelper.containsNonWhitespace(bookingContext.getComment())) {
			order.setComment(bookingContext.getComment());
		}
		return acService.updateOrder(order);
	}

}
