package org.olat.modules.immunityproof.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.immunityproof.ImmunityProof;
import org.olat.modules.immunityproof.ImmunityProofContext;
import org.olat.modules.immunityproof.ImmunityProofModule;
import org.olat.modules.immunityproof.manager.ImmunityProofCertificateChecker;
import org.olat.modules.immunityproof.ui.event.ImmunityProofFoundEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class ImmunityProofUploadCertificateController extends FormBasicController {

	private static final Logger log = Tracing.createLoggerFor(ImmunityProofUploadCertificateController.class);

	private FileElement uploadElement;
	private ImmunityProofContext context;
	private VFSContainer tempUploadFolder;

	@Autowired
	private ImmunityProofModule immunityProofModule;

	public ImmunityProofUploadCertificateController(UserRequest ureq, WindowControl wControl,
			ImmunityProofContext context) {
		super(ureq, wControl, LAYOUT_VERTICAL);

		setTranslator(Util.createPackageTranslator(ImmunityProof.class, getLocale(), getTranslator()));
		this.context = context;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Set<String> imageMimeTypes = new HashSet<>();
		imageMimeTypes.add("image/gif");
		imageMimeTypes.add("image/jpg");
		imageMimeTypes.add("image/jpeg");
		imageMimeTypes.add("image/png");
		imageMimeTypes.add("application/pdf");

		uploadElement = uifactory.addFileElement(getWindowControl(), getIdentity(), "automatic.import", formLayout);
		uploadElement.addActionListener(FormEvent.ONCHANGE);
		uploadElement.setMaxUploadSizeKB(10000, "attachment.max.size", new String[] { "10000" });
		uploadElement.limitToMimeType(imageMimeTypes, "wrong.mime.type", null);

	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == uploadElement) {
			if (uploadElement.getUploadFile() != null) {
				if (validateFormLogic(ureq)) {
					// To check mime types
					List<ValidationStatus> fileStatus = new ArrayList<>();
					uploadElement.validate(fileStatus);

					if (!uploadElement.hasError()) {
						// Create temp folder
						if (tempUploadFolder == null) {
							tempUploadFolder = VFSManager.olatRootContainer(
									File.separator + "tmp/" + CodeHelper.getGlobalForeverUniqueID() + "/", null);
						}
						// Move file
						uploadElement.moveUploadFileTo(tempUploadFolder);

						// Cast to java file
						LocalFileImpl uploadedFile = (LocalFileImpl) tempUploadFolder
								.resolve(uploadElement.getUploadFileName());

						// Calculate path to uploaded file
						String path = uploadedFile.getBasefile().getAbsolutePath();

						// Check the certificate
						doCheckCertificate(ureq, context, path);
					}
				}
			}
		}
	}

	private void doCheckCertificate(UserRequest ureq, ImmunityProofContext context, String path) {
		List<String> cmds = new ArrayList<String>();
		cmds.add(immunityProofModule.getPythonDir());
		cmds.add(immunityProofModule.getValidationScriptDir() + "/verify_ehc.py");
		cmds.add("--image");
		cmds.add(path);
		CountDownLatch doneSignal = new CountDownLatch(1);

		ImmunityProofCertificateChecker certificateChecker = new ImmunityProofCertificateChecker(immunityProofModule,
				context, cmds, doneSignal);
		certificateChecker.start();

		try {
			if (doneSignal.await(5000, TimeUnit.MILLISECONDS)) {
				// Reload context for safety
				context = certificateChecker.getContext();

				if (context.isCertificateFound()) {
					fireEvent(ureq, new ImmunityProofFoundEvent(context));
				} else {
					getWindowControl().setWarning(translate("warning.invalid.certificate"));
				}
			} else {
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
	protected void doDispose() {
		if (tempUploadFolder != null) {
			tempUploadFolder.delete();
		}
	}

}
