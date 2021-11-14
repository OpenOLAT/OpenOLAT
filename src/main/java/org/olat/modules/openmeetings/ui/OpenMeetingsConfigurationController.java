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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.openmeetings.ui;

import java.net.URI;
import java.net.URISyntaxException;

import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.openmeetings.OpenMeetingsModule;
import org.olat.modules.openmeetings.manager.OpenMeetingsException;
import org.olat.modules.openmeetings.manager.OpenMeetingsManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  10 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OpenMeetingsConfigurationController extends FormBasicController {

	private static final String PLACEHOLDER = "xxx-placeholder-xxx";
	
	private FormLink checkLink;
	private TextElement urlEl;
	private TextElement loginEl;
	private TextElement passwordEl;
	private MultipleSelectionElement moduleEnabled;

	private static final String[] enabledKeys = new String[]{"on"};
	private final String[] enabledValues;
	
	private String replacedValue;
	
	@Autowired
	private OpenMeetingsModule openMeetingsModule;
	@Autowired
	private OpenMeetingsManager openMeetingsManager;
	
	public OpenMeetingsConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "adminconfig");
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
		
			moduleEnabled = uifactory.addCheckboxesHorizontal("openmeetings.module.enabled", moduleFlc, enabledKeys, enabledValues);
			moduleEnabled.select(enabledKeys[0], openMeetingsModule.isEnabled());
			moduleEnabled.addActionListener(FormEvent.ONCHANGE);
			
			//spacer
			uifactory.addSpacerElement("Spacer", moduleFlc, false);

			//account configuration
			String vmsUri = openMeetingsModule.getOpenMeetingsURI().toString();
			urlEl = uifactory.addTextElement("openmeetings-url", "option.baseurl", 255, vmsUri, moduleFlc);
			urlEl.setDisplaySize(60);
			String login = openMeetingsModule.getAdminLogin();
			loginEl = uifactory.addTextElement("openmeetings-login", "option.adminlogin", 32, login, moduleFlc);
			String credential = openMeetingsModule.getAdminPassword();
			if(StringHelper.containsNonWhitespace(credential)) {
				replacedValue = credential;
				credential = PLACEHOLDER;
			}
			passwordEl = uifactory.addPasswordElement("openmeetings-password", "option.adminpassword", 32, credential, moduleFlc);
			passwordEl.setAutocomplete("new-password");
			
			String externalType = openMeetingsManager.getOpenOLATExternalType();
			uifactory.addStaticTextElement("om.externaltype", "openolat.externaltype", externalType, moduleFlc);
	
			//buttons save - check
			FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("save", getTranslator());
			moduleFlc.add(buttonLayout);
			uifactory.addFormSubmitButton("save", buttonLayout);
			checkLink = uifactory.addFormLink("check", buttonLayout, Link.BUTTON);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		try {
			String url = urlEl.getValue();
			openMeetingsModule.setOpenMeetingsURI(new URI(url));
			
			String login = loginEl.getValue();
			openMeetingsModule.setAdminLogin(login);
			
			String credential = passwordEl.getValue();
			if(!PLACEHOLDER.equals(credential)) {
				openMeetingsModule.setAdminPassword(credential);
				passwordEl.setValue(PLACEHOLDER);
			} else if(StringHelper.containsNonWhitespace(replacedValue)) {
				openMeetingsModule.setAdminPassword(replacedValue);
			}
		} catch (URISyntaxException e) {
			logError("", e);
			urlEl.setErrorKey("error.url.invalid", null);
		} catch(NumberFormatException e) {
			logError("", e);
			urlEl.setErrorKey("error.customer.invalid", null);
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		//validate only if the module is enabled
		if(openMeetingsModule.isEnabled()) {
			allOk &= validateURL();

			try {
				String credential = passwordEl.getValue();
				if(PLACEHOLDER.equals(credential)) {
					if(StringHelper.containsNonWhitespace(replacedValue)) {
						credential = replacedValue;
					} else {
						credential = openMeetingsModule.getAdminPassword();
					}
				} else {
					replacedValue = credential;
					passwordEl.setValue(PLACEHOLDER);
				}
				boolean ok = openMeetingsManager.checkConnection(urlEl.getValue(), loginEl.getValue(), credential);
				if(!ok) {
					passwordEl.setValue("");
					urlEl.setErrorKey("error.customerDoesntExist", null);
					allOk = false;
				}
			} catch (OpenMeetingsException e) {
				showError(OpenMeetingsException.SERVER_NOT_I18N_KEY);
			}
		}
		
		return allOk;
	}
	
	private boolean validateURL() {
		boolean allOk = true;
		
		String url = urlEl.getValue();
		urlEl.clearError();
		if(StringHelper.containsNonWhitespace(url)) {
			try {
				URI uri = new URI(url);
				uri.getHost();
			} catch(Exception e) {
				urlEl.setErrorKey("error.url.invalid", null);
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
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == moduleEnabled) {
			boolean enabled = moduleEnabled.isSelected(0);
			openMeetingsModule.setEnabled(enabled);
			// update collaboration tools list
			CollaborationToolsFactory.getInstance().initAvailableTools();
		} else if(source == checkLink) {
			if(validateURL()) {
				checkConnection();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	
	protected boolean checkConnection() {
		String url = urlEl.getValue();
		String login = loginEl.getValue();
		String credential = passwordEl.getValue();
		if(PLACEHOLDER.equals(credential)) {
			if(StringHelper.containsNonWhitespace(replacedValue)) {
				credential = replacedValue;
			} else {
				credential = openMeetingsModule.getAdminPassword();
			}
		} else {
			replacedValue = credential;
			passwordEl.setValue(PLACEHOLDER);
		}

		try {
			boolean ok = openMeetingsManager.checkConnection(url, login, credential);
			if(ok) {
				showInfo("check.ok");
			} else {
				showError("check.nok");
			}
			return ok;
		} catch (NumberFormatException e) {
			showError("error.customer.invalid");
			return false;
		} catch (OpenMeetingsException e) {
			showError(OpenMeetingsException.SERVER_NOT_I18N_KEY);
			return false;
		}
	}
}