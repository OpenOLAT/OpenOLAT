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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.util.mail;

import java.util.UUID;

/**
 * A mail package is the sum of the template, the context and it's result.
 * All or part of thesse can be null.
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MailPackage {
	
	private final String uuid;
	private final boolean sendEmail;
	private final MailTemplate template;
	private final MailContext context;
	private final MailerResult result;
	
	public MailPackage() {
		this(true);
	}
	
	public MailPackage(boolean sendMail) {
		this(null, new MailerResult(), (MailContext)null, sendMail);
	}
	
	public MailPackage(MailTemplate template, MailContext context) {
		this(template, new MailerResult(), context, true);
	}
	
	public MailPackage(MailTemplate template, String businessPath, boolean sendMail) {
		this(template, new MailerResult(), new MailContextImpl(null, null, businessPath), sendMail);
	}
	
	public MailPackage(MailerResult result, String businessPath, boolean sendMail) {
		this(null, result, new MailContextImpl(null, null, businessPath), sendMail);
	}
	
	public MailPackage(MailTemplate template, MailerResult result, String businessPath, boolean sendMail) {
		this(template, result, new MailContextImpl(null, null, businessPath), sendMail);
	}
	
	public MailPackage(MailTemplate template, MailerResult result, MailContext context, boolean sendMail) {
		this.sendEmail = sendMail;
		this.template = template;
		this.context = context;
		this.result = result;
		this.uuid = UUID.randomUUID().toString();
	} 


	/**
	 * Default is true, you want to send mails. But in rare case, this flag
	 * give you the possibility to skip the mails.
	 * @return
	 */
	public boolean isSendEmail() {
		return sendEmail;
	}

	public String getUuid() {
		return uuid;
	}

	public MailTemplate getTemplate() {
		return template;
	}
	
	public MailContext getContext() {
		return context;
	}
	
	public MailerResult getResult() {
		return result;
	}

	public void appendResult(MailerResult newResult) {
		if(result != null) {
			result.append(newResult);
		}
	}
}
