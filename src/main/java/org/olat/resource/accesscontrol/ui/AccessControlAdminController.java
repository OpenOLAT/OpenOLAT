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

import java.util.Set;

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
	
	private MultipleSelectionElement enabled, homeEnabled;
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
			"free.method",
			"token.method",
			"paypal.method"
		};

		methodValues = new String[methodKeys.length];
		for(int i=0; i<methodKeys.length; i++) {
			AccessMethodHandler handler = acModule.getAccessMethodHandler(methodKeys[i]);
			methodValues[i] = handler.getMethodName(getLocale());
		}
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.title");
		setFormDescription("admin.desc");

		enabled = uifactory.addCheckboxesHorizontal("ac.enabled", formLayout, keys, values, null);
		enabled.select(keys[0], acModule.isEnabled());
		
		uifactory.addSpacerElement("spaceman", formLayout, false);
		
		methods = uifactory.addCheckboxesVertical("ac.methods", formLayout, methodKeys, methodValues, null, 1);
		methods.select("free.method", acModule.isFreeEnabled());
		methods.select("token.method", acModule.isTokenEnabled());
		methods.select("paypal.method", acModule.isPaypalEnabled());
		methods.setEnabled(acModule.isEnabled());
		methods.addActionListener(this, FormEvent.ONCHANGE);
		
		uifactory.addSpacerElement("itgirl", formLayout, false);
		
		homeEnabled = uifactory.addCheckboxesHorizontal("ac.home.enabled", formLayout, keys, values, null);
		homeEnabled.select(keys[0], acModule.isPaypalEnabled() || acModule.isHomeOverviewEnabled());
		
		final FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		buttonGroupLayout.setRootForm(mainForm);
		uifactory.addFormSubmitButton("save", buttonGroupLayout);
		
		formLayout.add(buttonGroupLayout);
		update();
	}
	
	public void update() {
		Set<String> selectedMethods = methods.getSelectedKeys();
		if(selectedMethods.contains("paypal.method")) {
			homeEnabled.select(keys[0], true);
			homeEnabled.setEnabled(false);
		} else {
			homeEnabled.setEnabled(true);
		}
	}

	@Override
	protected void doDispose() {
		//
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
		
		Set<String> selectedMethods = methods.getSelectedKeys();
		acModule.setFreeEnabled(selectedMethods.contains("free.method"));
		acModule.setTokenEnabled(selectedMethods.contains("token.method"));
		boolean paypalEnabled = selectedMethods.contains("paypal.method");
		acModule.setPaypalEnabled(paypalEnabled);
		
		boolean homeOverviewEnabled = paypalEnabled || !homeEnabled.getSelectedKeys().isEmpty();
		acModule.setHomeOverviewEnabled(homeOverviewEnabled);
		
		methods.setEnabled(on);
		showInfo("ac.saved");
	}
}
