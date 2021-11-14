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

import org.olat.core.commons.services.sms.spi.WebSMSProvider;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WebSMSConfigurationController extends AbstractSMSConfigurationController {

	private static final String PLACEHOLDER = "xxx-placeholder-xxx";
	
	private TextElement usernameEl;
	private TextElement passwordEl;
	
	private String replacedValue;
	
	@Autowired
	private WebSMSProvider webSmsProvider;
	
	public WebSMSConfigurationController(UserRequest ureq, WindowControl wControl, Form form) {
		super(ureq, wControl, form);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String uname = webSmsProvider.getUsername();
		usernameEl = uifactory.addTextElement("websms.username", 128, uname, formLayout);
		usernameEl.setMandatory(true);
		
		String creds = webSmsProvider.getPassword();
		if(StringHelper.containsNonWhitespace(creds)) {
			replacedValue = creds;
			creds = PLACEHOLDER;
		}
		passwordEl = uifactory.addPasswordElement("websms.password", "websms.password", 128, creds, formLayout);
		passwordEl.setMandatory(true);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		usernameEl.clearError();
		if(!StringHelper.containsNonWhitespace(usernameEl.getValue())) {
			usernameEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		passwordEl.clearError();
		if(!StringHelper.containsNonWhitespace(passwordEl.getValue())) {
			passwordEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(!PLACEHOLDER.equals(passwordEl.getValue())) {
			replacedValue = passwordEl.getValue();
			passwordEl.setValue(PLACEHOLDER);
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		webSmsProvider.setUsername(usernameEl.getValue());
		
		String credential = passwordEl.getValue();
		if(!PLACEHOLDER.equals(credential)) {
			webSmsProvider.setPassword(credential);
			passwordEl.setValue(PLACEHOLDER);
		} else if(StringHelper.containsNonWhitespace(replacedValue)) {
			webSmsProvider.setPassword(replacedValue);
		}
	}
}
