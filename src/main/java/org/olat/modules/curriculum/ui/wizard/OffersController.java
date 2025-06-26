/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.wizard;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.model.OrganisationWithParents;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.olat.modules.curriculum.ui.wizard.MembersContext.AccessInfos;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.BillingAddress;
import org.olat.resource.accesscontrol.BillingAddressSearchParams;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalAccessMethod;
import org.olat.resource.accesscontrol.provider.paypalcheckout.model.PaypalCheckoutAccessMethod;
import org.olat.resource.accesscontrol.ui.BillingAddressItem;
import org.olat.resource.accesscontrol.ui.BillingAddressSelectionController;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OffersController extends StepFormBasicController {
	
	private static final String NO_BOOKING  = "nob";
	
	private TextElement commentEl;
	private SingleSelection bookingsEl;
	private FormLayoutContainer billingAddressInfoCont;
	private BillingAddressItem billingAddressEl;
	private FormLink billingAdresseSelectLink;
	private StaticTextElement priceEl;
	private TextElement purchaseOrderNumberEl;
	private StaticTextElement cancellationFeeEl;
	
	private CloseableModalController cmc;
	private BillingAddressSelectionController addressSelectionCtrl;
	
	private final MembersContext membersContext;
	private final List<AccessInfos> validOffers;
	private boolean allIdentitiesInSameOrganisations;
	private BillingAddress uniqueUserBillingAddress;
	private boolean needBillingAddress;
	
	@Autowired
	private ACService acService;
	@Autowired
	private AccessControlModule acModule;

	public OffersController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext,
			MembersContext membersContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));
		this.membersContext = membersContext;
		validOffers = validOffers(membersContext);
		updateIdentitiesInSameOrganisations();
		initForm(ureq);
		updateUI();
	}

	private List<AccessInfos> validOffers(MembersContext context) {
		List<Identity> identities = membersContext.getSelectedIdentities();
		List<OrganisationRef> organisations = null;
		for(Identity identity:identities) {
			List<OrganisationRef> organisationsRefs = acService.getOfferOrganisations(identity);
			if(organisations == null) {
				organisations = new ArrayList<>(organisationsRefs);
			} else {
				organisations.retainAll(organisationsRefs);
			}
		}

		List<Offer> offers = context.getOffers();
		List<AccessInfos> accessList = new ArrayList<>();
		Map<Long,List<Organisation>> offerKeyToOrganisations = acService.getOfferKeyToOrganisations(offers);
		for(Offer offer:offers) {
			if(offer.isGuestAccess()) {
				continue;
			}
			List<Organisation> offerOrganisations = offerKeyToOrganisations.get(offer.getKey());
			if(allowedByOrganisations(organisations, offerOrganisations)) {
				List<OfferAccess> offerAccess = acService.getOfferAccess(offer, true);
				for(OfferAccess link:offerAccess) {
					if(validOfferAccess(link)) {
						accessList.add(new AccessInfos(offer, link, offerOrganisations));
					}
				}
			}
		}
		return accessList;
	}
	
	private void updateIdentitiesInSameOrganisations() {
		Set<Organisation> refOrganisations = null;
		for (List<OrganisationWithParents> userOrganisations : membersContext.getIdentityKeyToUserOrganisations().values()) {
			if (refOrganisations == null) {
				refOrganisations = userOrganisations.stream()
						.map(OrganisationWithParents::getOrganisation)
						.collect(Collectors.toSet());
			} else {
				Set<Organisation> currentOrganisations = userOrganisations.stream()
						.map(OrganisationWithParents::getOrganisation)
						.collect(Collectors.toSet());
				if (!refOrganisations.equals(currentOrganisations)) {
					allIdentitiesInSameOrganisations = false;
					uniqueUserBillingAddress = null;
					return;
				}
			}
		}
		
		allIdentitiesInSameOrganisations = true;
		
		BillingAddressSearchParams baSearchParams = new BillingAddressSearchParams();
		baSearchParams.setOrganisations(refOrganisations);
		List<BillingAddress> billingAddresses = acService.getBillingAddresses(baSearchParams);
		if (billingAddresses.size() == 1) {
			uniqueUserBillingAddress = billingAddresses.get(0);
		} else {
			uniqueUserBillingAddress = null;
		}
	}
	
	/**
	 * Exclude Paypal
	 * 
	 * @param offerAccess The offer access
	 * @return Accepted or not for this process
	 */
	private boolean validOfferAccess(OfferAccess offerAccess) {
		AccessMethod method = offerAccess.getMethod();
		if(method instanceof PaypalAccessMethod || method instanceof PaypalCheckoutAccessMethod) {
			return false;
		}
		return true;
	}
	
	private boolean allowedByOrganisations(List<OrganisationRef> organisations, List<Organisation> offerOrganisations) {
		if(organisations == null || organisations.isEmpty()) return false;
		if(offerOrganisations == null || offerOrganisations.isEmpty()) return true;
		
		for(Organisation offerOrganisation:offerOrganisations) {
			for(OrganisationRef organisation:organisations) {
				if(offerOrganisation.getKey().equals(organisation.getKey())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues bookingPK = new SelectionValues();
		if(membersContext.isOffersAvailable()) {
			for(AccessInfos access:validOffers) {
				bookingPK.add(forgeOfferAccess(access.offer(), access.offerAccess(), access.organisations()));
			}
		}
		if(membersContext.isOffersAvailable() && bookingPK.isEmpty()) {
			setFormWarning("warning.no.offer.available");
		}

		bookingPK.add(SelectionValues.entry(NO_BOOKING, translate("booking.no.offer"), translate("booking.no.offer.desc"),
				"o_icon o_icon_forward", null, true));
		bookingsEl = uifactory.addCardSingleSelectHorizontal("booking.order", "booking.order", formLayout, bookingPK);
		bookingsEl.addActionListener(FormEvent.ONCHANGE);
		bookingsEl.select(bookingPK.keys()[0], true);
		
		uifactory.addSpacerElement("after-booking", formLayout, false);
		
		billingAddressInfoCont = FormLayoutContainer.createHorizontalFormLayout("billingAddressInfo", getTranslator());
		billingAddressInfoCont.setFormDescription(translate("booking.billing.address.multi.organisations"));
		billingAddressInfoCont.setRootForm(mainForm);
		formLayout.add(billingAddressInfoCont);
		
		billingAddressEl = new BillingAddressItem("billing,address", getLocale());
		billingAddressEl.setLabel("booking.billing.address", null);
		formLayout.add(billingAddressEl);
		if (allIdentitiesInSameOrganisations && uniqueUserBillingAddress != null) {
			billingAddressEl.setBillingAddress(uniqueUserBillingAddress);
		}
		
		billingAdresseSelectLink = uifactory.addFormLink("select.billing.address", formLayout, Link.BUTTON);
		billingAdresseSelectLink.setElementCssClass("o_sel_billing_address_select");
		
		priceEl = uifactory.addStaticTextElement("booking.offer.price", "booking.offer.price", "", formLayout);
		cancellationFeeEl = uifactory.addStaticTextElement("booking.offer.cancellation.fee", "booking.offer.cancellation.fee", "", formLayout);
		
		purchaseOrderNumberEl = uifactory.addTextElement("booking.po.number", 128, "", formLayout);
		commentEl = uifactory.addTextAreaElement("booking.offer.comment", "booking.offer.comment", 4000, 3, 60, false, false, false, "", formLayout);
	}
	
	private SelectionValue forgeOfferAccess(Offer offer, OfferAccess offerAccess, List<Organisation> offerOrganisations) {
		AccessMethod accessMethod = offerAccess.getMethod();
		AccessMethodHandler handler = acModule.getAccessMethodHandler(accessMethod.getType());
		String iconCssClass = "o_icon o_icon-fw " + accessMethod.getMethodCssClass() + "_icon";
		String name = handler.getMethodName(getLocale());
		String description = forgeOfferAccessDescription(offer, offerAccess, offerOrganisations);
		return SelectionValues.entry(offerAccess.getKey().toString(),
				name, description, iconCssClass, null, true);
	}
	
	private String forgeOfferAccessDescription(Offer offer, OfferAccess offerAccess, List<Organisation> offerOrganisations) {
		List<String> infos = new ArrayList<>();
		
		// Organisations
		if(offerOrganisations != null && !offerOrganisations.isEmpty()) {
			List<String> orgNames = offerOrganisations.stream()
					.map(Organisation::getDisplayName)
					.map(StringHelper::escapeHtml).toList();
			String orgNamesVal = String.join(", ", orgNames);
			infos.add(translate("booking.organisations", orgNamesVal));
		}
		
		// Dates
		Date from = offerAccess.getValidFrom();
		Date to = offerAccess.getValidTo();
		Formatter formatter = Formatter.getInstance(getLocale());
		String[] dateArgs = new String[] { formatter.formatDate(from), formatter.formatDate(to) };
		if(from != null && to != null) {
			infos.add(translate("booking.date.from.to", dateArgs));
		} else if(from != null) {
			infos.add(translate("booking.date.from", dateArgs));
		} else if(to != null) {
			infos.add(translate("booking.date.to", dateArgs));
		}
		
		// Price
		Price price = offer.getPrice();
		if(price != null) {
			String val = PriceFormat.fullFormat(price);
			infos.add(translate("booking.price", val));
		}
		
		return String.join(";<br>", infos);
	}
	
	private AccessInfos getAccessInfos(String key) {
		for(AccessInfos infos:validOffers) {
			if(infos.offerAccess().getKey().toString().equals(key)) {
				return infos;
			}
		}
		return null;
	}
	
	private void updateUI() {
		boolean selected = bookingsEl.isOneSelected() && !NO_BOOKING.equals(bookingsEl.getSelectedKey());
		if(selected) {
			AccessInfos infos = getAccessInfos(bookingsEl.getSelectedKey());
			AccessMethod method = infos.offerAccess().getMethod();
			boolean withPayment = method.isPaymentMethod();
			
			commentEl.setVisible(true);
			
			String price = PriceFormat.fullFormat(infos.offer().getPrice());
			priceEl.setValue(price);
			priceEl.setVisible(StringHelper.containsNonWhitespace(price) && withPayment);
			
			String cancellationFee = PriceFormat.fullFormat(infos.offer().getCancellingFee());
			cancellationFeeEl.setValue(cancellationFee);
			cancellationFeeEl.setVisible(StringHelper.containsNonWhitespace(cancellationFee) && withPayment);
			
			needBillingAddress = method.isNeedBillingAddress();
			billingAddressInfoCont.setVisible(needBillingAddress && !allIdentitiesInSameOrganisations);
			billingAddressEl.setVisible(needBillingAddress && allIdentitiesInSameOrganisations);
			billingAdresseSelectLink.setVisible(needBillingAddress && allIdentitiesInSameOrganisations);
			purchaseOrderNumberEl.setVisible(needBillingAddress);
		} else {
			needBillingAddress = false;
			commentEl.setVisible(false);
			priceEl.setVisible(false);
			cancellationFeeEl.setVisible(false);
			billingAddressInfoCont.setVisible(false);
			billingAddressEl.setVisible(false);
			billingAdresseSelectLink.setVisible(false);
			purchaseOrderNumberEl.setVisible(false);
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		bookingsEl.clearError();
		if(bookingsEl.isVisible() && !bookingsEl.isOneSelected()) {
			bookingsEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(bookingsEl == source) {
			updateUI();
		} else if(billingAdresseSelectLink == source) {
			doSelectBillingAddress(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (addressSelectionCtrl == source) {
			if (event == Event.DONE_EVENT) {
				billingAddressEl.setBillingAddress(addressSelectionCtrl.getBillingAddress());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(addressSelectionCtrl);
		removeAsListenerAndDispose(cmc);
		addressSelectionCtrl = null;
		cmc = null;
	}

	@Override
	protected void formNext(UserRequest ureq) {
		if(bookingsEl.isOneSelected()) {
			AccessInfos infos = getAccessInfos(bookingsEl.getSelectedKey());
			membersContext.setSelectedOffer(infos);
			
			membersContext.setNeedBillingAddress(needBillingAddress);
			if (needBillingAddress) {
				if (membersContext.getIdentityKeyToBillingAddress() == null) {
					membersContext.setIdentityKeyToBillingAddress(new HashMap<>(membersContext.getSelectedIdentities().size()));
				}
			}
			if (billingAddressEl.isVisible()) {
				membersContext.setBillingAddress(billingAddressEl.getBillingAddress());
			} else {
				membersContext.setBillingAddress(null);
			}
			
			if(purchaseOrderNumberEl.isVisible() && StringHelper.containsNonWhitespace(purchaseOrderNumberEl.getValue())) {
				membersContext.setPurchaseOrderNumber(purchaseOrderNumberEl.getValue());
			} else {
				membersContext.setPurchaseOrderNumber(null);
			}
			
			if(commentEl.isVisible() && StringHelper.containsNonWhitespace(commentEl.getValue())) {
				membersContext.setOrderComment(commentEl.getValue());
			} else {
				membersContext.setOrderComment(null);
			}
		}
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSelectBillingAddress(UserRequest ureq) {
		if (guardModalController(addressSelectionCtrl)) return;
		
		addressSelectionCtrl = new BillingAddressSelectionController(ureq, getWindowControl(), true, false, false,
				false, membersContext.getSelectedIdentities().get(0), billingAddressEl.getBillingAddress());
		listenTo(addressSelectionCtrl);
		
		String title = translate("select.billing.address");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				addressSelectionCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
}
