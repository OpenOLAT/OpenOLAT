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
import java.math.RoundingMode;
import java.util.Collection;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.CatalogInfo;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.model.PriceImpl;
import org.olat.resource.accesscontrol.provider.paypal.PaypalModule;
import org.olat.resource.accesscontrol.ui.AbstractConfigurationMethodController;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.springframework.beans.factory.annotation.Autowired;

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
	
	@Autowired
	private PaypalModule paypalModule;
	@Autowired
	private AccessControlModule acModule;
	
	public PaypalAccessConfigurationController(UserRequest ureq, WindowControl wControl, OfferAccess link,
			boolean offerOrganisationsSupported, Collection<Organisation> offerOrganisations, CatalogInfo catalogInfo,
			boolean edit) {
		super(ureq, wControl, link, offerOrganisationsSupported, offerOrganisations, catalogInfo, edit);
		vatValues = new String[]{ translate("vat.on") };
		initForm(ureq);
	}

	@Override
	protected void initCustomFormElements(FormItemContainer formLayout) {
		Price price = null;
		if(link.getOffer() != null && link.getOffer().getPrice() != null) {
			price = link.getOffer().getPrice();
		}
		
		String amount = null;
		if(price != null && price.getAmount() != null) {
			amount = price.getAmount().setScale(2, RoundingMode.HALF_EVEN).toString();
		}
		priceEl = uifactory.addTextElement("price", "price", 32, amount, formLayout);

		currencyEl = uifactory.addDropdownSingleselect("currency", "currency", formLayout, currencies, currencies, null);
		
		boolean selected = false;
		if(price != null && price.getCurrencyCode() != null) {
			for(String currency:currencies) {
				if(currency.equals(price.getCurrencyCode())) {
					currencyEl.select(currency, true);
					selected = true;
				}
			}
		}
		if(!selected) {
			if(StringHelper.containsNonWhitespace(paypalModule.getPaypalCurrency())) {
				currencyEl.select(paypalModule.getPaypalCurrency(), true);
				currencyEl.setEnabled(false);
			} else {
				currencyEl.select("CHF", true);
			}
		}
		
		vatEnabledEl = uifactory.addCheckboxesHorizontal("vat.enabled", "vat.enabled", formLayout, vatKeys, vatValues);
		if(acModule.isVatEnabled()) {
			vatEnabledEl.select(vatKeys[0], true);
		}
		vatEnabledEl.setEnabled(false);
	}

	@Override
	public void updateCustomChanges() {
		BigDecimal amount = new BigDecimal(priceEl.getValue());
		String currencyCode = currencyEl.getSelectedKey();
		PriceImpl price = new PriceImpl();
		price.setAmount(amount);
		price.setCurrencyCode(currencyCode);
		
		link.getOffer().setPrice(price);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		String priceStr = priceEl.getValue();
		Double priceDbl = 0d;
		
		priceEl.clearError();
		try {
			priceDbl = Double.parseDouble(priceStr);
			if(priceDbl <= 0.0d) {
				priceEl.setErrorKey("price.error", null);
				allOk = false;
			} 			
		} catch(Exception e) {
			priceEl.setErrorKey("price.error", null);
			allOk = false;
		}
		
		try {
			PriceFormat.format(BigDecimal.valueOf(priceDbl));
		} catch (Exception e) {
			priceEl.setErrorKey("price.error", null);
			allOk = false;
		}

		currencyEl.clearError();
		if(!currencyEl.isOneSelected()) {
			currencyEl.setErrorKey("currency.error", null);
			allOk = false;
		}

		return allOk;
	}
}
