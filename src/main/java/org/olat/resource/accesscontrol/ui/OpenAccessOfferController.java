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
package org.olat.resource.accesscontrol.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationNameComparator;
import org.olat.core.id.Roles;
import org.olat.resource.accesscontrol.Offer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OpenAccessOfferController extends FormBasicController {
	
	private static final String CATALOG_WEB = "web";
	
	private TextElement descEl;
	private MultipleSelectionElement organisationsEl;
	private MultipleSelectionElement catalogEl;

	private final Offer offer;
	private final Collection<Organisation> offerOrganisations;
	private final boolean edit;
	private List<Organisation> organisations;
	
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	
	public OpenAccessOfferController(UserRequest ureq, WindowControl wControl, Offer offer,
			Collection<Organisation> offerOrganisations, boolean edit) {
		super(ureq, wControl);
		this.offer = offer;
		this.offerOrganisations = offerOrganisations;
		this.edit = edit;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String desc = null;
		if(offer != null) {
			desc = offer.getDescription();
		}
		descEl = uifactory.addTextAreaElement("offer-desc", "offer.description", 2000, 6, 80, false, false, desc, formLayout);
		descEl.setElementCssClass("o_sel_accesscontrol_description");
		
		uifactory.addStaticTextElement("offer.period", translate("offer.period.status"), formLayout);
		
		if (organisationModule.isEnabled() && offerOrganisations != null) {
			initFormOrganisations(formLayout, ureq.getUserSession().getRoles());
		}
		
		SelectionValues catalogSV = new SelectionValues();
		catalogSV.add(SelectionValues.entry(CATALOG_WEB, translate("offer.catalog.web")));
		catalogEl = uifactory.addCheckboxesVertical("offer.catalog", formLayout, catalogSV.keys(), catalogSV.values(), 1);
		catalogEl.select(CATALOG_WEB, offer.isCatalogWebPublish());
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		buttonGroupLayout.setRootForm(mainForm);
		formLayout.add(buttonGroupLayout);

		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
		if(edit) {
			uifactory.addFormSubmitButton("save", buttonGroupLayout);
		} else {
			uifactory.addFormSubmitButton("create", buttonGroupLayout);
		}
	}
	
	private void initFormOrganisations(FormItemContainer formLayout, Roles roles) {
		organisations = organisationService.getOrganisations(getIdentity(), roles, OrganisationRoles.administrator,
				OrganisationRoles.learnresourcemanager, OrganisationRoles.author);
		
		for (Organisation offerOrganisation : offerOrganisations) {
			if (offerOrganisation != null && !organisations.contains(offerOrganisation)) {
				organisations.add(offerOrganisation);
			}
		}
		
		Collections.sort(organisations, new OrganisationNameComparator(getLocale()));
		
		SelectionValues orgSV = new SelectionValues();
		organisations.forEach(org -> orgSV.add(entry(org.getKey().toString(), org.getDisplayName())));
		organisationsEl = uifactory.addCheckboxesDropdown("organisations", "offer.organisations", formLayout,
				orgSV.keys(), orgSV.values(), null, null);
		organisationsEl.setMandatory(true);
		for (Organisation offerOrganisation : offerOrganisations) {
			if (organisationsEl.getKeys().contains(offerOrganisation.getKey().toString())) {
				organisationsEl.select(offerOrganisation.getKey().toString(), true);
			}
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (organisationsEl != null) {
			organisationsEl.clearError();
			if (organisationsEl.getSelectedKeys().isEmpty()) {
				organisationsEl.setErrorKey("form.legende.mandatory", null);
				allOk = false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	public Offer getOffer() {
		offer.setOpenAccess(true);
		offer.setDescription(descEl.getValue());
		offer.setCatalogWebPublish(catalogEl.isKeySelected(CATALOG_WEB));
		return offer;
	}
	
	public List<Organisation> getOfferOrganisations() {
		if (organisationsEl == null) return null;
		
		Collection<String> selectedOrgKeys = organisationsEl.getSelectedKeys();
		return organisations.stream()
				.filter(org -> selectedOrgKeys.contains(org.getKey().toString()))
				.collect(Collectors.toList());
	}

}
