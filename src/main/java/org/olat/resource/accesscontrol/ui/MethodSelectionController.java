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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.components.SimpleFormErrorTextItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.provider.free.FreeAccessHandler;
import org.olat.resource.accesscontrol.provider.invoice.InvoiceAccessHandler;
import org.olat.resource.accesscontrol.provider.paypal.PaypalAccessHandler;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutAccessHandler;
import org.olat.resource.accesscontrol.provider.token.TokenAccessHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 Sep 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MethodSelectionController extends FormBasicController {
	
	public final static String KEY_OPEN_ACCESS = "open.access";
	public final static String KEY_GUEST_ACCESS = "guest.access";

	private SingleSelection membershipNoneEl;
	private SingleSelection membershipEl;
	private SingleSelection memebershipPayEl;
	private SimpleFormErrorTextItem errorEl;
	
	private final boolean openAccessSupported;
	private final boolean guestSupported;
	private final List<AccessMethod> methods;

	@Autowired
	private AccessControlModule acModule;

	public MethodSelectionController(UserRequest ureq, WindowControl wControl, boolean openAccessSupported, boolean guestSupported, List<AccessMethod> methods) {
		super(ureq, wControl, "method_selection");
		this.openAccessSupported = openAccessSupported;
		this.guestSupported = guestSupported;
		this.methods = methods;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (guestSupported || openAccessSupported) {
			SelectionValues membershipNoneSV = new SelectionValues();
			
			if (guestSupported) {
				membershipNoneSV.add(new SelectionValue(KEY_GUEST_ACCESS, translate("create.offer.guest"),
						translate("create.offer.guest.description"), "o_icon o_ac_guests_icon", null, true));
			}
			
			if (openAccessSupported) {
				membershipNoneSV.add(new SelectionValue(KEY_OPEN_ACCESS, translate("create.offer.open"),
						translate("create.offer.open.description"), "o_icon o_ac_openaccess_icon", null, true));
			}
			
			membershipNoneEl = uifactory.addCardSingleSelectHorizontal("create.offer.membership.none", null, formLayout,
					membershipNoneSV.keys(), membershipNoneSV.values(), membershipNoneSV.descriptions(), membershipNoneSV.icons());
			membershipNoneEl.setAllowNoSelection(true);
			membershipNoneEl.addActionListener(FormEvent.ONCHANGE);
		}
		
		Map<String, AccessMethod> typeToMethod = methods.stream().collect(Collectors.toMap(AccessMethod::getType, Function.identity()));
		
		SelectionValues membershipSV = new SelectionValues();
		appendMethod(membershipSV, typeToMethod, FreeAccessHandler.METHOD_TYPE);
		appendMethod(membershipSV, typeToMethod, TokenAccessHandler.METHOD_TYPE);
		if (!membershipSV.isEmpty()) {
			membershipEl = uifactory.addCardSingleSelectHorizontal("create.offer.membership", null, formLayout,
					membershipSV.keys(), membershipSV.values(), membershipSV.descriptions(), membershipSV.icons());
			membershipEl.setAllowNoSelection(true);
			membershipEl.addActionListener(FormEvent.ONCHANGE);
		}
		
		SelectionValues membershipPaySV = new SelectionValues();
		appendMethod(membershipPaySV, typeToMethod, InvoiceAccessHandler.METHOD_TYPE);
		appendMethod(membershipPaySV, typeToMethod, PaypalCheckoutAccessHandler.METHOD_TYPE);
		appendMethod(membershipPaySV, typeToMethod, PaypalAccessHandler.METHOD_TYPE);
		if (!membershipPaySV.isEmpty()) {
			memebershipPayEl = uifactory.addCardSingleSelectHorizontal("create.offer.membership.pay", null, formLayout,
					membershipPaySV.keys(), membershipPaySV.values(), membershipPaySV.descriptions(), membershipPaySV.icons());
			memebershipPayEl.setAllowNoSelection(true);
			memebershipPayEl.addActionListener(FormEvent.ONCHANGE);
		}
		
		errorEl = uifactory.addErrorText("error", translate("create.offer.error.mandatory"), formLayout);
		errorEl.setVisible(false);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("create", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	private void appendMethod(SelectionValues selectionValues, Map<String, AccessMethod> typeToMethod, String type) {
		if (typeToMethod.containsKey(type)) {
			AccessMethod method = typeToMethod.get(type);
			AccessMethodHandler handler = acModule.getAccessMethodHandler(method.getType());
			selectionValues.add(new SelectionValue(
					handler.getType(),
					handler.getMethodName(getLocale()),
					handler.getDescription(getLocale()),
					"o_icon " + method.getMethodCssClass() + "_icon",
					null, true));
		}
	}

	public String getSelectedType() {
		if (membershipNoneEl != null && membershipNoneEl.isOneSelected()) {
			return membershipNoneEl.getSelectedKey();
		}
		if (membershipEl != null && membershipEl.isOneSelected()) {
			return membershipEl.getSelectedKey();
		}
		if (memebershipPayEl != null && memebershipPayEl.isOneSelected()) {
			return memebershipPayEl.getSelectedKey();
		}
		return null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == membershipNoneEl) {
			updateUI(membershipNoneEl);
		} else if (source == membershipEl) {
			updateUI(membershipEl);
		} else if (source == memebershipPayEl) {
			updateUI(memebershipPayEl);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void updateUI(SingleSelection selectedEl) {
		deselect(membershipNoneEl, selectedEl);
		deselect(membershipEl, selectedEl);
		deselect(memebershipPayEl, selectedEl);
	}

	private void deselect(SingleSelection toDeselectEl, SingleSelection selectedEl) {
		if (toDeselectEl != null && toDeselectEl.isOneSelected() && toDeselectEl != selectedEl) {
			toDeselectEl.select(toDeselectEl.getSelectedKey(), false);
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		errorEl.setVisible(false);
		if (getSelectedType() == null) {
			errorEl.setVisible(true);
			allOk &= false;
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

}
