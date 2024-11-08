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
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.CostCenter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 Nov 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CostCenterController extends FormBasicController implements Controller {

	private TextElement nameEl;
	private TextElement accountEl;
	private FormToggle enabledEl;

	private CostCenter costCenter;
	
	@Autowired
	private ACService acService;
	
	protected CostCenterController(UserRequest ureq, WindowControl wControl, CostCenter costCenter) {
		super(ureq, wControl);
		this.costCenter = costCenter;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String name = costCenter != null? costCenter.getName(): null;
		nameEl = uifactory.addTextElement("cost.center.name", 200, name, formLayout);
		nameEl.setMandatory(true);
		
		String account = costCenter != null? costCenter.getAccount(): null;
		accountEl = uifactory.addTextElement("cost.center.account", 200, account, formLayout);
		
		enabledEl = uifactory.addToggleButton("enabled", "cost.center.enabled", translate("on"), translate("off"), formLayout);
		enabledEl.setEnabled(costCenter == null);
		if (costCenter == null || costCenter.isEnabled()) {
			enabledEl.toggleOn();
		}
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		nameEl.clearError();
		if (!StringHelper.containsNonWhitespace(nameEl.getValue())) {
			nameEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		if (costCenter == null) {
			costCenter = acService.createCostCenter();
		}
		
		costCenter.setName(nameEl.getValue());
		costCenter.setAccount(accountEl.getValue());
		costCenter.setEnabled(enabledEl.isOn());
		
		costCenter = acService.updateCostCenter(costCenter);
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

}
