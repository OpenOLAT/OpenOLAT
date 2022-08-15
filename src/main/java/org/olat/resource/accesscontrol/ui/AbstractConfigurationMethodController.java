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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationNameComparator;
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
	private SingleSelection periodEl;
	private DateChooser datesEl;
	private MultipleSelectionElement organisationsEl;
	private MultipleSelectionElement catalogEl;
	private MultipleSelectionElement confirmationEmailEl;

	protected final OfferAccess link;
	private final boolean offerOrganisationsSupported;
	private final Collection<Organisation> offerOrganisations;
	private final CatalogInfo catalogInfo;
	private List<Organisation> organisations;
	private final boolean edit;
	
	@Autowired
	private ACService acService;
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
		String desc = null;
		if(link.getOffer() != null) {
			desc = link.getOffer().getDescription();
		}
		descEl = uifactory.addTextAreaElement("offer-desc", "offer.description", 2000, 6, 80, false, false, desc, formLayout);
		descEl.setElementCssClass("o_sel_accesscontrol_description");
		
		SelectionValues periodSV = new SelectionValues();
		periodSV.add(SelectionValues.entry(PERIOD_STATUS, translate("offer.period.status")));
		periodSV.add(SelectionValues.entry(PERIOD_DATE, translate("offer.period.date")));
		periodEl = uifactory.addRadiosVertical("offer.period", formLayout, periodSV.keys(), periodSV.values());
		periodEl.addActionListener(FormEvent.ONCHANGE);
		String selectedPeriodKey = link.getOffer() != null && (link.getOffer().getValidFrom() != null || link.getOffer().getValidTo() != null)
				? PERIOD_DATE
				: PERIOD_STATUS;
		periodEl.select(selectedPeriodKey, true);
		
		datesEl = uifactory.addDateChooser("from_" + link.getKey(), "offer.period.date.from", link.getValidFrom(), formLayout);
		datesEl.setSecondDate(true);
		datesEl.setSecondDate(link.getValidTo());
		datesEl.setSeparator("offer.period.date.to");
		
		if (organisationModule.isEnabled() && offerOrganisationsSupported) {
			initFormOrganisations(formLayout);
		}
		
		SelectionValues catalogSV = new SelectionValues();
		if (catalogModule.isEnabled()) {
			catalogSV.add(SelectionValues.entry(CATALOG_OO, translate("offer.catalog.openolat")));
			if (catalogModule.isWebPublishEnabled()) {
				catalogSV.add(SelectionValues.entry(CATALOG_WEB, translate("offer.catalog.web")));
			}
		}
		catalogEl = uifactory.addCheckboxesVertical("offer.catalog", formLayout, catalogSV.keys(), catalogSV.values(), 1);
		if (catalogEl.getKeys().contains(CATALOG_OO)) {
			catalogEl.select(CATALOG_OO, link.getOffer() != null && link.getOffer().isCatalogPublish());
		}
		if (catalogEl.getKeys().contains(CATALOG_WEB)) {
			catalogEl.select(CATALOG_WEB, link.getOffer() != null && link.getOffer().isCatalogWebPublish());
		}
		catalogEl.setVisible(catalogInfo.isCatalogSupported() && !catalogEl.getKeys().isEmpty());
		
		if (catalogEl.isVisible() && catalogInfo.isShowDetails()) {
			uifactory.addStaticTextElement("access.info.catalog.entries", catalogInfo.getDetails(), formLayout);
		}
		
		String[] onValues = new String[] { translate("on") };
		confirmationEmailEl = uifactory.addCheckboxesHorizontal("confirmation.email", formLayout, onKeys, onValues);
		confirmationEmailEl.select(onKeys[0], link.getOffer() != null && link.getOffer().isConfirmationEmail());
		confirmationEmailEl.setVisible(true);
		
		initCustomFormElements(formLayout);
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		buttonGroupLayout.setRootForm(mainForm);
		formLayout.add(buttonGroupLayout);

		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
		if(edit) {
			uifactory.addFormSubmitButton("save", buttonGroupLayout);
		} else {
			uifactory.addFormSubmitButton("create", buttonGroupLayout);
		}
		
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
	
	protected abstract void initCustomFormElements(FormItemContainer formLayout);
	
	protected abstract void updateCustomChanges();
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == periodEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	protected void updateUI() {
		boolean periodeDates = periodEl.isOneSelected() && PERIOD_DATE.equals(periodEl.getSelectedKey());
		datesEl.setVisible(periodeDates);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		datesEl.clearError();
		if (datesEl.getDate() != null && datesEl.getSecondDate() != null && datesEl.getDate().compareTo(datesEl.getSecondDate()) > 0) {
			datesEl.setErrorKey("date.error", null);
			allOk = false;
		}
		
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
	
	public OfferAccess getOfferAccess() {
		Offer offer = link.getOffer();
		offer.setDescription(descEl.getValue());
		boolean hasDate = periodEl.isKeySelected(PERIOD_DATE);
		Date validFrom = hasDate? datesEl.getDate(): null;
		Date validTo = hasDate? datesEl.getSecondDate(): null;
		offer.setValidFrom(validFrom);
		offer.setValidTo(validTo);
		offer.setCatalogPublish(catalogEl.isKeySelected(CATALOG_OO));
		offer.setCatalogWebPublish(catalogEl.isKeySelected(CATALOG_WEB));
		offer.setConfirmationEmail(confirmationEmailEl.isAtLeastSelected(1));
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
