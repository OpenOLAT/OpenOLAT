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

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseHandler;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 26.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LicenseUIFactory {
	
	static final String LICENSE_TYPE_TRANS = "license.type.trans.";

	private LicenseUIFactory() {
		// should not be instantiated
	}
	
	public static LicenseSelectionConfig createLicenseSelectionConfig(LicenseHandler handler, License license) {
		return new LicenseSelectionConfig(handler, license);
	}
	
	public static String translate(LicenseType licenseType, Locale locale) {
		Translator translator = Util.createPackageTranslator(LicenseAdminConfigController.class, locale);
		String i18nKey = LICENSE_TYPE_TRANS + licenseType.getName().toLowerCase();
		String translation = translator.translate(i18nKey);
		if(i18nKey.equals(translation) || translation.length() > 256) {
			translation = licenseType.getName();
		}
		return translation;
	}
	
	public static String getFormattedLicenseText(License license) {
		String licenseText = getLicenseText(license);
		return Formatter.formatURLsAsLinks(Formatter.escWithBR(licenseText).toString());
	}
	
	public static String getLicenseText(License license) {
		LicenseService licenseService = CoreSpringFactory.getImpl(LicenseService.class);
		String licenseText = "";
		if (license != null && license.getLicenseType() != null) {
			LicenseType licenseType = license.getLicenseType();
			if (licenseService.isFreetext(licenseType) && StringHelper.containsNonWhitespace(license.getFreetext())) {
				licenseText = license.getFreetext();
			} else if (StringHelper.containsNonWhitespace(licenseType.getText())) {
				licenseText = licenseType.getText();
			}
		}
		return licenseText;
	}
	
	public static String getCssOrDefault(LicenseType licenseType) {
		if (licenseType != null && StringHelper.containsNonWhitespace(licenseType.getCssClass())) {
			return licenseType.getCssClass();
		}
		return "o_icon_lic_general";
	}

}
