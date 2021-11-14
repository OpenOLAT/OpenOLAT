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
package org.olat.modules.vitero.ui;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

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
import org.olat.modules.vitero.ViteroModule;
import org.olat.modules.vitero.ViteroTimezoneIDs;
import org.olat.modules.vitero.manager.ViteroManager;
import org.olat.modules.vitero.manager.VmsNotAvailableException;
import org.olat.modules.vitero.model.CheckUserInfo;
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
public class ViteroConfigurationController extends FormBasicController {
	
	private static final String PLACEHOLDER = "xxx-placeholder-xxx";
	
	private FormLink checkLink;
	private FormLink checkUserLink;
	private TextElement urlEl;
	private TextElement loginEl;
	private TextElement passwordEl;
	private TextElement customerEl;
	private MultipleSelectionElement viteroEnabled;
	private MultipleSelectionElement inspireEnabled;
	private SingleSelection timeZoneEl;

	private static final String[] enabledKeys = new String[]{"on"};
	private String[] enabledValues;
	
	@Autowired
	private ViteroModule viteroModule;
	@Autowired
	private ViteroManager viteroManager;
	
	public ViteroConfigurationController(UserRequest ureq, WindowControl wControl) {
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
		
			viteroEnabled = uifactory.addCheckboxesHorizontal("vitero.module.enabled", moduleFlc, enabledKeys, enabledValues);
			viteroEnabled.select(enabledKeys[0], viteroModule.isEnabled());
			viteroEnabled.addActionListener(FormEvent.ONCHANGE);
			
			//spacer
			uifactory.addSpacerElement("Spacer", moduleFlc, false);
			
			List<String> timeZoneKeys = new ArrayList<>(ViteroTimezoneIDs.TIMEZONE_IDS);
			Collections.sort(timeZoneKeys, null);

			String[] timeZoneValues = new String[timeZoneKeys.size()];
			int i=0;
			for(String timeZoneKey:timeZoneKeys) {
				TimeZone timezone = TimeZone.getTimeZone(timeZoneKey);
				if(timezone == null) {
					timeZoneValues[i++] = timeZoneKey;
				} else {
					String value = timezone.getDisplayName(false, TimeZone.LONG);
					timeZoneValues[i++] = timeZoneKey + " ( " + value + " )";
				}
			}

			timeZoneEl = uifactory.addDropdownSingleselect("option.olatTimeZone", moduleFlc,
					timeZoneKeys.toArray(new String[timeZoneKeys.size()]), timeZoneValues, null);
			timeZoneEl.select(viteroModule.getTimeZoneId(), true);
			
			inspireEnabled = uifactory.addCheckboxesHorizontal("option.inspire", moduleFlc, enabledKeys, enabledValues);
			if(viteroModule.isInspire()) {
				inspireEnabled.select(enabledKeys[0], true);
			}
			
			//account configuration
			String vmsUri = viteroModule.getVmsURI().toString();
			urlEl = uifactory.addTextElement("vitero-url", "option.baseurl", 255, vmsUri, moduleFlc);
			urlEl.setExampleKey("option.baseurl.example", null);
			urlEl.setDisplaySize(60);
			String login = viteroModule.getAdminLogin();
			loginEl = uifactory.addTextElement("vitero-login", "option.adminlogin", 32, login, moduleFlc);
			String credential = viteroModule.getAdminPassword();
			if(StringHelper.containsNonWhitespace(credential)) {
				credential = PLACEHOLDER;
			}
			passwordEl = uifactory.addPasswordElement("vitero-password", "option.adminpassword", 32, credential, moduleFlc);
			passwordEl.setAutocomplete("new-password");
			int customerId = viteroModule.getCustomerId();
			String customer = customerId > 0 ? Integer.toString(customerId) : null;
			customerEl = uifactory.addTextElement("option.customerId", "option.customerId", 32, customer, moduleFlc);

			//buttons save - check
			FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("save", getTranslator());
			moduleFlc.add(buttonLayout);
			uifactory.addFormSubmitButton("save", buttonLayout);
			checkLink = uifactory.addFormLink("check", buttonLayout, Link.BUTTON);
			checkUserLink = uifactory.addFormLink("check.users", buttonLayout, Link.BUTTON);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		try {
			String url = urlEl.getValue();
			viteroModule.setVmsURI(new URI(url));
			
			String login = loginEl.getValue();
			viteroModule.setAdminLogin(login);
			
			String credential = passwordEl.getValue();
			if(!PLACEHOLDER.equals(credential)) {
				viteroModule.setAdminPassword(credential);
			}
			String customerId = customerEl.getValue();
			viteroModule.setCustomerId(Integer.parseInt(customerId));
			if(timeZoneEl.isOneSelected()) {
				String timeZoneId = timeZoneEl.getSelectedKey();
				viteroModule.setTimeZoneId(timeZoneId);
			}
			
			viteroModule.setInspire(inspireEnabled.isAtLeastSelected(1));
			
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
		if(viteroModule.isEnabled()) {
			allOk &= validateURL();
			customerEl.clearError();
			String customerIdStr = customerEl.getValue();
			if(!StringHelper.containsNonWhitespace(customerIdStr)) {
				customerEl.setErrorKey("error.customer.invalid", null);
				allOk = false;
			} else {
				int customerId = -1;
				try {
					customerId = Integer.parseInt(customerIdStr);
				} catch(Exception e) {
					customerEl.setErrorKey("error.customer.invalid", null);
					allOk = false;
				}
				
				if(customerId > 0) {
					try {
						String credential = passwordEl.getValue();
						if(PLACEHOLDER.equals(credential)) {
							credential = viteroModule.getAdminPassword();
						}
						boolean ok = viteroManager.checkConnection(urlEl.getValue(), loginEl.getValue(), credential, customerId);
						if(!ok) {
							customerEl.setErrorKey("error.customerDoesntExist", null);
							allOk = false;
						}
					} catch (VmsNotAvailableException e) {
						showError(VmsNotAvailableException.I18N_KEY);
					}
				}
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
		if(source == viteroEnabled) {
			boolean enabled = viteroEnabled.isSelected(0);
			viteroModule.setEnabled(enabled);
		} else if(source == checkLink) {
			if(validateURL()) {
				checkConnection();
			}
		} else if(source == checkUserLink) {
			if(validateURL()) {
				checkUsers();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void checkUsers() {
		try {
			CheckUserInfo infos = viteroManager.checkUsers();
			if(infos.getAuthenticationCreated() == 0 && infos.getAuthenticationDeleted() == 0) {
				showInfo("check.users.ok");
			} else {
				String[] args = new String[] {
						Integer.toString(infos.getAuthenticationCreated()),
						Integer.toString(infos.getAuthenticationDeleted()),
						Integer.toString(infos.getAuthenticationCreated() + infos.getAuthenticationDeleted())
				};
				getWindowControl().setInfo(translate("check.users.nok", args));
			}
		}catch (VmsNotAvailableException e) {
			showError(VmsNotAvailableException.I18N_KEY);
		}
	}
	
	protected boolean checkConnection() {
		String url = urlEl.getValue();
		String login = loginEl.getValue();
		String credential = passwordEl.getValue();
		if(PLACEHOLDER.equals(credential)) {
			credential = viteroModule.getAdminPassword();
		}
		String customerIdObj = customerEl.getValue();

		try {
			int customerId = Integer.parseInt(customerIdObj);
			boolean ok = viteroManager.checkConnection(url, login, credential, customerId);
			if(ok) {
				showInfo("check.ok");
			} else {
				showError("check.nok");
			}
			return ok;
		} catch (NumberFormatException e) {
			showError("error.customer.invalid");
			return false;
		} catch (VmsNotAvailableException e) {
			showError(VmsNotAvailableException.I18N_KEY);
			return false;
		}
	}
}