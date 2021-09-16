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
package org.olat.modules.immunityproof.ui;

import org.olat.core.commons.editor.htmleditor.HTMLEditorControllerWithoutFile;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.immunityproof.ImmunityProof;
import org.olat.modules.immunityproof.ImmunityProofModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 08.09.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ImmunityProofEntranceCodesController extends FormBasicController {
	
    private FormLink qrCodeInstructionsReset;
    private FormLink qrCodeInstructionsCustomize;
    
    private FormLayoutContainer pdfPreviewContainer;
    private FormLink qrCodeGeneratePdfButton;
    
    private HTMLEditorControllerWithoutFile qrCodeInstructionsEditController;
    private ImmunityProofConfirmResetController confirmResetQRCodeInstructionsController;
    private ImmunityProofQrPdfPreviewController pdfController;
    private CloseableModalController cmc;
    
    @Autowired
    private ImmunityProofModule immunityProofModule;
    @Autowired
    private PdfService pdfService;
    
	public ImmunityProofEntranceCodesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		setTranslator(Util.createPackageTranslator(ImmunityProof.class, getLocale(), getTranslator()));
		
		initForm(ureq);
		loadConfiguration(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("entrance.qr.code");
		
		// Buttons
		FormLayoutContainer qrCodeButtons = FormLayoutContainer.createButtonLayout("qrCodeButtons", getTranslator());
		qrCodeButtons.setLabel("qr.description", null);
        formLayout.add(qrCodeButtons);
        qrCodeInstructionsReset = uifactory.addFormLink("reset", qrCodeButtons, Link.BUTTON);
        qrCodeInstructionsCustomize = uifactory.addFormLink("customize", qrCodeButtons, Link.BUTTON);
        
        // Preview
        pdfPreviewContainer = FormLayoutContainer.createCustomFormLayout("pdfPreview", getTranslator(), velocity_root + "/immunity_proof_qr_preview.html");
        pdfPreviewContainer.setLabel("entrance.preview", null);
        formLayout.add(pdfPreviewContainer);
        
        // Print preview button
        FormLayoutContainer previewButtons = FormLayoutContainer.createButtonLayout("previewButtons", getTranslator());
        formLayout.add(previewButtons);
        qrCodeGeneratePdfButton = uifactory.addFormLink("generate.pdf", previewButtons, Link.BUTTON);
	}
	
	private void loadConfiguration(UserRequest ureq) {
		pdfController = new ImmunityProofQrPdfPreviewController(ureq, getWindowControl());
        pdfPreviewContainer.put("pdfPreview", pdfController.getInitialComponent());
		
		flc.setDirty(true);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == qrCodeInstructionsCustomize) {
			doOpenCustomText(ureq);
		} else if (source == qrCodeInstructionsReset) {
			doAskForReset(ureq);
		} else if (source == qrCodeGeneratePdfButton) {
			doGeneratePdf(ureq);
		}
	}
	
	private void doOpenCustomText(UserRequest ureq) {
		String qrCodeInstructions = StringHelper.xssScan(immunityProofModule.getQrEntranceText());
        qrCodeInstructionsEditController = WysiwygFactory.createWysiwygControllerWithoutFile(ureq, getWindowControl(), null, qrCodeInstructions, null);
        cmc = new CloseableModalController(getWindowControl(), translate("close"), qrCodeInstructionsEditController.getInitialComponent(), true, translate("qr.description.edit"), true, true);
        listenTo(qrCodeInstructionsEditController);
        listenTo(cmc);
        cmc.activate();
	}
	
	private void doAskForReset(UserRequest ureq) {
		confirmResetQRCodeInstructionsController = new ImmunityProofConfirmResetController(ureq, getWindowControl(), "qr.text.reset.warning");
        cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmResetQRCodeInstructionsController.getInitialComponent(), true, translate("qr.description.reset"), true, true);
        listenTo(confirmResetQRCodeInstructionsController);
        listenTo(cmc);
        cmc.activate();
	}
	
	private void doGeneratePdf(UserRequest ureq) {
		MediaResource pdf = pdfService.convert("3G_QR_Code.pdf", getIdentity(), (lureq, lwcontrol) -> { return new ImmunityProofQrPdfPreviewController(lureq, lwcontrol); }, getWindowControl());
        ureq.getDispatchResult().setResultingMediaResource(pdf);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == qrCodeInstructionsEditController) {
            if (event == FormEvent.DONE_EVENT) {
                immunityProofModule.setQrEntranceText(qrCodeInstructionsEditController.getHTMLContent());
                loadConfiguration(ureq);
            }

            cleanUp();
        } else if (source == confirmResetQRCodeInstructionsController) {
            if (event == FormEvent.DONE_EVENT) {
                immunityProofModule.setQrEntranceText(null);
                loadConfiguration(ureq);
            }

            cleanUp();
        } else if (source == cmc) {
            cleanUp();
        }
	}
	
	private void cleanUp() {
		 if (cmc != null && cmc.isCloseable()) {
	            cmc.deactivate();
	        }

	        removeAsListenerAndDispose(cmc);
	        removeAsListenerAndDispose(qrCodeInstructionsEditController);
	        removeAsListenerAndDispose(confirmResetQRCodeInstructionsController);

	        cmc = null;
	        qrCodeInstructionsEditController = null;
	        confirmResetQRCodeInstructionsController = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// Nothing to do here
	}

	@Override
	protected void doDispose() {
		// Nothing to do here
	}

}
