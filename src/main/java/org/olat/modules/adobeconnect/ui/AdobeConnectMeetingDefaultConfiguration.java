package org.olat.modules.adobeconnect.ui;

/**
 * 
 * Initial date: 23 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectMeetingDefaultConfiguration {
	
	private final boolean allowGuestAccess;
	
	public AdobeConnectMeetingDefaultConfiguration(boolean allowGuestAccess) {
		this.allowGuestAccess = allowGuestAccess;
	}
	
	public boolean isAllowGuestAccess() {
		return allowGuestAccess;
	}

}
