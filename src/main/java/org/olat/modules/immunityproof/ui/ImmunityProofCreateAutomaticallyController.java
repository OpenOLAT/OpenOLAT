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
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.qrscanner.GenericQrScanController;
import org.olat.core.gui.qrscanner.QrCodeDetectedEvent;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.modules.immunityproof.ImmunityProof;
import org.olat.modules.immunityproof.ImmunityProofContext;
import org.olat.modules.immunityproof.ImmunityProofModule;
import org.olat.modules.immunityproof.ImmunityProofService;
import org.olat.modules.immunityproof.manager.ImmunityProofCertificateChecker;
import org.olat.modules.immunityproof.ui.event.ImmunityProofFoundEvent;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

public class ImmunityProofCreateAutomaticallyController extends FormBasicController {

	private static final Logger log = Tracing.createLoggerFor(ImmunityProofCreateAutomaticallyController.class);
	private static final String[] CONFIRMATION_KEYS = new String[] { "on" };

	private FormLayoutContainer buttonLayout;
	private FormLink scanLink;
	private FormLink uploadLink;

	private FormLayoutContainer confirmationLayout;
	private StaticTextElement firstNameEl;
	private StaticTextElement lastNameEl;
	private StaticTextElement birthDateEl;
	private StaticTextElement validUntilEl;
	private MultipleSelectionElement confirmOverrideEl;
	private MultipleSelectionElement confirmReminderEl;
	private MultipleSelectionElement confirmTruthEl;

	private FormSubmit submitLink;

	private CloseableModalController cmc;
	private GenericQrScanController scanController;
	private ImmunityProofUploadCertificateController uploadCertificateController;

	private ImmunityProofContext context;

	private boolean usedByCovidCommissioner;

	@Autowired
	private ImmunityProofModule immunityProofModule;
	@Autowired
	private ImmunityProofService immunityProofService;
	@Autowired
	private UserManager userManager;

	public ImmunityProofCreateAutomaticallyController(UserRequest ureq, WindowControl wControl,
			Identity editedIdentity, boolean usedByCovidCommissioner) {
		super(ureq, wControl, LAYOUT_VERTICAL);

		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		setTranslator(Util.createPackageTranslator(ImmunityProof.class, getLocale(), getTranslator()));

		context = new ImmunityProofContext();
		context.setIdentity(editedIdentity);

		this.usedByCovidCommissioner = usedByCovidCommissioner;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer buttonWrapperLayout = FormLayoutContainer.createDefaultFormLayout("buttons.wrapper",
				getTranslator());
		buttonWrapperLayout.setRootForm(mainForm);
		formLayout.add(buttonWrapperLayout);
		
		buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		buttonLayout.setRootForm(mainForm);
		buttonLayout.setLabel("automatic.scan.import", null);
		buttonWrapperLayout.add(buttonLayout);

		scanLink = uifactory.addFormLink("automatic.scan", buttonLayout, Link.BUTTON);
		scanLink.setIconLeftCSS("o_icon o_icon_lg o_icon_qrcode");
		uploadLink = uifactory.addFormLink("automatic.import", buttonLayout, Link.BUTTON);
		uploadLink.setIconLeftCSS("o_icon o_icon_lg o_icon_upload");

		confirmationLayout = FormLayoutContainer.createDefaultFormLayout("confirmation.layout", getTranslator());
		confirmationLayout.setRootForm(mainForm);
		confirmationLayout.setVisible(false);
		confirmationLayout.setFormTitle(translate("confirmation.title"));
		formLayout.add(confirmationLayout);

		firstNameEl = uifactory.addStaticTextElement("form.name.firstName", "", confirmationLayout);
		lastNameEl = uifactory.addStaticTextElement("form.name.lastName", "", confirmationLayout);
		birthDateEl = uifactory.addStaticTextElement("form.name.birthDay", "", confirmationLayout);
		validUntilEl = uifactory.addStaticTextElement("valid.until", "", confirmationLayout);

		SelectionValues truthOptions = new SelectionValues(
				new SelectionValue("confirm.truth", translate("confirm.truth")));
		confirmTruthEl = uifactory.addCheckboxesVertical("confirm.truth.label", confirmationLayout, truthOptions.keys(),
				truthOptions.values(), 1);

		SelectionValues reminderOptions = new SelectionValues(
				new SelectionValue("confirm.reminder", translate("confirm.reminder")));
		confirmReminderEl = uifactory.addCheckboxesVertical("confirm.reminder.label", confirmationLayout,
				reminderOptions.keys(), reminderOptions.values(), 1);
		confirmReminderEl.select("confirm.reminder", true);

		confirmOverrideEl = uifactory.addCheckboxesHorizontal("confirm.override.label", confirmationLayout,
				CONFIRMATION_KEYS,
				new String[] { translate("confirm.date.value") });

		FormLayoutContainer formWrapperLayout = FormLayoutContainer.createDefaultFormLayout("form.buttons.wrapper",
				getTranslator());
		formWrapperLayout.setRootForm(mainForm);
		formLayout.add(formWrapperLayout);

		FormLayoutContainer formButtonLayout = FormLayoutContainer.createButtonLayout("form.buttons", getTranslator());
		formButtonLayout.setRootForm(mainForm);
		formWrapperLayout.add(formButtonLayout);

		submitLink = uifactory.addFormSubmitButton("submit", formButtonLayout);
		submitLink.setVisible(false);
		uifactory.addFormCancelButton("cancel", formButtonLayout, ureq, getWindowControl());
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
				context = ((ImmunityProofFoundEvent) event).getContext();
				doShowConfirmation(context);
			}

