/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.mail;

/**
 * 
 * Initial date: 6 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SentEmailTemplates {
	
	private final Long applicationKey;
	private final String[] templates;
	
	public SentEmailTemplates(Long applicationKey, String[] templates) {
		this.applicationKey = applicationKey;
		this.templates = templates;
	}
	
	public Long getApplicationKey() {
		return applicationKey;
	}
	
	public String[] getTemplates() {
		return templates;
	}
}
