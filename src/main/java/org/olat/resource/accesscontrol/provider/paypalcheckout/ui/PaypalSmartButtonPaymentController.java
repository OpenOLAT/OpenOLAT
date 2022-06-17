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
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutManager;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutModule;
import org.olat.resource.accesscontrol.ui.FormController;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PaypalSmartButtonPaymentController extends FormBasicController implements FormController {
	
	private OfferAccess link;
	
	@Autowired
	private PaypalCheckoutModule paypalModule;
	@Autowired
	private PaypalCheckoutManager paypalCheckoutManager;
	
	public PaypalSmartButtonPaymentController(UserRequest ureq, WindowControl wControl, OfferAccess link) {
		super(ureq, wControl, "paypal_smart_buttons");
		this.link = link;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String appId = paypalModule.getClientId();
			layoutCont.contextPut("clientId", appId);
			String currency = link.getOffer().getPrice().getCurrencyCode();
			layoutCont.contextPut("currency", currency);
			String excludeFundings = paypalModule.getExcludeFundings();
			layoutCont.contextPut("excludeFundings", excludeFundings == null ? "" : excludeFundings);
			layoutCont.contextPut("csrfToken", ureq.getUserSession().getCsrfToken());
			
			String preferedLocale = paypalCheckoutManager.getPreferredLocale(getLocale());
			layoutCont.contextPut("plocale", preferedLocale);
			
			String description = link.getOffer().getDescription();
			if(StringHelper.containsNonWhitespace(description)) {
				if(!StringHelper.isHtml(description)) {
					description = Formatter.escWithBR(description).toString();
				}
				description = StringHelper.xssScan(description);
				layoutCont.contextPut("description", description);
			}
			
			Price price = link.getOffer().getPrice();
			String priceStr = PriceFormat.fullFormat(price);
			layoutCont.contextPut("price", priceStr);
			
			String mapperUri = registerMapper(ureq, new PaypalSmartButtonMapper(getIdentity(), link, this));
			layoutCont.contextPut("mapperUri", mapperUri);
		}
	}
	
	@Override
	protected void fireEvent(UserRequest ureq, Event event) {
		super.fireEvent(ureq, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
