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
package org.olat.modules.selectus.ui.admin;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.selectus.RecruitingModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 mars 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class SelectusAdminController extends FormBasicController {
	
	private FormToggle enabledButton;
	private FormToggle linkLoginToggle;
	
	@Autowired
	private RecruitingModule selectusModule;
	
	public SelectusAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("menu.selectus");
		
		enabledButton = uifactory.addToggleButton("selectus.enabled", "selectus.enabled", translate("on"), translate("off"), formLayout);
		enabledButton.toggle(selectusModule.isEnabled());
		enabledButton.addActionListener(FormEvent.ONCHANGE);
		
		linkLoginToggle = uifactory.addToggleButton("selectus.positions.login", "selectus.positions.login", translate("on"), translate("off"), formLayout);
		linkLoginToggle.toggle(selectusModule.isPositionsLoginEnabled());
		linkLoginToggle.addActionListener(FormEvent.ONCHANGE);
		linkLoginToggle.setVisible(enabledButton.isOn());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == enabledButton || source == linkLoginToggle) {
			updateEnable();
			getWindowControl().setInfo(translate("saved"));
		}
	}
	
	private void updateEnable() {
		boolean on = enabledButton.isOn();
		selectusModule.setEnabled(on);
		
		boolean linkOn = linkLoginToggle.isOn() && on;
		selectusModule.setPositionsLoginEnabled(linkOn);
		linkLoginToggle.setVisible(on);
	}
}