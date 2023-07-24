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
package org.olat.modules.cemedia.ui.medias;

import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.commons.services.license.manager.LicenseTypeDAO;
import org.olat.core.commons.services.license.ui.LicenseSelectionConfig;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaCenterLicenseHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 juil. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractCollectMediaController extends FormBasicController {
	
	private SingleSelection licenseEl;
	private TextElement licensorEl;
	private TextElement licenseFreetextEl;
	
	protected Media mediaReference;
	private ResourceLicense license;
	
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private MediaCenterLicenseHandler licenseHandler;
	
	public AbstractCollectMediaController(UserRequest ureq, WindowControl wControl, Media media, Translator translator) {
		super(ureq, wControl, translator);
		this.mediaReference = media;
		if(media != null) {
			license = licenseService.loadLicense(media);
		}
	}
	
	protected void initLicenseForm(FormItemContainer formLayout) {
		if (licenseModule.isEnabled(licenseHandler)) {
			LicenseSelectionConfig licenseSelectionConfig;
			if(license != null) {
				licenseSelectionConfig = LicenseUIFactory.createLicenseSelectionConfig(licenseHandler, license.getLicenseType());
			} else {
				licenseSelectionConfig = LicenseUIFactory.createLicenseSelectionConfig(licenseHandler);
			}
			
			licenseEl = uifactory.addDropdownSingleselect("rights.license", formLayout,
					licenseSelectionConfig.getLicenseTypeKeys(),
					licenseSelectionConfig.getLicenseTypeValues(getLocale()));
			licenseEl.setElementCssClass("o_sel_repo_license");
			licenseEl.setMandatory(licenseSelectionConfig.isLicenseMandatory());
			if (licenseSelectionConfig.getSelectionLicenseTypeKey() != null) {
				licenseEl.select(licenseSelectionConfig.getSelectionLicenseTypeKey(), true);
			} else {
				String noLicenseKey = licenseService.loadLicenseTypeByName(LicenseTypeDAO.NO_LICENSE_NAME).getKey().toString();
				licenseEl.select(noLicenseKey, true);
			}
			licenseEl.addActionListener(FormEvent.ONCHANGE);
			
			String licensor = license == null ? null :  license.getLicensor();
			licensorEl = uifactory.addTextElement("rights.licensor", 1000, licensor, formLayout);

			String freetext = license != null && licenseService.isFreetext(license.getLicenseType())
					? license.getFreetext() : null;
			licenseFreetextEl = uifactory.addTextAreaElement("rights.freetext", 4, 72, freetext, formLayout);
			updateUILicense();
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (licenseEl != null) {
			licenseEl.clearError();
			if (LicenseUIFactory.validateLicenseTypeMandatoryButNonSelected(licenseEl)) {
				licenseEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == licenseEl) {
			updateUILicense();
		}
	}
	
	protected void updateUILicense() {
		LicenseUIFactory.updateVisibility(licenseEl, licensorEl, licenseFreetextEl);
	}
	
	protected void setLicenseVisibility(boolean visible) {
		if(visible && !licenseEl.isVisible() && !licenseEl.isOneSelected()) {
			String noLicenseKey = licenseService.loadLicenseTypeByName(LicenseTypeDAO.NO_LICENSE_NAME).getKey().toString();
			licenseEl.select(noLicenseKey, true);
		}
		
		licenseEl.setVisible(visible);
		licensorEl.setVisible(visible);
		licenseFreetextEl.setVisible(visible);
	}
	
	protected void saveLicense() {
		if (licenseModule.isEnabled(licenseHandler)) {
			license = licenseService.loadOrCreateLicense(mediaReference);
			
			if (licenseEl != null && licenseEl.isOneSelected()) {
				String licenseTypeKey = licenseEl.getSelectedKey();
				LicenseType licneseType = licenseService.loadLicenseTypeByKey(licenseTypeKey);
				license.setLicenseType(licneseType);
			}
			String licensor = null;
			String freetext = null;
			if (licensorEl != null && licensorEl.isVisible()) {
				licensor = StringHelper.containsNonWhitespace(licensorEl.getValue())? licensorEl.getValue(): null;
			}
			if (licenseFreetextEl != null && licenseFreetextEl.isVisible()) {
				freetext = StringHelper.containsNonWhitespace(licenseFreetextEl.getValue())? licenseFreetextEl.getValue(): null;
			}
			license.setLicensor(licensor);
			license.setFreetext(freetext);
			license = licenseService.update(license);
			if(licensorEl != null && licenseFreetextEl != null) {
				licensorEl.setValue(license.getLicensor());
				licenseFreetextEl.setValue(license.getFreetext());
			}
		}
	}
}
