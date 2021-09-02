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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.CertificatesModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 30.08.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class UploadExternalCertificateController extends FormBasicController {
	
	private Identity assessedIdentity;
	
	private TextElement titleEl;
	private DateChooser issuedDateEl;
	private FileElement certificateEl;
	
	@Autowired
	CertificatesManager certificatesManager;
	@Autowired
	CertificatesModule certificatesModule;

	public UploadExternalCertificateController(UserRequest ureq, WindowControl wControl, Identity assessedIdentity) {
		super(ureq, wControl);
		
		this.assessedIdentity = assessedIdentity;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		titleEl = uifactory.addTextElement("certificate.title", 255, null, formLayout);
		issuedDateEl = uifactory.addDateChooser("certificate.date", null, formLayout);
		certificateEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "certificate.file", formLayout);
		certificateEl.setMaxUploadSizeKB(certificatesModule.getUploadLimit() * 1024, "certiicate.file.error", null);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		buttonLayout.setRootForm(mainForm);
		formLayout.add(buttonLayout);
		
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		titleEl.clearError();
		if (!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		issuedDateEl.clearError();
		if (issuedDateEl.getDate() == null) {
			issuedDateEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		certificateEl.clearError();
		if (certificateEl.getUploadFile() == null || !certificateEl.getUploadFileName().endsWith(".pdf")) {
			allOk &= false;
			certificateEl.setErrorKey("certificate.file.error", null);
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		certificatesManager.uploadStandaloneCertificate(assessedIdentity, issuedDateEl.getDate(), titleEl.getValue(), -1l, certificateEl.getUploadFile());
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void doDispose() {
		
	}

}
