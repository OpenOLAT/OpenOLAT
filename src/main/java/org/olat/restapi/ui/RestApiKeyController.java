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
package org.olat.restapi.ui;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.restapi.security.RestApiAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 oct. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RestApiKeyController extends FormBasicController {
	
	private final String clientId;
	private final String clientSecret;
	private final Identity changeableIdentity;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RestApiAuthenticationProvider apiProvider;
	
	public RestApiKeyController(UserRequest ureq, WindowControl wControl, Identity changeableIdentity) {
		super(ureq, wControl);
		this.changeableIdentity = changeableIdentity;
		clientId = apiProvider.generateClientId();
		clientSecret = apiProvider.generateClientSecret();

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("api.key.title");
		setFormWarning("api.key.desc", null);
		
		uifactory.addStaticTextElement("api.client.id", clientId, formLayout);
		uifactory.addStaticTextElement("api.client.secret", clientSecret, formLayout);
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save.api.key", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		apiProvider.setClientAuthentication(changeableIdentity, clientId, clientSecret);
		dbInstance.commitAndCloseSession();
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
