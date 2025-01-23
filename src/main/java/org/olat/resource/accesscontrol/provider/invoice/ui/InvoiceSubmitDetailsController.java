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

import static org.olat.core.util.ArrayHelper.emptyStrings;

import java.util.List;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.BillingAddress;
import org.olat.resource.accesscontrol.BillingAddressSearchParams;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.ui.AccessEvent;
import org.olat.resource.accesscontrol.ui.BillingAddressController;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 Nov 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class InvoiceSubmitDetailsController extends FormBasicController {
	
	private SingleSelection billingAddressEl;
	private TextElement purchseNumberEl;
	private FormLink billingAddressLink;
	private TextAreaElement commentEl;
	
	private CloseableModalController cmc;
	private BillingAddressController editCtrl;

	private final OfferAccess link;
	private final Identity identity;
	private BillingAddress billingAddress;
	
	@Autowired
	private ACService acService;

	protected InvoiceSubmitDetailsController(UserRequest ureq, WindowControl wControl, OfferAccess link, Identity identity) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(BillingAddressController.class, getLocale(), getTranslator()));
		this.link = link;
		this.identity = identity;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Offer offer = link.getOffer();
		
		if (offer.getCancellingFee() != null) {
			String cancellingFee = PriceFormat.fullFormat(offer.getCancellingFee());
			if (offer.getCancellingFeeDeadlineDays() != null) {
				if (offer.getCancellingFeeDeadlineDays() == 1) {
					cancellingFee += " (" + translate("cancelling.fee.addon.one") + ")";
				} else {
					cancellingFee += " (" + translate("cancelling.fee.addon", offer.getCancellingFeeDeadlineDays().toString()) + ")";
				}
			}
			uifactory.addStaticTextElement("cancelling.fee", cancellingFee, formLayout);
		}
		
		billingAddressEl = uifactory.addDropdownSingleselect("billing.address", formLayout, emptyStrings(), emptyStrings());
		billingAddressEl.setMandatory(true);
		billingAddressEl.enableNoneSelection(translate("billing.address.select"));
		loadBillingAddresses(ureq);
		
		FormLayoutContainer billingAddressButtonsCont = FormLayoutContainer.createButtonLayout("billingAddressButtons", getTranslator());
		billingAddressButtonsCont.setRootForm(mainForm);
		formLayout.add(billingAddressButtonsCont);
		
		billingAddressLink = uifactory.addFormLink("billing.address.create", billingAddressButtonsCont, Link.BUTTON);
		
		purchseNumberEl = uifactory.addTextElement("purchase.number", 100, null, formLayout);
		
		commentEl = uifactory.addTextAreaElement("comment", 4, 72, null, formLayout);
		
		uifactory.addFormSubmitButton("access.button.fee", formLayout);
	}

	private void loadBillingAddresses(UserRequest ureq) {
		SelectionValues billingAddressSV = new SelectionValues();
		
		BillingAddressSearchParams searchParams = new BillingAddressSearchParams();
		searchParams.setEnabled(Boolean.TRUE);
		searchParams.setIdentityKeys(List.of(identity));
		acService.getBillingAddresses(searchParams).forEach(
				address -> billingAddressSV.add(SelectionValues.entry(
						address.getKey().toString(),
						address.getIdentifier())));
		
		
		List<OrganisationRef> userOrganisations = ureq.getUserSession().getRoles().getOrganisationsWithRole(OrganisationRoles.user);
		if (userOrganisations != null && !userOrganisations.isEmpty()) {
			searchParams = new BillingAddressSearchParams();
			searchParams.setEnabled(Boolean.TRUE);
			searchParams.setOrganisations(userOrganisations);
			acService.getBillingAddresses(searchParams).forEach(
					address -> billingAddressSV.add(SelectionValues.entry(
							address.getKey().toString(),
							address.getIdentifier())));
		}
		
		billingAddressSV.sort(SelectionValues.VALUE_ASC);
		billingAddressEl.setKeysAndValues(billingAddressSV.keys(), billingAddressSV.values(), null);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (editCtrl == source) {
			if (event == Event.CHANGED_EVENT) {
				loadBillingAddresses(ureq);
				String key = editCtrl.getBillingAddress().getKey().toString();
				if (billingAddressEl.containsKey(key)) {
					billingAddressEl.select(key, true);
					billingAddressEl.clearError();
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(cmc);
		editCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == billingAddressLink) {
			doCreateBillingAddress(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		billingAddressEl.clearError();
		if (!billingAddressEl.isOneSelected()) {
			billingAddressEl.setErrorKey("form.legende.mandatory");
			allOk = false;
		} else {
			BillingAddressSearchParams searchParams = new BillingAddressSearchParams();
			searchParams.setBillingAddressKeys(List.of(Long.valueOf(billingAddressEl.getSelectedKey())));
			List<BillingAddress> billingAddresses = acService.getBillingAddresses(searchParams);
			if (billingAddresses.isEmpty()) {
				loadBillingAddresses(ureq);
				billingAddressEl.setErrorKey("form.legende.mandatory");
				allOk = false;
			} else {
				billingAddress = billingAddresses.get(0);
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		AccessResult result = acService.accessResource(identity, link, null, getIdentity());
		
		if (result.isAccessible()) {
			Order order = result.getOrder();
			if (order != null) {
				order = acService.addBillingAddress(order, billingAddress);
				
				if (StringHelper.containsNonWhitespace(purchseNumberEl.getValue())) {
					order.setPurchaseOrderNumber(purchseNumberEl.getValue());
				}
				if (StringHelper.containsNonWhitespace(commentEl.getValue())) {
					order.setComment(commentEl.getValue());
				}
				order = acService.updateOrder(order);
			}
			fireEvent(ureq, AccessEvent.ACCESS_OK_EVENT);
		} else {
			fireEvent(ureq, AccessEvent.ACCESS_FAILED_EVENT);
		}
	}

	private void doCreateBillingAddress(UserRequest ureq) {
		if (guardModalController(editCtrl)) return;
		
		editCtrl = new BillingAddressController(ureq, getWindowControl(), null, null, identity);
		listenTo(editCtrl);
		
		String title = translate("billing.address.create");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

}
