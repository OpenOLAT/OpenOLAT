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
package org.olat.core.commons.services.license.ui;

import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 27.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LicenseQuickviewController extends FormBasicController {

	private final License license;
	private final LicenseType licenseType;
	
	public LicenseQuickviewController(UserRequest ureq, WindowControl wControl, License license) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.license = license;
		this.licenseType = license.getLicenseType();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String type = licenseType != null? LicenseUIFactory.translate(licenseType, getLocale()): "";
		uifactory.addStaticTextElement("license.quickview.type", type, formLayout);

		String licensor = StringHelper.containsNonWhitespace(license.getLicensor())? license.getLicensor(): "";
		uifactory.addStaticTextElement("license.quickview.licensor", licensor, formLayout);
		
		uifactory.addStaticTextElement("license.quickview.text", LicenseUIFactory.getFormattedLicenseText(license),
				formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
