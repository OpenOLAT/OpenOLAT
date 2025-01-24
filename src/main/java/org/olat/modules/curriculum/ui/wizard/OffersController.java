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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
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
import org.olat.core.id.Roles;
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
import org.olat.resource.accesscontrol.ui.BillingAddressController;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.olat.user.UserManager;
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
	private SpacerElement spacerEl;
	private SingleSelection bookingsEl;
	private StaticTextElement priceEl;
	private TextElement purchaseOrderNumberEl;
	private StaticTextElement cancellationFeeEl;
	private SingleSelection billingAdressEl;
	private FormLink createBillingAdresseButton;
	
	private BillingAddress newBillingAddress;
	private final MembersContext membersContext;
	private final List<AccessInfos> validOffers;
	private final Map<Long,BillingAddress> billingAddressMap = new HashMap<>();
	
	private CloseableModalController cmc;
	private BillingAddressController billingAddressCtrl;
	
	@Autowired
	private ACService acService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private BaseSecurity securityManager;
	
	public OffersController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext,
			MembersContext membersContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));
		this.membersContext = membersContext;
		validOffers = validOffers(membersContext);
		initForm(ureq);
		updateUI();
	}
	
	private List<AccessInfos> validOffers(MembersContext context) {
		List<Identity> identities = membersContext.getSelectedIdentities();
		List<OrganisationRef> organisations = null;
		for(Identity identity:identities) {
			Roles roles = securityManager.getRoles(identity);
			List<OrganisationRef> organisationsRefs = roles.getOrganisationsWithRole(OrganisationRoles.user);
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
		
		spacerEl = uifactory.addSpacerElement("after-booking", formLayout, false);
		
		SelectionValues addressPK = forgeBillingAddress();
		billingAdressEl = uifactory.addDropdownSingleselect("booking.billing.address", formLayout,
				addressPK.keys(), addressPK.values());
		billingAdressEl.setMandatory(true);
		createBillingAdresseButton = uifactory.addFormLink("create.billing.address", formLayout, Link.BUTTON);
		createBillingAdresseButton.setVisible(membersContext.getSelectedIdentities().size() == 1);
		
		priceEl = uifactory.addStaticTextElement("booking.offer.price", "booking.offer.price", "", formLayout);
		cancellationFeeEl = uifactory.addStaticTextElement("booking.offer.cancellation.fee", "booking.offer.cancellation.fee", "", formLayout);
		
		purchaseOrderNumberEl = uifactory.addTextElement("booking.po.number", 128, "", formLayout);
		commentEl = uifactory.addTextAreaElement("booking.offer.comment", "booking.offer.comment", 4000, 3, 60, false, false, false, "", formLayout);
		
	}
	
	private SelectionValues forgeBillingAddress() {
		SelectionValues addressPK = new SelectionValues();
		addressPK.add(SelectionValues.entry("", translate("select.billing.address")));

		Set<Organisation> organisations = new HashSet<>();
		for(AccessInfos offer:validOffers) {
			organisations.addAll(offer.organisations());
		}
		if(!organisations.isEmpty()) {
			BillingAddressSearchParams organisationParams = new BillingAddressSearchParams();
			organisationParams.setOrganisations(organisations);
			List<BillingAddress> billingAddressList = acService.getBillingAddresses(organisationParams);
			for(BillingAddress billingAddress:billingAddressList) {
				String value = billingAddressLabel(billingAddress);
				addressPK.add(SelectionValues.entry(billingAddress.getKey().toString(), value));
				billingAddressMap.put(billingAddress.getKey(), billingAddress);
			}
		}
		
		List<Identity> identities = membersContext.getSelectedIdentities();
		if(identities.size() == 1) {
			BillingAddressSearchParams userParams = new BillingAddressSearchParams();
			userParams.setIdentityKeys(identities);
			List<BillingAddress> billingAddressList = acService.getBillingAddresses(userParams);
			for(BillingAddress billingAddress:billingAddressList) {
				String value = billingAddressLabel(billingAddress);
				addressPK.add(SelectionValues.entry(billingAddress.getKey().toString(), value));
				billingAddressMap.put(billingAddress.getKey(), billingAddress);
			}
		}
		
		if(newBillingAddress != null) {
			String value = billingAddressLabel(newBillingAddress);
			addressPK.add(SelectionValues.entry(newBillingAddress.getKey().toString(), value));
			billingAddressMap.put(newBillingAddress.getKey(), newBillingAddress);
		}
		
		return addressPK;
	}
	
	private String billingAddressLabel(BillingAddress billingAddress) {
		List<String> sb = new ArrayList<>();
		if(StringHelper.containsNonWhitespace(billingAddress.getIdentifier())) {
			sb.add(billingAddress.getIdentifier());
		}
		
		if(billingAddress.getIdentity() != null) {
			String fullName = userManager.getUserDisplayName(billingAddress.getIdentity());
			sb.add(fullName);
		}
		
		if(billingAddress.getOrganisation() != null) {
			sb.add(billingAddress.getOrganisation().getDisplayName());
		}
		return String.join(" \u00B7 ", sb);
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
		spacerEl.setVisible(selected);
		
		if(selected) {
			AccessInfos infos = getAccessInfos(bookingsEl.getSelectedKey());
			String price = PriceFormat.fullFormat(infos.offer().getPrice());
			priceEl.setValue(price);
			priceEl.setVisible(StringHelper.containsNonWhitespace(price));
			
			String cancellationFee = PriceFormat.fullFormat(infos.offer().getCancellingFee());
			cancellationFeeEl.setValue(cancellationFee);
			cancellationFeeEl.setVisible(StringHelper.containsNonWhitespace(cancellationFee));
		} else {
			priceEl.setVisible(false);
			cancellationFeeEl.setVisible(false);
		}
		
		createBillingAdresseButton.setVisible(selected);
		purchaseOrderNumberEl.setVisible(selected);
		billingAdressEl.setVisible(selected);
		commentEl.setVisible(selected);
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(billingAddressCtrl == source) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				newBillingAddress = billingAddressCtrl.getBillingAddress();
				updateBillingAddress(newBillingAddress);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void updateBillingAddress(BillingAddress billingAddressToSelect) {
		SelectionValues addressPK = forgeBillingAddress();
		billingAdressEl.setKeysAndValues(addressPK.keys(), addressPK.values(), null);
		if(billingAddressToSelect != null) {
			billingAdressEl.select(billingAddressToSelect.getKey().toString(), true);
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(billingAddressCtrl);
		removeAsListenerAndDispose(cmc);
		billingAddressCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(bookingsEl == source) {
			updateUI();
		} else if(createBillingAdresseButton == source) {
			doCreateBillingAdresseButton(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formNext(UserRequest ureq) {
		if(bookingsEl.isOneSelected()) {
			AccessInfos infos = getAccessInfos(bookingsEl.getSelectedKey());
			membersContext.setSelectedOffer(infos);
			
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
			
			String addressKey = billingAdressEl.isVisible() && billingAdressEl.isOneSelected()
					? billingAdressEl.getSelectedKey()
					: null;
			if(StringHelper.isLong(addressKey)) {
				BillingAddress address = billingAddressMap.get(Long.valueOf(addressKey));
				membersContext.setBillingAddress(address);
			}
		}
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doCreateBillingAdresseButton(UserRequest ureq) {
		Identity identity = membersContext.getSelectedIdentities().get(0);
		billingAddressCtrl = new BillingAddressController(ureq, getWindowControl(), null, null, identity);
		listenTo(billingAddressCtrl);
		
		String title = translate("create.billing.address");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), billingAddressCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
}
