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
package org.olat.core.commons.services.sms.ui;

import java.util.List;

import org.olat.core.commons.services.sms.MessagesSPI;
import org.olat.core.commons.services.sms.SimpleMessageModule;
import org.olat.core.commons.services.sms.SimpleMessageService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SimpleMessageServiceAdminConfigurationController extends FormBasicController {
	
	private static final String[] onKeys = new String[]{ "on" };
	
	private SingleSelection serviceEl;
	private MultipleSelectionElement enableEl;
	private MultipleSelectionElement resetPasswordEl;
	
	@Autowired
	private SimpleMessageModule messageModule;
	@Autowired
	private SimpleMessageService messagesService;
	
	public SimpleMessageServiceAdminConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.configuration.title");
		setFormDescription("admin.configuration.description");
		
		String[] onValues = new String[]{ translate("on") };
		enableEl = uifactory.addCheckboxesHorizontal("enable", "admin.enable", formLayout, onKeys, onValues);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		if(messageModule.isEnabled()) {
			enableEl.select(onKeys[0], true);
		}
		
		List<MessagesSPI> spies = messagesService.getMessagesSpiList();
		String[] serviceKeys = new String[spies.size()];
		String[] serviceValues = new String[spies.size()];
		for(int i=spies.size(); i-->0; ) {
			serviceKeys[i] = spies.get(i).getId();
			serviceValues[i] = spies.get(i).getName();
		}
		serviceEl = uifactory.addDropdownSingleselect("service", "service", formLayout, serviceKeys, serviceValues, null);
		if(messagesService.getMessagesSpi() != null) {
			String activeServiceId = messagesService.getMessagesSpi().getId();
			for(int i=serviceKeys.length; i-->0; ) {
				if(serviceKeys[i].equals(activeServiceId)) {
					serviceEl.select(serviceKeys[i], true);
				}
			}
		}

		String[] resetPasswordValues = new String[]{ translate("on.sms") };
		resetPasswordEl = uifactory.addCheckboxesHorizontal("reset.password", "reset.password", formLayout, onKeys, resetPasswordValues);
		resetPasswordEl.addActionListener(FormEvent.ONCHANGE);
		if(messageModule.isResetPasswordEnabled()) {
			resetPasswordEl.select(onKeys[0], true);
		}
		updateEnableDisable();
	}
	
	private void updateEnableDisable() {
		boolean enabled = enableEl.isAtLeastSelected(1);
		serviceEl.setVisible(enabled);
		resetPasswordEl.setVisible(enabled);
		
		serviceEl.clearError();
		if(serviceEl.isOneSelected()) {
			String serviceId = serviceEl.getSelectedKey();
			MessagesSPI spi = messagesService.getMessagesSpi(serviceId);
			if(spi != null && !spi.isValid()) {
				serviceEl.setErrorKey("warning.spi.not.configured", new String[]{ spi.getName() });
			}
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source) {
			updateEnableDisable();
			messageModule.setEnabled(enableEl.isAtLeastSelected(1));
		} else if(resetPasswordEl == source) {
			messageModule.setResetPasswordEnabled(resetPasswordEl.isAtLeastSelected(1));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	
	

}
