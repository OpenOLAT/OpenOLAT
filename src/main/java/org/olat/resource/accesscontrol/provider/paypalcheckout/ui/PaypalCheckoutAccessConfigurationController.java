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

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.CatalogInfo;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.model.PriceImpl;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutModule;
import org.olat.resource.accesscontrol.ui.AbstractConfigurationMethodController;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PaypalCheckoutAccessConfigurationController extends AbstractConfigurationMethodController {
	
	private TextElement priceEl;
	private SingleSelection currencyEl;
	
	@Autowired
	private PaypalCheckoutModule paypalModule;
	
	public PaypalCheckoutAccessConfigurationController(UserRequest ureq, WindowControl wControl, OfferAccess link,
			boolean offerOrganisationsSupported, Collection<Organisation> offerOrganisations,
			boolean confirmationByManagerSupported, CatalogInfo catalogInfo, boolean edit) {
		super(ureq, wControl, link, offerOrganisationsSupported, offerOrganisations, confirmationByManagerSupported, catalogInfo, edit);
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
			amount = PriceFormat.formatMoneyForTextInput(price.getAmount());
		}
		priceEl = uifactory.addTextElement("price", "price", 32, amount, formLayout);
		
		SelectionValues currencies = new SelectionValues();
		List<String> paypalCurrencies = paypalModule.getPaypalCurrencies();
		paypalCurrencies.forEach(currency -> currencies.add(SelectionValues.entry(currency, currency)));
		currencyEl = uifactory.addDropdownSingleselect("currency", "currency", formLayout, currencies.keys(), currencies.values(), null);
		
		boolean selected = false;
		if(price != null && price.getCurrencyCode() != null) {
			for(String currency:paypalCurrencies) {
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
		
		priceEl.clearError();
		if (!PriceFormat.validateMoney(priceEl.getValue())) {
			priceEl.setErrorKey("price.error");
			allOk = false;
		}

		currencyEl.clearError();
		if(!currencyEl.isOneSelected()) {
			currencyEl.setErrorKey("currency.error");
			allOk = false;
		}

		return allOk;
	}

}
