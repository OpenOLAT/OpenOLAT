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
package org.olat.repository.ui.author;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationNameComparator;
import org.olat.core.id.Roles;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAllowToLeaveOptions;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.ui.AccessConfigurationController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 Nov 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthoringEditAccessAndBookingController extends FormBasicController {
	
	private static final String[] leaveKeys = new String[]{
			RepositoryEntryAllowToLeaveOptions.atAnyTime.name(),
			RepositoryEntryAllowToLeaveOptions.afterEndDate.name(),
			RepositoryEntryAllowToLeaveOptions.never.name()
		};
	private static final String[] accessKey = new String[] { "private", "booking", "shared" };
	private static final String[] onKeys = new String[] { "on" };
		
	private SingleSelection leaveEl;
	private SingleSelection accessEl;
	private SingleSelection statusEl;
	private MultipleSelectionElement guestEl;
	private MultipleSelectionElement organisationsEl;
	private StaticTextElement explainGuestAccessEl;

	private AccessConfigurationController acCtr;
	
	private final boolean status;
	private final boolean embbeded;
	private final boolean guestSupported;
	private final boolean readOnly;
	private RepositoryEntry entry;
	private List<Organisation> repositoryEntryOrganisations;
	
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryHandlerFactory handlerFactory;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	
	public AuthoringEditAccessAndBookingController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean readOnly) {
		super(ureq, wControl, "acces_and_booking");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.readOnly = readOnly;
		embbeded = false;
		status = false;
		guestSupported = handlerFactory.getRepositoryHandler(entry).supportsGuest(entry);
		initForm(ureq);
		updateUI();
	}
	
	public AuthoringEditAccessAndBookingController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "acces_and_booking", rootForm);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.readOnly = false;
		embbeded = true;
		status = true;
		guestSupported = handlerFactory.getRepositoryHandler(entry).supportsGuest(entry);
		initForm(ureq);
		updateUI();
	}
	
	public boolean isBookable() {
		return accessEl.isOneSelected() && accessEl.isSelected(1);
	}
	
	public boolean isAllUsers() {
		return accessEl.isOneSelected() && accessEl.isSelected(2);
	}
	
	public boolean isGuests() {
		return accessEl.isOneSelected() && accessEl.isSelected(2)
				&& guestEl.isVisible() && guestEl.isAtLeastSelected(1);
	}
	
	/**
	 * Return the publication status
	 */
	public RepositoryEntryStatusEnum getEntryStatus() {
		return RepositoryEntryStatusEnum.valueOf(statusEl.getSelectedKey());
	}
	
	public RepositoryEntry getEntry() {
		return entry;
	}
	
	public RepositoryEntryAllowToLeaveOptions getSelectedLeaveSetting() {
		RepositoryEntryAllowToLeaveOptions setting;
		if(leaveEl.isOneSelected()) {
			setting = RepositoryEntryAllowToLeaveOptions.valueOf(leaveEl.getSelectedKey());
		} else {
			setting = RepositoryEntryAllowToLeaveOptions.atAnyTime;
		}
		return setting;
	}
	
	public List<Organisation> getSelectedOrganisations() {
		if(organisationsEl == null || !organisationsEl.isVisible()) {
			return repositoryEntryOrganisations;
		}
		
		List<Organisation> organisations = new ArrayList<>();

		Set<String> organisationKeys = organisationsEl.getKeys();
		Collection<String> selectedOrganisationKeys = organisationsEl.getSelectedKeys();

		Set<String> currentOrganisationKeys = new HashSet<>();
		for(Iterator<Organisation> it=organisations.iterator(); it.hasNext(); ) {
			String key = it.next().getKey().toString();
			currentOrganisationKeys.add(key);
			if(organisationKeys.contains(key) && !selectedOrganisationKeys.contains(key)) {
				it.remove();
			}
		}

		for(String selectedOrganisationKey:selectedOrganisationKeys) {
			if(!currentOrganisationKeys.contains(selectedOrganisationKey)) {
				Organisation organisation = organisationService.getOrganisation(new OrganisationRefImpl(Long.valueOf(selectedOrganisationKey)));
				if(organisation != null) {
					organisations.add(organisation);
				}
			}
		}

		return organisations;
	}
	
	public List<OfferAccess> getOfferAccess() {
		return acCtr.getOfferAccess();
	}
	
	public List<Offer> getDeletedOffers() {
		return acCtr.getDeletedOffers();
	}
	
	public boolean isSendConfirmationEmail() {
		return acCtr.isSendConfirmationEmail();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer accessCont = FormLayoutContainer.createDefaultFormLayout("access", getTranslator());
		accessCont.setRootForm(mainForm);
		formLayout.add("access", accessCont);
		
		UserSession usess = ureq.getUserSession();
		initFormOrganisations(accessCont, usess);
		organisationsEl.setVisible(organisationModule.isEnabled());
		organisationsEl.setEnabled(!readOnly);
		initStatus(accessCont);
		statusEl.setVisible(status);
		statusEl.setEnabled(!readOnly);
		
		String[] accessValues = new String[] {
				getAccessTranslatedValue("rentry.access.type.private", "rentry.access.type.private.explain", "o_icon_locked"),
				getAccessTranslatedValue("rentry.access.type.booking", "rentry.access.type.booking.explain", "o_icon_booking"),
				getAccessTranslatedValue("rentry.access.type.shared", "rentry.access.type.shared.explain", "o_icon_unlocked")
		};
		accessEl = uifactory.addRadiosVertical("entry.access.type", "rentry.access.type", accessCont, accessKey, accessValues);
		accessEl.addActionListener(FormEvent.ONCHANGE);
		accessEl.setElementCssClass("o_repo_with_explanation");
		accessEl.setEnabled(!readOnly);
		if(entry.isAllUsers()) {
			accessEl.select(accessKey[2], true);
		} else if(entry.isBookable()) {
			accessEl.select(accessKey[1], true);
		} else {
			accessEl.select(accessKey[0], true);
		}
		
		String explainAccess = "<i class='o_icon o_icon_warn'> </i> ".concat(translate("rentry.access.type.explain"));
		StaticTextElement explainAccessEl = uifactory.addStaticTextElement("rentry.access.type.explain", null, explainAccess, accessCont);
		explainAccessEl.setElementCssClass("o_repo_explanation");

		String[] guestValues = new String[] { translate("rentry.access.guest.on") };
		guestEl = uifactory.addCheckboxesHorizontal("entry.access.guest", "rentry.access.guest", accessCont, onKeys, guestValues);
		guestEl.setElementCssClass("o_sel_repositoryentry_access_guest o_repo_with_explanation");
		guestEl.setEnabled(!readOnly);
		
		if(entry.isGuests()) {
			guestEl.select(onKeys[0], true);
		}
		
		explainGuestAccessEl = uifactory.addStaticTextElement("rentry.access.guest.explain", null, explainAccess, accessCont);
		explainGuestAccessEl.setElementCssClass("o_repo_explanation");
		explainGuestAccessEl.setEnabled(!readOnly);

		boolean managedBookings = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.bookings);
		acCtr = new AccessConfigurationController(ureq, getWindowControl(), entry.getOlatResource(), entry.getDisplayname(),
				true, !managedBookings && !readOnly, mainForm);
		listenTo(acCtr);
		formLayout.add("bookings", acCtr.getInitialFormItem());

		FormLayoutContainer optionsCont = FormLayoutContainer.createDefaultFormLayout("otherOptions", getTranslator());
		optionsCont.setRootForm(mainForm);
		formLayout.add("options", optionsCont);
		initLeaveOption(optionsCont);

		if(!embbeded && !readOnly) {
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			buttonsCont.setRootForm(mainForm);
			formLayout.add("buttons", buttonsCont);
			uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
			uifactory.addFormSubmitButton("save", buttonsCont);
		}
	}
	
	private void updateUI() {
		boolean shared = accessEl.isSelected(2);
		guestEl.setVisible(shared  && guestSupported);
		explainGuestAccessEl.setVisible(shared && guestSupported);
		acCtr.getInitialFormItem().setVisible(accessEl.isSelected(1));
	}
	
	private String getAccessTranslatedValue(String i18nKey, String explanationI18nKey, String iconCssClass) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("<i class='o_icon o_icon-fq ").append(iconCssClass).append("'> </i> ")
		  .append(translate(i18nKey)).append(" <small>")
		  .append(translate(explanationI18nKey)).append("</small>");
		return sb.toString();
	}
	
	private void initStatus(FormItemContainer formLayout) {
		// make configuration read only when managed by external system
		final boolean managedAccess = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.access);
		final boolean closedOrDeleted = entry.getEntryStatus() == RepositoryEntryStatusEnum.closed
				|| entry.getEntryStatus() == RepositoryEntryStatusEnum.trash
				|| entry.getEntryStatus() == RepositoryEntryStatusEnum.deleted;
		
		String[] publishedKeys;
		String[] publishedValues;
		if(closedOrDeleted) {
			publishedKeys = new String[] {
					RepositoryEntryStatusEnum.preparation.name(), RepositoryEntryStatusEnum.review.name(),
					RepositoryEntryStatusEnum.coachpublished.name(), RepositoryEntryStatusEnum.published.name(),
					RepositoryEntryStatusEnum.closed.name(), RepositoryEntryStatusEnum.trash.name(),
					RepositoryEntryStatusEnum.deleted.name()
			};
			publishedValues = new String[] {
					translate("cif.status.preparation"), translate("cif.status.review"),
					translate("cif.status.coachpublished"), translate("cif.status.published"),
					translate("cif.status.closed"), translate("cif.status.trash"),
					translate("cif.status.deleted")
			};
		} else {
			publishedKeys = new String[] {
					RepositoryEntryStatusEnum.preparation.name(), RepositoryEntryStatusEnum.review.name(),
					RepositoryEntryStatusEnum.coachpublished.name(), RepositoryEntryStatusEnum.published.name()
			};
			publishedValues = new String[] {
					translate("cif.status.preparation"), translate("cif.status.review"),
					translate("cif.status.coachpublished"), translate("cif.status.published")
			};
		}
		statusEl = uifactory.addDropdownSingleselect("publishedStatus", "cif.publish", formLayout, publishedKeys, publishedValues, null);
		statusEl.setElementCssClass("o_sel_repositoryentry_access_publication");
		statusEl.setEnabled(!managedAccess && !closedOrDeleted);
		statusEl.select(entry.getStatus(), true);
	}
	
	private void initLeaveOption(FormItemContainer formLayout) {
		String[] leaveValues = new String[]{
				translate("rentry.leave.atanytime"),
				translate("rentry.leave.afterenddate"),
				translate("rentry.leave.never")
		};
		
		final boolean managedLeaving = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.membersmanagement);
		leaveEl = uifactory.addDropdownSingleselect("entry.leave", "rentry.leave.option", formLayout, leaveKeys, leaveValues, null);
		boolean found = false;
		for(String leaveKey:leaveKeys) {
			if(leaveKey.equals(entry.getAllowToLeaveOption().name())) {
				leaveEl.select(leaveKey, true);
				found = true;
			}
		}
		if(!found) {
			if(managedLeaving) {
				leaveEl.select(RepositoryEntryAllowToLeaveOptions.never.name(), true);
			} else {
				RepositoryEntryAllowToLeaveOptions defaultOption = repositoryModule.getAllowToLeaveDefaultOption();
				leaveEl.select(defaultOption.name(), true);
			}
		}
		leaveEl.setEnabled(!managedLeaving && !readOnly);
	}
	
	private void initFormOrganisations(FormItemContainer formLayout, UserSession usess) {
		Roles roles = usess.getRoles();
		List<Organisation> organisations = organisationService.getOrganisations(getIdentity(), roles,
				OrganisationRoles.administrator, OrganisationRoles.learnresourcemanager, OrganisationRoles.author);
		List<Organisation> organisationList = new ArrayList<>(organisations);

		List<Organisation> reOrganisations = repositoryService.getOrganisations(entry);
		repositoryEntryOrganisations = new ArrayList<>(reOrganisations);
		
		for(Organisation reOrganisation:reOrganisations) {
			if(reOrganisation != null && !organisationList.contains(reOrganisation)) {
				organisationList.add(reOrganisation);
			}
		}
		
		Collections.sort(organisationList, new OrganisationNameComparator(getLocale()));
		
		List<String> keyList = new ArrayList<>();
		List<String> valueList = new ArrayList<>();
		for(Organisation organisation:organisationList) {
			keyList.add(organisation.getKey().toString());
			valueList.add(organisation.getDisplayName());
		}
		organisationsEl = uifactory.addCheckboxesDropdown("organisations", "cif.organisations", formLayout,
				keyList.toArray(new String[keyList.size()]), valueList.toArray(new String[valueList.size()]),
				null, null);
		organisationsEl.setEnabled(!RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.organisations) && !readOnly);
		for(Organisation reOrganisation:reOrganisations) {
			if(keyList.contains(reOrganisation.getKey().toString())) {
				organisationsEl.select(reOrganisation.getKey().toString(), true);
			}
		}
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (organisationsEl != null) {
			organisationsEl.clearError();
			if(organisationsEl.isVisible() && !organisationsEl.isAtLeastSelected(1)) {
				organisationsEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		accessEl.clearError();
		if(!accessEl.isOneSelected()) {
			accessEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(accessEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	public RepositoryEntry commitChanges() {
		boolean bookable = isBookable();
		List<Organisation> organisations = getSelectedOrganisations();
		entry = repositoryManager.setAccess(entry,
				isAllUsers(), isGuests(), bookable,
				getSelectedLeaveSetting(), organisations);
		
		if(bookable) {
			acCtr.commitChanges();
		} else {
			acCtr.invalidateBookings();
		}
		return entry;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
