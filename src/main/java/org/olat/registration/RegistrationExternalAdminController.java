/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.registration;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.UrlElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Feb 25, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RegistrationExternalAdminController extends FormBasicController {


	@Autowired
	private RegistrationModule registrationModule;

	public RegistrationExternalAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "admin_external");

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer externalContainer = FormLayoutContainer.createDefaultFormLayout("externalCont", getTranslator());
		externalContainer.setRootForm(mainForm);
		externalContainer.setFormTitle(translate("remote.login.title"));
		formLayout.add(externalContainer);

		String selfRegLink = Settings.getServerContextPathURI() + "/url/registration/0";
		UrlElement selfRegLinkEl = uifactory.addUrlElement("admin.registration.code.reg", "admin.registration.code.reg", selfRegLink, externalContainer);
		selfRegLinkEl.setVisible(registrationModule.isSelfRegistrationLinkEnabled());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
