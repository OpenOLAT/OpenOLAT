/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.model.mail.MailAttachment;

/**
 * 
 * Initial date: 12.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SubjectAndBody {
	
	private final String subject;
	private final String body;
	private final MailAttachment attachment;
	
	public SubjectAndBody(String subject, String body) {
		this.subject = subject;
		this.body = body;
		this.attachment = null;
	}
	
	public SubjectAndBody(String subject, String body, MailAttachment attachment) {
		this.subject = subject;
		this.body = body;
		this.attachment = attachment;
	}

	public String getSubject() {
		return subject;
	}

	public String getBody() {
		return body;
	}
	
	public MailAttachment getLetter() {
		return attachment;
	}
	
	public boolean isHtml() {
		return StringHelper.isHtml(body);
	}
}
