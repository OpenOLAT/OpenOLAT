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
package com.frentix.olat.vitero.ui;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.TimeZone;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;

import com.frentix.olat.vitero.ViteroModule;
import com.frentix.olat.vitero.ViteroTimezoneIDs;
import com.frentix.olat.vitero.manager.ViteroManager;
import com.frentix.olat.vitero.manager.VmsNotAvailableException;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  10 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroConfigurationController extends FormBasicController {
	
	private final ViteroModule viteroModule;
	private final ViteroManager viteroManager;
	
	private FormLink checkLink;
	private TextElement urlEl;
	private TextElement loginEl;
	private TextElement passwordEl;
	private TextElement customerIdEl;
	private MultipleSelectionElement viteroEnabled;
	private SingleSelection timeZoneEl;

	private static final String[] enabledKeys = new String[]{"on"};
	private String[] enabledValues;
	
	public ViteroConfigurationController(UserRequest ureq, WindowControl wControl, ViteroModule viteroModule) {
		super(ureq, wControl, "adminconfig");
		
		this.viteroModule = viteroModule;
		viteroManager = (ViteroManager)CoreSpringFactory.getBean("viteroManager");
		
		enabledValues = new String[]{translate("enabled")};
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutContainer = (FormLayoutContainer)formLayout;
			
			//module configuration
			FormLayoutContainer moduleFlc = FormLayoutContainer.createDefaultFormLayout("flc_module", getTranslator());
			layoutContainer.add(moduleFlc);
		
			viteroEnabled = uifactory.addCheckboxesHorizontal("vitero.module.enabled", moduleFlc, enabledKeys, enabledValues, null);
			viteroEnabled.select(enabledKeys[0], viteroModule.isEnabled());
			viteroEnabled.addActionListener(listener, FormEvent.ONCHANGE);
			
			//spacer
			uifactory.addSpacerElement("Spacer", moduleFlc, false);
			
			String[] timeZoneKeys = ViteroTimezoneIDs.TIMEZONE_IDS;
			String[] timeZoneValues = new String[timeZoneKeys.length];
			int i=0;
			for(String timeZoneKey:timeZoneKeys) {
				TimeZone timezone = TimeZone.getTimeZone(timeZoneKey);
				if(timezone == null) {
					timeZoneValues[i++] = timeZoneKey;
				} else {
					String value = timezone.getDisplayName(false, TimeZone.LONG);
					timeZoneValues[i++] = value;
				}
			}

			timeZoneEl = uifactory.addDropdownSingleselect("vc.olatTimeZone", moduleFlc, timeZoneKeys, timeZoneValues, null);
			timeZoneEl.select(viteroModule.getTimeZoneId(), true);
			
			//account configuration
			String vmsUri = viteroModule.getVmsURI().toString();
			urlEl = uifactory.addTextElement("vitero-url", "vc.vitero.baseurl", 255, vmsUri, moduleFlc);
			urlEl.setDisplaySize(60);
			String login = viteroModule.getAdminLogin();
			loginEl = uifactory.addTextElement("vitero-login", "vc.vitero.adminlogin", 32, login, moduleFlc);
			String password = viteroModule.getAdminPassword();
			passwordEl = uifactory.addPasswordElement("vitero-password", "vc.vitero.adminpassword", 32, password, moduleFlc);
			String customerId = Integer.toString(viteroModule.getCustomerId());
			customerIdEl = uifactory.addTextElement("vitero-customerId", "vc.vitero.customerId", 32, customerId, moduleFlc);

			//buttons save - check
			FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("save", getTranslator());
			moduleFlc.add(buttonLayout);
			uifactory.addFormSubmitButton("save", buttonLayout);
			checkLink = uifactory.addFormLink("vc.check", buttonLayout, Link.BUTTON);
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		try {
			String url = urlEl.getValue();
			viteroModule.setVmsURI(new URI(url));
			
			String login = loginEl.getValue();
			viteroModule.setAdminLogin(login);
			
			String password = passwordEl.getValue();
			viteroModule.setAdminPassword(password);
			
			String customerId = customerIdEl.getValue();
			viteroModule.setCustomerId(Integer.parseInt(customerId));
			
			if(timeZoneEl.isOneSelected()) {
				String timeZoneId = timeZoneEl.getSelectedKey();
				viteroModule.setTimeZoneId(timeZoneId);
			}
		} catch (URISyntaxException e) {
			logError("", e);
			urlEl.setErrorKey("vc.check.url.invalid", null);
		} catch(NumberFormatException e) {
			logError("", e);
			urlEl.setErrorKey("vc.check.customer.invalid", null);
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		String url = urlEl.getValue();
		urlEl.clearError();
		if(StringHelper.containsNonWhitespace(url)) {
			try {
				new URI(url);
			} catch(Exception e) {
				urlEl.setErrorKey("vc.check.url.invalid", null);
				allOk = false;
			}
		} else {
			urlEl.setErrorKey("form.legende.mandatory", null);
			allOk = false;
		}
		
		String login = loginEl.getValue();
		loginEl.clearError();
		if(!StringHelper.containsNonWhitespace(login)) {
			loginEl.setErrorKey("form.legende.mandatory", null);
			allOk = false;
		}
		
		String password = passwordEl.getValue();
		passwordEl.clearError();
		if(!StringHelper.containsNonWhitespace(password)) {
			passwordEl.setErrorKey("form.legende.mandatory", null);
			allOk = false;
		}
		
		String customerId = customerIdEl.getValue();
		customerIdEl.clearError();
		if(StringHelper.containsNonWhitespace(customerId)) {
			try {
				Integer.parseInt(customerId);
			} catch(Exception e) {
				customerIdEl.setErrorKey("vc.check.customer.invalid", null);
				allOk = false;
			}
		} else {
			customerIdEl.setErrorKey("form.legende.mandatory", null);
			allOk = false;
		}
		
		return allOk && super.validateFormLogic(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == viteroEnabled) {
			boolean enabled = viteroEnabled.isSelected(0);
			viteroModule.setEnabled(enabled);
		} else if(source == checkLink) {
			if(validateFormLogic(ureq)) {
				checkConnection(ureq);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	protected void checkConnection(UserRequest ureq) {
		String url = urlEl.getValue();
		String login = loginEl.getValue();
		String password = passwordEl.getValue();
		String customerId = customerIdEl.getValue();

		try {
			boolean ok = viteroManager.checkConnection(url, login, password, Integer.parseInt(customerId));
			if(ok) {
				showInfo("vc.check.ok");
			} else {
				showError("vc.check.nok");
			}
		} catch (NumberFormatException e) {
			showError("vc.check.customer.invalid");
		} catch (VmsNotAvailableException e) {
			showError(VmsNotAvailableException.I18N_KEY);
		}
	}
}