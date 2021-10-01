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
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.modules.immunityproof.ImmunityProof;
import org.olat.modules.immunityproof.ImmunityProofContext;
import org.olat.modules.immunityproof.ImmunityProofModule;
import org.olat.modules.immunityproof.manager.ImmunityProofCertificateChecker;
import org.springframework.beans.factory.annotation.Autowired;

public class ImmunityProofTestScriptController extends FormBasicController {

	private static final Logger log = Tracing.createLoggerFor(ImmunityProofTestScriptController.class);

	private FormLink testScriptLink;
	private FormLink testInvalidCertificateLink;
	private FormLink testValidCertificateLink;
	private FormLink testNoCertificateLink;

	private FormLayoutContainer outputEl;

	@Autowired
	private ImmunityProofModule immunityProofModule;

	public ImmunityProofTestScriptController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);

		setTranslator(Util.createPackageTranslator(ImmunityProof.class, getLocale(), getTranslator()));

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("test.script.description");

		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		buttonLayout.setRootForm(mainForm);
		formLayout.add(buttonLayout);

		testScriptLink = uifactory.addFormLink("test.script.help", buttonLayout, Link.BUTTON);
		// testInvalidCertificateLink = uifactory.addFormLink("test.script.invalid",
		// buttonLayout, Link.BUTTON);
		testValidCertificateLink = uifactory.addFormLink("test.script.valid", buttonLayout, Link.BUTTON);
		testNoCertificateLink = uifactory.addFormLink("test.script.no.certificate", buttonLayout, Link.BUTTON);

		String outputPage = Util.getPackageVelocityRoot(getClass()) + "/immunity_proof_script_output.html";
		outputEl = FormLayoutContainer.createCustomFormLayout("output", getTranslator(), outputPage);
		outputEl.setRootForm(mainForm);
		formLayout.add(outputEl);

		outputEl.contextPut("output", "");
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == testScriptLink) {
			showHelp();
		} else if (source == testInvalidCertificateLink) {

		} else if (source == testValidCertificateLink) {
			verifyValidCert();
		} else if (source == testNoCertificateLink) {
			verifyNoCert();
		}
	}

	private void showHelp() {
		List<String> cmds = new ArrayList<String>();
		cmds.add(immunityProofModule.getPythonDir());
		cmds.add(immunityProofModule.getValidationScriptDir() + "/verify_ehc.py");
		cmds.add("--help");

		CountDownLatch doneSignal = new CountDownLatch(1);
		ImmunityProofContext context = new ImmunityProofContext();

		ImmunityProofCertificateChecker certificateChecker = new ImmunityProofCertificateChecker(immunityProofModule,
				context, cmds, doneSignal);
		certificateChecker.start();

		try {
			if (doneSignal.await(5000, TimeUnit.MILLISECONDS)) {
				log.info("Successfully run verify-ehc script - Help was shown");
				getWindowControl().setInfo("Successfully run verify-ehc script!");

				outputEl.contextPut("output", context.getOutput().toString());
			} else {
				log.error("Could not run verify-ehc script - Timeout after 5s");

				getWindowControl().setError("Timeout after 5s - Could not run verify-ehc script");
			}
		} catch (InterruptedException e) {
			log.error("", e);
		}

		certificateChecker.destroyProcess();
	}

	private void verifyValidCert() {
		List<String> cmds = new ArrayList<String>();
		cmds.add(immunityProofModule.getPythonDir());
		cmds.add(immunityProofModule.getValidationScriptDir() + "/verify_ehc.py");
		cmds.add("--image");
		cmds.add(immunityProofModule.getValidationScriptDir() + "/examples/valid_cert.png");

		CountDownLatch doneSignal = new CountDownLatch(1);
		ImmunityProofContext context = new ImmunityProofContext();

		ImmunityProofCertificateChecker certificateChecker = new ImmunityProofCertificateChecker(immunityProofModule,
				context, cmds,
				doneSignal);
		certificateChecker.start();

		try {
			if (doneSignal.await(5000, TimeUnit.MILLISECONDS)) {
				context = certificateChecker.getContext();

				if (context.isCertificateFound()) {
					log.info("Successfully run verify-ehc script - Certificate was found");
					getWindowControl().setInfo("Successfully run verify-ehc script - Certificate was found");
					outputEl.contextPut("output", context.getOutput().toString());
				} else {
					log.error("Successfully run verify-ehc script - Certificate was found");
					getWindowControl().setError("Successfully run verify-ehc script - No certificate was found");
					outputEl.contextPut("output", context.getOutput().toString());
				}

			} else {
				log.error("Could not run verify-ehc script - Timeout after 5s");

				getWindowControl().setError("Timeout after 5s - Could not run verify-ehc script");
			}
		} catch (InterruptedException e) {
			log.error("", e);
		}

		certificateChecker.destroyProcess();
	}

	private void verifyNoCert() {
		List<String> cmds = new ArrayList<String>();
		cmds.add(immunityProofModule.getPythonDir());
		cmds.add(immunityProofModule.getValidationScriptDir() + "/verify_ehc.py");
		cmds.add("--image");
		cmds.add(immunityProofModule.getValidationScriptDir() + "/examples/no_cert.png");

		CountDownLatch doneSignal = new CountDownLatch(1);
		ImmunityProofContext context = new ImmunityProofContext();

		ImmunityProofCertificateChecker certificateChecker = new ImmunityProofCertificateChecker(immunityProofModule,
				context, cmds,
				doneSignal);
		certificateChecker.start();

		try {
			if (doneSignal.await(5000, TimeUnit.MILLISECONDS)) {
				context = certificateChecker.getContext();

				if (!context.isCertificateFound()) {
					log.info("Successfully run verify-ehc script - No certificate was found");
					getWindowControl().setInfo("Successfully run verify-ehc script - No ertificate was found");
					outputEl.contextPut("output", context.getErrors().toString());
				} else {
					log.error("Successfully run verify-ehc script - But certificate was found and it shouldn't be!");
					getWindowControl().setError(
							"Successfully run verify-ehc script - But certificate was found and it shouldn't be!");
					outputEl.contextPut("output", context.getOutput().toString());
				}

			} else {
				log.error("Could not run verify-ehc script - Timeout after 5s");

				getWindowControl().setError("Timeout after 5s - Could not run verify-ehc script");
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
	protected void doDispose() {

	}

}
