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
package org.olat.login.webauthn.ui;

import java.util.List;

import org.olat.admin.privacy.PrivacyAdminController;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.login.LoginModule;
import org.springframework.beans.factory.annotation.Autowired;

import com.webauthn4j.data.AttestationConveyancePreference;
import com.webauthn4j.data.UserVerificationRequirement;

/**
 * 
 * Initial date: 10 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WebAuthnAuthenticationAdminController extends FormBasicController {

	private FormToggle enabledEl;
	private SingleSelection attestationEl;
	private SingleSelection userVerificationEl;
	private MultipleSelectionElement removeOlatTokenEl;
	private MultipleSelectionElement mandatoryPasskeyForEl;
	
	@Autowired
	private LoginModule loginModule;
	
	public WebAuthnAuthenticationAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(PrivacyAdminController.class, ureq.getLocale()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("addmin.configuration.title");
		formLayout.setElementCssClass("o_sel_passkey_admin_configuration");
		
		enabledEl = uifactory.addToggleButton("enabled.passkey", "enabled.passkey", translate("on"), translate("off"), formLayout);
		enabledEl.addActionListener(FormEvent.ONCHANGE);
		if(loginModule.isOlatProviderWithPasskey()) {
			enabledEl.toggleOn();
		} else {
			enabledEl.toggleOff();
		}
		
		SelectionValues truePK = new SelectionValues();
		truePK.add(SelectionValues.entry("true", ""));
		removeOlatTokenEl = uifactory.addCheckboxesHorizontal("remove.olat.token", formLayout, truePK.keys(), truePK.values());
		removeOlatTokenEl.addActionListener(FormEvent.ONCHANGE);
		if(loginModule.isPasskeyRemoveOlatToken()) {
			removeOlatTokenEl.select("true", true);
		}
		
		SelectionValues userVerificationPK = new SelectionValues();
		userVerificationPK.add(SelectionValues.entry(UserVerificationRequirement.DISCOURAGED.getValue(), translate("user.verification.discouraged")));
		userVerificationPK.add(SelectionValues.entry(UserVerificationRequirement.PREFERRED.getValue(), translate("user.verification.preferred")));
		userVerificationPK.add(SelectionValues.entry(UserVerificationRequirement.REQUIRED.getValue(), translate("user.verification.required")));
		userVerificationEl = uifactory.addDropdownSingleselect("passkey.user.verification", formLayout,
				userVerificationPK.keys(), userVerificationPK.values());
		userVerificationEl.select(loginModule.getPasskeyUserVerification().getValue(), true);
		userVerificationEl.addActionListener(FormEvent.ONCHANGE);
		userVerificationEl.setVisible(enabledEl.isOn());
		
		SelectionValues attestationPK = new SelectionValues();
		attestationPK.add(SelectionValues.entry(AttestationConveyancePreference.NONE.getValue(), translate("attestation.none")));
		attestationPK.add(SelectionValues.entry(AttestationConveyancePreference.DIRECT.getValue(), translate("attestation.direct")));
		attestationPK.add(SelectionValues.entry(AttestationConveyancePreference.INDIRECT.getValue(), translate("attestation.indirect")));
		attestationPK.add(SelectionValues.entry(AttestationConveyancePreference.ENTERPRISE.getValue(), translate("attestation.interprise")));
		attestationEl = uifactory.addDropdownSingleselect("passkey.attestation", formLayout,
				attestationPK.keys(), attestationPK.values());
		attestationEl.select(loginModule.getPasskeyAttestationConveyancePreference().getValue(), true);
		attestationEl.addActionListener(FormEvent.ONCHANGE);
		attestationEl.setVisible(enabledEl.isOn());
		
		OrganisationRoles[] roles = BaseSecurityModule.getUserAllowedRoles();
		SelectionValues mandatoryPasskeyPK = new SelectionValues();
		for(int i=roles.length; i-->0; ) {
			mandatoryPasskeyPK.add(SelectionValues.entry(roles[i].name(), translate("admin.props." + roles[i].name() + "s")));
		}
		mandatoryPasskeyForEl = uifactory.addCheckboxesVertical("mandatory.passkey.for", formLayout, mandatoryPasskeyPK.keys(), mandatoryPasskeyPK.values(), 1);
		mandatoryPasskeyForEl.addActionListener(FormEvent.ONCHANGE);
		List<OrganisationRoles> mandatoryRoles = loginModule.getPasskeyMandatoryForRoles();
		for(OrganisationRoles mandatoryRole:mandatoryRoles) {
			mandatoryPasskeyForEl.select(mandatoryRole.name(), true);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enabledEl == source || userVerificationEl == source || attestationEl == source
				|| removeOlatTokenEl == source || mandatoryPasskeyForEl == source) {
			doSave();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave();
	}
	
	private void doSave() {
		boolean enabled = enabledEl.isOn();
		loginModule.setOlatProviderWithPasskey(enabled);
		
		if(enabled && userVerificationEl.isOneSelected()) {
			String selectedValue = UserVerificationRequirement.create(userVerificationEl.getSelectedKey()).getValue();
			loginModule.setPasskeyUserVerification(selectedValue);
		}
		
		if(enabled && attestationEl.isOneSelected()) {
			String selectedValue = AttestationConveyancePreference.create(attestationEl.getSelectedKey()).getValue();
			loginModule.setPasskeyAttestationConveyancePreference(selectedValue);
		}
		
		if(enabled) {
			loginModule.setPasskeyRemoveOlatToken(removeOlatTokenEl.isAtLeastSelected(1));
		}
		
		if(enabled) {
			String roles = String.join(",", mandatoryPasskeyForEl.getSelectedKeys());
			loginModule.setPasskeyMandatoryForRoles(roles);
		}
		
		removeOlatTokenEl.setVisible(enabled);
		userVerificationEl.setVisible(enabled);
		attestationEl.setVisible(enabled);
		removeOlatTokenEl.setVisible(enabled);
		mandatoryPasskeyForEl.setVisible(enabled);
	}
}
