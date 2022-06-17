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
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutManager;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutStatus;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutTransaction;
import org.olat.resource.accesscontrol.provider.paypalcheckout.model.CheckoutRequest;
import org.olat.resource.accesscontrol.ui.FormController;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PaypalCheckoutSubmitController extends FormBasicController implements FormController {
	
	private OfferAccess link;
	private final String mapperUri;
	
	@Autowired
	private ACService acService;
	@Autowired
	private PaypalCheckoutManager paypalManager;
	
	public PaypalCheckoutSubmitController(UserRequest ureq, WindowControl wControl, OfferAccess link) {
		super(ureq, wControl, "submit");
		this.link = link;

		String businessPath = wControl.getBusinessControl().getAsString() + "[Payment:0]";
		mapperUri = registerMapper(ureq, new PaypalCheckoutMapper(businessPath, paypalManager));
			
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer elementCont = FormLayoutContainer.createVerticalFormLayout("elements", getTranslator());
		elementCont.setRootForm(mainForm);
		formLayout.add(elementCont);
		
		String uuid = (String)ureq.getUserSession().getEntry("paypal-checkout-uuid");
		if(StringHelper.containsNonWhitespace(uuid)) {
			PaypalCheckoutTransaction transaction = paypalManager.loadTransactionByUUID(uuid);
			PaypalCheckoutStatus status = transaction.getStatus();
			if(status == PaypalCheckoutStatus.CANCELED) {
				setFormWarning("paypal.cancelled.transaction");
			} else if(status == PaypalCheckoutStatus.ERROR) {
				setFormWarning("paypal.error.transaction");
			}
		}
		
		Price price = link.getOffer().getPrice();
		String priceStr = PriceFormat.fullFormat(price);
		uifactory.addStaticTextElement("offer.price", priceStr, elementCont);
			
		uifactory.addFormSubmitButton("access.button", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(acService.reserveAccessToResource(getIdentity(), link)) {
			CheckoutRequest response = paypalManager.request(getIdentity(), link, mapperUri, ureq.getHttpReq().getSession().getId());
			if(response == null) {
				setFormWarning("paypal.before.redirect.error");
			} else if (PaypalCheckoutStatus.CREATED.name().equals(response.getStatus())){
				redirectToPaypal(ureq, response);
			} else {
				setFormWarning("paypal.before.redirect.error");
			}
		} else {
			setFormWarning("reservation.failed");
		}
	}
	
	private void redirectToPaypal(UserRequest ureq, CheckoutRequest response) {
		String nextUrl= response.getRedirectToPaypalUrl();
		MediaResource redirect = new RedirectMediaResource(nextUrl);
		ureq.getDispatchResult().setResultingMediaResource(redirect);
	}
}