			cleanUp();
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

				if (!context.isCertificateFound()) {
					getWindowControl().setWarning("Please provide a valid COVID certificate.");
				} else if (!context.isCertificateValid()) {
					getWindowControl().setWarning("Please provide a valid COVID certificate.");
				} else {

					doShowConfirmation(context);
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

	private void doShowConfirmation(ImmunityProofContext context) {
		if (!context.isCertificateFound() || !context.isCertificateValid()) {
			return;
		}
		
		confirmationLayout.setVisible(true);
		
		firstNameEl.setValue(context.getFirstName());
		lastNameEl.setValue(context.getLastName());

		if (context.getBirthDate() != null) {
			String birthDate = Formatter.getInstance(getLocale()).formatDate(context.getBirthDate());
			birthDateEl.setValue(birthDate);
		}
		if (context.getSafeUntil() != null) {
			String safeUntil = Formatter.getInstance(getLocale()).formatDate(context.getSafeUntil());
			validUntilEl.setValue(safeUntil);
		}
		
		confirmOverrideEl.setVisible(immunityProofService.getImmunityProof(context.getIdentity()) != null);
		confirmReminderEl.setVisible(true);
		confirmReminderEl.select("confirm.reminder", true);
		confirmTruthEl.setVisible(true);
		submitLink.setVisible(true);

		if (!context.isCertificateBelongsToUser()) {
			showWarning("warning.certificate.belongs.not.to.user");

			if (!usedByCovidCommissioner) {
				confirmOverrideEl.setVisible(false);
				confirmTruthEl.setVisible(false);
				confirmReminderEl.setVisible(false);
				submitLink.setVisible(false);
			}
		}

	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk =  super.validateFormLogic(ureq);
		
		allOk &= context.isCertificateFound() && context.isCertificateValid()
				&& (context.isCertificateBelongsToUser() || usedByCovidCommissioner);
		
		confirmOverrideEl.clearError();
		if (confirmOverrideEl.isVisible() && !confirmOverrideEl.isSelected(0)) {
			allOk &= false;
			confirmOverrideEl.setErrorKey("form.legende.mandatory", null);
		}
		
		confirmTruthEl.clearError();
		if (confirmTruthEl.isVisible() && !confirmTruthEl.isSelected(0)) {
			allOk &= false;
			confirmTruthEl.setErrorKey("form.legende.mandatory", null);
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		immunityProofService.createImmunityProofFromCertificate(context.getIdentity(), context.getSafeUntil(),
				confirmReminderEl.isAtLeastSelected(1), true);

		fireEvent(ureq, Event.DONE_EVENT);
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
