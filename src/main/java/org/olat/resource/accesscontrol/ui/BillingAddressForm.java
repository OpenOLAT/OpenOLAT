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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.BillingAddress;
import org.olat.resource.accesscontrol.model.TransientBillingAddress;

/**
 * 
 * Initial date: 10 Feb 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class BillingAddressForm extends FormBasicController implements Controller {

	private TextElement identifierEl;
	private TextElement nameLine1El;
	private TextElement nameLine2El;
	private TextElement addressLine1El;
	private TextElement addressLine2El;
	private TextElement addressLine3El;
	private TextElement addressLine4El;
	private TextElement poBoxEl;
	private TextElement regionEl;
	private TextElement zipEl;
	private TextElement cityEl;
	private TextElement countryEl;
	
	private final BillingAddress billingAddress;
	private final Organisation addressOrganisation;
	private final Identity addressIdentitiy;
	
	public BillingAddressForm(UserRequest ureq, WindowControl wControl, Form form, BillingAddress billingAddress) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, form);
		this.billingAddress = billingAddress;
		this.addressOrganisation = billingAddress != null? billingAddress.getOrganisation(): null;
		this.addressIdentitiy = billingAddress != null? billingAddress.getIdentity(): null;
		
		initForm(ureq);
	}
	
	public BillingAddressForm(UserRequest ureq, WindowControl wControl, Form form, Organisation addressOrganisation,
			Identity addressIdentitiy) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, form);
		this.billingAddress = null;
		this.addressOrganisation = addressOrganisation;
		this.addressIdentitiy = addressIdentitiy;
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (billingAddress != null && billingAddress.getKey() != null && ureq.getUserSession().getRoles().isAdministrator()) {
			uifactory.addStaticTextElement("billing.address.id", billingAddress.getKey().toString(), formLayout);
		}
		
		String identifier = billingAddress != null? billingAddress.getIdentifier(): null;
		identifierEl = uifactory.addTextElement("billing.address.identifier", 255, identifier, formLayout);
		identifierEl.setMandatory(true);
		
		String nameLine1 = billingAddress != null? billingAddress.getNameLine1(): null;
		nameLine1El = uifactory.addTextElement("billing.address.name.line1", 255, nameLine1, formLayout);
		nameLine1El.setMandatory(true);
		
		String nameLine2 = billingAddress != null? billingAddress.getNameLine2(): null;
		nameLine2El = uifactory.addTextElement("billing.address.name.line2", 255, nameLine2, formLayout);
		
		String addressLine1 = billingAddress != null? billingAddress.getAddressLine1(): null;
		addressLine1El = uifactory.addTextElement("billing.address.address.line1", 255, addressLine1, formLayout);
		addressLine1El.setMandatory(true);
		
		String addressLine2 = billingAddress != null? billingAddress.getAddressLine2(): null;
		addressLine2El = uifactory.addTextElement("billing.address.address.line2", 255, addressLine2, formLayout);
		
		String addressLine3 = billingAddress != null? billingAddress.getAddressLine3(): null;
		addressLine3El = uifactory.addTextElement("billing.address.address.line3", 255, addressLine3, formLayout);
		
		String addressLine4 = billingAddress != null? billingAddress.getAddressLine4(): null;
		addressLine4El = uifactory.addTextElement("billing.address.address.line4", 255, addressLine4, formLayout);
		
		String poBox = billingAddress != null? billingAddress.getPoBox(): null;
		poBoxEl = uifactory.addTextElement("billing.address.pobox", 255, poBox, formLayout);
		
		String region = billingAddress != null? billingAddress.getRegion(): null;
		regionEl = uifactory.addTextElement("billing.address.region", 255, region, formLayout);
		
		String zip = billingAddress != null? billingAddress.getZip(): null;
		zipEl = uifactory.addTextElement("billing.address.zip", 255, zip, formLayout);
		
		String city = billingAddress != null? billingAddress.getCity(): null;
		cityEl = uifactory.addTextElement("billing.address.city", 255, city, formLayout);
		cityEl.setMandatory(true);
		
		String country = billingAddress != null? billingAddress.getCountry(): null;
		countryEl = uifactory.addTextElement("billing.address.country", 255, country, formLayout);
		countryEl.setMandatory(true);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= validateMandatory(identifierEl);
		allOk &= validateMandatory(nameLine1El);
		allOk &= validateMandatory(addressLine1El);
		allOk &= validateMandatory(cityEl);
		allOk &= validateMandatory(countryEl);
		
		return allOk;
	}
	
	private boolean validateMandatory(TextElement el) {
		boolean allOk = true;
		el.clearError();
		if (getInitialFormItem().isVisible() && el.isEnabled() && el.isVisible()) {
			String val = el.getValue();
			if (!StringHelper.containsNonWhitespace(val)) {
				el.setErrorKey("form.mandatory.hover");
				allOk = false;
			}
		}
		return allOk;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public void setIdentifierVisible(boolean visible) {
		identifierEl.setVisible(visible);
	}

	public String getIdentifier() {
		return identifierEl.getValue();
	}

	public String getNameLine1() {
		return nameLine1El.getValue();
	}

	public String getNameLine2() {
		return nameLine2El.getValue();
	}

	public String getAddressLine1() {
		return addressLine1El.getValue();
	}

	public String getAddressLine2() {
		return addressLine2El.getValue();
	}

	public String getAddressLine3() {
		return addressLine3El.getValue();
	}

	public String getAddressLine4() {
		return addressLine4El.getValue();
	}

	public String getPoBox() {
		return poBoxEl.getValue();
	}

	public String getRegion() {
		return regionEl.getValue();
	}

	public String getZip() {
		return zipEl.getValue();
	}

	public String getCity() {
		return cityEl.getValue();
	}

	public String getCountry() {
		return countryEl.getValue();
	}
	
	public TransientBillingAddress getTransientBillingAddress() {
		TransientBillingAddress transientAddress = new TransientBillingAddress();
		transientAddress.setIdentifier(getIdentifier());
		transientAddress.setNameLine1(getNameLine1());
		transientAddress.setNameLine2(getNameLine2());
		transientAddress.setAddressLine1(getAddressLine1());
		transientAddress.setAddressLine2(getAddressLine2());
		transientAddress.setAddressLine3(getAddressLine3());
		transientAddress.setAddressLine4(getAddressLine4());
		transientAddress.setPoBox(getPoBox());
		transientAddress.setRegion(getRegion());
		transientAddress.setZip(getZip());
		transientAddress.setCity(getCity());
		transientAddress.setCountry(getCountry());
		transientAddress.setOrganisation(addressOrganisation);
		transientAddress.setIdentity(addressIdentitiy);
		return transientAddress;
	}

}
