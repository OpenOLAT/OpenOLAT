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
package org.olat.repository.ui.list;

import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryLicenseHandler;
import org.olat.resource.accesscontrol.ACService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryDetailsLinkController extends FormBasicController {
	
	private final RepositoryEntry entry;
	
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private RepositoryEntryLicenseHandler licenseHandler;
	@Autowired
	protected ACService acService;

	public RepositoryEntryDetailsLinkController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, Util.getPackageVelocityRoot(RepositoryEntryDetailsController.class) + "/details_link.html");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.entry = entry;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			
			// License
			boolean licEnabled = licenseModule.isEnabled(licenseHandler);
			if (licEnabled) {
				License license = licenseService.loadOrCreateLicense(entry.getOlatResource());
				LicenseType licenseType = license.getLicenseType();
				if (licenseService.isNoLicense(licenseType)) {
					// dont' show the no-license
					layoutCont.contextPut("licSwitch", Boolean.FALSE);
				} else {
					layoutCont.contextPut("licSwitch", Boolean.TRUE);
					layoutCont.contextPut("license", LicenseUIFactory.translate(licenseType, getLocale()));
					layoutCont.contextPut("licenseIconCss", LicenseUIFactory.getCssOrDefault(licenseType));
					String licensor = StringHelper.containsNonWhitespace(license.getLicensor())? license.getLicensor(): "";
					layoutCont.contextPut("licensor", licensor);
					layoutCont.contextPut("licenseText", LicenseUIFactory.getFormattedLicenseText(license));	
				}
			} else {
				layoutCont.contextPut("licSwitch", Boolean.FALSE);
			}
			
			// Link to bookmark entry
			String url = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + entry.getKey();
			layoutCont.contextPut("extlink", url);
			Boolean guestAllowed = Boolean.valueOf(entry.isPublicVisible() && acService.isGuestAccessible(entry, false));
			layoutCont.contextPut("isGuestAllowed", guestAllowed);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
