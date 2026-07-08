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
import org.olat.core.gui.components.date.OffsetDirection;
import org.olat.core.gui.components.date.RelativeDateElement;
import org.olat.core.gui.components.date.RelativeDateSelection;
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
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.repository.ExecutionPeriodRelativeDateContext;
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
	private RelativeDateElement fromDateRelEl;
	private SingleSelection untilModeEl;
	private DateChooser untilDateEl;
	private RelativeDateElement untilDateRelEl;
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
	private ExecutionPeriodRelativeDateContext relDateContext;
	
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
		this.relDateContext = ExecutionPeriodRelativeDateContext.of(getTranslator(), catalogInfo.getStartDate(), catalogInfo.getEndDate());
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
		boolean hasCustom = offer != null && ((offer.getValidStatus() != null && !offer.getValidStatus().isEmpty())
				|| offer.getValidFrom() != null || offer.getValidTo() != null || offer.getValidDateConfig() != null);
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

		OfferDateConfig dateConfig = offer != null ? offer.getValidDateConfig() : null;

		fromModeEl = uifactory.addButtonGroupSingleSelectHorizontal("offer.from", catalogCont, modeSV);
		fromModeEl.addActionListener(FormEvent.ONCHANGE);
		String fromMode = DATE_MODE_STATUS;
		if (dateConfig != null && dateConfig.getFromValue() != null) {
			fromMode = DATE_MODE_RELATIVE;
		} else if (offer != null && offer.getValidFrom() != null) {
			fromMode = DATE_MODE_ABSOLUTE;
		}
		fromModeEl.select(fromMode, true);

		fromDateEl = uifactory.addDateChooser("from_" + link.getKey(), null, offer != null ? offer.getValidFrom() : null, catalogCont);
		fromDateEl.setAriaLabel(translate("offer.from"));

		fromDateRelEl = uifactory.addRelativeDateElement("fromDateRel", null,
				catalogCont, getWindowControl(), relDateContext);
		fromDateRelEl.setAriaLabel(translate("offer.from.rel"));
		fromDateRelEl.setVisible(false);
		fromDateRelEl.addActionListener(FormEvent.ONCHANGE);
		if (dateConfig != null && dateConfig.getFromValue() != null) {
			fromDateRelEl.setValue(toRelativeDateSelection(dateConfig.getFromRef(), dateConfig.getFromUnit(), dateConfig.getFromValue()));
		}

		untilModeEl = uifactory.addButtonGroupSingleSelectHorizontal("offer.until", catalogCont, modeSV);
		untilModeEl.addActionListener(FormEvent.ONCHANGE);
		String untilMode = DATE_MODE_STATUS;
		if (dateConfig != null && dateConfig.getToValue() != null) {
			untilMode = DATE_MODE_RELATIVE;
		} else if (offer != null && offer.getValidTo() != null) {
			untilMode = DATE_MODE_ABSOLUTE;
		}
		untilModeEl.select(untilMode, true);

		untilDateEl = uifactory.addDateChooser("until_" + link.getKey(), null, offer != null ? offer.getValidTo() : null, catalogCont);
		untilDateEl.setAriaLabel(translate("offer.until"));

		untilDateRelEl = uifactory.addRelativeDateElement("untilDateRel", null,
				catalogCont, getWindowControl(), relDateContext);
		untilDateRelEl.setAriaLabel(translate("offer.until.rel"));
		untilDateRelEl.setVisible(false);
		untilDateRelEl.addActionListener(FormEvent.ONCHANGE);
		if (dateConfig != null && dateConfig.getToValue() != null) {
			untilDateRelEl.setValue(toRelativeDateSelection(dateConfig.getToRef(), dateConfig.getToUnit(), dateConfig.getToValue()));
		}

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
		} else if (source == fromDateRelEl) {
			if (fromDateRelEl.getValue() == null) {
				fromModeEl.select(DATE_MODE_STATUS, true);
				updateUI();
			} else {
				updateRelDateWarning(fromDateRelEl);
			}
		} else if (source == untilDateRelEl) {
			if (untilDateRelEl.getValue() == null) {
				untilModeEl.select(DATE_MODE_STATUS, true);
				updateUI();
			} else {
				updateRelDateWarning(untilDateRelEl);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
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
		fromDateRelEl.setVisible(customCondition && DATE_MODE_RELATIVE.equals(fromMode));
		if (fromDateRelEl.isVisible()) {
			updateRelDateWarning(fromDateRelEl);
		}

		untilModeEl.setVisible(customCondition);
		String untilMode = untilModeEl.isOneSelected() ? untilModeEl.getSelectedKey() : DATE_MODE_STATUS;
		untilDateEl.setVisible(customCondition && DATE_MODE_ABSOLUTE.equals(untilMode));
		untilDateRelEl.setVisible(customCondition && DATE_MODE_RELATIVE.equals(untilMode));
		if (untilDateRelEl.isVisible()) {
			updateRelDateWarning(untilDateRelEl);
		}
	}

	private void updateRelDateWarning(RelativeDateElement el) {
		el.clearWarning();
		RelativeDateSelection sel = el.getValue();
		if (sel == null) {
			return;
		}
		boolean missing = "BEGIN".equals(sel.getRefKey())
				? catalogInfo.getStartDate() == null
				: catalogInfo.getEndDate() == null;
		if (missing) {
			el.setWarningKey("offer.relative.date.missing");
		}
	}

	private Date resolveEffectiveDate(SingleSelection modeEl, DateChooser absoluteEl, RelativeDateElement relativeEl) {
		if (modeEl.isKeySelected(DATE_MODE_ABSOLUTE)) {
			return absoluteEl.getDate();
		}
		if (modeEl.isKeySelected(DATE_MODE_RELATIVE)) {
			RelativeDateSelection sel = relativeEl.getValue();
			if (sel == null) {
				return null;
			}
			RelativeDateConfig config = toRelativeDateConfig(sel);
			return config.ref().computeDate(catalogInfo.getStartDate(), catalogInfo.getEndDate(), config.unit(), config.value());
		}
		return null;
	}

	private RelativeDateSelection toRelativeDateSelection(OfferDateRef ref, OfferDateUnit unit, Integer value) {
		if (ref == null || unit == null) {
			return null;
		}
		if (unit == OfferDateUnit.SAME_DAY) {
			String refName = ref.name();
			String refKey = refName.substring(refName.indexOf('_') + 1);
			return new RelativeDateSelection(refKey, OffsetDirection.BEFORE, null, null, false);
		}
		String refName = ref.name();
		int underscore = refName.indexOf('_');
		OffsetDirection direction = OffsetDirection.valueOf(refName.substring(0, underscore));
		String refKey = refName.substring(underscore + 1);
		String unitKey = unit.name();
		return new RelativeDateSelection(refKey, direction, unitKey, value, true);
	}

	private record RelativeDateConfig(OfferDateRef ref, OfferDateUnit unit, int value) {}

	private RelativeDateConfig toRelativeDateConfig(RelativeDateSelection sel) {
		if (!sel.isOffsetEnabled()) {
			OfferDateRef ref = "BEGIN".equals(sel.getRefKey()) ? OfferDateRef.BEFORE_BEGIN : OfferDateRef.AFTER_END;
			return new RelativeDateConfig(ref, OfferDateUnit.SAME_DAY, 0);
		}
		return new RelativeDateConfig(
				OfferDateRef.valueOf(sel.getDirection().name() + "_" + sel.getRefKey()),
				OfferDateUnit.valueOf(sel.getUnitKey()),
				sel.getValue() != null ? sel.getValue() : 0);
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
			fromDateRelEl.clearError();
			untilDateEl.clearError();
			untilDateRelEl.clearError();

			if (fromDateEl.isVisible() && fromDateEl.getDate() == null) {
				fromDateEl.setErrorKey("form.legende.mandatory");
				allOk = false;
			}
			if (fromDateRelEl.isVisible() && fromDateRelEl.getValue() == null) {
				fromDateRelEl.setErrorKey("form.mandatory.hover");
				allOk = false;
			}

			if (untilDateEl.isVisible() && untilDateEl.getDate() == null) {
				untilDateEl.setErrorKey("form.legende.mandatory");
				allOk = false;
			}
			if (untilDateRelEl.isVisible() && untilDateRelEl.getValue() == null) {
				untilDateRelEl.setErrorKey("form.mandatory.hover");
				allOk = false;
			}

			Date resolvedFrom = resolveEffectiveDate(fromModeEl, fromDateEl, fromDateRelEl);
			Date resolvedUntil = resolveEffectiveDate(untilModeEl, untilDateEl, untilDateRelEl);
			if (resolvedFrom != null && resolvedUntil != null && resolvedFrom.compareTo(resolvedUntil) > 0) {
				if (fromModeEl.isKeySelected(DATE_MODE_ABSOLUTE)) {
					fromDateEl.setErrorKey("form.error.first.after.second.date");
				} else {
					fromDateRelEl.setErrorKey("form.error.first.after.second.date");
				}
				allOk = false;
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
			} else if (fromModeEl.isKeySelected(DATE_MODE_RELATIVE) && fromDateRelEl.getValue() != null) {
				dateConfig = new OfferDateConfig();
				RelativeDateConfig fromConfig = toRelativeDateConfig(fromDateRelEl.getValue());
				dateConfig.setFromRef(fromConfig.ref());
				dateConfig.setFromUnit(fromConfig.unit());
				dateConfig.setFromValue(fromConfig.value());
			}
			if (untilModeEl.isKeySelected(DATE_MODE_ABSOLUTE)) {
				validTo = untilDateEl.getDate();
			} else if (untilModeEl.isKeySelected(DATE_MODE_RELATIVE) && untilDateRelEl.getValue() != null) {
				if (dateConfig == null) {
					dateConfig = new OfferDateConfig();
				}
				RelativeDateConfig untilConfig = toRelativeDateConfig(untilDateRelEl.getValue());
				dateConfig.setToRef(untilConfig.ref());
				dateConfig.setToUnit(untilConfig.unit());
				dateConfig.setToValue(untilConfig.value());
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
