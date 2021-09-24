package org.olat.core.gui.qrscanner;

import org.olat.core.gui.control.Event;

public class QrCodeDetectedEvent extends Event {

	public static final String QR_CODE_DETECTED_COMMAND = "qr_code_detected";

	private static final long serialVersionUID = -1389903058142862500L;

	private final String qrCode;
	
	public QrCodeDetectedEvent(String qrCode) {
		super(QR_CODE_DETECTED_COMMAND);

		this.qrCode = qrCode;
	}

	public String getQrCode() {
		return this.qrCode;
	}
}
