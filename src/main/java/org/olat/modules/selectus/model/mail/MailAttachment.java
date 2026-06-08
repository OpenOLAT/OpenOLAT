/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
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
