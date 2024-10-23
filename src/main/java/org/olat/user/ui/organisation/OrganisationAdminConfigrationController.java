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
package org.olat.user.ui.organisation;

import org.olat.basesecurity.OrganisationModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationAdminConfigrationController extends FormBasicController {
	
	private FormToggle enableEl;
	private FormToggle emailDomainEnableEl;
	
	@Autowired
	private OrganisationModule organisationModule;
	
	public OrganisationAdminConfigrationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("admin.description");
		setFormContextHelp("manual_admin/administration/Modules_Organisations/");
		
		enableEl = uifactory.addToggleButton("organisation.admin.enabled", "organisation.admin.enabled", translate("on"), translate("off"), formLayout);
		enableEl.toggle(organisationModule.isEnabled());
		enableEl.addActionListener(FormEvent.ONCHANGE);
		
		emailDomainEnableEl = uifactory.addToggleButton("email.domain", "organisation.admin.email.domain.enabled", translate("on"), translate("off"), formLayout);
		emailDomainEnableEl.toggle(organisationModule.isEmailDomainEnabled());
		emailDomainEnableEl.addActionListener(FormEvent.ONCHANGE);
	}

	private void updateUI() {
		emailDomainEnableEl.setVisible(enableEl.isOn());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source) {
			organisationModule.setEnabled(enableEl.isOn());
			updateUI();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(emailDomainEnableEl == source) {
			organisationModule.setEmailDomainEnabled(emailDomainEnableEl.isOn());
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}
}
