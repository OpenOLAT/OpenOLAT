package org.olat.core.commons.services.sms;

/**
 * 
 * Initial date: 7 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SimpleMessageException extends Exception {

	private static final long serialVersionUID = 787789477989775426L;
	
	private final ErrorCode code;

	public SimpleMessageException(Exception cause, ErrorCode code) {
		super(cause);
		this.code = code;
	}
	
	public ErrorCode getErrorCode() {
		return code;
	}
	
	public enum ErrorCode {
		numberNotValid,
		
	}
}
