/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.resource.accesscontrol.provider.invoice.ui;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.CatalogInfo;
import org.olat.resource.accesscontrol.CostCenter;
import org.olat.resource.accesscontrol.CostCenterSearchParams;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.model.PriceImpl;
import org.olat.resource.accesscontrol.provider.invoice.InvoiceModule;
import org.olat.resource.accesscontrol.ui.AbstractConfigurationMethodController;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Objects;

import io.jsonwebtoken.lang.Arrays;

/**
 * 
 * Initial date: 5 Nov 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class InvoiceAccessConfigurationController extends AbstractConfigurationMethodController {

	private SingleSelection currencyEl;
	private TextElement priceAmountEl;
	private FormToggle cancellingEnabledEl;
	private TextElement cancellingFeeAmountEl;
	private FormLayoutContainer cancellingFeeFreeCont;
	private TextElement cancellingFeeFreeEl;
	private SingleSelection costCenterEl;
	
	private String currencyCode = null;
	
	@Autowired
	private InvoiceModule invoiceModule;

	public InvoiceAccessConfigurationController(UserRequest ureq, WindowControl wControl, OfferAccess link,
			boolean offerOrganisationsSupported, Collection<Organisation> offerOrganisations, CatalogInfo catalogInfo,
			boolean edit) {
		super(ureq, wControl, link, offerOrganisationsSupported, offerOrganisations, catalogInfo, edit);
		initForm(ureq);
	}
	
	@Override
	protected boolean isConfirmationByManagerSupported() {
		return true;
	}

	@Override
	protected void initCustomFormElements(FormItemContainer formLayout) {
		Offer offer = link.getOffer();
		if (acService.hasOrder(offer)) {
			setFormWarning("error.offers.exists");
		}
		
		String priceAmount = null;
		String cancellingFeeDeadlineDays = null;
		String cancellingFeeAmount = null;
		
		if (offer != null && offer.getKey() != null) {
			if (offer.getPrice() != null) {
				if (offer.getPrice().getAmount() != null) {
					priceAmount = PriceFormat.format(offer.getPrice().getAmount());
				}
				if (offer.getPrice().getCurrencyCode() != null) {
					currencyCode = offer.getPrice().getCurrencyCode();
				} else {
					currencyCode = invoiceModule.getCurrencyDefault();
				}
			}
			
			if (offer.getCancellingFee() != null && offer.getCancellingFee().getAmount() != null) {
				cancellingFeeAmount = PriceFormat.format(offer.getCancellingFee().getAmount());
			}
			
			if (offer.getCancellingFeeDeadlineDays() != null) {
				cancellingFeeDeadlineDays = offer.getCancellingFeeDeadlineDays().toString();
			}
			
		} else {
			currencyCode = invoiceModule.getCurrencyDefault();
			if (StringHelper.containsNonWhitespace(currencyCode)) {
				BigDecimal defaultCancellingFee = invoiceModule.getCancellingFeeDefaults().get(currencyCode);
				if (defaultCancellingFee != null) {
					cancellingFeeAmount = PriceFormat.format(defaultCancellingFee);
				}
			}
			if (invoiceModule.getCancellingFeeDeadlineDaysDefault() != null) {
				cancellingFeeDeadlineDays = invoiceModule.getCancellingFeeDeadlineDaysDefault().toString();
			}
		}
		
		SelectionValues currenciesSV = new SelectionValues();
		List<String> currencies = invoiceModule.getCurrencies();
		currencies.forEach(currency -> currenciesSV.add(SelectionValues.entry(currency, currency)));
		currencyEl = uifactory.addDropdownSingleselect("currency", formLayout, currenciesSV.keys(), currenciesSV.values(), null);
		currencyEl.addActionListener(FormEvent.ONCHANGE);
		if (Arrays.asList(currencyEl.getKeys()).contains(currencyCode)) {
			currencyEl.select(currencyCode, true);
		} else {
			currencyEl.select(currencyEl.getKey(0), true);
		}
		
		priceAmountEl = uifactory.addTextElement("price", 32, priceAmount, formLayout);
		priceAmountEl.setMandatory(true);
		
		cancellingEnabledEl = uifactory.addToggleButton("cancelling.fee.enabled", "cancelling.fee.enabled",
				translate("on"), translate("off"), formLayout);
		cancellingEnabledEl.toggle(StringHelper.containsNonWhitespace(cancellingFeeAmount));
		cancellingEnabledEl.addActionListener(FormEvent.ONCHANGE);
		
		cancellingFeeAmountEl = uifactory.addTextElement("cancelling.fee", 32, cancellingFeeAmount, formLayout);
		cancellingFeeAmountEl.setMandatory(true);
		
		cancellingFeeFreeCont = FormLayoutContainer.createCustomFormLayout("freeCont", getTranslator(), velocity_root + "/cancelling_fee_free.html");
		cancellingFeeFreeCont.setLabel("cancelling.fee.free", null);
		cancellingFeeFreeCont.setRootForm(mainForm);
		if (!catalogInfo.isStartDateAvailable()) {
			cancellingFeeFreeCont.setWarningKey("cancelling.fee.free.start.missing");
		}
		formLayout.add(cancellingFeeFreeCont);
		
		cancellingFeeFreeEl = uifactory.addTextElement("cancelling.fee.free", 8, cancellingFeeDeadlineDays, cancellingFeeFreeCont);
		cancellingFeeFreeEl.setDisplaySize(8);
		updateCancellingUI();
		
		SelectionValues costCenterSV = new SelectionValues();
		CostCenterSearchParams costCenterSearchParams = new CostCenterSearchParams();
		costCenterSearchParams.setEnabled(Boolean.TRUE);
		acService.getCostCenters(costCenterSearchParams).forEach(costCenter -> costCenterSV.add(SelectionValues.entry(
				costCenter.getKey().toString(),
				costCenter.getName()
			)));
		if (offer != null && offer.getCostCenter() != null) {
			if (!costCenterSV.containsKey(offer.getCostCenter().getKey().toString())) {
				 costCenterSV.add(SelectionValues.entry(
						offer.getCostCenter().getKey().toString(),
						translate("cost.center.deactivated", offer.getCostCenter().getName())
					));
			}
		}
		
		if (!costCenterSV.isEmpty()) {
			costCenterEl = uifactory.addDropdownSingleselect("cost.center", formLayout, costCenterSV.keys(), costCenterSV.values(), null);
			costCenterEl.enableNoneSelection(translate("cost.center.none"));
			if (offer != null && offer.getCostCenter() != null) {
				costCenterEl.select(offer.getCostCenter().getKey().toString(), true);
			}
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == currencyEl) {
			updateCancellingFeeAmount();
		} else if (source == cancellingEnabledEl) {
			updateCancellingUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void updateCancellingFeeAmount() {
		if (!currencyEl.isOneSelected()) {
			return;
		}
		
		String selectedCurreny = currencyEl.getSelectedKey();
		if (selectedCurreny.equals(currencyCode)) {
			return;
		}
		
		BigDecimal previousFeeDefault = invoiceModule.getCancellingFeeDefaults().get(currencyCode);
		String perviousFeeValue = "";
		if (previousFeeDefault != null) {
			perviousFeeValue = PriceFormat.format(previousFeeDefault);
		}
		
		String currentFeeValue = cancellingFeeAmountEl.getValue();
		if (Objects.equal(perviousFeeValue, currentFeeValue)) {
			String newFeeValue = null;
			BigDecimal currentFeeDefault = invoiceModule.getCancellingFeeDefaults().get(selectedCurreny);
			if (currentFeeDefault != null) {
				newFeeValue = PriceFormat.format(currentFeeDefault);
			}
			cancellingFeeAmountEl.setValue(newFeeValue);
			cancellingEnabledEl.toggle(currentFeeDefault != null);
			updateCancellingUI();
		}
		
		currencyCode = selectedCurreny;
	}
	
	private void updateCancellingUI() {
		boolean cancellingFeeEnabled = cancellingEnabledEl.isOn();
		cancellingFeeAmountEl.setVisible(cancellingFeeEnabled);
		cancellingFeeFreeCont.setVisible(cancellingFeeEnabled);
		cancellingFeeFreeEl.setVisible(cancellingFeeEnabled);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		currencyEl.clearError();
		if (!currencyEl.isOneSelected()) {
			currencyEl.setErrorKey("form.legende.mandatory");
			allOk = false;
		}
		
		priceAmountEl.clearError();
		if (StringHelper.containsNonWhitespace(priceAmountEl.getValue())) {
			try {
				if (Double.valueOf(priceAmountEl.getValue()) < 0) {
					priceAmountEl.setErrorKey("form.error.nofloat");
					allOk &= false;
				}
			} catch (Exception e) {
				priceAmountEl.setErrorKey("form.error.nofloat");
				allOk &= false;
			}
		} else {
			priceAmountEl.setErrorKey("form.legende.mandatory");
			allOk = false;
		}
		
		cancellingFeeAmountEl.clearError();
		if (cancellingFeeAmountEl.isVisible()) {
			if (StringHelper.containsNonWhitespace(cancellingFeeAmountEl.getValue())) {
				try {
					if (Double.valueOf(cancellingFeeAmountEl.getValue()) < 0) {
						cancellingFeeAmountEl.setErrorKey("form.error.nofloat");
						allOk &= false;
					}
				} catch (Exception e) {
					cancellingFeeAmountEl.setErrorKey("form.error.nofloat");
					allOk &= false;
				}
			} else {
				cancellingFeeAmountEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}
		
		cancellingFeeFreeCont.clearError();
		if (cancellingFeeFreeEl.isVisible()
				&& StringHelper.containsNonWhitespace(cancellingFeeFreeEl.getValue())
				&& (!StringHelper.isLong(cancellingFeeFreeEl.getValue()) || Long.parseLong(cancellingFeeFreeEl.getValue()) < 1)) {
			cancellingFeeFreeCont.setErrorKey("form.error.nointeger");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	public void updateCustomChanges() {
		String currencyCode = currencyEl.getSelectedKey();
		
		BigDecimal priceAmount = new BigDecimal(priceAmountEl.getValue());
		PriceImpl price = new PriceImpl();
		price.setAmount(priceAmount);
		price.setCurrencyCode(currencyCode);
		link.getOffer().setPrice(price);
		
		PriceImpl cancellingFee = null;
		if (cancellingFeeAmountEl.isVisible()) {
			if (StringHelper.containsNonWhitespace(cancellingFeeAmountEl.getValue())) {
				BigDecimal cancellingFeeAmount = new BigDecimal(cancellingFeeAmountEl.getValue());
				cancellingFee = new PriceImpl();
				cancellingFee.setAmount(cancellingFeeAmount);
				cancellingFee.setCurrencyCode(currencyCode);
			}
		}
		link.getOffer().setCancellingFee(cancellingFee);
		
		Integer cancellingFeeDeadlineDays = null;
		if (cancellingFeeFreeEl.isVisible()) {
			if (StringHelper.containsNonWhitespace(cancellingFeeFreeEl.getValue())) {
				cancellingFeeDeadlineDays = Integer.valueOf(cancellingFeeFreeEl.getValue());
			}
		}
		link.getOffer().setCancellingFeeDeadlineDays(cancellingFeeDeadlineDays);
		
		link.getOffer().setCostCenter(null);
		if (costCenterEl != null && costCenterEl.isOneSelected()) {
			CostCenterSearchParams costCenterSearchParams = new CostCenterSearchParams();
			costCenterSearchParams.setCostCenterKeys(List.of(Long.valueOf(costCenterEl.getSelectedKey())));
			List<CostCenter> costCenters = acService.getCostCenters(costCenterSearchParams);
			if (!costCenters.isEmpty()) {
				link.getOffer().setCostCenter(costCenters.get(0));
			}
		}
	}

}
