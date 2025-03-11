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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.AccessControlModule.VAT;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Mar 11, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class VatAdminController extends FormBasicController {

	private FormToggle enabledEl;
	private SingleSelection optionEl;
	private TextElement numberEl;
	private FormLayoutContainer rateCont;
	private TextElement rateEl;
	private StaticTextElement exampleEl;
	
	@Autowired
	private AccessControlModule acModule;
	
	public VatAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
		updateUI();
		updateExampleUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.vat.title");
		setFormInfo("admin.vat.info");
		
		enabledEl = uifactory.addToggleButton("admin.vat.enabled", "admin.vat.enabled", translate("on"), translate("off"), formLayout);
		enabledEl.toggle(VAT.disabled != acModule.getVat());
		enabledEl.addActionListener(FormEvent.ONCHANGE);
		
		SelectionValues optionsSV = new SelectionValues();
		optionsSV.add(SelectionValues.entry(VAT.inclusive.name(), translate("vat.inclusive")));
		optionsSV.add(SelectionValues.entry(VAT.exclusive.name(), translate("vat.exclusive")));
		optionEl = uifactory.addRadiosHorizontal("admin.vat.option", formLayout, optionsSV.keys(), optionsSV.values());
		optionEl.addActionListener(FormEvent.ONCHANGE);
		if (VAT.exclusive == acModule.getVat()) {
			optionEl.select(VAT.exclusive.name(), true);
		} else {
			optionEl.select(VAT.inclusive.name(), true);
		}
		
		numberEl = uifactory.addTextElement("admin.vat.number", 100, acModule.getVatNumber(), formLayout);
		
		rateCont = FormLayoutContainer.createCustomFormLayout("freeCont", getTranslator(), velocity_root + "/rate_config.html");
		rateCont.setLabel("admin.vat.rate", null);
		rateCont.setRootForm(mainForm);
		formLayout.add(rateCont);
		
		rateEl = uifactory.addTextElement("admin.vat.rate", 8, acModule.getVatRate(), rateCont);
		rateEl.setDisplaySize(8);
		rateEl.addActionListener(FormEvent.ONCHANGE);
		
		exampleEl = uifactory.addStaticTextElement("admin.vat.example", "", formLayout);
	}

	private void updateUI() {
		boolean vatEnabled = enabledEl.isOn();
		optionEl.setVisible(vatEnabled);
		numberEl.setVisible(vatEnabled);
		rateCont.setVisible(vatEnabled);
		rateEl.setVisible(vatEnabled);
		exampleEl.setVisible(vatEnabled);
	}
	
	private void updateExampleUI() {
		String formatVat = PriceFormat.formatVat(getTranslator(), acModule);
		if (StringHelper.containsNonWhitespace(formatVat)) {
			exampleEl.setValue("(" + formatVat + ")");
		} else {
			exampleEl.setValue(null);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == enabledEl) {
			updateUI();
			save();
		} else if (source == optionEl) {
			updateUI();
			save();
		} else if (source == numberEl) {
			updateUI();
			save();
		} else if (source == rateEl) {
			updateUI();
			save();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void save() {
		VAT vat;
		if (enabledEl.isOn()) {
			vat = VAT.toVAT(optionEl.getSelectedKey());
		} else {
			vat = VAT.disabled;
		}
		acModule.setVat(vat);
		
		boolean validated = validateRate();
		if (!validated) {
			return;
		}
		
		if (numberEl.isVisible()) {
			acModule.setVatNumber(numberEl.getValue());
		}
		
		if (rateEl.isVisible()) {
			acModule.setVatRate(rateEl.getValue());
		}
		
		updateExampleUI();
	}

	private boolean validateRate() {
		rateCont.clearError();
		if (rateEl.isVisible() && StringHelper.containsNonWhitespace(rateEl.getValue())) {
			try {
				double rate = Double.parseDouble(rateEl.getValue());
				if (rate <= 0l) {
					rateCont.setErrorKey("form.error.positive.integer");
					return false;
				}
			} catch (NumberFormatException e) {
				rateCont.setErrorKey("form.error.positive.integer");
				return false;
			}
		}
		return true;
	}

}
