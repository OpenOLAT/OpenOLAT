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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.license.LicenseHandler;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 22.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LicenseSelectionConfig {
	
	private final LicenseType actualLicenseType;
	private List<LicenseType> activeLicenseTypes;
	private boolean mandatory;
	private List<LicenseType> selectableLicenseTypes;
	private boolean actualLicenseTypeIsInactive = false;
	
	private final LicenseService licenseService;

	public LicenseSelectionConfig(LicenseHandler licenseHandler) {
		this(CoreSpringFactory.getImpl(LicenseService.class), licenseHandler, null);
	}
		
	public LicenseSelectionConfig(LicenseHandler licenseHandler, LicenseType actualLicenseType) {
		this(CoreSpringFactory.getImpl(LicenseService.class), licenseHandler, actualLicenseType);
	}

	// Used for testing
	LicenseSelectionConfig(LicenseService licenseService, LicenseHandler licenseHandler, LicenseType actualLicenseType) {
		this.licenseService = licenseService;
		this.actualLicenseType = actualLicenseType;
		init(licenseHandler);
	}

	private void init(LicenseHandler licenseHandler) {
		activeLicenseTypes = licenseService.loadActiveLicenseTypes(licenseHandler);
		mandatory = checkMandatory(activeLicenseTypes);
		initSelectableLicenseTypes();
	}

	private boolean checkMandatory(List<LicenseType> licenseTypes) {
		for (LicenseType licenseType: licenseTypes) {
			if (licenseService.isNoLicense(licenseType)) {
				return false;
			}
		}
		return true;
	}

	private void initSelectableLicenseTypes() {
		selectableLicenseTypes = new ArrayList<>(activeLicenseTypes);
		Collections.sort(selectableLicenseTypes);
		boolean actualLicenceTypeMissing = actualLicenseType != null && !selectableLicenseTypes.contains(actualLicenseType);
		if (actualLicenceTypeMissing) {
			selectableLicenseTypes.add(0, actualLicenseType);
			actualLicenseTypeIsInactive = true;
		}
	}

	public boolean isLicenseMandatory() {
		return mandatory;
	}
	
	public String[] getLicenseTypeKeys() {
		return selectableLicenseTypes.stream()
				.map(LicenseType::getKey)
				.map(String::valueOf)
				.toArray(String[]::new);
	}

	public String[] getLicenseTypeValues(Locale locale) {
		String[] values = new String[selectableLicenseTypes.size()];
		int count = 0;
		for(LicenseType licenseType: selectableLicenseTypes) {
			String translation = LicenseUIFactory.translate(licenseType, locale);
			translation = setSpecialTranslation(translation, licenseType, locale);
			values[count++] = translation;
		}
		return values;
	}
	
	private String setSpecialTranslation(String translation, LicenseType licenseType, Locale locale) {
		Translator translator = Util.createPackageTranslator(LicenseAdminConfigController.class, locale);
		String specialTranslation = translation;
		if (actualLicenseTypeIsInactive && licenseType.equals(actualLicenseType)) {
			if (licenseService.isNoLicense(actualLicenseType)) {
				specialTranslation = translator.translate("license.type.missing");
			} else {
				specialTranslation = translator.translate("license.type.inactive", new String[] {translation});
			}
		}
		return specialTranslation;
	}

	public String getSelectionLicenseTypeKey() {
		if (actualLicenseType == null) return null;
		if (mandatory && licenseService.isNoLicense(actualLicenseType)) return null;
		
		return String.valueOf(actualLicenseType.getKey());
	}

}
