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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.BillingAddress;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.ui.AccessEvent;
import org.olat.resource.accesscontrol.ui.BillingAddressController;
import org.olat.resource.accesscontrol.ui.BillingAddressItem;
import org.olat.resource.accesscontrol.ui.BillingAddressSelectionController;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 Nov 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class InvoiceSubmitDetailsController extends FormBasicController {
	
	private FormLayoutContainer billingAddressCont;
	private BillingAddressItem billingAddressItem;
	private TextElement purchseNumberEl;
	private FormLink billingAddressLink;
	private TextAreaElement commentEl;
	
	private CloseableModalController cmc;
	private BillingAddressSelectionController addressSelectionCtrl;

	private final OfferAccess link;
	private final Identity bookedIdentity;
	private BillingAddress billingAddress;
	
	@Autowired
	private ACService acService;

	protected InvoiceSubmitDetailsController(UserRequest ureq, WindowControl wControl, OfferAccess link, Identity bookedIdentity) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(BillingAddressController.class, getLocale(), getTranslator()));
		this.link = link;
		this.bookedIdentity = bookedIdentity;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		
		billingAddressCont = FormLayoutContainer.createCustomFormLayout("billingAddress", getTranslator(), velocity_root + "/billing_address.html");
		billingAddressCont.setLabel("billing.address", null);
		billingAddressCont.setMandatory(true);
		billingAddressCont.setRootForm(mainForm);
		formLayout.add(billingAddressCont);
		
		billingAddressItem = new BillingAddressItem("billing.address", getLocale());
		billingAddressItem.setTemporaryAddressWarning(false);
		billingAddressCont.add(billingAddressItem.getName(), billingAddressItem);
		updateBillingAddress(billingAddress);
		
		billingAddressLink = uifactory.addFormLink("billing.address.select", billingAddressCont, Link.BUTTON);
		
		purchseNumberEl = uifactory.addTextElement("order.purchase.number", 100, null, formLayout);
		
		commentEl = uifactory.addTextAreaElement("order.comment", 4, 72, null, formLayout);
		
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
		
		uifactory.addFormSubmitButton("access.button.fee", formLayout);
	}

	private void updateBillingAddress(BillingAddress billingAddress) {
		this.billingAddress = billingAddress;
		billingAddressItem.setBillingAddress(billingAddress);
		billingAddressItem.setVisible(billingAddress != null);
		billingAddressCont.setDirty(true);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (addressSelectionCtrl == source) {
			if (event == Event.DONE_EVENT) {
				updateBillingAddress(addressSelectionCtrl.getBillingAddress());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(addressSelectionCtrl);
		removeAsListenerAndDispose(cmc);
		addressSelectionCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == billingAddressLink) {
			doSelectBillingAddress(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		billingAddressCont.clearError();
		if (billingAddress == null) {
			billingAddressCont.setErrorKey("form.legende.mandatory");
			allOk = false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		AccessResult result = acService.accessResource(bookedIdentity, link, OrderStatus.PREPAYMENT, null, getIdentity());
		
		if (result.isAccessible()) {
			Order order = result.getOrder();
			if (order != null) {
				if (billingAddress.getKey() == null) {
					BillingAddress newAddress = billingAddress;
					billingAddress = acService.createBillingAddress(billingAddress.getOrganisation(), billingAddress.getIdentity());
					billingAddress.setIdentifier(newAddress.getIdentifier());
					billingAddress.setNameLine1(newAddress.getNameLine1());
					billingAddress.setNameLine2(newAddress.getNameLine2());
					billingAddress.setAddressLine1(newAddress.getAddressLine1());
					billingAddress.setAddressLine2(newAddress.getAddressLine2());
					billingAddress.setAddressLine3(newAddress.getAddressLine3());
					billingAddress.setAddressLine4(newAddress.getAddressLine4());
					billingAddress.setPoBox(newAddress.getPoBox());
					billingAddress.setRegion(newAddress.getRegion());
					billingAddress.setZip(newAddress.getZip());
					billingAddress.setCity(newAddress.getCity());
					billingAddress.setCountry(newAddress.getCountry());
					billingAddress = acService.updateBillingAddress(billingAddress);
				}
				
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

	private void doSelectBillingAddress(UserRequest ureq) {
		if (guardModalController(addressSelectionCtrl)) return;
		
		addressSelectionCtrl = new BillingAddressSelectionController(ureq, getWindowControl(), true, true, true, true,
				bookedIdentity, billingAddress);
		listenTo(addressSelectionCtrl);
		
		String title = translate("billing.address.select");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				addressSelectionCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

}
