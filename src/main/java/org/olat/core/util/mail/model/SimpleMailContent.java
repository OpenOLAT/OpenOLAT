/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.util.mail.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.mail.MailContent;

/**
 * 
 * Initial date: 17 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SimpleMailContent implements MailContent {
	
	private String subject;
	private String body;
	private List<File> attachments;
	
	public SimpleMailContent(String subject, String body, File[] attachmentArr) {
		this.subject = subject;
		this.body = body;
		
		attachments = new ArrayList<>();
		if(attachmentArr != null && attachmentArr.length > 0) {
			for(File attachment:attachmentArr) {
				if(attachment != null && attachment.exists()) {
					attachments.add(attachment);
				}
			}
		}
	}
	
	public SimpleMailContent(String subject, String body, List<File> attachmentList) {
		this.subject = subject;
		this.body = body;
		if(attachmentList == null) {
			this.attachments = new ArrayList<>(1);
		} else {
			this.attachments = new ArrayList<>(attachmentList);
		}
	}

	@Override
	public String getSubject() {
		return subject;
	}
	
	@Override
	public void setSubject(String subject) {
		this.subject = subject;
	}

	@Override
	public String getBody() {
		return body;
	}
	
	@Override
	public void setBody(String body) {
		this.body = body;
	}

	@Override
	public List<File> getAttachments() {
		return attachments;
	}

	@Override
	public void setAttachments(List<File> attachments) {
		this.attachments = attachments;
	}
}
