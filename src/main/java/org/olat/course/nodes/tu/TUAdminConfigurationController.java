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
package org.olat.course.nodes.tu;

import org.olat.admin.privacy.PrivacyAdminController;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class TUAdminConfigurationController extends FormBasicController {
	
	private static final String ON_KEY = "on";

	private FormToggle enableEl;
	private StaticTextElement tunnelInfosEl;
	private MultipleSelectionElement tunnelEl;

	@Autowired
	private TUModule tuModule;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public TUAdminConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(PrivacyAdminController.class, ureq.getLocale()));
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.menu.title");
		this.setFormInfo("admin.tunnel.descr");
		
		enableEl = uifactory.addToggleButton("enabled", "enabled", translate("on"), translate("off"), formLayout);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		enableEl.toggle(tuModule.isEnabled());
		
		String infos = "<p class='o_info_with_icon'><strong>" + translate("tunnel.title") + "</strong><br>" + translate("tunnel.desc") + "</p>";
		tunnelInfosEl = uifactory.addStaticTextElement("cbb.infos", null, infos, formLayout);
		tunnelInfosEl.setDomWrapperElement(DomWrapperElement.div);
		
		SelectionValues onKP = new SelectionValues();
		onKP.add(SelectionValues.entry(ON_KEY, ""));
		tunnelEl = uifactory.addCheckboxesHorizontal("tunnel.cbb", formLayout, onKP.keys(), onKP.values());
		tunnelEl.select(ON_KEY, "enabled".equals(securityModule.getUserInfosTunnelCourseBuildingBlock()));
		tunnelEl.addActionListener(FormEvent.ONCHANGE);
	}
	
	private void updateUI() {
		boolean enabled = enableEl.isOn();
		tunnelEl.setVisible(enabled);
		tunnelInfosEl.setVisible(enabled);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source) {
			tuModule.setEnabled(enableEl.isOn());
			updateUI();
		} else if (source == tunnelEl) {
			boolean headerEnabled = tunnelEl.isAtLeastSelected(1);
			securityModule.setUserInfosTunnelCourseBuildingBlock(headerEnabled ? "enabled" : "disabled");
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	

}
