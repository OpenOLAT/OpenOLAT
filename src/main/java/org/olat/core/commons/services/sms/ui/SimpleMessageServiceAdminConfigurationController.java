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
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
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
	
	private AbstractSMSConfigurationController providerConfigCtrl;
	
	@Autowired
	private SimpleMessageModule messageModule;
	@Autowired
	private SimpleMessageService messagesService;
	
	public SimpleMessageServiceAdminConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "admin_configuration");
		
		initForm(ureq);
		updateEnableDisable();
		updateProviderConfiguration(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer serviceCont = FormLayoutContainer.createDefaultFormLayout("service", getTranslator());
		formLayout.add(serviceCont);
		
		setFormTitle("admin.configuration.title");
		setFormDescription("admin.configuration.description");
		
		String[] onValues = new String[]{ translate("on") };
		enableEl = uifactory.addCheckboxesHorizontal("enable", "admin.enable", serviceCont, onKeys, onValues);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		if(messageModule.isEnabled()) {
			enableEl.select(onKeys[0], true);
		}
		
		List<MessagesSPI> spies = messagesService.getMessagesSpiList();
		String[] serviceKeys = new String[spies.size() + 1];
		String[] serviceValues = new String[spies.size() + 1];
		serviceKeys[0] = "devnull";
		serviceValues[0] = translate("no.service.provider");
		for(int i=spies.size(); i-->0; ) {
			serviceKeys[i + 1] = spies.get(i).getId();
			serviceValues[i + 1] = spies.get(i).getName();
		}
		serviceEl = uifactory.addDropdownSingleselect("service.providers", "service", serviceCont, serviceKeys, serviceValues, null);
		serviceEl.addActionListener(FormEvent.ONCHANGE);
		if(messagesService.getMessagesSpi() != null) {
			String activeServiceId = messageModule.getProviderId();
			for(int i=serviceKeys.length; i-->0; ) {
				if(serviceKeys[i].equals(activeServiceId)) {
					serviceEl.select(serviceKeys[i], true);
				}
			}
		}

		String[] resetPasswordValues = new String[]{ translate("on.sms") };
		resetPasswordEl = uifactory.addCheckboxesHorizontal("reset.password", "reset.password", serviceCont, onKeys, resetPasswordValues);
		if(messageModule.isResetPasswordEnabled()) {
			resetPasswordEl.select(onKeys[0], true);
		}
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonCont);
		uifactory.addFormSubmitButton("save", buttonCont);
	}
	
	private void updateProviderConfiguration(UserRequest ureq) {
		removeControllerListener(providerConfigCtrl);
		if(serviceEl.isOneSelected()) {
			flc.remove("configuration");
			
			String selectedProviderId = serviceEl.getSelectedKey();
			List<MessagesSPI> spies = messagesService.getMessagesSpiList();
			for(MessagesSPI spi:spies) {
				if(spi.getId().equals(selectedProviderId)) {
					providerConfigCtrl = spi.getConfigurationController(ureq, getWindowControl(), mainForm);
					if(providerConfigCtrl != null) {
						flc.add("configuration", providerConfigCtrl.getInitialFormItem());
					}
				}
			}	
		} else {
			flc.remove("configuration");
		}
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
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		if(providerConfigCtrl != null) {
			allOk &= providerConfigCtrl.validateFormLogic(ureq);
		}
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source) {
			updateEnableDisable();
		} else if(serviceEl == source) {
			updateProviderConfiguration(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		messageModule.setEnabled(enableEl.isAtLeastSelected(1));
		messageModule.setResetPasswordEnabled(resetPasswordEl.isAtLeastSelected(1));
		messageModule.setProviderId(serviceEl.getSelectedKey());
		if(providerConfigCtrl != null) {
			providerConfigCtrl.formOK(ureq);
		}
	}
}
