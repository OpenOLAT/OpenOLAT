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
package org.olat.core.util.mail;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.util.mail.model.SimpleMailContent;

/**
 * 
 * Initial date: 11.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MailBundle {
	
	private MailContext context;
	private Identity fromId;
	private String from;
	private Identity toId;
	private String to;
	private Identity cc;
	private List<ContactList> contactLists; 
	private String metaId;
	private MailContent content;
	
	public MailBundle() {
		//
	}
	
	public MailBundle(MailContext context) {
		this.context = context;
	}
	
	public MailContext getContext() {
		return context;
	}
	
	public void setContext(MailContext context) {
		this.context = context;
	}
	
	public Identity getFromId() {
		return fromId;
	}
	
	public void setFromId(Identity fromId) {
		this.fromId = fromId;
	}
	
	public String getFrom() {
		return from;
	}
	
	public void setFrom(String from) {
		this.from = from;
	}
	
	public Identity getToId() {
		return toId;
	}
	
	public void setToId(Identity toId) {
		this.toId = toId;
	}
	
	public String getTo() {
		return to;
	}
	
	public void setTo(String to) {
		this.to = to;
	}
	
	public Identity getCc() {
		return cc;
	}
	
	public void setCc(Identity cc) {
		this.cc = cc;
	}
	
	public List<ContactList> getContactLists() {
		return contactLists;
	}
	
	public void setContactList(ContactList list) {
		this.contactLists = Collections.singletonList(list);
	}
	
	public void setContactLists(List<ContactList> list) {
		this.contactLists = list;
	}
	
	public String getMetaId() {
		return metaId;
	}
	
	public void setMetaId(String metaId) {
		this.metaId = metaId;
	}
	
	public MailContent getContent() {
		return content;
	}
	
	public void setContent(MailContent content) {
		this.content = content;
	}
	
	public void setContent(String subject, String body, File... attachments) {
		List<File> attachmentList = new ArrayList<File>();
		if(attachments != null && attachments.length > 0) {
			for(File attachment:attachments) {
				if(attachment != null && attachment.exists()) {
					attachmentList.add(attachment);
				}
			}
		}
		content = new SimpleMailContent(subject, body, attachmentList);
	}
}
