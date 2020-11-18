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
package org.olat.ims.qti21.ui;

import java.io.File;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.crypto.CryptoUtil;
import org.olat.core.util.crypto.X509CertificatePrivateKeyPair;
import org.olat.ims.qti21.QTI21Module;
import org.olat.ims.qti21.QTI21Module.CorrectionWorkflow;
import org.olat.ims.qti21.ui.assessment.ValidationXmlSignatureController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Administration for system-wide settings.
 * 
 * Initial date: 25.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21AdminController extends FormBasicController {
	
	private static final String PLACEHOLDER = "xOOx32x00x";

	private static final String[] onKeys = new String[]{ "on" };
	private static final String[] onValues = new String[]{ "" };
	private static final String[] visibilityKeys = new String[] { "visible", "not-visible" };
	
	private FormLink validationButton;
	private MultipleSelectionElement mathExtensionEl;
	private MultipleSelectionElement digitalSignatureEl;
	private MultipleSelectionElement anonymCorrectionWorkflowEl;
	private SingleSelection resultsVisibilityAfterCorrectionEl;
	private FileElement certificateEl;
	private TextElement certificatePasswordEl;
	
	private CloseableModalController cmc;
	private ValidationXmlSignatureController validationCtrl;

	@Autowired
	private QTI21Module qti21Module;
	
	public QTI21AdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.title");

		validationButton = uifactory.addFormLink("validate.xml.signature", formLayout, Link.BUTTON);
		validationButton.setCustomEnabledLinkCSS("btn btn-default pull-right");
		validationButton.getComponent().setSuppressDirtyFormWarning(true);
		
		digitalSignatureEl = uifactory.addCheckboxesHorizontal("digital.signature", "digital.signature", formLayout,
				onKeys, onValues);
		if(qti21Module.isDigitalSignatureEnabled()) {
			digitalSignatureEl.select(onKeys[0], true);
		}
		digitalSignatureEl.setExampleKey("digital.signature.text", null);
		digitalSignatureEl.addActionListener(FormEvent.ONCHANGE);
		
		certificateEl = uifactory.addFileElement(getWindowControl(), "digital.signature.certificate", "digital.signature.certificate", formLayout);
		certificateEl.setExampleKey("digital.signature.certificate.example", null);
		certificateEl.setHelpText(translate("digital.signature.certificate.hint"));
		if(StringHelper.containsNonWhitespace(qti21Module.getDigitalSignatureCertificate())) {
			File certificate = qti21Module.getDigitalSignatureCertificateFile();
			certificateEl.setInitialFile(certificate);
		}
		
		String certificatePassword = qti21Module.getDigitalSignatureCertificatePassword();
		String password = StringHelper.containsNonWhitespace(certificatePassword) ? PLACEHOLDER : "";
		certificatePasswordEl = uifactory.addPasswordElement("digital.signature.certificate.password", "digital.signature.certificate.password",
				256, password, formLayout);
		certificatePasswordEl.setAutocomplete("new-password");
		
		anonymCorrectionWorkflowEl = uifactory.addCheckboxesHorizontal("correction.workflow", "correction.workflow", formLayout,
				onKeys, new String[] { translate("correction.workflow.anonymous") });
		if(qti21Module.getCorrectionWorkflow() == CorrectionWorkflow.anonymous) {
			anonymCorrectionWorkflowEl.select(onKeys[0], true);
		}
		
		String[] visibilityValues = new String[] {
				translate("results.visibility.correction.visible"), translate("results.visibility.correction.not.visible")
		};
		resultsVisibilityAfterCorrectionEl = uifactory.addRadiosVertical("results.visibility.correction", "results.visibility.correction", formLayout,
				visibilityKeys, visibilityValues);
		if(qti21Module.isResultsVisibleAfterCorrectionWorkflow()) {
			resultsVisibilityAfterCorrectionEl.select(visibilityKeys[0], true);
		} else {
			resultsVisibilityAfterCorrectionEl.select(visibilityKeys[1], true);
		}
		
		mathExtensionEl = uifactory.addCheckboxesHorizontal("math.extension", "math.extension", formLayout,
				onKeys, onValues);
		if(qti21Module.isMathAssessExtensionEnabled()) {
			mathExtensionEl.select(onKeys[0], true);
		}
		mathExtensionEl.setExampleKey("math.extension.text", null);
		mathExtensionEl.addActionListener(FormEvent.ONCHANGE);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private void updateUI() {
		certificateEl.setVisible(digitalSignatureEl.isSelected(0));
		certificatePasswordEl.setVisible(digitalSignatureEl.isSelected(0));
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(certificateEl.getUploadFile() != null) {
			File uploadedCertificate = certificateEl.getUploadFile();
			if(uploadedCertificate != null && uploadedCertificate.exists()) {
				allOk &= validateCertificatePassword(uploadedCertificate);
			}
		} else {
			String credential = certificatePasswordEl.getValue();
			if(!PLACEHOLDER.equals(credential) && certificateEl.getInitialFile() != null) {
				allOk &= validateCertificatePassword(certificateEl.getInitialFile());
			}
		}
		
		resultsVisibilityAfterCorrectionEl.clearError();
		if(!resultsVisibilityAfterCorrectionEl.isOneSelected()) {
			resultsVisibilityAfterCorrectionEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validateCertificatePassword(File file) {
		boolean allOk = true;
		
		try {
			String password = certificatePasswordEl.getValue();
			X509CertificatePrivateKeyPair kp = CryptoUtil.getX509CertificatePrivateKeyPairPfx(file, password);
			if(kp.getX509Cert() == null) {
				certificateEl.setErrorKey("error.digital.certificate.noX509", null);
				allOk &= false;
			} else if(kp.getPrivateKey() == null) {
				certificateEl.setErrorKey("error.digital.certificate.noPrivateKey", null);
				allOk &= false;
			}
		} catch (Exception e) {
			logError("", e);
			String message = e.getMessage() == null ? "" : e.getMessage();
			String [] errorArgs = new String[]{ message };
			if(message != null && message.contains("password")) {
				certificateEl.setErrorKey("error.digital.certificate.wrongpassword", errorArgs);
			} else {
				certificateEl.setErrorKey("error.digital.certificate.cannotread", errorArgs);
			}
			allOk &= false;
		}
		
		return allOk;
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem source, FormEvent fe) {
		if(source != this.validationButton) {
			super.propagateDirtinessToContainer(source, fe);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(validationCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(digitalSignatureEl == source) {
			updateUI();
		} else if(validationButton == source) {
			doValidate(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		CorrectionWorkflow correctionWf = anonymCorrectionWorkflowEl.isAtLeastSelected(1)
				? CorrectionWorkflow.anonymous : CorrectionWorkflow.named;
		qti21Module.setCorrectionWorkflow(correctionWf);
		qti21Module.setResultsVisibleAfterCorrectionWorkflow(resultsVisibilityAfterCorrectionEl.isSelected(0));
		qti21Module.setMathAssessExtensionEnabled(mathExtensionEl.isSelected(0));
		qti21Module.setDigitalSignatureEnabled(digitalSignatureEl.isSelected(0));
		if(digitalSignatureEl.isSelected(0)) {
			File uploadedCertificate = certificateEl.getUploadFile();
			if(uploadedCertificate != null && uploadedCertificate.exists()) {
				qti21Module.setDigitalSignatureCertificateFile(uploadedCertificate, certificateEl.getUploadFileName());
				File newFile = qti21Module.getDigitalSignatureCertificateFile();
				certificateEl.reset();// make sure the same certificate is not load twice
				certificateEl.setInitialFile(newFile);
			}
			String credential = certificatePasswordEl.getValue();
			if(!PLACEHOLDER.equals(credential)) {
				qti21Module.setDigitalSignatureCertificatePassword(credential);
			}
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(validationCtrl);
		removeAsListenerAndDispose(cmc);
		validationCtrl = null;
		cmc = null;
	}

	private void doValidate(UserRequest ureq) {
		if(guardModalController(validationCtrl)) return;
		
		validationCtrl = new ValidationXmlSignatureController(ureq, getWindowControl());
		listenTo(validationCtrl);
		cmc = new CloseableModalController(getWindowControl(), "close", validationCtrl.getInitialComponent(),
				true, translate("validate.xml.signature"));
		cmc.activate();
		listenTo(cmc);
	}
}