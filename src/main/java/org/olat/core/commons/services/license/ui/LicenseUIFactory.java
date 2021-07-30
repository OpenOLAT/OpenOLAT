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

import org.apache.logging.log4j.Level;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseHandler;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
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
	
	/**
	 * Get configuration for a SingleSelectionElement. The keys and values contains
	 * all active LicenseTypes for the LicenseHandler.
	 *
	 * @param handler
	 * @return
	 */
	public static LicenseSelectionConfig createLicenseSelectionConfig(LicenseHandler handler) {
		return new LicenseSelectionConfig(handler);
	}
	
	/**
	 * Get configuration for a SingleSelectionElement. The keys and values contains
	 * all active LicenseTypes for the LicenseHandler. The actual LicenseType is
	 * always in the configuration, even if it is not active. It is marked as
	 * "inactive" in the values.
	 *
	 * @param handler
	 * @param actualLicenseType
	 * @return
	 */
	public static LicenseSelectionConfig createLicenseSelectionConfig(LicenseHandler handler, LicenseType actualLicenseType) {
		return new LicenseSelectionConfig(handler, actualLicenseType);
	}
	
	public static String translate(LicenseType licenseType, Locale locale) {
		Translator translator = Util.createPackageTranslator(LicenseAdminConfigController.class, locale);
		String i18nKey = LICENSE_TYPE_TRANS + licenseType.getName().toLowerCase();
		// It is ok to have missing i18n keys, the license name itself is shown in this case.
		// So do not log missing keys and pollute the log file.
		String translation = translator.translate(i18nKey, null, Level.OFF);
		if(i18nKey.equals(translation) || translation.length() > 256) {
			translation = licenseType.getName();
		}
		return translation;
	}
	
	public static String getFormattedLicenseText(License license) {
		String licenseText = getLicenseText(license);
		return Formatter.formatURLsAsLinks(Formatter.escWithBR(licenseText).toString(), true);
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
		String cssClass = "";
		if (licenseType != null)
			if (StringHelper.containsNonWhitespace(licenseType.getCssClass())) {
				cssClass = licenseType.getCssClass();
			} else {
				cssClass = "o_icon_lic_general";
		}
		return cssClass;
	}

	public static boolean validateLicenseTypeMandatoryButNonSelected(SingleSelection licenseEl) {
		if (licenseEl == null) return false;
		if (!licenseEl.isMandatory()) return false;
		
		LicenseService licenseService = CoreSpringFactory.getImpl(LicenseService.class);
		boolean isNoLicenseSelected = false;
		if (licenseEl.isOneSelected()) {
			String selectedKey = licenseEl.getSelectedKey();
			LicenseType selectedLicenseType = licenseService.loadLicenseTypeByKey(selectedKey);
			isNoLicenseSelected = licenseService.isNoLicense(selectedLicenseType);
		}
		return isNoLicenseSelected;
	}
	
	public static void updateVisibility(SingleSelection licenseEl, TextElement licensorEl, TextElement licenseFreetextEl) {
		boolean licenseSelected = false;
		boolean freetextSelected = false;
		if (licenseEl != null && licenseEl.isOneSelected()) {
			LicenseService licenseService = CoreSpringFactory.getImpl(LicenseService.class);
			String selectedKey = licenseEl.getSelectedKey();
			LicenseType licenseType = licenseService.loadLicenseTypeByKey(selectedKey);
			licenseSelected = !licenseService.isNoLicense(licenseType);
			freetextSelected = licenseService.isFreetext(licenseType);
		}
		if (licensorEl != null) {
			licensorEl.setVisible(licenseSelected);
		}
		if (licenseFreetextEl != null) {
			licenseFreetextEl.setVisible(freetextSelected);
		}
	}

}
