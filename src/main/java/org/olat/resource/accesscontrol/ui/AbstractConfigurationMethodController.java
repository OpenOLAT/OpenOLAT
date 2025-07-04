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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultiSelectionFilterElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.OrganisationUIFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.CatalogInfo;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial Date:  18 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public abstract class AbstractConfigurationMethodController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	private static final String PERIOD_STATUS = "status";
	private static final String PERIOD_DATE = "date";
	private static final String CATALOG_OO = "openolat";
	private static final String CATALOG_WEB = "web";
	
	private TextElement descEl;
	private TextElement labelEl;
	private SingleSelection periodEl;
	private DateChooser datesEl;
	private MultiSelectionFilterElement organisationsEl;
	private MultipleSelectionElement catalogEl;
	private MultipleSelectionElement confirmationEmailEl;
	private MultipleSelectionElement confirmationByManagerEl;

	protected final OfferAccess link;
	private final boolean offerOrganisationsSupported;
	private final Collection<Organisation> offerOrganisations;
	protected final CatalogInfo catalogInfo;
	private List<Organisation> organisations;
	private final boolean edit;
	
	@Autowired
	protected ACService acService;
	@Autowired
	private CatalogV2Module catalogModule;
	@Autowired
	private OrganisationModule organisationModule;
	
	public AbstractConfigurationMethodController(UserRequest ureq, WindowControl wControl, OfferAccess link,
			boolean offerOrganisationsSupported, Collection<Organisation> offerOrganisations, CatalogInfo catalogInfo,
			boolean edit) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AbstractConfigurationMethodController.class, getLocale(), getTranslator()));
		this.link = link;
		this.offerOrganisationsSupported = offerOrganisationsSupported;
		this.offerOrganisations = offerOrganisations;
		this.catalogInfo = catalogInfo;
		this.edit = edit;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Label
		String label = link.getOffer() != null ? link.getOffer().getLabel() : null;
		labelEl = uifactory.addTextElement("offer.label", "offer.label", 128, label, formLayout);
		labelEl.setElementCssClass("o_sel_accesscontrol_label");
		labelEl.setHelpTextKey("offer.label.help", null);
		
		// Catalog
		SelectionValues catalogSV = new SelectionValues();
		if (catalogModule.isEnabled()) {
			catalogSV.add(SelectionValues.entry(CATALOG_OO, translate("offer.publish.in.intern")));
			if (catalogModule.isWebPublishEnabled()) {
				catalogSV.add(SelectionValues.entry(CATALOG_WEB, translate("offer.publish.in.extern")));
			}
		}
		catalogEl = uifactory.addCheckboxesVertical("offer.publish.in", formLayout, catalogSV.keys(), catalogSV.values(), 1);
		catalogEl.setElementCssClass("o_sel_accesscontrol_publish");
		if (catalogEl.getKeys().contains(CATALOG_OO)) {
			catalogEl.select(CATALOG_OO, link.getOffer() != null && link.getOffer().isCatalogPublish());
		}
		if (catalogEl.getKeys().contains(CATALOG_WEB)) {
			catalogEl.select(CATALOG_WEB, link.getOffer() != null && link.getOffer().isCatalogWebPublish());
		}
		catalogEl.setVisible(catalogInfo.isCatalogSupported() && !catalogEl.getKeys().isEmpty());
		catalogEl.addActionListener(FormEvent.ONCHANGE);
		
		// Organisations
		if (organisationModule.isEnabled() && offerOrganisationsSupported) {
			initFormOrganisations(formLayout);
		}
		
		// Period
		SelectionValues periodSV = new SelectionValues();
		periodSV.add(SelectionValues.entry(PERIOD_STATUS, catalogInfo.getStatusPeriodOption()));
		periodSV.add(SelectionValues.entry(PERIOD_DATE, translate("offer.available.in.period")));
		periodEl = uifactory.addRadiosVertical("offer.available.in", formLayout, periodSV.keys(), periodSV.values());
		periodEl.addActionListener(FormEvent.ONCHANGE);
		String selectedPeriodKey = link.getOffer() != null && (link.getOffer().getValidFrom() != null || link.getOffer().getValidTo() != null)
				? PERIOD_DATE
				: PERIOD_STATUS;
		periodEl.select(selectedPeriodKey, true);
		
		datesEl = uifactory.addDateChooser("from_" + link.getKey(), "offer.period.date.from", link.getValidFrom(), formLayout);
		datesEl.setSecondDate(true);
		datesEl.setSecondDate(link.getValidTo());
		datesEl.setSeparator("offer.period.date.to");
		datesEl.setHelpTextKey("offer.preiod.help", null);
		
		uifactory.addSpacerElement("confirmations", formLayout, false);
		
		// Confirmations
		if (isConfirmationByManagerSupported()) {
			confirmationByManagerEl = uifactory.addCheckboxesHorizontal("confirmation.by", formLayout, onKeys,
					new String[] { translate("confirmation.by.admin") });
			confirmationByManagerEl.setElementCssClass("o_sel_accesscontrol_confirmation_manager");
			confirmationByManagerEl.select(onKeys[0], link.getOffer() != null && link.getOffer().isConfirmationByManagerRequired());
		}
		
		confirmationEmailEl = uifactory.addCheckboxesHorizontal("email.confirmation", formLayout, onKeys,
				new String[] { translate("email.confirmation.self") });
		confirmationEmailEl.select(onKeys[0], link.getOffer() != null && link.getOffer().isConfirmationEmail());
		
		uifactory.addSpacerElement("others", formLayout, false);
		
		// Custom
		initCustomFormElements(formLayout);
		
		// Description
		String desc = null;
		if(link.getOffer() != null) {
			desc = link.getOffer().getDescription();
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
		
		updateUI();
	}
	
	private void initFormOrganisations(FormItemContainer formLayout) {
		organisations = acService.getSelectionOfferOrganisations(getIdentity());
		
		if (offerOrganisations != null && !offerOrganisations.isEmpty()) {
			for (Organisation offerOrganisation : offerOrganisations) {
				if (offerOrganisation != null && !organisations.contains(offerOrganisation)) {
					organisations.add(offerOrganisation);
				}
			}
		}
		
		SelectionValues orgSV = OrganisationUIFactory.createSelectionValues(organisations, getLocale());
		organisationsEl = uifactory.addCheckboxesFilterDropdown("organisations", "offer.released.for", formLayout, getWindowControl(), orgSV);
		organisationsEl.setMandatory(true);
		offerOrganisations.forEach(organisation -> organisationsEl.select(organisation.getKey().toString(), true));
	}
	
	protected boolean isConfirmationByManagerSupported() {
		return false;
	}

	protected abstract void initCustomFormElements(FormItemContainer formLayout);
	
	protected abstract void updateCustomChanges();
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == periodEl) {
			updateUI();
		} else if (source == catalogEl) {
			updateCatalogUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	protected void updateUI() {
		boolean periodeDates = periodEl.isOneSelected() && PERIOD_DATE.equals(periodEl.getSelectedKey());
		datesEl.setVisible(periodeDates);
	}

	private void updateCatalogUI() {
		if (catalogEl.getKeys().contains(CATALOG_WEB)) {
			if (!catalogEl.getSelectedKeys().contains(CATALOG_OO)) {
				catalogEl.select(CATALOG_WEB, false);
				catalogEl.setEnabled(CATALOG_WEB, false);
			} else {
				catalogEl.setEnabled(CATALOG_WEB, true);
			}
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		datesEl.clearError();
		if (datesEl.getDate() != null && datesEl.getSecondDate() != null && datesEl.getDate().compareTo(datesEl.getSecondDate()) > 0) {
			datesEl.setErrorKey("date.error");
			allOk = false;
		}
		
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
	
	public OfferAccess getOfferAccess() {
		Offer offer = link.getOffer();
		offer.setLabel(labelEl.getValue());
		offer.setDescription(descEl.getValue());
		boolean hasDate = periodEl.isKeySelected(PERIOD_DATE);
		Date validFrom = hasDate? datesEl.getDate(): null;
		Date validTo = hasDate? datesEl.getSecondDate(): null;
		offer.setValidFrom(validFrom);
		offer.setValidTo(validTo);
		offer.setCatalogPublish(catalogEl.isKeySelected(CATALOG_OO));
		offer.setCatalogWebPublish(catalogEl.isKeySelected(CATALOG_WEB));
		offer.setConfirmationEmail(confirmationEmailEl.isAtLeastSelected(1));
		if (confirmationByManagerEl != null) {
			offer.setConfirmationByManagerRequired(confirmationByManagerEl.isAtLeastSelected(1));
		}
		link.setValidFrom(datesEl.getDate());
		link.setValidTo(datesEl.getSecondDate());
		updateCustomChanges();
		return link;
	}
	
	public List<Organisation> getOfferOrganisations() {
		if (organisationsEl == null) {
			if (offerOrganisations == null) {
				return null;
			}
			return List.copyOf(offerOrganisations);
		}
		
		Collection<String> selectedOrgKeys = organisationsEl.getSelectedKeys();
		return organisations.stream()
				.filter(org -> selectedOrgKeys.contains(org.getKey().toString()))
				.collect(Collectors.toList());
	}
	
}
