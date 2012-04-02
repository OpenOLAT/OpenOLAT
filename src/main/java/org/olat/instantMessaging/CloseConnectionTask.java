package org.olat.instantMessaging;

import org.jivesoftware.smack.XMPPConnection;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CloseConnectionTask implements Runnable {
	
	private OLog log = Tracing.createLoggerFor(CloseConnectionTask.class);
	
	private final XMPPConnection connectionToClose;
	
	protected CloseConnectionTask(XMPPConnection connectionToClose) {
		this.connectionToClose = connectionToClose;
	}
	
	public void run() {
		try {
			if (connectionToClose != null && connectionToClose.isConnected()){
				connectionToClose.disconnect();
			}
		} catch (RuntimeException e) {
			log.warn("Error while trying to close instant messaging connection", e);
		}
	}

}
