/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.creditpoint.ui;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.creditpoint.CreditPointExpirationType;
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.ui.component.ExpirationFormItem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CreditPointSystemEditController extends FormBasicController {
	
	private TextElement nameEl;
	private TextElement labelEl;
	private FormToggle expirationEl;
	private ExpirationFormItem defaultExpirationEl;
	
	private CreditPointSystem creditPointSystem;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CreditPointService creditPointService;
	
	public CreditPointSystemEditController(UserRequest ureq, WindowControl wControl, CreditPointSystem creditPointSystem) {
		super(ureq, wControl);
		this.creditPointSystem = creditPointSystem;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String name = creditPointSystem == null ? null : creditPointSystem.getName();
		nameEl = uifactory.addTextElement("system.name", 255, name, formLayout);
		nameEl.setMandatory(true);
		
		String label = creditPointSystem == null ? null : creditPointSystem.getLabel();
		labelEl = uifactory.addTextElement("system.label", 16, label, formLayout);
		labelEl.setMandatory(true);
		
		expirationEl = uifactory.addToggleButton("validity.period", "validity.period", translate("on"), translate("off"), formLayout);
		expirationEl.toggle(creditPointSystem != null && creditPointSystem.getDefaultExpiration() != null && creditPointSystem.getDefaultExpirationUnit() != null);

		defaultExpirationEl = new ExpirationFormItem("system.default.expiration", false, getTranslator());
		defaultExpirationEl.setLabel("system.default.expiration", null);
		if(creditPointSystem != null && creditPointSystem.getDefaultExpiration() != null) {
			defaultExpirationEl.setValue(creditPointSystem.getDefaultExpiration().toString());
			defaultExpirationEl.setType(creditPointSystem.getDefaultExpirationUnit());
		}
		defaultExpirationEl.setVisible(expirationEl.isOn());
		formLayout.add(defaultExpirationEl);
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		nameEl.clearError();
		if(!StringHelper.containsNonWhitespace(nameEl.getValue())) {
			nameEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		labelEl.clearError();
		if(!StringHelper.containsNonWhitespace(labelEl.getValue())) {
			labelEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		defaultExpirationEl.clearError();
		if(expirationEl.isOn()) {
			if(defaultExpirationEl.isEmpty()) {
				defaultExpirationEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else if(defaultExpirationEl.getValue() != null) {
				int val = defaultExpirationEl.getValue().intValue();
				if(val <= 0) {
					defaultExpirationEl.setErrorKey("form.error.positive.integer");
					allOk &= false;
				}
			}
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(expirationEl != null) {
			defaultExpirationEl.setVisible(expirationEl.isOn());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String name = nameEl.getValue();
		String label = labelEl.getValue();
		
		boolean expiration = expirationEl.isOn();
		Integer defaultExpiration = expiration ? defaultExpirationEl.getValue() : null;
		CreditPointExpirationType defaultExpirationType = expiration ? defaultExpirationEl.getType() : null;
		
		if(creditPointSystem == null) {
			creditPointSystem = creditPointService.createCreditPointSystem(name, label,
					defaultExpiration, defaultExpirationType);
		} else {
			creditPointSystem.setName(name);
			creditPointSystem.setLabel(label);
			creditPointSystem.setDefaultExpiration(defaultExpiration);
			creditPointSystem.setDefaultExpirationUnit(defaultExpirationType);
			creditPointSystem = creditPointService.updateCreditPointSystem(creditPointSystem);
		}
		dbInstance.commit();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
