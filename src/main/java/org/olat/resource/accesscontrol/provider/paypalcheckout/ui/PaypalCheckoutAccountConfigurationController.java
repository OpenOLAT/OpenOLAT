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

import java.text.Collator;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nModule;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The account settings, it needs to be a Paypal Business Account.
 * 
 * Initial date: 25 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PaypalCheckoutAccountConfigurationController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	private static final String[] smartButtonsKeys = new String[] { "smartbuttons", "standard" };
	
	private TextElement clientIdEl;
	private TextElement clientSecretEl;
	private SingleSelection currencyEl;
	private SingleSelection smartButtonsEl;
	private MultipleSelectionElement enableEl;
	private MultipleSelectionElement pendingReviewEl;
	private MultipleSelectionElement preferredCountriesEl;
	
	private final List<String> paypalCurrencies;

	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private PaypalCheckoutModule paypalModule;
	
	public PaypalCheckoutAccountConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		paypalCurrencies = paypalModule.getPaypalCurrencies();
		initForm(ureq);
		updateUI();
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("checkout.config.title");
		setFormDescription("checkout.config.description");
		
		String[] onValues = new String[] { translate("on") };
		enableEl = uifactory.addCheckboxesHorizontal("enable", "paypal.enable", formLayout, onKeys, onValues);
		enableEl.select(onKeys[0], acModule.isPaypalCheckoutEnabled());
		enableEl.addActionListener(FormEvent.ONCHANGE);
		
		SelectionValues smartButtons = new SelectionValues();
		smartButtons.add(SelectionValues.entry(smartButtonsKeys[0], translate("checkout.smart.buttons.enabled")));
		smartButtons.add(SelectionValues.entry(smartButtonsKeys[1], translate("checkout.standard")));
		smartButtonsEl = uifactory.addRadiosVertical("checkout.smart.buttons", "checkout.smart.buttons", formLayout, smartButtons.keys(), smartButtons.values());
		if(paypalModule.isSmartButtons()) {
			smartButtonsEl.select(smartButtonsKeys[0], true);
		} else {
			smartButtonsEl.select(smartButtonsKeys[1], true);
		}

		String[] onPendingValues = new String[] { translate("paypal.pending.review.accept") };
		pendingReviewEl = uifactory.addCheckboxesHorizontal("pending.review", "paypal.pending.review", formLayout, onKeys, onPendingValues);
		pendingReviewEl.setExampleKey("paypal.pending.review.accept.explain", null);
		pendingReviewEl.select(onKeys[0], paypalModule.isAcceptPendingReview());
		pendingReviewEl.addActionListener(FormEvent.ONCHANGE);
		
		SelectionValues currencies = new SelectionValues();
		paypalCurrencies.forEach(currency -> currencies.add(SelectionValues.entry(currency, currency)));
		currencyEl = uifactory.addDropdownSingleselect("currency", "currency", formLayout, currencies.keys(), currencies.values(), null);
		if(StringHelper.containsNonWhitespace(paypalModule.getPaypalCurrency())) {
			currencyEl.select(paypalModule.getPaypalCurrency(), true);
		} else {
			currencyEl.select("CHF", true);
		}
		
		SelectionValues countries = getCountries();
		preferredCountriesEl = uifactory.addCheckboxesDropdown("countries", "paypal.preferred.countries", formLayout,
				countries.keys(), countries.values());
		preferredCountriesEl.setHelpTextKey("paypal.preferred.countries.hint", null);
		List<String> preferredCountries = paypalModule.getPreferredCountriesList();
		for(String country:preferredCountries) {
			if(countries.containsKey(country)) {
				preferredCountriesEl.select(country, true);
			}
		}
		
		String clientId = paypalModule.getClientId();
		clientIdEl = uifactory.addTextElement("checkout.client.id", 128, clientId, formLayout);
		String clientSecret = paypalModule.getClientSecret();
		clientSecretEl = uifactory.addTextElement("checkout.client.secret", 128, clientSecret, formLayout);

		final FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		uifactory.addFormSubmitButton("save", buttonGroupLayout);
	}
	
	private SelectionValues getCountries() {
		SelectionValues keyValues = new SelectionValues();
		Collection<Locale> languages = i18nModule.getAllLocales().values();
		Locale[] locales = Locale.getAvailableLocales();
		for(Locale language:languages) {
			for(Locale locale:locales) {
				if(language.getLanguage().equals(locale.getLanguage())) {
					String country = locale.getCountry();
					String displayCountry = locale.getDisplayCountry(getLocale());
					if(!keyValues.containsKey(country) && StringHelper.containsNonWhitespace(displayCountry)) {
						keyValues.add(SelectionValues.entry(country, displayCountry));
					}
				}
			}
		}
		
		final Collator collator = Collator.getInstance(getLocale());
		keyValues.sort((k1, k2) -> collator.compare(k1.getValue(), k2.getValue()));
		return keyValues;
	}
	
	private void updateUI() {
		boolean enabled = enableEl.isAtLeastSelected(1);
		currencyEl.setVisible(enabled);
		clientIdEl.setVisible(enabled);
		clientSecretEl.setVisible(enabled);
		smartButtonsEl.setVisible(enabled);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		currencyEl.clearError();
		if(enableEl.isAtLeastSelected(1) && !currencyEl.isOneSelected()) {
			currencyEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		allOk &= validateId(clientIdEl);
		allOk &= validateId(clientSecretEl);
		return allOk;
	}
	
	private boolean validateId(TextElement element) {
		boolean allOk = true;
		
		element.clearError();
		if(enableEl.isAtLeastSelected(1)
				&& !StringHelper.containsNonWhitespace(element.getValue())) {
			element.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == enableEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private String getPreferredCountries() {
		StringBuilder sb = new StringBuilder();
		for(String country:preferredCountriesEl.getSelectedKeys()) {
			if(sb.length() > 0) sb.append(",");
			sb.append(country);
		}
		return sb.toString();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = enableEl.isAtLeastSelected(1);
		acModule.setPaypalCheckoutEnabled(enabled);
		if(enabled) {
			paypalModule.setClientId(clientIdEl.getValue());
			paypalModule.setClientSecret(clientSecretEl.getValue());
			if(currencyEl.isOneSelected() && paypalCurrencies.contains(currencyEl.getSelectedKey())) {
				paypalModule.setPaypalCurrency(currencyEl.getSelectedKey());
			}
			paypalModule.setSmartButtons(smartButtonsEl.isOneSelected() && smartButtonsEl.isSelected(0));
			paypalModule.setAcceptPendingReview(pendingReviewEl.isAtLeastSelected(1));
			paypalModule.setPreferredCountries(getPreferredCountries());
			doUpdateWebhook();
		} else {
			paypalModule.setClientId(null);
			paypalModule.setClientSecret(null);
		}
		showInfo("saved");
	}
	
	private void doUpdateWebhook() {
		try {
			paypalModule.updateWebhook();
		} catch (Exception e) {
			logError("", e);
		}
	}
}