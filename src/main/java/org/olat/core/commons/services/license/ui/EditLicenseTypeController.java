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

import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EditLicenseTypeController extends FormBasicController {

	private static final String LICENSE_TYPE_OER_KEY = "license.type.oer";
	
	private TextElement nameEl;
	private TextElement textEl;
	private TextElement cssClassEl;
	private MultipleSelectionElement oerEl;
	
	private LicenseType licenseType;
	
	@Autowired
	private LicenseService licenseService;

	public EditLicenseTypeController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, null);
	}
	
	public EditLicenseTypeController(UserRequest ureq, WindowControl wControl, LicenseType licenseType) {
		super(ureq, wControl);
		this.licenseType = licenseType;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (licenseType == null) {
			nameEl = uifactory.addTextElement("license.type.name", 100, null, formLayout);
			nameEl.setMandatory(true);
		} else {
			uifactory.addStaticTextElement("license.type.name", licenseType.getName(), formLayout);
		}
		
		String text = licenseType == null ? "" : licenseType.getText();
		textEl = uifactory.addTextAreaElement("license.type.text", 4, 72, text, formLayout);
		
		String cssClass = licenseType == null ? "" : licenseType.getCssClass();
		cssClassEl = uifactory.addTextElement("license.type.css.class", 64, cssClass, formLayout);

		String[] oerKeys = new String[]{LICENSE_TYPE_OER_KEY};
		String[] oerValues = new String[]{translate("license.type.oer.qualifies")};

		oerEl = uifactory.addCheckboxesVertical(LICENSE_TYPE_OER_KEY, formLayout, oerKeys, oerValues, 1);
		oerEl.select(oerKeys[0], licenseType != null && licenseType.isOerLicense());
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
	
		if (nameEl != null) {
			nameEl.clearError();
			if (!StringHelper.containsNonWhitespace(nameEl.getValue())) {
				nameEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			} else if (licenseService.licenseTypeExists(nameEl.getValue())) {
				nameEl.setErrorKey("error.license.type.name.exists", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (licenseType == null) {
			licenseType = licenseService.createLicenseType(nameEl.getValue());
		}
		if (oerEl.isEnabled()) {
			licenseType.setOerLicense(oerEl.isKeySelected(LICENSE_TYPE_OER_KEY));
		}
		String text = StringHelper.containsNonWhitespace(textEl.getValue())
				? textEl.getValue()
				: null;
		licenseType.setText(text);
		String cssClass = StringHelper.containsNonWhitespace(cssClassEl.getValue())
				? cssClassEl.getValue()
				: null;
		licenseType.setCssClass(cssClass);
		licenseType = licenseService.saveLicenseType(licenseType);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
