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
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
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
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AccessMethod;
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
	
	private SpacerElement spacerEl;
	private SingleSelection bookingsEl;
	private StaticTextElement priceEl;
	private StaticTextElement cancellationFeeEl;
	
	private final MembersContext membersContext;
	private final List<AccessInfos> validOffers;
	
	@Autowired
	private ACService acService;
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
					accessList.add(new AccessInfos(offer, link, offerOrganisations));
				}
			}
		}
		return accessList;
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
		
		priceEl = uifactory.addStaticTextElement("booking.offer.price", "booking.offer.price", "", formLayout);
		cancellationFeeEl = uifactory.addStaticTextElement("booking.offer.cancellation.fee", "booking.offer.cancellation.fee", "", formLayout);
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
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(bookingsEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formNext(UserRequest ureq) {
		if(bookingsEl.isOneSelected()) {
			AccessInfos infos = getAccessInfos(bookingsEl.getSelectedKey());
			membersContext.setSelectedOffer(infos);
		}
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
