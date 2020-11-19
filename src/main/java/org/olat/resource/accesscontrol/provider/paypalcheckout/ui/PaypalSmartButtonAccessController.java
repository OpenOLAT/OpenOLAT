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
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSFormItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Price;
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
public class PaypalSmartButtonAccessController extends FormBasicController implements FormController {
	
	private OfferAccess link;
	
	@Autowired
	private PaypalCheckoutModule paypalModule;
	
	public PaypalSmartButtonAccessController(UserRequest ureq, WindowControl wControl, OfferAccess link) {
		super(ureq, wControl, "paypal_smart_buttons");
		this.link = link;
		initForm(ureq);
	}
	
	public PaypalSmartButtonAccessController(UserRequest ureq, WindowControl wControl, OfferAccess link, Form form) {
		super(ureq, wControl, LAYOUT_CUSTOM, "paypal_smart_buttons", form);
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
			
			String description = link.getOffer().getDescription();
			if(StringHelper.containsNonWhitespace(description)) {
				description = StringHelper.xssScan(description);
				layoutCont.contextPut("description", description);
			}
			
			Price price = link.getOffer().getPrice();
			String priceStr = PriceFormat.fullFormat(price);
			layoutCont.contextPut("price", priceStr);
			
			String mapperUri = registerMapper(ureq, new PaypalSmartButtonMapper(getIdentity(), link, this));
			layoutCont.contextPut("mapperUri", mapperUri);
		}
		
		JSAndCSSFormItem js = new JSAndCSSFormItem("js", new String[] { "https://www.paypal.com/sdk/js?client-id=" + paypalModule.getClientId() + "&currency=CHF&intent=authorize" });
		formLayout.add(js);
	}
	
	@Override
	protected void fireEvent(UserRequest ureq, Event event) {
		super.fireEvent(ureq, event);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
