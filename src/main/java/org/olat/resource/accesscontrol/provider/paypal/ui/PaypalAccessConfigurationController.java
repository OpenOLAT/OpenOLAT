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

import java.math.BigDecimal;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.OfferAccess;
import org.olat.resource.accesscontrol.model.PriceImpl;
import org.olat.resource.accesscontrol.provider.paypal.PaypalModule;
import org.olat.resource.accesscontrol.ui.AbstractConfigurationMethodController;

/**
 * 
 * Description:<br>
 * Configuration for a paypal payment
 * 
 * <P>
 * Initial Date:  15 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PaypalAccessConfigurationController extends AbstractConfigurationMethodController {
	
	private final OfferAccess link;
	private final PaypalModule paypalModule;
	private final AccessControlModule acModule;

	private TextElement descEl;
	private TextElement priceEl;
	private SingleSelection currencyEl;
	private MultipleSelectionElement vatEnabledEl;
	
	private static final String[] vatKeys = new String[]{"on"};
	private final String[] vatValues;
	
	
	private static final String[] currencies = new String[] {
		"AUD",
		"CAD",
		"CZK",
		"DKK",
		"EUR",
		"HKD",
		"HUF",
		"ILS",
		"JPY",
		"MXN",
		"NOK",
		"NZD",
		"PHP",
		"PLN",
		"GBP",
		"SGD",
		"SEK",
		"CHF",
		"TWD",
		"THB",
		"TRY",
		"USD"
	};
	
	public PaypalAccessConfigurationController(UserRequest ureq, WindowControl wControl, OfferAccess link) {
		super(ureq, wControl);
		this.link = link;
		acModule = CoreSpringFactory.getImpl(AccessControlModule.class);
		paypalModule = CoreSpringFactory.getImpl(PaypalModule.class);
		vatValues = new String[]{ translate("vat.on") };
		initForm(ureq);
	}

	public PaypalAccessConfigurationController(UserRequest ureq, WindowControl wControl, OfferAccess link, Form form) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, form);
		this.link = link;
		acModule = CoreSpringFactory.getImpl(AccessControlModule.class);
		paypalModule = CoreSpringFactory.getImpl(PaypalModule.class);
		vatValues = new String[]{ translate("vat.on") };
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		descEl = uifactory.addTextAreaElement("offer-desc", "offer.description", 2000, 6, 80, false, null, formLayout);
		
		priceEl = uifactory.addTextElement("price", "price", 32, "", formLayout);

		currencyEl = uifactory.addDropdownSingleselect("currency", "currency", formLayout, currencies, currencies, null);
		if(StringHelper.containsNonWhitespace(paypalModule.getPaypalCurrency())) {
			currencyEl.select(paypalModule.getPaypalCurrency(), true);
			currencyEl.setEnabled(false);
		} else {
			currencyEl.select("CHF", true);
		}
		
		vatEnabledEl = uifactory.addCheckboxesHorizontal("vat.enabled", "vat.enabled", formLayout, vatKeys, vatValues, null);
		if(acModule.isVatEnabled()) {
			vatEnabledEl.select(vatKeys[0], true);
		}
		vatEnabledEl.setEnabled(false);
		
		super.initForm(formLayout, listener, ureq);
	}
	
	@Override
	public AccessMethod getMethod() {
		return link.getMethod();
	}

	@Override
	public OfferAccess commitChanges() {
		BigDecimal amount = new BigDecimal(priceEl.getValue());
		String currencyCode = currencyEl.getSelectedKey();
		PriceImpl price = new PriceImpl();
		price.setAmount(amount);
		price.setCurrencyCode(currencyCode);
		link.getOffer().setPrice(price);
		link.getOffer().setDescription(descEl.getValue());
		return link;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		String priceStr = priceEl.getValue();
		priceEl.clearError();
		try {
			double priceDbl = Double.parseDouble(priceStr);
			if(priceDbl <= 0.0d) {
				priceEl.setErrorKey("price.error", null);
				allOk = false;
			}
		} catch(Exception e) {
			priceEl.setErrorKey("price.error", null);
			allOk = false;
		}

		currencyEl.clearError();
		if(!currencyEl.isOneSelected()) {
			currencyEl.setErrorKey("currency.error", null);
			allOk = false;
		}

		return allOk && super.validateFormLogic(ureq);
	}
}
