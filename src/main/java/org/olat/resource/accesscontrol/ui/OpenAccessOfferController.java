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

import java.util.Collection;
import java.util.List;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.CatalogInfo;
import org.olat.resource.accesscontrol.Offer;
import org.olat.user.ui.organisation.OrganisationSelectionSource;
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
	private ObjectSelectionElement organisationsEl;
	private MultipleSelectionElement catalogEl;

	private final Offer offer;
	private final boolean offerOrganisationsSupported;
	private final Collection<Organisation> offerOrganisations;
	private final CatalogInfo catalogInfo;
	private final boolean edit;
	
	@Autowired
	private ACService acService;
	@Autowired
	private CatalogV2Module catalogModule;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	
	public OpenAccessOfferController(UserRequest ureq, WindowControl wControl, Offer offer,
			boolean offerOrganisationsSupported, Collection<Organisation> offerOrganisations, CatalogInfo catalogInfo,
			boolean edit) {
		super(ureq, wControl);
		this.offer = offer;
		this.offerOrganisationsSupported = offerOrganisationsSupported;
		this.offerOrganisations = offerOrganisations;
		this.catalogInfo = catalogInfo;
		this.edit = edit;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_accesscontrol_open_form");
		
		// Catalog
		SelectionValues catalogSV = new SelectionValues();
		if (catalogModule.isWebPublishEnabled()) {
			catalogSV.add(SelectionValues.entry(CATALOG_WEB, translate("offer.publish.in.extern")));
		}
		catalogEl = uifactory.addCheckboxesVertical("offer.publish.in", formLayout, catalogSV.keys(), catalogSV.values(), 1);
		catalogEl.setElementCssClass("o_sel_accesscontrol_catalog");
		if (catalogEl.getKeys().contains(CATALOG_WEB)) {
			catalogEl.select(CATALOG_WEB,offer != null && offer.isCatalogWebPublish());
		}
		catalogEl.setVisible(catalogInfo.isCatalogSupported() && !catalogEl.getKeys().isEmpty());
		
		// Organisations
		if (organisationModule.isEnabled() && offerOrganisationsSupported) {
			initFormOrganisations(formLayout);
		}
		
		// Period
		uifactory.addStaticTextElement("offer.available.in", catalogInfo.getStatusPeriodOption(), formLayout);
		
		uifactory.addSpacerElement("others", formLayout, false);
		
		// Description
		String desc = null;
		if(offer != null) {
			desc = offer.getDescription();
		}
		descEl = uifactory.addTextAreaElement("offer-desc", "offer.description", 2000, 6, 80, false, false, desc, formLayout);
		descEl.setElementCssClass("o_sel_accesscontrol_description");
		descEl.setHelpTextKey("offer.description.help", null);
		
		// Buttons
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		buttonGroupLayout.setRootForm(mainForm);
		formLayout.add(buttonGroupLayout);

		if(edit) {
			uifactory.addFormSubmitButton("save", buttonGroupLayout);
		} else {
			uifactory.addFormSubmitButton("create", buttonGroupLayout);
		}
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
	}
	
	private void initFormOrganisations(FormItemContainer formLayout) {
		Collection<? extends OrganisationRef> selectedOrganisations = offerOrganisations != null? offerOrganisations: List.of();
		OrganisationSelectionSource organisationSource = new OrganisationSelectionSource(
				selectedOrganisations,
				() -> acService.getSelectionOfferOrganisations(getIdentity()));
		organisationsEl = uifactory.addObjectSelectionElement("organisations", "offer.released.for", formLayout,
				getWindowControl(), true, organisationSource);
		organisationsEl.setMandatory(true);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (organisationsEl != null) {
			organisationsEl.clearError();
			if (organisationsEl.getSelectedKeys().isEmpty()) {
				organisationsEl.setErrorKey("form.legende.mandatory");
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
		if (organisationsEl == null) {
			if (offerOrganisations == null) {
				return null;
			}
			return List.copyOf(offerOrganisations);
		}
		
		return organisationService.getOrganisation(OrganisationSelectionSource.toRefs(organisationsEl.getSelectedKeys()));
	}

}
