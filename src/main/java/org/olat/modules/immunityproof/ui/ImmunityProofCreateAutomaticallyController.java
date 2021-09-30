package org.olat.modules.immunityproof.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.qrscanner.GenericQrScanController;
import org.olat.core.gui.qrscanner.QrCodeDetectedEvent;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.modules.immunityproof.ImmunityProof;
import org.olat.modules.immunityproof.ImmunityProofContext;
import org.olat.modules.immunityproof.ImmunityProofModule;
import org.olat.modules.immunityproof.ImmunityProofService;
import org.olat.modules.immunityproof.manager.ImmunityProofCertificateChecker;
import org.olat.modules.immunityproof.ui.event.ImmunityProofAddedEvent;
import org.olat.modules.immunityproof.ui.event.ImmunityProofFoundEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class ImmunityProofCreateAutomaticallyController extends FormBasicController {

	private static final Logger log = Tracing.createLoggerFor(ImmunityProofCreateAutomaticallyController.class);

	private FormLayoutContainer buttonLayout;

	private FormLink scanLink;
	private FormLink uploadLink;
	private FormSubmit submitLink;

	private CloseableModalController cmc;
	private GenericQrScanController scanController;
	private ImmunityProofUploadCertificateController uploadCertificateController;

	private ImmunityProofContext context;

	@Autowired
	private ImmunityProofModule immunityProofModule;
	@Autowired
	private ImmunityProofService immunityProofService;

	public ImmunityProofCreateAutomaticallyController(UserRequest ureq, WindowControl wControl,
			Identity editedIdentity) {
		super(ureq, wControl);

		setTranslator(Util.createPackageTranslator(ImmunityProof.class, getLocale(), getTranslator()));

		context = new ImmunityProofContext();
		context.setIdentity(editedIdentity);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		buttonLayout.setRootForm(mainForm);
		buttonLayout.setLabel("automatic.scan.import", null);
		formLayout.add(buttonLayout);

		scanLink = uifactory.addFormLink("automatic.scan", buttonLayout, Link.BUTTON);
		scanLink.setIconLeftCSS("o_icon o_icon_lg o_icon_qrcode");
		uploadLink = uifactory.addFormLink("automatic.import", buttonLayout, Link.BUTTON);
		uploadLink.setIconLeftCSS("o_icon o_icon_lg o_icon_upload");

		FormLayoutContainer formButtonLayout = FormLayoutContainer.createButtonLayout("form.buttons", getTranslator());
		formButtonLayout.setRootForm(mainForm);
		formLayout.add(formButtonLayout);

		submitLink = uifactory.addFormSubmitButton("submit", formButtonLayout);
		submitLink.setVisible(false);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == uploadLink) {
			doShowUpload(ureq);
		} else if (source == scanLink) {
			doShowScan(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == scanController) {
			if (event instanceof QrCodeDetectedEvent) {
				QrCodeDetectedEvent qrCodeEvent = (QrCodeDetectedEvent) event;
				context.setQrCode(qrCodeEvent.getQrCode());
				
				doCheckCertificate(ureq, context);
			}

			cleanUp();
		} else if (source == uploadCertificateController) {
			if (event instanceof ImmunityProofFoundEvent) {

				// TODO FOR TESTING
				immunityProofService.createImmunityProofFromCertificate(context.getIdentity(), context.getSafeUntil(),
						true, true);

				fireEvent(ureq, new ImmunityProofAddedEvent(context.getIdentity()));
			}
		} else if (source == cmc) {
			cmc.deactivate();

			cleanUp();
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(uploadCertificateController);
		removeAsListenerAndDispose(scanController);
		removeAsListenerAndDispose(cmc);

		uploadCertificateController = null;
		scanController = null;
		cmc = null;
	}

	private void doShowUpload(UserRequest ureq) {
		uploadCertificateController = new ImmunityProofUploadCertificateController(ureq, getWindowControl(), context);
		listenTo(uploadCertificateController);

		cmc = new CloseableModalController(getWindowControl(), translate("cancel"),
				uploadCertificateController.getInitialComponent(), true, translate("add.immunity.proof"));

		listenTo(cmc);
		cmc.activate();
	}

	private void doShowScan(UserRequest ureq) {
		scanController = new GenericQrScanController(ureq, getWindowControl());
		listenTo(scanController);

		cmc = new CloseableModalController(getWindowControl(), translate("cancel"),
				scanController.getInitialComponent(), true, translate("add.immunity.proof"));

		listenTo(cmc);
		cmc.activate();
	}

	private void doCheckCertificate(UserRequest ureq, ImmunityProofContext context) {
		List<String> cmds = new ArrayList<String>();
		cmds.add(immunityProofModule.getPythonDir());
		cmds.add(immunityProofModule.getValidationScriptDir() + "/verify_ehc.py");
		cmds.add(context.getQrCode());
		CountDownLatch doneSignal = new CountDownLatch(1);

		ImmunityProofCertificateChecker certificateChecker = new ImmunityProofCertificateChecker(immunityProofModule,
				context, cmds,
				doneSignal);
		certificateChecker.start();

		try {
			if (doneSignal.await(5000, TimeUnit.MILLISECONDS)) {
				// Reload context for safety
				context = certificateChecker.getContext();

				if (context.isCertificateFound() && context.isCertificateValid()
						&& context.isCertificateBelongsToUser()) {
					// TODO Ask for mail and removal
					immunityProofService.createImmunityProofFromCertificate(context.getIdentity(),
							context.getSafeUntil(), true, true);

					fireEvent(ureq, Event.DONE_EVENT);

				} else {
					getWindowControl().setWarning("Please provide a valid COVID certificate.");
				}
			} else {
				log.warn("Cannot validate certificate in 5s: {}", context);
				
				getWindowControl()
						.setError("Timeout - Could not validate Certificate" + "<br><br>" + "Please try again!");
			}
		} catch (InterruptedException e) {
			log.error("", e);
		}

		certificateChecker.destroyProcess();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		if (scanController != null) {
			scanController.stopScanner();
		}

		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void doDispose() {
		
	}

}
