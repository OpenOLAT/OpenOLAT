/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

/**
 * 
 * Initial date: 11 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MailLogInfos {
	
	private final RejectionEmailLog mailLog;
	
	public MailLogInfos(RejectionEmailLog emailLog) {
		this.mailLog = emailLog;
	}
	
	public ApplicationLight getApplication() {
		return mailLog.getApplication();
	}
	
	public Long getApplicationKey() {
		return mailLog.getApplication().getKey();
	}
	
	public RejectionEmailLog getMailLog() {
		return mailLog;
	}
}
