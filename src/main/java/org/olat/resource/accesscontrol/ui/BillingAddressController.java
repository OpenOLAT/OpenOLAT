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
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.BillingAddress;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 Nov 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class BillingAddressController extends FormBasicController implements Controller {

	private BillingAddressForm billingAddressForm;

	private BillingAddress billingAddress;
	private final Organisation organisation;
	private final Identity addressIdentity;
	
	@Autowired
	private ACService acService;
	
	public BillingAddressController(UserRequest ureq, WindowControl wControl, BillingAddress billingAddress,
			Organisation organisation, Identity addressIdentity) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.billingAddress = billingAddress;
		this.organisation = organisation;
		this.addressIdentity = addressIdentity;
		
		initForm(ureq);
	}
	
	public BillingAddress getBillingAddress() {
		return billingAddress;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		billingAddressForm = new BillingAddressForm(ureq, getWindowControl(), mainForm, billingAddress);
		listenTo(billingAddressForm);
		formLayout.add(billingAddressForm.getInitialFormItem());
		
		FormLayoutContainer buttonsWrapperCont = FormLayoutContainer.createDefaultFormLayout("buttonsWrapper", getTranslator());
		formLayout.add("buttonsWrapper", buttonsWrapperCont);
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonLayout.setElementCssClass("o_sel_billing_address_buttons");
		buttonsWrapperCont.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		if (billingAddress == null) {
			billingAddress = acService.createBillingAddress(organisation, addressIdentity);
		}
		
		billingAddress.setIdentifier(billingAddressForm.getIdentifier());
		billingAddress.setNameLine1(billingAddressForm.getNameLine1());
		billingAddress.setNameLine2(billingAddressForm.getNameLine2());
		billingAddress.setAddressLine1(billingAddressForm.getAddressLine1());
		billingAddress.setAddressLine2(billingAddressForm.getAddressLine2());
		billingAddress.setAddressLine3(billingAddressForm.getAddressLine3());
		billingAddress.setAddressLine4(billingAddressForm.getAddressLine4());
		billingAddress.setPoBox(billingAddressForm.getPoBox());
		billingAddress.setRegion(billingAddressForm.getRegion());
		billingAddress.setZip(billingAddressForm.getZip());
		billingAddress.setCity(billingAddressForm.getCity());
		billingAddress.setCountry(billingAddressForm.getCountry());
		
		billingAddress = acService.updateBillingAddress(billingAddress);
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

}
