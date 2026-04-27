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
import java.util.HashSet;
import java.util.List;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.CatalogInfo;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.OfferDateConfig;
import org.olat.resource.accesscontrol.OfferDateRef;
import org.olat.resource.accesscontrol.OfferDateUnit;
import org.olat.user.ui.organisation.OrganisationSelectionSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial Date:  18 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public abstract class AbstractConfigurationMethodController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	private static final String PERIOD_STATUS = "status";
	private static final String PERIOD_CUSTOM = "custom";
	private static final String DATE_MODE_STATUS = "status";
	private static final String DATE_MODE_ABSOLUTE = "absolute";
	private static final String DATE_MODE_RELATIVE = "relative";
	private static final String CATALOG_OO = "openolat";
	private static final String CATALOG_WEB = "web";
	protected static final String CONFIRMATION_BY_MANAGER_NO = "confirmation.by.manager.no";
	protected static final String CONFIRMATION_BY_MANAGER_YES = "confirmation.by.manager.yes";
	
	protected TextElement descEl;
	private TextElement labelEl;
	private SingleSelection periodEl;
	private StaticTextElement conditionHeaderEl;
	private MultipleSelectionElement statusEl;
	private SingleSelection fromModeEl;
	private DateChooser fromDateEl;
	private FormLayoutContainer fromRelCont;
	private TextElement fromValueEl;
	private SingleSelection fromUnitEl;
	private SingleSelection fromRefEl;
	private SingleSelection untilModeEl;
	private DateChooser untilDateEl;
	private FormLayoutContainer untilRelCont;
	private TextElement untilValueEl;
	private SingleSelection untilUnitEl;
	private SingleSelection untilRefEl;
	private ObjectSelectionElement organisationsEl;
	private MultipleSelectionElement catalogEl;
	protected SingleSelection confirmationByManagerEl;
	private MultipleSelectionElement confirmationEmailEl;

	protected final OfferAccess link;
	private final boolean offerOrganisationsSupported;
	private final Collection<Organisation> offerOrganisations;
	private final boolean confirmationByManagerSupported;
	protected final CatalogInfo catalogInfo;
	private final boolean edit;
	
	@Autowired
	protected ACService acService;
	@Autowired
	private CatalogV2Module catalogModule;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	
	public AbstractConfigurationMethodController(UserRequest ureq, WindowControl wControl, OfferAccess link,
			boolean offerOrganisationsSupported, Collection<Organisation> offerOrganisations,
			boolean confirmationByManagerSupported, CatalogInfo catalogInfo, boolean edit) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(AbstractConfigurationMethodController.class, getLocale(), getTranslator()));
		this.link = link;
		this.offerOrganisationsSupported = offerOrganisationsSupported;
		this.offerOrganisations = offerOrganisations;
		this.confirmationByManagerSupported = confirmationByManagerSupported;
		this.catalogInfo = catalogInfo;
		this.edit = edit;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer generalCont = FormLayoutContainer.createDefaultFormLayout("generalCont", getTranslator());
		generalCont.setRootForm(mainForm);
		formLayout.add(generalCont);

		String label = link.getOffer() != null ? link.getOffer().getLabel() : null;
		if (label == null && !edit && link.getOffer() != null) {
			OLATResource resource = link.getOffer().getResource();
			List<Offer> existingOffers = acService.findOfferByResource(resource, true, null, null);
			label = String.format("O-%02d", existingOffers.size() + 1);
		}
		labelEl = uifactory.addTextElement("offer.label", "offer.internal.label", 128, label, generalCont);
		labelEl.setElementCssClass("o_sel_accesscontrol_label");

		initCustomFormElements(generalCont);

		String desc = null;
		if(link.getOffer() != null) {
			desc = link.getOffer().getDescription();
		}
		descEl = uifactory.addTextAreaElement("offer-desc", "offer.description", 2000, 6, 80, false, false, desc, generalCont);
		descEl.setElementCssClass("o_sel_accesscontrol_description");
		descEl.setHelpTextKey("offer.description.help", null);

		FormLayoutContainer catalogCont = FormLayoutContainer.createDefaultFormLayout("catalogCont", getTranslator());
		catalogCont.setFormTitle(translate("offer.catalog.title"));
		catalogCont.setRootForm(mainForm);
		formLayout.add(catalogCont);

		SelectionValues catalogSV = new SelectionValues();
		if (catalogModule.isEnabled()) {
			catalogSV.add(SelectionValues.entry(CATALOG_OO, translate("offer.publish.in.intern"), null, "o_icon o_icon-fw o_icon_catalog_intern", null, true));
			if (catalogModule.isWebPublishEnabled()) {
				catalogSV.add(SelectionValues.entry(CATALOG_WEB, translate("offer.publish.in.extern"), null, "o_icon o_icon-fw o_icon_catalog_extern", null, true));
			}
		}
		catalogEl = uifactory.addCheckboxesButtonGroup("offer.publish.in", "offer.publish.in", catalogCont, catalogSV);
		catalogEl.setElementCssClass("o_sel_accesscontrol_publish");
		if (catalogEl.getKeys().contains(CATALOG_OO)) {
			catalogEl.select(CATALOG_OO, link.getOffer() != null && link.getOffer().isCatalogPublish());
		}
		if (catalogEl.getKeys().contains(CATALOG_WEB)) {
			catalogEl.select(CATALOG_WEB, link.getOffer() != null && link.getOffer().isCatalogWebPublish());
		}
		catalogEl.setVisible(catalogInfo.isCatalogSupported() && !catalogEl.getKeys().isEmpty());
		catalogEl.addActionListener(FormEvent.ONCHANGE);

		if (organisationModule.isEnabled() && offerOrganisationsSupported) {
			Collection<? extends OrganisationRef> selectedOrganisations = offerOrganisations != null? offerOrganisations: List.of();
			OrganisationSelectionSource organisationSource = new OrganisationSelectionSource(
					selectedOrganisations,
					() -> acService.getSelectionOfferOrganisations(getIdentity()));
			organisationsEl = uifactory.addObjectSelectionElement("organisations", "offer.released.for", catalogCont,
					getWindowControl(), true, organisationSource);
			organisationsEl.setMandatory(true);
		}

		SelectionValues periodSV = new SelectionValues();
		periodSV.add(SelectionValues.entry(PERIOD_STATUS, catalogInfo.getStatusPeriodOption()));
		periodSV.add(SelectionValues.entry(PERIOD_CUSTOM, translate("offer.available.custom.condition")));
		periodEl = uifactory.addRadiosVertical("offer.available.if", catalogCont, periodSV.keys(), periodSV.values());
		periodEl.addActionListener(FormEvent.ONCHANGE);
		Offer offer = link.getOffer();
		boolean hasCustom = offer != null && offer.getValidStatus() != null && !offer.getValidStatus().isEmpty();
		periodEl.select(hasCustom ? PERIOD_CUSTOM : PERIOD_STATUS, true);

		conditionHeaderEl = uifactory.addStaticTextElement("offer.conditions.met", "offer.conditions.met",
				null, catalogCont);

		SelectionValues availableStatuses = catalogInfo.getAvailableStatuses();
		if (availableStatuses != null && !availableStatuses.isEmpty()) {
			statusEl = uifactory.addCheckboxesButtonGroup("offer.status.is", "offer.status.is", catalogCont, availableStatuses);
			if (offer != null && offer.getValidStatus() != null) {
				for (String key : offer.getValidStatus()) {
					if (statusEl.getKeys().contains(key)) {
						statusEl.select(key, true);
					}
				}
			}
		}

		SelectionValues modeSV = new SelectionValues();
		modeSV.add(SelectionValues.entry(DATE_MODE_STATUS, translate("offer.date.mode.status")));
		modeSV.add(SelectionValues.entry(DATE_MODE_ABSOLUTE, translate("offer.date.mode.absolute")));
		modeSV.add(SelectionValues.entry(DATE_MODE_RELATIVE, translate("offer.date.mode.relative")));

		SelectionValues unitSV = new SelectionValues();
		unitSV.add(SelectionValues.entry(OfferDateUnit.SAME_DAY.name(), translate("offer.unit.same.day")));
		unitSV.add(SelectionValues.entry(OfferDateUnit.DAYS.name(), translate("unit.days")));
		unitSV.add(SelectionValues.entry(OfferDateUnit.WEEKS.name(), translate("unit.weeks")));
		unitSV.add(SelectionValues.entry(OfferDateUnit.MONTHS.name(), translate("unit.months")));
		unitSV.add(SelectionValues.entry(OfferDateUnit.YEARS.name(), translate("unit.years")));

		SelectionValues refSV = new SelectionValues();
		for (OfferDateRef ref : OfferDateRef.values()) {
			refSV.add(SelectionValues.entry(ref.name(), translate("offer.relative." + ref.i18nSuffix())));
		}

		OfferDateConfig dateConfig = offer != null ? offer.getValidDateConfig() : null;

		fromModeEl = uifactory.addRadiosHorizontal("offer.from", "offer.from", catalogCont, modeSV.keys(), modeSV.values());
		fromModeEl.addActionListener(FormEvent.ONCHANGE);
		String fromMode = DATE_MODE_STATUS;
		if (dateConfig != null && dateConfig.getFromValue() != null) {
			fromMode = DATE_MODE_RELATIVE;
		} else if (offer != null && offer.getValidFrom() != null) {
			fromMode = DATE_MODE_ABSOLUTE;
		}
		fromModeEl.select(fromMode, true);

		fromDateEl = uifactory.addDateChooser("from_" + link.getKey(), null, offer != null ? offer.getValidFrom() : null, catalogCont);

		fromRelCont = uifactory.addInlineFormLayout("fromRel", null, catalogCont);
		fromValueEl = uifactory.addTextElement("from.value", null, 6, "", fromRelCont);
		fromValueEl.setDisplaySize(6);
		fromUnitEl = uifactory.addDropdownSingleselect("from.unit", null, fromRelCont, unitSV.keys(), unitSV.values());
		fromUnitEl.addActionListener(FormEvent.ONCHANGE);
		fromRefEl = uifactory.addDropdownSingleselect("from.ref", null, fromRelCont, refSV.keys(), refSV.values());
		fromRefEl.addActionListener(FormEvent.ONCHANGE);
		uifactory.addStaticTextElement("from.period", null, translate("offer.relative.execution.period"), fromRelCont);
		if (dateConfig != null && dateConfig.getFromValue() != null) {
			fromValueEl.setValue(dateConfig.getFromValue().toString());
			if (dateConfig.getFromUnit() != null && unitSV.containsKey(dateConfig.getFromUnit().name())) {
				fromUnitEl.select(dateConfig.getFromUnit().name(), true);
			}
			if (dateConfig.getFromRef() != null && refSV.containsKey(dateConfig.getFromRef().name())) {
				fromRefEl.select(dateConfig.getFromRef().name(), true);
			}
		}
		if (!fromUnitEl.isOneSelected()) {
			fromUnitEl.select(OfferDateUnit.DAYS.name(), true);
		}
		if (!fromRefEl.isOneSelected()) {
			fromRefEl.select(OfferDateRef.BEFORE_BEGIN.name(), true);
		}
		updateRelDateWarning(fromRefEl, fromRelCont);

		untilModeEl = uifactory.addRadiosHorizontal("offer.until", "offer.until", catalogCont, modeSV.keys(), modeSV.values());
		untilModeEl.addActionListener(FormEvent.ONCHANGE);
		String untilMode = DATE_MODE_STATUS;
		if (dateConfig != null && dateConfig.getToValue() != null) {
			untilMode = DATE_MODE_RELATIVE;
		} else if (offer != null && offer.getValidTo() != null) {
			untilMode = DATE_MODE_ABSOLUTE;
		}
		untilModeEl.select(untilMode, true);

		untilDateEl = uifactory.addDateChooser("until_" + link.getKey(), null, offer != null ? offer.getValidTo() : null, catalogCont);

		untilRelCont = uifactory.addInlineFormLayout("untilRel", null, catalogCont);
		untilValueEl = uifactory.addTextElement("until.value", null, 6, "", untilRelCont);
		untilValueEl.setDisplaySize(6);
		untilUnitEl = uifactory.addDropdownSingleselect("until.unit", null, untilRelCont, unitSV.keys(), unitSV.values());
		untilUnitEl.addActionListener(FormEvent.ONCHANGE);
		untilRefEl = uifactory.addDropdownSingleselect("until.ref", null, untilRelCont, refSV.keys(), refSV.values());
		untilRefEl.addActionListener(FormEvent.ONCHANGE);
		uifactory.addStaticTextElement("until.period", null, translate("offer.relative.execution.period"), untilRelCont);
		if (dateConfig != null && dateConfig.getToValue() != null) {
			untilValueEl.setValue(dateConfig.getToValue().toString());
			if (dateConfig.getToUnit() != null && unitSV.containsKey(dateConfig.getToUnit().name())) {
				untilUnitEl.select(dateConfig.getToUnit().name(), true);
			}
			if (dateConfig.getToRef() != null && refSV.containsKey(dateConfig.getToRef().name())) {
				untilRefEl.select(dateConfig.getToRef().name(), true);
			}
		}
		if (!untilUnitEl.isOneSelected()) {
			untilUnitEl.select(OfferDateUnit.DAYS.name(), true);
		}
		if (!untilRefEl.isOneSelected()) {
			untilRefEl.select(OfferDateRef.AFTER_END.name(), true);
		}
		updateRelDateWarning(untilRefEl, untilRelCont);

		FormLayoutContainer membershipCont = FormLayoutContainer.createDefaultFormLayout("membershipCont", getTranslator());
		membershipCont.setFormTitle(translate("offer.membership.title"));
		membershipCont.setRootForm(mainForm);
		formLayout.add(membershipCont);

		if (confirmationByManagerSupported) {
			SelectionValues confirmationSV = new SelectionValues();
			confirmationSV.add(SelectionValues.entry(CONFIRMATION_BY_MANAGER_NO, translate("membership.confirmation.standard"), translate("membership.confirmation.standard.desc"), "o_icon o_ac_membership_standard_icon", null, true));
			confirmationSV.add(SelectionValues.entry(CONFIRMATION_BY_MANAGER_YES, translate("membership.confirmation.manager"), translate("membership.confirmation.manager.desc"), "o_icon o_ac_membership_confirmation_icon", null, true));
			confirmationByManagerEl = uifactory.addCardSingleSelectHorizontal("membership.confirmation", "membership.confirmation", membershipCont, confirmationSV);
			confirmationByManagerEl.setElementCssClass("o_sel_accesscontrol_confirmation_manager");
			confirmationByManagerEl.addActionListener(FormEvent.ONCHANGE);
			if (link.getOffer() != null && link.getOffer().isConfirmationByManagerRequired()) {
				confirmationByManagerEl.select(CONFIRMATION_BY_MANAGER_YES, true);
			} else {
				confirmationByManagerEl.select(CONFIRMATION_BY_MANAGER_NO, true);
			}
		}

		confirmationEmailEl = uifactory.addCheckboxesHorizontal("booking.receipt", membershipCont, onKeys,
				new String[] { translate("booking.receipt.option") });
		confirmationEmailEl.select(onKeys[0], link.getOffer() != null && link.getOffer().isConfirmationEmail());

		initCustomMembershipElements(membershipCont);

		FormLayoutContainer buttonsWrapperCont = FormLayoutContainer.createDefaultFormLayout("buttonsWrapper", getTranslator());
		buttonsWrapperCont.setElementCssClass("o_sel_accesscontrol_buttons");
		buttonsWrapperCont.setRootForm(mainForm);
		formLayout.add(buttonsWrapperCont);
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		buttonGroupLayout.setRootForm(mainForm);
		buttonsWrapperCont.add(buttonGroupLayout);

		if(edit) {
			uifactory.addFormSubmitButton("save", buttonGroupLayout);
		} else {
			uifactory.addFormSubmitButton("create", buttonGroupLayout);
		}
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());

		updateUI();
	}
	
	/**
	 * @param formLayout the container to place the elements
	 */
	protected void initCustomMembershipElements(FormItemContainer formLayout) {
		//
	}
	
	/**
	 * @param formLayout the container to place the elements
	 */
	protected void initCustomFormElements(FormItemContainer formLayout) {
		//
	}
	
	protected void updateCustomChanges() {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == periodEl) {
			updateUI();
		} else if (source == catalogEl) {
			updateCatalogUI();
		} else if (source == fromModeEl || source == untilModeEl) {
			updateUI();
		} else if (source == fromUnitEl) {
			if (fromUnitEl.isOneSelected() && OfferDateUnit.SAME_DAY.name().equals(fromUnitEl.getSelectedKey())) {
				fromValueEl.setValue("");
			}
			updateUI();
		} else if (source == untilUnitEl) {
			if (untilUnitEl.isOneSelected() && OfferDateUnit.SAME_DAY.name().equals(untilUnitEl.getSelectedKey())) {
				untilValueEl.setValue("");
			}
			updateUI();
		} else if (source == fromRefEl) {
			updateRelDateWarning(fromRefEl, fromRelCont);
		} else if (source == untilRefEl) {
			updateRelDateWarning(untilRefEl, untilRelCont);
		}
		super.formInnerEvent(ureq, source, event);
	}

	protected void updateUI() {
		boolean customCondition = periodEl.isOneSelected() && PERIOD_CUSTOM.equals(periodEl.getSelectedKey());

		conditionHeaderEl.setVisible(customCondition);
		if (statusEl != null) {
			statusEl.setVisible(customCondition);
			if (customCondition && !statusEl.isAtLeastSelected(1) && catalogInfo.getDefaultStatuses() != null) {
				for (String key : catalogInfo.getDefaultStatuses()) {
					if (statusEl.getKeys().contains(key)) {
						statusEl.select(key, true);
					}
				}
			}
		}

		fromModeEl.setVisible(customCondition);
		String fromMode = fromModeEl.isOneSelected() ? fromModeEl.getSelectedKey() : DATE_MODE_STATUS;
		fromDateEl.setVisible(customCondition && DATE_MODE_ABSOLUTE.equals(fromMode));
		fromRelCont.setVisible(customCondition && DATE_MODE_RELATIVE.equals(fromMode));

		untilModeEl.setVisible(customCondition);
		String untilMode = untilModeEl.isOneSelected() ? untilModeEl.getSelectedKey() : DATE_MODE_STATUS;
		untilDateEl.setVisible(customCondition && DATE_MODE_ABSOLUTE.equals(untilMode));
		untilRelCont.setVisible(customCondition && DATE_MODE_RELATIVE.equals(untilMode));

		fromValueEl.setEnabled(!fromUnitEl.isOneSelected() || !OfferDateUnit.SAME_DAY.name().equals(fromUnitEl.getSelectedKey()));
		untilValueEl.setEnabled(!untilUnitEl.isOneSelected() || !OfferDateUnit.SAME_DAY.name().equals(untilUnitEl.getSelectedKey()));
	}

	private void updateRelDateWarning(SingleSelection refEl, FormLayoutContainer relCont) {
		if (!refEl.isOneSelected()) {
			relCont.setWarningKey(null);
			return;
		}
		OfferDateRef ref = OfferDateRef.valueOf(refEl.getSelectedKey());
		boolean missing = switch (ref) {
			case BEFORE_BEGIN, AFTER_BEGIN -> !catalogInfo.isStartDateAvailable();
			case BEFORE_END, AFTER_END -> !catalogInfo.isEndDateAvailable();
		};
		relCont.setWarningKey(missing ? "offer.relative.date.missing" : null);
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

		if (periodEl.isOneSelected() && PERIOD_CUSTOM.equals(periodEl.getSelectedKey())) {
			if (statusEl != null) {
				statusEl.clearError();
				if (!statusEl.isAtLeastSelected(1)) {
					statusEl.setErrorKey("form.legende.mandatory");
					allOk = false;
				}
			}

			fromDateEl.clearError();
			untilDateEl.clearError();
			if (fromDateEl.isVisible() && untilDateEl.isVisible()
					&& fromDateEl.getDate() != null && untilDateEl.getDate() != null
					&& fromDateEl.getDate().compareTo(untilDateEl.getDate()) > 0) {
				fromDateEl.setErrorKey("form.error.first.after.second.date");
				allOk = false;
			}

			fromValueEl.clearError();
			if (fromRelCont.isVisible()
					&& fromUnitEl.isOneSelected()
					&& !OfferDateUnit.SAME_DAY.name().equals(fromUnitEl.getSelectedKey())) {
				if (!isValidPositiveInteger(fromValueEl.getValue())) {
					fromValueEl.setErrorKey("form.error.nointeger");
					allOk = false;
				}
			}

			untilValueEl.clearError();
			if (untilRelCont.isVisible()
					&& untilUnitEl.isOneSelected()
					&& !OfferDateUnit.SAME_DAY.name().equals(untilUnitEl.getSelectedKey())) {
				if (!isValidPositiveInteger(untilValueEl.getValue())) {
					untilValueEl.setErrorKey("form.error.nointeger");
					allOk = false;
				}
			}
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

	private boolean isValidPositiveInteger(String value) {
		if (!StringHelper.containsNonWhitespace(value)) {
			return false;
		}
		try {
			return Integer.parseInt(value.trim()) >= 0;
		} catch (NumberFormatException e) {
			return false;
		}
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
		offer.setDescription(descEl.isVisible()? descEl.getValue(): null);

		Date validFrom = null;
		Date validTo = null;
		OfferDateConfig dateConfig = null;

		if (periodEl.isKeySelected(PERIOD_CUSTOM)) {
			if (statusEl != null) {
				offer.setValidStatus(new HashSet<>(statusEl.getSelectedKeys()));
			}
			if (fromModeEl.isKeySelected(DATE_MODE_ABSOLUTE)) {
				validFrom = fromDateEl.getDate();
			} else if (fromModeEl.isKeySelected(DATE_MODE_RELATIVE)) {
				dateConfig = new OfferDateConfig();
				boolean sameDayFrom = fromUnitEl.isOneSelected() && OfferDateUnit.SAME_DAY.name().equals(fromUnitEl.getSelectedKey());
				dateConfig.setFromValue(sameDayFrom ? 0 : parseIntValue(fromValueEl.getValue()));
				dateConfig.setFromUnit(fromUnitEl.isOneSelected() ? OfferDateUnit.valueOf(fromUnitEl.getSelectedKey()) : OfferDateUnit.DAYS);
				dateConfig.setFromRef(fromRefEl.isOneSelected() ? OfferDateRef.valueOf(fromRefEl.getSelectedKey()) : OfferDateRef.BEFORE_BEGIN);
			}
			if (untilModeEl.isKeySelected(DATE_MODE_ABSOLUTE)) {
				validTo = untilDateEl.getDate();
			} else if (untilModeEl.isKeySelected(DATE_MODE_RELATIVE)) {
				if (dateConfig == null) {
					dateConfig = new OfferDateConfig();
				}
				boolean sameDayUntil = untilUnitEl.isOneSelected() && OfferDateUnit.SAME_DAY.name().equals(untilUnitEl.getSelectedKey());
				dateConfig.setToValue(sameDayUntil ? 0 : parseIntValue(untilValueEl.getValue()));
				dateConfig.setToUnit(untilUnitEl.isOneSelected() ? OfferDateUnit.valueOf(untilUnitEl.getSelectedKey()) : OfferDateUnit.DAYS);
				dateConfig.setToRef(untilRefEl.isOneSelected() ? OfferDateRef.valueOf(untilRefEl.getSelectedKey()) : OfferDateRef.AFTER_END);
			}
		} else {
			offer.setValidStatus(null);
		}

		offer.setValidFrom(validFrom);
		offer.setValidTo(validTo);
		offer.setValidDateConfig(dateConfig);
		offer.setCatalogPublish(catalogEl.isKeySelected(CATALOG_OO));
		offer.setCatalogWebPublish(catalogEl.isKeySelected(CATALOG_WEB));
		offer.setConfirmationEmail(confirmationEmailEl.isAtLeastSelected(1));
		if (confirmationByManagerEl != null) {
			offer.setConfirmationByManagerRequired(confirmationByManagerEl.isOneSelected() && confirmationByManagerEl.isKeySelected(CONFIRMATION_BY_MANAGER_YES));
		}
		link.setValidFrom(validFrom);
		link.setValidTo(validTo);
		updateCustomChanges();
		return link;
	}

	private Integer parseIntValue(String value) {
		try {
			return StringHelper.containsNonWhitespace(value) ? Integer.parseInt(value.trim()) : 0;
		} catch (NumberFormatException e) {
			return 0;
		}
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
