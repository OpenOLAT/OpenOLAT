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
package org.olat.course.certificate.ui;

import static org.olat.core.gui.translator.TranslatorHelper.translateAll;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.EmailAddressValidator;
import org.olat.course.certificate.CertificatesModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 31 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificatesSettingsAdminController extends FormBasicController {
	
	private static final String[] onKeys = new String[]{ "on" };

	private TextElement bccEl;
	private MultipleSelectionElement userCanUploadExternalEl;
	private MultipleSelectionElement userManagerCanUploadExternalEl;
	private MultipleSelectionElement enableBccEl;
	private MultipleSelectionElement enableLinemanagerEl;
	
	@Autowired
	private CertificatesModule certificatesModule;
	
	/**
	 * @param name
	 * @param chatEnabled
	 */
	public CertificatesSettingsAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm (ureq);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.certificates.options.title");
		
		SelectionValue enableUpload = new SelectionValue("on", translate("on"));
		SelectionValues uploadOptions = new SelectionValues(enableUpload);
		
		userCanUploadExternalEl = uifactory.addCheckboxesVertical("admin.certificates.upload.user", formLayout, uploadOptions.keys(), uploadOptions.values(), 1);
		userCanUploadExternalEl.select(enableUpload.getKey(), certificatesModule.canUserUploadExternalCertificates());
		
		userManagerCanUploadExternalEl = uifactory.addCheckboxesVertical("admin.certificates.upload.user.manager", formLayout, uploadOptions.keys(), uploadOptions.values(), 1);
		userManagerCanUploadExternalEl.select(enableUpload.getKey(), certificatesModule.canUserManagerUploadExternalCertificates());
		
		// Add instruction sample
		String page = Util.getPackageVelocityRoot(getClass()) + "/external_certificates_hint.html";
		FormLayoutContainer instructions = FormLayoutContainer.createCustomFormLayout("instructions", getTranslator(), page);
		instructions.setLabel("no.text", null);
		formLayout.add(instructions);

		String bcc = certificatesModule.getCertificateBcc();
		enableBccEl = uifactory.addCheckboxesHorizontal("enableBcc", "admin.certificates.bcc.enable", formLayout,
				onKeys, translateAll(getTranslator(), onKeys));
		enableBccEl.setHelpText(translate("admin.certificates.bcc.enable.help"));
		enableBccEl.addActionListener(FormEvent.ONCHANGE);
		if(StringHelper.containsNonWhitespace(bcc)) {
			enableBccEl.select(onKeys[0], true);
		}
		
		bccEl = uifactory.addTextElement("bcc", "admin.certificates.bcc", 1024, bcc, formLayout);
		bccEl.setVisible(enableBccEl.isAtLeastSelected(1));
		
		enableLinemanagerEl = uifactory.addCheckboxesHorizontal("admin.certificates.linemanager", formLayout, onKeys,
				translateAll(getTranslator(), onKeys));
		enableLinemanagerEl.select(onKeys[0], certificatesModule.isCertificateLinemanager());
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		bccEl.clearError();
		if(enableBccEl.isAtLeastSelected(1)) {
			String emails = bccEl.getValue();
			if(!StringHelper.containsNonWhitespace(emails)) {
				bccEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			} else {
				List<String> emailList = certificatesModule.splitEmails(bccEl.getValue());
				for(String email:emailList) {
					if(!EmailAddressValidator.isValidEmailAddress(email)) {
						bccEl.setErrorKey("error.mail.invalid", null);
						allOk &= false;
					}
				}
			}
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableBccEl == source) {
			bccEl.setVisible(enableBccEl.isAtLeastSelected(1));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(enableBccEl.isAtLeastSelected(1)) {
			certificatesModule.setCertificateBcc(bccEl.getValue());
		} else {
			certificatesModule.setCertificateBcc("");
		}
		
		boolean linemanager = enableLinemanagerEl.isAtLeastSelected(1);
		certificatesModule.setCertificateLinemanager(linemanager);
		
		certificatesModule.setUserCanUploadExternalCertificates(userCanUploadExternalEl.isAtLeastSelected(1));
		certificatesModule.setUserManagerCanUploadExternalCertificates(userManagerCanUploadExternalEl.isAtLeastSelected(1));
	}
}