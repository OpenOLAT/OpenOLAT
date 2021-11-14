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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.resource.accesscontrol.provider.paypal.ui;

import java.math.BigDecimal;
import java.net.UnknownHostException;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.provider.paypal.PaypalModule;
import org.olat.resource.accesscontrol.provider.paypal.manager.PaypalManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Set the account settings
 * 
 * <P>
 * Initial Date:  26 mai 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PaypalMasterAccountController extends FormBasicController {
	
	private TextElement usernameEl;
	private TextElement passwordEl;
	private TextElement signatureEl;
	private TextElement applicationIdEl;
	private TextElement firstReceiverEl;
	private TextElement deviceIpEl;
	private TextElement vatNumberEl;
	private TextElement vatRateEl;
	private MultipleSelectionElement vatEnabledEl;
	private SingleSelection currencyEl;
	
	private FormLink checkButton;
	
	@Autowired
	private PaypalModule paypalModule;
	@Autowired
	private PaypalManager paypalManager;
	@Autowired
	private AccessControlModule acModule;
	
	private static final String[] vatKeys = new String[]{"on"};
	private final String[] vatValues;
	
	private static final String[] currencies = new String[] {
		"",
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
	
	public PaypalMasterAccountController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		vatValues = new String[]{ translate("vat.on") };
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("paypal.config.title");
		setFormWarning("paypal.config.deprecated");

		if(acModule.isPaypalEnabled()) {
			setFormDescription("paypal.config.description");
			setFormContextHelp("PayPal Configuration");

			currencyEl = uifactory.addDropdownSingleselect("currency", "currency", formLayout, currencies, currencies, null);
			String currency = paypalModule.getPaypalCurrency();
			if(StringHelper.containsNonWhitespace(currency)) {
				currencyEl.select(currency, true);
			} else {
				currencyEl.select("", true);
			}
			
			vatEnabledEl = uifactory.addCheckboxesHorizontal("vat.enabled", "vat.enabled", formLayout, vatKeys, vatValues);
			vatEnabledEl.addActionListener(FormEvent.ONCHANGE);
			if(acModule.isVatEnabled()) {
				vatEnabledEl.select(vatKeys[0], true);
			}
			
			String vatNr = acModule.getVatNumber();
			vatNumberEl = uifactory.addTextElement("vat.nr", "vat.nr", 255, vatNr, formLayout);
			
			BigDecimal vatRate = acModule.getVat();
			String vatRateStr = vatRate == null ? "" : vatRate.toPlainString();
			vatRateEl = uifactory.addTextElement("vat.rate", "vat.rate", 5, vatRateStr, formLayout);
			vatRateEl.setDisplaySize(5);

			uifactory.addSpacerElement("paypal-space", formLayout, false);
			
			String firstReceiver = paypalModule.getPaypalFirstReceiverEmailAddress();
			firstReceiverEl = uifactory.addTextElement("first-receiver", "paypal.config.first.receiver", 255, firstReceiver, formLayout);
			String userId = paypalModule.getPaypalSecurityUserId();
			usernameEl = uifactory.addTextElement("api-username", "paypal.config.username", 255, userId, formLayout);
			passwordEl = uifactory.addPasswordElement("api-password", "paypal.config.password", 255, "", formLayout);
			passwordEl.setExampleKey("paypal.config.password.expl", null);
			passwordEl.setAutocomplete("new-password");
			String signature = paypalModule.getPaypalSecuritySignature();
			signatureEl = uifactory.addTextElement("api-signature", "paypal.config.signature", 255, signature, formLayout);
			String applicationId = paypalModule.getPaypalApplicationId();
			
			uifactory.addSpacerElement("paypal-space2", formLayout, false);

			applicationIdEl = uifactory.addTextElement("application-id", "paypal.config.application.id", 255, applicationId, formLayout);
			try {
				deviceIpEl = uifactory.addTextElement("device-ip", "paypal.config.device.ip", 255, "", formLayout);
				String deviceIp = paypalModule.getDeviceIpAddress();
				deviceIpEl.setValue(deviceIp);
			} catch (UnknownHostException e) {
				logError("", e);
			}
			
			
			final FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
			formLayout.add(buttonGroupLayout);
			
			checkButton = uifactory.addFormLink("paypal.check", buttonGroupLayout, Link.BUTTON);
			uifactory.addFormSubmitButton("save", buttonGroupLayout);

		} else {
			String fxSupport = "contact@frentix.com";
			setFormWarning("paypal.config.disabled.warning", new String[]{fxSupport});
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean vatEnabled = vatEnabledEl.isMultiselect() && vatEnabledEl.isSelected(0);
		acModule.setVatEnabled(vatEnabled);
		
		String vatNr = vatNumberEl.getValue();
		acModule.setVatNumber(vatNr);
		
		String vatRate = vatRateEl.getValue();
		if(StringHelper.containsNonWhitespace(vatRate)) {
			try {
				acModule.setVat(new BigDecimal(vatRate));
			} catch (Exception e) {
				//error
				vatRateEl.setErrorKey("", null);
			}
		} else {
			acModule.setVat(BigDecimal.ZERO);
		}
		
		String currency = currencyEl.isOneSelected() ? currencyEl.getSelectedKey() : "";
		paypalModule.setPaypalCurrency(currency);
		
		String userId = usernameEl.getValue();
		if(StringHelper.containsNonWhitespace(userId)) {
			paypalModule.setPaypalSecurityUserId(userId);
		}
		String password = passwordEl.getValue();
		if(StringHelper.containsNonWhitespace(password)) {
			paypalModule.setPaypalSecurityPassword(password);
		}
		String signature = signatureEl.getValue();
		if(StringHelper.containsNonWhitespace(signature)) {
			paypalModule.setPaypalSecuritySignature(signature);
		}
		String applicationId = applicationIdEl.getValue();
		if(StringHelper.containsNonWhitespace(applicationId)) {
			paypalModule.setPaypalApplicationId(applicationId);
		}
		String deviceIp = deviceIpEl.getValue();
		if(StringHelper.containsNonWhitespace(deviceIp)) {
			paypalModule.setDeviceIpAddress(deviceIp);
		}
		String firstReceiver = firstReceiverEl.getValue();
		if(StringHelper.containsNonWhitespace(firstReceiver)) {
			paypalModule.setPaypalFirstReceiverEmailAddress(firstReceiver);
		}

		showInfo("paypal.config.saved");
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == checkButton) {
			checkCredentials();
		} else if (source == vatEnabledEl) {
			if (vatEnabledEl.isSelected(0)) {
				vatNumberEl.setEnabled(true);
				vatRateEl.setEnabled(true);
			} else {
				vatNumberEl.setEnabled(false);
				vatRateEl.setEnabled(false);				
				vatRateEl.setValue(null);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void checkCredentials() {
		if(paypalManager.convertCurrency()) {
			showInfo("paypal.config.success");
		} else {
			showError("paypal.config.error");
		}
	}
}