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
* Copyright (c) 2008 frentix GmbH, Switzerland<br>
* <p>
*/

package org.olat.resource.accesscontrol.provider.paypal.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
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
import org.olat.resource.accesscontrol.provider.paypal.PaypalAccessHandler;
import org.olat.resource.accesscontrol.provider.paypal.manager.PaypalManager;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalTransaction;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalTransactionStatus;
import org.olat.resource.accesscontrol.ui.FormController;
import org.olat.resource.accesscontrol.ui.PriceFormat;

import com.paypal.svcs.types.ap.PayResponse;
import com.paypal.svcs.types.common.AckCode;

/**
 * 
 * Description:<br>
 * Show the button to pay with Paypal
 * 
 * <P>
 * Initial Date:  15 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PaypalAccessController extends FormBasicController implements FormController {
	
	private final String mapperUri;
	private final OfferAccess link;
	private final ACService acService;
	private final PaypalManager paypalManager;
	
	public PaypalAccessController(UserRequest ureq, WindowControl wControl, OfferAccess link) {
		super(ureq, wControl);

		this.link = link;
		acService = CoreSpringFactory.getImpl(ACService.class);
		paypalManager = CoreSpringFactory.getImpl(PaypalManager.class);
		
		String businessPath = wControl.getBusinessControl().getAsString() + "[Payment:0]";
		mapperUri = registerMapper(ureq, new PaypalMapper(businessPath, paypalManager));
			
		initForm(ureq);
	}
	
	public PaypalAccessController(UserRequest ureq, WindowControl wControl, OfferAccess link, Form form) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, form);
		
		this.link = link;
		acService = CoreSpringFactory.getImpl(ACService.class);
		paypalManager = CoreSpringFactory.getImpl(PaypalManager.class);

		String businessPath = wControl.getBusinessControl().getAsString() + "[Payment:0]";
		mapperUri = registerMapper(ureq, new PaypalMapper(businessPath, paypalManager));
			
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("access.paypal.title");
		setFormDescription("access.paypal.desc");
		setFormTitleIconCss("o_icon o_icon-fw " + PaypalAccessHandler.METHOD_CSS_CLASS + "_icon");
		
		String uuid = (String)ureq.getUserSession().getEntry("paypal-uuid");
		if(StringHelper.containsNonWhitespace(uuid)) {
			PaypalTransaction transaction = paypalManager.loadTransactionByUUID(uuid);
			PaypalTransactionStatus status =transaction.getStatus();
			if(status == PaypalTransactionStatus.CANCELED) {
				setFormWarning("paypal.cancelled.transaction");
			} else if(status == PaypalTransactionStatus.ERROR) {
				setFormWarning("paypal.error.transaction");
			}
		}
		
		String description = link.getOffer().getDescription();
		if(StringHelper.containsNonWhitespace(description)) {
			description = StringHelper.xssScan(description);
			uifactory.addStaticTextElement("offer.description", description, formLayout);
		}
		
		Price price = link.getOffer().getPrice();
		String priceStr = PriceFormat.fullFormat(price);
		uifactory.addStaticTextElement("offer.price", priceStr, formLayout);

		final FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		buttonGroupLayout.setRootForm(mainForm);
		formLayout.add(buttonGroupLayout);
			
		uifactory.addFormSubmitButton("access.button", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(acService.reserveAccessToResource(getIdentity(), link)) {
			PayResponse response = paypalManager.request(getIdentity(), link, mapperUri, ureq.getHttpReq().getSession().getId());
			if(response == null) {
				setFormWarning("paypal.before.redirect.error");
			} else if (response.getResponseEnvelope().getAck().equals(AckCode.SUCCESS)){
				redirectToPaypal(ureq, response);
			} else if (response.getResponseEnvelope().getAck().equals(AckCode.SUCCESSWITHWARNING)){
				redirectToPaypal(ureq, response);
			} else {
				setFormWarning("paypal.before.redirect.error");
			}
		} else {
			setFormWarning("reservation.failed");
		}
	}

	@Override
	public FormItem getInitialFormItem() {
		return flc;
	}
	
	private void redirectToPaypal(UserRequest ureq, PayResponse response) {
		String nextUrl= paypalManager.getPayRedirectUrl(response);
		MediaResource redirect = new RedirectMediaResource(nextUrl);
		ureq.getDispatchResult().setResultingMediaResource(redirect);
	}
}