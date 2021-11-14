/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.resource.accesscontrol.ui;

import java.util.Collection;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.provider.free.FreeAccessHandler;
import org.olat.resource.accesscontrol.provider.paypal.PaypalAccessHandler;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutAccessHandler;
import org.olat.resource.accesscontrol.provider.token.TokenAccessHandler;

/**
 *
 * Description:<br>
 *
 * <P>
 * Initial Date:  26 mai 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class AccessControlAdminController extends FormBasicController {

	private static final String METHOD_AUTO = "ac.method.auto.name";
	private MultipleSelectionElement enabled;
	private MultipleSelectionElement homeEnabled;
	private MultipleSelectionElement methods;

	private String[] values = {""};
	private String[] keys = {"on"};

	private String[] methodValues = {""};
	private String[] methodKeys = {""};

	private final AccessControlModule acModule;

	public AccessControlAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		acModule = CoreSpringFactory.getImpl(AccessControlModule.class);

		values = new String[] {
			getTranslator().translate("ac.on")
		};

		methodKeys = new String[] {
			FreeAccessHandler.METHOD_TYPE,
			TokenAccessHandler.METHOD_TYPE,
			PaypalAccessHandler.METHOD_TYPE,
			PaypalCheckoutAccessHandler.METHOD_TYPE,
			METHOD_AUTO
		};

		methodValues = new String[methodKeys.length];
		for(int i=0; i<methodKeys.length-1; i++) {
			AccessMethodHandler handler = acModule.getAccessMethodHandler(methodKeys[i]);
			methodValues[i] = handler.getMethodName(getLocale());
		}
		methodValues[methodValues.length - 1] = getTranslator().translate(METHOD_AUTO);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.title");
		setFormDescription("admin.desc");

		enabled = uifactory.addCheckboxesHorizontal("ac.enabled", formLayout, keys, values);
		enabled.select(keys[0], acModule.isEnabled());

		uifactory.addSpacerElement("spaceman", formLayout, false);

		methods = uifactory.addCheckboxesVertical("ac.methods", formLayout, methodKeys, methodValues, 1);
		methods.select(FreeAccessHandler.METHOD_TYPE, acModule.isFreeEnabled());
		methods.select(TokenAccessHandler.METHOD_TYPE, acModule.isTokenEnabled());
		methods.select(PaypalAccessHandler.METHOD_TYPE, acModule.isPaypalEnabled());
		methods.select(PaypalCheckoutAccessHandler.METHOD_TYPE, acModule.isPaypalCheckoutEnabled());
		methods.select(METHOD_AUTO, acModule.isAutoEnabled());
		methods.setEnabled(acModule.isEnabled());
		methods.addActionListener(FormEvent.ONCHANGE);

		uifactory.addSpacerElement("itgirl", formLayout, false);

		homeEnabled = uifactory.addCheckboxesHorizontal("ac.home.enabled", formLayout, keys, values);
		homeEnabled.select(keys[0], acModule.isPaypalEnabled() || acModule.isPaypalCheckoutEnabled() || acModule.isHomeOverviewEnabled());

		final FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		buttonGroupLayout.setRootForm(mainForm);
		uifactory.addFormSubmitButton("save", buttonGroupLayout);

		formLayout.add(buttonGroupLayout);
		update();
	}

	public void update() {
		Collection<String> selectedMethods = methods.getSelectedKeys();
		if(selectedMethods.contains(PaypalAccessHandler.METHOD_TYPE)) {
			homeEnabled.select(keys[0], true);
			homeEnabled.setEnabled(false);
		} else {
			homeEnabled.setEnabled(true);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == methods) {
			update();
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean on = !enabled.getSelectedKeys().isEmpty();
		acModule.setEnabled(on);

		Collection<String> selectedMethods = methods.getSelectedKeys();
		acModule.setFreeEnabled(selectedMethods.contains(FreeAccessHandler.METHOD_TYPE));
		acModule.setTokenEnabled(selectedMethods.contains(TokenAccessHandler.METHOD_TYPE));
		boolean paypalEnabled = selectedMethods.contains(PaypalAccessHandler.METHOD_TYPE);
		acModule.setPaypalEnabled(paypalEnabled);
		boolean paypalCheckoutEnabled = selectedMethods.contains(PaypalCheckoutAccessHandler.METHOD_TYPE);
		acModule.setPaypalCheckoutEnabled(paypalCheckoutEnabled);
		acModule.setAutoEnabled(selectedMethods.contains(METHOD_AUTO));

		boolean homeOverviewEnabled = paypalEnabled || !homeEnabled.getSelectedKeys().isEmpty();
		acModule.setHomeOverviewEnabled(homeOverviewEnabled);

		methods.setEnabled(on);
		showInfo("ac.saved");
	}
}
