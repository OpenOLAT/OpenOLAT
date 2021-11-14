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
package org.olat.resource.accesscontrol.provider.paypalcheckout.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutManager;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutTransaction;
import org.olat.resource.accesscontrol.ui.FormController;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PaypalCheckoutTransactionDetailsController extends FormBasicController implements FormController {
	
	private final Order order;
	private final PaypalCheckoutTransaction transaction;

	@Autowired
	private UserManager userManager;
	@Autowired
	private PaypalCheckoutManager  paypalManager;
	
	public PaypalCheckoutTransactionDetailsController(UserRequest ureq, WindowControl wControl, PaypalCheckoutTransaction transaction) {
		super(ureq, wControl);
		this.transaction = transaction;
		order = paypalManager.getOrder(transaction);
		initForm(ureq);
	}
	
	public PaypalCheckoutTransactionDetailsController(UserRequest ureq, WindowControl wControl, PaypalCheckoutTransaction transaction, Form form) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, form);
		this.transaction = transaction;
		order = paypalManager.getOrder(transaction);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(order != null) {
			Identity delivery = order.getDelivery();
			String fullName = userManager.getUserDisplayName(delivery);
			uifactory.addStaticTextElement("delivery", "delivery", fullName, formLayout);
		}

		Price securePrice = transaction.getSecurePrice();
		String securePriceStr = PriceFormat.fullFormat(securePrice);
		uifactory.addStaticTextElement("amount", "paypal.transaction.amount", securePriceStr, formLayout);
		uifactory.addStaticTextElement("oo.order.nr", "oo.order.nr", transaction.getOrderNr(), formLayout);
		uifactory.addStaticTextElement("order.id", "paypal.order.id", transaction.getPaypalOrderId(), formLayout);
		uifactory.addStaticTextElement("order.status", "paypal.order.status", transaction.getPaypalOrderStatus(), formLayout);
		if(StringHelper.containsNonWhitespace(transaction.getPaypalOrderStatusReason())) {
			uifactory.addStaticTextElement("order.status.reason", "paypal.order.status.reason", transaction.getPaypalOrderStatusReason(), formLayout);
		}
		uifactory.addStaticTextElement("order.capture.id", "paypal.capture.id", transaction.getPaypalCaptureId(), formLayout);
		uifactory.addStaticTextElement("order.invoice.id", "paypal.invoice.id", transaction.getPaypalInvoiceId(), formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
