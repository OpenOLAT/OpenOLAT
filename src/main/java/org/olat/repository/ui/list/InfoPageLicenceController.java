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
package org.olat.repository.ui.list;

import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryLicenseHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 18 Aug 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class InfoPageLicenceController extends BasicController {

	private boolean hasContent;

	@Autowired
	private LicenseService licenseService;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private RepositoryEntryLicenseHandler licenseHandler;

	public InfoPageLicenceController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(),
				Util.createPackageTranslator(LicenseUIFactory.class, getLocale(), getTranslator())));

		if (licenseModule.isEnabled(licenseHandler)) {
			License license = licenseService.loadOrCreateLicense(entry.getOlatResource());
			LicenseType licenseType = license.getLicenseType();
			if (!licenseService.isNoLicense(licenseType)) {
				hasContent = true;
				VelocityContainer mainVC = createVelocityContainer("licence");
				mainVC.contextPut("licenseIconCss", LicenseUIFactory.getCssOrDefault(licenseType));
				mainVC.contextPut("license", LicenseUIFactory.translate(licenseType, getLocale()));
				String licensor = StringHelper.containsNonWhitespace(license.getLicensor()) ? license.getLicensor() : "";
				mainVC.contextPut("licensor", licensor);
				mainVC.contextPut("licenseText", LicenseUIFactory.getFormattedLicenseText(license));
				putInitialPanel(mainVC);
				return;
			}
		}

		putInitialPanel(new Panel("empty"));
	}

	public boolean hasContent() {
		return hasContent;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
