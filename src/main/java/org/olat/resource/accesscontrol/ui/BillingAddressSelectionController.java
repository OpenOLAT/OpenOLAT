/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.resource.accesscontrol.ui;

import java.util.List;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.BillingAddress;
import org.olat.resource.accesscontrol.BillingAddressSearchParams;
import org.springframework.beans.factory.annotation.Autowired;

import io.jsonwebtoken.lang.Arrays;

/**
 * 
 * Initial date: Feb 9, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class BillingAddressSelectionController extends FormBasicController {
	
	private static final String KEY_NEW_ADDRESS_ORGANISATION = "new.organisation";
	private static final String KEY_NEW_ADDRESS_USER = "new.user";
	
	private SingleSelection organisationAddressesEl;
	private SingleSelection userAddressesEl;
	
	private BillingAddressForm organisationAddressForm;
	private BillingAddressForm userAddressForm;

	private final Identity bookedIdentity;
	private final BillingAddress preselectedAddress;

	@Autowired
	private ACService acService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;

	public BillingAddressSelectionController(UserRequest ureq, WindowControl wControl, Identity bookedIdentity,
			BillingAddress preselectedAddress) {
		super(ureq, wControl, "billing_address_selection");
		this.bookedIdentity = bookedIdentity;
		this.preselectedAddress = preselectedAddress;
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (organisationModule.isEnabled()) {
			List<Organisation> organisations = organisationService.getOrganisations(bookedIdentity,
					OrganisationRoles.user);
			if (organisations != null && !organisations.isEmpty()) {
				BillingAddressSearchParams searchParams = new BillingAddressSearchParams();
				searchParams.setEnabled(Boolean.TRUE);
				searchParams.setOrganisations(organisations);
				
				SelectionValues organisationBillingAddressSV = new SelectionValues();
				List<BillingAddress> organsationAddresses = acService.getBillingAddresses(searchParams);
				organsationAddresses.forEach(address -> organisationBillingAddressSV
						.add(createSelectionValue(address)));
				organisationBillingAddressSV.sort(SelectionValues.VALUE_ASC);
				organisationBillingAddressSV.add( SelectionValues.entry(
						KEY_NEW_ADDRESS_ORGANISATION,
						translate("billing.address.create.organisation"),
						translate("billing.address.create.organisation.desc"),
						"o_icon o_icon_edit",
						null, true));
				
				organisationAddressesEl = uifactory.addCardSingleSelectHorizontal("organisation.addresses", null,
						formLayout, organisationBillingAddressSV);
				organisationAddressesEl.setAllowNoSelection(true);
				organisationAddressesEl.addActionListener(FormEvent.ONCHANGE);
				
				// Temporary address is displayed as organisation address
				if (preselectedAddress != null && preselectedAddress.getKey() == null && preselectedAddress.getIdentity() == null) {
					organisationAddressForm = new BillingAddressForm(ureq, getWindowControl(), mainForm, preselectedAddress);
				} else {
					organisationAddressForm = new BillingAddressForm(ureq, getWindowControl(), mainForm, null, null);
				}
				organisationAddressForm.setIdentifierVisible(false);
				listenTo(organisationAddressForm);
				formLayout.add("organsiation.address", organisationAddressForm.getInitialFormItem());
			}
		}

		BillingAddressSearchParams searchParams = new BillingAddressSearchParams();
		searchParams.setEnabled(Boolean.TRUE);
		searchParams.setIdentityKeys(List.of(bookedIdentity));

		SelectionValues userBillingAddressSV = new SelectionValues();
		acService.getBillingAddresses(searchParams).forEach(address -> userBillingAddressSV
				.add(createSelectionValue(address)));
		userBillingAddressSV.sort(SelectionValues.VALUE_ASC);
		userBillingAddressSV.add( SelectionValues.entry(
				KEY_NEW_ADDRESS_USER,
				translate("billing.address.create.user"),
				translate("billing.address.create.user.desc"),
				"o_icon o_icon_add",
				null, true));

		userAddressesEl = uifactory.addCardSingleSelectHorizontal("user.addresses", null, formLayout,
				userBillingAddressSV);
		userAddressesEl.setAllowNoSelection(true);
		userAddressesEl.addActionListener(FormEvent.ONCHANGE);
		
		if (preselectedAddress != null && preselectedAddress.getIdentity() != null && preselectedAddress.getKey() == null) {
			userAddressForm = new BillingAddressForm(ureq, getWindowControl(), mainForm, preselectedAddress);
		} else {
			userAddressForm = new BillingAddressForm(ureq, getWindowControl(), mainForm, null, bookedIdentity);
		}
		listenTo(userAddressForm);
		formLayout.add("user.address", userAddressForm.getInitialFormItem());
		
		// Init selection 
		// 1. Address is preselected
		if (preselectedAddress != null) {
			if (preselectedAddress.getKey() != null) {
				if (organisationAddressesEl != null && preselectedAddress.getOrganisation() != null && Arrays.asList(organisationAddressesEl.getKeys()).contains(preselectedAddress.getKey().toString())) {
					// 1.1 Organisation address is preselected
					organisationAddressesEl.select(preselectedAddress.getKey().toString(), true);
				} else if (preselectedAddress.getIdentity() != null && Arrays.asList(userAddressesEl.getKeys()).contains(preselectedAddress.getKey().toString())) {
					// 1.2 User address is preselected
					userAddressesEl.select(preselectedAddress.getKey().toString(), true);
				}
			} else {
				if (organisationAddressesEl != null && preselectedAddress.getIdentity() == null) {
					// 2.1 Select new organisation address
					organisationAddressesEl.select(KEY_NEW_ADDRESS_ORGANISATION, true);
				} else if (preselectedAddress.getIdentity() != null) {
					userAddressesEl.select(KEY_NEW_ADDRESS_USER, true);
				}
			}
		}
		if (!organisationAddressesEl.isOneSelected() && !userAddressesEl.isOneSelected()) {
			if (organisationAddressesEl != null && organisationAddressesEl.getKeys().length > 1) {
				// 3.1 Select first organisation address if any available
				organisationAddressesEl.select(organisationAddressesEl.getKey(0), true);
			} else if (userAddressesEl.getKeys().length > 1) {
				// 3.2 Select first user address if any available
				userAddressesEl.select(userAddressesEl.getKey(0), true);
			} else if (organisationAddressesEl != null) {
				// 4.1 Create new organisation address if module enabled
				organisationAddressesEl.select(KEY_NEW_ADDRESS_ORGANISATION, true);
			} else {
				// 4.2 Create new user address if module enabled
				userAddressesEl.select(KEY_NEW_ADDRESS_USER, true);
			}
		}
		if (organisationAddressForm != null) {
			organisationAddressForm.getInitialFormItem().setVisible(organisationAddressesEl.isOneSelected()
					&& KEY_NEW_ADDRESS_ORGANISATION.equals(organisationAddressesEl.getSelectedKey()));
			
		}
		userAddressForm.getInitialFormItem().setVisible(userAddressesEl.isOneSelected()
				&& KEY_NEW_ADDRESS_USER.equals(userAddressesEl.getSelectedKey()));

		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonLayout.setElementCssClass("o_block_top");
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("select", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	private SelectionValue createSelectionValue(BillingAddress address) {
		return SelectionValues.entry(
				address.getKey().toString(),
				StringHelper.escapeHtml(address.getIdentifier()),
				BillingAddressUIFactory.getFormattedAddress(address),
				"o_icon o_icon_billing_address",
				null, true);
	}
	
	private void updateUI(boolean organisationAddressSelected) {
		if (organisationAddressSelected) {
			if (organisationAddressesEl.isOneSelected()) {
				if (userAddressesEl.isOneSelected()) {
					userAddressesEl.select(userAddressesEl.getSelectedKey(), false);
				}
			}
		} else {
			if (userAddressesEl.isOneSelected()) {
				if (organisationAddressesEl.isOneSelected()) {
					organisationAddressesEl.select(organisationAddressesEl.getSelectedKey(), false);
				}
			}
		}
		
		if (organisationAddressForm != null) {
			organisationAddressForm.getInitialFormItem().setVisible(organisationAddressesEl.isOneSelected()
					&& organisationAddressesEl.isKeySelected(KEY_NEW_ADDRESS_ORGANISATION));
		}
		userAddressForm.getInitialFormItem().setVisible(userAddressesEl.isOneSelected()
				&& userAddressesEl.isKeySelected(KEY_NEW_ADDRESS_USER));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == organisationAddressesEl) {
			updateUI(true);
		} else if (source == userAddressesEl) {
			updateUI(false);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	public BillingAddress getBillingAddress() {
		if (organisationAddressesEl != null && organisationAddressesEl.isOneSelected()) {
			String selectedKey = organisationAddressesEl.getSelectedKey();
			if (KEY_NEW_ADDRESS_ORGANISATION.equals(selectedKey)) {
				return organisationAddressForm.getTransientBillingAddress();
			}
			return getBillingAddress(Long.valueOf(selectedKey));
		} else if (userAddressesEl != null && userAddressesEl.isOneSelected()) {
			String selectedKey = userAddressesEl.getSelectedKey();
			if (KEY_NEW_ADDRESS_USER.equals(selectedKey)) {
				return userAddressForm.getTransientBillingAddress();
			}
			return getBillingAddress(Long.valueOf(selectedKey));
		}
		return null;
	}

	private BillingAddress getBillingAddress(Long billingAddressKey) {
		BillingAddressSearchParams searchParams = new BillingAddressSearchParams();
		searchParams.setEnabled(Boolean.TRUE);
		searchParams.setBillingAddressKeys(List.of(billingAddressKey));
		List<BillingAddress> billingAddresses = acService.getBillingAddresses(searchParams);
		return !billingAddresses.isEmpty()? billingAddresses.get(0): null;
	}

}
