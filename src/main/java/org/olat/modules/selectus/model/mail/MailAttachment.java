/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.mail;

import org.olat.core.CoreSpringFactory;

import org.olat.modules.selectus.manager.ApplicationDAO;
import org.olat.modules.selectus.model.Attachment;

/**
 * 
 * Initial date: 26 avr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MailAttachment {
	
	private String contentToPdf;
	private String filename;
	private byte[] content;
	private String mimeType;
	
	public static MailAttachment toPdf(String content, String filename) {
		MailAttachment attachment = new MailAttachment();
		attachment.setContentToPdf(content);
		attachment.setFilename(filename);
		attachment.setMimeType("application/pdf");
		return attachment;
	}
	
	public static MailAttachment valueOf(Attachment attachment) {
		MailAttachment mailAttachment = new MailAttachment();
		byte[] datas = CoreSpringFactory.getImpl(ApplicationDAO.class).getAttachmentDatas(attachment);
		mailAttachment.setContent(datas);
		mailAttachment.setFilename(attachment.getName());
		mailAttachment.setMimeType(attachment.getType());
		return mailAttachment;	
	}
	
	public String getContentToPdf() {
		return contentToPdf;
	}
	
	public void setContentToPdf(String contentToPdf) {
		this.contentToPdf = contentToPdf;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public byte[] getContent() {
		return content;
	}
	
	public void setContent(byte[] content) {
		this.content = content;
	}
	
	public String getMimeType() {
		return mimeType;
	}
	
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
}
