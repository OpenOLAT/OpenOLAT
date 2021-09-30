
package org.olat.core.gui.qrscanner;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.util.StringHelper;

public class GenericQrScanController extends BasicController {

	private final VelocityContainer mainVC;

	private boolean isScanning = true;

	public GenericQrScanController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("generic_qr_scanner");
		
		String[] js = new String[]{"js/qrscanner/qr-scanner.umd.min.js", "js/qrscanner/qr-scanner-worker.min.js"};
		JSAndCSSComponent qrScannerJS = new JSAndCSSComponent("glossHelpJs", js, null);
		
		mainVC.put("qrScannerJS", qrScannerJS);

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (event.getCommand().equals("QrCodeFoundEvent")) {
			String qrCode = ureq.getHttpReq().getParameter("qrCode");

			if (StringHelper.containsNonWhitespace(qrCode)) {
				fireEvent(ureq, new QrCodeDetectedEvent(qrCode));
			}
		}
	}

	@Override
	protected void doDispose() {
		stopScanner();
	}

	public void startScanner() {
		// TODO Discuss the timeout. Reason: If not there, errors are shown sometimes,
		// because the code is not in DOM yet
		if (!isScanning) {
			JSCommand startScanner = new JSCommand(
					"jQuery(initCameraAndScanner);");
			getWindowControl().getWindowBackOffice().sendCommandTo(startScanner);

			isScanning = true;
		}
	}

	public void stopScanner() {
		// TODO Problem: If the camera is opened once, closed and opened again, it is
		// not closed again properly
		if (isScanning) {
			JSCommand stopScanner = new JSCommand(
					"jQuery(cleanUpScanner);");
			getWindowControl().getWindowBackOffice().sendCommandTo(stopScanner);

			isScanning = false;
		}
	}

}
