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
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.PriceImpl;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutModule;
import org.olat.resource.accesscontrol.ui.AbstractConfigurationMethodController;
import org.olat.resource.accesscontrol.ui.AccessConfigurationController;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PaypalCheckoutAccessConfigurationController extends AbstractConfigurationMethodController {
	
	private TextElement descEl;
	private TextElement priceEl;
	private DateChooser datesEl;
	private SingleSelection currencyEl;
	private MultipleSelectionElement vatEnabledEl;
	
	private static final String[] vatKeys = new String[]{"on"};
	private final String[] vatValues;
	
	private OfferAccess link;
	
	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private PaypalCheckoutModule paypalModule;
	
	public PaypalCheckoutAccessConfigurationController(UserRequest ureq, WindowControl wControl, OfferAccess link, boolean edit) {
		super(ureq, wControl, edit);
		setTranslator(Util.createPackageTranslator(AccessConfigurationController.class, getLocale(), getTranslator()));
		this.link = link;
		vatValues = new String[]{ translate("vat.on") };
		initForm(ureq);
	}
	
	@Override
	public AccessMethod getMethod() {
		return link.getMethod();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String desc = null;
		if(link.getOffer() != null) {
			desc = link.getOffer().getDescription();
		}
		descEl = uifactory.addTextAreaElement("offer.desc", "offer.description", 2000, 6, 80, false, false, desc, formLayout);
		
		Price price = null;
		if(link.getOffer() != null && link.getOffer().getPrice() != null) {
			price = link.getOffer().getPrice();
		}
		
		String amount = null;
		if(price != null && price.getAmount() != null) {
			amount = PriceFormat.format(price.getAmount());
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
		
		vatEnabledEl = uifactory.addCheckboxesHorizontal("vat.enabled", "vat.enabled", formLayout, vatKeys, vatValues);
		if(acModule.isVatEnabled()) {
			vatEnabledEl.select(vatKeys[0], true);
		}
		vatEnabledEl.setEnabled(false);
		
		datesEl = uifactory.addDateChooser("from_" + link.getKey(), "from", link.getValidFrom(), formLayout);
		datesEl.setHelpText(translate("from.hint"));
		datesEl.setSecondDate(true);
		datesEl.setSecondDate(link.getValidTo());
		datesEl.setSeparator("to");

		super.initForm(formLayout, listener, ureq);
	}

	@Override
	public OfferAccess commitChanges() {
		BigDecimal amount = new BigDecimal(priceEl.getValue());
		String currencyCode = currencyEl.getSelectedKey();
		PriceImpl price = new PriceImpl();
		price.setAmount(amount);
		price.setCurrencyCode(currencyCode);
		
		Offer offer = link.getOffer();
		offer.setPrice(price);
		offer.setDescription(descEl.getValue());
		offer.setValidFrom(datesEl.getDate());
		offer.setValidTo(datesEl.getSecondDate());
		link.setValidFrom(datesEl.getDate());
		link.setValidTo(datesEl.getSecondDate());
		return link;
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

		if (datesEl.getDate() != null && datesEl.getSecondDate() != null && datesEl.getDate().compareTo(datesEl.getSecondDate()) > 0) {
			datesEl.setErrorKey("date.error", null);
			allOk = false;
		}

		return allOk;
	}

}
