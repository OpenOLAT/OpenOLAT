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
package org.olat.admin.restapi;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.restapi.RestModule;
import org.olat.restapi.security.RestSecurityHelper;

/**
 * 
 * Description:<br>
 * This is a controller to configure the SimpleVersionConfig, the configuration
 * of the versioning system for briefcase.
 * 
 * <P>
 * Initial Date:  21 sept. 2009 <br>
 *
 * @author srosse
 */
public class RestapiAdminController extends FormBasicController {
	
	private MultipleSelectionElement enabled;
	private FormLayoutContainer docLinkFlc;
	private FormLayoutContainer accessDataFlc;
	
	private String[] values = {""};
	private String[] keys = {"on"};

	public RestapiAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "rest");

		values = new String[] {
			getTranslator().translate("rest.on")
		};
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("rest.title");
		setFormContextHelp(RestapiAdminController.class.getPackage().getName(), "rest.html", "help.hover.rest");
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutContainer = (FormLayoutContainer)formLayout;
			
			boolean restEnabled = isEnabled();
			
			docLinkFlc = FormLayoutContainer.createCustomFormLayout("doc_link", getTranslator(), velocity_root + "/docLink.html");
			layoutContainer.add(docLinkFlc);
			docLinkFlc.setVisible(restEnabled);
			
			String link = Settings.getServerContextPathURI() + RestSecurityHelper.SUB_CONTEXT + "/api/doc";
			docLinkFlc.contextPut("docLink", link);
			
			accessDataFlc = FormLayoutContainer.createDefaultFormLayout("flc_access_data", getTranslator());
			layoutContainer.add(accessDataFlc);

			enabled = uifactory.addCheckboxesHorizontal("rest.enabled", accessDataFlc, keys, values, null);
			enabled.select(keys[0], restEnabled);
			
			accessDataFlc.setVisible(true);
	
			final FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
			buttonGroupLayout.setRootForm(mainForm);
			accessDataFlc.add(buttonGroupLayout);
			
			uifactory.addFormSubmitButton("save", buttonGroupLayout);
			formLayout.add(accessDataFlc);
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean on = !enabled.getSelectedKeys().isEmpty();
		setEnabled(on);
		docLinkFlc.setVisible(on);
		getWindowControl().setInfo("saved");
	}
	
	private boolean isEnabled() {
		RestModule config = (RestModule) CoreSpringFactory.getBean("restModule");
		return config.isEnabled();
	}
	
	private void setEnabled(boolean enabled) {
		RestModule config = (RestModule) CoreSpringFactory.getBean("restModule");
		config.setEnabled(enabled);
	}
}
