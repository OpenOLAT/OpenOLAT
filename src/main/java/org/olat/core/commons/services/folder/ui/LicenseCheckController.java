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
package org.olat.core.commons.services.folder.ui;

import java.util.List;
import java.util.function.Consumer;

import org.olat.core.commons.modules.bc.FolderLicenseHandler;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ui.LicenseSelectionConfig;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 Apr 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class LicenseCheckController extends FormBasicController {
	
	private SingleSelection licenseEl;
	private TextElement licensorEl;
	private TextAreaElement licenseFreetextEl;
	
	private final VFSContainer targetContainer;
	private final List<VFSItem> itemsToCopy;
	private final int numMissingLicenses;
	private final Consumer<List<String>> successMessage;

	@Autowired
	private LicenseService licenseService;
	@Autowired
	private FolderLicenseHandler licenseHandler;

	protected LicenseCheckController(UserRequest ureq, WindowControl wControl, VFSContainer targetContainer,
			List<VFSItem> itemsToCopy, int numMissingLicenses, Consumer<List<String>> successMessage) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(LicenseUIFactory.class, getLocale(), getTranslator()));
		this.targetContainer = targetContainer;
		this.itemsToCopy = itemsToCopy;
		this.numMissingLicenses = numMissingLicenses;
		this.successMessage = successMessage;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormInfo("license.check.info", new String[] {String.valueOf(numMissingLicenses)});
		
		License defaultLicense = licenseService.createDefaultLicense(licenseHandler, getIdentity());
		
		LicenseSelectionConfig licenseSelectionConfig = LicenseUIFactory
				.createLicenseSelectionConfig(licenseHandler, defaultLicense.getLicenseType());
		licenseEl = uifactory.addDropdownSingleselect("license", formLayout,
				licenseSelectionConfig.getLicenseTypeKeys(),
				licenseSelectionConfig.getLicenseTypeValues(getLocale()));
		licenseEl.setMandatory(licenseSelectionConfig.isLicenseMandatory());
		if (licenseSelectionConfig.getSelectionLicenseTypeKey() != null) {
			licenseEl.select(licenseSelectionConfig.getSelectionLicenseTypeKey(), true);
		}
		licenseEl.addActionListener(FormEvent.ONCHANGE);
		
		licensorEl = uifactory.addTextElement("licensor", 1000, defaultLicense.getLicensor(), formLayout);
		
		String freetext = licenseService.isFreetext(defaultLicense.getLicenseType()) ? defaultLicense.getFreetext() : "";
		licenseFreetextEl = uifactory.addTextAreaElement("freetext", 4, 72, freetext, formLayout);
		LicenseUIFactory.updateVisibility(licenseEl, licensorEl, licenseFreetextEl);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", "ok", buttonsCont);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == licenseEl) {
			LicenseUIFactory.updateVisibility(licenseEl, licensorEl, licenseFreetextEl);
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	public VFSContainer getTargetContainer() {
		return targetContainer;
	}

	public List<VFSItem> getItemsToCopy() {
		return itemsToCopy;
	}

	public Consumer<List<String>> getSuccessMessage() {
		return successMessage;
	}

	public License getLicense() {
		if (licenseEl.isOneSelected()) {
			License license = licenseService.createLicense(null);
			String licenseTypeKey = licenseEl.getSelectedKey();
			LicenseType licneseType = licenseService.loadLicenseTypeByKey(licenseTypeKey);
			license.setLicenseType(licneseType);
			if (licensorEl.isVisible() && StringHelper.containsNonWhitespace(licensorEl.getValue())) {
				String licensor = licensorEl.getValue();
				license.setLicensor(licensor);
			}
			if (licenseFreetextEl.isVisible() && StringHelper.containsNonWhitespace(licenseFreetextEl.getValue())) {
				String freetext = licenseFreetextEl.getValue();
				license.setFreetext(freetext);
			}
			return license;
		}
		
		return null;
	}

}
